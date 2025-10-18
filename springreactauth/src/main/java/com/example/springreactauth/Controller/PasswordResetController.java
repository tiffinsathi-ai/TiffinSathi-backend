package com.example.springreactauth.Controller;

import com.example.springreactauth.Entity.User;
import com.example.springreactauth.Repository.UserRepository;
import com.example.springreactauth.Service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Optional;
import java.util.Random;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PasswordResetController { // Consider adding these methods to AuthController

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder encoder;

    private static final long OTP_VALID_DURATION = 5 * 60 * 1000; // 5 minutes

    // DTOs for request bodies are omitted for brevity, assuming simple String/object inputs

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email not found!");
        }

        User user = userOptional.get();
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setResetPasswordOtp(otp);
        user.setOtpExpirationTime(Instant.now().toEpochMilli() + OTP_VALID_DURATION);
        userRepository.save(user);

        String subject = "Password Reset OTP";
        String content = "Your OTP for password reset is: " + otp + ". It is valid for 5 minutes.";
        emailService.sendSimpleMessage(user.getEmail(), subject, content);

        return ResponseEntity.ok("Password reset OTP sent to email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String email, @RequestParam String otp, @RequestParam String newPassword) {
        Optional<User> userOptional = userRepository.findByEmail(email);

        if (!userOptional.isPresent()) {
            return ResponseEntity.badRequest().body("Error: User not found!");
        }

        User user = userOptional.get();
        long currentTime = Instant.now().toEpochMilli();

        if (user.getResetPasswordOtp() == null || !user.getResetPasswordOtp().equals(otp)) {
            return ResponseEntity.badRequest().body("Error: Invalid OTP.");
        }

        if (user.getOtpExpirationTime() == null || user.getOtpExpirationTime() < currentTime) {
            return ResponseEntity.badRequest().body("Error: OTP has expired.");
        }

        user.setPassword(encoder.encode(newPassword));
        user.setResetPasswordOtp(null);
        user.setOtpExpirationTime(null);
        userRepository.save(user);

        return ResponseEntity.ok("Password reset successfully!");
    }
}
