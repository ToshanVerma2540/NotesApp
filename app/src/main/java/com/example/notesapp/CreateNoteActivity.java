package com.example.notesapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import com.example.notesapp.Models.NotesModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;

public class CreateNoteActivity extends AppCompatActivity {

    FloatingActionButton saveNotes;
    EditText titleText;
    EditText contentText;
   FirebaseFirestore db;
    NotesModel notesModel;
    Button updateButton, cancelButton;
    String notesId = "";
    int position = 0;
    Constants constants;
    ProgressDialog progressDialog;
    int count = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        ActionBar actionBar = getSupportActionBar();
        titleText = findViewById(R.id.title_text);
        contentText = findViewById(R.id.content_text);
        saveNotes = findViewById(R.id.save_notes);
        db = FirebaseFirestore.getInstance();
        constants = new Constants();
        updateButton = findViewById(R.id.update_button);
        cancelButton = findViewById(R.id.cancel_button);
        progressDialog = new ProgressDialog(this);
        notesModel =  new NotesModel("","","");
        //getting Extra
        int pos = getIntent().getIntExtra("Data",-1);
        //if pos is > 0 that means i have come here with data and setting that to content and title text
        //hiding the save button and disabling the edittext
        if(pos>=0){
            actionBar.setTitle("");
            position= pos;
            count = 1;
            saveNotes.setVisibility(View.GONE);
            notesModel = NotesActivity.notes.get(pos);
            contentText.setText(notesModel.content);
            titleText.setText(notesModel.title);
            titleText.setEnabled(false);
            contentText.setEnabled(false);
        }else{
            actionBar.setTitle("ADD YOUR NOTE");
        }

