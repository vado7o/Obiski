package com.example.obiski.words;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.obiski.R;

public class WordHolder extends RecyclerView.ViewHolder {

    ImageView wordImage;
    TextView wordName;
    ImageView checkIcon;
    ImageView wrongIcon;
    ImageView fade;
    ImageView fadeT;

    public WordHolder(@NonNull View wordView) {
        super(wordView);
        wordName = wordView.findViewById(R.id.wordname);
        wordImage = wordView.findViewById(R.id.wordimage);
        checkIcon = wordView.findViewById(R.id.check_icon);
        wrongIcon = wordView.findViewById(R.id.wrong_icon);
        fade = wordView.findViewById(R.id.fade);
        fadeT = wordView.findViewById(R.id.fadeT);
    }
}
