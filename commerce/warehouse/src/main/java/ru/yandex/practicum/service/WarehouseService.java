package ru.yandex.practicum.service;

import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;

public interface WarehouseService {
    void addNewProductToWarehouse(NewProductInWarehouseRequest request);

    BookedProductsDto checkProductAvailability(ShoppingCartDto cart);

    void takeProductToWarehouse(AddProductToWarehouseRequest request);

    AddressDto getWarehouseAddress();
}
