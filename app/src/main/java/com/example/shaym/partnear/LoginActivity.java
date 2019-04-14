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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {


    private EditText etUserName;
    private EditText etPSW;
    private TextView tvRegister;
    private Button bLogin;
    private ProgressBar logginProgress;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        etUserName = (EditText) findViewById(R.id.etUserName);
        etPSW = (EditText) findViewById(R.id.etPSW);
        tvRegister = (TextView) findViewById(R.id.tvRegister);
        bLogin = (Button) findViewById(R.id.bLogin);
        logginProgress = (ProgressBar)findViewById(R.id.login_progress);


        bLogin.setOnClickListener(new View.OnClickListener() { //TODO add progress bar (Part2 16:20)
            @Override
            public void onClick(View view) {

                String loginEmail = etUserName.getText().toString();
                String loginPsw = etPSW.getText().toString();

                if(!TextUtils.isEmpty(loginEmail)&& !TextUtils.isEmpty(loginPsw)){
                    logginProgress.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(loginEmail,loginPsw).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendToMain();
                            }
                            else{
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(LoginActivity.this,R.string.error + errorMessage,Toast.LENGTH_LONG).show();

                            }
                            logginProgress.setVisibility(View.INVISIBLE);
                        }
                    });
                    Intent intent = new Intent(LoginActivity.this,MainActivity.class);
                    LoginActivity.this.startActivity(intent);
                }

                else
                    Toast.makeText(LoginActivity.this, R.string.login_fail ,Toast.LENGTH_LONG).show();
            }
        });


        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);

                LoginActivity.this.startActivity(intent);
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
        Intent mainIntent = new Intent(LoginActivity.this,MainActivity.class);
        LoginActivity.this.startActivity(mainIntent);
        finish();
    }
}
