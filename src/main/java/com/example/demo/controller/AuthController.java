package com.example.demo.controller;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.model.Guest;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.GuestService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final GuestService guestService;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    public AuthController(GuestService guestService,
                          JwtTokenProvider jwtTokenProvider,
                          AuthenticationManager authenticationManager,
                          PasswordEncoder passwordEncoder) {
        this.guestService = guestService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public Guest register(@RequestBody RegisterRequest request) {

        Guest guest = new Guest();
        guest.setEmail(request.getEmail());
        guest.setPassword(request.getPassword());
        guest.setFullName(request.getFullName());
        guest.setPhoneNumber(request.getPhoneNumber());
        guest.setRole(request.getRole());

        return guestService.createGuest(guest);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtTokenProvider.generateToken(authentication);

        Map<String, Object> response = new HashMap<>();
        response.put("token", token);
        response.put("email", request.getEmail());

        return response;
    }
}
