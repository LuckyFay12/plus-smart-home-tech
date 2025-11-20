package ru.yandex.practicum.feign;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

@Component
@Slf4j
public class WarehouseClientFallback implements WarehouseClient {

    @Override
    public void addNewProductToWarehouse(NewProductInWarehouseRequest request) {
        throw new RuntimeException("Warehouse service недоступен");
    }

    @Override
    public void checkProductAvailability(ShoppingCartDto cart) {
        log.warn("Warehouse service недоступен. Невозможно проверить наличие товара для корзины с Id: {}", cart.getShoppingCartId());
    }

    @Override
    public void takeProductToWarehouse(AddProductToWarehouseRequest request) {
        throw new RuntimeException("Warehouse service недоступен");
    }

    @Override
    public AddressDto getWarehouseAddress() {
        throw new RuntimeException("Warehouse service недоступен");
    }
}
