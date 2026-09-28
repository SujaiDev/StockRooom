package com.inventorymanagement.service;

import java.util.Locale;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.inventorymanagement.repository.InventoryUserRepository;

@Service
public class InventoryUserDetailsService implements UserDetailsService {

    private final InventoryUserRepository users;

    public InventoryUserDetailsService(InventoryUserRepository users) {
        this.users = users;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        var account = users.findByEmailIgnoreCase(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return User.withUsername(account.getEmail()).password(account.getPasswordHash())
                .roles(account.getRole()).build();
    }
}