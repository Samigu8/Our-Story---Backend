package com.example.resource;

/*
 * Timeline event database entity used by the timeline API endpoints.
 */

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "timeline_events")
public class TimelineEvent extends PanacheEntity {
    public String title;
    public String date;
    public String description;
}
