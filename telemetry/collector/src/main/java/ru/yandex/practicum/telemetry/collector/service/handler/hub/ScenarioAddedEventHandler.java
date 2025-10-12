package ru.yandex.practicum.telemetry.collector.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.telemetry.collector.model.*;
import ru.yandex.practicum.telemetry.collector.service.KafkaEventProducer;

import java.util.List;

@Component
public class ScenarioAddedEventHandler extends BaseHubEventHandler<ScenarioAddedEventAvro> {
    public ScenarioAddedEventHandler(KafkaEventProducer producer) {
        super(producer);
    }

    @Override
    public HubEventType getMessageType() {
        return HubEventType.SCENARIO_ADDED;
    }

    @Override
    protected ScenarioAddedEventAvro mapToAvro(HubEvent event) {
        ScenarioAddedEvent scenarioAddedEvent = (ScenarioAddedEvent) event;


        List<ScenarioConditionAvro> conditions = scenarioAddedEvent.getConditions().stream().map(this::mapToConditionAvro).toList();
        List<DeviceActionAvro> actions = scenarioAddedEvent.getActions().stream().map(this::mapToActionAvro).toList();

        return ScenarioAddedEventAvro.newBuilder()
                .setName(scenarioAddedEvent.getName())
                .setActions(actions)
                .setConditions(conditions)
                .build();
    }

    private ScenarioConditionAvro mapToConditionAvro(ScenarioCondition scenarioCondition) {
        ConditionTypeAvro conditionTypeAvro = ConditionTypeAvro.valueOf(scenarioCondition.getType().name());
        ConditionOperationAvro conditionOperationAvro = ConditionOperationAvro.valueOf(scenarioCondition.getOperation().name());

        return ScenarioConditionAvro.newBuilder()
                .setType(conditionTypeAvro)
                .setSensorId(scenarioCondition.getSensorId())
                .setValue(scenarioCondition.getValue())
                .setOperation(conditionOperationAvro)
                .build();
    }

    private DeviceActionAvro mapToActionAvro(DeviceAction deviceAction) {
        ActionTypeAvro actionTypeAvro = ActionTypeAvro.valueOf(deviceAction.getType().name());

        return DeviceActionAvro.newBuilder()
                .setType(actionTypeAvro)
                .setSensorId(deviceAction.getSensorId())
                .setValue(deviceAction.getValue())
                .build();
    }
}

