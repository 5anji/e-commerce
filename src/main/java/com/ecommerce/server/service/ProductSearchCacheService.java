package com.ecommerce.server.service;

import com.ecommerce.server.dto.product.ProductResponse;
import com.ecommerce.server.dto.product.ProductSearchRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

/**
 * Redis cache for POST /api/products/search.
 * Each entry is keyed by the request path and the serialized request body,
 * and expires after the configured TTL.
 */
@Service
public class ProductSearchCacheService {
    public static final String KEY_PREFIX = "/api/products/search:";

    private static final Logger log = LoggerFactory.getLogger(ProductSearchCacheService.class);
    private static final TypeReference<List<ProductResponse>> RESPONSE_TYPE = new TypeReference<>() {};

    private final StringRedisTemplate redisTemplate;
    private final JsonMapper jsonMapper;
    private final Duration ttl;

    public ProductSearchCacheService(
            StringRedisTemplate redisTemplate,
            JsonMapper jsonMapper,
            @Value("${app.cache.product-search.ttl:60s}") Duration ttl
    ) {
        this.redisTemplate = redisTemplate;
        this.jsonMapper = jsonMapper;
        this.ttl = ttl;
    }

    public Optional<List<ProductResponse>> get(String path, ProductSearchRequest request) {
        String key = buildKey(path, request);

        try {
            String cachedResponse = redisTemplate.opsForValue().get(key);

            if (cachedResponse == null) {
                return Optional.empty();
            }

            return Optional.of(jsonMapper.readValue(cachedResponse, RESPONSE_TYPE));
        } catch (JacksonException ex) {
            log.warn("Could not deserialize cached product search response for key '{}', evicting it", key, ex);
            evictQuietly(key);
            return Optional.empty();
        } catch (DataAccessException ex) {
            log.warn("Could not read product search cache from Redis: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    public void put(String path, ProductSearchRequest request, List<ProductResponse> response) {
        String key = buildKey(path, request);

        try {
            redisTemplate.opsForValue().set(
                    key,
                    jsonMapper.writeValueAsString(response),
                    ttl
            );
        } catch (JacksonException ex) {
            log.warn("Could not serialize product search response for caching", ex);
        } catch (DataAccessException ex) {
            log.warn("Could not write product search cache to Redis: {}", ex.getMessage());
        }
    }

    private String buildKey(String path, ProductSearchRequest request) {
        try {
            return path + ":" + jsonMapper.writeValueAsString(request);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Could not serialize product search request", ex);
        }
    }

    private void evictQuietly(String key) {
        try {
            redisTemplate.delete(key);
        } catch (DataAccessException ex) {
            log.warn("Could not evict cached product search response for key '{}': {}", key, ex.getMessage());
        }
    }
}
