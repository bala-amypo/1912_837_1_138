package com.example.demo.security;

import com.example.demo.model.Guest;
import com.example.demo.repository.GuestRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final GuestRepository guestRepository;

    public CustomUserDetailsService(GuestRepository guestRepository) {
        this.guestRepository = guestRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Guest guest = guestRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email: " + email));

        return new User(
                guest.getEmail(),
                guest.getPassword(),
                guest.getActive(),
                true,
                true,
                true,
                Collections.singleton(
                        new SimpleGrantedAuthority(guest.getRole())
                )
        );
    }
}
