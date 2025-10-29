package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;
import ru.yandex.practicum.telemetry.analyzer.dal.Scenario;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final SnapshotAnalyzer analyzer;
    private final HubEventClient hubEventClient;
    private static final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final List<String> topics;
    private final Duration pollTimeout;


    @Autowired
    public SnapshotProcessor(SnapshotAnalyzer analyzer, KafkaConfig kafkaConfig, ScenarioService scenarioService,
                             HubEventClient hubRouterClient) {
        this.analyzer = analyzer;
        this.hubEventClient = hubRouterClient;

        KafkaConfig.ConsumerConfig consumerConfig = kafkaConfig.getConsumers().get(this.getClass().getSimpleName());

        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.topics = consumerConfig.getTopics();
        this.pollTimeout = consumerConfig.getPollTimeout();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Сработал хук на завершение JVM. Прерываю работу консьюмера снапшотов. ");
            consumer.wakeup();
        }));
    }

    public void start() {
        try {
            consumer.subscribe(topics);
            log.info("Подписались на топики снапшотов: {}", topics);

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(pollTimeout);
                int count = 0;

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    processSnapshotRecord(record.value());
                    updateOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitSync();
            }

        } catch (WakeupException e) {
            log.info("Получен WakeupException - завершаем работу консьюмера снапшотов");
        } catch (Exception e) {
            log.error("Ошибка во время обработки снапшотов", e);
        } finally {
            consumer.close();
            log.info("Консьюмер снапшотов закрыт");
        }
    }

    private static void updateOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record, int count,
                                      KafkaConsumer<String, SensorsSnapshotAvro> consumer) {
        // обновляем текущий оффсет для топика-партиции
        offsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );
        if(count % 100 == 0) {
            consumer.commitAsync(offsets, (offsets, exception) -> {
                if(exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    private void processSnapshotRecord(SensorsSnapshotAvro sensorsSnapshotAvro) {
        try {
            String hubId = sensorsSnapshotAvro.getHubId();
            List<Scenario> scenarios = analyzer.analyze(hubId, sensorsSnapshotAvro);
            for (Scenario scenario : scenarios) {
                hubEventClient.sendScenarioCommands(scenario);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки события {}", sensorsSnapshotAvro, e);
        }
    }
}
