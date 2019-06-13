package com.augmos.iink.prototype;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.augmos.analyzer.Analyzer;
import com.augmos.analyzer.AnalyzerImpl;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.myscript.iink.ConversionState;
import com.myscript.iink.Editor;
import com.myscript.iink.MimeType;
import com.myscript.iink.uireferenceimplementation.EditorView;
import com.myscript.iink.uireferenceimplementation.InputController;

import java.io.IOException;


public class EditorFragmentActivity extends AppCompatActivity implements View.OnClickListener {
    String TAG = "EditorFragmentActivity";
    EditorView editorView;
    private FirebaseFirestore fb;
    private DocumentReference studentRef;
    private DocumentReference teacherRef;
    private CollectionReference exercisesCollection;
    private Pair<String, Exercise> currentExcercise;
    private long startTime;
    private int ExcercisesDone = 0;
    private int AnzahlExcercises = 0;
    //Todo Progress spinnt noch (keine 50%)


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ErrorActivity.installHandler(this);
        //Show Editor Layout composed out of 2 Fragments
        setContentView(R.layout.activity_fragment_editor);
        editorView = findViewById(R.id.editor_view);

        fb = FirebaseFirestore.getInstance();
        studentRef = fb.collection("students").document(Session.getStudentID());
        teacherRef = fb.collection("teachers").document(Session.getTeacherID());
        exercisesCollection = fb.collection("exercises").document(Session.getAufgabengebiet()).collection("exercises");

        //get current Exercises
        getExcercises(true);

        findViewById(R.id.button_input_mode_forcePen).setOnClickListener(this);
        findViewById(R.id.button_input_mode_forceTouch).setOnClickListener(this);
        findViewById(R.id.button_input_mode_auto).setOnClickListener(this);
        findViewById(R.id.button_undo).setOnClickListener(this);
        findViewById(R.id.button_redo).setOnClickListener(this);
        findViewById(R.id.button_clear).setOnClickListener(this);

        invalidateIconButtons();


    }



    @Override
        public boolean onCreateOptionsMenu(Menu menu)
        {
            getMenuInflater().inflate(R.menu.activity_main, menu);

            MenuItem convertMenuItem = menu.findItem(R.id.menu_convert);
            convertMenuItem.setEnabled(true);

            return super.onCreateOptionsMenu(menu);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.menu_convert:
                {
                    analyze();
                    return true;
                }
                default:
                {
                    return super.onOptionsItemSelected(item);
                }
            }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.button_input_mode_forcePen:
                setInputMode(InputController.INPUT_MODE_FORCE_PEN);
                break;
            case R.id.button_input_mode_forceTouch:
                setInputMode(InputController.INPUT_MODE_FORCE_TOUCH);
                break;
            case R.id.button_input_mode_auto:
                setInputMode(InputController.INPUT_MODE_AUTO);
                break;
            case R.id.button_undo:
                editorView.getEditor().undo();
                break;
            case R.id.button_redo:
                editorView.getEditor().redo();
                break;
            case R.id.button_clear:
                editorView.getEditor().clear();
                break;
            default:
                Log.e(TAG, "Failed to handle click event");
                break;
        }
    }

    private void setInputMode(int inputMode)
    {
        editorView.setInputMode(inputMode);
        findViewById(R.id.button_input_mode_forcePen).setEnabled(inputMode != InputController.INPUT_MODE_FORCE_PEN);
        findViewById(R.id.button_input_mode_forceTouch).setEnabled(inputMode != InputController.INPUT_MODE_FORCE_TOUCH);
        findViewById(R.id.button_input_mode_auto).setEnabled(inputMode != InputController.INPUT_MODE_AUTO);
    }

    private void invalidateIconButtons()
    {
        Editor editor = editorView.getEditor();
        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                ImageButton imageButtonUndo = findViewById(R.id.button_undo);
                imageButtonUndo.setEnabled(true);
                ImageButton imageButtonRedo = findViewById(R.id.button_redo);
                imageButtonRedo.setEnabled(true);
            }
        });
    }


    //-----------Methoden von Augmos-----------

    private void analyze(){
        long usedTime = System.nanoTime() - startTime;
        Editor editor = editorView.getEditor();
        ConversionState[] supportedStates = editor.getSupportedTargetConversionStates(null);
        if (supportedStates.length > 0)
            editor.convert(editor.getRootBlock(), supportedStates[0]);
        try {
            //Hier zu fb pushen bzw ausgeben
            Analyzer analyzer = new AnalyzerImpl();
            System.out.println(editor.export_(editor.getRootBlock(), MimeType.MATHML));
            ExerciseSolution solution = analyzer.analyze(currentExcercise.getKey(), currentExcercise.getValue(), editor.export_(editor.getRootBlock(), MimeType.JIIX), editor.export_(editor.getRootBlock(), MimeType.MATHML));
            createExcerciseSummaryDialog(solution, usedTime / 1000000000).show();
            studentRef.collection("solutions").document().set(solution);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void nextExcercise(){
        currentExcercise = Session.getNextExcercise();
        setTitle(currentExcercise.getValue().getContent());
        startTime = System.nanoTime();
    }


    private void getExcercises(final Boolean startExcercise){
        exercisesCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Log.d(TAG, document.getId() + " => " + document.getData());
                        Session.addExcercise(document.getId(), document.toObject(Exercise.class));
                        AnzahlExcercises++;
                    }
                    if(startExcercise){
                        nextExcercise();
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private Dialog createExcerciseSummaryDialog(ExerciseSolution solution, long timeSpend){
        String title;
        String message;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(solution.getCorrect()){
            title = "Richtig";
            message = "Du hast diese Aufgabe richtig innerhalb von " + timeSpend + " Sekunden gelöst";
            builder.setMessage(message)
                    .setTitle(title)
                    .setPositiveButton("Nächste Aufgabe", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editorView.getEditor().clear();
                            nextExcercise();
                            studentRef.update("progress", (++ExcercisesDone / AnzahlExcercises) * 100);
                            Log.d(TAG, "Progress: " + (ExcercisesDone / AnzahlExcercises) * 100);
                        }
                    })
                    .setNegativeButton("Nochmal", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    editorView.getEditor().clear();
                    startTime = System.nanoTime();
                    studentRef.update("progress", (ExcercisesDone / AnzahlExcercises) * 100);
                    Log.d(TAG, "Progress: " + (ExcercisesDone / AnzahlExcercises) * 100);
                }
            });
        }else{
            title = "Falsch";
            message = "Du hast diese Aufgabe falsch innerhalb von " + timeSpend + " Sekunden gelöst";
            builder.setMessage(message)
                    .setTitle(title)
                    .setNegativeButton("Nächste Aufgabe", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editorView.getEditor().clear();
                            nextExcercise();
                            studentRef.update("progress", (++ExcercisesDone / AnzahlExcercises) * 100);
                            Log.d(TAG, "Progress: " + (ExcercisesDone / AnzahlExcercises) * 100);
                        }
                    })
                    .setPositiveButton("Nochmal", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            editorView.getEditor().clear();
                            startTime = System.nanoTime();
                            studentRef.update("progress", (ExcercisesDone / AnzahlExcercises) * 100);
                            Log.d(TAG, "Progress: " + (ExcercisesDone / AnzahlExcercises) * 100);
                        }
                    });
        }

        return builder.create();
    }
}
