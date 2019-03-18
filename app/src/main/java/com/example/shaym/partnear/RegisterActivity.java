package com.example.shaym.partnear;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {

    private EditText etAge;
    private EditText etEmail;
    private EditText etFName;
    private EditText etLname;
    private Switch sGender;
    private EditText etPSW1;
    private EditText etPSW2;
    private Button bRegister;
    private ProgressBar regProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        etAge = (EditText) findViewById(R.id.etDate);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etFName = (EditText) findViewById(R.id.etFName);
        etLname = (EditText) findViewById(R.id.etLName);
        sGender = (Switch) findViewById(R.id.sGender);
        etPSW1 = (EditText) findViewById(R.id.etPSW1);
        etPSW2 = (EditText) findViewById(R.id.etPSW2);

        bRegister = (Button) findViewById(R.id.bRegister);
        regProgress = (ProgressBar)findViewById(R.id.reg_progress);

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String email = etEmail.getText().toString();
                String pass = etPSW1.getText().toString();
                String confirmPass = etPSW2.getText().toString();

                if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmPass)){

                    if(pass.equals(confirmPass)){

                        regProgress.setVisibility(View.VISIBLE);
                        mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()){

                                    Intent setupIntent = new Intent(RegisterActivity.this,SetupActivity.class);
                                    RegisterActivity.this.startActivity(setupIntent);

                                    finish();
                                }
                                else{

                                    String errorMessage = task.getException().getMessage();
                                    Toast.makeText(RegisterActivity.this,"Error : "+ errorMessage ,Toast.LENGTH_LONG).show();
                                }
                                regProgress.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                    else{

                        Toast.makeText(RegisterActivity.this,"The confirm password and the password are not match",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent intent = new Intent(RegisterActivity.this,MainActivity.class);
        RegisterActivity.this.startActivity(intent);
        finish();
    }
}
