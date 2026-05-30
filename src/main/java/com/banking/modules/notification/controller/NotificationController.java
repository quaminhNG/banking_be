package com.banking.modules.notification.controller;

import com.banking.modules.notification.dto.request.SubscribeNotificationRequest;
import com.banking.modules.notification.service.NotificationSubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationSubscriptionService notificationService;

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(
            @Valid @RequestBody SubscribeNotificationRequest request) {
        String result = notificationService.subscribe(request);
        return ResponseEntity.ok(result);
    }
}
