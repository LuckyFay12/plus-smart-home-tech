package ru.yandex.practicum.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.exception.ProductNotFoundException;

import java.util.UUID;

public interface ShoppingStoreService {

    ProductDto createNewProduct(ProductDto productDto);

    ProductDto updateProduct(ProductDto productDto) throws ProductNotFoundException;

    ProductDto getProduct(UUID productId) throws ProductNotFoundException;

    Page<ProductDto> getProducts(ProductCategory category, Pageable pageable);

    void removeProductFromStore(UUID productId) throws ProductNotFoundException;

    void setProductQuantityState(SetProductQuantityStateRequest request) throws ProductNotFoundException;
}

