package com.example.notesapp;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.example.notesapp.Models.NotesAdapter;
import com.example.notesapp.Models.NotesModel;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class NotesActivity extends AppCompatActivity {
    //change the color of the action bar
    // make multiple delete option
    //make random colors to the items of recyclerview
    //make dialogs for delete and save and don't save options
    FloatingActionButton createNoteFab;
    RecyclerView recyclerView;
    static ArrayList<NotesModel> notes;
    static NotesAdapter adapter;
    int data = -1;
    Constants constants;
    boolean isContexualMode = false;
    HashSet<Item> selected;
    ActionBar actionBar;
    Button cancelButton,deleteButton;
    ProgressDialog progressDialog;
    FirebaseFirestore db;
    private AlertDialog deleteConfirmationDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notes);
         actionBar = getSupportActionBar();
        actionBar.setTitle("YOUR NOTES");
        createNoteFab = findViewById(R.id.create_note_fab);
        recyclerView = findViewById(R.id.rv);
        constants = new Constants();
        notes = new ArrayList<>();
        cancelButton = findViewById(R.id.cancel_bt);
        deleteButton = findViewById(R.id.delete_button);
        selected = new HashSet<>();
        db = FirebaseFirestore.getInstance();
        progressDialog = new ProgressDialog(this);
       createNoteFab.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View view) {

               //going to createnotes activity with extra data so that i can use there.
               Intent intent = new Intent(NotesActivity.this,CreateNoteActivity.class);
               intent.putExtra("Data",-1);
               startActivity(intent);
           }
       });

        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
         adapter = new NotesAdapter(getApplicationContext(), notes, new NotesAdapter.OnItemClickListener() {
             @Override
             public void onItemClick(int position,View view) {
                 if(isContexualMode == true){
                     startSelection(adapter.getAdapterPosition(position), view);
                 }else {
                     //going to createnotes activity with extra position so that i can get data using that.
                     Intent intent = new Intent(NotesActivity.this, CreateNoteActivity.class);
                     data = position;
                     intent.putExtra("Data", data);
                     startActivity(intent);
                 }
             }
         }, new NotesAdapter.OnItemLongClickListener() {
             @Override
             public void onItemLongClick(int position, View view) {
                 startSelection(adapter.getAdapterPosition(position), view);
             }
         });
        recyclerView.setAdapter(adapter);
        getNotes();
        adapter.notifyDataSetChanged();


        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                unselect();
            }
        });
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDeleteConfirmationDialog();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
       getMenuInflater().inflate(R.menu.log_out,menu);
       return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                //logging out from firebase and going to login activity.
               FirebaseAuth.getInstance().signOut();
               Intent intent = new Intent(NotesActivity.this,LoginActivity.class);
               startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    //getting all the notes from the firebase firestore;
    public void getNotes() {

        CollectionReference notesCollectionRef = db.collection(constants.NOTES)
                .document(FirebaseAuth.getInstance().getUid())
                .collection(constants.MY_NOTES);
        db.enableNetwork();
        notesCollectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot querySnapshot, FirebaseFirestoreException e) {
                if (e != null) {
                    // Handle error
                    return;
                }

                notes.clear();
                for (DocumentSnapshot documentSnapshot : querySnapshot) {
                    if (documentSnapshot.exists()) {
                        // Document exists, retrieve the data
                        Map<String, Object> data = documentSnapshot.getData();
                        NotesModel model = new NotesModel();
                        model.title = (String) data.get(constants.TITLE);
                        model.content = (String) data.get(constants.CONTENT);
                        model.ProductId = (String) data.get(constants.NOTES_ID);
                        notes.add(model);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });
    }



    //selecting and unselecting items
    public void startSelection(int position,View view) {
        if (selected.isEmpty()) {
            selected.add(new Item(position,view));
            view.setBackgroundColor(getResources().getColor(R.color.primary_color_50));
            if (actionBar != null) {
                actionBar.hide();
            }
            createNoteFab.setVisibility(View.GONE);
            deleteButton.setVisibility(View.VISIBLE);
            cancelButton.setVisibility(View.VISIBLE);
            isContexualMode = true;
        } else if (!selected.isEmpty() && selected.contains(new Item(position,view))) {
            Item item = new Item(position,view);
            selected.remove(item);
            view.setBackground(getDrawable(R.drawable.outline_bg));
            if (selected.size() == 0) {
                //toolbar.getMenu().clear();
                deleteButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                    actionBar.show();
                createNoteFab.setVisibility(View.VISIBLE);
                isContexualMode = false;
            }
        }else {
            selected.add(new Item(position,view));
            view.setBackgroundColor(getResources().getColor(R.color.primary_color_50));
            isContexualMode = true;
        }
    }

    public void unselect(){
            List<Item> dataList = new ArrayList<>(selected);
            for(int i =0;i<dataList.size();i++){
                dataList.get(i).view.setBackground(getDrawable(R.drawable.outline_bg));
            }
            cancelButton.setVisibility(View.GONE);
            deleteButton.setVisibility(View.GONE);
            createNoteFab.setVisibility(View.VISIBLE);
            isContexualMode = false;
            actionBar.show();
            selected.clear();
    }

    public class Item {
        int position;
        View view;

        public Item(int position, View view) {
            this.position = position;
            this.view = view;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            Item otherItem = (Item) other;
            return position == otherItem.position && Objects.equals(view, otherItem.view);
        }

        @Override
        public int hashCode() {
            return Objects.hash(position, view);
        }
    }

    public void delete(){
        showProgressDialog();
        List<Item> dataList = new ArrayList<>(selected);
        for(int i =0;i<dataList.size();i++){
           deleteFromFirebase(notes.get(dataList.get(i).position).ProductId);
        }
        hideProgressDialog();
        Toast.makeText(getApplicationContext(), "Notes deleted", Toast.LENGTH_SHORT).show();
        cancelButton.setVisibility(View.GONE);
        deleteButton.setVisibility(View.GONE);
        createNoteFab.setVisibility(View.VISIBLE);
        isContexualMode = false;
        actionBar.show();
        selected.clear();
    }
    //fuction to delete particular note from database
    public void deleteFromFirebase(String noteId) {
       // showProgressDialog();
        // Replace 'db' with your Firestore instance
        db.collection(constants.NOTES)
                .document(FirebaseAuth.getInstance().getUid())
                .collection(constants.MY_NOTES)
                .document(noteId)
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                      //  hideProgressDialog();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                       // hideProgressDialog();
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


    //showing the confirmation dialoge box
    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Item")
                .setMessage("Are you sure you want to delete this item?")
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Perform delete action here
                        delete();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // Perform cancel action here
                        if (deleteConfirmationDialog != null && deleteConfirmationDialog.isShowing()) {
                            deleteConfirmationDialog.dismiss();
                            // Handle back button press when the dialog is showing
                            unselect();
                        }
                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        // Handle back button press
                        unselect();
                    }
                });

        deleteConfirmationDialog = builder.create();
        deleteConfirmationDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (deleteConfirmationDialog != null && deleteConfirmationDialog.isShowing()) {
            deleteConfirmationDialog.dismiss();
            // Handle back button press when the dialog is showing
            unselect();
        }else if(selected.size()>0){
           unselect();
        }  else{
            super.onBackPressed();
        }
    }

}