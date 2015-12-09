package com.itraveller;

/**
 * Created by rohan bundelkhandi on 11/29/2015.
 */
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.gcm.GcmListenerService;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ShowMessage extends AppCompatActivity {

    GCMIntentService gcmIntentService;
    // UI elements
    static EditText editText;
    // Send Message button
    Button btnSend;
    static ListView msgList;
    static ChatArrayAdapter chatArrayAdapter;

    Controller aController;
    String name;
    String message;
    String UserDeviceIMEI;
    String senderName;
    String senderIMEI;
    String receiverIMEI;
    String receiverName;
    ShowMessage activity = null;
    public static boolean isActive,isStop;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show_message);
        /******************* Intialize Database *************/
        DBAdapter.init(this);

        isActive=true;
        isStop=true;

        RemoveAllNotification();
        // Get Global Controller Class object
        // (see application tag in AndroidManifest.xml)
        aController = (Controller) getApplicationContext();

        gcmIntentService=new GCMIntentService();


        editText = (EditText) findViewById(R.id.chatText);
        btnSend    = (Button) findViewById(R.id.buttonSend);
        msgList=(ListView) findViewById(R.id.msg_list);

        chatArrayAdapter = new ChatArrayAdapter(getApplicationContext(), R.layout.activity_chat_singlemessage);
        msgList.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        msgList.setAdapter(chatArrayAdapter);

        Intent intent=getIntent();

        receiverName=intent.getStringExtra("name");
        receiverIMEI=intent.getStringExtra("imei");
        senderIMEI=intent.getStringExtra("sendfrom");

        UserDeviceIMEI=intent.getStringExtra("imei");

        Log.d("Sender IMEI",""+senderIMEI);
        Log.d("Receiver IMEI",""+receiverIMEI);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Itraveller Support Group Chat");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Check if Internet present
        if (!aController.isConnectingToInternet())
        {

            // Internet Connection is not present
            aController.showAlertDialog(ShowMessage.this, "Internet Connection Error", "Please connect to Internet connection", false);
            // stop executing code by return
            return;
        }

        senderName=""+DBAdapter.getUserName(senderIMEI);


        //    List<UserData> data = DBAdapter.getUserIMEIMessage(senderIMEI,receiverIMEI);
        List<UserData> data = DBAdapter.getUserMessage();

        for (UserData dt : data)
        {
            if(!dt.getMessage().equals("Registered on server."))
            {
                String date_time_str=dt.getTime();
                String date_time_arr[]=date_time_str.split(" ");
                if((dt.getName()).equals(senderName))
                {
                    chatArrayAdapter.add(new ChatMessage(false,""+"You" +" :"+"\n"+dt.getMessage()));
                }
                else
                {
                    chatArrayAdapter.add(new ChatMessage(true,""+dt.getName()+" :"+"\n"+dt.getMessage()));
                }
            }
        }

        int local_row_count=DBAdapter.getUserDataCount();

        Log.d("Number of rows testing",""+local_row_count);

        // WebServer Request URL to get new messages
        String serverURL = Config.YOUR_SERVER_URL+"fetch_data.php";
        new FetchFromServer().execute(serverURL,String.valueOf(local_row_count),"","");

/*        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (new FetchFromServer().getStatus() == AsyncTask.Status.RUNNING)
                    new FetchFromServer().cancel(true);
            }
        }, 10000);

*/
        msgList.setSelection(chatArrayAdapter.getCount() - 1);

    /*    chatArrayAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
            //    msgList.setSelection(chatArrayAdapter.getCount() - 1);
            }
        });
*/

        // Register custom Broadcast receiver to show messages on activity
