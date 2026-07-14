package com.caique.advancedcrud.auth;

import com.caique.advancedcrud.auth.dto.LoginRequest;
import com.caique.advancedcrud.auth.dto.RefreshRequest;
import com.caique.advancedcrud.auth.dto.RegisterRequest;
import com.caique.advancedcrud.auth.dto.TokenResponse;
import jakarta.servlet.http.HttpServletRequest;
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
    public TokenResponse login(@Valid @RequestBody LoginRequest request,
                               HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        return this.authService.login(request, ip);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request,
                                 HttpServletRequest http) {
        String ip = http.getRemoteAddr();
        return this.authService.refresh(request, ip);
    }

}
