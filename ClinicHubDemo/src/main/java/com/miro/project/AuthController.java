package com.miro.project;


import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth service")
public class AuthController {

    @PostMapping("/register")
    public String registerUser() {
        return null;
    }

    @PostMapping("/login")
    public String login() {
        return null;
    }
}
