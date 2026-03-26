package com.example.resource;

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

@Path("/timeline")
public class TimelineResource {

    private final List<TimelineEvent> timelineEvents = new ArrayList<>(List.of(
            createEvent(1, "First Date", "January 15, 2023", "The day we met at the cozy coffee shop downtown. We talked for hours and knew something special was beginning."),
            createEvent(2, "Beach Vacation", "March 22, 2023", "Our first trip together. Watching the sunset by the ocean, creating memories that would last forever."),
            createEvent(3, "Moving In Together", "June 10, 2023", "We found our perfect little apartment and started building our home together, filling it with love and laughter."),
            createEvent(4, "Anniversary Dinner", "January 15, 2024", "Celebrating one year together at our favorite restaurant where it all began. So grateful for this journey."),
            createEvent(5, "Road Trip Adventure", "April 8, 2024", "An unforgettable cross-country road trip, discovering new places and making countless memories along the way."),
            createEvent(6, "Family Gathering", "August 20, 2024", "The first time our families met. A beautiful day filled with warmth, love, and new connections.")
    ));

    private TimelineEvent createEvent(int id, String title, String date, String description) {
        TimelineEvent event = new TimelineEvent();
        event.id = id;
        event.title = title;
        event.date = date;
        event.description = description;
        return event;
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", message))
                .build();
    }

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
        } else if (event.date.trim().length() > 40) {
            errors.put("date", "Date must be 40 characters or fewer.");
        }

        if (event.description == null || event.description.trim().isEmpty()) {
            errors.put("description", "Description is required.");
        } else if (event.description.trim().length() > 400) {
            errors.put("description", "Description must be 400 characters or fewer.");
        }

        return errors;
    }

    private int nextId() {
        int maxId = 0;
        for (TimelineEvent event : timelineEvents) {
            maxId = Math.max(maxId, event.id);
        }
        return maxId + 1;
    }

    @GET
    public Response getEvents() {
        return Response.ok(timelineEvents).build();
    }

    @GET
    @Path("/{id}")
    public Response getEventById(@PathParam("id") int id) {
        if (id < 1) {
            return badRequest("Please provide a valid timeline event ID.");
        }

        for (TimelineEvent event : timelineEvents) {
            if (event.id == id) {
                return Response.ok(event).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No timeline event was found for that ID."))
                .build();
    }

    @POST
    public Response addEvent(TimelineEvent event) {
        Map<String, String> errors = validateEvent(event);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        TimelineEvent eventToStore = new TimelineEvent();
        eventToStore.id = nextId();
        eventToStore.title = event.title.trim();
        eventToStore.date = event.date.trim();
        eventToStore.description = event.description.trim();
        timelineEvents.add(eventToStore);

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("message", "Timeline event created.", "event", eventToStore))
                .build();
    }

    @PUT
    @Path("/{id}")
    public Response updateEvent(@PathParam("id") int id, TimelineEvent updatedEvent) {
        if (id < 1) {
            return badRequest("Please provide a valid timeline event ID.");
        }

        Map<String, String> errors = validateEvent(updatedEvent);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        for (TimelineEvent event : timelineEvents) {
            if (event.id == id) {
                event.title = updatedEvent.title.trim();
                event.date = updatedEvent.date.trim();
                event.description = updatedEvent.description.trim();
                return Response.ok(Map.of("message", "Timeline event updated.", "event", event)).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No timeline event was found for that ID."))
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteEvent(@PathParam("id") int id) {
        if (id < 1) {
            return badRequest("Please provide a valid timeline event ID.");
        }

        for (int i = 0; i < timelineEvents.size(); i++) {
            if (timelineEvents.get(i).id == id) {
                timelineEvents.remove(i);
                return Response.ok(Map.of("message", "Timeline event deleted."))
                        .build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No timeline event was found for that ID."))
                .build();
    }
}
