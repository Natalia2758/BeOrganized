package com.example.note.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.example.note.dto.NoteCreateRequest;
import com.example.note.dto.QuickNoteCreateRequest;
import com.example.note.dto.NoteFilter;
import com.example.note.dto.NoteResponse;
import com.example.note.service.NoteService;

import jakarta.validation.Valid;


/**
 * 
 * <b>NoteController</b>
 * <br>
 * REST controller responsible for communication and note data exchange with the frontend side of application
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    /**
     * Receives filtering criteria and responds with filtered user notes
     * @param filter    a DTO representing criteria to filter notes
     * @return          a list of filtered notes mapped to response DTOs
     */
    @GetMapping
    public List<NoteResponse> getNotes(@ModelAttribute NoteFilter filter) {
        return noteService.getAllNotes(filter);
    }

    /**
     * Sends response with an note data by the provided ID
     * @param idNote   an ID of the requested note
     * @return          a response DTO with an note data
     */
    @GetMapping("/{idNote}")
    public NoteResponse getNote(@PathVariable("idNote") Long idNote) {
        return noteService.getNoteById(idNote);
    }
    

    /**
     * Receives a request DTO with data of a note to be created
     * @param request   a request DTO with note data provided by the user
     * @return          a response DTO with a created note
     */
    @PostMapping("/create")
    public NoteResponse createNote(@Valid @RequestBody NoteCreateRequest request) {
        return noteService.createNote(request);
    }

    /**
     * Receives a shortened version of a request DTO with data of a note to be created
     * @param request   a request DTO with note data provided by the user
     * @return          a response DTO with a created note
     */
    @PostMapping("/create-quick")
    @ResponseStatus(HttpStatus.CREATED)
    public NoteResponse createNoteQuick(@Valid @RequestBody QuickNoteCreateRequest request) {
        return noteService.createNoteQuick(request);
    }
    
    /**
     * Receives a request DTO with modified data of an existing note
     * @param idNote   an ID of the note to be modified
     * @param request   a request DTO with the data to be changed
     * @return          a response DTO with modified note data
     */
    @PutMapping("/{idNote}")
    public NoteResponse updateNote(@PathVariable("idNote") Long idNote, @RequestBody NoteCreateRequest request) {
        return noteService.updateNote(idNote, request);
    }

    /**
     * Receives a request to delete a note
     * @param idNote an ID of the note to be deleted
     */
    @DeleteMapping("/{idNote}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNote(@PathVariable("idNote") Long idNote) {
        noteService.deleteNote(idNote);
    }  
    
    /**
     * Sends all notes filtered according to the provided date
     * @param year  a year as an integer
     * @param month a month as an integer
     * @param day   a day of month as an integer
     * @return      a list of response DTOs representing filtered notes
     */
    @GetMapping("/calendar")
    public List<NoteResponse> getNotesByDate(@RequestParam("year") int year, @RequestParam("month") int month, @RequestParam("day") int day) {
        return noteService.getNotesByDate(year, month, day);
    }

}
