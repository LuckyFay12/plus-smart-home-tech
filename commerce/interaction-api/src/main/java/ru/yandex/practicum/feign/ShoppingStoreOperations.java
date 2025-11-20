package ru.yandex.practicum.feign;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.dto.store.SetProductQuantityStateRequest;
import ru.yandex.practicum.enums.ProductCategory;

import java.util.UUID;

public interface ShoppingStoreOperations {

    @PutMapping
    ProductDto createNewProduct(@RequestBody @Valid ProductDto productDto);

    @PostMapping
    ProductDto updateProduct(@RequestBody @Valid ProductDto productDto);

    @GetMapping("/{productId}")
    ProductDto getProduct(@PathVariable @NotNull UUID productId);

    @GetMapping
    Page<ProductDto> getProducts(@RequestParam("category") @NotNull ProductCategory category,
                                 @PageableDefault(size = 20) Pageable pageable);

    @PostMapping("/removeProductFromStore")
    void removeProductFromStore(@RequestBody @NotNull UUID productId);

    @PostMapping("/quantityState")
    void setProductQuantityState(@ModelAttribute @Valid SetProductQuantityStateRequest request);
}
