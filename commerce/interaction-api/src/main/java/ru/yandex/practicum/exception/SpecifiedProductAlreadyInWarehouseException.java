package ru.yandex.practicum.exception;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class SpecifiedProductAlreadyInWarehouseException extends RuntimeException {
    public SpecifiedProductAlreadyInWarehouseException(String message, @NotNull UUID productId) {
        super(message);
    }
}
