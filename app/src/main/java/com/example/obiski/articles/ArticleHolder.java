package com.example.obiski.articles;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.obiski.R;

public class ArticleHolder extends RecyclerView.ViewHolder {

    ImageView articleImage;
    TextView articleName;
    ImageView checkIcon;
    ImageView fade;
    public ArticleHolder (@NonNull View artcileView) {
        super(artcileView);
        articleName = artcileView.findViewById(R.id.articlename);
        articleImage = artcileView.findViewById(R.id.articleimage);
        checkIcon = artcileView.findViewById(R.id.check_icon);
        fade = artcileView.findViewById(R.id.fade);
    }
}
