package com.nikoloz.secureapp.controller;

import com.nikoloz.secureapp.model.AppUser;
import com.nikoloz.secureapp.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String adminDashboard(Authentication authentication, Model model) {
        log.info("Admin dashboard accessed by user '{}'", authentication.getName());

        List<AppUser> allUsers = userService.getAllUsers();

        long adminCount   = allUsers.stream().filter(u -> "ROLE_ADMIN".equals(u.getRole())).count();
        long userCount    = allUsers.stream().filter(u -> "ROLE_USER".equals(u.getRole())).count();
        long disabledCount = allUsers.stream().filter(u -> !u.isEnabled()).count();

        log.debug("Dashboard stats — total: {}, admins: {}, users: {}, disabled: {}",
                allUsers.size(), adminCount, userCount, disabledCount);

        model.addAttribute("totalUsers",    allUsers.size());
        model.addAttribute("adminCount",    adminCount);
        model.addAttribute("userCount",     userCount);
        model.addAttribute("disabledCount", disabledCount);

        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(Model model) {
        List<AppUser> users = userService.getAllUsers();
        log.debug("User list page loaded — {} users in table", users.size());
        model.addAttribute("users", users);
        return "admin/users";
    }

    @PostMapping("/users/{id}/disable")
    public String disableUser(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        log.info("Admin '{}' is disabling user id {}", authentication.getName(), id);
        try {
            userService.disableUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User account has been disabled.");
        } catch (Exception e) {
            log.error("Failed to disable user id {} — reason: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not disable user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/enable")
    public String enableUser(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        log.info("Admin '{}' is enabling user id {}", authentication.getName(), id);
        try {
            userService.enableUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User account has been enabled.");
        } catch (Exception e) {
            log.error("Failed to enable user id {} — reason: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not enable user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        log.info("Admin '{}' is deleting user id {}", authentication.getName(), id);
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User has been deleted.");
        } catch (Exception e) {
            log.error("Failed to delete user id {} — reason: {}", id, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Could not delete user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
