package yandex.practicum.telemetry.aggregator.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class SnapshotService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new HashMap<>();

    Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot = snapshots.getOrDefault(event.getHubId(),
                SensorsSnapshotAvro.newBuilder()
                        .setHubId(event.getHubId())
                        .setTimestamp(event.getTimestamp())
                        .setSensorsState(new HashMap<>())
                        .build());

        Map<String, SensorStateAvro> states = snapshot.getSensorsState();

        if (states.containsKey(event.getHubId())) {
            SensorStateAvro oldState = states.get(event.getHubId());

            if (oldState.getTimestamp().isAfter(event.getTimestamp()) ||
                oldState.getData().equals(event.getPayload())) {
                return Optional.empty();
            }
        }
        SensorStateAvro newState = SensorStateAvro.newBuilder()
                .setData(event.getPayload())
                .setTimestamp(event.getTimestamp())
                .build();

        states.put(event.getId(), newState);

        SensorsSnapshotAvro updatedSnapshot = SensorsSnapshotAvro.newBuilder(snapshot)
                .setTimestamp(event.getTimestamp())
                .build();

        snapshots.put(event.getHubId(), updatedSnapshot);

        return Optional.of(updatedSnapshot);
    }
}