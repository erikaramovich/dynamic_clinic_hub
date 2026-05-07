package com.miro.project.dto.response;

import com.miro.project.model.Role;
import lombok.Builder;
import lombok.Data;
import java.util.UUID;

@Data
@Builder
public class UserInternalResponse {
    private UUID id;
    private String name;
    private String email;
    private Role role;
}