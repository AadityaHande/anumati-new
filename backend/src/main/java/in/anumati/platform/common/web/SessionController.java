package in.anumati.platform.common.web;

import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import in.anumati.platform.common.security.AuthSessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.web.csrf.CsrfToken;

import java.util.List;

@RestController
@RequestMapping("/api/v1/session")
public class SessionController {
    private final AuthenticationManager authenticationManager;
    private final AuthSessionService sessionService;

    public SessionController(AuthenticationManager authenticationManager, AuthSessionService sessionService) {
        this.authenticationManager = authenticationManager;
        this.sessionService = sessionService;
    }

    @SecurityRequirements
    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @SecurityRequirements
    @PostMapping("/login")
    public SessionResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken.unauthenticated(request.username(), request.password()));
        AuthSessionService.IssuedSession session = sessionService.create(authentication.getName());
        response.addHeader(HttpHeaders.SET_COOKIE, sessionService.sessionCookie(session.rawToken()).toString());
        return response(authentication, session.expiresAt());
    }

    @GetMapping
    public SessionResponse current(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Authentication is required");
        }
        return response(authentication, null);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String rawToken = readCookie(request, sessionService.cookieName());
        sessionService.revoke(rawToken);
        response.addHeader(HttpHeaders.SET_COOKIE, sessionService.clearCookie().toString());
    }

    private SessionResponse response(Authentication authentication, java.time.Instant expiresAt) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replaceFirst("^ROLE_", ""))
                .sorted()
                .toList();
        return new SessionResponse(authentication.getName(), roles, expiresAt);
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) if (name.equals(cookie.getName())) return cookie.getValue();
        return null;
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record SessionResponse(String username, List<String> roles, java.time.Instant expiresAt) {}
}
