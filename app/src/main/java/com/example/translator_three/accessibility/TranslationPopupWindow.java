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

//一个普通的java类，并没有继承View或其子类，用于创建和管理系统级弹窗
public class TranslationPopupWindow {
    private static final String TAG = "TranslationPopupWindow";
    private final Context context;
    private WindowManager windowManager;
    private View popupView;
    private boolean isShowing = false;
    //Handler：用于在不同线程间发送和处理消息，可以将任务从后台线程切换到主线程执行
    private Handler mainHandler = new Handler(Looper.getMainLooper()); //获取主线程的Looper，用于在主线程处理消息

    public TranslationPopupWindow(Context context) {  //安卓上下文对象，提供访问应用资源和系统服务的能力
        //保存上下文对象，用于后续创建弹窗视图和访问应用资源
        this.context = context;
        //获取WM系统服务，用于后续创建弹窗视图和管理弹窗。（能跨app显示的）
        this.windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        //初始化弹窗视图，加载弹窗布局文件
        initView();
    }

    private void initView() {
        Log.d(TAG, "初始化系统级弹窗");

        try {
            // 加载布局
            //LayoutInflater布局加载器：用于将XML布局文件转换为对应的View对象
            popupView = LayoutInflater.from(context).inflate(R.layout.popup_translation, null);

            Log.d(TAG, "布局加载成功");

        } catch (Exception e) {
            Log.e(TAG, "布局加载失败: " + e.getMessage(), e);
            return;
        }
    }
    
    //显示弹窗方法：在指定位置显示弹窗，并设置弹窗内容
    public void showAtPosition(int x, int y, String result) {
        Log.d(TAG, "显示系统弹窗 - 位置: (" + x + ", " + y + "), 内容: " + result);

        mainHandler.post(() -> { //UI操作必须在主线程执行
            try {
                // 如果正在显示，先隐藏：避免重复显示弹窗
                if (isShowing) {
                    Log.d(TAG, "弹窗正在显示，先隐藏");
                    hide();
                    // 添加短暂延迟确保隐藏完成，给系统50ms时间处理隐藏操作
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
                        WindowManager.LayoutParams.WRAP_CONTENT,  //宽
                        WindowManager.LayoutParams.WRAP_CONTENT,  //高：自适应内容大小
                        Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?  //如果是API 26及以上使用应用级弹窗
                                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                                WindowManager.LayoutParams.TYPE_PHONE,
                        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE |
                                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,  //三个标志位从上到下分别表示：
                                //1. 弹窗不获取焦点，不阻塞其他应用
                                //2. 弹窗不接收触摸事件，防止干扰用户操作
                                //3. 弹窗显示时保持屏幕亮起，防止自动熄灭
                        PixelFormat.TRANSLUCENT  //像素格式：半透明，允许底层可见
                );

                //设置弹窗位置
                params.gravity = Gravity.TOP | Gravity.START;  //坐标系原点为屏幕左上角
                params.width = 600;         //固定宽度600像素
                params.height = WindowManager.LayoutParams.WRAP_CONTENT;  //自适应高度
                params.x = x;  //设置具体显示位置
                params.y = y;

                Log.d(TAG, "窗口参数设置完成，准备添加到窗口管理器");

                // 添加到窗口管理器
                windowManager.addView(popupView, params);
                isShowing = true;

                Log.d(TAG, "-v- 系统弹窗显示成功！");

                // 5秒后自动隐藏
                mainHandler.postDelayed(() -> {
                    hide();
                }, 5000);

            } catch (Exception e) {
                Log.e(TAG, "显示系统弹窗异常: " + e.getMessage(), e);
            }
        });
    }

    //隐藏弹窗
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