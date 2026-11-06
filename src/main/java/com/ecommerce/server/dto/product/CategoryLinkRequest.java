package com.ecommerce.server.dto.product;

import jakarta.validation.constraints.NotNull;

public record CategoryLinkRequest(
        @NotNull(message = "Category id is required")
        Long categoryId
) {}
