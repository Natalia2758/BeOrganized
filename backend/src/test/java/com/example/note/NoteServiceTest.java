package com.example.note;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.category.model.Category;
import com.example.category.service.CategoryService;
import com.example.common.throwables.exception.InvalidRequestException;
import com.example.common.throwables.exception.NotFoundException;
import com.example.note.dto.NoteCreateRequest;
import com.example.note.dto.NoteFilter;
import com.example.note.dto.NoteResponse;
import com.example.note.dto.QuickNoteCreateRequest;
import com.example.note.mapper.NoteMapper;
import com.example.note.model.Note;
import com.example.note.repository.NoteRepository;
import com.example.note.service.NoteService;
import com.example.user.model.UserEntity;
import com.example.user.repository.UserRepository;
import com.example.user.service.CurrentUserService;

@ExtendWith(MockitoExtension.class)
public class NoteServiceTest {

	@Mock
	private NoteRepository noteRepo;

	@Mock
	private NoteMapper mapper;

	@Mock
	private CurrentUserService currentUserService;

	@Mock
	private UserRepository userRepo;

	@Mock
	private CategoryService categoryService;

	@InjectMocks
	private NoteService noteService;

	@Test
	void getAllNotesReturnsAllUserNotes() {
		Long idUser = 1L;
		UserEntity user = authenticatedUser(idUser);

		Note note1 = new Note();
		Note note2 = new Note();
		List<Note> notes = List.of(note1, note2);

		List<NoteResponse> expectedResponses = List.of(createResponse(), createResponse());
		NoteFilter filter = new NoteFilter(2L);

		when(noteRepo.filterNotes(user, filter.idCategory())).thenReturn(notes);
		when(mapper.toResponseList(notes)).thenReturn(expectedResponses);

		List<NoteResponse> result = noteService.getAllNotes(filter);

		assertEquals(expectedResponses, result);
		verify(currentUserService).getCurrentUserId();
		verify(userRepo).findById(idUser);
		verify(noteRepo).filterNotes(user, filter.idCategory());
		verify(mapper).toResponseList(notes);
	}

	@Test
	void getNoteByIdReturnsNoteWhenExists() {
		Long idNote = 1L;
		Long idUser = 10L;

		authenticatedUser(idUser);

		Note note = new Note();
		NoteResponse expectedResponse = mock(NoteResponse.class);

		when(noteRepo.findByIdAndIdUser(idNote, idUser)).thenReturn(Optional.of(note));
		when(mapper.toResponse(note)).thenReturn(expectedResponse);

		NoteResponse result = noteService.getNoteById(idNote);

		assertEquals(expectedResponse, result);
		verify(noteRepo).findByIdAndIdUser(idNote, idUser);
		verify(mapper).toResponse(note);
	}

	@Test
	void getNoteByIdThrowsExceptionWhenNoteDoesNotExist() {
		Long idNote = 1L;
		Long idUser = 10L;

		authenticatedUser(idUser);

		when(noteRepo.findByIdAndIdUser(idNote, idUser)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> noteService.getNoteById(idNote));
		verify(noteRepo).findByIdAndIdUser(idNote, idUser);
		verify(mapper, never()).toResponse(any(Note.class));
	}

