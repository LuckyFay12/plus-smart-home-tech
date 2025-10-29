package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.dal.Condition;
import ru.yandex.practicum.telemetry.analyzer.dal.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SnapshotAnalyzer {

    private final ScenarioRepository scenarioRepository;

    public List<Scenario> analyze(String hubId, SensorsSnapshotAvro sensorsSnapshotAvro) {
        List<Scenario> allScenarios = scenarioRepository.findByHubId(hubId);

        if (allScenarios.isEmpty()) {
            return Collections.emptyList();
        }

        List<Scenario> triggeredScenarios = allScenarios.stream()
                .filter(scenario -> isScenarioRunning(scenario, sensorsSnapshotAvro))
                .peek(scenario -> log.info("Сценарий '{}' для хаба {} активирован",
                        scenario.getName(), hubId))
                .collect(Collectors.toList());

        return triggeredScenarios;
    }

    private boolean isScenarioRunning(Scenario scenario, SensorsSnapshotAvro sensorsSnapshot) {
        Map<String, Condition> conditions = scenario.getConditions();
        // Если нет условий - сценарий всегда выполняется
        if (conditions.isEmpty()) {
            log.info("Сценарий {} не имеет условий, выполняется", scenario.getName());
            return true;
        }

        boolean allConditionsMet = conditions.entrySet().stream()
                .allMatch(entry -> evaluateConditions(entry.getKey(), entry.getValue(), sensorsSnapshot));
        if (allConditionsMet) {
            log.info("Все условия выполнены, сценарий {} активирован", scenario.getName());
            return true;
        }
        return false;
    }

    private boolean evaluateConditions(String sensorId, Condition condition, SensorsSnapshotAvro snapshot) {
        SensorStateAvro sensorState = findSensorInSnapshot(sensorId, snapshot);
        if (sensorState == null) {
            log.warn("Сенсор {} не найден в снапшоте", sensorId);
            return false;
        }
        // Получаем значение сенсора в зависимости от типа условия
        Integer sensorValue = extractSensorValue(sensorState, condition.getType());
        if (sensorValue == null) {
            log.warn("Не удалось получить значение для типа условия {} из сенсора {}", condition.getType(), sensorId);
            return false;
        }
        return checkConditionValue(sensorValue, condition.getOperation(), condition.getValue());
    }

    private SensorStateAvro findSensorInSnapshot(String sensorId, SensorsSnapshotAvro snapshot) {
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        if (sensorsState == null) {
            return null;
        }
        return sensorsState.get(sensorId);
    }

    private Integer extractSensorValue(SensorStateAvro sensorState, ConditionTypeAvro conditionType) {
        Object sensorData = sensorState.getData();

        return switch (conditionType) {
            case TEMPERATURE -> extractTemperature(sensorData);
            case HUMIDITY -> extractHumidity(sensorData);
            case CO2LEVEL -> extractCo2Level(sensorData);
            case LUMINOSITY -> extractLuminosity(sensorData);
            case MOTION -> extractMotion(sensorData);
            case SWITCH -> extractSwitchState(sensorData);
        };
    }

    private Integer extractTemperature(Object sensorData) {
        if (sensorData instanceof ClimateSensorAvro climateSensor) {
            return climateSensor.getTemperatureC();
        } else if (sensorData instanceof TemperatureSensorAvro temperatureSensor) {
            return temperatureSensor.getTemperatureC();
        }
        return null;
    }

    private Integer extractHumidity(Object sensorData) {
        return (sensorData instanceof ClimateSensorAvro climateSensor) ?
                climateSensor.getHumidity() : null;
    }

    private Integer extractCo2Level(Object sensorData) {
        return (sensorData instanceof ClimateSensorAvro climateSensor) ?
                climateSensor.getCo2Level() : null;
    }

    private Integer extractLuminosity(Object sensorData) {
        return (sensorData instanceof LightSensorAvro lightSensor) ?
                lightSensor.getLuminosity() : null;
    }

    private Integer extractMotion(Object sensorData) {
        return (sensorData instanceof MotionSensorAvro motionSensor) ?
                (motionSensor.getMotion() ? 1 : 0) : null;
    }

    private Integer extractSwitchState(Object sensorData) {
        return (sensorData instanceof SwitchSensorAvro switchSensor) ?
                (switchSensor.getState() ? 1 : 0) : null;
    }

    private boolean checkConditionValue(Integer sensorValue, ConditionOperationAvro operation, Integer conditionValue) {
        if (sensorValue == null || conditionValue == null) {
            return false;
        }

        return switch (operation) {
            case EQUALS -> sensorValue.equals(conditionValue);
            case GREATER_THAN -> sensorValue > conditionValue;
            case LOWER_THAN -> sensorValue < conditionValue;
        };
    }
}


