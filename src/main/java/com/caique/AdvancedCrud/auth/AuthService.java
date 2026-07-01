package com.caique.AdvancedCrud.auth;

import com.caique.AdvancedCrud.auth.dto.LoginRequest;
import com.caique.AdvancedCrud.auth.dto.RegisterRequest;
import com.caique.AdvancedCrud.auth.dto.TokenResponse;
import com.caique.AdvancedCrud.auth.token.TokenService;
import com.caique.AdvancedCrud.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final TokenService tokenService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserService userService, TokenService tokenService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationManager = authenticationManager;
    }

    public void register(RegisterRequest request) {
        this.userService.createUser(request.name(), request.email(), request.password());
    }

    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String accessToken = tokenService.generateAccessToken(userDetails);

        return new TokenResponse(accessToken, null, tokenService.accessTokenTtlSeconds());
    }

}
