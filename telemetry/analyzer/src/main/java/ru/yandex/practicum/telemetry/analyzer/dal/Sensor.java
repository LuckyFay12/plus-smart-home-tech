package ru.yandex.practicum.telemetry.analyzer.dal;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "sensors")
public class Sensor {
    @Id
    private String id;

    @Column(name = "hub_id")
    private String hubId;
}