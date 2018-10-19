package com.example.model;

import javax.persistence.*;

@Entity
@Table(name = "targetManager")
public class TargetManager {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "target_manager_id")
    private int id;

    @Column(name = "target_id")
    private String targetId;

    @Column(name = "name")
    private String name;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "image_target")
    private String image;

    @Column(name = "active")
    private boolean active;

    @ManyToOne
    @JoinColumn(name="location")
    private Location location;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
