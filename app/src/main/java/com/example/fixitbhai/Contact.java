package com.example.fixitbhai;

import java.io.Serializable;

public class Contact implements Serializable {
    private final String name;
    private final String phone;
    private final String category;

    public Contact(String name, String phone, String category) {
        this.name = name;
        this.phone = phone;
        this.category = category;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getCategory() { return category; }
}