package com.example.resource;

/*
 * Memory photo database entity used by the memories API endpoints.
 */

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "memory_photos")
public class MemoryPhoto extends PanacheEntity {
    public String caption;
    public String imageUrl;
}
