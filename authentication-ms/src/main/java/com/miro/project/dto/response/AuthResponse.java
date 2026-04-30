package com.miro.project.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.miro.project.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String name;
    private Role role;
    @Builder.Default
    private String tokenType = "Bearer";
}
