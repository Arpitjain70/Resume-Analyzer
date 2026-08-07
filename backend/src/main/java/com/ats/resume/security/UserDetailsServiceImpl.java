package com.ats.resume.security;

import com.ats.resume.entity.User;
import com.ats.resume.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * ─────────────────────────────────────────────────────────
 * UserDetailsServiceImpl — Bridge Between Spring Security and Our Database
 * Location: security/UserDetailsServiceImpl.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * Spring Security needs to load user data during authentication.
 * But Spring Security doesn't know about our database or User entity.
 *
 * UserDetailsService is an interface Spring Security provides.
 * We implement it to tell Spring Security:
 *   "When you need a user, look them up in MY database by email."
 *
 * WHAT IS UserDetails?
 * Spring Security's own representation of a user. It has:
 * - getUsername()    → email in our case
 * - getPassword()    → the hashed password
 * - getAuthorities() → list of roles/permissions (we have none for now)
 * - isEnabled()      → is the account active? (we return true always)
 *
 * HOW IT CONNECTS:
 * JwtAuthenticationFilter extracts email from JWT
 * → calls userDetailsService.loadUserByUsername(email)
 * → this method queries DB for the User
 * → returns UserDetails
 * → filter sets the SecurityContext with this user
 *
 * @RequiredArgsConstructor → Lombok generates constructor that injects UserRepository.
 * This is the preferred way over @Autowired field injection.
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Load a user from the database by their email (we use email as username).
     *
     * Spring Security calls this automatically during authentication.
     *
     * @param email the email address (our "username")
     * @return UserDetails object Spring Security can work with
     * @throws UsernameNotFoundException if no user with this email exists
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user in database by email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email)
                );

        /**
         * org.springframework.security.core.userdetails.User (not our User entity!)
         * is Spring Security's built-in UserDetails implementation.
         *
         * We pass:
         * - username: user's email
         * - password: the BCrypt-hashed password stored in DB
         * - authorities: empty list (no roles in this project)
         *
         * Spring Security will compare the provided password against this hashed password
         * using BCryptPasswordEncoder.matches().
         */
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .authorities("ROLE_USER")  // basic role for every authenticated user
                .build();
    }
}
