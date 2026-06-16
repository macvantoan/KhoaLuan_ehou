package com.phonestore.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 100)
    private String password;

    @NotBlank
    private String fullName;

    @Pattern(regexp = "^(0[3-9]\\d{8})$")
    private String phone;

    private String gender;
    private Boolean newsletter;
}
