package com.example.EcoBazaar_module2.service;

import com.example.EcoBazaar_module2.model.PasswordResetToken;
import com.example.EcoBazaar_module2.model.Role;
import com.example.EcoBazaar_module2.model.User;
import com.example.EcoBazaar_module2.repository.PasswordResetTokenRepository;
import com.example.EcoBazaar_module2.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.frontend.url:http://localhost:3000}")
    private String frontendUrl;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Transactional
    public User registerUser(String email, String password, String fullName, Role role) {
        // Validate email is not already in use
        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // IMPORTANT: Restrict role creation - only SELLER and USER can be created via signup
        if (role == Role.ADMIN) {
            throw new RuntimeException("Admin accounts cannot be created through registration. Please contact system administrator.");
        }

        // Only allow SELLER and USER roles
        if (role != Role.SELLER && role != Role.USER) {
            throw new RuntimeException("Invalid role. Only SELLER and USER (Shopper) roles are allowed for registration.");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setRole(role);
        user.setActive(true);
        user.setTotalCarbonSaved(0.0);
        user.setEcoScore(0);

        return userRepository.save(user);
    }

    public User authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!user.isActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        return user;
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void initiatePasswordReset(String email) {
        System.out.println("Password reset initiated for: " + email);

        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isEmpty()) {
                System.out.println("User not found: " + email);
                throw new RuntimeException("No user found with this email address");
            }

            User user = userOptional.get();
            System.out.println("User found: " + user.getEmail());

            // Delete any existing tokens for this user
            passwordResetTokenRepository.deleteByUser(user);
            System.out.println("Existing tokens deleted");

            // Create new token
            String token = UUID.randomUUID().toString();
            PasswordResetToken passwordResetToken = new PasswordResetToken(token, user);
            passwordResetTokenRepository.save(passwordResetToken);

            System.out.println("Token created: " + token);

            // Send email
            sendPasswordResetEmail(user, token);
            System.out.println("Email sent successfully");

        } catch (Exception e) {
            System.err.println("Error in initiatePasswordReset: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to initiate password reset: " + e.getMessage());
        }
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        System.out.println("Resetting password with token: " + token);

        try {
            PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                    .orElseThrow(() -> {
                        System.out.println("Token not found: " + token);
                        return new RuntimeException("Invalid or expired token");
                    });

            System.out.println("Token found, checking expiration...");

            if (passwordResetToken.isExpired()) {
                passwordResetTokenRepository.delete(passwordResetToken);
                System.out.println("Token expired, deleted: " + token);
                throw new RuntimeException("Token has expired");
            }

            User user = passwordResetToken.getUser();
            System.out.println("Resetting password for user: " + user.getEmail());

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            // Delete the token after successful reset
            passwordResetTokenRepository.delete(passwordResetToken);
            System.out.println("Password reset successful for user: " + user.getEmail());

        } catch (Exception e) {
            System.err.println("Error in resetPassword: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    public boolean validatePasswordResetToken(String token) {
        System.out.println("Validating token: " + token);

        try {
            Optional<PasswordResetToken> passwordResetToken = passwordResetTokenRepository.findByToken(token);
            if (passwordResetToken.isEmpty()) {
                System.out.println("Token not found: " + token);
                return false;
            }

            boolean isValid = !passwordResetToken.get().isExpired();
            System.out.println("Token valid: " + isValid);
            return isValid;
        } catch (Exception e) {
            System.err.println("Error validating token: " + e.getMessage());
            return false;
        }
    }

    private void sendPasswordResetEmail(User user, String token) {
        try {
            String resetUrl = frontendUrl + "/reset-password?token=" + token;

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(user.getEmail());
            message.setSubject("Password Reset Request - EcoBazaar");
            message.setText("Dear " + user.getFullName() + ",\n\n" +
                    "You requested a password reset for your EcoBazaar account.\n\n" +
                    "To reset your password, please click the link below:\n" +
                    resetUrl + "\n\n" +
                    "This link will expire in 24 hours.\n\n" +
                    "If you didn't request this reset, please ignore this email.\n\n" +
                    "Best regards,\nEcoBazaar Team");

            mailSender.send(message);
            System.out.println("Password reset email sent to: " + user.getEmail());
        } catch (Exception e) {
            System.err.println("Failed to send email: " + e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
    }
}
