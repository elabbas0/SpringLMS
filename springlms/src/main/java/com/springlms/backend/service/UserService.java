package com.springlms.backend.service;

import com.springlms.backend.model.user.Role;
import com.springlms.backend.model.user.State;
import com.springlms.backend.model.user.StudentProfile;
import com.springlms.backend.model.user.TeacherProfile;
import com.springlms.backend.model.user.User;
import com.springlms.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User registerUser(User user) {
        validateForRegistration(user);

        user.setState(State.PENDING_APPROVAL); // default

        if (user.getStudentProfile() != null) {
            user.getStudentProfile().setUser(user);
        }

        if (user.getTeacherProfile() != null) {
            user.getTeacherProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    public User approveUser(User user) {
        validateUserProfileByRole(user); // check user type
        user.setState(State.ACTIVE);

        if (user.getStudentProfile() != null) {
            user.getStudentProfile().setUser(user);
        }

        if (user.getTeacherProfile() != null) {
            user.getTeacherProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    public User updateUser(User user) {
        if (user.getState() == State.ACTIVE) {
            validateUserProfileByRole(user);
        }

        if (user.getStudentProfile() != null) {
            user.getStudentProfile().setUser(user);
        }

        if (user.getTeacherProfile() != null) {
            user.getTeacherProfile().setUser(user);
        }

        return userRepository.save(user);
    }

    private void validateForRegistration(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required.");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (user.getRole() == null) {
            throw new IllegalArgumentException("User role is required.");
        }

        if (user.getRole() == Role.ADMIN) {
            throw new IllegalArgumentException("Admin users cannot self-register.");
        }

        if (user.getStudentProfile() != null && user.getTeacherProfile() != null) {
            throw new IllegalArgumentException("A user cannot register with both student and teacher profiles.");
        }

        if (user.getRole() == Role.STUDENT && user.getTeacherProfile() != null) {
            throw new IllegalArgumentException("Student users cannot have a teacher profile.");
        }

        if (user.getRole() == Role.TEACHER && user.getStudentProfile() != null) {
            throw new IllegalArgumentException("Teacher users cannot have a student profile.");
        }
    }

    private void validateUserProfileByRole(User user) {
        if (user.getRole() == Role.ADMIN) {
            if (user.getStudentProfile() != null || user.getTeacherProfile() != null) {
                throw new IllegalArgumentException("Admin users cannot have any other profiles.");
            }
        }

        if (user.getRole() == Role.STUDENT) {
            if (user.getStudentProfile() == null) {
                throw new IllegalArgumentException("Student users must have a student profile.");
            }

            if (user.getTeacherProfile() != null) {
                throw new IllegalArgumentException("Student users cannot have a teacher profile.");
            }

            validateStudentProfile(user.getStudentProfile());
        }

        if (user.getRole() == Role.TEACHER) {
            if (user.getTeacherProfile() == null) {
                throw new IllegalArgumentException("Teacher users must have a teacher profile.");
            }

            if (user.getStudentProfile() != null) {
                throw new IllegalArgumentException("Teacher users cannot have a student profile.");
            }

            validateTeacherProfile(user.getTeacherProfile());
        }
    }

    private void validateStudentProfile(StudentProfile profile) {
        if (profile.getStudentNumber() == null || profile.getStudentNumber().isBlank()) {
            throw new IllegalArgumentException("Student number is required.");
        }

        if (profile.getFirstName() == null || profile.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Student first name is required.");
        }

        if (profile.getLastName() == null || profile.getLastName().isBlank()) {
            throw new IllegalArgumentException("Student last name is required.");
        }

        if (profile.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Student date of birth is required.");
        }

        if (profile.getPhoneNumber() == null || profile.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Student phone number is required.");
        }

        if (profile.getAddress() == null || profile.getAddress().isBlank()) {
            throw new IllegalArgumentException("Student address is required.");
        }
    }

    private void validateTeacherProfile(TeacherProfile profile) {
        if (profile.getEmployeeNumber() == null || profile.getEmployeeNumber().isBlank()) {
            throw new IllegalArgumentException("Employee number is required.");
        }

        if (profile.getFirstName() == null || profile.getFirstName().isBlank()) {
            throw new IllegalArgumentException("Teacher first name is required.");
        }

        if (profile.getLastName() == null || profile.getLastName().isBlank()) {
            throw new IllegalArgumentException("Teacher last name is required.");
        }

        if (profile.getDateOfBirth() == null) {
            throw new IllegalArgumentException("Teacher date of birth is required.");
        }

        if (profile.getPhoneNumber() == null || profile.getPhoneNumber().isBlank()) {
            throw new IllegalArgumentException("Teacher phone number is required.");
        }

        if (profile.getAddress() == null || profile.getAddress().isBlank()) {
            throw new IllegalArgumentException("Teacher address is required.");
        }
    }
}