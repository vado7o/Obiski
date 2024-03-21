package com.example.obiski.articles;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.obiski.media.SoundPlayer;
import com.example.obiski.R;
import com.example.obiski.words.WordsCycle;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Активити с выбором категории слов (выбор тем для изучения)
public class ArticlesPage extends AppCompatActivity {
    private Context context;
    private Button next;
    private final List<Article> articles = new ArrayList<>();     // это список всех тем, которые будут прочитаны из сети (названия тем и их картинки)
    private ArticleAdapter articleAdapter;
    private ArrayList<String> themes = new ArrayList<>();   // это список отмеченных тем, для дальнейшего изучения

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_themes);

        context = this;

        // Инициализация Firebase
        FirebaseApp.initializeApp(ArticlesPage.this);
        FirebaseStorage storage = FirebaseStorage.getInstance();

        RecyclerView recyclerView = findViewById(R.id.recyclertheme);

        next = findViewById(R.id.next);
        next.setEnabled(false);             // по умолчанию кнопку "Начать" делаем НЕдоступной

        // Получаем референс в базе данных Firebase к директориям со словами
        StorageReference reference = storage.getReference("themes");

        // Получаем весь список тем из названий каталогов внутри папки themes в сети (используя ранее полученный референс БД)
        reference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                // создаём коллекцию задач для ожидания полной прогрузки ВСЕХ названий тем
                List<Task<Void>> tasks = new ArrayList<>();
                // для каждой директории внутри папки themes в БД создаём задачу по вытягиванию названия темы и её изображения (при помощи метода addArticle)
                for (StorageReference reference : listResult.getPrefixes()) {
                    Task<Void> task = addArticle(reference);
                    tasks.add(task);
                }
                // когда все задачи Task будут отработаны передаём все полученные данные в адаптер RecyclerView
                Tasks.whenAllSuccess(tasks)
                        .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> objects) {
                        articles.sort(Comparator.comparing(Article::getName));     // сортируем темы по названию
                        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
                        articleAdapter = new ArticleAdapter(context, articles);
                        recyclerView.setAdapter(articleAdapter);

                        new SoundPlayer(null).play(new ArrayList<>(        // проигрываем фразу "Выберите тему для изучения"
                                Collections.singletonList(storage.getReference("sounds")
                                        .child("System")
                                                .child("choosetheme.mp3"))),
                                getApplicationContext(), null, null);

                        // Устанавливаем слушатель нажатия на изображения тем для изменения состояния кнопки "Начать"
                        articleAdapter.setOnItemsCheckStateListener(checkedItemCounter -> {
                            if (checkedItemCounter.size() == 0) {
                                next.setEnabled(false);
                                themes = checkedItemCounter;
                            } else {
                                next.setEnabled(true);
                                themes = checkedItemCounter;
                            }
                        });

                        // По нажатию на кнопке "Начать" переходим в Активити для изучения слов.
                        next.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent activity = new Intent(getApplicationContext(),
                                        WordsCycle.class);
                                activity.putExtra("stuff", themes);         // передаём в следующее Активити коллекцию выбранных тем
                                startActivity(activity);
                                finish();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ArticlesPage.this, "Ошибка чтения тем 1!",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArticlesPage.this, "Ошибка чтения тем 2!",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }


    // Метод для добавления в коллекцию "articles" названия тем слов и ссылок на их изображения
    private Task<Void> addArticle(StorageReference reference) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        reference.child("title.jpg").getDownloadUrl()
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                articles.add(new Article(reference.getName(), uri));
                taskCompletionSource.setResult(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ArticlesPage.this, "Ошибка чтения тем 3!",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        return taskCompletionSource.getTask();
    }
}