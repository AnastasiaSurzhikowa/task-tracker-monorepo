package com.tasktracker.userservice.service;

import com.tasktracker.userservice.dto.UserRequest;
import com.tasktracker.userservice.dto.UserResponse;
import com.tasktracker.userservice.entity.User;
import com.tasktracker.userservice.exception.UserNotFoundException;
import com.tasktracker.userservice.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(UserRequest request) {
        User user = new User(request.getFirstName(), request.getLastName(), request.getEmail());
        User savedUser = userRepository.save(user);
        return toResponse(savedUser);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        return toResponse(user);
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail());
    }
}