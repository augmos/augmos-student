package com.augmos.iink.prototype;

import java.util.HashMap;
import java.util.Map;

public class Exercise {
    private final String content;
    private final Map<String, String> solution;

    public Exercise(
            final String content,
            final Map<String, String> solution
    ) {
        this.content = content;
        this.solution = solution;
    }

    public Exercise() {
        this.content = "Thats weird";
        this.solution = new HashMap<String, String>();
    }

    public String getContent() {
        return content;
    }

    public Map<String, String> getSolution() {
        return solution;
    }

}