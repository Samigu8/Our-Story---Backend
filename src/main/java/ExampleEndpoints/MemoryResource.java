package com.example.resource;

/*
 * In-memory CRUD API for memory photos, including upload metadata validation.
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

@Path("/memories/photos")
public class MemoryResource {

    private final List<MemoryPhoto> photos = new ArrayList<>(List.of(
            createPhoto(1, "Sunset at the beach during our first vacation together"),
            createPhoto(2, "Coffee date where we first met and fell in love"),
            createPhoto(3, "Cozy winter evening by the fireplace"),
            createPhoto(4, "Hiking adventure in the mountains"),
            createPhoto(5, "Celebrating our first anniversary"),
            createPhoto(6, "Dancing under the stars at the summer festival"),
            createPhoto(7, "Cooking together in our new home"),
            createPhoto(8, "Road trip memories and scenic views"),
            createPhoto(9, "Laughing together at the amusement park"),
            createPhoto(10, "Quiet moment reading books on a lazy Sunday"),
            createPhoto(11, "Our families meeting for the first time"),
            createPhoto(12, "Spontaneous picnic in the park")
    ));

    // Helper for creating seeded photo records.
    private MemoryPhoto createPhoto(int id, String caption) {
        MemoryPhoto photo = new MemoryPhoto();
        photo.id = id;
        photo.caption = caption;
        photo.imageUrl = "";
        return photo;
    }

    // Builds a standardized 400 response with a user-friendly message.
    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", message))
                .build();
    }

    // Performs field-level validation for memory photo payloads.
    private Map<String, String> validatePhoto(MemoryPhoto photo) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (photo == null) {
            errors.put("photo", "Photo details are required.");
            return errors;
        }

        if (photo.caption == null || photo.caption.trim().isEmpty()) {
            errors.put("caption", "Caption is required.");
        } else if (photo.caption.trim().length() > 240) {
            errors.put("caption", "Caption must be 240 characters or fewer.");
        }

        if (photo.imageUrl != null && photo.imageUrl.trim().length() > 300) {
            errors.put("imageUrl", "Image URL must be 300 characters or fewer.");
        }

        return errors;
    }

    // Generates the next in-memory ID value.
    private int nextId() {
        int maxId = 0;
        for (MemoryPhoto photo : photos) {
            maxId = Math.max(maxId, photo.id);
        }
        return maxId + 1;
    }

    // Returns all stored memory photos.
    @GET
    public Response getPhotos() {
        return Response.ok(photos).build();
    }

    // Returns one memory photo by ID.
    @GET
    @Path("/{id}")
    public Response getPhotoById(@PathParam("id") int id) {
        if (id < 1) {
            return badRequest("Please provide a valid photo ID.");
        }

        for (MemoryPhoto photo : photos) {
            if (photo.id == id) {
                return Response.ok(photo).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No photo was found for that ID."))
                .build();
    }

    // Adds a new memory photo record.
    @POST
    public Response uploadPhoto(MemoryPhoto photo) {
        Map<String, String> errors = validatePhoto(photo);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        MemoryPhoto photoToStore = new MemoryPhoto();
        photoToStore.id = nextId();
        photoToStore.caption = photo.caption.trim();
        photoToStore.imageUrl = photo.imageUrl == null ? "" : photo.imageUrl.trim();

        photos.add(photoToStore);

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("message", "Photo uploaded.", "photo", photoToStore))
                .build();
    }

    // Updates an existing memory photo record.
    @PUT
    @Path("/{id}")
    public Response updatePhoto(@PathParam("id") int id, MemoryPhoto updatedPhoto) {
        if (id < 1) {
            return badRequest("Please provide a valid photo ID.");
        }

        Map<String, String> errors = validatePhoto(updatedPhoto);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        for (MemoryPhoto photo : photos) {
            if (photo.id == id) {
                photo.caption = updatedPhoto.caption.trim();
                photo.imageUrl = updatedPhoto.imageUrl == null ? "" : updatedPhoto.imageUrl.trim();
                return Response.ok(Map.of("message", "Photo updated.", "photo", photo)).build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No photo was found for that ID."))
                .build();
    }

    // Deletes a memory photo by ID.
    @DELETE
    @Path("/{id}")
    public Response deletePhoto(@PathParam("id") int id) {
        if (id < 1) {
            return badRequest("Please provide a valid photo ID.");
        }

        for (int i = 0; i < photos.size(); i++) {
            if (photos.get(i).id == id) {
                photos.remove(i);
                return Response.ok(Map.of("message", "Photo deleted."))
                        .build();
            }
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No photo was found for that ID."))
                .build();
    }
}
