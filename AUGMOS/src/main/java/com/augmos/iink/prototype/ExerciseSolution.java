package com.augmos.iink.prototype;

import com.google.firebase.Timestamp;


public class ExerciseSolution {
    private String excerciseID;
    private Boolean correct;
    private String jinx;
    private String mathml;
    private Timestamp timestamp;

    public ExerciseSolution(String excerciseID, Boolean correct, Timestamp time, String jinx, String mathml) {
        this.excerciseID = excerciseID;
        this.correct = correct;
        this.timestamp = time;
        this.jinx = jinx;
        this.mathml = mathml;
    }
    public ExerciseSolution() {

    }
    public ExerciseSolution(String excerciseID, String jinx) {
        this.excerciseID = excerciseID;
        this.jinx = jinx;
    }
    public ExerciseSolution(String excerciseID, String jinx, String mathml) {
        this.excerciseID = excerciseID;
        this.jinx = jinx;
        this.mathml = mathml;
    }

    public String getExcerciseID() {
        return excerciseID;
    }

    public void setExcerciseID(String excerciseID) {
        this.excerciseID = excerciseID;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public void setCorrect(Boolean correct) {
        this.correct = correct;
    }

    public String getJinx() {
        return jinx;
    }

    public void setJinx(String jinx) {
        this.jinx = jinx;
    }

    public String getMathml() {
        return mathml;
    }

    public void setMathml(String mathml) {
        this.mathml = mathml;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
