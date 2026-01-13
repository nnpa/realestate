/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 *
 * @author User
 */
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender; 
    
    @Value("${spring.mail.username}")
    private String from;
    
    public void SendVerificationEmail(String email,String token){
        String subject = "Подтверждение";
        String path = "/verify";
        String message = "Подтвердите email: http://localhost:8181/verify?token=" + token;

        sendEmail(email,token,subject,path,message);
    }

    private void sendEmail(String email, String token, String subject, String path, String message) {
        

        try {
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setFrom(from);
            helper.setText(message,true);
            
            mailSender.send(mimeMessage);
            
            
        } catch (MessagingException ex) {
            System.getLogger(EmailService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
        
    }

    public void sendNewPassword(String email, String password) {
                try {
            
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
            
            helper.setTo(email);
            helper.setSubject("new password");
            helper.setFrom(from);
            helper.setText("password: " + password,false);
            
            mailSender.send(mimeMessage);
            
            
        } catch (MessagingException ex) {
            System.getLogger(EmailService.class.getName()).log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    
    
}
