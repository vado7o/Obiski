package com.example.obiski.words;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.obiski.R;
import com.example.obiski.articles.ArticlesPage;
import com.example.obiski.media.VideoPlayer;
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
import java.util.List;

public class WordsCycle extends AppCompatActivity {
    private ArrayList<String> articles = new ArrayList<>();   // это список отмеченных тем, для дальнейшего изучения (передаётся из Активити ArticlesPage)
    private final List<Word> words = new ArrayList<>();     // это список слов, которые будут прочитаны из Firebase Database (в соответствии с выбранным списком тем)
    private List<Word> cutWords = new ArrayList<>();  // это список для обрезания количества до размера "RECYCLE_SIZE" для показа
    private List<Word> soundList = new ArrayList<>(); // это список для оставшихся слов к озвучке
    private WordAdapter wordAdapter;
    private static final int RECYCLE_SIZE = 8;   // число слов на экране
    private Context context;

    public WordsCycle() {
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_words);

        context = this;

        // Принимаем лист выбранных тем из предыдущего активити
        articles = getIntent().getStringArrayListExtra("stuff");

        // Инициализация Firebase
        FirebaseApp.initializeApp(WordsCycle.this);
        FirebaseStorage storage = FirebaseStorage.getInstance();

        // создаём список задач для ожидания полной прогрузки референсов файлов из выбранных каталогов
        List<Task<Void>> tasksRefs = new ArrayList<>();

        // для каждой выбранной пользователем тем, начинаем формировать слова Words со ссылками на изображение и звук.
        for (String article : articles) {
            StorageReference imageReference = storage.getReference("themes/" + article);
            StorageReference soundReference = storage.getReference("sounds/" + article);
            Task<Void> task = getRefs(imageReference, soundReference);
            tasksRefs.add(task);
        }

        // создаём список задач для вытягивания Uri картинок из референсов слов в полученной коллекции
        // при этом данная процедура начнётся ТОЛЬКО когда коллекция words будет ПОЛНОСТЬЮ сформирована
        // это достигается путём добавления Tasks.whenAllSuccess
        List<Task<Void>> tasksPicUris = new ArrayList<>();
        Tasks.whenAllSuccess(tasksRefs)
                .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
            @Override
            public void onSuccess(List<Object> objects) {
                for (Word word : words) {
                    Task<Void> task = getPicUri(word);
                    tasksPicUris.add(task);
                }
                Tasks.whenAllSuccess(tasksPicUris)
                        .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                    @Override
                    public void onSuccess(List<Object> objects) {
                        fillRecycler();        // и, наконец, когда все ссылки на изображения добавлены, начинаем загрузку коллекции в RecyclerView !!!!
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(WordsCycle.this, "Ошибка чтения слов 1!",
                                Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WordsCycle.this, "Ошибка чтения слов 2!",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
    }


    // метод вытягивания reference файлов из выбранных каталогов-тем для изучения
    public Task<Void> getRefs(StorageReference imageReference, StorageReference soundReference) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        imageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                // для каждого каталога вытягиваем reference файлов
                listResult.getItems().forEach(item ->
                {
                    String name = item.getName();
                    if (!name.equals("title.jpg"))      // пропускаем намеренно title.jpg, поскольку в каждом каталоге имеется такой файл и это всего лишь обёртка заголовка темы
                        words.add(new Word(name.substring(0, name.length() - 4),   // добавляем в коллекцию слово, его название путём "отрезания" расширения файла от его имени
                                item,     // также референс на его изображение
                                soundReference.child(name
                                        .substring(0, name.length() - 4) + ".mp3")));  // и, наконец добавляем референс на звук
                });
                taskCompletionSource.setResult(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WordsCycle.this, "Ошибка чтения слов 3!",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        return taskCompletionSource.getTask();
    }


    // метод для вытягивания из референсов Uri картинок для каждого слова в коллекции words
    public Task<Void> getPicUri(Word word) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();
        word.getImageReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri imgUri) {
                word.setImgUri(imgUri);
                taskCompletionSource.setResult(null);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(WordsCycle.this, "Ошибка чтения слов 4!",
                        Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        });
        return taskCompletionSource.getTask();
    }


    // метод передачи коллекции слов и формирования списка через инструмент RecyclerView
    public void fillRecycler() {

        if (words.isEmpty()) {  // если слов не осталось, то проигрываем победное видео
            Intent activity = new Intent(getApplicationContext(), VideoPlayer.class);
            startActivity(activity);
            finish();

        } else {
            Collections.shuffle(words);   // перемешиваем слова
            if (words.size() >= RECYCLE_SIZE) {  // если слов больше чем задано для показа, то начинаем обрезать до нужного количества "первую партию" для показа
                cutWords.clear();
                for (int i = 0; i < RECYCLE_SIZE; i++) {
                    cutWords.add(words.remove(0));
                }

            } else {                // если же слов в коллекции уже итак меньше или как раз столько сколько нужно для показа, то просто составляем полностью из них список для показа
                cutWords = new ArrayList<>(words);
                words.clear();
            }

            soundList = new ArrayList<>(cutWords);           // копируем список для озвучки (список озвучки отдельный, тк список изображений на экране должен оставаться стабильным)
            RecyclerView recyclerView = findViewById(R.id.recyclertheme);
            recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
            wordAdapter = new WordAdapter(context, cutWords);
            recyclerView.setAdapter(wordAdapter);
            Button again = findViewById(R.id.again);
            new WordsPage(soundList, context, wordAdapter, again, WordsCycle.this).start();   // запускаем Активити с первой партией слов
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, ArticlesPage.class);
        startActivity(intent);
        finish();
    }
}
