package com.rz.kuliahku.model;

import java.util.List;

public class ParentSubject {
    String day;
    List<Subject> subjectList;

    public ParentSubject(String day, List<Subject> subjectList) {
        this.day = day;
        this.subjectList = subjectList;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<Subject> getSubjectList() {
        return subjectList;
    }

    public void setSubjectList(List<Subject> subjectList) {
        this.subjectList = subjectList;
    }
}
