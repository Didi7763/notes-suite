package com.notes.service;

import com.notes.model.User;
import com.notes.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        logger.debug("Loading user by email: {}", email);
        
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new UsernameNotFoundException("User Not Found with email: " + email);
                });

        // Vérification que l'utilisateur est actif
        if (!user.getIsActive()) {
            logger.warn("Inactive user attempted to login: {}", email);
            throw new UsernameNotFoundException("User account is inactive: " + email);
        }

        logger.debug("Successfully loaded user: {}", email);
        return user;
    }

    @Transactional
    public UserDetails loadUserById(Long id) throws UsernameNotFoundException {
        logger.debug("Loading user by ID: {}", id);
        
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new UsernameNotFoundException("User Not Found with ID: " + id);
                });

        // Vérification que l'utilisateur est actif
        if (!user.getIsActive()) {
            logger.warn("Inactive user attempted to access: {}", id);
            throw new UsernameNotFoundException("User account is inactive: " + id);
        }

        logger.debug("Successfully loaded user by ID: {}", id);
        return user;
    }
}