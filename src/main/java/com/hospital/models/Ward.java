package com.hospital.models;

import com.hospital.enums.WardType;

public class Ward {
    private int id;
    private String name;
    private WardType type;

    public Ward() {}

    public Ward(int id, String name, WardType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public WardType getType() { return type; }
    public void setType(WardType type) { this.type = type; }
}
