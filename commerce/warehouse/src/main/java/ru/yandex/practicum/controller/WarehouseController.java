package ru.yandex.practicum.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.feign.WarehouseOperations;
import ru.yandex.practicum.service.WarehouseService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/warehouse")
public class WarehouseController implements WarehouseOperations {

    private final WarehouseService service;

    @Override
    public void addNewProductToWarehouse(NewProductInWarehouseRequest request) {
        service.addNewProductToWarehouse(request);
    }

    @Override
    public void checkProductAvailability(ShoppingCartDto cart) {
        BookedProductsDto dto = service.checkProductAvailability(cart);
    }

    @Override
    public void takeProductToWarehouse(AddProductToWarehouseRequest request) {
        service.takeProductToWarehouse(request);
    }

    @Override
    public AddressDto getWarehouseAddress() {
        return service.getWarehouseAddress();
    }
}