package vsu.cs.domain;

import java.util.Objects;

public class Car implements Identifiable<Long> {
    private Long id;
    private Long clientId;
    private String brand;
    private String model;
    private String plateNumber;
    private int year;

    public Car(Long id, Long clientId, String brand, String model, String plateNumber, int year) {
        this.id = id;
        this.clientId = clientId;
        this.brand = brand;
        this.model = model;
        this.plateNumber = plateNumber;
        this.year = year;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getClientId() { return clientId; }
    public void setClientId(Long clientId) { this.clientId = clientId; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    @Override
    public String toString() {
        return "Car{id=" + id + ", clientId=" + clientId + ", brand='" + brand
                + "', model='" + model + "', plate='" + plateNumber + "', year=" + year + "}";
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        Car car = (Car) object;
        return Objects.equals(id, car.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}