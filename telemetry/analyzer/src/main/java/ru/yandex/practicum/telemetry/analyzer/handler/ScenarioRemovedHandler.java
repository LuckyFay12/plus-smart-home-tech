//package ru.yandex.practicum.telemetry.analyzer.handler;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
//import ru.yandex.practicum.kafka.telemetry.event.ScenarioRemovedEventAvro;
//import ru.yandex.practicum.telemetry.analyzer.dal.Scenario;
//import ru.yandex.practicum.telemetry.analyzer.repository.ActionRepository;
//import ru.yandex.practicum.telemetry.analyzer.repository.ConditionRepository;
//import ru.yandex.practicum.telemetry.analyzer.repository.ScenarioRepository;
//
//import java.util.Optional;
//
//@Component
//@RequiredArgsConstructor
//public class ScenarioRemovedHandler implements HubEventHandler {
//
//    private final ScenarioRepository scenarioRepository;
//    private final ActionRepository actionRepository;
//    private final ConditionRepository conditionRepository;
//
//    @Override
//    public Class<?> getPayloadClass() {
//        return ScenarioRemovedEventAvro.class;
//    }
//
//    @Override
//    public void handle(HubEventAvro event) {
//        ScenarioRemovedEventAvro scenarioRemovedEventAvro = (ScenarioRemovedEventAvro) event.getPayload();
//        String name = scenarioRemovedEventAvro.getName();
//
//        Optional<Scenario> optScenario = scenarioRepository.findByHubIdAndName(event.getHubId(), name);
//        Scenario scenario;
//        if (optScenario.isEmpty()) {
//            return;
//        } else {
//            scenario = optScenario.get();
//        }
//
//        conditionRepository.deleteAll(scenario.getConditions().values());
//        actionRepository.deleteAll(scenario.getActions().values());
//        scenarioRepository.delete(scenario);
//    }
//}