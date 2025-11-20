package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.aspect.LogExecution;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.dto.warehouse.AddProductToWarehouseRequest;
import ru.yandex.practicum.dto.warehouse.AddressDto;
import ru.yandex.practicum.dto.warehouse.BookedProductsDto;
import ru.yandex.practicum.dto.warehouse.NewProductInWarehouseRequest;
import ru.yandex.practicum.exception.NoSpecifiedProductInWarehouseException;
import ru.yandex.practicum.exception.ProductInShoppingCartLowQuantityInWarehouse;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.exception.SpecifiedProductAlreadyInWarehouseException;
import ru.yandex.practicum.mapper.WarehouseMapper;
import ru.yandex.practicum.model.Address;
import ru.yandex.practicum.model.WarehouseProduct;
import ru.yandex.practicum.repository.WarehouseRepository;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseServiceImpl implements WarehouseService {
    private final WarehouseRepository repository;
    private final WarehouseMapper mapper;

    @Override
    @Transactional
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public void addNewProductToWarehouse(NewProductInWarehouseRequest request) {
        if (repository.existsById(request.getProductId())) {
            throw new SpecifiedProductAlreadyInWarehouseException(
                    "Товар c id {} уже зарегистрирован на складе", request.getProductId()
            );
        }
        WarehouseProduct product = mapper.mapToProduct(request);
        repository.save(product);
    }

    @Override
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public BookedProductsDto checkProductAvailability(ShoppingCartDto cart) {
        validateProductQuantities(cart.getProducts());
        return calculateOrderDetails(cart.getProducts());
    }

    @Override
    @Transactional
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public void takeProductToWarehouse(AddProductToWarehouseRequest request) {
        WarehouseProduct product = repository.findById(request.getProductId())
                .orElseThrow(() -> new NoSpecifiedProductInWarehouseException(
                        "Товар c id " + request.getProductId() + " уже зарегистрирован на складе"));

        Long newQuantity = product.getQuantity() + request.getQuantity();
        product.setQuantity(newQuantity);
        repository.save(product);
    }

    @Override
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public AddressDto getWarehouseAddress() {
        String address = new Address().getAddress();
        return AddressDto.builder()
                .country(address)
                .city(address)
                .street(address)
                .house(address)
                .flat(address)
                .build();
    }

    private void validateProductQuantities(Map<UUID, Long> cartProducts) {
        Map<UUID, WarehouseProduct> warehouseProducts = getWarehouseProducts(cartProducts.keySet());

        cartProducts.forEach((productId, requestedQuantity) -> {
            WarehouseProduct warehouseProduct = warehouseProducts.get(productId);

            if (warehouseProduct == null) {
                throw new ProductNotFoundException("Товар c id " + productId + " не найден на складе");
            }

            if (requestedQuantity > warehouseProduct.getQuantity()) {
                throw new ProductInShoppingCartLowQuantityInWarehouse(
                        "Недостаточно товара на складе для продукта " + productId
                );
            }
        });
    }

    private BookedProductsDto calculateOrderDetails(Map<UUID, Long> cartProducts) {
        Map<UUID, WarehouseProduct> warehouseProducts = getWarehouseProducts(cartProducts.keySet());

        double totalWeight = 0;
        double totalVolume = 0;
        boolean hasFragile = false;

        for (Map.Entry<UUID, Long> cartProduct : cartProducts.entrySet()) {
            WarehouseProduct warehouseProduct = warehouseProducts.get(cartProduct.getKey());

            double productVolume = warehouseProduct.getDimension().getHeight() *
                                   warehouseProduct.getDimension().getDepth() *
                                   warehouseProduct.getDimension().getWidth();

            totalVolume += productVolume * cartProduct.getValue();
            totalWeight += warehouseProduct.getWeight() * cartProduct.getValue();

            if (warehouseProduct.getFragile()) {
                hasFragile = true;
            }
        }

        return BookedProductsDto.builder()
                .deliveryVolume(totalVolume)
                .deliveryWeight(totalWeight)
                .fragile(hasFragile)
                .build();
    }

    private Map<UUID, WarehouseProduct> getWarehouseProducts(Set<UUID> productIds) {
        return repository.findAllById(productIds)
                .stream()
                .collect(Collectors.toMap(WarehouseProduct::getProductId, Function.identity()));
    }
}
