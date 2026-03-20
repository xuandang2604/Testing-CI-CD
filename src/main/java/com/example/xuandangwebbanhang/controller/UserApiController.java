package com.example.xuandangwebbanhang.controller;

import com.example.xuandangwebbanhang.dto.api.UserApiRequest;
import com.example.xuandangwebbanhang.dto.api.UserApiResponse;
import com.example.xuandangwebbanhang.model.BookUser;
import com.example.xuandangwebbanhang.repository.BookUserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;

@CrossOrigin(origins = {"http://127.0.0.1:5500", "http://localhost:5500"})
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserApiController {
    private final BookUserRepository userRepository;

    @GetMapping
    public List<UserApiResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserApiResponse> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> ResponseEntity.ok(toResponse(user)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createUser(@Valid @RequestBody UserApiRequest request) {
        if (!StringUtils.hasText(request.getName()) || !StringUtils.hasText(request.getEmail())) {
            return ResponseEntity.badRequest().body("Ten va email la bat buoc");
        }
        if (!StringUtils.hasText(request.getPassword())) {
            return ResponseEntity.badRequest().body("Mat khau la bat buoc");
        }

        BookUser user = new BookUser();
        applyRequest(user, request, false);
        BookUser saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // Frontend hien tai dang goi PUT/DELETE, nen giu de UI chay on dinh.
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody UserApiRequest request) {
        return userRepository.findById(id)
                .map(existing -> {
                    applyRequest(existing, request, true);
                    BookUser saved = userRepository.save(existing);
                    return ResponseEntity.ok(toResponse(saved));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyRequest(BookUser user, UserApiRequest request, boolean isUpdate) {
        if (StringUtils.hasText(request.getName())) {
            user.setName(request.getName().trim());
        }
        if (StringUtils.hasText(request.getEmail())) {
            user.setEmail(request.getEmail().trim());
        }

        String password = request.getPassword();
        if (StringUtils.hasText(password) && (!isUpdate || !"defaultPassword".equals(password))) {
            user.setPassword(password);
        }

        if (StringUtils.hasText(request.getRole())) {
            user.setRole(request.getRole().trim());
        } else if (!isUpdate && !StringUtils.hasText(user.getRole())) {
            user.setRole("USER");
        }

        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }

        String phone = request.getPhoneNumber();
        if (!StringUtils.hasText(phone)) {
            phone = request.getPhone();
        }
        if (phone != null) {
            user.setPhoneNumber(phone);
        }

        if (StringUtils.hasText(request.getDateOfBirth())) {
            try {
                user.setDateOfBirth(LocalDate.parse(request.getDateOfBirth()));
            } catch (DateTimeParseException ex) {
                throw new IllegalArgumentException("Ngay sinh khong dung dinh dang yyyy-MM-dd");
            }
        } else if (!isUpdate) {
            user.setDateOfBirth(null);
        }

        if (!isUpdate && user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDateTime.now());
        }
    }

    private UserApiResponse toResponse(BookUser user) {
        UserApiResponse response = new UserApiResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setUsername(user.getName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setAddress(user.getAddress());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setPhone(user.getPhoneNumber());
        response.setDateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        response.setCreatedAt(user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        response.setRoles(user.getRole() == null ? List.of() : List.of(user.getRole()));
        return response;
    }
}
