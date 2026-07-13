package com.caique.AdvancedCrud.auth;

import com.caique.AdvancedCrud.auth.dto.LoginRequest;
import com.caique.AdvancedCrud.auth.dto.RefreshRequest;
import com.caique.AdvancedCrud.auth.dto.RegisterRequest;
import com.caique.AdvancedCrud.auth.dto.TokenResponse;
import com.caique.AdvancedCrud.auth.rateLimit.LoginRateLimitService;
import com.caique.AdvancedCrud.auth.rateLimit.RefreshRateLimitService;
import com.caique.AdvancedCrud.auth.token.RefreshTokenService;
import com.caique.AdvancedCrud.auth.token.TokenService;
import com.caique.AdvancedCrud.shared.exceptions.InvalidRefreshTokenException;
import com.caique.AdvancedCrud.user.UserService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
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
    private final RefreshRateLimitService refreshRateLimitService;
    private final AuthenticationManager authenticationManager;

    private final Counter loginSuccessCounter;
    private final Counter loginFailedCounter;

    public AuthService(UserService userService, UserDetailsServiceImpl userDetailsService, TokenService tokenService, RefreshTokenService refreshTokenService, LoginRateLimitService loginRateLimitService, RefreshRateLimitService refreshRateLimitService, AuthenticationManager authenticationManager, MeterRegistry meterRegistry) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
        this.refreshTokenService = refreshTokenService;
        this.loginRateLimitService = loginRateLimitService;
        this.refreshRateLimitService = refreshRateLimitService;
        this.authenticationManager = authenticationManager;

        this.loginSuccessCounter = Counter.builder("auth.login")
                .tag("result", "success")
                .description("Login Attempts")
                .register(meterRegistry);

        this.loginFailedCounter = Counter.builder("auth.login")
                .tag("result", "failed")
                .description("Login Attempts")
                .register(meterRegistry);
    }

    public void register(RegisterRequest request) {
        this.userService.createUser(request.name(), request.email(), request.password());
    }

    public TokenResponse login(LoginRequest request, String ip) {
        loginRateLimitService.checkAllowed(request.email(), ip);

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password()));

            loginRateLimitService.reset(request.email());

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
            UUID userId = userDetails.getUser().getPublicId();

            String accessToken = tokenService.generateAccessToken(userDetails);
            UUID refreshToken = refreshTokenService.createNewToken(userId);

            loginSuccessCounter.increment();
            return new TokenResponse(accessToken, refreshToken.toString(), tokenService.accessTokenTtlSeconds());
        } catch (AuthenticationException e) {
            loginRateLimitService.recordFailure(request.email(), ip);
            loginFailedCounter.increment();
            throw e;
        }
    }

    public TokenResponse refresh(RefreshRequest request, String ip) {
        refreshRateLimitService.checkAllowed(ip);

        UUID tokenId;
        try {
            tokenId = UUID.fromString(request.refreshToken());
        } catch (IllegalArgumentException e) {
            throw new InvalidRefreshTokenException();
        }

        RefreshTokenService.RotationResult result = refreshTokenService.rotate(tokenId);

        UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByPublicId(result.userId());
        if(!userDetails.isEnabled()) {
            refreshTokenService.revokeAllSessions(userDetails.getUser().getPublicId());
            throw new DisabledException("User not active");
        }
        String accessToken = tokenService.generateAccessToken(userDetails);

        return new TokenResponse(accessToken, result.newTokenId().toString(), tokenService.accessTokenTtlSeconds());
    }

}
