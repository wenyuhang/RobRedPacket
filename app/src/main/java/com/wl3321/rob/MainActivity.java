package com.wl3321.rob;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.wl3321.rob.pojo.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button tvStatus;
    private TextView tvContent;

    private StringBuffer logBuffer = new StringBuffer();

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 9999:
                    //日志输出
                    if (null!=tvContent){
                        tvContent.setText(logBuffer.toString());
                    }
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //初始化EventBus
        EventBus.getDefault().register(this);
        //初始化控件
        initView();
    }

    /**
     * 初始化控件
     */
    private void initView() {
        tvStatus = findViewById(R.id.tv_status);
        tvContent = findViewById(R.id.tv_content);
        final Switch aSwitch = findViewById(R.id.switch1);
        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isStartAccessibilityService(MainActivity.this)) {
                    if (isChecked) {
                        Toast.makeText(MainActivity.this, "快速模式已开启", Toast.LENGTH_SHORT).show();
                        appendStr("快速模式已开启");
                    } else {
                        Toast.makeText(MainActivity.this, "快速模式已关闭", Toast.LENGTH_SHORT).show();
                        appendStr("快速模式已关闭");
                    }
                    //快速抢红包模式
                    MessageEvent event = MessageEvent.getInstance();
                    event.setCode(1001);
                    event.setObj(isChecked);
                    EventBus.getDefault().post(event);
                }else {
                    Toast.makeText(MainActivity.this, "请先启动服务", Toast.LENGTH_SHORT).show();
                    aSwitch.setChecked(false);
                }
            }
        });
    }

    /**
     * 启动服务
     *
     * @param view
     */
    public void btnStart(View view) {
        boolean isWeChetServiceOpen = isStartAccessibilityService(this);
        if (isWeChetServiceOpen) {
            Toast.makeText(this, "服务已启动请勿重复启动", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "无障碍权限未开启", Toast.LENGTH_SHORT).show();
            appendStr("启动插件服务");
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }

    private void appendStr(String value) {
        logBuffer.append(value).append("\n");
        handler.sendEmptyMessage(9999);
    }

    /**
     * 是否开启
     *
     * @param context
     * @return
     */
    public boolean isStartAccessibilityService(Context context) {
        String name = ".RobRPService";
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> serviceInfos = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : serviceInfos) {
            String id = info.getId();
            if (id.contains(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取事件订阅  快速模式 code          message-code = 1001
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getControll(MessageEvent event){
        switch (event.getCode()){
            //启动状态
            case 2001:
                appendStr((String) event.getObj());
                if (null!=tvStatus){
                    tvStatus.setText("服务已启动");
                }
                break;
            //销毁状态 使用需要重新启动
            case 2002:
                appendStr((String) event.getObj());
                if (null!=tvStatus){
                    tvStatus.setText("启动服务");
                }
                break;
            //中断状态 使用需要中断
            case 2003:
                appendStr((String) event.getObj());
                if (null!=tvStatus){
                    tvStatus.setText("服务中断");
                }
                break;
        }
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        //解绑EventBus
        EventBus.getDefault().unregister(this);
    }
}