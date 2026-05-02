package com.miro.project.dto.request;

import com.miro.project.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LoginRequest {

    @Schema(description = "Your name", example = "John")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Your password", example = "jacksonLS63!+")
    @NotBlank(message = "Password is required")
    private String password;

    @Schema(description = "The role you are logging in as", example = "PATIENT")
    @NotNull(message = "Role is required to identify the account")
    private Role role;
}