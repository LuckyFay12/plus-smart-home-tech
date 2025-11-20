package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.feign.ShoppingCartOperations;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.service.ShoppingCartService;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping-cart")
@RequiredArgsConstructor
@Slf4j
public class ShoppingCartController implements ShoppingCartOperations {

    private final ShoppingCartService shoppingCartService;

    @Override
    public ShoppingCartDto getShoppingCart(String username) {
        return shoppingCartService.getShoppingCart(username);
    }

    @Override
    public ShoppingCartDto addProductToCart(String username,
                                            Map<UUID, Long> request) {
        return shoppingCartService.addProductToCart(username, request);
    }

    @Override
    public void deactivatingUserCart(String username) {
        shoppingCartService.deactivatingUserCart(username);
    }

    @Override
    public ShoppingCartDto removeProductFromCart(String username,
                                                 List<UUID> productIds) {
        return shoppingCartService.removeProductFromCart(username, productIds);
    }

    @Override
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        return shoppingCartService.changeQuantity(username, request);
    }
}