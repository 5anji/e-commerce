package com.ecommerce.server.dto.product;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

/**
 * Optional product filters for POST /api/products/search.
 * Omitted (null) filters are ignored; supplied filters are combined with AND.
 */
public record ProductSearchRequest(
        @DecimalMin(value = "0.00", message = "minPrice must be zero or greater")
        @Digits(integer = 8, fraction = 2, message = "minPrice must have at most 8 digits and 2 decimals")
        BigDecimal minPrice,

        @DecimalMin(value = "0.00", message = "maxPrice must be zero or greater")
        @Digits(integer = 8, fraction = 2, message = "maxPrice must have at most 8 digits and 2 decimals")
        BigDecimal maxPrice,

        @Min(value = 0, message = "minStock must be zero or greater")
        Integer minStock,

        @Min(value = 0, message = "maxStock must be zero or greater")
        Integer maxStock,

        Long categoryId
) {}
