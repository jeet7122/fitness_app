package com.fitness.user_service.dto;

import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.lang.annotation.Target;

@Data
public class RegisterRequest {
    @NotBlank(message = "Email is required!")
    @Email(message = "Invalid Email Format!")

    private String email;
    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "password must have at-least 6 characters")
    private String password;
    private String firstName;
    private String lastName;
}
