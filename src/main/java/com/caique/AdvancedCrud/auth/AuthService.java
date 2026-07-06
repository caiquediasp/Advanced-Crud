package com.caique.AdvancedCrud.auth;

import com.caique.AdvancedCrud.auth.dto.LoginRequest;
import com.caique.AdvancedCrud.auth.dto.RefreshRequest;
import com.caique.AdvancedCrud.auth.dto.RegisterRequest;
import com.caique.AdvancedCrud.auth.dto.TokenResponse;
import com.caique.AdvancedCrud.auth.rateLimit.LoginRateLimitService;
import com.caique.AdvancedCrud.auth.token.RefreshTokenService;
import com.caique.AdvancedCrud.auth.token.TokenService;
import com.caique.AdvancedCrud.shared.exceptions.InvalidRefreshTokenException;
import com.caique.AdvancedCrud.user.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserService userService;
    private final UserDetailsServiceImpl userDetailsService;
    private final TokenService tokenService;
    private final RefreshTokenService refreshTokenService;
    private final LoginRateLimitService loginRateLimitService;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserService userService, UserDetailsServiceImpl userDetailsService, TokenService tokenService, RefreshTokenService refreshTokenService, LoginRateLimitService loginRateLimitService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.loginRateLimitService = loginRateLimitService;
        this.authenticationManager = authenticationManager;
    }

    public void register(RegisterRequest request) {
        this.userService.createUser(request.name(), request.email(), request.password());
    }

    public TokenResponse login(LoginRequest request, String ip) {
        loginRateLimitService.checkAllowed(request.email(), ip);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            loginRateLimitService.reset(request.email(), ip);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            UUID userId = userDetails.getUser().getPublicId();

            String accessToken = tokenService.generateAccessToken(userDetails);
            UUID refreshToken = refreshTokenService.createNewToken(userId);

            return new TokenResponse(accessToken, refreshToken.toString(), tokenService.accessTokenTtlSeconds());
        } catch (AuthenticationException e) {
            loginRateLimitService.recordFailure(request.email(), ip);
            throw e;
        }
    }

    public TokenResponse refresh(RefreshRequest request) {
        UUID tokenId;
        try {
            tokenId = UUID.fromString(request.refreshToken());
        } catch (IllegalArgumentException e) {
            throw new InvalidRefreshTokenException();
        }

        RefreshTokenService.RotationResult result = refreshTokenService.rotate(tokenId);

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByPublicId(result.userId());
        String accessToken = tokenService.generateAccessToken(userDetails);

        return new TokenResponse(accessToken, result.newTokenId().toString(), tokenService.accessTokenTtlSeconds());
    }

}
