package ru.yandex.practicum.dto.store;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.enums.QuantityState;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ProductDto {

    private UUID productId;

    @NotBlank(message = "Название товара не может быть пустым")
    private String productName;

    @NotBlank(message = "Описание товара не может быть пустым")
    private String description;

    private String imageSrc;

    @NotNull(message = "Количество товара не может быть null")
    private QuantityState quantityState;

    @NotNull
    private ProductState productState;

    @NotNull(message = "Категория товара не может быть null")
    private ProductCategory productCategory;

    @NotNull(message = "Цена товара не может быть null")
    @Positive(message = "Цена товара должна быть положительным числом")
    private BigDecimal price;
}