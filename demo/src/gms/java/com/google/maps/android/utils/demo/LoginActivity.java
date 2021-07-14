package com.google.maps.android.utils.demo;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.HashMap;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class LoginActivity extends AppCompatActivity {

    static Retrofit retrofit;
    static RetrofitInterface retrofitInterface;
    static String BASE_URL = "http://192.249.18.166/";

    private Button btn_log;
    private Button btn_reg;
    private EditText id;
    private EditText pw;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btn_log = findViewById(R.id.btn_log);
        btn_reg = findViewById(R.id.btn_reg);
        id = findViewById(R.id.et_id);
        pw = findViewById(R.id.et_pw);

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        retrofitInterface = retrofit.create(RetrofitInterface.class);

        btn_log.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Login();
            }
        });

        btn_reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void Login() {
        HashMap<String, String> map = new HashMap<>();

        map.put("id", id.getText().toString());
        map.put("password", pw.getText().toString());

        Call<LoginResult> call = retrofitInterface.executeLogin(map);

        call.enqueue(new Callback<LoginResult>() {
            @Override
            public void onResponse(Call<LoginResult> call, Response<LoginResult> response) {

                if (response.code() == 200) {

                    LoginResult result = response.body();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("id", result.getID());

                    Toast.makeText(getApplicationContext(), "로그인에 성공하였습니다.",
                            Toast.LENGTH_SHORT).show();

                    startActivity(intent);

                } else if (response.code() == 404) {
                    Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다.",
                            Toast.LENGTH_SHORT).show();

                }

            }

            @Override
            public void onFailure(Call<LoginResult> call, Throwable t) {
                Toast.makeText(getApplicationContext(), t.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


}
