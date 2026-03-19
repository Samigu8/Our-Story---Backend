package com.example.resource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;


@Path("/personalized-greeting")
public class PersonalizedGreetingResource {

    @GET
    public String hello() {
        return "Hello, Samuel Gutierrez!";
    }

    @GET
    @Path("/{name}")
    public String helloWithParam(@PathParam("name") String name) {
        return "Hello, " + name + "!";
    }

    @GET
    @Path("/with-body")
    public String helloWithBody(Person person) {
        return "Hello, " + person.name + "! I hear you are " + person.age + " years old and your favorite thing is " + person.favoriteThing + ".";
    }
}