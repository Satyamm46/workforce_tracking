package com.institute.workforce_tracking.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.request.PushSubscriptionRequest;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.service.PushService;

import jakarta.validation.Valid;

/**
 * REST endpoints for browser push subscriptions. All authenticated — every
 * user may enable push for their own account.
 */
@RestController
@RequestMapping(ApiConstants.PUSH_BASE)
public class PushController {

    private final PushService pushService;

    public PushController(PushService pushService) {
        this.pushService = pushService;
    }

    /** The VAPID public key the browser needs to subscribe. */
    @GetMapping("/public-key")
    public ResponseEntity<ApiResponse<String>> getPublicKey() {
        return ResponseEntity.ok(
                ApiResponse.of("Push public key retrieved", pushService.getPublicKey()));
    }

    /** Registers this browser for push notifications. */
    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<Void>> subscribe(
            @Valid @RequestBody PushSubscriptionRequest request,
            Authentication authentication) {

        pushService.subscribe(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.message("Push subscription registered"));
    }

    /** Removes this browser's push subscription. */
    @DeleteMapping("/unsubscribe")
    public ResponseEntity<ApiResponse<Void>> unsubscribe(
            @RequestParam String endpoint,
            Authentication authentication) {

        pushService.unsubscribe(authentication.getName(), endpoint);
        return ResponseEntity.ok(ApiResponse.message("Push subscription removed"));
    }
}
