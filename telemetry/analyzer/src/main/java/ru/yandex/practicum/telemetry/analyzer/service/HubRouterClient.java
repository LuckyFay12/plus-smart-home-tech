package ru.yandex.practicum.telemetry.analyzer.service;

import com.google.protobuf.Timestamp;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;
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
@Service
public class HubRouterClient {

    @GrpcClient("hub-router")
    private final HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient;

    public HubRouterClient(@GrpcClient("hub-router") HubRouterControllerGrpc.HubRouterControllerBlockingStub hubRouterClient) {
        this.hubRouterClient = hubRouterClient;
    }

    public void handleScenario(Scenario scenario) {
        log.trace("Получение сценария {} для отправки хабу {}.", scenario, scenario.getHubId());
        Instant time = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(time.getEpochSecond())
                .setNanos(time.getNano())
                .build();
        for (Map.Entry<String, Action> actions : scenario.getActions().entrySet()) {
            Action scenarioAction = actions.getValue();
            DeviceActionProto.Builder acctionBuilder = DeviceActionProto.newBuilder()
                    .setSensorId(actions.getKey())
                    .setType(ActionTypeProto.valueOf(scenarioAction.getType().name()));
            if (scenarioAction.getType().equals(ActionTypeAvro.SET_VALUE)) {
                acctionBuilder.setValue(scenarioAction.getValue());
            }
            try {
                log.info("Отправка сообщения хабу.");
                hubRouterClient.handleDeviceAction(DeviceActionRequest.newBuilder()
                        .setHubId(scenario.getHubId())
                        .setScenarioName(scenario.getName())
                        .setAction(acctionBuilder.build())
                        .setTimestamp(timestamp)
                        .build());
            } catch (Exception e) {
                log.error("Ошибка при отправке хабу {} действия {} для устройства {}.", scenario.getHubId(),
                        scenarioAction, scenarioAction.getId(), e);
            }
        }
    }
}