package com.example.obiski.words;

import android.net.Uri;

import com.google.firebase.storage.StorageReference;

// Класс для создания элемента коллекции и дальнейшей загрузки в RecyclerView
public class Word {

    private final String name;
    private Uri uri;
    private final StorageReference imageReference;
    private final StorageReference soundReference;


    public Word(String name, StorageReference imageReference, StorageReference soundReference) {
        this.name = name;
        this.imageReference = imageReference;
        this.soundReference = soundReference;
    }

    public String getName() { return name; }
    public Uri getImgUri() { return uri; }
    public StorageReference getImageReference() {return imageReference;}
    public StorageReference getSoundReference() { return soundReference; }
    public void setImgUri(Uri uri) {this.uri = uri;}
}
