package ru.yandex.practicum.feign;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ShoppingCartOperations {

//    @CircuitBreaker(name = "myCircuitBreaker", fallbackMethod = "recoverMethod")
    @GetMapping
    ShoppingCartDto getShoppingCart(@RequestParam String username);

    @PutMapping
    ShoppingCartDto addProductToCart(@RequestParam("username") String username,
                                     @RequestBody Map<UUID, Long> request);

    @DeleteMapping
    void deactivatingUserCart(@RequestParam("username") String username);

    @PostMapping("/remove")
    ShoppingCartDto removeProductFromCart(@RequestParam("username") String username,
                                          @RequestBody List<UUID> productIds);

    @PostMapping("/change-quantity")
    ShoppingCartDto changeQuantity(@RequestParam("username") String username,
                                   @RequestBody @Valid ChangeProductQuantityRequest request);
    }
