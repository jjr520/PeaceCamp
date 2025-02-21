package com.example.peacecamp;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class DepthPageTransformer implements ViewPager2.PageTransformer {
    private static final float MIN_SCALE = 0.75f;

    @Override
    public void transformPage(@NonNull View page, float position) {
        int pageWidth = page.getWidth();

        if (position < -1) { // [-Infinity, -1)
            page.setAlpha(0);
        } else if (position <= 0) { // [-1, 0]
            page.setAlpha(1);
            page.setTranslationX(0);
            page.setScaleX(1);
            page.setScaleY(1);
        } else if (position <= 1) { // (0, 1]
            page.setAlpha(1 - position);
            page.setTranslationX(pageWidth * -position);
            float scaleFactor = MIN_SCALE + (1 - MIN_SCALE) * (1 - Math.abs(position));
            page.setScaleX(scaleFactor);
            page.setScaleY(scaleFactor);
        } else { // (1, +Infinity]
            page.setAlpha(0);
        }
    }
}

/*
import android.view.View;
import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class DynamicSlidePageTransformer implements ViewPager2.PageTransformer {
    @Override
    public void transformPage(@NonNull View page, float position) {
        int pageWidth = page.getWidth();

        if (position < -1) { // [-Infinity, -1)
            page.setAlpha(0);
        } else if (position <= 0) { // [-1, 0]
            // 从左侧滑入
            page.setAlpha(1 + position);
            page.setTranslationX(pageWidth * -position);
        } else if (position <= 1) { // (0, 1]
            // 从右侧滑入
            page.setAlpha(1 - position);
            page.setTranslationX(-pageWidth * position);
        } else { // (1, +Infinity]
            page.setAlpha(0);
        }
    }
}
* */

