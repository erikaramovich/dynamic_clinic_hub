package com.miro.project.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Schema(description = "Your name", example = "John")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Your password", example = "jacksonLS63!+")
    @NotBlank(message = "Password is required")
    private String password;
}