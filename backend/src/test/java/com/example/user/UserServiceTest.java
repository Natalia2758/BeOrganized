package com.example.user;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

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
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;
import com.example.user.service.UserService;

@ExtendWith (MockitoExtension.class)
class UserServiceTest {

    @Mock 
    private UserRepository userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private CurrentUserService currentUserService;

    @Mock
    private TaskService taskService;

    @Mock
    private NoteService noteService;

    @Mock
    private EventService eventService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private ReminderService reminderService;

    @Mock
    private UserMapper mapper;

    @InjectMocks 
    private UserService userService;

    @Test 
    void registerUserCreatesAndSavesUserWhenRequestIsValid() {

        RegisterRequest request = mock(RegisterRequest.class);

        when(request.email()).thenReturn("email@example.com");
        when(request.username()).thenReturn("Username");
        when(request.password()).thenReturn("password123");
        when(userRepo.existsByEmail("email@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");

        UserEntity savedUser = new UserEntity(
            "email@example.com",
            "Username",
            "encodedPassword"
        );

        when(userRepo.save(any(UserEntity.class))).thenReturn(savedUser);

        UserEntity result = userService.registerUser(request);

        assertEquals(savedUser, result);
        verify(userRepo).existsByEmail("email@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepo).save(any(UserEntity.class));
    }


    @Test
    void registerUserThrowsExceptionWhenEmailAlreadyExists() {

        RegisterRequest request = mock(RegisterRequest.class);

        when(request.email()).thenReturn("email@example.com");

        when(userRepo.existsByEmail("email@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> userService.registerUser(request));

        verify(userRepo).existsByEmail("email@example.com");

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepo, never()).save(any(UserEntity.class));
    }


    // ---------------------------------------------------------
    // getUserById
    // ---------------------------------------------------------

    @Test
    void getUserByIdReturnsUserWhenUserExists() {

        Long idUser = 1L;
        UserEntity user = new UserEntity();
        user.setIdUser(idUser);

        when(userRepo.findById(idUser)).thenReturn(Optional.of(user));

        UserEntity result = userService.getUserById(idUser);

        assertEquals(user, result);
        verify(userRepo).findById(idUser);
    }

    @Test
    void getUserByIdThrowsExceptionWhenUserDoesNotExist() {
        Long idUser = 1L;

        when(userRepo.findById(idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserById(idUser));
        verify(userRepo).findById(idUser);
    }

    @Test
    void getUserResponseReturnsMappedCurrentUser() {

        Long idUser = 1L;

        UserEntity user = new UserEntity();
        user.setIdUser(idUser);

        UserResponse response = mock(UserResponse.class);

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(userRepo.findById(idUser)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.getUserResponse();

        assertEquals(response, result);
        verify(mapper).toResponse(user);
    }

    @Test
    void deleteAccountDeletesAllUserDataAndUser() {

        Long idUser = 1L;
        UserEntity user = new UserEntity();
        user.setIdUser(idUser);

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(userRepo.findById(idUser)).thenReturn(Optional.of(user));

        userService.deleteAccount();

        verify(reminderService).deleteRemindersByUser(user);
        verify(taskService).deleteTasksByUser(user);
        verify(eventService).deleteEventsByUser(user);
        verify(noteService).deleteNotesByUser(user);
        verify(categoryService).deleteCategoriesByUser(user);
        verify(userRepo).delete(user);
    }


    @Test
    void deleteAccountDoesNotDeleteAnythingWhenUserDoesNotExist() {

        Long idUser = 1L;

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(userRepo.findById(idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.deleteAccount());

        verify(reminderService, never()).deleteRemindersByUser(any());
        verify(taskService, never()).deleteTasksByUser(any());
        verify(eventService, never()).deleteEventsByUser(any());
        verify(noteService, never()).deleteNotesByUser(any());
        verify(categoryService, never()).deleteCategoriesByUser(any());

        verify(userRepo, never()).delete(any());
    }

    @Test
    void updateUsernameUpdatesUserAndReturnsResponse() {

        Long idUser = 1L;
        UserEntity user = new UserEntity();
        user.setIdUser(idUser);
        user.setUsername("OldName");

        UserResponse response = mock(UserResponse.class);

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(userRepo.findById(idUser)).thenReturn(Optional.of(user));
        when(userRepo.save(user)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(response);

        UserResponse result = userService.updateUsername("NewName");

        assertEquals(response, result);
        assertEquals("NewName", user.getUsername());
        verify(userRepo).save(user);
        verify(mapper).toResponse(user);
    }

    @Test
    void validateRegistrationRequestThrowsExceptionWhenEmailIsNull() {

        RegisterRequest request = mock(RegisterRequest.class);

        when(request.email()).thenReturn(null);
        when(request.password()).thenReturn("password123");

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.validateRegistartionRequest(request));

        assertEquals("Email is required.", exception.getMessage());
    }


    @Test
    void validateRegistrationRequestThrowsExceptionWhenPasswordIsNull() {

        RegisterRequest request = mock(RegisterRequest.class);

        when(request.email()).thenReturn("email@example.com");
        when(request.password()).thenReturn(null);

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.validateRegistartionRequest(request));

        assertEquals("Password is required.", exception.getMessage());
    }


    @Test
    void validateRegistrationRequestThrowsExceptionWhenPasswordIsTooShort() {

        RegisterRequest request = mock(RegisterRequest.class);

        when(request.email()).thenReturn("email@example.com");
        when(request.password()).thenReturn("1234567");

        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> userService.validateRegistartionRequest(request));

        assertEquals("Password must be at least 8 characters.", exception.getMessage());
    }


    @Test
    void validateRegistrationRequestAllowsValidRequest() {

        RegisterRequest request = mock(RegisterRequest.class);

        when(request.email()).thenReturn("email@example.com");
        when(request.password()).thenReturn("password123");

        assertDoesNotThrow(() -> userService.validateRegistartionRequest(request));
    }

    @Test
    void getCurrentUserReturnsCurrentUser() {

        Long idUser = 1L;
        UserEntity user = new UserEntity();
        user.setIdUser(idUser);

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(userRepo.findById(idUser)).thenReturn(Optional.of(user));

        UserEntity result = userService.getCurrentUser();

        assertEquals(user, result);
        verify(currentUserService).getCurrentUserId();
        verify(userRepo).findById(idUser);
    }


    @Test
    void getCurrentUserThrowsExceptionWhenUserDoesNotExist() {

        Long idUser = 1L;

        when(currentUserService.getCurrentUserId()).thenReturn(idUser);
        when(userRepo.findById(idUser)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getCurrentUser());
        verify(userRepo).findById(idUser);
    }
}
