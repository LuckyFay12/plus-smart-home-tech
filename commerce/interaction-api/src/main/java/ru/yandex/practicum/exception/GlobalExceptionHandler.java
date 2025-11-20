package ru.yandex.practicum.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.yandex.practicum.dto.exception.ApiError;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ProductNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleProductNotFoundException(ProductNotFoundException e) {
        log.debug("Товар не найден: {}", e.getMessage());
        return ApiError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .userMessage("Запрашиваемый товар не найден")
                .message(e.getMessage())
                .localizedMessage(e.getLocalizedMessage())
                .build();

    }

    @ExceptionHandler(NoSpecifiedProductInWarehouseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNoSpecifiedProductInWarehouseException(NoSpecifiedProductInWarehouseException e) {
        log.debug("Товар отсутствует на складе: {}", e.getMessage());
        return ApiError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .userMessage("Данный товар отсутствует на складе")
                .message(e.getMessage())
                .localizedMessage(e.getLocalizedMessage())
                .build();
    }

    @ExceptionHandler(ShoppingCartNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleShoppingCartNotFoundException(ShoppingCartNotFoundException e) {
        log.debug("Корзина не найдена: {}", e.getMessage());
        return ApiError.builder()
                .httpStatus(HttpStatus.NOT_FOUND)
                .userMessage("Корзина покупок не найдена")
                .message(e.getMessage())
                .localizedMessage(e.getLocalizedMessage())
                .build();
    }

    @ExceptionHandler(NoProductsInShoppingCartException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleNoProductsInShoppingCartException(NoProductsInShoppingCartException e) {
        log.debug("Корзина пуста: {}", e.getMessage());
        return ApiError.builder()
                .httpStatus(HttpStatus.BAD_REQUEST)
                .userMessage("Ваша корзина пуста")
                .message(e.getMessage())
                .localizedMessage(e.getLocalizedMessage())
                .build();
    }

    @ExceptionHandler(NotAuthorizedUserException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ApiError handleNotAuthorizedUserException(NotAuthorizedUserException e) {
        log.debug("Неавторизованный пользователь: {}", e.getMessage());
        return ApiError.builder()
                .httpStatus(HttpStatus.UNAUTHORIZED)
                .userMessage("Для выполнения действия требуется авторизация")
                .message(e.getMessage())
                .localizedMessage(e.getLocalizedMessage())
                .build();
    }

    @ExceptionHandler(ProductInShoppingCartLowQuantityInWarehouse.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleProductsInShoppingCartLowQuantityInWarehouse(ProductInShoppingCartLowQuantityInWarehouse e) {
        log.debug("Недостаточное количество товара на складе: {}", e.getMessage());
        return ApiError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .userMessage("Недостаточное количество товара на складе")
                .message(e.getMessage())
                .localizedMessage(e.getLocalizedMessage())
                .build();
    }

    @ExceptionHandler(SpecifiedProductAlreadyInWarehouseException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiError handleSpecifiedProductAlreadyInWarehouseException(SpecifiedProductAlreadyInWarehouseException e) {
        log.debug("Товар уже существует на складе: {}", e.getMessage());
        return ApiError.builder()
                .httpStatus(HttpStatus.CONFLICT)
                .userMessage("Данный товар уже существует на складе")
                .message(e.getMessage())
                .localizedMessage(e.getLocalizedMessage())
                .build();
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiError handleGenericException(Exception e) {
        log.error("Internal error: {}", e.getMessage());
        return ApiError.builder()
                .httpStatus(HttpStatus.INTERNAL_SERVER_ERROR)
                .userMessage("Произошла внутренняя ошибка сервера")
                .message(e.getMessage())
                .build();
    }
}
