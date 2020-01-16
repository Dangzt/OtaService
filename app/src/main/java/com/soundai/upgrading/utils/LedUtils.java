package com.soundai.upgrading.utils;

import android.content.Context;
import android.content.Intent;

public class LedUtils {
    public static final String LED_EVENT = "com.soundai.ledcontroler.LED_EVENT";
    public static final String EXTRA_EVENT_TYPE = "EVENT_TYPE";
    public static final String EXTRA_EVENT_ACTION = "EVENT_ACTION";
    public static final String EVENT_ACTION_ADD = "EVENT_ACTION_ADD";
    public static final String EVENT_ACTION_REMOVE = "EVENT_ACTION_REMOVE";
    public static void  sendEvent(Context context,String action, String event){
        Intent intent = new Intent(LED_EVENT);
        intent.putExtra(EXTRA_EVENT_ACTION,action);
        intent.putExtra(EXTRA_EVENT_TYPE,event);
        context.sendBroadcast(intent,"com.soundai.ledcontroler.receive_led_event");
    }
}
