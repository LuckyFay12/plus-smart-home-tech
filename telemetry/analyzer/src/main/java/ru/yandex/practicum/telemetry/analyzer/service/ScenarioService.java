package ru.yandex.practicum.telemetry.analyzer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.analyzer.dal.Action;
import ru.yandex.practicum.telemetry.analyzer.dal.Condition;
import ru.yandex.practicum.telemetry.analyzer.dal.Scenario;
import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ConditionRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;

import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScenarioService {
    private final ScenarioRepository scenarioRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final SensorRepository sensorRepository;

    public Scenario save(ScenarioAddedEventAvro event, String hubId) {
        log.info("Начало сохранения сценария '{}' для хаба {}", event.getName(), hubId);
        Set<String> sensors = new HashSet<>();

        for (ScenarioConditionAvro condition : event.getConditions()) {
            sensors.add(condition.getSensorId());
        }
        for (DeviceActionAvro action : event.getActions()) {
            sensors.add(action.getSensorId());
        }
        log.debug("Проверка существования сенсоров: {}", sensors);
        boolean allSensorsExists = sensorRepository.existsByIdInAndHubId(sensors, hubId);
        if (!allSensorsExists) {
            throw new IllegalStateException("Обнаружены несуществующие устройства в запросе на создание сценария");
        }

        Optional<Scenario> existingScenario = scenarioRepository.findByHubIdAndName(hubId, event.getName());

        Scenario scenario;

        if (existingScenario.isEmpty()) {
            log.debug("Создание нового сценария '{}' для хаба {}", event.getName(), hubId);
            scenario = new Scenario();
            scenario.setName(event.getName());
            scenario.setHubId(hubId);
            scenario.setConditions(new HashMap<>());
            scenario.setActions(new HashMap<>());
        } else {
            scenario = existingScenario.get();
            log.debug("Удаление {} условий и {} действий сценария",
                    scenario.getConditions().size(), scenario.getActions().size());
            conditionRepository.deleteAll(scenario.getConditions().values());
            actionRepository.deleteAll(scenario.getActions().values());
            scenario.getConditions().clear();
            scenario.getActions().clear();
        }

        log.debug("Добавление {} условий сценария", event.getConditions().size());
        for (ScenarioConditionAvro eventCondition : event.getConditions()) {
            Condition condition = new Condition();
            condition.setType(eventCondition.getType());
            condition.setOperation(eventCondition.getOperation()); // напрямую используем enum
            condition.setValue(extractValue(eventCondition.getValue()));

            Condition savedCondition = conditionRepository.save(condition);
            scenario.getConditions().put(eventCondition.getSensorId(), savedCondition);
        }

        log.debug("Добавление {} действий сценария", event.getActions().size());
        for (DeviceActionAvro eventAction : event.getActions()) {
            Action action = new Action();
            action.setType(eventAction.getType());
            if (eventAction.getType().equals(ActionTypeAvro.SET_VALUE)) {
                action.setValue(extractValue(eventAction.getValue()));
            }

            // Сохраняем действие и добавляем в сценарий
            Action savedAction = actionRepository.save(action);
            scenario.getActions().put(eventAction.getSensorId(), savedAction);
        }

        return scenarioRepository.save(scenario);
    }

    public void delete(String name, String hubId) {
        log.info("Запрос на удаление сценария '{}' из хаба {}", name, hubId);
        Optional<Scenario> optScenario = scenarioRepository.findByHubIdAndName(hubId, name);
        if (optScenario.isPresent()) {
            Scenario scenario = optScenario.get();
            conditionRepository.deleteAll(scenario.getConditions().values());
            actionRepository.deleteAll(scenario.getActions().values());
            scenarioRepository.delete(scenario);
            log.info("Сценарий '{}' успешно удален из хаба {}", name, hubId);
        } else {
            log.warn("Сценарий '{}' не найден в хабе {} для удаления", name, hubId);
        }
    }

    private Integer extractValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Integer integerValue) {
            return integerValue;
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? 1 : 0;
        }
        log.debug("Необрабатываемый тип значения: {}", value.getClass().getSimpleName());
        return null;
    }
}


