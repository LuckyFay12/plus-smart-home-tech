package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.aspect.LogExecution;
import ru.yandex.practicum.dto.cart.ChangeProductQuantityRequest;
import ru.yandex.practicum.dto.cart.ShoppingCartDto;
import ru.yandex.practicum.exception.NotAuthorizedUserException;
import ru.yandex.practicum.exception.ProductNotFoundException;
import ru.yandex.practicum.exception.ShoppingCartNotFoundException;
import ru.yandex.practicum.feign.WarehouseClient;
import ru.yandex.practicum.mapper.ShoppingCartMapper;
import ru.yandex.practicum.model.ShoppingCart;
import ru.yandex.practicum.model.ShoppingCartState;
import ru.yandex.practicum.repository.ShoppingCartRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ShoppingCartServiceImpl implements ShoppingCartService {

    private final ShoppingCartRepository shoppingCartRepository;
    private final ShoppingCartMapper shoppingCartMapper;
    private final WarehouseClient warehouse;


    @Override
    @Transactional
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public ShoppingCartDto addProductToCart(String username, Map<UUID, Long> request) {
        ShoppingCart cart = getOrCreateShoppingCartByUser(username);

        if (cart.getState() == ShoppingCartState.DEACTIVATE) {
            throw new IllegalArgumentException("Нельзя добавлять товары. Корзина деактивирована");
        }

        request.forEach((productId, quantity) -> {
            if (quantity <= 0) {
                throw new IllegalArgumentException("Количество товара должно быть положительным: " + productId);
            }
            cart.addProduct(productId, quantity);
        });

        warehouse.checkProductAvailability(shoppingCartMapper.mapToDto(cart));
        shoppingCartRepository.save(cart);

        return shoppingCartMapper.mapToDto(cart);
    }

    @Override
    @Transactional(readOnly = true)
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public ShoppingCartDto getShoppingCart(String username) {
        checkUser(username);
        ShoppingCart cart = getOrCreateShoppingCartByUser(username);
        return shoppingCartMapper.mapToDto(cart);
    }

    @Override
    @Transactional
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public void deactivatingUserCart(String username) {
        ShoppingCart cart = getExistingShoppingCartByUser(username);
        if (cart.getState() == ShoppingCartState.DEACTIVATE) {
            log.info("Корзина пользователя {} уже деактивирована", username);
        }

        cart.setState(ShoppingCartState.DEACTIVATE);
        shoppingCartRepository.save(cart);
    }

    @Override
    @Transactional
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public ShoppingCartDto removeProductFromCart(String username, List<UUID> productIds) {
        ShoppingCart cart = getExistingShoppingCartByUser(username);

        if (cart.getState() == ShoppingCartState.DEACTIVATE) {
            throw new IllegalArgumentException("Нельзя удалять товары. Корзина деактивирована");
        }

       Set<UUID> cartProductIds  = cart.getProducts().keySet();
              if (!cartProductIds.containsAll(productIds)) {
                  List<UUID> missingProduct = productIds.stream()
                          .filter(id -> !cartProductIds.contains(id))
                          .collect(Collectors.toList());
                  throw new ProductNotFoundException("Не найдены товары: " + missingProduct);
              }

        cart.removeProducts(productIds);
        ShoppingCart savedCart = shoppingCartRepository.save(cart);
        return shoppingCartMapper.mapToDto(savedCart);
    }

    @Override
    @Transactional
    @LogExecution(level = LogExecution.LogLevel.INFO)
    public ShoppingCartDto changeQuantity(String username, ChangeProductQuantityRequest request) {
        ShoppingCart cart = getExistingShoppingCartByUser(username);
        if (cart.getState() == ShoppingCartState.DEACTIVATE) {
            throw new IllegalArgumentException("Корзина деактивирована");
        }

        if (request.getNewQuantity() > 0) {
            cart.getProducts().put(request.getProductId(), request.getNewQuantity());
        } else {
            cart.getProducts().remove(request.getProductId());
        }

        ShoppingCart savedCart = shoppingCartRepository.save(cart);
        return shoppingCartMapper.mapToDto(savedCart);
    }

    private ShoppingCart getOrCreateShoppingCartByUser(String username) {
        checkUser(username);

        Optional<ShoppingCart> cart = shoppingCartRepository.findAllByUsername(username);
        if (cart.isPresent()) {
            return cart.get();
        } else {
            ShoppingCart newCart = ShoppingCart.builder()
                    .username(username)
                    .state(ShoppingCartState.ACTIVE)
                    .products(new HashMap<>())
                    .build();
            ShoppingCart savedCart = shoppingCartRepository.save(newCart);
            return savedCart;
        }
    }

    private ShoppingCart getExistingShoppingCartByUser(String username) {
        checkUser(username);
        Optional<ShoppingCart> cart = shoppingCartRepository.findAllByUsername(username);
        if (cart.isPresent()) {
            return cart.get();
        } else {
            throw new ShoppingCartNotFoundException("Корзина не найдена");
        }
    }

    private void checkUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new NotAuthorizedUserException("Имя пользователя не может быть пустым");
        }
    }
}
