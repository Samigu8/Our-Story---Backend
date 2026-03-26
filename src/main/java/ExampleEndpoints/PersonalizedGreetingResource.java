package com.example.resource;

/*
 * Demo greeting endpoints showing static and parameterized response patterns.
 */

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;


@Path("/personalized-greeting")
public class PersonalizedGreetingResource {

    // Returns a fixed greeting.
    @GET
    public String hello() {
        return "Hello, Samuel Gutierrez!";
    }

    // Returns a greeting that includes a path parameter.
    @GET
    @Path("/{name}")
    public String helloWithParam(@PathParam("name") String name) {
        return "Hello, " + name + "!";
    }

    // Returns a greeting built from a request body.
    @GET
    @Path("/with-body")
    public String helloWithBody(Person person) {
        return "Hello, " + person.name + "! I hear you are " + person.age + " years old and your favorite thing is " + person.favoriteThing + ".";
    }
}