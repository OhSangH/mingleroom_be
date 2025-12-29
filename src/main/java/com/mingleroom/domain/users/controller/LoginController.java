package com.mingleroom.domain.users.controller;

import com.mingleroom.domain.users.repository.UserRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LoginController {

    @PostMapping("/login")
    public String login() {
        return "login";
    }

}
