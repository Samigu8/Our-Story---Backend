package com.example.resource;

/*
 * Database-backed CRUD API for timeline events used by the frontend timeline page.
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
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Path("/timeline")
public class TimelineResource {

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

    // Performs field-level validation for timeline event payloads.
    private Map<String, String> validateEvent(TimelineEvent event) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (event == null) {
            errors.put("event", "Event details are required.");
            return errors;
        }

        if (event.title == null || event.title.trim().isEmpty()) {
            errors.put("title", "Title is required.");
        } else if (event.title.trim().length() > 80) {
            errors.put("title", "Title must be 80 characters or fewer.");
        }

        if (event.date == null || event.date.trim().isEmpty()) {
            errors.put("date", "Date is required.");
        } else if (!isValidDateFormat(event.date.trim())) {
            errors.put("date", "Date must use format Mon D, YYYY (example: Apr 8, 2024).");
        }

        if (event.description == null || event.description.trim().isEmpty()) {
            errors.put("description", "Description is required.");
        } else if (event.description.trim().length() > 400) {
            errors.put("description", "Description must be 400 characters or fewer.");
        }

        if (event.imageUrl != null && event.imageUrl.trim().length() > 500) {
            errors.put("imageUrl", "Image URL must be 500 characters or fewer.");
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

    // Returns all timeline events.
    @GET
    public Response getEvents() {
        return Response.ok(TimelineEvent.listAll()).build();
    }

    // Returns one timeline event by ID.
    @GET
    @Path("/{id}")
    public Response getEventById(@PathParam("id") Long id) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid timeline event ID.");
        }

        TimelineEvent event = TimelineEvent.findById(id);
        if (event != null) {
            return Response.ok(event).build();
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No timeline event was found for that ID."))
                .build();
    }

    // Creates a new timeline event.
    @POST
    @Transactional
    public Response addEvent(TimelineEvent event) {
        Map<String, String> errors = validateEvent(event);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        event.id = null;
        event.title = event.title.trim();
        event.date = event.date.trim();
        event.description = event.description.trim();
        event.imageUrl = event.imageUrl == null || event.imageUrl.trim().isEmpty() ? null : event.imageUrl.trim();
        event.persist();

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("message", "Timeline event created.", "event", event))
                .build();
    }

    // Updates an existing timeline event.
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updateEvent(@PathParam("id") Long id, TimelineEvent updatedEvent) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid timeline event ID.");
        }

        Map<String, String> errors = validateEvent(updatedEvent);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        TimelineEvent existingEvent = TimelineEvent.findById(id);
        if (existingEvent == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "No timeline event was found for that ID."))
                    .build();
        }

        existingEvent.title = updatedEvent.title.trim();
        existingEvent.date = updatedEvent.date.trim();
        existingEvent.description = updatedEvent.description.trim();
        existingEvent.imageUrl = updatedEvent.imageUrl == null || updatedEvent.imageUrl.trim().isEmpty()
            ? null
            : updatedEvent.imageUrl.trim();

        return Response.ok(Map.of("message", "Timeline event updated.", "event", existingEvent)).build();
    }

    // Deletes a timeline event by ID.
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deleteEvent(@PathParam("id") Long id) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid timeline event ID.");
        }

        TimelineEvent event = TimelineEvent.findById(id);
        if (event != null) {
            event.delete();
            return Response.ok(Map.of("message", "Timeline event deleted."))
                    .build();
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No timeline event was found for that ID."))
                .build();
    }
}
