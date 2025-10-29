package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;
import ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro;
import ru.yandex.practicum.telemetry.analyzer.dal.Action;
import ru.yandex.practicum.telemetry.analyzer.dal.Scenario;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
public class HubEventClient {
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public HubEventClient(@GrpcClient("hub-router")
                          HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
    }

    public void sendScenarioCommands(Scenario scenarioRule) {
        log.debug("Подготовка команд для сценария '{}' устройства {}",
                scenarioRule.getName(), scenarioRule.getHubId());

        Instant currentTime = Instant.now();
        Timestamp protocolTimestamp = Timestamp.newBuilder()
                .setSeconds(currentTime.getEpochSecond())
                .setNanos(currentTime.getNano())
                .build();

        for (Map.Entry<String, Action> commandEntry : scenarioRule.getActions().entrySet()) {
            executeDeviceCommand(scenarioRule, commandEntry.getKey(),
                    commandEntry.getValue(), protocolTimestamp);
        }
    }

    private void executeDeviceCommand(Scenario scenario, String sensorIdentifier,
                                      Action command, Timestamp timestamp) {
        try {
            DeviceActionProto.Builder commandBuilder = DeviceActionProto.newBuilder()
                    .setSensorId(sensorIdentifier)
                    .setType(convertActionType(command.getType()));

            if (requiresValueSetting(command.getType())) {
                commandBuilder.setValue(command.getValue());
            }

            DeviceActionRequest commandRequest = DeviceActionRequest.newBuilder()
                    .setHubId(scenario.getHubId())
                    .setScenarioName(scenario.getName())
                    .setAction(commandBuilder.build())
                    .setTimestamp(timestamp)
                    .build();

            log.info("Отправка команды управления устройством");
            hubRouterClient.handleDeviceAction(commandRequest);

        } catch (Exception transmissionError) {
            log.error("Сбой передачи команды устройству {} для датчика {}",
                    scenario.getHubId(), sensorIdentifier, transmissionError);
        }
    }

    private ActionTypeProto convertActionType(ActionTypeAvro avroType) {
        return ActionTypeProto.valueOf(avroType.name());
    }

    private boolean requiresValueSetting(ActionTypeAvro actionType) {
        return actionType.equals(ActionTypeAvro.SET_VALUE);
    }
}



