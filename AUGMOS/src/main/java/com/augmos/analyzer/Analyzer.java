package com.augmos.analyzer;

import com.augmos.iink.prototype.Exercise;
import com.augmos.iink.prototype.ExerciseSolution;

public interface Analyzer {

    public ExerciseSolution analyze(String id, Exercise exercise, String jiix, String mathml);

}
