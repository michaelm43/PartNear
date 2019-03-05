package com.example.shaym.partnear;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

public class RegisterActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText etAge = (EditText) findViewById(R.id.etDate);
        final EditText etEmail = (EditText) findViewById(R.id.etEmail);
        final EditText etFName = (EditText) findViewById(R.id.etFName);
        final EditText etLname = (EditText) findViewById(R.id.etLName);
        final Switch sGender = (Switch) findViewById(R.id.sGender);
        final EditText etPSW1 = (EditText) findViewById(R.id.etPSW1);
        final EditText etPSW2 = (EditText) findViewById(R.id.etPSW2);

        final Button bRegister = (Button) findViewById(R.id.bRegister);

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this,LoginActivity.class);

                RegisterActivity.this.startActivity(intent);
            }
        });

    }
}
