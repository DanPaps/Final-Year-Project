package paps.bookman.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import paps.bookman.R;
import paps.bookman.ui.ReminderActivity;

/**
 * Daniel Pappoe
 * bookman-android
 */

public class RingtonePlayingService extends Service {

    MediaPlayer media_song;
    int startId;
    boolean isRunning;



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("Lacal Service","Received start id" + startId + ": " + intent);


        //fetch the extra string values
        String state = intent.getExtras().getString("extra");

        Log.e("RingtoneState:extra is" ,state);






        //this converts the extra strings from the intent to
        //to start ids to 0 or 1
        assert state != null;
        switch (state) {
            case "alarm on":
                startId = 1;
                break;
            case "alarm off":
                startId = 0;
                break;
            default:
                startId = 0;
                break;
        }

        //if else statements

        //if the music is playing and the user press the alarm on
        //music should start playing
        if (!this.isRunning && startId == 1){
            Log.e("there is no music","and you want start");

            //create an instance of the media player
            media_song = MediaPlayer.create(this, R.raw.cityofthefallen);
            //start the ringtone
            media_song.start();

            this.isRunning = true;
            this.startId = 0;

            //put the notification here and test it out
            // This is setting up the notification for the application
            //notification

            //set up the notification service
            NotificationManager notify_manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            //set up an intent to go to the main activity
            Intent i = new Intent(this.getApplicationContext(), ReminderActivity.class);
            //set up a pending intent
            PendingIntent pending_intent_main_activity = PendingIntent.getActivity(this,0,
                    i,0);

            //make the notification parameters
            Notification notification_popup = new Notification.Builder(this)
                    .setContentTitle("Ghotel Reminder is going off")
                    .setContentText("Click me!")
                    .setSmallIcon(R.drawable.ic_hotel_black_24dp)
                    .setContentIntent(pending_intent_main_activity)
                    .setAutoCancel(true)
                    .build();

            //set up the notification call command
            notify_manager.notify(0,notification_popup);

        }
        //if the music is playing and the user press the alarm off
        //music should stop
        else if (this.isRunning && startId == 0){
            Log.e("there is music","and you want end");

            //stop the ringtone
            media_song.stop();
            media_song.reset();

            this.isRunning = false;
            this.startId =0;


        }
        // these are when the user presses the button randomly
        //just to bug-proof the app
        //if there is no music playing and the user presses the alarm off button
        //do nothing
        else if (!this.isRunning && startId == 0){
            Log.e("there is no music","and you want end");
            this.isRunning = false;
            this.startId =0;


        }
        //if there is music playing and the user presses the alarm on button
        //do nothing
        else if (this.isRunning && startId == 1){
            Log.e("there is  music","and you want start");

            this.isRunning = true;
            this.startId =1;

        }
        //can't think of anything else, just to catch the odd event
        else {
            Log.e("else","somehow you reached this");


        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        //Tell the user we stopped
        Log.e("on destroy called", "you quit the app");
        super.onDestroy();
        this.isRunning =false;
    }
}
