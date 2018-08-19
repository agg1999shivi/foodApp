package com.example.dell.yoursapp.Model;

/**
 * Created by 123456 on 2017/11/16.
 */

public class User {
    private String Name;
    private String Password;
    private String Phone;
    private String IsStaff;
    private String secureCode;

    public User() {
    }

    public User(String name, String password,String secureCode) {
        Name = name;
        Password = password;
        IsStaff="false";
        this.secureCode=secureCode;
    }

    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public void setPhone(String phone) {
        Phone = phone;
    }

    public String getPhone() {

        return Phone;
    }


    public String getIsStaff() {
        return IsStaff;
    }

    public void setIsStaff(String isStaff) {
        IsStaff = isStaff;

    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPassword() {
        return Password;
    }

    public void setPassword(String password) {
        Password = password;
    }
}
