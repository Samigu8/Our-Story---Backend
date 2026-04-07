package com.example.resource;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/*
 * Person data model used by the in-memory people API endpoints.
 */

@Entity
@Table(name = "people")
public class Person extends PanacheEntity {
    public String name;
    public int age;
    public String favoriteThing;
}