package com.rz.kuliahku.model;

import com.google.firebase.firestore.FirebaseFirestore;

public class Subject {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String id, name, code, day, startTime, endTime;

    public Subject(String id, String name, String code, String day, String startTime, String endTime) {
        this.id = id;
        this.name = name;
        this.code = code;
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
