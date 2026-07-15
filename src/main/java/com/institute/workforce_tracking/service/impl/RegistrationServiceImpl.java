package com.institute.workforce_tracking.service.impl;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.institute.workforce_tracking.dto.request.RegisterRequest;
import com.institute.workforce_tracking.dto.request.RegistrationDecisionRequest;
import com.institute.workforce_tracking.dto.response.PagedResponse;
import com.institute.workforce_tracking.dto.response.RegistrationResponse;
import com.institute.workforce_tracking.entity.RegistrationRequest;
import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.RegistrationStatus;
import com.institute.workforce_tracking.enums.Role;
import com.institute.workforce_tracking.event.RegistrationSubmittedEvent;
import com.institute.workforce_tracking.exception.BadRequestException;
import com.institute.workforce_tracking.exception.DuplicateResourceException;
import com.institute.workforce_tracking.exception.ResourceNotFoundException;
import com.institute.workforce_tracking.mapper.RegistrationMapper;
import com.institute.workforce_tracking.repository.RegistrationRequestRepository;
import com.institute.workforce_tracking.repository.UserRepository;
import com.institute.workforce_tracking.service.RegistrationService;
import com.institute.workforce_tracking.util.PageUtils;

/**
 * Default implementation of {@link RegistrationService}.
 *
 * <p>Owns the approval workflow's business rules: no SUPER_ADMIN
 * self-registration, one pending request per email, no request for an email
 * that already has an account, and decisions only on PENDING requests.
 * Approval copies the stored (already-hashed) password into the new User, so
 * the applicant logs in with the password they chose at signup.</p>
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final RegistrationRequestRepository registrationRepository;
    private final UserRepository userRepository;
    private final RegistrationMapper registrationMapper;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrationServiceImpl(RegistrationRequestRepository registrationRepository,
                                   UserRepository userRepository,
                                   RegistrationMapper registrationMapper,
                                   PasswordEncoder passwordEncoder,
                                   ApplicationEventPublisher eventPublisher) {
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
        this.registrationMapper = registrationMapper;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public RegistrationResponse register(RegisterRequest request) {
        if (request.requestedRole() == Role.SUPER_ADMIN) {
            throw new BadRequestException("The SUPER_ADMIN role cannot be requested.");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("User", "email", request.email());
        }
        if (registrationRepository.existsByEmailAndStatus(request.email(), RegistrationStatus.PENDING)) {
            throw new DuplicateResourceException(
                    "A registration request for this email is already pending approval.");
        }

        RegistrationRequest registration = new RegistrationRequest();
        registration.setFullName(request.fullName());
        registration.setEmail(request.email());
        registration.setPassword(passwordEncoder.encode(request.password()));
        registration.setRequestedRole(request.requestedRole());
        registration.setStatus(RegistrationStatus.PENDING);
        RegistrationRequest saved = registrationRepository.save(registration);

        // Announce the submission — listeners alert the Super Admin(s) in-app
        // and by email after this transaction commits.
        eventPublisher.publishEvent(new RegistrationSubmittedEvent(
                saved.getId(), saved.getFullName(), saved.getEmail(), saved.getRequestedRole()));

        return registrationMapper.toRegistrationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<RegistrationResponse> getRequests(RegistrationStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(
                PageUtils.safePage(page),
                PageUtils.safeSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<RegistrationResponse> result = registrationRepository.findByStatus(status, pageable)
                .map(registrationMapper::toRegistrationResponse);
        return PagedResponse.from(result);
    }

    @Override
    @Transactional(readOnly = true)
    public long getPendingCount() {
        return registrationRepository.countByStatus(RegistrationStatus.PENDING);
    }

    @Override
    @Transactional
    public RegistrationResponse approve(Long id, RegistrationDecisionRequest decision, String deciderEmail) {
        RegistrationRequest registration = getPendingRequest(id);

        // The account may have been created by other means since submission.
        if (userRepository.existsByEmail(registration.getEmail())) {
            throw new DuplicateResourceException("User", "email", registration.getEmail());
        }

        Role role = decision.role() != null ? decision.role() : registration.getRequestedRole();
        if (role == Role.SUPER_ADMIN) {
            throw new BadRequestException("Approval cannot grant the SUPER_ADMIN role.");
        }

        User user = new User();
        user.setFullName(registration.getFullName());
        user.setEmail(registration.getEmail());
        user.setPassword(registration.getPassword()); // already BCrypt-hashed at signup
        user.setRole(role);
        user.setEnabled(true);
        userRepository.save(user);

        decide(registration, RegistrationStatus.APPROVED, decision.comment(), deciderEmail);
        return registrationMapper.toRegistrationResponse(registration);
    }

    @Override
    @Transactional
    public RegistrationResponse reject(Long id, RegistrationDecisionRequest decision, String deciderEmail) {
        RegistrationRequest registration = getPendingRequest(id);
        decide(registration, RegistrationStatus.REJECTED, decision.comment(), deciderEmail);
        return registrationMapper.toRegistrationResponse(registration);
    }

    /** Loads a request and ensures it is still awaiting a decision. */
    private RegistrationRequest getPendingRequest(Long id) {
        RegistrationRequest registration = registrationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Registration request", "id", id));
        if (registration.getStatus() != RegistrationStatus.PENDING) {
            throw new BadRequestException("This registration request has already been "
                    + registration.getStatus().name().toLowerCase() + ".");
        }
        return registration;
    }

    /** Records the decision on the request. */
    private void decide(RegistrationRequest registration, RegistrationStatus status,
                        String comment, String deciderEmail) {
        User decider = userRepository.findByEmail(deciderEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", deciderEmail));
        registration.setStatus(status);
        registration.setDecidedBy(decider);
        registration.setDecisionComment(comment);
        registrationRepository.save(registration);
    }
}
