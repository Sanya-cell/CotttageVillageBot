package com.example.hose2.Model;


import lombok.*;



@Data
public class Vehicle {

    private  Long id;


    private String licensePlate;
    private String nameModel;
    private String model;
    private String color;
    private String city;
    private String street;
    private String home;

    public Vehicle(String licensePlate, String nameModel, String model, String color, String city, String street, String home) {

        this.licensePlate = licensePlate;
        this.nameModel = nameModel;
        this.model = model;
        this.color = color;
        this.city = city;
        this.street = street;
        this.home = home;
    }
}

