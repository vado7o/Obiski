package com.example.obiski.words;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.example.obiski.media.SoundPlayer;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class WordsPage {    // Класс для проработки одного листа слов (размером RECYCLE_SIZE)
    private final List<Word> soundList;
    private final Context context;
    private final WordAdapter wordAdapter;
    private final StorageReference referenceSound = FirebaseStorage.getInstance()     // Референс к папке звуков
            .getReference("sounds");
    private final StorageReference referenceRight = referenceSound                  // Референс к папке звуков "ПРАВИЛЬНО"
            .child("dima").child("right");
    private final StorageReference referenceWrong = referenceSound                  // Референс к папке звуков "НЕПРАВИЛЬНО"
            .child("dima").child("wrong");
    private static final int RIGHT_SIZE = 7;    // число файлов с озвучкой "ПРАВИЛЬНО!"
    private static final int WRONG_SIZE = 4;    // число файлов с озвучкой "НЕПРАВИЛЬНО!"
    private final Random rnd = new Random();
    private String name; // переменная, хранящая название слова
    private int count;   // зафиксированное значение random для выбора имени слова, картинки и т.д.
    private final Button again;
    private final WordsCycle wordsCycle;

    public WordsPage(List<Word> soundList, Context context, WordAdapter wordAdapter,
                     Button again, WordsCycle wordsCycle) {
        this.soundList = soundList;
        this.context = context;
        this.wordAdapter = wordAdapter;
        this.again = again;
        this.wordsCycle = wordsCycle;
    }

    public void start() {
        // выбираем случайное слово из имеющихся
        count = rnd.nextInt(soundList.size());
        name = soundList.get(count).getName();

        // проигрываем слово в начале показа карточек
        wordAdapter.setDisabled(true);
        List<StorageReference> list = new ArrayList<>(
                Arrays.asList(soundList.get(count).getSoundReference()));
        new SoundPlayer(this).play(list, context, wordAdapter, name);
    }

    public void playRound() {
        // Ставим слушателя интерфейса для wordAdapter для отслеживания выбора пользователя
        wordAdapter.setOnItemsCheckStateListener((checkedWord, anim, animR, fade, checkIcon,
                                                  wrongIcon, fadeT, wordImage, adapter) -> {

            // Если слово выбрано правильно
            if (Objects.equals(checkedWord, name)) {
                soundList.remove(count);  // удаляем угаданный звук из списка

                // создаём список звуков ПРАВИЛЬНО для последующего его проигрывания
                StorageReference reference1 = referenceSound
                        .child("System").child("right-answer.mp3");
                StorageReference reference2 = referenceRight
                        .child(rnd.nextInt(RIGHT_SIZE) + ".mp3");
                List<StorageReference> list2 = new ArrayList<>(Arrays.asList(reference1, reference2));

                if (!soundList.isEmpty()) {  // если список не пуст то добавляем ещё следующее слово для проигрывания
                    count = rnd.nextInt(soundList.size()); // выбираем random для выбора слова и его звука
                    name = soundList.get(count).getName(); // выбираем слово из оставшихся
                    StorageReference reference3 = soundList.get(count).getSoundReference();
                    list2.add(reference3);  // добавляем референс слова в список проигрывания

                    new SoundPlayer(this).play(list2, context, wordAdapter, name);
                }

                else new SoundPlayer(true, wordsCycle, this)  // если список звуков теперь пуст, то передаём ещё переменную emptyList = true
                        .play(list2, context, wordAdapter, null);

                // начинаем анимацию картинки
                fadeT.setVisibility(View.VISIBLE);
                checkIcon.setVisibility(View.VISIBLE);
                animR.start();
                wordImage.setEnabled(false);

            } else {      // Если слово выбрано НЕправильно
                // создаём список звуков НЕПРАВИЛЬНО и проигрываем его
                StorageReference reference1 = referenceSound
                        .child("System").child("wrong-answer.mp3");
                StorageReference reference2 = referenceWrong
                        .child(rnd.nextInt(WRONG_SIZE) + ".mp3");
                List<StorageReference> list2 = new ArrayList<>(Arrays.asList(reference1, reference2));

                new SoundPlayer(this).play(list2, context, wordAdapter, name);

                // начинаем анимацию картинки
                fade.setVisibility(View.VISIBLE);
                wrongIcon.setVisibility(View.VISIBLE);
                anim.start();
            }
        });

        // Cлушатель кнопки "Ещё раз". Повторяем проигрывание слова.
        again.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                List<StorageReference> list = new ArrayList<>(
                        Arrays.asList(soundList.get(count).getSoundReference()));
                new SoundPlayer(WordsPage.this).play(list, context, wordAdapter, name);
            }
        });
    }
}
