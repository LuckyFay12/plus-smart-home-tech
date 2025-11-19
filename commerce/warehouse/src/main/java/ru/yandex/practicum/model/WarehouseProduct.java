package ru.yandex.practicum.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Builder
public class WarehouseProduct {

    @Id
    @Column(name = "product_id")
    private UUID productId;

    @NotNull(message = "Поле fragile не может быть null")
    private Boolean fragile;

    @Positive(message = "Вес должен быть положительным числом")
    @NotNull(message = "Вес не может быть null")
    private Double weight;

    @NotNull(message = "Размеры не могут быть null")
    @Embedded
    private Dimension dimension;

    @PositiveOrZero(message = "Количество не может быть отрицательным")
    private Long quantity;
}