package com.miro.project.dto.request;

import com.miro.project.model.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @Schema(description = "Your name", example = "John")
    @NotBlank(message = "Name is required")
    private String name;

    @Schema(description = "Your email", example = "john@miro.com")
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @Schema(description = "Your password", example = "jacksonLS63!+")
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?+&])[A-Za-z\\d@$!%*?+&]{8,}$",
            message = "Password must contain at least one uppercase, one lowercase, one number, and one special character (@$!%*?+&)"
    )
    private final String password;


    @Schema(description = "Your Role in clinic hub", example = "PATIENT")
    @NotNull(message = "Role is required (PATIENT, DOCTOR, ADMINISTRATOR)")
    private Role role;
}