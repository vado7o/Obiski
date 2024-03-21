package com.example.obiski.articles;

import android.net.Uri;

// Класс для создания элемента коллекции и дальнейшей загрузки в RecyclerView
public class Article {
    private boolean isSelected = false;
    private final String name;
    private final Uri uri;

    public Article(String name, Uri uri) {
        this.name = name;
        this.uri = uri;
    }
    public void setSelected(boolean selected) {
        isSelected = selected;
    }
    public boolean isSelected() {
        return isSelected;
    }

    public String getName() { return name; }
    public Uri getUri() { return uri; }
}
