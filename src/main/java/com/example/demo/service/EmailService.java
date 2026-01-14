package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final AsyncEmailService async;

    public EmailService(AsyncEmailService async) {
        this.async = async;
    }

    public void sendVerificationEmail(String email, String token) {
        String message =
                "Подтвердите email: http://localhost:8181/verify?token=" + token;

        async.send(email, "Подтверждение", message, false);
    }

    public void sendNewPassword(String email, String password) {

        async.send(
                email,
                "Новый пароль",
                "Ваш новый пароль: " + password,
                false
        );
    }
}
