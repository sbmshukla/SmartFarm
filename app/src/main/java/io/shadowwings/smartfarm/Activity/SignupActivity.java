package io.shadowwings.smartfarm.Activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import io.shadowwings.smartfarm.InterfaceAPI;
import io.shadowwings.smartfarm.R;
import io.shadowwings.smartfarm.RetrofitClientInstance;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SignupActivity extends AppCompatActivity {

    RelativeLayout BTN_LOGIN, BTN_HELP;
    CardView BTN_SIGNUP; DatabaseReference ROOT;
    EditText ET_EMAIL, ET_NAME, ET_PHONE, ET_PASSWORD;
    String sEmail = "", sName = "", sPhone = "", sPassword = "";
    ProgressBar PROGRESS;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_signup);
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.GLARE_WHITE));
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        init();

    }

    public void init(){
        BTN_HELP = findViewById(R.id.BTN_HELP);
        BTN_SIGNUP = findViewById(R.id.BTN_SIGNUP);
        BTN_LOGIN = findViewById(R.id.BTN_LOGIN);
        ET_EMAIL = findViewById(R.id.ET_EMAIL);
        ET_NAME = findViewById(R.id.ET_NAME);
        ET_PHONE = findViewById(R.id.ET_PHONE);
        ET_PASSWORD = findViewById(R.id.ET_PASSWORD);
        PROGRESS = findViewById(R.id.PROGRESS);
        PROGRESS.setVisibility(View.GONE);
        ROOT = FirebaseDatabase.getInstance("https://smart-farm-c0b94-default-rtdb.firebaseio.com/").getReference();


        onClick();
    }

    public void onClick(){
        BTN_LOGIN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignupActivity.this, LoginActivity.class));
            }
        });

        BTN_SIGNUP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String authToken=createAuthToken(sPhone,sEmail);
                checkSignUpDetails(authToken);
                CHECK_INPUTS();

            }
        });
    }

    public void CHECK_INPUTS(){
        sEmail = ET_EMAIL.getText().toString();
        sName = ET_NAME.getText().toString();
        sPhone = ET_PHONE.getText().toString();
        sPassword = ET_PASSWORD.getText().toString();
//        if (sEmail.equals("") || sName.equals("") || sPhone.equals("") || sPassword.equals("")){
//            Toast.makeText(this, "Please enter required information", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if(sPhone.length() != 10){
//            Toast.makeText(this, "Phone number must be of 10 digit", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        if(sPassword.length() < 8){
//            Toast.makeText(this, "Password must greater than 8 character", Toast.LENGTH_SHORT).show();
//            return;
//        }

        PROGRESS.setVisibility(View.VISIBLE);
        CREATE_ACCOUNT();

    }

    private void checkSignUpDetails(String authToken) {
        Retrofit retrofit= RetrofitClientInstance.getRetrofitInstance();
        final InterfaceAPI api=retrofit.create(InterfaceAPI.class);
        Call<String> call=api.checkSignup(authToken);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.isSuccessful()){
                    assert response.body() != null;
                    if (response.body().matches("success"))
                    {
                        Toast.makeText(getApplicationContext(),"Already Exist",Toast.LENGTH_LONG).show();
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(),"!!!",Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e("TAF",t.toString());
                t.printStackTrace();
            }
        });
    }

    private String createAuthToken(String sPhone,String sEmail ) {
        byte [] data =new byte[0];
        data=(sPhone+":"+sEmail).getBytes(StandardCharsets.UTF_8);
        return  "Basic "+ android.util.Base64.encodeToString(data, android.util.Base64.NO_WRAP);
    }

    public void CREATE_ACCOUNT() {
        try {
            HashMap<String, String> MAP = new HashMap<String, String>();
            MAP.put("EMAIL", sEmail);
            MAP.put("NAME", sName);
            MAP.put("PHONE", sPhone);
            MAP.put("PASSWORD", sPassword);

            ROOT.child("USERS").child(sPhone).setValue(MAP).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    SharedPreferences.Editor editor = getSharedPreferences("SMART_FARM", MODE_PRIVATE).edit();
                    editor.putString("AUTH","true");
                    editor.putString("USERNAME",sPhone);
                    editor.apply();
                    Toast.makeText(SignupActivity.this, "Account Create Successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(SignupActivity.this, MainActivity.class));
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(SignupActivity.this, "Unknown error occurred", Toast.LENGTH_SHORT).show();
                    PROGRESS.setVisibility(View.GONE);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}