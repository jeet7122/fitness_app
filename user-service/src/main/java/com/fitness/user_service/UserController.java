package com.fitness.user_service;

import com.fitness.user_service.dto.RegisterRequest;
import com.fitness.user_service.dto.UserResponse;
import com.fitness.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private UserService userService;

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUserProfile(@PathVariable String userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request){
        return ResponseEntity.ok(userService.register(request));
    }
}
