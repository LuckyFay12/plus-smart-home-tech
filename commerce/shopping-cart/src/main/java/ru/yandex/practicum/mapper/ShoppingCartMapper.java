package ru.yandex.practicum.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.model.ShoppingCart;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ShoppingCartMapper {

    @Mapping(target = "shoppingCartId", source = "shoppingCartId")
    ShoppingCartDto mapToDto(ShoppingCart shoppingCart);

    ShoppingCart mapToEntity(ShoppingCartDto shoppingCartDto);
}
