//package ru.yandex.practicum.telemetry.analyzer.handler;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;
//import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
//import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
//import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
//import ru.yandex.practicum.telemetry.analyzer.dal.*;
//import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
//import ru.yandex.practicum.telemetry.analyzer.repository.ConditionRepository;
//import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
//
//import java.util.Map;
//import java.util.Optional;
//
//import static java.util.stream.Collectors.toMap;
//
//@Slf4j
//@Component
//@RequiredArgsConstructor
//public class ScenarioAddedHandler implements HubEventHandler {
//
//    private final ScenarioRepository scenarioRepository;
//    private final ActionRepository actionRepository;
//    private final ConditionRepository conditionRepository;
//
//    @Override
//    public Class<?> getPayloadClass() {
//        return ScenarioAddedEventAvro.class;
//    }
//
//    @Override
//    public void handle(HubEventAvro event) {
//        String hubId = event.getHubId();
//        ScenarioAddedEventAvro scenarioAddedEventAvro = (ScenarioAddedEventAvro) event.getPayload();
//        String scenarioName = scenarioAddedEventAvro.getName();
//
//        Map<String, Action> actions = scenarioAddedEventAvro.getActions().stream()
//                .collect(toMap(DeviceActionAvro::getSensorId, action -> Action.builder()
//                        .type(ActionType.valueOf(action.getType().name()))
//                        .value(action.getValue())
//                        .build()));
//
//        Map<String, Condition> conditions = scenarioAddedEventAvro.getConditions().stream()
//                .collect(toMap(ScenarioConditionAvro::getSensorId, condition -> {
//                    Object value = condition.getValue();
//                    return Condition.builder()
//                            .operation(ConditionOperation.valueOf(condition.getOperation().name()))
//                            .type(ConditionType.valueOf(condition.getType().name()))
//                            .value(value instanceof Integer ? (Integer) value : value.equals(Boolean.TRUE) ? 1 : 0)
//                            .build();
//                }));
//
//        Optional<Scenario> oldScenario = scenarioRepository.findByHubIdAndName(event.getHubId(), scenarioAddedEventAvro.getName());
//        if (oldScenario.isPresent()) {
//            updateScenario(oldScenario.get(), actions, conditions);
//            return;
//        }
//
//        Scenario scenario = Scenario.builder()
//                .hubId(hubId)
//                .name(scenarioName)
//                .actions(actions)
//                .conditions(conditions)
//                .build();
//        log.info("Saving scenario {}", scenario);
//        scenarioRepository.saveAndFlush(scenario);
//    }
//
//    private void updateScenario(Scenario scenario,
//                                Map<String, Action> actions,
//                                Map<String, Condition> conditions) {
//        log.info("Updating scenario {}", scenario);
//
//        actionRepository.deleteAll(scenario.getActions().values());
//        conditionRepository.deleteAll(scenario.getConditions().values());
//
//        scenario.setActions(actions);
//        scenario.setConditions(conditions);
//
//        scenarioRepository.saveAndFlush(scenario);
//    }
//}