package com.miro.project.dto.response;

import com.miro.project.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInternalResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
}