	@Test
	void createNoteSavesRequestValuesAndReturnsResponse() {
		Long idUser = 1L;
		Long idCategory = 2L;

		UserEntity user = authenticatedUser(idUser);
		Category category = new Category();
		LocalDate date = LocalDate.now().plusDays(1);

		NoteCreateRequest request = new NoteCreateRequest(
			"Note title",
			"Note content",
			date,
			idCategory
		);

		NoteResponse expectedResponse = createResponse();

		when(categoryService.categoryExistsById(idCategory)).thenReturn(true);
		when(categoryService.validateCatBelongsToUser(idCategory)).thenReturn(category);
		when(noteRepo.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(mapper.toResponse(any(Note.class))).thenReturn(expectedResponse);

		NoteResponse result = noteService.createNote(request);

		assertEquals(expectedResponse, result);

		ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
		verify(noteRepo).save(noteCaptor.capture());
		Note savedNote = noteCaptor.getValue();

		assertEquals(request.title(), savedNote.getTitle());
		assertEquals(request.content(), savedNote.getContent());
		assertEquals(request.date(), savedNote.getDate());
		assertEquals(category, savedNote.getCategory());
		assertEquals(user, savedNote.getUser());

		verify(categoryService).categoryExistsById(idCategory);
		verify(categoryService).validateCatBelongsToUser(idCategory);
		verify(mapper).toResponse(savedNote);
	}

	@Test
	void createNoteDoesNotSaveWhenRequestIsInvalid() {
		authenticatedUser(1L);

		NoteCreateRequest request = new NoteCreateRequest(
			"",
			null,
			LocalDate.now().plusDays(1),
			null
		);

		assertThrows(InvalidRequestException.class, () -> noteService.createNote(request));
		verify(noteRepo, never()).save(any(Note.class));
		verify(mapper, never()).toResponse(any(Note.class));
	}

	@Test
	void createNoteQuickReturnsResponseWhenSuccessfullyCreated() {
		Long idUser = 1L;
		UserEntity user = authenticatedUser(idUser);

		QuickNoteCreateRequest request = new QuickNoteCreateRequest("Quick note", "Quick content");
		NoteResponse expectedResponse = createResponse();

		when(noteRepo.save(any(Note.class))).thenAnswer(invocation -> invocation.getArgument(0));
		when(mapper.toResponse(any(Note.class))).thenReturn(expectedResponse);

		NoteResponse result = noteService.createNoteQuick(request);

		assertEquals(expectedResponse, result);

		ArgumentCaptor<Note> noteCaptor = ArgumentCaptor.forClass(Note.class);
		verify(noteRepo).save(noteCaptor.capture());
		Note savedNote = noteCaptor.getValue();

		assertEquals(request.title(), savedNote.getTitle());
		assertEquals(request.content(), savedNote.getContent());
		assertEquals(user, savedNote.getUser());

		verify(mapper).toResponse(savedNote);
	}

	@Test
	void validateCategoryReturnsNullWhenCategoryIsNotProvided() {
		NoteCreateRequest request = createRequest(null);

		assertNull(noteService.validateCategory(request));
		verify(categoryService, never()).categoryExistsById(anyLong());
		verify(categoryService, never()).validateCatBelongsToUser(anyLong());
	}

	@Test
	void validateCategoryReturnsNullWhenCategoryDoesNotExist() {
		Long idCategory = 2L;

		NoteCreateRequest request = createRequest(idCategory);

		when(categoryService.categoryExistsById(idCategory)).thenReturn(false);

		assertNull(noteService.validateCategory(request));
		verify(categoryService).categoryExistsById(idCategory);
		verify(categoryService, never()).validateCatBelongsToUser(anyLong());
	}

	@Test
	void validateCategoryReturnsCategoryWhenItExistsForUser() {
		Long idCategory = 2L;

		Category category = new Category();
		NoteCreateRequest request = createRequest(idCategory);

		when(categoryService.categoryExistsById(idCategory)).thenReturn(true);
		when(categoryService.validateCatBelongsToUser(idCategory)).thenReturn(category);

		Category result = noteService.validateCategory(request);

		assertEquals(category, result);
		verify(categoryService).categoryExistsById(idCategory);
		verify(categoryService).validateCatBelongsToUser(idCategory);
	}

	@Test
	void updateNoteUpdatesValuesAndReturnsResponse() {
		Long idNote = 1L;
		Long idUser = 10L;

		authenticatedUser(idUser);

		Note note = new Note();
		NoteCreateRequest request = createRequest(2L);
		NoteResponse expectedResponse = createResponse();
		Category category = new Category();

		when(noteRepo.findByIdAndIdUser(idNote, idUser)).thenReturn(Optional.of(note));
		when(categoryService.categoryExistsById(2L)).thenReturn(true);
		when(categoryService.validateCatBelongsToUser(2L)).thenReturn(category);
		when(noteRepo.save(note)).thenReturn(note);
		when(mapper.toResponse(note)).thenReturn(expectedResponse);

		NoteResponse result = noteService.updateNote(idNote, request);

		assertEquals(expectedResponse, result);
		assertEquals(request.title(), note.getTitle());
		assertEquals(request.content(), note.getContent());
		assertEquals(request.date(), note.getDate());
		assertEquals(category, note.getCategory());

		verify(noteRepo).save(note);
		verify(mapper).toResponse(note);
	}

	@Test
	void updateNoteThrowsExceptionWhenNoteDoesNotExist() {
		Long idNote = 1L;
		Long idUser = 10L;

		authenticatedUser(idUser);

		when(noteRepo.findByIdAndIdUser(idNote, idUser)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> noteService.updateNote(idNote, createRequest(null)));

		verify(noteRepo).findByIdAndIdUser(idNote, idUser);
		verify(noteRepo, never()).save(any(Note.class));
		verify(mapper, never()).toResponse(any(Note.class));
	}

	@Test
	void deleteNoteDeletesNoteWhenItExists() {
		Long idNote = 1L;
		Long idUser = 10L;

		authenticatedUser(idUser);

		Note note = new Note();

		when(noteRepo.findByIdAndIdUser(idNote, idUser)).thenReturn(Optional.of(note));

		noteService.deleteNote(idNote);

		verify(noteRepo).findByIdAndIdUser(idNote, idUser);
		verify(noteRepo).delete(note);
	}

	@Test
	void deleteNoteThrowsExceptionWhenNoteDoesNotExist() {
		Long idNote = 1L;
		Long idUser = 10L;

		authenticatedUser(idUser);

		when(noteRepo.findByIdAndIdUser(idNote, idUser)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> noteService.deleteNote(idNote));

		verify(noteRepo).findByIdAndIdUser(idNote, idUser);
		verify(noteRepo, never()).delete(any(Note.class));
	}

	@Test
	void validateNoteBelongsToUserReturnsNoteWhenItExists() {
		Long idNote = 1L;
		Long idUser = 2L;

		Note note = new Note();

		when(currentUserService.getCurrentUserId()).thenReturn(idUser);
		when(noteRepo.findByIdAndIdUser(idNote, idUser)).thenReturn(Optional.of(note));

		Note result = noteService.validateNoteBelongsToUser(idNote);

		assertEquals(note, result);
		verify(currentUserService).getCurrentUserId();
		verify(noteRepo).findByIdAndIdUser(idNote, idUser);
	}

	@Test
	void validateNoteBelongsToUserThrowsExceptionWhenNoteDoesNotExist() {
		Long idNote = 1L;
		Long idUser = 2L;

		when(currentUserService.getCurrentUserId()).thenReturn(idUser);
		when(noteRepo.findByIdAndIdUser(idNote, idUser)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> noteService.validateNoteBelongsToUser(idNote));
		verify(noteRepo).findByIdAndIdUser(idNote, idUser);
	}

	@Test
	void validateCreationRequestThrowsExceptionWhenRequestIsNull() {
		InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> noteService.validateCreationRequest(null));

		assertEquals("Note data is required.", exception.getMessage());
	}

