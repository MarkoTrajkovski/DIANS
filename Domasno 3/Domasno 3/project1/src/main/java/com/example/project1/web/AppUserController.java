package com.example.project1.web;

import com.example.project1.service.AppUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class AppUserController {

    private final AppUserService appUserService;

    @PostMapping("/register")
    public String addUser(@RequestParam(name = "username") String username,
                          @RequestParam(name = "password") String password,
                          Model model) {
        try {
            appUserService.addUser(username, password);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("text", "Username already exists");
            return "register";
        }
    }
}
