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

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

//无障碍服务的官方示例和文档大多使用 Java 编写
//1、无障碍服务类定义
public class TranslationAccessibilityService extends AccessibilityService {
    //static可以节省内存，而且static final这么写明确这是类级别的常量
    private static final String CHANNEL_ID = "TranslateService";  //通知渠道的标识符，用于区分不同的通知渠道
    private static final int NOTIFICATION_ID = 1;  //通知的唯一标识符，系统用这个id来区分不同的通知
    private static final long DEBOUNCE_DELAY = 500; // 防抖延迟500ms
    private static final int MIN_TEXT_LENGTH = 2;   // 最小文本长度
    private static final int MAX_TEXT_LENGTH = 500; // 最大文本长度

    private final Handler mainHandler = new Handler(Looper.getMainLooper()); // 与主线程绑定的Handler处理器，在子线程执行耗时操作后，用于在主线程更新UI
    private TranslationRepository translationRepo;  //翻译仓库，用于访问翻译API
    private String currentSelectedText;  //当前选择的文本
    private TranslationPopupWindow popupWindow;  //翻译弹窗窗口
    private static final String TAG = "TranslateService";  //日志标签，用于区分不同的日志输出来源

    // 防抖机制Runnable：避免频繁触发翻译 ：当用户连续选中文本或调整选择范围时，不会每次都触发翻译
    //Java8+的Lambda表达式，用于创建一个 Runnable 接口的匿名实现类
    private final Runnable debounceRunnable = () -> {  //通过 Handler.postDelayed(debounceRunnable, DEBOUNCE_DELAY) 延迟 500ms 执行
        if (currentSelectedText != null
                && currentSelectedText.length() >= MIN_TEXT_LENGTH
                && currentSelectedText.length() <= MAX_TEXT_LENGTH) {
            translateSelectedText(currentSelectedText);
        }
    };

    //无障碍服务的生命周期
    //（1）服务创建
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("TranslateService", "无障碍服务已创建并启动，onCreate 被调用！");

        // 关键：给初始化加try-catch，抓出隐藏异常
        //初始化核心组件（翻译仓库、弹窗窗口）
        try {
            translationRepo = new TranslationRepository(this);  //这里传入当前类的实例，因为当前类最终就是继承Context的所以
            Log.d(TAG, " translationRepo 初始化成功！"); // 能打印说明构造方法没抛异常
        } catch (Exception e) {
            Log.e(TAG, " translationRepo 初始化失败！原因：", e); // 打印所有异常信息
            // 即使失败也要设置一个默认值，避免空指针
            translationRepo = null;
        }

        popupWindow = new TranslationPopupWindow(this);

        //创建通知渠道和通知管理服务实例
        createNotificationChannel();

