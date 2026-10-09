package com.example.user.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.user.repository.UserRepository;
import com.example.category.service.CategoryService;
import com.example.common.throwables.exception.DuplicateEmailException;
import com.example.common.throwables.exception.InvalidRequestException;
import com.example.common.throwables.exception.NotFoundException;
import com.example.event.service.EventService;
import com.example.note.service.NoteService;
import com.example.reminder.service.ReminderService;
import com.example.task.service.TaskService;
import com.example.user.dto.RegisterRequest;
import com.example.user.dto.UserResponse;
import com.example.user.mapper.UserMapper;
import com.example.user.model.UserEntity;

/**
 * 
 * <b>UserService</b><br>
 * 
 * Service class that handles all business logic related to user, such as registration, account deletion, and username updates, as well as helper methods returning current user data.
 * The class does not include user authentication, it is handled by Spring Security.
 * 
 */
@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserService currentUserService;
    private final TaskService taskService;
    private final NoteService noteService;
    private final EventService eventService;
    private final CategoryService categoryService;
    private final ReminderService reminderService;
    private final UserMapper mapper;


    public UserService(
        UserRepository userRepo,
        PasswordEncoder passwordEncoder,
        CurrentUserService currentUserService,
        TaskService taskService,
        NoteService noteService,
        EventService eventService,
        CategoryService categoryService,
        ReminderService reminderService,
        UserMapper mapper
    ) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.currentUserService = currentUserService;
        this.taskService = taskService;
        this.noteService = noteService;
        this.eventService = eventService;
        this.categoryService = categoryService;
        this.reminderService = reminderService;
        this.mapper = mapper;
    }

    /**
     * Checks if the user with the provided email already exists.
     * If not, registartion request is validated and new user is added to the database.
     * @param request   a registartion request DTO with user data
     * @return          a newly created UserEntity object
     */
    public UserEntity registerUser(RegisterRequest request) {
        
        if (userRepo.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        validateRegistartionRequest(request);
        UserEntity user = new UserEntity(request.email(), request.username(), passwordEncoder.encode(request.password()));

        return userRepo.save(user);
    }

    /**
     * Returns UserEntity object found by the ID
     * @param idUser    an ID of the user
     * @return          UserEntity object with user data
     */
    public UserEntity getUserById(Long idUser) {
        return userRepo.findById(idUser)
            .orElseThrow(() -> new NotFoundException("User", idUser));
    }

    /**
     * Returns data of the current user mapped to user response DTO
     * @return  a UserResponse DTO object
     */
    public UserResponse getUserResponse() {
        UserEntity user = getCurrentUser();
        return mapper.toResponse(user);
    }

    /**
     * Deletes all user data from the database, including tasks, reminders, events, notes, and categories created by the user.
     */
    @Transactional 
    public void deleteAccount() {
        UserEntity user = getCurrentUser();

        reminderService.deleteRemindersByUser(user);
        taskService.deleteTasksByUser(user);
        eventService.deleteEventsByUser(user);
        noteService.deleteNotesByUser(user);
        categoryService.deleteCategoriesByUser(user);

        userRepo.delete(user);
    }

    /**
     * Used to change username of the user
     * @param username  new username provided by the user
     * @return          a response DTO object with updated username
     */
    public UserResponse updateUsername(String username) {
        UserEntity user = getCurrentUser();
        user.setUsername(username);
        return mapper.toResponse(userRepo.save(user));
    }

    /**
     * Validates if provided data during user registartion is correct
     * @param request   a request DTO with user registartion data
     */
    public void validateRegistartionRequest(RegisterRequest request) {
        if (request.email() == null) {
            throw new InvalidRequestException("EMAIL_REQUIRED", "Email is required.");
        }
        if (request.password() == null) {
            throw new InvalidRequestException("PASSWORD_REQUIRED", "Password is required.");
        } else if (request.password().length() < 8) {
            throw new InvalidRequestException("PASSWORD_TOO_SHORT", "Password must be at least 8 characters.");
        }
    }

    /**
     * Helper method used to get data of the current user from the database
     * @return  an object with user data
     */
    public UserEntity getCurrentUser() {
        Long idUser = currentUserService.getCurrentUserId();
        return userRepo.findById(idUser)
            .orElseThrow(() -> new NotFoundException("User", idUser));
    }
}
