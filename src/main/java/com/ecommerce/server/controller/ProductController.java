package com.ecommerce.server.controller;

import com.ecommerce.server.dto.product.CategoryLinkRequest;
import com.ecommerce.server.dto.product.ProductRequest;
import com.ecommerce.server.dto.product.ProductResponse;
import com.ecommerce.server.dto.product.ProductSearchRequest;
import com.ecommerce.server.dto.product.ProductUpdateRequest;
import com.ecommerce.server.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getProducts() {
        return productService.findAll();
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PostMapping("/search")
    public List<ProductResponse> searchProducts(
            @Valid @RequestBody ProductSearchRequest request
    ) {
        return productService.search(request);
    }

    @PatchMapping("/{id}")
    public ProductResponse updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return productService.update(id, request);
    }

    @PutMapping("/{id}/category")
    public ProductResponse linkCategory(
            @PathVariable Long id,
            @Valid @RequestBody CategoryLinkRequest request
    ) {
        return productService.linkCategory(id, request.categoryId());
    }

    @DeleteMapping("/{id}/category")
    public ProductResponse unlinkCategory(@PathVariable Long id) {
        return productService.unlinkCategory(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) {
        productService.delete(id);
    }
}
