package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.aspect.LogExecution;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.enums.ProductCategory;
import ru.yandex.practicum.enums.ProductState;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.mapper.ProductMapper;
import ru.yandex.practicum.model.Product;
import ru.yandex.practicum.repository.ShoppingStoreRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShoppingStoreServiceImpl implements ShoppingStoreService {

    private final ShoppingStoreRepository shoppingStoreRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public ProductDto createNewProduct(ProductDto productDto) {
        Product product = productMapper.toProduct(productDto);
        if (product.getProductState() == null) {
            product.setProductState(ProductState.ACTIVE);
        }
        Product savedProduct = shoppingStoreRepository.save(product);
        return productMapper.toDto(savedProduct);
    }

    @Override
    @Transactional
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public ProductDto updateProduct(ProductDto productDto) throws ProductNotFoundException {
        Product product = findProductById(productDto.getProductId());
        productMapper.update(productDto, product);
        Product updatedProduct = shoppingStoreRepository.save(product);

        return productMapper.toDto(updatedProduct);
    }

    @Override
    @Transactional(readOnly = true)
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public ProductDto getProduct(UUID productId) throws ProductNotFoundException {
        Product product = findProductById(productId);
        return productMapper.toDto(product);
    }

    @Override
    @Transactional(readOnly = true)
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public Page<ProductDto> getProducts(ProductCategory category, Pageable pageable) {
        Page<Product> products = shoppingStoreRepository.findByProductCategoryAndProductState(
                category, ProductState.ACTIVE, pageable);
        return products.map(productMapper::toDto);
    }

    @Override
    @Transactional
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public void removeProductFromStore(UUID productId) throws ProductNotFoundException {
        Product product = findProductById(productId);
        product.setProductState(ProductState.DEACTIVATE);
        shoppingStoreRepository.save(product);
    }

    @Override
    @Transactional
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public void setProductQuantityState(SetProductQuantityStateRequest request) throws ProductNotFoundException {
        Product product = findProductById(request.getProductId());
        product.setQuantityState(request.getQuantityState());
        shoppingStoreRepository.save(product);
    }

    @LogExecution(level = LogExecution.LogLevel.INFO)
    private Product findProductById(UUID productId) {
        return shoppingStoreRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Товар не найден: " + productId));
    }
}



