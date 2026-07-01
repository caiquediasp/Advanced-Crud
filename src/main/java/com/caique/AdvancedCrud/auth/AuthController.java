package com.caique.AdvancedCrud.auth;

import com.caique.AdvancedCrud.auth.dto.LoginRequest;
import com.caique.AdvancedCrud.auth.dto.RegisterRequest;
import com.caique.AdvancedCrud.auth.dto.TokenResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody RegisterRequest request) {
        this.authService.register(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return this.authService.login(request);
    }

}
