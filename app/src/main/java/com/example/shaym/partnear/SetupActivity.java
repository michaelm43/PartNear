package com.example.shaym.partnear;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

import static com.example.shaym.partnear.Util.Constants.AVATAR;
import static com.example.shaym.partnear.Util.Constants.NAME;
import static com.example.shaym.partnear.Util.Constants.PHONE;
import static com.example.shaym.partnear.Util.Constants.PROFILE_PICTURE;
import static com.example.shaym.partnear.Util.Constants.collection_users;


public class SetupActivity extends AppCompatActivity {

    private CircleImageView imageProfile;
    private Uri mainImageURI = null;
    private static final int SELECTED_PIC=1;
    private Button saveButton;
    private TextView tvName;
    private TextView tvPhone;
    private TextView textView;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private String user_id;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        Toolbar setupToolbar = findViewById(R.id.setup_toolbar);
        tvName = findViewById(R.id.tv_name);
        tvPhone = findViewById(R.id.tv_phone);
        textView = findViewById(R.id.textview);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child(collection_users);

        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle(R.string.acc_setup);

        saveButton = findViewById(R.id.button_save);
        imageProfile = findViewById(R.id.setup_image);

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)

                    if(ContextCompat.checkSelfPermission(SetupActivity.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(SetupActivity.this,R.string.perm_denied, Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this ,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else {

                        Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent, SELECTED_PIC);
                        textView.setVisibility(View.VISIBLE);
                    }
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();

            }
        });

            mDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = String.valueOf(dataSnapshot.child(NAME).getValue());

                    tvName.setText(name);
                    tvPhone.setText(String.valueOf(dataSnapshot.child(PHONE).getValue()));

                    String imageUrl = String.valueOf(dataSnapshot.child(AVATAR).getValue());
                    if(!mDatabase.child(mAuth.getCurrentUser().getUid()).child(AVATAR).toString().equals("default")) {
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_profile_picture);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(imageUrl).into(imageProfile);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==SELECTED_PIC && resultCode == RESULT_OK && data != null && data.getData()!= null) {
            mainImageURI = data.getData();
            Glide.with(this).load(mainImageURI).into(imageProfile);
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
        if(mainImageURI !=null){
            final StorageReference image_path = mStorage.child(PROFILE_PICTURE).child(mAuth.getCurrentUser().getEmail()+"." + getFileExtension(mainImageURI));

        image_path.putFile(mainImageURI).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if(!task.isSuccessful()){
                    throw task.getException();
                }
                return image_path.getDownloadUrl();

            }
        })
                .addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if(task.isSuccessful()){

                            Toast.makeText(SetupActivity.this,R.string.upload_success ,Toast.LENGTH_LONG).show();
                            Uri downloadUri = task.getResult();

                            mDatabase.child(mAuth.getCurrentUser().getUid()).child(AVATAR).setValue(downloadUri.toString());

                        }
                    }
                });
            Intent saveIntent = new Intent(SetupActivity.this,MainActivity.class);
            SetupActivity.this.startActivity(saveIntent);

        }
        else{
            Toast.makeText(this,R.string.no_file, Toast.LENGTH_LONG).show();
        }
    }
}
