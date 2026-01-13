/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.demo.controllers;

import com.example.demo.models.Build;
import com.example.demo.models.User;
import com.example.demo.repo.BuildRepository;
import com.example.demo.repo.UserRepo;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

/**
 *
 * @author User
 */
@Controller
public class BuildController {
    @Autowired
    private BuildRepository buildRepository;
        
    private static final String UPLOAD_DIR = "uploads/";
    @Autowired
    private UserRepo userRepo;
        @PreAuthorize("isAuthenticated()")
        @GetMapping("/dashboard")
	public String home( Model model) {
                     Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            
        }
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String username = userDetails.getUsername();
        
        User user = userRepo.findByUsername(username);
        
        
                Iterable<Build> builds = buildRepository.findByUserId(user.getId());
                model.addAttribute("builds", builds);
                return "dashboard";
	}
        
        // показать форму
    @GetMapping("/new")
    public String showForm(Model model) {
        
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()
                || auth.getPrincipal().equals("anonymousUser")) {
            
        }
        
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String username = userDetails.getUsername();
        
        User user = userRepo.findByUsername(username);
        
        model.addAttribute("user_id", user.getId());
        return "create";
    }

    // обработка формы
    @PostMapping("/create")
    public String createBuild(
            @RequestParam String name,
            @RequestParam int user_id,
            @RequestParam String description,
            @RequestParam String address,
            @RequestParam String flor,
            @RequestParam String rooms,
            @RequestParam String cost,
            @RequestParam String square,
            @RequestParam String phone,

            @RequestParam("image") MultipartFile image
    ) throws IOException {

        if (image.isEmpty()) {
            throw new RuntimeException("Файл не выбран");
        }

        Path uploadDir = Paths.get(UPLOAD_DIR).toAbsolutePath();
            Files.createDirectories(uploadDir);

            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);

            // 🔥 ВОТ ЭТА СТРОКА
            image.transferTo(filePath.toFile());
        
        // сохраняем в БД
        Build build = new Build();
        build.setName(name);
        build.setUser_id(user_id);
        build.setDescription(description);
        build.setAddress(address);
        build.setFlor(flor);
        build.setImg(filename);
        build.setRooms(rooms);
        build.setSquare(square);
        build.setCost(cost);
        build.setPhone(phone);

        buildRepository.save(build);
        
        return "redirect:/dashboard";
    }
    
    @GetMapping("/image/{filename}")
    @ResponseBody
    public ResponseEntity<Resource> getImage(@PathVariable String filename)
            throws IOException {

        Path file = Paths.get("uploads").resolve(filename).toAbsolutePath();
        Resource resource = new UrlResource(file.toUri());

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG) // можно определить автоматически
                .body(resource);
    }
    
    // форма редактирования
    @GetMapping("/edit/{id}")
    public String editBuildForm(@PathVariable Long id, Model model) {
        Build build = buildRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Build not found"));
        model.addAttribute("build", build);
        return "edit";
    }
    
    // обработка редактирования
    @PostMapping("/edit/{id}")
    public String editBuild(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam String address,
            @RequestParam String flor,
            @RequestParam String rooms,
            @RequestParam String cost,
            @RequestParam String square,
            @RequestParam String phone,

            @RequestParam(value = "image", required = false) MultipartFile image
    ) throws IOException {

        Build build = buildRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Build not found"));

        // обновляем поля
        build.setName(name);
        build.setDescription(description);
        build.setAddress(address);
        build.setFlor(flor);
        build.setRooms(rooms);
        build.setSquare(square);
        build.setCost(cost);
        build.setPhone(phone);

        // сохраняем новую картинку, если выбрана
        if (image != null && !image.isEmpty()) {
            Path uploadDir = Paths.get("uploads").toAbsolutePath();
            Files.createDirectories(uploadDir);

            String filename = UUID.randomUUID() + "_" + image.getOriginalFilename();
            Path filePath = uploadDir.resolve(filename);
            image.transferTo(filePath.toFile());

            build.setImg(filename);
        }

        // user_id НЕ меняется
        buildRepository.save(build);

        return "redirect:/dashboard";
    }
    
        // Удаление Build по id
    @GetMapping("/delete/{id}")
    public String deleteBuild(@PathVariable Long id) throws IOException {
        Build build = buildRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Build not found"));

        // Удаляем файл картинки, если он существует
        if (build.getImg() != null && !build.getImg().isEmpty()) {
            Path imgPath = Paths.get("uploads").resolve(build.getImg()).toAbsolutePath();
            Files.deleteIfExists(imgPath);  // безопасно удаляет файл, если есть
        }

        // Удаляем запись из БД
        buildRepository.delete(build);

        return "redirect:/dashboard";
    }
}
