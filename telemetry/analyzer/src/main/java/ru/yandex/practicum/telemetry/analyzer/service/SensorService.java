package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.telemetry.analyzer.dal.Sensor;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository sensorRepository;

    @Transactional
    public void createSensor(String hubId, String sensorId) {
        Optional<Sensor> existingSensor = sensorRepository.findById(sensorId);
        if (existingSensor.isPresent()) {
            log.warn("Датчик {} уже существует.", sensorId);
            return;
        }

        Sensor sensor = new Sensor();
        sensor.setId(sensorId);
        sensor.setHubId(hubId);
        sensorRepository.save(sensor);
        log.info("Датчик {} для хаба {} сохранен.", sensorId, hubId);
    }

    @Transactional
    public void removeSensor(String hubId, String sensorId) {
        Optional<Sensor> sensor = sensorRepository.findById(sensorId);
        if (sensor.isPresent()) {
            if (!sensor.get().getHubId().equals(hubId)) {
                log.warn("Датчик {} не принадлежит хабу {}.", sensorId, hubId);
                return;
            }
            sensorRepository.delete(sensor.get());
            log.info("Датчик {} для хаба {} удален.", sensorId, hubId);
        } else {
            log.warn("Датчик {} не найден.", sensorId);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Sensor> findByIdAndHubId(String id, String hubId) {
        return sensorRepository.findByIdAndHubId(id, hubId);
    }
}

