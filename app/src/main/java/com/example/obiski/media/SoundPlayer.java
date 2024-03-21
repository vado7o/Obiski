package com.example.obiski.media;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.obiski.words.WordAdapter;
import com.example.obiski.words.WordsCycle;
import com.example.obiski.words.WordsPage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.List;

public class SoundPlayer {
    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private boolean emptyList;   // bool для определения, что список пуст
    private WordsCycle wordsCycle;
    private WordsPage wordsPage;

    public SoundPlayer(boolean emptyList, WordsCycle wordsCycle, WordsPage wordsPage) {
        this.emptyList = emptyList;
        this.wordsCycle = wordsCycle;
        this.wordsPage = wordsPage;
    }

    public SoundPlayer(WordsPage wordsPage) {
        this.wordsPage = wordsPage;
    }


    // метод проигрывания списка слов
    public void play(List<StorageReference> list, Context context, WordAdapter adapter, String name) {
        list.get(0).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri soundUri) {

                try {
                    mediaPlayer.reset();
                    mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                    mediaPlayer.setDataSource(String.valueOf(soundUri));
                    mediaPlayer.prepare();
                    mediaPlayer.start();

                } catch (IOException e) {
                    Toast.makeText(context, "Ошибка проигрывания 1!", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                list.remove(0);
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        if (wordsPage != null) {

                            // по завершению проигрыша одного звука и если список звуков всё ещё не пустой, то проигрываем
                            // следующий звук из списка
                            if (!list.isEmpty()) play(list, context, adapter, name);

                            else {
                                adapter.setDisabled(false);
                                mp.release();
                                mediaPlayer.release();

                                if (emptyList)
                                    wordsCycle.fillRecycler();  // если СЛОВА для ОДНОГО показа recyclerView закончились, то заполняем новыми словами

                                else {
                                    wordsPage.playRound(); // иначе просто продолжаем игру с текущей коллекцией слов

                                    if (name != null)     // также показываем текст со словом для Родителей
                                        Toast.makeText(context, name, Toast.LENGTH_LONG).show();
                                }
                            }
                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, "Ошибка проигрывания 2!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }
}
