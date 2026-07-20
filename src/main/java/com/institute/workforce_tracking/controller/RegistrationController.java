package com.institute.workforce_tracking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.request.RegisterRequest;
import com.institute.workforce_tracking.dto.request.RegistrationDecisionRequest;
import com.institute.workforce_tracking.dto.request.SendOtpRequest;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.RegistrationResponse;
import com.institute.workforce_tracking.enums.RegistrationStatus;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.service.RegistrationService;

import jakarta.validation.Valid;

/**
 * REST endpoints for the self-registration / approval workflow.
 *
 * <p>{@code POST /register} is the only public endpoint — anyone can apply.
 * Reviewing and deciding requests is exclusively the Super Admin's, enforced
 * with {@code @PreAuthorize} per method.</p>
 */
@RestController
@RequestMapping(ApiConstants.REGISTRATIONS_BASE)
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    /**
     * Public: emails a one-time verification code to the given address. The
     * applicant must present this code back when submitting the registration,
     * which proves they control the inbox.
     */
    @PostMapping("/send-otp")
    public ResponseEntity<ApiResponse<Void>> sendOtp(
            @Valid @RequestBody SendOtpRequest request) {

        registrationService.sendOtp(request.email());
        return ResponseEntity.ok(ApiResponse.of(
                "A verification code has been sent to your email.", null));
    }

    /** Public: submits a registration request for Super Admin approval. */
    @PostMapping
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        RegistrationResponse created = registrationService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of(
                        "Registration submitted. You can log in once the administrator approves it.",
                        created));
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<RegistrationResponse>>> getRequests(
            @RequestParam(defaultValue = "PENDING") RegistrationStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponse<RegistrationResponse> requests =
                registrationService.getRequests(status, page, size);
        return ResponseEntity.ok(ApiResponse.of("Registration requests retrieved", requests));
    }

    @GetMapping("/pending-count")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Long>> getPendingCount() {
        return ResponseEntity.ok(
                ApiResponse.of("Pending count retrieved", registrationService.getPendingCount()));
    }

    @PatchMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> approve(
            @PathVariable Long id,
            @Valid @RequestBody RegistrationDecisionRequest decision,
            Authentication authentication) {

        RegistrationResponse decided =
                registrationService.approve(id, decision, authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Registration approved — account created", decided));
    }

    @PatchMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<RegistrationResponse>> reject(
            @PathVariable Long id,
            @Valid @RequestBody RegistrationDecisionRequest decision,
            Authentication authentication) {

        RegistrationResponse decided =
                registrationService.reject(id, decision, authentication.getName());
        return ResponseEntity.ok(ApiResponse.of("Registration rejected", decided));
    }
}
