package com.miro.project.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ErrorResponse(
        @Schema(description = "Type of error or HTTP status code representation", example = "UNAUTHORIZED")
        String code,
        @Schema(description = "Brief, human-readable error message indicating what went wrong", example = "Invalid credentials provided")
        String message,
        @Schema(description = "Detailed explanation or specific field validation errors", example = "email: Please provide a valid email address")
        String details) {

    public ErrorResponse(String code, String message, String details) {
        this.code = code;
        this.message = message;
        this.details = details;
    }
}