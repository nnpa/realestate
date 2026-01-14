/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controllers;

import com.example.demo.models.Build;
import com.example.demo.models.User;
import com.example.demo.models.UserRole;
import com.example.demo.repo.BuildRepository;
import com.example.demo.repo.RoleRepo;
import com.example.demo.repo.UserRepo;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import com.example.demo.service.EmailService;
import com.example.demo.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.reactive.TransactionSynchronization;
import org.springframework.transaction.reactive.TransactionSynchronizationManager;

@Controller
public class MainController {
        @Autowired
        private RoleRepo roleRepo;
        @Autowired
        private BuildRepository buildPerpository;
        @Autowired
        private EmailService emailService;
        @Autowired
        private UserService userService;
	@GetMapping("/")
	public String home( Model model) {
                
		return "home";
	}
    @Autowired
    private UserRepo userRepo;
    
    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }
    
    @GetMapping("/reset")
    public String reset(){
        
        return "reset";
    }
    
    @PostMapping("/reset2")
    public ResponseEntity<String> reset2(  @RequestParam String email){
        User user = userRepo.findByEmail(email);
        
        if(user == null){
            return new ResponseEntity<>("Email не найден",HttpStatus.BAD_REQUEST);
        }
        String password = getSaltString();
        user.setPassword(password);
        emailService.sendNewPassword(user.getEmail(), password);
        userRepo.save(user);
        return new ResponseEntity<>("вам на email выслан пароль",HttpStatus.OK);
    }
    
@PostMapping("/registration")
public String register(User user) {

    user.setVerificationToken(getSaltString());
    user.setIsVerified(false);

    userService.register(user);

    emailService.sendVerificationEmail(
        user.getEmail(),
        user.getVerificationToken()
    );

    return "successful";
}

    
    
    @GetMapping("/login")
    public String login(){
        return "login";
    }
    
    public String getSaltString() {
        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 12) { // length of the random string.
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            salt.append(SALTCHARS.charAt(index));
        }
        String saltStr = salt.toString();
        return saltStr;

    }
    
    @GetMapping("/verify")
    public String verify(@RequestParam("token") String token){
        User user = userRepo.findByVerificationToken(token);
        
        if(user == null){

        }
        user.setVerificationToken(null);
        user.setIsVerified(true);
        user.setActive(true);


        userRepo.save(user);
        return  "verify";

    }
    
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/test")
    public ResponseEntity<String> test(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            return new ResponseEntity<>("not login",HttpStatus.OK);
        }
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String username = userDetails.getUsername();
        return new ResponseEntity<>(username,HttpStatus.OK);
    }
}