package com.example.skripsiemergence;

public class User {
    private String email;
    private String password;
    private String fullname;
    private String phone;
    private String gender;
    private String address;
    private String bloodType;
    private String Location;
    private String fam1;
    private String fam2;
    private String fam3;
    public User() {}
    public User(String email, String password){
        this.email = email;
        this.password = password;
    }

    public User(String email, String password, String fullname, String gender, String phone, String address, String bloodType, String Location) {
        this.email = email;
        this.password = password;
        this.fullname = fullname;
        this.gender = gender;
        this.phone = phone;
        this.address = address;
        this.bloodType = bloodType;
        this.Location = Location;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getFullname() {
        return fullname;
    }

    public String getPhone() {
        return phone;
    }

    public String getAddress() {
        return address;
    }

    public String getBloodType() {
        return bloodType;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setBloodType(String bloodType) {
        this.bloodType = bloodType;
    }

    public String getFam1() {
        return fam1;
    }

    public void setFam1(String fam1) {
        this.fam1 = fam1;
    }

    public String getFam2() {
        return fam2;
    }

    public void setFam2(String fam2) {
        this.fam2 = fam2;
    }

    public String getFam3() {
        return fam3;
    }

    public void setFam3(String fam3) {
        this.fam3 = fam3;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }
}
