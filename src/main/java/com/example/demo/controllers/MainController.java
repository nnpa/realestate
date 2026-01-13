/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controllers;

import com.example.demo.models.Build;
import com.example.demo.models.User;
import com.example.demo.repo.BuildRepository;
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
import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.jdbc.core.JdbcTemplate;

@Controller
public class MainController {
        @Autowired
        private BuildRepository buildPerpository;
        @Autowired
        private EmailService emailService;
        
	@GetMapping("/")
	public String home( Model model) {
            Iterable<Build> builds = buildPerpository.findByUserId(123L);
                model.addAttribute("builds", builds);
		model.addAttribute("name", "test");
                
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
    
    @Transactional
    @PostMapping("/registration")
    public ResponseEntity<String> addUser(User user, Map<String, Object> model) {
        User userFromDb = userRepo.findByUsername(user.getUsername());

        if (userFromDb != null) {
            return new ResponseEntity<>("User exists!",HttpStatus.BAD_REQUEST);
        }
        userFromDb = userRepo.findByEmail(user.getEmail());
        
        if (userFromDb != null) {
            return new ResponseEntity<>("Email exists!",HttpStatus.BAD_REQUEST);
        }
        
        String verificationToken = getSaltString();
        user.setVereficationToken(verificationToken);
        
        userRepo.save(user);
        userRepo.insertRole(user.getUsername());
        emailService.SendVerificationEmail(user.getEmail(), verificationToken);
        return new ResponseEntity<>("Вы удачно зарегистрированны подтвердите email",HttpStatus.OK);
        //return "redirect:/login";
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
        User user = userRepo.findByVereficationToken(token);
        
        if(user == null){

        }
        user.setVereficationToken(null);
        user.setIsVeriefied(true);
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