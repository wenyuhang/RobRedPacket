package com.wl3321.rob.pojo;

/**
 * author : WYH
 * e-mail : wenyuhang@qinjia001.com
 * date   : 2020/6/15 14:24
 * desc   : EventBus 消息体
 * version: 1.0.0
 */
public class MessageEvent {

    //控制器发送至服务
    //code  1001-----快速抢红包模式


    //服务发送至控制器
    //code  2001-----服务启动状态

    public static MessageEvent getInstance() {
        return new MessageEvent();
    }

    private int code ;
    private Object obj;

    public int getCode() {
        return code;
    }

    public Object getObj() {
        return obj;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
