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
import android.widget.LinearLayout;
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
    private LinearLayout backLight;

    private View statusBarOverlay;

    private View originalLayout; // 原 layout
    private View stickyLayout;   // 吸顶 layout
    private Button stickyBtnHot, stickyBtnLatest;

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
        backLight = findViewById(R.id.back_light);
        statusBarOverlay = findViewById(R.id.status_bar_overlay);

        originalLayout = findViewById(R.id.layout);
        stickyLayout = findViewById(R.id.sticky_layout);
        stickyBtnHot = findViewById(R.id.sticky_btn_hot);
        stickyBtnLatest = findViewById(R.id.sticky_btn_latest);

        // 设置状态栏高度
        setStatusBarHeight();
        // 初始化状态栏图标颜色
        setStatusBarIconColor(false);

        backButton.setImageResource(R.mipmap.cg_icon_back_light); // 设置初始图标

        // 设置滚动监听
        scrollView.setOnScrollChangedListener(scrollY -> {
            if (ivShow.getHeight() == 0) return;

            // 计算透明度（0-1）
            float alpha = (float) scrollY / ivShow.getHeight();
            alpha = Math.min(1f, Math.max(0f, alpha));

            // 更新状态栏遮罩
            statusBarOverlay.setAlpha(alpha); // 遮罩逐渐显现
            statusBarOverlay.setBackground(getDrawable(R.color.black));

            // 根据透明度切换状态栏图标颜色
            // 更新遮罩层效果
            if (alpha > 0.3f) {
                // 上划时：白色背景+深色图标
                statusBarOverlay.setBackgroundColor(Color.WHITE);
                setStatusBarIconColor(true);
                statusBarOverlay.setAlpha(alpha);
            } else {
                // 下划时：黑色半透明背景+浅色图标
                statusBarOverlay.setBackgroundColor(Color.BLACK);
                setStatusBarIconColor(false);
                statusBarOverlay.setAlpha(0.2f * (1 - alpha)); // 保持半透明效果
            }

            // 更新标题栏背景透明度（从透明到#F7FAFC）
            int bgAlpha = (int) (alpha * 255);
            int statusBarColor = Color.argb(bgAlpha, 247, 250, 252);
            titleBar.setBackgroundColor(statusBarColor);

            // 更新返回按钮图标
            if (alpha > 0.1f) { // 当透明度超过10%时切换图标
                backButton.setImageResource(R.mipmap.cg_icon_back_light_two);
            } else {
                backButton.setImageResource(R.mipmap.cg_icon_back_light);
            }

            // 更新标题文本透明度
            titleText.setAlpha(alpha);
            titleBar.setVisibility(alpha > 0 ? View.VISIBLE : View.INVISIBLE);

            // 计算吸顶条件
            int[] location = new int[2];
            originalLayout.getLocationOnScreen(location);
            int originalTop = location[1]; // 原 layout 在屏幕中的 Y 坐标
            int titleBarHeight = titleBar.getHeight() + statusBarOverlay.getHeight();

            if (originalTop <= titleBarHeight) {
                stickyLayout.setVisibility(View.VISIBLE);
                // 同步按钮状态
                updateStickyButtonState(viewPager.getCurrentItem());
            } else {
                stickyLayout.setVisibility(View.GONE);
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
        // 吸顶按钮点击事件（与原按钮同步）
        stickyBtnHot.setOnClickListener(v -> viewPager.setCurrentItem(0, true));
        stickyBtnLatest.setOnClickListener(v -> viewPager.setCurrentItem(1, true));

        viewPager.setNestedScrollingEnabled(false);
        viewPager.setPageTransformer(new DepthPageTransformer());
        // ViewPager2 页面切换监听
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                // 更新按钮状态
                updateButtonState(position); // 更新原按钮
                updateStickyButtonState(position); // 更新吸顶按钮
            }
        });
    }

    private void toggleText() {
        if (isExpanded) { // 如果已展开，则收起并更新文本为省略状态
            textViewContent.setMaxLines(4);
            expand.setText(R.string.full); // 更新按钮文本为“展开”
        } else { // 如果未展开，则显示全部文本并更新按钮状态
            textViewContent.setMaxLines(Integer.MAX_VALUE);
            expand.setText(R.string.pack); // 更新按钮文本为“收起”
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

    private void setStatusBarHeight() {
        int statusBarHeight = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
        }
        ViewGroup.LayoutParams params = statusBarOverlay.getLayoutParams();
        params.height = statusBarHeight;
        statusBarOverlay.setLayoutParams(params);

        // 设置初始状态
        statusBarOverlay.setAlpha(0.2f);
        statusBarOverlay.setBackgroundColor(Color.BLACK);


        // 设置标题栏的 marginTop
        ViewGroup.MarginLayoutParams titleBarParams = (ViewGroup.MarginLayoutParams) titleBar.getLayoutParams();
        titleBarParams.topMargin = statusBarHeight; // 动态调整标题栏位置
        titleBar.setLayoutParams(titleBarParams);

        ViewGroup.MarginLayoutParams backLightParams = (ViewGroup.MarginLayoutParams) backLight.getLayoutParams();
        backLightParams.topMargin = statusBarHeight; // 动态调整标题栏位置
        backLight.setLayoutParams(backLightParams);
    }

    private void setStatusBarIconColor(boolean dark) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int flags = decorView.getSystemUiVisibility();
            if (dark) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            } else {
                flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            }
            decorView.setSystemUiVisibility(flags);
        }
    }

    // 更新吸顶按钮状态（与原按钮同步）
    private void updateStickyButtonState(int position) {
        stickyBtnHot.setSelected(position == 0);
        stickyBtnLatest.setSelected(position == 1);
        // 根据 position 更新颜色和背景，逻辑与原按钮一致
        if (position == 0) {
            stickyBtnHot.setTextColor(getResources().getColor(R.color.black));
            stickyBtnLatest.setTextColor(getResources().getColor(R.color.color_A600050A));
            stickyBtnHot.setBackground(getResources().getDrawable(R.drawable.button_bg1));
            stickyBtnLatest.setBackground(getResources().getDrawable(R.color.transparent));
        } else {
            stickyBtnLatest.setTextColor(getResources().getColor(R.color.black));
            stickyBtnHot.setTextColor(getResources().getColor(R.color.color_A600050A));
            stickyBtnHot.setBackground(getResources().getDrawable(R.color.transparent));
            stickyBtnLatest.setBackground(getResources().getDrawable(R.drawable.button_bg1));
        }
    }
}