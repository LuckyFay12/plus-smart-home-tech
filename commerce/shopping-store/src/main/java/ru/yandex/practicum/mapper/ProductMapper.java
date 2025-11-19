package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.yandex.practicum.dto.store.ProductDto;
import ru.yandex.practicum.model.Product;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    ProductDto toDto(Product product);

    Product toProduct(ProductDto productDto);

    @Mapping(target = "productId", ignore = true)
    void update(ProductDto productDto, @MappingTarget Product product);
}