//        registerReceiver(mHandleMessageReceiver, new IntentFilter(Config.DISPLAY_MESSAGE_ACTION));

        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    message = editText.getText().toString();

                    if(message.length() > 0) {
                        // WebServer Request URL to send message to device.
                        String serverURL = Config.YOUR_SERVER_URL + "sendpush.php";

                        new LongOperation().execute(serverURL, UserDeviceIMEI + "::" + senderIMEI, message.trim(), senderIMEI);

                        return true;
                    }
                    else
                    {
                        Toast.makeText(ShowMessage.this,"Can't send empty message",Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                return false;
            }
        });

        activity  = this;

        // Click event on Register button
        btnSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // Get data from EditText
                message = editText.getText().toString();

                if(message.length()>0) {
                    // WebServer Request URL to send message to device.
                    String serverURL = Config.YOUR_SERVER_URL + "sendpush.php";

                    new LongOperation().execute(serverURL, UserDeviceIMEI + "::" + senderIMEI, message.trim(), senderIMEI);

                }
                else
                {
                    Toast.makeText(ShowMessage.this,"Can't send empty message",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public  void addReceiverMessageList(final String sender,final String msg, final String date_time_str)
    {
        msgList.post(new Runnable() {
            public void run() {

                String date_time_arr[]=date_time_str.split(" ");
                chatArrayAdapter.add(new ChatMessage(true,sender+" :"+"\n"+ msg));
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        finish();
        Log.e("test", "onBackPressed");
    }

/*
    // Create a broadcast receiver to get message and show on screen
    private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String newMessage = intent.getExtras().getString(Config.EXTRA_MESSAGE);
        //    String newName = intent.getExtras().getString("name");
        //    String newIMEI = intent.getExtras().getString("imei");

            Log.i("GCMBroadcast","Broadcast called.");

            // Waking up mobile if it is sleeping
            aController.acquireWakeLock(getApplicationContext());

            Toast.makeText(getApplicationContext(),
                    "Got Message: " + newMessage,
                    Toast.LENGTH_LONG).show();


            // Releasing wake lock
            aController.releaseWakeLock();
        }
    };

*/    /*********** Send message *****************/
    public class LongOperation  extends AsyncTask<String, Void, String> {

        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(ShowMessage.this);
        String data  = "";
        int sizeData = 0;

        protected void onPreExecute() {
            //Start Progress Dialog (Message)

            Dialog.setMessage("Please wait..");
            Dialog.show();

        }

        // Call after onPreExecute method
        protected String doInBackground(String... params) {

            /************ Make Post Call To Web Server ***ReceiverName********/
            BufferedReader reader=null;
            String Content = "";
            // Send data
            try{

                // Defined URL  where to send data
                URL url = new URL(params[0]);

                // Set Request parameter
                if(!params[1].equals(""))
                    data +="&" + URLEncoder.encode("data1", "UTF-8") + "="+params[1].toString();
                if(!params[2].equals(""))
                    data +="&" + URLEncoder.encode("data2", "UTF-8") + "="+params[2].toString();
                if(!params[3].equals(""))
                    data +="&" + URLEncoder.encode("data3", "UTF-8") + "="+params[3].toString().trim();

                Log.d("Parameter test", "" + params[1] + "" + params[2] + "" + params[3].trim());

                // Send POST data request
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write( data );
                wr.flush();

                Log.d("Parameter test", "Request");
                // Get the server response
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + " ");
                }

                Log.d("Parameter test","Response");
                // Append Server Response To Content String
                Content = sb.toString();
            }
            catch(Exception ex)
            {
                Log.d("Parameter test","Exception");
                Error = ex.getMessage();
            }
            finally
            {
                try
                {

                    reader.close();
                }

                catch(Exception ex) {}
            }

            Log.d("Send message","hi");
            /*****************************************************/
            return Content;
        }

        protected void onPostExecute(String Result) {
            // NOTE: You can call UI Element here.

            // Close progress dialog
            Dialog.dismiss();

            if (Error != null)
            {
                Toast.makeText(getBaseContext(), "Error: "+Error, Toast.LENGTH_LONG).show();

            }
            else
            {

                UserData userdata = new UserData(1, senderName, senderIMEI, message,""+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
                // Show Response Json On Screen (activity)
                DBAdapter.addUserData(userdata);

                String date_time_str=""+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                String date_time_arr[]=date_time_str.split(" ");
                chatArrayAdapter.add(new ChatMessage(false, "You" + " :" + "\n" + message));
                editText.setText("");

                // WebServer Request URL to send message to device.
                String serverURL = Config.YOUR_SERVER_URL+"store_data.php";
                new SendToServer().execute(serverURL, senderIMEI, senderName, message.toString().trim());

                if (editText.getText().length() > 0) {
                    editText.setText(null);
                }

                Toast.makeText(getBaseContext(), "Message sent."+Result, Toast.LENGTH_LONG).show();

            }
        }
    }

    /*********** Upload Message To Server *****************/
    public class SendToServer extends AsyncTask<String, Void, String> {

        private String Error = null;
        String data  = "";

        protected void onPreExecute() {

        }

        // Call after onPreExecute method
        protected String doInBackground(String... params) {

            /************ Make Post Call To Web Server ***ReceiverName********/
            BufferedReader reader=null;
            String Content = "";
            // Send data
            try{

                // Defined URL  where to send data
                URL url = new URL(params[0]);

                // Set Request parameter
                if(!params[1].equals(""))
                    data +="&" + URLEncoder.encode("data1", "UTF-8") + "="+params[1].toString();
                if(!params[2].equals(""))
                    data +="&" + URLEncoder.encode("data2", "UTF-8") + "="+params[2].toString();
                if(!params[3].equals(""))
                    data +="&" + URLEncoder.encode("data3", "UTF-8") + "="+params[3].toString();

                Log.d("Parameter test",""+params[1].toString()+""+params[2].toString()+""+params[3].toString());

                // Send POST data request
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write( data );
                wr.flush();

                Log.d("Parameter test", "send request");

                // Get the server response
                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                Log.d("Paremeter test","Received Response");

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + " ");
                }

                // Append Server Response To Content String
                Content = sb.toString();
            }
            catch(Exception ex)
            {
                Error = ex.getMessage();
            }
            finally
            {
                try
                {

                    reader.close();
                }

                catch(Exception ex) {}
            }

            Log.d("Successfully Uploaded","");
            /*****************************************************/
            return Content;
        }

        protected void onPostExecute(String Result) {
            // NOTE: You can call UI Element here.

            if (Error != null)
            {
                //    Toast.makeText(getBaseContext(), "Error: "+Error, Toast.LENGTH_LONG).show();

            } else
            {
                //    Toast.makeText(getBaseContext(), "Uploaded to server."+Result, Toast.LENGTH_LONG).show();
            }
        }

    }


    // Class with extends AsyncTask class
    public class FetchFromServer  extends AsyncTask<String, Void, String> {

        // Required initialization

        //private final HttpClient Client = new DefaultHttpClient();
        // private Controller aController = null;
        private String Error = null;
        private ProgressDialog Dialog = new ProgressDialog(ShowMessage.this);
        String data ="";

        protected void onPreExecute() {
            // NOTE: You can call UI Element here.

            //Start Progress Dialog (Message)

            Dialog.setMessage("Loading new messages...");
            Dialog.show();

        }

        // Call after onPreExecute method
        protected String doInBackground(String... params) {

            /************ Make Post Call To Web Server ***********/
            BufferedReader reader=null;
            String Content = "";
            // Send data
            try{

                // Defined URL  where to send data
                URL url = new URL(params[0]);

                Log.d("Parameter test",""+params[1]);
                Log.d("Parameter test",""+params[2]);
                Log.d("Parameter test",""+params[3]);

                // Set Request parameter
                if(!params[1].equals(""))
                    data +="&" + URLEncoder.encode("data1", "UTF-8") + "="+params[1].toString();
                if(!params[2].equals(""))
                    data +="&" + URLEncoder.encode("data2", "UTF-8") + "="+params[2].toString();
                if(!params[3].equals(""))
                    data +="&" + URLEncoder.encode("data3", "UTF-8") + "="+params[3].toString();
                Log.i("GCM", data);

                Log.d("Fetch test","request");
                // Send POST data request
                URLConnection conn = url.openConnection();
                conn.setDoOutput(true);
                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write(data);
                wr.flush();

                Log.d("Fetch test", "response");
                // Get the server response

                reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line = null;

                // Read Server Response
                while((line = reader.readLine()) != null)
                {
                    // Append server response in string
                    sb.append(line + " ");
                }

                // Append Server Response To Content String
                Content = sb.toString();

            }
            catch(Exception ex)
            {
                Error = ex.getMessage();
            }
            finally
            {
                try
                {

                    reader.close();
                }

                catch(Exception ex) {}
            }

            Log.d("Fetch test", "" + Content);

            return Content;
        }

        protected void onPostExecute(String Content) {
            // NOTE: You can call UI Element here.

            // Close progress dialog
            Dialog.dismiss();

            if (Error != null) {

                Toast.makeText(ShowMessage.this,"Unable to fetch data from server",Toast.LENGTH_LONG).show();
            }
            else
            {

                // Show Response Json On Screen (activity)

                /****************** Start Parse Response JSON Data *************/
                aController.clearUserData();

                JSONObject jsonResponse;

                try {

                    // Creates a new JSONObject with name/value mappings from the JSON string.
                    jsonResponse = new JSONObject(Content);

                    // Returns the value mapped by name if it exists and is a JSONArray.
                    // Returns null otherwise.
                    JSONArray jsonMainNode = jsonResponse.optJSONArray("Android");

                    /*********** Process each JSON Node ************/

                    int lengthJsonArr = jsonMainNode.length();

                    for(int i=0; i < lengthJsonArr; i++)
                    {
                        /****** Get Object for each JSON node.***********/
                        JSONObject jsonChildNode = jsonMainNode.getJSONObject(i);

                        /******* Fetch node values **********/
                        String Name       = jsonChildNode.optString("name").toString();
                        String IMEI       =jsonChildNode.optString("imei").toString();
                        String Message       = jsonChildNode.optString("message").toString();
                        String Time_Stamp      = jsonChildNode.optString("time_stamp").toString();

                        Log.i("GCM test", "---" + Name);
                        Log.i("GCM test", "---" + IMEI);
                        Log.i("GCM test", "---" + Message);
                        Log.i("GCM test", "---" + Time_Stamp);

                        if(!Name.equals("Data not found."))
                        {
                            UserData userData = new UserData(1, Name, IMEI, Message, Time_Stamp);

                            DBAdapter.addUserData(userData);

                            String date_time_arr[]=Time_Stamp.split(" ");

                            if ((userData.getName()).equals(senderName))
                            {
                                chatArrayAdapter.add(new ChatMessage(false, "" + "You" + " :" + "\n" + userData.getMessage()));
                            }
                            else
                            {
                                chatArrayAdapter.add(new ChatMessage(true, "" + userData.getName() + " :" + "\n" + userData.getMessage()));
                                //    gcmIntentService.generateNotification(ShowMessage.this,""+userData.getName(),""+userData.getMessage(),""+userData.getIMEI());
                            }
                        }
                    }
                }
                catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }

    protected void onResume(){
        super.onResume();
        isActive=true;
        isStop=true;
        Log.d("State","Resume");
        RemoveAllNotification();
    }

    protected void onPause() {
        super.onPause();
        Log.d("State","Pause");
        isActive=false;
        isStop=true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("State","Stop");
        isActive=false;
        isStop=true;
    }

    public void RemoveAllNotification() {

        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.cancelAll();
    }

    @Override
    protected void onDestroy() {
  /*      try {
            // Unregister Broadcast Receiver
            unregisterReceiver(mHandleMessageReceiver);


        } catch (Exception e) {
            Log.e("UnRegister Receiver Error", "> " + e.getMessage());
        }
    */    super.onDestroy();
        Log.d("State","Destroy");
    }

}
