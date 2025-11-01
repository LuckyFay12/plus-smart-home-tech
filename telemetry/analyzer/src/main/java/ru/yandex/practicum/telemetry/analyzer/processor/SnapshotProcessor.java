package ru.yandex.practicum.telemetry.analyzer.processor;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;
import ru.yandex.practicum.telemetry.analyzer.dal.Scenario;
import ru.yandex.practicum.telemetry.analyzer.service.HubRouterClient;
import ru.yandex.practicum.telemetry.analyzer.service.SnapshotAnalyzer;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class SnapshotProcessor {
    private final Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final List<String> topics;
    private final Duration pollTimeout;
    private final SnapshotAnalyzer snapshotAnalyzer;
    private final HubRouterClient hubRouterClient;

    public SnapshotProcessor(KafkaConfig config, SnapshotAnalyzer snapshotAnalyzer, HubRouterClient hubRouterClient) {
        final KafkaConfig.ConsumerConfig consumerConfig = config.getConsumers().get(this.getClass().getSimpleName());
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.topics = consumerConfig.getTopics();
        this.pollTimeout = consumerConfig.getPollTimeout();
        this.snapshotAnalyzer = snapshotAnalyzer;
        this.hubRouterClient = hubRouterClient;

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("ShutdownHook: Получен сигнал завершения работы консьюмера.");
            consumer.wakeup();
        }));
    }

    public void start() {
        try {
            log.trace("Подписка на топики {}.", topics);
            consumer.subscribe(topics);
            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(pollTimeout);
                int count = 0;
                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    log.trace("Обработка сообщения от хаба {} partition {} offset {}.",
                            record.key(), record.partition(), record.offset());
                    handleRecord(record.value());
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignores) {
            log.info("WakeupException: Завершение работы.");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от хабов", e);
        } finally {
            try {
                consumer.commitSync(offsets);
            } finally {
                log.info("Закрытие консьюмера.");
                consumer.close();
            }
        }
    }

    private void manageOffsets(ConsumerRecord<String, SensorsSnapshotAvro> record, int count,
                               KafkaConsumer<String, SensorsSnapshotAvro> consumer) {
        offsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );
        if (count % 100 == 0) {
            consumer.commitAsync(offsets, (committedOffsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}.", committedOffsets, exception);
                }
            });
        }
    }

    private void handleRecord(SensorsSnapshotAvro sensorsSnapshotAvro) {
        try {
            String hubId = sensorsSnapshotAvro.getHubId();
            List<Scenario> scenarios = snapshotAnalyzer.analyze(hubId, sensorsSnapshotAvro);
            for (Scenario scenario : scenarios) {
                hubRouterClient.handleScenario(scenario);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки события {}.", sensorsSnapshotAvro, e);
        }
    }
}