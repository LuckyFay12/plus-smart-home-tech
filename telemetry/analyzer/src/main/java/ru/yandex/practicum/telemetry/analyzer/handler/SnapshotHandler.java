//package ru.yandex.practicum.telemetry.analyzer.handler;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.kafka.telemetry.event.*;
//import ru.yandex.practicum.telemetry.analyzer.client.HubRouterClient;
//import ru.yandex.practicum.telemetry.analyzer.dal.Action;
//import ru.yandex.practicum.telemetry.analyzer.dal.Condition;
//import ru.yandex.practicum.telemetry.analyzer.dal.Scenario;
//import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
//import ru.yandex.practicum.telemetry.analyzer.repository.ConditionRepository;
//import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class SnapshotHandler {
//    private final ConditionRepository conditionRepository;
//    private final ScenarioRepository scenarioRepository;
//    private final ActionRepository actionRepository;
//    private final HubRouterClient hubRouterClient;
//
//    public void buildSnapshot(SensorsSnapshotAvro sensorsSnapshot) {
//        log.info("Начало обработки снапшота для хаба {}", sensorsSnapshot.getHubId());
//
//        try {
//            Map<String, SensorStateAvro> sensorStateMap = sensorsSnapshot.getSensorsState();
//            List<Scenario> scenarios = scenarioRepository.findScenariosByHubId(sensorsSnapshot.getHubId());
//
//            scenarios.forEach(scenario -> {
//                if (handleScenario(scenario, sensorStateMap)) {
//                    log.info("Условия сценария '{}' выполнены", scenario.getName());
//                    sendScenarioActions(scenario);
//                }
//            });
//        } catch (Exception e) {
//            log.error("Ошибка обработки снапшота", e);
//        }
//    }
//
//    private boolean handleScenario(Scenario scenario, Map<String, SensorStateAvro> sensorStateMap) {
//        List<Condition> conditions = conditionRepository.findConditionsByScenario(scenario);
//        return conditions.stream()
//                .allMatch(condition -> checkCondition(condition, sensorStateMap));
//    }
//
//    private boolean checkCondition(Condition condition, Map<String, SensorStateAvro> sensorStateMap) {
//        String sensorId = condition.getSensor().getId();
//        SensorStateAvro sensorState = sensorStateMap.get(sensorId);
//        if (sensorState == null) {
//            return false;
//        }
//
//        switch (condition.getType()) {
//            case LUMINOSITY -> {
//                LightSensorAvro lightSensor = (LightSensorAvro) sensorState.getData();
//                return Boolean.TRUE.equals(handleOperation(condition, lightSensor.getLuminosity()));
//            }
//            case TEMPERATURE -> {
//                ClimateSensorAvro temperatureSensor = (ClimateSensorAvro) sensorState.getData();
//                return Boolean.TRUE.equals(handleOperation(condition, temperatureSensor.getTemperatureC()));
//            }
//            case MOTION -> {
//                MotionSensorAvro motionSensor = (MotionSensorAvro) sensorState.getData();
//                return Boolean.TRUE.equals(handleOperation(condition, motionSensor.getMotion() ? 1 : 0));
//            }
//            case SWITCH -> {
//                SwitchSensorAvro switchSensor = (SwitchSensorAvro) sensorState.getData();
//                return Boolean.TRUE.equals(handleOperation(condition, switchSensor.getState() ? 1 : 0));
//            }
//            case CO2LEVEL -> {
//                ClimateSensorAvro climateSensor = (ClimateSensorAvro) sensorState.getData();
//                return Boolean.TRUE.equals(handleOperation(condition, climateSensor.getCo2Level()));
//            }
//            case HUMIDITY -> {
//                ClimateSensorAvro climateSensor = (ClimateSensorAvro) sensorState.getData();
//                return Boolean.TRUE.equals(handleOperation(condition, climateSensor.getHumidity()));
//            }
//            case null -> {
//                return false;
//            }
//        }
//    }
//
//    private Boolean handleOperation(Condition condition, Integer currentValue) {
//        ConditionOperationAvro conditionOperation = condition.getOperation();
//        Integer targetValue = condition.getValue();
//
//        switch (conditionOperation) {
//            case EQUALS -> {
//                return Objects.equals(targetValue, currentValue);
//            }
//            case LOWER_THAN -> {
//                return currentValue < targetValue;
//            }
//            case GREATER_THAN -> {
//                return currentValue > targetValue;
//            }
//            case null -> {
//                return null;
//            }
//        }
//    }
//
//    private void sendScenarioActions(Scenario scenario) {
//        try {
//            List<Action> actions = actionRepository.findActionsByScenario(scenario);
//            log.info("Отправка {} действий для сценария {}", actions.size(), scenario.getName());
//
//            actions.forEach(action -> {
//                try {
//                    hubRouterClient.sendRequest(action);
//                    log.debug("Действие с ID: {}, успешно отправлено", action.getId());
//                } catch (Exception e) {
//                    log.error("Ошибка отправки действия с ID: {}", action.getId(), e);
//                }
//            });
//        } catch (Exception e) {
//            log.error("Ошибка получения действий для сценария {}", scenario.getName(), e);
//        }
//    }
//}