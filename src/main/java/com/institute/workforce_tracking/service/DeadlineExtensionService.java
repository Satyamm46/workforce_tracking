package com.institute.workforce_tracking.service;

import com.institute.workforce_tracking.dto.request.GrantDeadlineExtensionRequest;
import com.institute.workforce_tracking.dto.response.DeadlineExtensionResponse;
import com.institute.workforce_tracking.dto.response.PagedResponse;

/**
 * Admin-granted grace periods on submission deadlines (work plan, work
 * report, lecture summary). Granting also reverses an already-applied
 * penalty for that user and day.
 */
public interface DeadlineExtensionService {

    /**
     * Grants (or updates) an extension. Reverses an existing penalty:
     * un-marks a report absence, restores a SUMMARY_MISSED lecture to
     * COMPLETED, clears a work plan's late flag. Notifies the user.
     */
    DeadlineExtensionResponse grantExtension(String adminEmail,
                                             GrantDeadlineExtensionRequest request);

    /** A page of all granted extensions, newest target date first. */
    PagedResponse<DeadlineExtensionResponse> getExtensions(int page, int size);

    /** Revokes an extension. Does not re-apply penalties retroactively. */
    void revokeExtension(Long extensionId);
}
