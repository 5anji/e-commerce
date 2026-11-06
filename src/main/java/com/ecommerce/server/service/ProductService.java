package com.ecommerce.server.service;

import com.ecommerce.server.dto.product.CategoryLinkRequest;
import com.ecommerce.server.dto.product.ProductRequest;
import com.ecommerce.server.dto.product.ProductResponse;
import com.ecommerce.server.dto.product.ProductSearchRequest;
import com.ecommerce.server.dto.product.ProductUpdateRequest;
import com.ecommerce.server.entity.Category;
import com.ecommerce.server.entity.Product;
import com.ecommerce.server.repository.CategoryRepository;
import com.ecommerce.server.repository.ProductRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            CategoryRepository categoryRepository
    ) {
        this.productRepository = productRepository;
        this.categoryRepository = categoryRepository;
    }

    public List<ProductResponse> findAll() {
        return productRepository
                .findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return productRepository
                .findById(id)
                .map(this::toResponse)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> search(ProductSearchRequest request) {
        if (request.minPrice() != null
                && request.maxPrice() != null
                && request.minPrice().compareTo(request.maxPrice()) > 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "minPrice must be less than or equal to maxPrice"
            );
        }

        if (request.minStock() != null
                && request.maxStock() != null
                && request.minStock() > request.maxStock()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "minStock must be less than or equal to maxStock"
            );
        }

        Specification<Product> specification = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (request.minPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"), request.minPrice()));
            }

            if (request.maxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"), request.maxPrice()));
            }

            if (request.minStock() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("stock"), request.minStock()));
            }

            if (request.maxStock() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("stock"), request.maxStock()));
            }

            if (request.categoryId() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("category").get("id"), request.categoryId()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository
                .findAll(specification)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String name = request.name().trim();

        if (productRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A product with this name already exists"
            );
        }

        Category category = null;
        if (request.categoryId() != null) {
            category = categoryRepository
                    .findById(request.categoryId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Category not found"
                    ));
        }

        Product product = new Product(
                name,
                request.description(),
                request.price(),
                request.stock() == null ? 0 : request.stock(),
                category
        );

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = productRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        if (request.name() != null) {
            String name = request.name().trim();

            if (name.isEmpty()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Name must not be blank"
                );
            }

            if (productRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "A product with this name already exists"
                );
            }

            product.setName(name);
        }

        if (request.description() != null) {
            product.setDescription(request.description());
        }

        if (request.price() != null) {
            product.setPrice(request.price());
        }

        if (request.stock() != null) {
            product.setStock(request.stock());
        }

        if (request.categoryId() != null) {
            Category category = categoryRepository
                    .findById(request.categoryId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Category not found"
                    ));

            product.setCategory(category);
        }

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse linkCategory(Long productId, Long categoryId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        Category category = categoryRepository
                .findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Category not found"
                ));

        product.setCategory(category);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse unlinkCategory(Long productId) {
        Product product = productRepository
                .findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        product.setCategory(null);

        return toResponse(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found"
            );
        }

        productRepository.deleteById(id);
    }

    private ProductResponse toResponse(Product product) {
        Long categoryId = product.getCategory() == null ? null : product.getCategory().getId();
        String categoryName = product.getCategory() == null ? null : product.getCategory().getName();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStock(),
                categoryId,
                categoryName,
                product.getCreatedAt()
        );
    }
}
