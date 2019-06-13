package com.augmos.analyzer;

import com.augmos.iink.prototype.Exercise;
import com.augmos.iink.prototype.ExerciseSolution;
import com.google.firebase.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public final class AnalyzerImpl implements Analyzer {

    public static final Logger LOG = LoggerFactory.getLogger(AnalyzerImpl.class);

    public static final String INTEGER_SOLUTION = "%int";

    public static final String MATHML_HEADER = "<math xmlns='http://www.w3.org/1998/Math/MathML'>";
    public static final String MATHML_TRAILER = "</math>";
    public static final String MATHML_TABLE_HEADER = "<mtable columnalign='left'>";
    public static final String MATHML_TABLE_TRAILER = "</mtable>";
    public static final String MATHML_TABLE_MTR_HEADER = "<mtr>";
    public static final String MATHML_TABLE_MTR_TRAILER = "</mtr>";
    public static final String MATHML_TABLE_MTD_HEADER = "<mtd>";
    public static final String MATHML_TABLE_MTD_TRAILER = "</mtd>";

    public static final List<String> MATHML_EXCLUDE = new LinkedList<>();
    static {
        MATHML_EXCLUDE.add("<mn>");
        MATHML_EXCLUDE.add("</mn>");
        MATHML_EXCLUDE.add("<mo>");
        MATHML_EXCLUDE.add("</mo>");
        MATHML_EXCLUDE.add("<mi>");
        MATHML_EXCLUDE.add("</mi>");
    }

    public static void main(String[] args) {
        final String id = "some_id";
        final Map<String, String> solution = new HashMap<>();
        solution.put("%int", "2");
        final Exercise exercise = new Exercise("What is 2+2?", solution);
        final String jiix = "jiix-string";
        final String mathml = "<math xmlns='http://www.w3.org/1998/Math/MathML'> <mn> 2 </mn> </math>";
        final AnalyzerImpl anal = new AnalyzerImpl();
        final ExerciseSolution sol = anal.analyze(id, exercise, jiix, mathml);
        System.out.println("" + sol.getCorrect());
    }

    @Override
    public ExerciseSolution analyze(
            final String id,
            final Exercise exercise,
            final String jiix,
            final String mathml
    ) {
        final LinkedList<String> tokens = new LinkedList<>(
                Arrays.asList(
                        mathml
                                .replaceAll("[\r\n\t]+","")
                                .replaceAll(">\\s+", ">")
                                .replaceAll("\\s+<", "<")
                                .split("(?<=>)|(?=<)", -1)
                )
        );
        tokens.removeAll(Arrays.asList("", null));
        LOG.info("Starting analysis, recognized tokens: {}.", tokens);

        final LinkedList<String> lines = new LinkedList<>();

        //check for mathml boundary
        if (!MATHML_HEADER.equals(tokens.getFirst()) || !MATHML_TRAILER.equals(tokens.getLast()))
            throw new RuntimeException("Not valid MathML."); //TODO custom exception

        //one or more lines
        if (!MATHML_TABLE_HEADER.equals(tokens.get(1)) || !MATHML_TABLE_TRAILER.equals(tokens.get(tokens.size() - 2))) {
            final List<String> content = tokens.subList(1, tokens.size() - 1);
            final StringBuilder output = new StringBuilder();

            for (final String token : content) {
                if (!MATHML_EXCLUDE.contains(token))
                    output.append(token);
            }

            lines.add(output.toString());

        } else {
            final List<String> content = tokens.subList(2, tokens.size() - 2);
            final StringBuilder builder = new StringBuilder();
            int phase = 0;  //0 - begin, 1 - mtd open, 2 - mtr open, 3 - mtr closed

            for (final String token : content) {
                switch (phase) {
                    case 0 : {
                        if (MATHML_TABLE_MTR_HEADER.equals(token))
                            phase++;
                    } break;

                    case 1 : {
                        if (MATHML_TABLE_MTD_HEADER.equals(token))
                            phase++;
                    } break;

                    case 2 : {
                        if (MATHML_TABLE_MTD_TRAILER.equals(token)) {
                            phase++;
                            lines.add(builder.toString());
                            builder.setLength(0);   //resets builder
                        } else if (!MATHML_EXCLUDE.contains(token)) {
                            builder.append(token);
                        }
                    } break;

                    case 3 : {
                        if (MATHML_TABLE_MTR_TRAILER.equals(token))
                            phase = 0;
                    } break;
                }
            }
        }

        final Collection<String> keys = exercise.getSolution().keySet();
        final List<String> regex = new LinkedList<>();
        boolean result;

        //if arithmetic exercise
        if (keys.contains(INTEGER_SOLUTION)) {
            final String integerSolutionRegex = "(.*=" + exercise.getSolution().get(INTEGER_SOLUTION) + ".*)|" + exercise.getSolution().get(INTEGER_SOLUTION);
            regex.add(integerSolutionRegex);

            result = lines.getLast().matches(integerSolutionRegex);
        } else {
            for (String k : keys) {
                regex.add(".*" + k + ".*=" + exercise.getSolution().get(k) + ".*");
            }

            final Map<String, Boolean> results = new HashMap<>();
            boolean current;

            for (String r : regex) {
                current = false;

                for (String l : lines) {
                    if (l.matches(r)) {
                        current = true;
                    }
                }

                results.put(r, current);
            }

            result = true;
            for (String r : regex) {
                if (!results.get(r)) {
                    result = false;
                    break;
                }
            }
        }

        LOG.info("Analysis complete. Lines: {}. Regex: {}. Result is {}.", lines, regex, result);

        return new ExerciseSolution(id, result, Timestamp.now(), jiix, mathml);
    }

}