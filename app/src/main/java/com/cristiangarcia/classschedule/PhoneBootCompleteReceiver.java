package com.cristiangarcia.classschedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class PhoneBootCompleteReceiver extends BroadcastReceiver {

    public static boolean phoneBootSucessful = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
            phoneBootSucessful = true;
    }
}
