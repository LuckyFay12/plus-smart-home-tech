package ru.yandex.practicum.model;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dimension {

    @Positive
    @NotNull
    private Double width;

    @Positive
    @NotNull
    private Double height;

    @Positive
    @NotNull
    private Double depth;
}

