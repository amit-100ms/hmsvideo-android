package live.hms.video;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by Karthikeyan NG
 * Copyright © 100ms.live. All rights reserved.
 * Initial login screen with user details
 */

public class LaunchActivity extends AppCompatActivity {
    private String TAG = "HMSMainActivity";
    private Button connectButton;
    private EditText roomIdEditText, userIdEditText, serverEditText;
    private String newToken ="";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launch);

        roomIdEditText = (EditText) findViewById(R.id.editTextRoom);
        userIdEditText = (EditText) findViewById(R.id.editTextUserName);
        serverEditText = (EditText) findViewById(R.id.editTextServer);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy =
                    new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        connectButton = (Button) findViewById(R.id.connect_button);
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    getNewToken();
            }
        });


    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev){
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_hmsvideo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            //Do your stuff here
            Intent callIntent = new Intent(LaunchActivity.this, SettingsActivity.class);
            callIntent.putExtra("from", "launchscreen");
            startActivity(callIntent);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(TAG, "onNewIntent call");
        setIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent appLinkIntent = getIntent();

        String appLinkAction = appLinkIntent.getAction();
        Uri appLinkData = appLinkIntent.getData();

        if(appLinkData!=null) {
            Log.v(TAG, "incoming URI: room:" + appLinkData.getQueryParameter("room") + " host: " + appLinkData.getHost());
            roomIdEditText.setText(appLinkData.getQueryParameter("room"));
            serverEditText.setText("wss://"+appLinkData.getHost()+"/ws");
        }
    }

    void getNewToken()
    {
        String resStr = "";

        if(roomIdEditText.getText().length() == 0) {
            Toast.makeText(getApplicationContext(), "Room id is a mandatory parameter", Toast.LENGTH_LONG).show();
        }

        // create your json here
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("room_id", roomIdEditText.getText().toString());
            jsonObject.put("user_name", userIdEditText.getText().toString());
            jsonObject.put("role", "Guest");
            jsonObject.put("env", serverEditText.getText().toString().split("\\.")[0].replace("wss://", "") );
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.v("HMSClient", jsonObject.toString());

        OkHttpClient client = new OkHttpClient();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        // put your json here
        RequestBody body = RequestBody.create(JSON, jsonObject.toString());

        Request request = new Request.Builder()
                .url(BuildConfig.TOKEN_ENDPOINT)
                .post(body)
                .build();

        Response response = null;
        JSONObject jsonObj = null;
        String  val = "";
        try {
            response = client.newCall(request).execute();
            resStr = response.body().string();
            Log.v("HMSClient", "token: "+resStr);

            jsonObj = new JSONObject(resStr);
            //Log.v("token", jsonObj.getString("token"));
            val= jsonObj.getString("token");

            // removeWorkingDialog();

            Intent callIntent = new Intent(LaunchActivity.this, VideoActivity.class);
            callIntent.putExtra("server", serverEditText.getText().toString());
            callIntent.putExtra("room", roomIdEditText.getText().toString());
            callIntent.putExtra("user", userIdEditText.getText().toString().length() == 0 ? "JohnDoe" : userIdEditText.getText().toString());
            callIntent.putExtra("auth_token", val);
            callIntent.putExtra("env", "others");
            //callIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(callIntent);

        } catch (Exception e) {
            // removeWorkingDialog();
            Toast.makeText(getApplicationContext(), "Error in receiving token", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
        // return val;
    }


    private ProgressDialog working_dialog;

    private void showWorkingDialog() {
        working_dialog = ProgressDialog.show(LaunchActivity.this, "","Working please wait...", true);
    }

    private void removeWorkingDialog() {
        if (working_dialog != null) {
            working_dialog.dismiss();
            working_dialog = null;
        }
    }


}