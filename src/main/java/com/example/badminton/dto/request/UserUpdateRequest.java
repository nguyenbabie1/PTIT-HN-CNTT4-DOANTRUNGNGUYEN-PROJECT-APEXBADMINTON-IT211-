package com.example.badminton.dto.request;

import com.example.badminton.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {

    @Size(min = 6, max = 100)
    private String password;

    @Size(max = 150)
    private String fullName;

    @Email
    private String email;

    private Role role;

    private Boolean enabled;
}
