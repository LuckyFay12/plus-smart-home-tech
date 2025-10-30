//package ru.yandex.practicum.telemetry.analyzer.handler;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Component;
//import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
//import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
//import ru.yandex.practicum.telemetry.analyzer.repository.SensorRepository;
//
//@Component
//@RequiredArgsConstructor
//public class DeviceRemovedHandler implements HubEventHandler {
//
//    private final SensorRepository sensorRepository;
//
//    @Override
//    public Class<?> getPayloadClass() {
//        return DeviceRemovedEventAvro.class;
//    }
//
//    @Override
//    public void handle(HubEventAvro event) {
//        DeviceRemovedEventAvro payload = (DeviceRemovedEventAvro) event.getPayload();
//        String sensorId = payload.getId();
//
//        sensorRepository.deleteById(sensorId);
//    }
//}