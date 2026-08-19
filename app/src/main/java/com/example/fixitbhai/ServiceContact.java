package com.example.fixitbhai;

public class ServiceContact {
    private int id;
    private String name;
    private String phone;
    private String category;
    private String notes;

    public ServiceContact(int id, String name, String phone, String category, String notes) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.category = category;
        this.notes = notes;
    }

    public ServiceContact(String name, String phone, String category, String notes) {
        this.name = name;
        this.phone = phone;
        this.category = category;
        this.notes = notes;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getCategory() { return category; }
    public String getNotes() { return notes; }

    public void setId(int id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCategory(String category) { this.category = category; }
    public void setNotes(String notes) { this.notes = notes; }
}