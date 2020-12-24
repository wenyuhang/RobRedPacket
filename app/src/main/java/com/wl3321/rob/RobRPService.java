package com.wl3321.rob;

import android.accessibilityservice.AccessibilityService;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import androidx.annotation.RequiresApi;

import com.wl3321.rob.pojo.MessageEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;



/**
 * author : WYH
 * e-mail : wenyuhang@qinjia001.com
 * date   : 2020/12/23 9:54
 * desc   : 无障碍服务
 */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class RobRPService extends AccessibilityService {

    public static final String TAG = "ROB-RobRPService";
    private AccessibilityNodeInfo root;

    //是否开启快速抢红包模式
    private boolean isQuicken = false;

    /**
     * 服务启动回调
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        EventBus.getDefault().register(this);
//        Log.d(TAG, "服务启动了");
        //服务已启动      message-code = 2001
        MessageEvent event = MessageEvent.getInstance();
        event.setCode(2001);
        event.setObj("红包插件服务运行中......");
        EventBus.getDefault().post(event);
    }

    //---------------------------  PAGE  ---------------------------//
    //微信首页包名
    public static final String LAUNCHER = "com.tencent.mm.ui.LauncherUI";
    //微信聊天（包含群聊和单聊）
    public static final String WXCHAT = "android.widget.LinearLayout";
    //微信红包页面
    public static final String WX_RP_UI = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyNotHookReceiveUI";
    //微信红包详情页
    public static final String WX_RP_DETAIL = "com.tencent.mm.plugin.luckymoney.ui.LuckyMoneyDetailUI";


    //---------------------------  ID  ---------------------------//
    //聊天标题
    public static final String CHAT_TITLE = "com.tencent.mm:id/gas";
    //微信红包标识_1  tv---(微信红包)
    public static final String RED_PACKET_FALG_1 = "com.tencent.mm:id/ra";
    //微信红包已被领取
    public static final String RED_PACKET_ALREADY = "com.tencent.mm:id/r0";
    //微信红包id
    public static final String RED_PACKET = "com.tencent.mm:id/aag";
    //红包弹框 button(开) id
    public static final String RED_PACKET_OPEN = "com.tencent.mm:id/den";
    //红包详情页 关闭按钮
    public static final String DETAIL_CLOSE = "com.tencent.mm:id/dn";

    /**
     * 监听窗口变化回调
     *
     * @param event
     */

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //接收事件
        int eventType = event.getEventType();
//        Log.d(TAG,"有事件触发了"+eventType);
        switch (eventType) {
            //通知栏事件
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
//                Log.d(TAG, "通知栏事件触发了");
                break;
            //检测到窗口变化
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                robRedPacket(event);
                break;
            //窗口内容发生变化了
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
//                Log.d(TAG, "窗口内容发生变化了");
                robRedPacket(event);
                break;
        }
    }

    /**
     * 抢红包
     * @param event
     */
    private void robRedPacket(AccessibilityEvent event) {
        root = getRootInActiveWindow();
        String className = event.getClassName().toString();
//        Log.d(TAG, "窗口变化了" + className);
        switch (className) {
            //进入微信首页
            case LAUNCHER:
                //判断是否是聊天页面
                if (checkChatPage()) {
                    //检测是否有未领取红包 并进行领取操作
                    checkHaveRenPacket();
                }
                break;
            //进入红包弹框页面
            case WX_RP_UI:
                List<AccessibilityNodeInfo> viewList = root.findAccessibilityNodeInfosByViewId(RED_PACKET_OPEN);
                if (!viewList.isEmpty()) {
                    viewList.get(viewList.size() - 1).performAction(AccessibilityNodeInfo.ACTION_CLICK);
                }
                break;
            //红包详情页
            case WX_RP_DETAIL:
//                        Log.d(TAG,"===>进入红包详情页");
                if (isQuicken){
                    List<AccessibilityNodeInfo> btnList = root.findAccessibilityNodeInfosByViewId(DETAIL_CLOSE);
                    if (!btnList.isEmpty()) {
                        btnList.get(btnList.size() - 1).getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    }
                }
                break;
            default:
                //判断是否是聊天页面
                if (checkChatPage()) {
                    //检测是否有未领取红包 并进行领取操作
                    checkHaveRenPacket();
                }
        }
    }

    /**
     * 查询是否有未领取红包
     *
     * @return 0(没有红包) 10(有未领取的红包) 20(有已领取的红包)
     */
    private void checkHaveRenPacket() {
        try {
//            //TextView 文案---微信红包
//            List<AccessibilityNodeInfo> flagViewOne = root.findAccessibilityNodeInfosByViewId(RED_PACKET_FALG_1);
//            //TextView 文案---已领取
//            List<AccessibilityNodeInfo> flagViewTwo = root.findAccessibilityNodeInfosByViewId(RED_PACKET_ALREADY);
            //微信红包
            List<AccessibilityNodeInfo> redPacket = root.findAccessibilityNodeInfosByViewId(RED_PACKET);
//            Log.d(TAG,"===>"+redPacket.size());
            for (AccessibilityNodeInfo nodeInfo : redPacket) {
                int childCount = nodeInfo.getChildCount();
                //有未领取的红包
                if (childCount == 1) {
                    //模拟点击领取红包
//                    Log.d(TAG,"===>模拟点击领取红包");
                    nodeInfo.getParent().performAction(AccessibilityNodeInfo.ACTION_CLICK);
                } else if (childCount == 2 && "已领取".equals(nodeInfo.getChild(1).getText().toString())) {
                    //有已领取的红包
//                    Log.d(TAG,"===>有已领取的红包");
                }
            }

        } catch (Exception e) {
        }

    }


    /**
     * 判断是首页 还是聊天页面
     *
     * @return true/聊天页面  false/首页
     */
    private boolean checkChatPage() {
        try {
            List<AccessibilityNodeInfo> viewList = root.findAccessibilityNodeInfosByViewId(CHAT_TITLE);
            if (!viewList.isEmpty()) {
                String text = viewList.get(0).getText().toString();
                if (!TextUtils.isEmpty(text)) {
                    return true;
                }
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    /**
     * 获取事件订阅  快速模式 code          message-code = 1001
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void getControll(MessageEvent event){
        if (1001==event.getCode()){
            //是否开启快速抢红包模式
            isQuicken = (boolean) event.getObj();
        }
    }

    /**
     * 服务中断回调
     */
    @Override
    public void onInterrupt() {
        //服务中断      message-code = 2003
        MessageEvent event = MessageEvent.getInstance();
        event.setCode(2003);
        event.setObj("红包插件服务已中断......");
        EventBus.getDefault().post(event);
        EventBus.getDefault().unregister(this);
    }

    /**
     * 服务销毁
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //服务已销毁      message-code = 2002
        MessageEvent event = MessageEvent.getInstance();
        event.setCode(2002);
        event.setObj("红包插件服务已销毁......");
        EventBus.getDefault().post(event);
        EventBus.getDefault().unregister(this);
    }
}
