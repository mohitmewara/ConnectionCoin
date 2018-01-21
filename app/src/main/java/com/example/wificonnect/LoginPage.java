package com.example.wificonnect;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginPage extends AppCompatActivity {
    private Button btn1;
    Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_page);
        btn1 = (Button) findViewById(R.id.user_button);
        context = this;
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText username = (EditText) findViewById(R.id.UserNameText);
                EditText password = (EditText) findViewById(R.id.PasswordText);
                if(username.getText().toString().equals("swamphacks18") && password.getText().toString().equals("swamphacks")){
                    Intent intent = new Intent(context, MainActivity.class);
                    startActivity(intent);
                }else{
                    Toast.makeText(LoginPage.this,"Invalid username or password",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
