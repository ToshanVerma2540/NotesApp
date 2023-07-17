package com.example.notesapp.Models;

public class NotesModel {
  public   String title = "";
    public String content = "";
  public  String ProductId ="";
    public NotesModel(){

    }
    public NotesModel(String title, String content, String productId) {
        this.title = title;
        this.content = content;
        this.ProductId = productId;
    }
 }
