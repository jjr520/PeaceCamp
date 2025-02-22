package com.example.peacecamp;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.stream.Collectors;

public class MainActivity extends AppCompatActivity {
    private HeadZoomScrollView scrollView;
    private TextView textViewContent;
    private TextView expand;
    private boolean isExpanded = false;

    private String fullText = "【参与方式】关注策划小白裙账号，在#一起去坐滑翔机#话题下晒出你在游戏中驾驶滑翔机的截图和技巧，或者分享你想和谁一起坐上滑翔机，你想怎么使用滑翔机。\n" +
            "【活动奖励】符合活动要求的动态中抽10人每人送200Q币，抽10人每人送100Q币，抽5人每人送游戏永久套装-壁垒格斗家一件。";


    private Button btnHot, btnLatest;
    private ViewPager2 viewPager;

    private ImageView ivShow, backButton;
    private View titleBar;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scrollView = findViewById(R.id.scrollView);
        textViewContent = findViewById(R.id.content);
        expand = findViewById(R.id.expand);
        btnHot = findViewById(R.id.btn_hot);
        btnLatest = findViewById(R.id.btn_latest);
        // 初始化 ViewPager2
        viewPager = findViewById(R.id.view_pager);

        ivShow = findViewById(R.id.iv_show);
        titleBar = findViewById(R.id.title_bar);
        titleText = findViewById(R.id.title_text);
        backButton = findViewById(R.id.back_button);

        // 设置全屏模式并保留状态栏图标
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT); // 状态栏透明
            window.getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        backButton.setImageResource(R.mipmap.cg_icon_back_light); // 设置初始图标

        // 设置滚动监听
        scrollView.setOnScrollChangedListener(scrollY -> {
            if (ivShow.getHeight() == 0) return;

            // 计算透明度（0-1）
            float alpha = (float) scrollY / ivShow.getHeight();
            alpha = Math.min(1f, Math.max(0f, alpha));

            // 更新标题栏背景透明度（从透明到#F7FAFC）
            int bgAlpha = (int) (alpha * 255);
            titleBar.setBackgroundColor(Color.argb(bgAlpha, 247, 250, 252));

            // 更新返回按钮图标
            if (alpha > 0.1f) { // 当透明度超过10%时切换图标
                backButton.setImageResource(R.mipmap.cg_icon_back_light_two);
            } else {
                backButton.setImageResource(R.mipmap.cg_icon_back_light);
            }

            // 更新标题文本透明度
            titleText.setAlpha(alpha);
            titleBar.setVisibility(alpha > 0 ? View.VISIBLE : View.INVISIBLE);

            // 更新状态栏（API 21+）
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int statusBarColor = Color.argb(bgAlpha, 255, 255, 255); // 白色背景
                getWindow().setStatusBarColor(statusBarColor);

                // 根据背景亮度调整状态栏图标颜色
                if (alpha > 0.5f) {
                    // 浅色背景使用深色图标
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
                } else {
                    // 深色背景使用浅色图标
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                }
            }
        });

        // 确保图片高度测量正确
        ivShow.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            // 触发一次滚动更新
            scrollView.post(() -> scrollView.scrollTo(0, scrollView.getScrollY()));
        });

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        textViewContent.setText(fullText);
        expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleText();
            }
        });

        // 按钮点击切换 Fragment
        btnHot.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        btnLatest.setOnClickListener(v -> viewPager.setCurrentItem(1, true));
        viewPager.setNestedScrollingEnabled(false);
        viewPager.setPageTransformer(new DepthPageTransformer());
        // ViewPager2 页面切换监听
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // 更新按钮状态
                updateButtonState(position);
            }
        });
    }

    private void toggleText() {
        if (isExpanded) { // 如果已展开，则收起并更新文本为省略状态
            textViewContent.setMaxLines(4);
            expand.setText("展开"); // 更新按钮文本为“展开”
        } else { // 如果未展开，则显示全部文本并更新按钮状态
            textViewContent.setMaxLines(Integer.MAX_VALUE);
            expand.setText("收起"); // 更新按钮文本为“收起”
        }
        isExpanded = !isExpanded; // 切换状态
    }

    // 更新按钮状态
    private void updateButtonState(int position) {
        btnHot.setSelected(position == 0);
        btnLatest.setSelected(position == 1);
        if (position == 0) {
            btnHot.setTextColor(getResources().getColor(R.color.black));
            btnLatest.setTextColor(getResources().getColor(R.color.color_A600050A));
            btnHot.setBackground(getResources().getDrawable(R.drawable.button_bg1));
            btnLatest.setBackground(getResources().getDrawable(R.color.transparent));
        } else if (position == 1) {
            btnLatest.setTextColor(getResources().getColor(R.color.black));
            btnHot.setTextColor(getResources().getColor(R.color.color_A600050A));
            btnHot.setBackground(getResources().getDrawable(R.color.transparent));
            btnLatest.setBackground(getResources().getDrawable(R.drawable.button_bg1));
        }
    }

}