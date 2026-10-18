package com.hotel.hotel_booking_system.model;

public class Guest{

    private long id;
    private String fullName;
    private String phone;
    private String email;
    private String idNumber;

    public Guest(){
    }

    public Guest(long id, String fullName, String phone, String email, String idNumber){

        this.id = id;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.idNumber = idNumber;
    }

    public long getId(){
        return id;
    }

    public void setId(long id){
        this.id = id;
    }

    public String getFullName(){
        return fullName;
    }

    public void setFullName(String fullName){
        this.fullName = fullName;
    }

    public String getPhone(){
        return phone;
    }

    public void setPhone(String phone){
        this.phone = phone;
    }

    public String getEmail(){
        return email;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getIdNumber(){
        return idNumber;
    }

    public void setIdNumber(String idNumber){
        this.idNumber = idNumber;
    }
}
