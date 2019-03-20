package com.example.shaym.partnear;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.example.shaym.partnear.Logic.Upload;
import com.example.shaym.partnear.Logic.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;

public class RegisterActivity extends AppCompatActivity {

    private EditText etAge;
    private EditText etEmail;
    private EditText etFName;
    private EditText etLname;
    private RadioGroup radioGenderGroup;
    private RadioButton radionGenderButton;
    private EditText etPSW1;
    private EditText etPSW2;
    private EditText etPhone;
    private Button bRegister;
    private ProgressBar regProgress;

    private DatePickerDialog.OnDateSetListener mDateSetListener;

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
        radioGenderGroup = (RadioGroup) findViewById(R.id.radio_gender);
        etPhone = (EditText) findViewById(R.id.etPhone);

        etPSW1 = (EditText) findViewById(R.id.etPSW1);
        etPSW2 = (EditText) findViewById(R.id.etPSW2);

        bRegister = (Button) findViewById(R.id.bRegister);
        regProgress = (ProgressBar)findViewById(R.id.reg_progress);

        bRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });

        etAge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar calander = Calendar.getInstance();
                int year = calander.get(calander.YEAR);
                int month = calander.get(calander.MONTH);
                int day = calander.get(calander.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        RegisterActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year,month,day);
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                month = month + 1;

                String date = day + "/" + month + "/" +year;
                etAge.setText(date);
            }
        };
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



    //method that take care of registration!
    private void startRegister() {

        final String email = etEmail.getText().toString();
        final String pass = etPSW1.getText().toString();
        String confirmPass = etPSW2.getText().toString();

        int selectedRadioId = radioGenderGroup.getCheckedRadioButtonId();
        radionGenderButton = (RadioButton)findViewById(selectedRadioId);

        if(!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmPass)){

            if(pass.equals(confirmPass)){

                regProgress.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){

                            String fName = etFName.getText().toString().trim();
                            String lName = etLname.getText().toString().trim();
                            String phoneNumber = etPhone.getText().toString().trim();
                            String date = etAge.getText().toString().trim();


                            //save reg info in database
                            mAuth.signInWithEmailAndPassword(email,pass);

                            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("users");
                            DatabaseReference currentUserDB = mDatabase.child(mAuth.getCurrentUser().getUid());

                            //do all in string!
                           /* currentUserDB.child("name").setValue(fName + " " + lName);
                            currentUserDB.child("age").setValue(date);
                            currentUserDB.child("gender").setValue(radionGenderButton.getText());
                            currentUserDB.child("image").setValue("default");
                            currentUserDB.child("phone").setValue(phoneNumber);*/

                            Upload upload = new Upload(fName +" " + lName,email,phoneNumber,date,radionGenderButton.getText().toString());
                            currentUserDB.setValue(upload);

                            /*User user = new User((fName + " " + lName), email , email ,phoneNumber,"default",radionGenderButton.getText().toString());
                            String uploadId = mDatabase.push().getKey();
                            mDatabase.child(uploadId).setValue(user);*/

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
}
