package com.example.translator_three.accessibility;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.translator_three.R;

public class TranslationPopupWindow {
    private static final String TAG = "TranslationPopupWindow";
    private final Context context;
    private WindowManager windowManager;
    private View popupView;
    private boolean isShowing = false;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    public TranslationPopupWindow(Context context) {
        this.context = context;
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        initView();
    }

    private void initView() {
        Log.d(TAG, "初始化系统级弹窗");

        try {
            // 加载布局
            popupView = LayoutInflater.from(context).inflate(R.layout.popup_translation, null);

            Log.d(TAG, "布局加载成功");

        } catch (Exception e) {
            Log.e(TAG, "布局加载失败: " + e.getMessage(), e);
            return;
        }
    }

//    public void showAtPosition(int x, int y, String result) {
//        Log.d(TAG, "显示系统弹窗 - 位置: (" + x + ", " + y + "), 内容: " + result);
//
//        mainHandler.post(() -> {
//            try {
//                // 如果正在显示，先隐藏
//                if (isShowing) {
//                    hide();
//                }
//
//                // 设置文本内容
//                TextView tvResult = popupView.findViewById(R.id.tv_translation_result);
//                if (tvResult != null) {
//                    tvResult.setText(result);
//                    Log.d(TAG, "文本设置成功: " + result);
//                } else {
//                    Log.e(TAG, "找不到 tv_translation_result");
//                    return;
//                }
//
//                // 创建窗口参数 - 关键代码！
//                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
//                        WindowManager.LayoutParams.WRAP_CONTENT,
//                        WindowManager.LayoutParams.WRAP_CONTENT,
//                        // 使用无障碍服务专用窗口类型（兼容所有版本）
//                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
//                                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY :
//                                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
//                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
//                                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, // 允许点击其他区域
//                        PixelFormat.TRANSLUCENT
//                );
//
//                params.gravity = Gravity.TOP | Gravity.START;
//                params.width = 600;  // 固定宽度
//                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
//                params.x = x;
//                params.y = y;
//
//                Log.d(TAG, "窗口参数设置完成，准备添加到窗口管理器");
//
//                // 添加到窗口管理器
//                windowManager.addView(popupView, params);
//                isShowing = true;
//
//                Log.d(TAG, "✅ 系统弹窗显示成功！");
//
//                // 5秒后自动隐藏
//                mainHandler.postDelayed(() -> {
//                    hide();
//                }, 5000);
//
//            } catch (Exception e) {
//                Log.e(TAG, "显示系统弹窗异常: " + e.getMessage(), e);
//            }
//        });
//    }

    public void showAtPosition(int x, int y, String result) {
        Log.d(TAG, "显示系统弹窗 - 位置: (" + x + ", " + y + "), 内容: " + result);

        mainHandler.post(() -> {
            try {
                // 如果正在显示，先隐藏
                if (isShowing) {
                    Log.d(TAG, "弹窗正在显示，先隐藏");
                    hide();
                    // 添加短暂延迟确保隐藏完成
                    Thread.sleep(50);
                }

                // 设置文本内容
                TextView tvResult = popupView.findViewById(R.id.tv_translation_result);
                if (tvResult != null) {
                    tvResult.setText(result);
                    Log.d(TAG, "文本设置成功: " + result);
                } else {
                    Log.e(TAG, "找不到 tv_translation_result");
                    return;
                }

                // 创建新的窗口参数（避免重复使用旧的）
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        WindowManager.LayoutParams.WRAP_CONTENT,
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                                WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                        PixelFormat.TRANSLUCENT
                );

                params.gravity = Gravity.TOP | Gravity.START;
                params.width = 600;
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;
                params.x = x;
                params.y = y;

                Log.d(TAG, "窗口参数设置完成，准备添加到窗口管理器");

                // 添加到窗口管理器
                windowManager.addView(popupView, params);
                isShowing = true;

                Log.d(TAG, "✅ 系统弹窗显示成功！");

                // 5秒后自动隐藏
                mainHandler.postDelayed(() -> {
                    hide();
                }, 5000);

            } catch (Exception e) {
                Log.e(TAG, "显示系统弹窗异常: " + e.getMessage(), e);
            }
        });
    }

    public void hide() {
        mainHandler.post(() -> {
            if (isShowing && popupView != null) {
                try {
                    windowManager.removeView(popupView);
                    isShowing = false;
                    Log.d(TAG, "弹窗已隐藏");
                } catch (Exception e) {
                    Log.e(TAG, "隐藏弹窗异常: " + e.getMessage());
                }
            }
        });
    }

    public int getWidth() {
        return 600;
    }
}