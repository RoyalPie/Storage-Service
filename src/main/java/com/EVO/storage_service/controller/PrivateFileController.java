package com.EVO.storage_service.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/private")
public class PrivateFileController {
    @GetMapping("/get-profile")
    public String getProfile(Authentication authentication){
        return "Profile picture"+authentication.getPrincipal();
    }
}
