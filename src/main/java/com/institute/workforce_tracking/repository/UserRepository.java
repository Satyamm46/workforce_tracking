package com.institute.workforce_tracking.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.institute.workforce_tracking.entity.User;
import com.institute.workforce_tracking.enums.Role;

/**
 * Data-access layer for {@link User} entities.
 *
 * <p>By extending {@link JpaRepository}, this interface inherits a complete set
 * of CRUD operations (save, findById, findAll, delete, count, …) with no
 * implementation required — Spring Data JPA generates a proxy implementation at
 * runtime. The custom methods below are "derived queries": Spring parses the
 * method name and writes the SQL for us.</p>
 *
 * <p>This is the Repository pattern: a clean abstraction over persistence that
 * the service layer depends on, keeping database access details out of the
 * business logic.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their (unique) email address.
     *
     * <p>Returns an {@link Optional} because a user with the given email may
     * not exist — the caller is forced to handle the "not found" case rather
     * than risk a null. Used during authentication to load the account by its
     * login identifier.</p>
     *
     * @param email the email to search by
     * @return the matching user, if any
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email already exists.
     *
     * <p>More efficient than fetching the whole entity when we only need to
     * know existence (e.g. before creating a user). Spring generates a
     * {@code SELECT COUNT/EXISTS} query rather than loading the row.</p>
     *
     * @param email the email to check
     * @return {@code true} if a user with that email exists
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether any user with the given role exists.
     *
     * <p>Used by the data seeder to decide whether the first Super Admin needs
     * to be created (avoids seeding a duplicate on every startup).</p>
     *
     * @param role the role to check for
     * @return {@code true} if at least one user has that role
     */
    boolean existsByRole(Role role);

    /** How many active (enabled) user accounts exist. */
    long countByEnabledTrue();

    /**
     * All users holding the given role. Used to notify every Super Admin when
     * a new registration request arrives.
     *
     * @param role the role to filter by
     * @return the users with that role
     */
    List<User> findByRole(Role role);

}