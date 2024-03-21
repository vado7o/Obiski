package com.example.obiski.words;

import android.animation.Animator;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.obiski.R;

import java.util.List;

// Адаптер для загрузки слов в RecyclerView для изучения
public class WordAdapter extends RecyclerView.Adapter<WordHolder> {
    private final Context context;
    private final List<Word> words;
    private Boolean disabled = false;      // bool для отслеживания кликабельности итемов в recyclerview (слова заблокированы пока играет mediaPlayer)

    // Далее для активации и деактивации кнопки "Ещё раз"
    private OnItemsCheckStateListener checkStateListener;
    private String checkedWord = "";


    public interface OnItemsCheckStateListener {    // при нажатии на итем
        void onItemCheckStateChanged(String checkedWord, ObjectAnimator anim, ObjectAnimator animR,
                                     ImageView fade, ImageView checkIcon, ImageView wrongIcon,
                                     ImageView fadeT, ImageView wordImage, WordAdapter disabled);
    }

    public void setOnItemsCheckStateListener(OnItemsCheckStateListener checkStateListener) {
        this.checkStateListener = checkStateListener;
    }

    public WordAdapter(Context context, List<Word> words) {
        this.context = context;
        this.words = words;
    }

    public void setDisabled(Boolean disabled) {
        this.disabled = disabled;
    }

    @NonNull
    @Override
    public WordHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View myView = LayoutInflater.from(context)
                .inflate(R.layout.word_view, viewGroup, false);

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
        return new WordHolder(myView);
    }


    @Override
    public void onBindViewHolder(@NonNull WordHolder holder, int position) {
        final Word model = words.get(position);
        holder.wordName.setVisibility(View.GONE);
        holder.wordName.setText(model.getName());
        Glide.with(this.context).load(model.getImgUri()).into(holder.wordImage);
        holder.checkIcon.setVisibility(View.GONE);
        holder.wrongIcon.setVisibility(View.GONE);
        holder.fade.setVisibility(View.GONE);
        holder.fadeT.setVisibility(View.GONE);

        // Создаём анимацию мигания при неправильном ответе
        ObjectAnimator animWrong = ObjectAnimator.ofInt(holder.fade, "ColorFilter",
                Color.RED, Color.TRANSPARENT, Color.RED);
        animWrong.setDuration(300);
        animWrong.setEvaluator(new ArgbEvaluator());
        animWrong.setRepeatCount(3);
        animWrong.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
                holder.fade.setVisibility(View.GONE);
                holder.wrongIcon.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
            }
        });

        // Создаём анимацию мигания при неправильном ответе
        ObjectAnimator animRight = ObjectAnimator.ofInt(holder.fadeT, "ColorFilter",
                Color.GREEN, Color.TRANSPARENT, Color.GREEN, Color.TRANSPARENT);
        animRight.setDuration(300);
        animRight.setEvaluator(new ArgbEvaluator());
        animRight.setRepeatCount(3);
        animRight.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationEnd(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationCancel(@NonNull Animator animation) {
            }

            @Override
            public void onAnimationRepeat(@NonNull Animator animation) {
            }
        });


        // Прописываем изменения каждого из элементов при его нажатии
        holder.wordImage.setOnClickListener(view -> {
            if (!this.disabled) {  // если в настоящий момент ещё не играет mediaPlayer
                checkedWord = model.getName(); // Если элемент нажат, то добавляем его название в checkedWord

                checkStateListener.onItemCheckStateChanged(checkedWord, animWrong, animRight,
                        holder.fade, holder.checkIcon, holder.wrongIcon, holder.fadeT,
                        holder.wordImage, this);
                setDisabled(true);
            }
        });
    }

    @Override
    public int getItemCount() {
        return words.size();
    }
}
