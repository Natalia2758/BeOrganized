package com.example.note.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.category.model.Category;
import com.example.category.service.CategoryService;
import com.example.common.throwables.exception.InvalidRequestException;
import com.example.common.throwables.exception.NotFoundException;
import com.example.note.dto.NoteCreateRequest;
import com.example.note.dto.QuickNoteCreateRequest;
import com.example.note.dto.NoteFilter;
import com.example.note.dto.NoteResponse;
import com.example.note.mapper.NoteMapper;
import com.example.note.model.Note;
import com.example.note.repository.NoteRepository;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;

/**
 * <b>NoteService</b>
 * <br>
 * Service class that handles all business logic and operations related to notes, inlcuding creating, deleting, retrieving, and helper functions
 */
@Service
public class NoteService {

    private final NoteRepository noteRepo;
    private final NoteMapper mapper;
    private final CurrentUserService currentUserService;
    private final UserRepository userRepo;
    private final CategoryService categoryService;

    public NoteService(
        NoteRepository noteRepo,
        NoteMapper mapper,
        CurrentUserService currentUserService,
        UserRepository userRepo,
        CategoryService categoryService
    ) {
        this.noteRepo = noteRepo;
        this.mapper = mapper;
        this.currentUserService = currentUserService;
        this.userRepo = userRepo;
        this.categoryService = categoryService;
    }

    /**
     * Retrieves all notes created by the user
     * @return a list of note response DTO
     */
    @Transactional(readOnly = true)
    public List<NoteResponse> getAllNotes(NoteFilter filter) {
        UserEntity user = getCurrentUser();

        List<Note> notes = noteRepo.filterNotes(
            user,
            filter.idCategory()
        );
        return mapper.toResponseList(notes);
    }

    /**
     * Retrieves a note by provided ID
     * @param idEvent   an ID of the note
     * @return          a note response DTO of the fetched note
     */
    @Transactional(readOnly = true)
    public NoteResponse getNoteById(Long idNote) {
        Note note = validateNoteBelongsToUser(idNote);
        return mapper.toResponse(note);
    }

    /**
     * Validates data provided by the user, creates a new note from the data, and saves note to the database
     * @param request  a request DTO containing note data
     * @return          a response DTO with newly created note's data
     */
    @Transactional
    public NoteResponse createNote(NoteCreateRequest request) {
        UserEntity user = getCurrentUser();

        validateCreationRequest(request);

        Note note = new Note();
        note.setTitle(request.title());
        note.setContent(request.content());
        note.setDate(request.date());
        note.setCategory(validateCategory(request));
        note.setUser(user);

        Note created = noteRepo.save(note);
        return mapper.toResponse(created);
    }

    /**
     * Validates data provided by the user, creates a new note from the quick note creation request, and saves note to the database
     * @param request  a request DTO containing note data
     * @return          a response DTO with newly created note's data
     */
    public NoteResponse createNoteQuick(QuickNoteCreateRequest request) {
        UserEntity user = getCurrentUser();

        validateQuickCreationRequest(request);

        Note note = new Note();
        note.setTitle(request.title());
        note.setContent(request.content());
        note.setUser(user);

        Note created = noteRepo.save(note);
        return mapper.toResponse(created);
    }

    /**
     * Validates the note with provided ID belongs to the user, updates note, and returns updated note response DTO
     * @param idCategory    an ID of the note to be updated
     * @param request       a request DTO with the data that the note should be updated with
     * @return              a response DTO with updated note data
     */
    @Transactional
    public NoteResponse updateNote(Long idNote, NoteCreateRequest request) {
        Note note = validateNoteBelongsToUser(idNote);
        
        validateCreationRequest(request);

        note.setTitle(request.title());
        note.setContent(request.content());
        note.setDate(request.date());
        note.setCategory(validateCategory(request));

        return mapper.toResponse(noteRepo.save(note));
    }

    /**
     * Validates the note with provided ID belongs to the user and deletes the note
     * @param idCategory an ID of the note to be deleted
     */
    public void deleteNote(Long idNote) {
        Note note = validateNoteBelongsToUser(idNote);
        noteRepo.delete(note);
    }

    /**
     * Ensures the provided note creation request contains correct data
     * @param request a request DTO with note data provided by the user
     */
    public void validateCreationRequest(NoteCreateRequest request) {
        if (request == null){
            throw new InvalidRequestException("NO_NOTE_DATA", "Note data is required.");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new InvalidRequestException("NOTE_CONTENT_REQUIRED", "Note content is required.");
        }
        if (request.date() != null && (request.date().isBefore(LocalDate.now()))) {
            throw new InvalidRequestException("NOTE_DATE_IN_PAST", "Date must be in the future.");
        }
    }

    /**
     * Ensures the provided quick note creation request contains correct data
     * @param request a request DTO with note data provided by the user
     */
    public void validateQuickCreationRequest(QuickNoteCreateRequest request) {
        if (request == null){
            throw new InvalidRequestException("NO_NOTE_DATA", "Note data is required.");
        }
        if (request.content() == null || request.content().isBlank()) {
            throw new InvalidRequestException("NOTE_CONTENT_REQUIRED", "Note content is required.");
        }
    }

    /**
     * Validates that selected category exists and belongs to the current user
     * @param request   an event creation request DTO
     * @return          a Category object if it exsists and belongs to the current user
     */
    public Category validateCategory(NoteCreateRequest request) {
        if (request.idCategory() != null && categoryService.categoryExistsById(request.idCategory())) {
            Category category;
            category = categoryService.validateCatBelongsToUser(request.idCategory());
            return category;
        } else {
            return null;
        }
    }

    /**
     * Retrieves notes filtered by the provided date
     * @param year  a year as an integer
     * @param month a month as an integer
     * @param day   a day of month as an integer
     * @return  a list of response DTOs representing filtered notes
     */
    public List<NoteResponse> getNotesByDate(int year, int month, int day) {
        List<Note> notes = noteRepo.findByDateAndUser(LocalDate.of(year, month, day), getCurrentUser());
        return mapper.toResponseList(notes);
    }

    /**
     * Validates the note belongs to a specific user
     * @param idCategory    an ID of a note to be checked
     * @return              an object of the verified note
     */
    public Note validateNoteBelongsToUser(Long idNote) {
        Long idUser = currentUserService.getCurrentUserId();

        return noteRepo.findByIdAndIdUser(idNote, idUser)
            .orElseThrow(() -> new NotFoundException("Note", idNote));
    }

    /**
     * Deletes all notes that belong to the user
     * @param user an object with the user data
     */
    public void deleteNotesByUser(UserEntity user) {
        noteRepo.deleteByUser(user);
    }

    /**
     * Retrieves a current user object
     * @return  an object with the current user's data
     */
    private UserEntity getCurrentUser() {
        Long idUser = currentUserService.getCurrentUserId();
        return userRepo.findById(idUser)
            .orElseThrow(() -> new NotFoundException("User", idUser));
    }
}
