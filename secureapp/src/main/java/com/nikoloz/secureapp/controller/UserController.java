package com.nikoloz.secureapp.controller;

import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Optional;

@Slf4j
@Controller
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    @PreAuthorize("isAuthenticated()")
    public String dashboard(Authentication authentication, Model model) {
        String username = authentication.getName();
        log.info("User '{}' accessed dashboard", username);

        Optional<AppUser> userOpt = userService.findByUsername(username);
        userOpt.ifPresent(user -> {
            model.addAttribute("user",        user);
            model.addAttribute("displayName", user.getDisplayName());
        });

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("isAdmin",  isAdmin);
        model.addAttribute("username", username);
        return "dashboard";
    }

    @GetMapping("/profile")
    @PreAuthorize("isAuthenticated()")
    public String profile(Authentication authentication, Model model) {
        String username = authentication.getName();
        log.debug("User '{}' viewing profile page", username);
        userService.findByUsername(username).ifPresent(u -> model.addAttribute("user", u));
        return "user/profile";
    }
}
