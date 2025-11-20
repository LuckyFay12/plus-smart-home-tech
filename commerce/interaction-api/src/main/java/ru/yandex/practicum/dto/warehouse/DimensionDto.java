package ru.yandex.practicum.dto.warehouse;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DimensionDto {

    @Min(1)
    @NotNull
    private Double width;

    @Min(1)
    @NotNull
    private Double height;

    @Min(1)
    @NotNull
    private Double depth;
}
