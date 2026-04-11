package com.example.resource;

/*
 * Database-backed CRUD API for love notes with payload validation and safe errors.
 */

import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

@Path("/lovenotes")
public class LoveNoteResource {

    private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .appendPattern("MMM d, uuuu")
            .toFormatter(Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT);

    // Builds a standardized 400 response with a user-friendly message.
    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", message))
                .build();
    }

    // Performs field-level validation for love note payloads.
    private Map<String, String> validateNote(LoveNote note) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (note == null) {
            errors.put("note", "Love note details are required.");
            return errors;
        }

        if (note.message == null || note.message.trim().isEmpty()) {
            errors.put("message", "Message is required.");
        } else if (note.message.trim().length() > 400) {
            errors.put("message", "Message must be 400 characters or fewer.");
        }

        if (note.author == null || note.author.trim().isEmpty()) {
            errors.put("author", "Author is required.");
        } else if (note.author.trim().length() > 40) {
            errors.put("author", "Author must be 40 characters or fewer.");
        }

        if (note.date == null || note.date.trim().isEmpty()) {
            errors.put("date", "Date is required.");
        } else if (!isValidDateFormat(note.date.trim())) {
            errors.put("date", "Date must use format Mon D, YYYY (example: Apr 8, 2024).");
        }

        if (note.color == null || note.color.trim().isEmpty()) {
            errors.put("color", "Color is required.");
        } else if (note.color.trim().length() > 60) {
            errors.put("color", "Color value is too long.");
        }

        return errors;
    }

    // Validates date strings with strict format: Mon D, YYYY (e.g., Apr 8, 2024).
    private boolean isValidDateFormat(String value) {
        try {
            LocalDate.parse(value, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }

    // Returns all love notes.
    @GET
    public Response getNotes() {
        return Response.ok(LoveNote.listAll()).build();
    }

    // Returns one love note by ID.
    @GET
    @Path("/{id}")
    public Response getNoteById(@PathParam("id") Long id) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid love note ID.");
        }

        LoveNote note = LoveNote.findById(id);
        if (note != null) {
            return Response.ok(note).build();
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No love note was found for that ID."))
                .build();
    }

    // Creates a new love note.
    @POST
    @Transactional
    public Response addNote(LoveNote note) {
        Map<String, String> errors = validateNote(note);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        note.id = null;
        note.message = note.message.trim();
        note.author = note.author.trim();
        note.date = note.date.trim();
        note.color = note.color.trim();
        note.persist();

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("message", "Love note added.", "note", note))
                .build();
    }

    // Updates an existing love note.
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateNote(@PathParam("id") Long id, LoveNote updatedNote) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid love note ID.");
        }

        Map<String, String> errors = validateNote(updatedNote);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        LoveNote existingNote = LoveNote.findById(id);
        if (existingNote == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "No love note was found for that ID."))
                    .build();
        }

        existingNote.message = updatedNote.message.trim();
        existingNote.author = updatedNote.author.trim();
        existingNote.date = updatedNote.date.trim();
        existingNote.color = updatedNote.color.trim();
        return Response.ok(Map.of("message", "Love note updated.", "note", existingNote)).build();
    }

    // Deletes a love note by ID.
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteNote(@PathParam("id") Long id) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid love note ID.");
        }

        LoveNote note = LoveNote.findById(id);
        if (note != null) {
            note.delete();
            return Response.ok(Map.of("message", "Love note deleted."))
                    .build();
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No love note was found for that ID."))
                .build();
    }
}