        //saving the new data to firebase and also adding to notes list and upding to adapter.
        saveNotes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //before adding checking if notes have both content and title or not
                if(validate()== false) {
                    Toast.makeText(getApplicationContext(),"Empty Note Can't be saved",Toast.LENGTH_SHORT).show();
                }else{
                        add();
                        NotesActivity.notes.add(new NotesModel(titleText.getText().toString().trim(),
                                contentText.getText().toString().trim(),notesId));
                        NotesActivity.adapter.notifyDataSetChanged();
                        finish();
                    }
                }
        });

        //if user want to make changes to existing note then upding that to firebase as well as notes list and adapter.
        //hiding the buttons and making editexts again disabled.
        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //before updating checking if notes have both content and title or not
                if(validate()) {
                    update(notesModel.ProductId);
                    NotesActivity.notes.get(pos).title = notesModel.title;
                    NotesActivity.notes.get(pos).content = notesModel.content;
                    NotesActivity.adapter.notifyDataSetChanged();
                }
            }
        });

        //canceling the changes made by user to existing note and
        //hiding all the buttons and making edittexts disabled
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                contentText.setText(notesModel.content);
                titleText.setText(notesModel.title);
                contentText.setEnabled(false);
                titleText.setEnabled(false);
            }
        });

        //setting the back arrow to actionbar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
      if(count<1&&validate()){
          showSaveConfirmationDialog();
      }else{
          onBackPressed();
      }
        return true;
    }

    public boolean validate(){
        if(titleText.getText().toString().trim().length() >0 ||contentText.getText().toString().trim().length() >0){
            //Toast.makeText(getApplicationContext(),"Content Required",Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
    public void add(){
        showProgressDialog();
        count = 1;
        notesId =   db.collection(constants.NOTES).document(FirebaseAuth.getInstance().getUid()).
                collection(constants.MY_NOTES).document().getId();
        HashMap<String,Object> data = new HashMap<>();
        data.put(constants.TITLE,titleText.getText().toString().trim());
        data.put(constants.CONTENT,contentText.getText().toString().trim());
        data.put(constants.NOTES_ID,notesId);


                db.collection(constants.NOTES).document(FirebaseAuth.getInstance().getUid()).
                        collection(constants.MY_NOTES).document(notesId).set(data,SetOptions.merge()).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressDialog();
                   Toast.makeText(getApplicationContext(),"Added",Toast.LENGTH_LONG).show();
                    }})
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressDialog();
                        Toast.makeText(getApplicationContext(),"Failed",Toast.LENGTH_SHORT).show();
                    }});
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflating menu only if it came by clicking list item
        if(notesModel.content.length()>0) {
            getMenuInflater().inflate(R.menu.delete_edit_menu, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(notesModel.content.length()>0) {
            //making update and cancel buttons visible
            //enbling the edittexts
            if (item.getItemId() == R.id.edit_note) {
                titleText.setEnabled(true);
                contentText.setEnabled(true);
                cancelButton.setVisibility(View.VISIBLE);
                updateButton.setVisibility(View.VISIBLE);
            }
        }
        if(item.getItemId() == R.id.delete_note){
            if(notesModel.ProductId.length()>0){
                //deleting the particular note
                //and also removing from list and updating adapter
               showDeleteConfirmationDialog();

            }
        }
        return super.onOptionsItemSelected(item);
    }
    public void update(String noteId){
        //updating data to firebase as well as in list and
        //hiding the buttons and disabling the edittext
       updateInFirebase(NotesActivity.notes.get(position).ProductId);
        updateButton.setVisibility(View.GONE);
        cancelButton.setVisibility(View.GONE);
        notesModel.content = contentText.getText().toString().trim();
        notesModel.title = titleText.getText().toString().trim();
        contentText.setEnabled(false);
        titleText.setEnabled(false);
    }

    //function to update particular note from database
    public void updateInFirebase(String noteId){
        showProgressDialog();
            HashMap<String, Object> data = new HashMap<>();
            data.put(constants.TITLE, titleText.getText().toString().trim());
            data.put(constants.CONTENT, contentText.getText().toString().trim());

            // Replace 'db' with your Firestore instance
            db.collection(constants.NOTES)
                    .document(FirebaseAuth.getInstance().getUid())
                    .collection(constants.MY_NOTES)
                    .document(noteId)
                    .update(data)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            Toast.makeText(getApplicationContext(), "Updated", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            Toast.makeText(getApplicationContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                        }
                    });
    }


    //fuction to delete particular note from database
    public void deleteFromFirebase(String noteId) {
        showProgressDialog();
        // Replace 'db' with your Firestore instance
        db.collection(constants.NOTES)
                .document(FirebaseAuth.getInstance().getUid())
                .collection(constants.MY_NOTES)
                .document(noteId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressDialog();
                        Toast.makeText(getApplicationContext(), "Note deleted", Toast.LENGTH_LONG).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        hideProgressDialog();
                        Toast.makeText(getApplicationContext(), "Failed to delete note", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void showProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("please wait..."); // Setting Message
        //progressDialog.setTitle("ProgressDialog"); // Setting Title
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
        progressDialog.show();
    }
    public void hideProgressDialog() {
        if (!isFinishing() && progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private AlertDialog deleteConfirmationDialog;
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Perform delete action here
                        deleteFromFirebase(notesModel.ProductId);
                        NotesActivity.notes.remove(position);
                        NotesActivity.adapter.notifyDataSetChanged();
                        finish();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Perform cancel action here
                        if (deleteConfirmationDialog != null && deleteConfirmationDialog.isShowing()) {
                            deleteConfirmationDialog.dismiss();
                            // Handle back button press when the dialog is showing

                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        // Handle back button press

                    }
                });

        deleteConfirmationDialog = builder.create();
        deleteConfirmationDialog.show();
    }
    private void showSaveConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Your Note is Not Saved")
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Perform delete action here
                        add();
                    }
                })
                .setNegativeButton("Don't Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                       count = 1;
                       onBackPressed();
                    }
                })
                .show();
    }


    @Override
    public void onBackPressed() {
        if (deleteConfirmationDialog != null && deleteConfirmationDialog.isShowing()) {
            deleteConfirmationDialog.dismiss();
            // Handle back button press when the dialog is showing
        } if(count<1&&validate()){
           showSaveConfirmationDialog();
        } else{
            super.onBackPressed();
        }
    }
}