	@Test
	void validateCreationRequestThrowsExceptionWhenContentIsBlank() {
		NoteCreateRequest request = new NoteCreateRequest(
			"Title",
			"",
			LocalDate.now().plusDays(1),
			null
		);

		InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> noteService.validateCreationRequest(request));

		assertEquals("Note content is required.", exception.getMessage());
	}

	@Test
	void validateCreationRequestThrowsExceptionWhenDateIsInPast() {
		NoteCreateRequest request = new NoteCreateRequest(
			"Title",
			"Content",
			LocalDate.now().minusDays(1),
			null
		);

		InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> noteService.validateCreationRequest(request));

		assertEquals("Date must be in the future.", exception.getMessage());
	}

	@Test
	void validateCreationRequestAllowsValidRequest() {
		NoteCreateRequest request = new NoteCreateRequest(
			"Title",
			"Content",
			LocalDate.now().plusDays(1),
			null
		);

		assertDoesNotThrow(() -> noteService.validateCreationRequest(request));
	}

	@Test
	void validateQuickCreationRequestThrowsExceptionWhenRequestIsNull() {
		InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> noteService.validateQuickCreationRequest(null));

		assertEquals("Note data is required.", exception.getMessage());
	}

	@Test
	void validateQuickCreationRequestThrowsExceptionWhenContentIsBlank() {
		QuickNoteCreateRequest request = new QuickNoteCreateRequest("Title", "");

		InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> noteService.validateQuickCreationRequest(request));

		assertEquals("Note content is required.", exception.getMessage());
	}

	@Test
	void validateQuickCreationRequestAllowsValidRequest() {
		QuickNoteCreateRequest request = new QuickNoteCreateRequest("Title", "Content");

		assertDoesNotThrow(() -> noteService.validateQuickCreationRequest(request));
	}

	@Test
	void getNotesByDateReturnsMappedNotes() {
		Long idUser = 1L;
		UserEntity user = authenticatedUser(idUser);
		LocalDate date = LocalDate.of(2026, 10, 1);

		List<Note> notes = List.of(new Note(), new Note());
		List<NoteResponse> expectedResponses = List.of(createResponse(), createResponse());

		when(noteRepo.findByDateAndUser(date, user)).thenReturn(notes);
		when(mapper.toResponseList(notes)).thenReturn(expectedResponses);

		List<NoteResponse> result = noteService.getNotesByDate(date.getYear(), date.getMonthValue(), date.getDayOfMonth());

		assertEquals(expectedResponses, result);
		verify(noteRepo).findByDateAndUser(date, user);
		verify(mapper).toResponseList(notes);
	}

	@Test
	void deleteNotesByUserDeletesAllUserNotes() {
		UserEntity user = new UserEntity();

		noteService.deleteNotesByUser(user);

		verify(noteRepo).deleteByUser(user);
	}

	private UserEntity authenticatedUser(Long idUser) {
		UserEntity user = new UserEntity();
		user.setIdUser(idUser);

		lenient().when(currentUserService.getCurrentUserId()).thenReturn(idUser);
		lenient().when(userRepo.findById(idUser)).thenReturn(Optional.of(user));

		return user;
	}

	private NoteCreateRequest createRequest(Long idCategory) {
		LocalDate date = LocalDate.now().plusDays(1);

		return new NoteCreateRequest(
			"Title",
			"Content",
			date,
			idCategory
		);
	}

	private NoteResponse createResponse() {
		return new NoteResponse(
			1L,
			"Title",
			"Content",
			LocalDate.now().plusDays(1),
			null
		);
	}
}
