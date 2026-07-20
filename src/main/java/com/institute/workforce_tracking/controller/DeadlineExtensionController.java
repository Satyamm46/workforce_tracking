package com.institute.workforce_tracking.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.institute.workforce_tracking.constants.ApiConstants;
import com.institute.workforce_tracking.dto.request.GrantDeadlineExtensionRequest;
import com.institute.workforce_tracking.dto.response.ApiResponse;
import com.institute.workforce_tracking.dto.response.DeadlineExtensionResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.service.DeadlineExtensionService;

import jakarta.validation.Valid;

/**
 * Admin endpoints for granting grace periods on submission deadlines
 * (work plan, work report, lecture summary). SUPER_ADMIN and ADMIN only.
 */
@RestController
@RequestMapping(ApiConstants.DEADLINE_EXTENSIONS_BASE)
public class DeadlineExtensionController {

    private final DeadlineExtensionService deadlineExtensionService;

    public DeadlineExtensionController(DeadlineExtensionService deadlineExtensionService) {
        this.deadlineExtensionService = deadlineExtensionService;
    }

    /** Grants (or updates) an extension and reverses any applied penalty. */
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<DeadlineExtensionResponse>> grantExtension(
            Authentication authentication,
            @Valid @RequestBody GrantDeadlineExtensionRequest request) {

        DeadlineExtensionResponse extension =
                deadlineExtensionService.grantExtension(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.of("Deadline extended", extension));
    }

    /** A page of all granted extensions, newest target date first. */
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<PagedResponse<DeadlineExtensionResponse>>> getExtensions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return ResponseEntity.ok(ApiResponse.of("Extensions retrieved",
                deadlineExtensionService.getExtensions(page, size)));
    }

    /** Revokes an extension (future deadlines only; no retroactive penalty). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> revokeExtension(@PathVariable Long id) {
        deadlineExtensionService.revokeExtension(id);
        return ResponseEntity.ok(ApiResponse.of("Extension revoked", null));
    }
}
