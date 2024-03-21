package com.example.obiski.media;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.obiski.R;
import com.example.obiski.articles.ArticlesPage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.Random;

public class VideoPlayer extends AppCompatActivity {

    private final StorageReference reference =
            FirebaseStorage.getInstance().getReference("video/win");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        VideoView videoView = findViewById(R.id.videoView);
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(videoView);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);

        // Инициализация Firebase
        FirebaseApp.initializeApp(VideoPlayer.this);

        // начинаем вытягивать ссылки на видеофайлы
        reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                Random random = new Random();
                StorageReference referenceUri = listResult.getItems().
                        get(random.nextInt(listResult.getItems().size()));  // выбираем наугад референс видеоролика из папки
                referenceUri.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        videoView.setVideoURI(uri);
                        videoView.start();
                        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                            // по окончании проигрывания возвращаемся в список выбора тем
                            @Override
                            public void onCompletion(MediaPlayer mp) {
                                mp.release();
                                Intent activity = new Intent(getApplicationContext(),
                                        ArticlesPage.class);
                                startActivity(activity);
                                finish();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(VideoPlayer.this, "Ошибка проигрывания видео 1!",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(VideoPlayer.this, "Ошибка проигрывания видео 2!",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }

    // по нажатию "назад"
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ArticlesPage.class);
        startActivity(intent);
        finish();
    }
}
