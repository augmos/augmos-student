// Copyright AUGMOS. All rights reserved.

package com.augmos.iink.prototype;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
  private String TAG = "MainActivity";
  private FirebaseFirestore fb;
  private ArrayList<String> themen = new ArrayList<>();
  private ArrayList<String> themenID = new ArrayList<>();

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    findViewById(R.id.button_main_chooseExcercise).setOnClickListener(this);
    findViewById(R.id.button_main_teacherexcercises).setOnClickListener(this);
    fb = FirebaseFirestore.getInstance();
    fb.collection("exercises").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
      @Override
      public void onComplete(Task<QuerySnapshot> task) {
        if (task.isSuccessful()) {
          for (QueryDocumentSnapshot document : task.getResult()) {
            Log.d(TAG, document.getId() + " => " + document.getData());
            themen.add(document.get("name").toString());
            themenID.add(document.getId());
          }
        } else {
          Log.d(TAG, "Error getting documents: ", task.getException());
        }
      }
    });
    //update Lehrer Aufgabengebiet
    fb.collection("teachers").document(Session.getTeacherID()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
      @Override
      public void onSuccess(DocumentSnapshot documentSnapshot) {
        Session.setAufgabengebiet(documentSnapshot.get("currentExerciseField").toString());
      }
    });
  }

  @Override
  public void onClick(View v) {
    switch (v.getId()){
      case R.id.button_main_teacherexcercises:
        Intent intent = new Intent(this, EditorFragmentActivity.class);
        startActivity(intent);
        break;
      case R.id.button_main_chooseExcercise:
        onCreateDialog(null).show();
        break;
    }

  }


  public Dialog onCreateDialog(Bundle savedInstanceState) {
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle("Themengebiet wählen")
            .setItems(themen.toArray(new String[]{}), new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                // The 'which' argument contains the index position
                // of the selected item
                Session.setAufgabengebiet(themenID.get(which));
                Intent intent2 = new Intent(getApplicationContext(), EditorFragmentActivity.class);
                startActivity(intent2);
              }
            });
    return builder.create();
  }

}
