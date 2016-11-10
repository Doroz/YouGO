package com.shedulerforevents;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.shedulerforevents.model.Event;

/**
 * Created by Usuario on 02/11/2016.
 */

public class EventBroadcastReceiver  extends BroadcastReceiver{

    public static final String ACTION = "com.shedulerforevents.NOTIFICATION";

      @Override
    public void onReceive(Context context, Intent intent) {

          Event event = null;

                  if (intent.hasExtra("event")){
                      event = intent.getExtras().getParcelable("event");
                  }
          Intent notifIntent = new Intent(context,MainActivity.class);

          NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

          // Intent para disparar o broadcast
          PendingIntent p = PendingIntent.getActivity(context, 0, notifIntent, PendingIntent.FLAG_UPDATE_CURRENT);


          NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                  .setContentIntent(p)
                  .setContentTitle(event.getTitle())
                  .setContentText(event.getAddress())
                  .setSmallIcon(R.mipmap.ic_launcher)
                  .setAutoCancel(true);

          // Dispara a notification
          Notification n = builder.build();
          manager.notify(0  , n);


      }
}
