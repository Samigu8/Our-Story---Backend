package com.example.resource;

/*
 * Database-backed CRUD API for people, including validation and user-safe error responses.
 */

import jakarta.transaction.Transactional;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Path("/person")
public class PersonResource {

    // Builds a standardized 400 response with a user-friendly message.
    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(Map.of("message", message))
                .build();
    }

    // Restricts name characters to a safe subset used by frontend validation.
    private boolean hasInvalidNameChars(String value) {
        for (char c : value.toCharArray()) {
            if (!(Character.isLetter(c) || c == ' ' || c == '-' || c == '\'')) {
                return true;
            }
        }
        return false;
    }

    // Performs field-level validation for create/update operations.
    private Map<String, String> validatePerson(Person person) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();

        if (person == null) {
            fieldErrors.put("person", "Person details are required.");
            return fieldErrors;
        }

        if (person.name == null || person.name.trim().isEmpty()) {
            fieldErrors.put("name", "Name is required.");
        } else {
            String trimmedName = person.name.trim();
            if (trimmedName.length() < 2 || trimmedName.length() > 40) {
                fieldErrors.put("name", "Name must be between 2 and 40 characters.");
            } else if (hasInvalidNameChars(trimmedName)) {
                fieldErrors.put("name", "Name may only include letters, spaces, hyphens, and apostrophes.");
            }
        }

        if (person.age < 1 || person.age > 130) {
            fieldErrors.put("age", "Age must be between 1 and 130.");
        }

        if (person.favoriteThing == null || person.favoriteThing.trim().isEmpty()) {
            fieldErrors.put("favoriteThing", "Favorite thing is required.");
        } else {
            String trimmedFavoriteThing = person.favoriteThing.trim();
            if (trimmedFavoriteThing.length() < 2 || trimmedFavoriteThing.length() > 80) {
                fieldErrors.put("favoriteThing", "Favorite thing must be between 2 and 80 characters.");
            }
        }

        return fieldErrors;
    }

    // Creates a person after validating payload and duplicate-name rules.
    @POST
    @Transactional
    public Response addPerson(Person person) {
        Map<String, String> fieldErrors = validatePerson(person);
        if (!fieldErrors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "message", "Please correct the highlighted fields.",
                            "errors", fieldErrors
                    ))
                    .build();
        }

        // Panache manages primary keys; ignore any client-supplied id on create.
        person.id = null;
        String normalizedName = person.name.trim().toLowerCase(Locale.ROOT);
        List<Person> existingPeople = Person.listAll();
        for (Person existingPerson : existingPeople) {
            if (existingPerson.name != null
                    && existingPerson.name.trim().toLowerCase(Locale.ROOT).equals(normalizedName)) {
                return badRequest("A person with that name already exists.");
            }
        }

        person.name = person.name.trim();
        person.favoriteThing = person.favoriteThing.trim();
        person.persist();

        return Response.status(Response.Status.CREATED)
                .entity("Person " + person.name + " created successfully.")
                .build();
    }

    // Returns all people currently stored in the database.
    @GET
    public Response getAllPeople() {
        return Response.ok(Person.listAll()).build();
    }

    // Returns a single person by ID.
    @GET
    @Path("/{id}")
    public Response getPersonById(@PathParam("id") Long id) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid person ID.");
        }

        Person person = Person.findById(id);
        if (person != null) {
            return Response.ok(person).build();
        }

        return Response.status(Response.Status.NOT_FOUND)
                .entity("No person was found for that ID.")
                .build();
    }

    // UPDATE
    @PATCH
    @Path("/{id}")
    @Transactional
    public Response patchPerson(@PathParam("id") Long id, Person updatePerson) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid person ID.");
        }

        Person currentPerson = Person.findById(id);
        if (currentPerson == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Person with ID " + id + " not found").build();
        }

        // if the updated fields are null, update them
        if (updatePerson.name != null) {
            currentPerson.name = updatePerson.name.trim();
        }
        if (updatePerson.age > 0) {
            currentPerson.age = updatePerson.age;
        }
        if (updatePerson.favoriteThing != null) {
            currentPerson.favoriteThing = updatePerson.favoriteThing.trim();
        }

        return Response.ok("Person with ID " + id + " updated successfully").build();
    }

    // Replaces a full person record by ID.
    @PUT
    @Path("/{id}")
    @Transactional
    public Response updatePerson(@PathParam("id") Long id, Person updatedPerson) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid person ID.");
        }

        Map<String, String> fieldErrors = validatePerson(updatedPerson);
        if (!fieldErrors.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of(
                            "message", "Please correct the highlighted fields.",
                            "errors", fieldErrors
                    ))
                    .build();
        }

        Person existingPerson = Person.findById(id);
        if (existingPerson == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Person with ID " + id + " not found").build();
        }

        String normalizedName = updatedPerson.name.trim().toLowerCase(Locale.ROOT);
        List<Person> allPeople = Person.listAll();
        for (Person person : allPeople) {
            if (person.id != null && !person.id.equals(id)
                    && person.name != null
                    && person.name.trim().toLowerCase(Locale.ROOT).equals(normalizedName)) {
                return badRequest("A person with that name already exists.");
            }
        }

        existingPerson.name = updatedPerson.name.trim();
        existingPerson.age = updatedPerson.age;
        existingPerson.favoriteThing = updatedPerson.favoriteThing.trim();

        return Response.ok("Person with ID " + id + " updated successfully").build();
    }

    // Deletes a person by ID.
    @DELETE
    @Path("/{id}")
    @Transactional
    public Response deletePerson(@PathParam("id") Long id) {
        if (id == null || id < 1) {
            return badRequest("Please provide a valid person ID.");
        }

        Person person = Person.findById(id);
        if (person == null) {
            return Response.status(Response.Status.NOT_FOUND).entity("Person with ID " + id + " not found").build();
        }

        person.delete();
        return Response.ok("Person with ID " + id + " deleted successfully").build();
    }
}
