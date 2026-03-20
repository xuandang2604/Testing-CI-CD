package com.example.xuandangwebbanhang.dto.api;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserApiRequest {
    @Size(min = 1, max = 50)
    private String username;

    // Frontend currently sends name; backend maps it to username if username is empty.
    private String name;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    private String phoneNumber;
    private String role;
    private String dateOfBirth;
    private String address;
    private String provider;
}
