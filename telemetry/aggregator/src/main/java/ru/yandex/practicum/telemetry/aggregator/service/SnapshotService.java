package ru.yandex.practicum.telemetry.aggregator.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class SnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        String hubId = String.valueOf(event.getHubId());
        String sensorId = String.valueOf(event.getId());

        log.debug("Получено событие: hubId={}, sensorId={}, timestamp={}",
                hubId, sensorId, event.getTimestamp());

        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(
                hubId,
                key -> {
                    log.info("Создаём новый снапшот для хаба: {}", key);
                    SensorsSnapshotAvro newSnapshot = new SensorsSnapshotAvro();
                    newSnapshot.setHubId(key);
                    newSnapshot.setTimestamp(Instant.EPOCH);
                    newSnapshot.setSensorsState(new HashMap<>());
                    return newSnapshot;
                }
        );

        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        SensorStateAvro oldState = sensorsState.get(sensorId);

        boolean shouldUpdate = false;

        if (oldState == null) {
            log.debug("Новый датчик, добавляем в снапшот");
            shouldUpdate = true;
        } else {
            // Проверяем timestamp: событие должно быть не старше
            if (event.getTimestamp().isBefore(oldState.getTimestamp())) {
                log.debug("Событие устарело (timestamp старше), пропускаем");
                return Optional.empty();
            }

            if (!oldState.getData().equals(event.getPayload())) {
                log.debug("Данные изменились, обновляем снапшот");
                shouldUpdate = true;
            } else {
                return Optional.empty();
            }
        }

        if (!shouldUpdate) {
            return Optional.empty();
        }

        SensorStateAvro newState = new SensorStateAvro();
        newState.setTimestamp(event.getTimestamp());
        newState.setData(event.getPayload());

        sensorsState.put(sensorId, newState);
        snapshot.setTimestamp(event.getTimestamp());

        log.info("Снапшот обновлён для хаба: {}, сенсор: {}", hubId, sensorId);
        return Optional.of(snapshot);
    }
}