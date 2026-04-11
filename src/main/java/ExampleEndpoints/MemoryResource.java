package com.example.resource;

/*
 * Database-backed CRUD API for memory photos, including upload metadata validation.
 */

import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.Map;

@Path("/memories/photos")
public class MemoryResource {

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

        if (photo.caption != null && photo.caption.trim().length() > 240) {
            errors.put("caption", "Caption must be 240 characters or fewer.");
        }

        if (photo.imageUrl == null || photo.imageUrl.trim().isEmpty()) {
            errors.put("imageUrl", "Image URL is required.");
        } else if (photo.imageUrl.trim().length() > 300) {
            errors.put("imageUrl", "Image URL must be 300 characters or fewer.");
        }

        return errors;
    }

    // Returns all stored memory photos.
    @GET
    public Response getPhotos() {
        return Response.ok(MemoryPhoto.listAll()).build();
    }

    // Returns one memory photo by ID.
    @GET
    @Path("/{id}")
    public Response getPhotoById(@PathParam("id") Long id) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid photo ID.");
        }

        MemoryPhoto photo = MemoryPhoto.findById(id);
        if (photo != null) {
            return Response.ok(photo).build();
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No photo was found for that ID."))
                .build();
    }

    // Adds a new memory photo record.
    @POST
    @Transactional
    public Response uploadPhoto(MemoryPhoto photo) {
        Map<String, String> errors = validatePhoto(photo);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        photo.id = null;
        photo.caption = photo.caption == null ? "" : photo.caption.trim();
        photo.imageUrl = photo.imageUrl.trim();
        photo.persist();

        return Response.status(Response.Status.CREATED)
                .entity(Map.of("message", "Photo uploaded.", "photo", photo))
                .build();
    }

    // Updates an existing memory photo record.
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updatePhoto(@PathParam("id") Long id, MemoryPhoto updatedPhoto) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid photo ID.");
        }

        Map<String, String> errors = validatePhoto(updatedPhoto);
        if (!errors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("message", "Please correct the highlighted fields.", "errors", errors))
                    .build();
        }

        MemoryPhoto existingPhoto = MemoryPhoto.findById(id);
        if (existingPhoto == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(Map.of("message", "No photo was found for that ID."))
                    .build();
        }

        existingPhoto.caption = updatedPhoto.caption == null ? "" : updatedPhoto.caption.trim();
        existingPhoto.imageUrl = updatedPhoto.imageUrl.trim();
        return Response.ok(Map.of("message", "Photo updated.", "photo", existingPhoto)).build();
    }

    // Deletes a memory photo by ID.
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deletePhoto(@PathParam("id") Long id) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid photo ID.");
        }

        MemoryPhoto photo = MemoryPhoto.findById(id);
        if (photo != null) {
            photo.delete();
            return Response.ok(Map.of("message", "Photo deleted."))
                    .build();
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No photo was found for that ID."))
                .build();
    }
}
