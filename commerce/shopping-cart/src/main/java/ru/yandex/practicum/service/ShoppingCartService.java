package ru.yandex.practicum.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartService {

    ShoppingCartDto addProductToCart(String username, Map<UUID, Long> request);

    ShoppingCartDto getShoppingCart(String username);

    void deactivatingUserCart(String username);

    ShoppingCartDto removeProductFromCart(String username, @Valid @NotNull @NotEmpty List<UUID> productIds);

    ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request);
}
