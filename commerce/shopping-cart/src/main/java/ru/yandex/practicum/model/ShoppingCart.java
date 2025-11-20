package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "shopping_cart")
public class ShoppingCart {
    @Id
    @UuidGenerator
    @Column(name = "shopping_cart_id")
    private UUID shoppingCartId;

    @ElementCollection
    @CollectionTable(name = "cart_products", joinColumns = @JoinColumn(name = "cart_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<UUID, Long> products;

    private String username;

    @Enumerated(EnumType.STRING)
    private ShoppingCartState state;

    public void addProduct(UUID productId, Long quantity) {
        products.merge(productId, quantity, Long::sum);
    }

    public void removeProducts(List<UUID> productIds) {
        productIds.forEach(products::remove);
    }
}