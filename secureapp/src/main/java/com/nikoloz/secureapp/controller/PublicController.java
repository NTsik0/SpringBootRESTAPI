package com.nikoloz.secureapp.controller;

import com.nikoloz.secureapp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
public class PublicController {

    private final UserService userService;

    public PublicController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping({"/", "/home"})
    public String home(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
            model.addAttribute("isAdmin",
                    authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }
        return "home";
    }

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error",  required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Authentication authentication,
            Model model) {

        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        if (error  != null) model.addAttribute("errorMessage",  "Wrong username or password. Please try again.");
        if (logout != null) model.addAttribute("logoutMessage", "You have been logged out successfully.");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String displayName,
            RedirectAttributes redirectAttributes) {

        log.info("Registration attempt for username '{}'", username);
        try {
            userService.registerUser(username, password, displayName);
            redirectAttributes.addFlashAttribute("successMessage", "Account created! You can now log in.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            log.warn("Registration rejected for '{}' — {}", username, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }
}
