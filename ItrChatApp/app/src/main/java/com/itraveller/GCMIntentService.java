package com.itraveller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GCMIntentService extends GCMBaseIntentService  {

    private static final String TAG = "GCMIntentService";

    private Controller aController = null;

    public static int count=0;

    ShowMessage showMessage;
    ChatArrayAdapter chatArrayAdapter;
    DBAdapter dbAdapter;
    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param      */
    public GCMIntentService() {
        super(Config.GOOGLE_SENDER_ID);
    }

    /**
     * Method called on device registered
     **/
    protected void onRegistered(Context context, String registrationId) {


        //Get Global Controller Class object (see application tag in AndroidManifest.xml)
        if(aController == null)
            aController = (Controller) getApplicationContext();

        Log.i(TAG, "---------- onRegistered -------------");
        Log.i(TAG, "Device registered: regId = " + registrationId);
        aController.displayRegistrationMessageOnScreen(context, "Your device registred with GCM");
        Log.d("NAME", MainActivity.name);

        aController.register(context, MainActivity.name, MainActivity.email, registrationId, MainActivity.imei);

        DBAdapter.addDeviceData(MainActivity.name, MainActivity.email, registrationId, MainActivity.imei);



    }

    /**
     * Method called on device unregistred
     * */
    protected void onUnregistered(Context context, String registrationId) {
        if(aController == null)
            aController = (Controller) getApplicationContext();
        Log.i(TAG, "---------- onUnregistered -------------");
        Log.i(TAG, "Device unregistered");
        aController.displayRegistrationMessageOnScreen(context, getString(R.string.gcm_unregistered));
        aController.unregister(context, registrationId, MainActivity.imei);
    }

    /**
     * Method called on Receiving a new message from GCM server
     * */
    protected void onMessage(Context context, Intent intent) {


        showMessage=new ShowMessage();

        Log.d("Page loading testing","hello");


        if(aController == null)
            aController = (Controller) getApplicationContext();

        Log.i(TAG, "---------- onMessage -------------");
        String message = intent.getExtras().getString("message");

        Log.i("GCM","message : "+message);

        String[] StringAll;
        StringAll = message.split("\\^");

        String title = "";
        String imei  = "";
        String to_imei="";

        int StringLength = StringAll.length;
        if (StringLength > 0) {

            title   = StringAll[0];
            imei    = StringAll[1];
            message = StringAll[2];

        /*    if(StringAll.length>3) {
                to_imei = StringAll[3];
                receiver_name=StringAll[4];
            }
     */   }

        Log.d("ToIMEI", "" + to_imei);
        // Call broadcast defined on ShowMessage.java to show message on ShowMessage.java screen
        aController.displayMessageOnScreen(context, title, message, imei);


        UserData userdata;

        userdata = new UserData(1, title, imei, message,""+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));

        DBAdapter.addUserData(userdata);

        if(message.equals("Registered on server."))
            Toast.makeText(GCMIntentService.this, ""+message, Toast.LENGTH_SHORT).show();
        else
        {
            Log.d("State","Active"+ShowMessage.isActive);
            if(ShowMessage.isStop)
            showMessage.addReceiverMessageList(title, message, "" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        }

        // generate notification to notify user
            generateNotification(context, title,message,imei);

    }

    /**
     * Method called on receiving a deleted message
     * */
    protected void onDeletedMessages(Context context, int total) {

        if(aController == null)
            aController = (Controller) getApplicationContext();

        Log.i(TAG, "---------- onDeletedMessages -------------");
        String message = getString(R.string.gcm_deleted, total);

        String title = "DELETED";
        // aController.displayMessageOnScreen(context, message);

        // generate notification to notify user
        generateNotification(context, title, message, "");
    }

    /**
     * Method called on Error
     * */
    public void onError(Context context, String errorId) {

        if(aController == null)
            aController = (Controller) getApplicationContext();

        Log.i(TAG, "---------- onError -------------");
        Log.i(TAG, "Received error: " + errorId);

        aController.displayRegistrationMessageOnScreen(context, getString(R.string.gcm_error, errorId));
    }

    /**
     * Create a notification to inform the user that server has sent a message.
     */
    public void generateNotification(Context context, String title, String message, String imei) {
        int icon = R.drawable.user_thumb;
        long when = System.currentTimeMillis();

        if(!(ShowMessage.isActive)) {

            NotificationManager notificationManager = (NotificationManager)
                    context.getSystemService(NOTIFICATION_SERVICE);

            Notification notification = new Notification(icon, message, when);

            Intent notificationIntent = new Intent(context, ShowMessage.class);

            TaskStackBuilder stackBuilder = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                stackBuilder = TaskStackBuilder.create(this);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                stackBuilder.addNextIntent(notificationIntent);
            }
            // set intent so it does not start a new activity
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

            PendingIntent resultPendingIntent = null;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                resultPendingIntent = stackBuilder.getPendingIntent(0,
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }

            DBAdapter.init(this);

            dbAdapter = new DBAdapter();

            notification = builder.setContentIntent(resultPendingIntent)
                    .setSmallIcon(icon).setTicker(message).setWhen(when)
                    .setAutoCancel(true).setContentTitle(title)
                    .setContentText(message).build();

//            notification.number=++count;
            //    notification.setLatestEventInfo(context, title, message, intent);
            notification.flags |= Notification.FLAG_AUTO_CANCEL;

            // Play default notification sound
            notification.defaults |= Notification.DEFAULT_SOUND;

            // Vibrate if vibrate is enabled
            notification.defaults |= Notification.DEFAULT_VIBRATE;

            notificationManager.notify(0, notification);
        }
        else
        {
            try {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}