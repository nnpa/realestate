/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.repo;


import com.example.demo.models.User;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;

public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);
    User findByEmail(String email);
    //@NativeQuery(value = "SELECT * FROM `user` WHERE verefication_token = ?1")
    User findByVereficationToken(String token);
    
    @Modifying
    @Transactional
    @Query(
        value = "INSERT INTO `user_roles` (username, role) VALUES (?1, 'ROLE_USER')",
        nativeQuery = true
    )
    void insertRole(String username);

}