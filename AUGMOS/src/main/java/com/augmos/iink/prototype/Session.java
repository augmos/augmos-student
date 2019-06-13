package com.augmos.iink.prototype;


import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class Session {
    private static String studentID = "AamLoHFMjnnkJFXSQ6EE";
    private static String teacherID = "p8pnhraij8kHIEm4ZT3S";
    private static String aufgabengebiet = "addition";
    private static LinkedList<Exercise> currentExcercises = new LinkedList<>();
    private static LinkedList<String> currentExcercisesID = new LinkedList<>();
    private static int currExInt = 0;

    public static String getStudentID() {
        return studentID;
    }

    public static String getTeacherID() {
        return teacherID;
    }

    public static String getAufgabengebiet() {
        return aufgabengebiet;
    }

    public static void setAufgabengebiet(String aufgabengebiet) {
        Session.aufgabengebiet = aufgabengebiet;
    }

    public static void addExcercise(String id, Exercise ex ){
        currentExcercisesID.add(id);
        currentExcercises.add(ex);
    }

    public static Pair<String, Exercise> getNextExcercise(){

        if(currentExcercises.isEmpty()){
            throw new RuntimeException("No Excercises Found");
        }

        if(currExInt >= currentExcercises.size() - 1){
            currExInt = 0;
        }else {
            currExInt++;
        }

        return new Pair<>(currentExcercisesID.get(currExInt), currentExcercises.get(currExInt));
    }
}
