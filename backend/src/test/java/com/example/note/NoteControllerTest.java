package com.example.note;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.example.note.controller.NoteController;
import com.example.note.dto.NoteCreateRequest;
import com.example.note.dto.NoteFilter;
import com.example.note.dto.NoteResponse;
import com.example.note.dto.QuickNoteCreateRequest;
import com.example.note.service.NoteService;

@WebMvcTest(NoteController.class)
public class NoteControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private NoteService noteService;

	@Test
	void getNotesReturnsNoteList() throws Exception {

		NoteResponse note = mock(NoteResponse.class);

		when(noteService.getAllNotes(any(NoteFilter.class))).thenReturn(List.of(note));

		mockMvc.perform(get("/api/notes"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(1));

		verify(noteService).getAllNotes(any(NoteFilter.class));
	}

	@Test
	void getNotesReturnsNoteListWhenFiltersProvided() throws Exception {

		when(noteService.getAllNotes(any(NoteFilter.class))).thenReturn(List.of());

		mockMvc.perform(get("/api/notes")
				.param("idCategory", "2"))
			.andExpect(status().isOk());

		ArgumentCaptor<NoteFilter> captor = ArgumentCaptor.forClass(NoteFilter.class);

		verify(noteService).getAllNotes(captor.capture());

		NoteFilter filter = captor.getValue();

		assertEquals(2L, filter.idCategory());
	}

	@Test
	void getNotesReturnsBadRequestWhenFilterIsInvalid() throws Exception {

		mockMvc.perform(get("/api/notes")
				.param("idCategory", "abc"))
			.andExpect(status().isBadRequest());

		verify(noteService, never()).getAllNotes(any(NoteFilter.class));
	}

	@Test
	void getNoteReturnsNote() throws Exception {

		Long idNote = 1L;
		NoteResponse note = mock(NoteResponse.class);

		when(noteService.getNoteById(idNote)).thenReturn(note);

		mockMvc.perform(get("/api/notes/{idNote}", idNote))
			.andExpect(status().isOk());

		verify(noteService).getNoteById(idNote);
	}

	@Test
	void getNoteReturnsBadRequestWhenIdIsInvalid() throws Exception {

		mockMvc.perform(get("/api/notes/{idNote}", "abc"))
			.andExpect(status().isBadRequest());

		verify(noteService, never()).getNoteById(anyLong());
	}

	@Test
	void createNoteReturnsOkStatusWhenValidRequest() throws Exception {

		NoteResponse response = mock(NoteResponse.class);

		when(noteService.createNote(any(NoteCreateRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/notes/create")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"title": "Team note",
						"content": "Discuss project scope",
						"date": "2026-12-10"
					}
				"""))
			.andExpect(status().isOk());

		verify(noteService).createNote(any(NoteCreateRequest.class));
	}

	@Test
	void createNotePassesCorrectRequestToService() throws Exception {

		when(noteService.createNote(any(NoteCreateRequest.class))).thenReturn(mock(NoteResponse.class));

		mockMvc.perform(post("/api/notes/create")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"title": "Team note",
						"content": "Discuss project scope",
						"date": "2026-12-10",
						"idCategory": 2
					}
				"""))
			.andExpect(status().isOk());

		ArgumentCaptor<NoteCreateRequest> captor = ArgumentCaptor.forClass(NoteCreateRequest.class);

		verify(noteService).createNote(captor.capture());

		NoteCreateRequest request = captor.getValue();

		assertEquals("Team note", request.title());
		assertEquals("Discuss project scope", request.content());
		assertEquals("2026-12-10", request.date().toString());
		assertEquals(2L, request.idCategory());
	}

	@Test
	void createNoteReturnsBadRequestWhenRequestIsInvalid() throws Exception {

		mockMvc.perform(post("/api/notes/create")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"title": "",
						"content": "",
						"date": "2026-12-10"
					}
				"""))
			.andExpect(status().isBadRequest());

		verify(noteService, never()).createNote(any(NoteCreateRequest.class));
	}

	@Test
	void createNoteQuickReturnsCreatedStatusWhenValidRequest() throws Exception {

		NoteResponse response = mock(NoteResponse.class);

		when(noteService.createNoteQuick(any(QuickNoteCreateRequest.class))).thenReturn(response);

		mockMvc.perform(post("/api/notes/create-quick")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"title": "Quick note",
						"content": "Quick content"
					}
				"""))
			.andExpect(status().isCreated());

		verify(noteService).createNoteQuick(any(QuickNoteCreateRequest.class));
	}

	@Test
	void createNoteQuickReturnsBadRequestWhenRequestIsInvalid() throws Exception {

		mockMvc.perform(post("/api/notes/create-quick")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"title": ""
					}
				"""))
			.andExpect(status().isBadRequest());

		verify(noteService, never()).createNoteQuick(any(QuickNoteCreateRequest.class));
	}

	@Test
	void updateNoteReturnsNoteWhenValidRequest() throws Exception {

		Long idNote = 1L;
		NoteResponse response = mock(NoteResponse.class);

		when(noteService.updateNote(eq(idNote), any(NoteCreateRequest.class))).thenReturn(response);

		mockMvc.perform(put("/api/notes/{idNote}", idNote)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"title": "Updated note",
						"content": "Updated content",
						"date": "2026-12-10"
					}
				"""))
			.andExpect(status().isOk());

		verify(noteService).updateNote(eq(idNote), any(NoteCreateRequest.class));
	}

	@Test
	void updateNotePassesCorrectRequestToService() throws Exception {

		Long idNote = 1L;

		when(noteService.updateNote(eq(idNote), any(NoteCreateRequest.class))).thenReturn(mock(NoteResponse.class));

		mockMvc.perform(put("/api/notes/{idNote}", idNote)
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"title": "Updated note",
						"content": "Updated content",
						"date": "2026-12-10",
						"idCategory": 3
					}
				"""))
			.andExpect(status().isOk());

		ArgumentCaptor<NoteCreateRequest> captor = ArgumentCaptor.forClass(NoteCreateRequest.class);

		verify(noteService).updateNote(eq(idNote), captor.capture());

		NoteCreateRequest request = captor.getValue();

		assertEquals("Updated note", request.title());
		assertEquals("Updated content", request.content());
		assertEquals("2026-12-10", request.date().toString());
		assertEquals(3L, request.idCategory());
	}

	@Test
	void updateNoteReturnsBadRequestWhenIdIsInvalid() throws Exception {

		mockMvc.perform(put("/api/notes/{idNote}", "abc")
				.contentType(MediaType.APPLICATION_JSON)
				.content("""
					{
						"title": "Updated note",
						"content": "Updated content",
						"date": "2026-12-10"
					}
				"""))
			.andExpect(status().isBadRequest());

		verify(noteService, never()).updateNote(anyLong(), any(NoteCreateRequest.class));
	}

	@Test
	void deleteNoteReturnsNoContentWhenSuccessfulRequest() throws Exception {

		Long idNote = 1L;

		mockMvc.perform(delete("/api/notes/{idNote}", idNote))
			.andExpect(status().isNoContent());

		verify(noteService).deleteNote(idNote);
	}

	@Test
	void deleteNoteReturnsBadRequestWhenIdIsInvalid() throws Exception {

		mockMvc.perform(delete("/api/notes/{idNote}", "abc"))
			.andExpect(status().isBadRequest());

		verify(noteService, never()).deleteNote(anyLong());
	}

	@Test
	void getNotesByDateReturnsNoteListWhenSuccessfulRequest() throws Exception {

		NoteResponse note = mock(NoteResponse.class);

		when(noteService.getNotesByDate(anyInt(), anyInt(), anyInt())).thenReturn(List.of(note));

		mockMvc.perform(get("/api/notes/calendar")
				.param("year", "2026")
				.param("month", "10")
				.param("day", "20"))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$").isArray())
			.andExpect(jsonPath("$.length()").value(1));

		verify(noteService).getNotesByDate(2026, 10, 20);
	}

	@Test
	void getNotesByDateReturnsBadRequestWhenMissingParam() throws Exception {

		mockMvc.perform(get("/api/notes/calendar")
				.param("year", "2026")
				.param("month", "10"))
			.andExpect(status().isBadRequest());

		verify(noteService, never()).getNotesByDate(anyInt(), anyInt(), anyInt());
	}

	@Test
	void getNotesByDateReturnsBadRequestWhenParamIsInvalid() throws Exception {

		mockMvc.perform(get("/api/notes/calendar")
				.param("year", "abc")
				.param("month", "10")
				.param("day", "20"))
			.andExpect(status().isBadRequest());

		verify(noteService, never()).getNotesByDate(anyInt(), anyInt(), anyInt());
	}
}
