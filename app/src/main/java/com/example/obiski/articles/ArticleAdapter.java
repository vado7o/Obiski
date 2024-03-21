package com.example.obiski.articles;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.obiski.R;

import java.util.ArrayList;
import java.util.List;

// Адаптер для загрузки заголовков тем в RecyclerView c темами для изучения
public class ArticleAdapter extends RecyclerView.Adapter<ArticleHolder> {
    private final Context context;
    private final List<Article> articles;

    // Далее для реализации счётчика для активации и деактивации кнопки "Начать"
    private OnItemsCheckStateListener checkStateListener;
    private final ArrayList<String> checkedItems = new ArrayList<>();


    public interface OnItemsCheckStateListener {    // при нажатии на итем
        void onItemCheckStateChanged(ArrayList<String> checkedItemCounter);
    }

    public void setOnItemsCheckStateListener(OnItemsCheckStateListener checkStateListener) {
        this.checkStateListener = checkStateListener;
    }


    public ArticleAdapter(Context context, List<Article> articles) {
        this.context = context;
        this.articles = articles;
    }


    @NonNull
    @Override
    public ArticleHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View myView = LayoutInflater.from(context)
                .inflate(R.layout.article_view, parent, false);

        // Далее настраиваем recyclerview под размер экрана устройства
        ViewTreeObserver viewTreeObserver = myView.getViewTreeObserver();
        GridLayoutManager.LayoutParams params =
                (GridLayoutManager.LayoutParams) myView.getLayoutParams();

        viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                myView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int viewWidth = myView.getWidth();
                int viewHeight = myView.getHeight() - 80;

                if (viewHeight / viewWidth < 4) {
                    params.width = viewHeight / 4;
                    params.height = viewHeight / 4;
                    params.leftMargin = (myView.getWidth() - (params.width)) / 2;

                } else {
                    params.height = viewWidth;
                    params.width = viewWidth;
                    params.leftMargin = (myView.getWidth() - (params.width)) / 2;
                }

                myView.setLayoutParams(params);
            }
        });
        return new ArticleHolder(myView);
    }


    @Override
    public void onBindViewHolder(@NonNull ArticleHolder holder, int position) {
        final Article model = articles.get(position);
        holder.articleName.setText(model.getName());
        Glide.with(this.context).load(model.getUri()).into(holder.articleImage);
        holder.checkIcon.setVisibility(model.isSelected() ? View.VISIBLE : View.GONE);
        holder.fade.setVisibility(model.isSelected() ? View.VISIBLE : View.GONE);

        // Прописываем изменения каждого из элементов при его нажатии
        holder.articleImage.setOnClickListener(view -> {
            model.setSelected(!model.isSelected());
            holder.checkIcon.setVisibility(model.isSelected() ? View.VISIBLE : View.GONE);
            holder.fade.setVisibility(model.isSelected() ? View.VISIBLE : View.GONE);

            // Если элемент отмечен, то добавляем его заголовок в коллекцию и меняем состояние кнопки "Начать"
            if (model.isSelected()) checkedItems.add(model.getName());
            else checkedItems.remove(model.getName());
            checkStateListener.onItemCheckStateChanged(checkedItems);
        });
    }

    @Override
    public int getItemCount() {
        return articles.size();
    }

}
