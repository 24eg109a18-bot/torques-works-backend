package com.garage.management.dto;

import java.time.LocalDateTime;
import java.util.Map;

/** Consistent JSON error body returned for every failed request. */
public record ApiError(LocalDateTime timestamp, int status, String error, String message, String path,
                       Map<String, String> fieldErrors) {
    public static ApiError of(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ApiError(LocalDateTime.now(), status, error, message, path, fieldErrors);
    }
}
