package com.miro.project;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/notification")
@Tag(name = "Notification Service")
public class NotificationController {

    @GetMapping
    public String getNotification() {
        return null;
    }

    @PostMapping()
    public String postNotification() {
        return null;
    }
}