        // Android 12+ 需要前台服务才能正常工作
        // Android 14+ 启动前台服务必须指定类型
        //根据不同安卓版本要求使用不同方式启动前台服务
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) { // Android 14（API 34，UPSIDE_DOWN_CAKE）
            //Service的知识点！如何保证服务不被杀死，1、使用startF这个前台服务！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！！
            startForeground(
                    NOTIFICATION_ID, // 通知ID，必须唯一
                    createForegroundNotification(),  // 创建前台通知
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE   // 14+需要明确指定“无障碍服务”类型
            );
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) { // Android 12-13（API 31-33）
            startForeground(NOTIFICATION_ID, createForegroundNotification()); // 旧版本不需要类型
        }

        // 添加测试Toast，延迟1000秒确认服务已启动后提示用户
        mainHandler.postDelayed(() -> {
            Toast.makeText(this, "划词翻译服务已启动", Toast.LENGTH_LONG).show();
        }, 1000);

    }

    // （2）服务连接：添加服务状态回调：用户在系统设置中开启的无障碍服务初始化完成时系统自动调用
    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        Log.d(TAG, "服务已连接,onServiceConnected 被调用！");

        // 设置服务配置对象：新建一个无障碍物服务配置信息类对象，用于定义服务的行为和能力
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();

        // 配置要监听的事件类型：只监听文本选择变化事件
        info.eventTypes = AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED;

        // 配置反馈类型：通用反馈，用于通知用户操作结果
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        
        // 配置通知超时时间：100毫秒，用于控制反馈提示的显示时间，避免短时间内收到过多重复事件
        info.notificationTimeout = 100;

        setServiceInfo(info);

        // 验证配置是否设置成功
        AccessibilityServiceInfo currentInfo = getServiceInfo();
        Log.d(TAG, "当前服务配置 - 事件类型: " + currentInfo.eventTypes);
        Log.d(TAG, "当前服务配置 - 包名限制: " + (currentInfo.packageNames == null ? "无限制" : "有限制"));
        Log.d(TAG, "当前服务配置 - 标志: " + currentInfo.flags);

        Toast.makeText(this, "服务配置完成,划词翻译已启动", Toast.LENGTH_SHORT).show();
    }

    //（3）事件处理
    //这是无障碍服务的核心回调方法，当系统中发生无障碍事件时，安卓系统会自动调用此方法，将事件传递给服务处理
    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // 接受系统发送的无障碍事件：添加事件来源信息
        Log.d(TAG, "收到无障碍事件 - 类型: " + event.getEventType() +
                ", 包名: " + event.getPackageName() +
                ", 类名: " + event.getClassName());

        // 过滤出文本选择事件：极简处理：只处理文本选择事件
        //触发时机：当用户在应用中选择了文本（如长按或拖动）时，系统会发送TYPE_VIEW_TEXT_SELECTION_CHANGED事件
        if (event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            Log.d(TAG, "处理文本选择事件 - 来源: " + event.getPackageName());

            //处理文本选择事件：提取选中文本并触发翻译
            handleTextSelection(event);
        }
    }

    //（4）文本选择处理：提取选中文本并触发翻译
    private void handleTextSelection(AccessibilityEvent event) {
        //无障碍节点信息类，表示界面上的一个UI元素，是该服务与界面交互的核心类
        //1.获取事件源节点（也就是用户选择的那个View）
        AccessibilityNodeInfo source = event.getSource();  //表示界面上的UI元素
        if (source == null) return;

        try {
            // 2.提取选中文本范围（起止索引，比如0什么的）
            int start = source.getTextSelectionStart();
            int end = source.getTextSelectionEnd();

            // 3.验证选中文本范围是否有效
            if (start >= 0 && end > start && source.getText() != null) {
                String fullText = source.getText().toString();
                if (fullText.length() >= end) {
                    // 4.提取选中的文本内容
                    //trim（）方法：空白字符（如空格、\t、\n）仅在字符串两端时被移除，中间部分保留。
                    String selectedText = fullText.substring(start, end).trim();  

                    // 5.验证选中文本长度是否符合要求
                    if (selectedText.length() >= 2 && selectedText.length() <= 100) {
                        Log.d(TAG, "选中文本: " + selectedText);
                        showSimpleToast("选中: " + selectedText); // 先用Toast测试

                        // ！！！重要：添加翻译调用 ！！！
                        //6.触发翻译（防抖）：避免用户选择范围内频繁触动翻译
                        currentSelectedText = selectedText;  //保存当前选中文本到全局变量中，在下面防抖对象的方法里要用到
                        mainHandler.removeCallbacks(debounceRunnable);  //移除之前的延迟任务，避免重复翻译，只翻译最后一次选择的结果
                        mainHandler.postDelayed(debounceRunnable, DEBOUNCE_DELAY);  //重新设置延迟任务500ms后执行
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "处理异常: " + e.getMessage());
        } finally {
            source.recycle(); // 必须回收节点信息，避免内存泄漏
        }
    }

    private void showSimpleToast(String message) {
        mainHandler.post(() -> {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        });
    }

    // 翻译选中的文本：在后台线程中执行翻译操作，处理网络请求、超时控制和异常处理，并在主线程中显示结果
    private void translateSelectedText(String text) {
        Log.d(TAG, "=== 开始翻译流程 ===");

        new Thread(() -> {
            try {
                Log.d(TAG, "开始真实翻译: " + text);

                //检查翻译服务的状态
                if (translationRepo == null) {
                    Log.e(TAG, "TranslationRepository 未初始化！");
                    mainHandler.post(() ->
                            Toast.makeText(TranslationAccessibilityService.this, "翻译服务未就绪", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

//                // Future 机制：异步执行翻译请求，主线程可以继续处理其他任务  ，04年的过时了
//                Future<String> future = translationRepo.translate(text);

                //调用Kotlin提供的Java兼容方法（返回CompletableFuture）!!!!!!!!!!!!!!!!!!!！！！！！！！！！================================================
                CompletableFuture<String> future=translationRepo.translateForJava(text);

                String result = future.get(10, TimeUnit.SECONDS); // 等待指定时间s，超时则抛出异常

                Log.d(TAG, "百度API返回结果(含缓存): " + (result != null ? result : "null"));

                //检查翻译结果是否有效，非空且非字符串
                if (result != null && !result.isEmpty()) {
                    showTranslationResult(result); // 显示弹窗
                } else {
                    mainHandler.post(() ->
                            Toast.makeText(TranslationAccessibilityService.this, "翻译失败，结果为空", Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (ExecutionException e) {  //一些异常处理 //翻译过程出错
                Log.e(TAG, "翻译执行异常: " + e.getMessage());
                mainHandler.post(() ->
                        Toast.makeText(TranslationAccessibilityService.this, "翻译失败: " + e.getCause().getMessage(), Toast.LENGTH_SHORT).show()
                );
            } catch (TimeoutException e) {  //翻译时间过长
                Log.e(TAG, "翻译超时");
                mainHandler.post(() ->
                        Toast.makeText(TranslationAccessibilityService.this, "翻译超时", Toast.LENGTH_SHORT).show()
                );
            } catch (InterruptedException e) {  //翻译线程被中断
                Log.e(TAG, "翻译被中断");
                Thread.currentThread().interrupt();
            }
        }).start();  //启动后台线程方法
    }

    // 显示翻译结果：在 主线程中显示翻译结果的弹窗 ，包括弹窗的初始化、位置计算和显示逻辑。
    private void showTranslationResult(String result) {
        Log.d(TAG, "=== 进入 showTranslationResult ===");

        mainHandler.post(() -> {  //将弹窗显示的代码切换到主线程，UI操作必须在主线程执行否则会抛出异常
            Log.d(TAG, "在主线程中执行显示");

            try {
                if (popupWindow == null) {
                    Log.d(TAG, "创建新的 popupWindow");
                    popupWindow = new TranslationPopupWindow(this);
                }

                //显示度量类：获取屏幕宽度和高度，用于弹窗位置计算
                DisplayMetrics metrics = getResources().getDisplayMetrics();
                int screenWidth = metrics.widthPixels;//屏幕宽度
                //int screenHeight = metrics.heightPixels;

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

    // 创建通知渠道，从安卓8.0开始必须创建通知渠道，而且前台服务必须有通知
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { //只在需要的版本上执行创建操作
            NotificationChannel channel = new NotificationChannel(  //通知渠道参数配置
                    CHANNEL_ID,  //渠道唯一标识
                    "翻译服务",  //用户可见的渠道名称
                    NotificationManager.IMPORTANCE_LOW  //通知重要程度，低 importance 表示通知对用户来说不是那么紧急（无声无震动）
            );

            //获取通知管理服务，该get方法会根据传入的Class对象类型，返回对应的系统服务实例
            NotificationManager manager = getSystemService(NotificationManager.class);  //JAVA的类字面量语法，是该get方法的现代调用方式
            if (manager != null) {
                manager.createNotificationChannel(channel);  //向系统注册渠道
            }
        }
    }

    // 创建一个符合前台服务要求的通知，用于向用户展示无障碍服务的运行状态
    private Notification createForegroundNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)  //创建通知构建器，指定上下文和渠道ID
                .setContentTitle("划词翻译已启动")  //通知标题
                .setContentText("长按选中文本即可翻译")  //通知内容
                .setSmallIcon(R.mipmap.ic_launcher_round)  //通知图标，使用应用图标
                .build();  //构建最终通知对象
    }

    //（6）服务解绑
    //当所有客户端断开与服务的连接时调用，用于清理资源和通知用户服务已关闭
    @Override
    public boolean onUnbind(Intent intent) { //参数是绑定服务时用的intent，用于判断是否是正常断开
        mainHandler.post(() ->
                Toast.makeText(this, "翻译服务已关闭，请重新开启", Toast.LENGTH_LONG).show()
        );
        return super.onUnbind(intent);
    }

    //（5）服务中断
    @Override
    public void onInterrupt() { Log.d(TAG, "服务被中断"); }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(translationRepo!=null){
            translationRepo.cancelAllCoroutines();   //如果关闭了无障碍服务，页面销毁，那就取消所有未完成的翻译协程
        }
    }
}