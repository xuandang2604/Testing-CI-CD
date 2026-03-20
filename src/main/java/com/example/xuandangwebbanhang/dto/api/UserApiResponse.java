package com.example.xuandangwebbanhang.dto.api;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserApiResponse {
    private Long id;
    private String username;
    private String name;
    private String email;
    private String phone;
    private String phoneNumber;
    private String provider;
    private String role;
    private String dateOfBirth;
    private String address;
    private String createdAt;
    private List<String> roles;
}
