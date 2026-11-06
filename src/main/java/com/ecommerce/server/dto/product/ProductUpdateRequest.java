package com.ecommerce.server.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Partial update payload for PATCH requests.
 * Fields left out (null) are not changed.
 */
public record ProductUpdateRequest(
        @Size(min = 1, max = 150, message = "Name must contain between 1 and 150 characters")
        String name,

        String description,

        @DecimalMin(value = "0.00", message = "Price must be zero or greater")
        @Digits(integer = 8, fraction = 2, message = "Price must have at most 8 digits and 2 decimals")
        BigDecimal price,

        @Min(value = 0, message = "Stock must be zero or greater")
        Integer stock,

        Long categoryId
) {}
