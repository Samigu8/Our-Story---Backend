package com.example.resource;

/*
 * In-memory CRUD API for people, including validation and user-safe error responses.
 */

import jakarta.ws.rs.POST;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Path("/person")
public class PersonResource {
    
    
    List<Person> people = new ArrayList<>(List.of(
        new Person() {{
            id = 1;
            name = "Harry Potter";
            age = 11;
            favoriteThing = "quidditch";
        }},
        new Person() {{
            id = 2;
            name = "Hermione Granger";
            age = 11;
            favoriteThing = "learning";
        }},
        new Person() {{
            id = 3;
            name = "Ron Weasley";
            age = 11;
            favoriteThing = "chess";
        }}
    ));

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

    // Generates the next in-memory ID value.
    private int nextId() {
        int maxId = 0;
        for (Person person : people) {
            maxId = Math.max(maxId, person.id);
        }
        return maxId + 1;
    }

    // Creates a person after validating payload and duplicate-name rules.
    @POST
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

        String normalizedName = person.name.trim().toLowerCase(Locale.ROOT);
        for (Person existingPerson : people) {
            if (existingPerson.name != null
                    && existingPerson.name.trim().toLowerCase(Locale.ROOT).equals(normalizedName)) {
                return badRequest("A person with that name already exists.");
            }
        }

        Person personToStore = new Person();
        personToStore.id = nextId();
        personToStore.name = person.name.trim();
        personToStore.age = person.age;
        personToStore.favoriteThing = person.favoriteThing.trim();

        people.add(personToStore);

        return Response.status(Response.Status.CREATED)
                .entity(Map.of(
                        "message", "Person added successfully.",
                        "person", personToStore
                ))
                .build();
    }

    // Returns all people currently stored in memory.
    @GET
    public Response getPeople() {
        return Response.ok(people).build();
    }

    // Returns a single person by ID.
    @GET
    @Path("/{id}")
    public Response getPersonById(@PathParam("id") int id) {
        if (id < 1) {
            return badRequest("Please provide a valid person ID.");
        }

        for (Person person : people) {
            if (person.id == id) {
                return Response.ok(person).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("message", "No person was found for that ID."))
                .build();
    }

    // Updates only the age field for a person.
    @PATCH
    @Path("/{id}/age")
    public Response updatePersonAge(@PathParam("id") int id, int newAge) {
        for (Person person : people) {
            if (person.id == id) {
                person.age = newAge;
                return Response.ok("Person " + person.name + "'s age updated to " + newAge).build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Person with ID " + id + " not found").build();
    }

    // Replaces a full person record by ID.
    @PUT
    @Path("/{id}")
    public Response updatePerson(@PathParam("id") int id, Person updatedPerson) {
        for (int i = 0; i < people.size(); i++) {
            if (people.get(i).id == id) {
                people.set(i, updatedPerson);
                return Response.ok("Person with ID " + id + " updated successfully").build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Person with ID " + id + " not found").build();
    }

    // Deletes a person by ID.
    @DELETE
    @Path("/{id}")
    public Response deletePerson(@PathParam("id") int id) {
        for (int i = 0; i < people.size(); i++) {
            if (people.get(i).id == id) {
                people.remove(i);
                return Response.ok("Person with ID " + id + " deleted successfully").build();
            }
        }
        return Response.status(Response.Status.NOT_FOUND).entity("Person with ID " + id + " not found").build();
    }
}

