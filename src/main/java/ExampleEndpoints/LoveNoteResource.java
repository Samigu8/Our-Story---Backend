package com.example.resource;

/*
 * In-memory CRUD API for love notes with payload validation and safe errors.
 */

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Path("/lovenotes")
public class LoveNoteResource {

    private final List<LoveNote> notes = new ArrayList<>(List.of(
            createNote(1, "Every moment with you feels like a dream come true. Thank you for being my person.", "From You", "Feb 14, 2024", "from-pink-400 to-rose-400"),
            createNote(2, "I love how you make me laugh even on the hardest days. You're my sunshine.", "From Me", "Feb 10, 2024", "from-purple-400 to-indigo-400"),
            createNote(3, "The way you look at me makes me feel like the luckiest person in the world.", "From You", "Jan 28, 2024", "from-blue-400 to-cyan-400"),
            createNote(4, "Home isn't a place, it's you. Wherever we are together, that's where I belong.", "From Me", "Jan 15, 2024", "from-pink-400 to-purple-400"),
            createNote(5, "Thank you for loving all of me - the good, the bad, and everything in between.", "From You", "Jan 5, 2024", "from-rose-400 to-pink-400"),
            createNote(6, "You make ordinary moments extraordinary just by being there. I love our little life together.", "From Me", "Dec 25, 2023", "from-indigo-400 to-purple-400"),
            createNote(7, "I fall in love with you more and more each day. Here's to forever and always.", "From You", "Dec 10, 2023", "from-cyan-400 to-blue-400"),
            createNote(8, "Your smile is my favorite thing in the world. Never stop being your wonderful self.", "From Me", "Nov 22, 2023", "from-purple-400 to-pink-400")
    ));

    // Helper for creating seeded love note records.
    private LoveNote createNote(int id, String message, String author, String date, String color) {
        LoveNote note = new LoveNote();
        note.id = id;
        note.message = message;
        note.author = author;
        note.date = date;
        note.color = color;
        return note;
    }

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
        } else if (note.date.trim().length() > 40) {
            errors.put("date", "Date must be 40 characters or fewer.");
        }

        if (note.color == null || note.color.trim().isEmpty()) {
            errors.put("color", "Color is required.");
        } else if (note.color.trim().length() > 60) {
            errors.put("color", "Color value is too long.");
        }

        return errors;
    }

    // Generates the next in-memory ID value.
    private int nextId() {
        int maxId = 0;
        for (LoveNote note : notes) {
            maxId = Math.max(maxId, note.id);
        }
        return maxId + 1;
    }

    // Returns all love notes.
    @GET
    public Response getNotes() {
        return Response.ok(notes).build();
    }

    // Returns one love note by ID.
    @GET
    @Path("/{id}")
    public Response getNoteById(@PathParam("id") int id) {
        if (id < 1) {
            return badRequest("Please provide a valid love note ID.");
        }

        for (LoveNote note : notes) {
            if (note.id == id) {
                return Response.ok(note).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No love note was found for that ID."))
                .build();
    }

    // Creates a new love note.
    @POST
    public Response addNote(LoveNote note) {
        Map<String, String> errors = validateNote(note);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        LoveNote noteToStore = new LoveNote();
        noteToStore.id = nextId();
        noteToStore.message = note.message.trim();
        noteToStore.author = note.author.trim();
        noteToStore.date = note.date.trim();
        noteToStore.color = note.color.trim();

        notes.add(noteToStore);

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("message", "Love note added.", "note", noteToStore))
                .build();
    }

    // Updates an existing love note.
    @PUT
    @Path("/{id}")
    public Response updateNote(@PathParam("id") int id, LoveNote updatedNote) {
        if (id < 1) {
            return badRequest("Please provide a valid love note ID.");
        }

        Map<String, String> errors = validateNote(updatedNote);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        for (LoveNote note : notes) {
            if (note.id == id) {
                note.message = updatedNote.message.trim();
                note.author = updatedNote.author.trim();
                note.date = updatedNote.date.trim();
                note.color = updatedNote.color.trim();
                return Response.ok(Map.of("message", "Love note updated.", "note", note)).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No love note was found for that ID."))
                .build();
    }

    // Deletes a love note by ID.
    @DELETE
    @Path("/{id}")
    public Response deleteNote(@PathParam("id") int id) {
        if (id < 1) {
            return badRequest("Please provide a valid love note ID.");
        }

        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).id == id) {
                notes.remove(i);
                return Response.ok(Map.of("message", "Love note deleted."))
                        .build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No love note was found for that ID."))
                .build();
    }
}
