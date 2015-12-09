package com.itraveller;

/**
 * Created by rohan bundelkhandi on 11/29/2015.
 */
public interface Config {

    // CONSTANTS

    // When you are using two simulator for testing application.
    // Then Make SECOND_SIMULATOR value true when opening/installing application in second simulator
    // Actually we are validating/saving device data on IMEI basis.
    // if it is true IMEI number change for second simulator

    static final boolean SECOND_SIMULATOR = false;

    // Server Url absolute url where php files are placed.
    static final String YOUR_SERVER_URL   =  "http://192.168.2.3/android_chat_server/";

    // Google project id
    static final String GOOGLE_SENDER_ID = "132433516320";

    /**
     * Tag used on log messages.
     */
    static final String TAG = "GCM Android Example";

    // Broadcast reciever name to show gcm registration messages on screen
    static final String DISPLAY_REGISTRATION_MESSAGE_ACTION =
            "com.itraveller.DISPLAY_REGISTRATION_MESSAGE";

    // Broadcast reciever name to show user messages on screen
    static final String DISPLAY_MESSAGE_ACTION =
            "com.itraveller.DISPLAY_MESSAGE";

    // Parse server message with this name
    static final String EXTRA_MESSAGE = "message";


}
