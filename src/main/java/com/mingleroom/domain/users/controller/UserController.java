package com.mingleroom.domain.users.controller;

import com.mingleroom.domain.users.entity.User;
import com.mingleroom.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/user")
    public ResponseEntity<List<User>> getAllUser(){
        return  ResponseEntity.ok(userRepository.findAll());
    }
}
