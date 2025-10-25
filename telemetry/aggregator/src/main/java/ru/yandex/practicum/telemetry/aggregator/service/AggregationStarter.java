package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Duration;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    @Value("${app.kafka.topics.sensor-events}")
    private String sensorTopic;

    @Value("${app.kafka.topics.snapshots}")
    private String snapshotsTopic;

    private final SnapshotService snapshotService;

    private final KafkaConsumer<String, SensorEventAvro> consumer;

    private final KafkaProducer<String, Object> producer;

    /**
     * Метод для начала процесса агрегации данных.
     * Подписывается на топики для получения событий от датчиков,
     * формирует снимок их состояния и записывает в кафку.
     */
    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения. Останавливаем агрегатор.");
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(Collections.singletonList(sensorTopic));

            log.info("Подписались на топик: {}", sensorTopic);

            while (true) {

                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.debug("Обрабатываем событие: {}", record.value());
                    snapshotService.updateState(record.value())
                            .ifPresent(snapshot -> {
                                log.info("Отправляем обновленный снапшот для хаба: {}", snapshot.getHubId());
                                producer.send(new ProducerRecord<>(snapshotsTopic, snapshot.getHubId(), snapshot));
                            });
                }
                consumer.commitSync();
            }
        } catch (WakeupException ignored) {
            log.info("Получен WakeupException - завершаем работу");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {

            try {
                log.info("Сбрасываем данные из буфера продюсера");
                producer.flush();
                log.info("Фиксируем оффсеты обработанных сообщений.");
                consumer.commitSync();
                log.info("Все данные сохранены, можно завершать работу");

            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}
