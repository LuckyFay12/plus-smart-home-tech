package ru.yandex.practicum.telemetry.collector.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.telemetry.collector.model.HubEvent;
import ru.yandex.practicum.telemetry.collector.model.HubEventType;
import ru.yandex.practicum.telemetry.collector.model.SensorEvent;
import ru.yandex.practicum.telemetry.collector.model.SensorEventType;
import ru.yandex.practicum.telemetry.collector.service.handler.HubEventHandler;
import ru.yandex.practicum.telemetry.collector.service.handler.SensorEventHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Validated
@RequestMapping("/events")
public class EventController {
    private final Map<SensorEventType, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventType, HubEventHandler> hubEventHandlers;

    public EventController(List<SensorEventHandler> sensorEventHandlers, List<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream()
                .collect(Collectors.toMap(HubEventHandler::getMessageType, Function.identity()));
    }

    @PostMapping("/sensors")
    public void collectSensorEvent(@RequestBody @Valid SensorEvent request) {
        log.info("json: {}", request.toString());
        SensorEventHandler sensorEventHandler = sensorEventHandlers.get(request.getType());
        if (sensorEventHandler == null) {
            throw new IllegalArgumentException("Неизвестный обработчик для события: " + request.getType());
        }
        sensorEventHandler.handle(request);
    }

    @PostMapping("/hubs")
    public void collectHubEvent(@RequestBody @Valid HubEvent request) {
        log.info("json: {}", request.toString());
        HubEventHandler hubEventHandler = hubEventHandlers.get(request.getType());
        if (hubEventHandler == null) {
            throw new IllegalArgumentException("Неизвестный обработчик для события: " + request.getType());
        }
        hubEventHandler.handle(request);
    }
}
