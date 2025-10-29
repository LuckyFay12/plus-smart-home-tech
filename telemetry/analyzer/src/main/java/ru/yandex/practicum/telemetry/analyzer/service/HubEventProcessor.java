package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.config.KafkaConfig;
import ru.yandex.practicum.telemetry.analyzer.dal.Sensor;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class HubEventProcessor implements Runnable {

    private final ScenarioService scenarioService;
    private final SensorService sensorService;
    private final List<String> topics;
    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final Duration pollTimeout;

    public HubEventProcessor(KafkaConfig kafkaConfig, SensorService sensorService, ScenarioService scenarioService) {
        this.sensorService = sensorService;
        this.scenarioService = scenarioService;
        KafkaConfig.ConsumerConfig consumerConfig = kafkaConfig.getConsumers().get(this.getClass().getSimpleName());
        this.consumer = new KafkaConsumer<>(consumerConfig.getProperties());
        this.pollTimeout = consumerConfig.getPollTimeout();
        this.topics = consumerConfig.getTopics();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Получен сигнал завершения. Останавливаем работу консьюмера.");
            consumer.wakeup();
        }));
    }

    @Override
    public void run() {
        try {
            consumer.subscribe(topics);
            log.info("Подписались на топики: {}", topics);

            while (true) {

                ConsumerRecords<String, HubEventAvro> records = consumer.poll(pollTimeout);

                if (!records.isEmpty()) {
                    log.debug("Получено {} записей", records.count());
                    processRecords(records);
                }
                consumer.commitSync();
            }

        } catch (WakeupException e) {
            log.info("Получен WakeupException - завершаем работу");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            consumer.close();
            log.info("Закрыт консьюмер");
        }
    }

    private void processRecords(ConsumerRecords<String, HubEventAvro> records) {
        for (ConsumerRecord<String, HubEventAvro> record : records) {
            try {
                HubEventAvro hubEvent = record.value();
                log.debug("Обрабатывается событие из топика {}: {}", record.topic(), hubEvent);
                switch (hubEvent.getPayload()) {
                    case DeviceAddedEventAvro deviceAdded -> handleDeviceAdded(hubEvent.getHubId(), deviceAdded);
                    case DeviceRemovedEventAvro deviceRemoved ->
                            handleDeviceRemoved(hubEvent.getHubId(), deviceRemoved);
                    case ScenarioAddedEventAvro scenarioAdded ->
                            handleScenarioAdded(hubEvent.getHubId(), scenarioAdded);
                    case ScenarioRemovedEventAvro scenarioRemoved ->
                            handleScenarioRemoved(hubEvent.getHubId(), scenarioRemoved);
                    default -> log.warn("Получено событие неизвестного типа: {}", hubEvent);
                }
            } catch (Exception e) {
                log.error("Не удалось обработать запись: {}", record, e);
            }
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro event) {
        Optional<Sensor> existingSensor = sensorService.findByIdAndHubId(event.getId(), hubId);
        if (existingSensor.isPresent()) {
            log.info("Датчик [{}] уже зарегистрирован в хабе [{}]", event.getId(), hubId);
            return;
        }

        Sensor newSensor = new Sensor();
        newSensor.setHubId(hubId);
        newSensor.setId(event.getId());
        sensorService.save(newSensor);

        log.debug("Зарегистрирован новый датчик [{}] в хабе [{}]", event.getId(), hubId);
    }

    private void handleDeviceRemoved(String hubId, DeviceRemovedEventAvro event) {
        sensorService.findByIdAndHubId(event.getId(), hubId)
                .ifPresentOrElse(
                        sensor -> {
                            sensorService.delete(sensor);
                            log.debug("Датчик {} удален из хаба [{}]", event.getId(), hubId);
                        },
                        () -> log.warn("Датчик {} не найден в хабе [{}] для удаления", event.getId(), hubId)
                );
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro event) {
        log.info("Добавление сценария '{}' для хаба {}", event.getName(), hubId);
        scenarioService.save(event, hubId);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro event) {
        log.info("Удаление сценария '{}' из хаба {}", event.getName(), hubId);
        scenarioService.delete(event.getName(), hubId);
    }
}



