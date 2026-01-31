package com.example.translator_three.accessibility;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Rect;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log; // 新增：导入Log类
import android.view.Gravity;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.translator_three.R;
import com.example.translator_three.repository.TranslationRepository;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TranslationAccessibilityService extends AccessibilityService {
    private static final String CHANNEL_ID = "TranslateService";
    private static final int NOTIFICATION_ID = 1;
    private static final long DEBOUNCE_DELAY = 500; // 防抖延迟500ms
    private static final int MIN_TEXT_LENGTH = 2;   // 最小文本长度
    private static final int MAX_TEXT_LENGTH = 500; // 最大文本长度

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private TranslationRepository translationRepo;
    private String currentSelectedText;
    private TranslationPopupWindow popupWindow;
    private static final String TAG = "TranslateService";
    //private Handler mainHandler = new Handler(Looper.getMainLooper());

    // 防抖Runnable
    private final Runnable debounceRunnable = () -> {
        if (currentSelectedText != null
                && currentSelectedText.length() >= MIN_TEXT_LENGTH
                && currentSelectedText.length() <= MAX_TEXT_LENGTH) {
            translateSelectedText(currentSelectedText);
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TranslateService", "✅ 无障碍服务已创建并启动，onCreate 被调用！");

        // 关键：给初始化加try-catch，抓出隐藏异常
        try {
            translationRepo = new TranslationRepository(this);
            Log.d(TAG, "✅ translationRepo 初始化成功！"); // 能打印说明构造方法没抛异常
        } catch (Exception e) {
            Log.e(TAG, "❌ translationRepo 初始化失败！原因：", e); // 打印所有异常信息
            // 即使失败也要设置一个默认值，避免空指针
            translationRepo = null;
        }

        popupWindow = new TranslationPopupWindow(this);
        createNotificationChannel();

        // Android 12+ 需要前台服务
        // 修复：Android 14+ 启动前台服务必须指定类型
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14（API 34）
            startForeground(
                    NOTIFICATION_ID,
                    createForegroundNotification(),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE   // 明确指定“无障碍服务”类型
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12-13（API 31-33）
            startForeground(NOTIFICATION_ID, createForegroundNotification()); // 旧版本不需要类型
        }

        // 添加测试Toast
        mainHandler.postDelayed(() -> {
            Toast.makeText(this, "划词翻译服务已启动", Toast.LENGTH_LONG).show();
        }, 1000);

    }

    // 添加服务状态回调
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "服务已连接,onServiceConnected 被调用！");

        // 设置服务配置
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        // 只监听文本选择变化事件
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED;

//        // 只监听用户交互的包，避免系统级监听。注释掉监听所有的应用
//        info.packageNames = new String[]{
//                "com.tencent.mm", // 微信
//                "com.eg.android.AlipayGphone", // 支付宝
//                "com.android.chrome", // Chrome
//                "com.android.browser", // 系统浏览器
//                "com.example.translator_three" // 你自己的应用
//        };

        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = 100;

//        // 添加这些标志以增强兼容性
//        info.flags = AccessibilityServiceInfo.DEFAULT;
//        info.flags |= AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
//        info.flags |= AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;

        setServiceInfo(info);

        // 验证配置是否设置成功
        AccessibilityServiceInfo currentInfo = getServiceInfo();
        Log.d(TAG, "当前服务配置 - 事件类型: " + currentInfo.eventTypes);
        Log.d(TAG, "当前服务配置 - 包名限制: " + (currentInfo.packageNames == null ? "无限制" : "有限制"));
        Log.d(TAG, "当前服务配置 - 标志: " + currentInfo.flags);

        Toast.makeText(this, "服务配置完成,划词翻译已启动", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 添加事件来源信息
        Log.d(TAG, "收到无障碍事件 - 类型: " + event.getEventType() +
                ", 包名: " + event.getPackageName() +
                ", 类名: " + event.getClassName());

        // 极简处理：只处理文本选择事件
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            Log.d(TAG, "处理文本选择事件 - 来源: " + event.getPackageName());

            handleTextSelection(event);
        }
    }

    private void handleTextSelection(AccessibilityEvent event) {
        AccessibilityNodeInfo source = event.getSource();
        if (source == null) return;

        try {
            // 极简文本提取：只检查选中范围
            int start = source.getTextSelectionStart();
            int end = source.getTextSelectionEnd();

            if (start >= 0 && end > start && source.getText() != null) {
                String fullText = source.getText().toString();
                if (fullText.length() >= end) {
                    String selectedText = fullText.substring(start, end).trim();

                    // 基本验证
                    if (selectedText.length() >= 2 && selectedText.length() <= 100) {
                        Log.d(TAG, "选中文本: " + selectedText);
                        showSimpleToast("选中: " + selectedText); // 先用Toast测试

                        // ！！！重要：添加翻译调用 ！！！
                        currentSelectedText = selectedText;
                        mainHandler.removeCallbacks(debounceRunnable);
                        mainHandler.postDelayed(debounceRunnable, DEBOUNCE_DELAY);
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "处理异常: " + e.getMessage());
        } finally {
            source.recycle(); // 必须回收
        }
    }

    private void showSimpleToast(String message) {
        mainHandler.post(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    // 改进的文本提取方法
    private String extractSelectedText(AccessibilityNodeInfo node) {
        if (node == null) {
            Log.d("TranslateService", "节点为null");
            return null;
        }

        try {
            // 方法1: 直接获取选中文本
            if (node.getTextSelectionStart() >= 0 && node.getTextSelectionEnd() > node.getTextSelectionStart()) {
                CharSequence text = node.getText();
                if (text != null) {
                    String selected = text.subSequence(
                            node.getTextSelectionStart(),
                            node.getTextSelectionEnd()
                    ).toString().trim();
                    Log.d("TranslateService", "通过选中范围提取文本: " + selected);
                    return selected;
                }
            }

            // 方法2: 获取节点文本内容
            if (node.getText() != null) {
                String text = node.getText().toString().trim();
                if (!text.isEmpty()) {
                    Log.d("TranslateService", "通过节点文本提取: " + text);
                    return text;
                }
            }

            // 方法3: 获取内容描述
            if (node.getContentDescription() != null) {
                String contentDesc = node.getContentDescription().toString().trim();
                if (!contentDesc.isEmpty()) {
                    Log.d("TranslateService", "通过内容描述提取: " + contentDesc);
                    return contentDesc;
                }
            }

            // 方法4: 递归检查子节点
            for (int i = 0; i < node.getChildCount(); i++) {
                AccessibilityNodeInfo child = node.getChild(i);
                if (child != null) {
                    String childText = extractSelectedText(child);
                    if (childText != null && !childText.isEmpty()) {
                        child.recycle();
                        return childText;
                    }
                    child.recycle();
                }
            }

        } catch (Exception e) {
            Log.e("TranslateService", "提取文本异常: " + e.getMessage());
        }

        return null;
    }

    // 翻译选中的文本
    private void translateSelectedText(String text) {
        Log.d(TAG, "=== 开始翻译流程 ===");

        new Thread(() -> {
            try {
                Log.d(TAG, "开始真实翻译: " + text);

                if (translationRepo == null) {
                    Log.e(TAG, "TranslationRepository 未初始化！");
                    mainHandler.post(() ->
                            Toast.makeText(TranslationAccessibilityService.this, "翻译服务未就绪", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                // 调用翻译并设置超时（10秒）
                Future<String> future = translationRepo.translate(text);
                String result = future.get(10, TimeUnit.SECONDS); // 添加超时控制

                Log.d(TAG, "百度API返回结果: " + (result != null ? result : "null"));

                if (result != null && !result.isEmpty()) {
                    showTranslationResult(result); // 显示弹窗
                } else {
                    mainHandler.post(() ->
                            Toast.makeText(TranslationAccessibilityService.this, "翻译失败，结果为空", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (ExecutionException e) {
                Log.e(TAG, "翻译执行异常: " + e.getMessage());
                mainHandler.post(() ->
                        Toast.makeText(TranslationAccessibilityService.this, "翻译失败: " + e.getCause().getMessage(), Toast.LENGTH_SHORT).show()
                );
            } catch (TimeoutException e) {
                Log.e(TAG, "翻译超时");
                mainHandler.post(() ->
                        Toast.makeText(TranslationAccessibilityService.this, "翻译超时", Toast.LENGTH_SHORT).show()
                );
            } catch (InterruptedException e) {
                Log.e(TAG, "翻译被中断");
                Thread.currentThread().interrupt();
            }
        }).start();
    }


    private void showError(String message) {
        mainHandler.post(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    // 显示翻译结果
    private void showTranslationResult(String result) {
        Log.d(TAG, "=== 进入 showTranslationResult ===");

        mainHandler.post(() -> {
            Log.d(TAG, "在主线程中执行显示");

            try {
                if (popupWindow == null) {
                    Log.d(TAG, "创建新的 popupWindow");
                    popupWindow = new TranslationPopupWindow(this);
                }

                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;
                int screenHeight = metrics.heightPixels;

                // 计算顶部居中位置（符合"最顶部弹出"需求）
                int x = (screenWidth - popupWindow.getWidth()) / 2; // 水平居中
                int y = 100; // 距离顶部100px

                Log.d(TAG, "弹窗位置: x=" + x + ", y=" + y);

                popupWindow.showAtPosition(x, y, result);
                Log.d(TAG, "=== 弹窗显示完成 ===");

            } catch (Exception e) {
                Log.e(TAG, "显示异常: " + e.getMessage(), e);
                Toast.makeText(this, "弹窗显示失败", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 创建通知渠道
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "翻译服务",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    // 创建前台服务通知
    private Notification createForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("划词翻译已启动")
                .setContentText("长按选中文本即可翻译")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        mainHandler.post(() ->
                Toast.makeText(this, "翻译服务已关闭，请重新开启", Toast.LENGTH_LONG).show()
        );
        return super.onUnbind(intent);
    }

    @Override
    public void onInterrupt() {}
}