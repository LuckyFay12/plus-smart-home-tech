package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.dal.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Transactional
public class SensorService {
    private final SensorRepository repository;

    public Sensor save(Sensor sensor) {
        return repository.save(sensor);
    }

    @Transactional(readOnly = true)
    public Optional<Sensor> findByIdAndHubId(String id, String hubId) {
        return repository.findByIdAndHubId(id, hubId);
    }

    public void delete(Sensor sensor) {
        repository.delete(sensor);
    }
}
