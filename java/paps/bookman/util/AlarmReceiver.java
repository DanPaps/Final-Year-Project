package paps.bookman.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e("We are in the receiver", "Yay!");

        String extra = intent.getExtras().getString("extra");
        Log.e("What is the key?", extra);

        //Create an intent to the ringtone service
        Intent service = new Intent(context, RingtonePlayingService.class);

        //pass the extra activity form the mainActivity to the ringtone playing service
        service.putExtra("extra", extra);

        //Start the ringtone service
        context.startService(service);


    }
}
