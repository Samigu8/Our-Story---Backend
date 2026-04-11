package com.example.resource;

/*
 * Love note database entity used by the love notes API endpoints.
 */

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "love_notes")
public class LoveNote extends PanacheEntity {
    public String message;
    public String author;
    public String date;
    public String color;
}
