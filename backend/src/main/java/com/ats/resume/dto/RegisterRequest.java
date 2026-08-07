package com.ats.resume.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * ─────────────────────────────────────────────────────────
 * RegisterRequest DTO
 * Location: dto/RegisterRequest.java
 * ─────────────────────────────────────────────────────────
 *
 * WHY THIS FILE EXISTS:
 * A DTO (Data Transfer Object) is a simple class that carries data
 * between layers. It's NOT an entity — it doesn't map to a table.
 *
 * WHY NOT USE THE User ENTITY DIRECTLY?
 * 1. Security: The User entity has fields we don't want exposed (like password hash).
 * 2. Validation: We validate user INPUT separately from what's stored in DB.
 * 3. Flexibility: The API shape can differ from the DB shape.
 *
 * VALIDATION ANNOTATIONS (Bean Validation):
 * These run automatically when @Valid is used on a @RequestBody in a controller.
 *
 * @NotBlank   → must not be null AND must have at least one non-whitespace character
 * @Email      → validates email format (must contain @)
 * @Size(min,max) → string length constraint
 *
 * HOW IT CONNECTS:
 * POST /api/auth/register → JSON body → deserialized into RegisterRequest
 *                                     → validated → AuthService uses it to create User
 */
@Data  // Lombok: generates getters, setters, toString, equals, hashCode
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
