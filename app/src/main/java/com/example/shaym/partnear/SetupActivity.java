package com.example.shaym.partnear;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

//import com.theartofdev.edmodo.cropper.CropImage;
//import com.theartofdev.edmodo.cropper.CropImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.shaym.partnear.Logic.Upload;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.net.URI;

import de.hdodenhof.circleimageview.CircleImageView;


public class SetupActivity extends AppCompatActivity {

    private CircleImageView imageProfile;
    private Uri mainImageURI = null;
    private static final int SELECTED_PIC=1;
    private Button saveButton;
    private TextView tvName;
    private TextView tvPhone;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private StorageReference mStorage;
    private DatabaseReference mDatabase;
    private String user_id;
    private ProgressBar setupProgress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);


        Toolbar setupToolbar = findViewById(R.id.setup_toolbar);
        tvName = findViewById(R.id.tv_name);
        tvPhone = findViewById(R.id.tv_phone);

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        mStorage = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("users");

        setSupportActionBar(setupToolbar);
        getSupportActionBar().setTitle("Account Setup");

        saveButton = findViewById(R.id.button_save);
        setupProgress = findViewById(R.id.setup_progressbar);
        imageProfile = findViewById(R.id.setup_image);

        imageProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)

                    if(ContextCompat.checkSelfPermission(SetupActivity.this , Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){

                        Toast.makeText(SetupActivity.this,"Permission Denied", Toast.LENGTH_LONG).show();
                        ActivityCompat.requestPermissions(SetupActivity.this ,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},1);
                    }
                    else{

                        Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(intent,SELECTED_PIC);
                    }
/*                    else
                        CropImage.activity()
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .start(SetupActivity.this);
                    }*/

            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();

            }
        });

        /*mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser() != null) {*/


            mDatabase.child(user_id).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String name = String.valueOf(dataSnapshot.child("name").getValue());

                    tvName.setText(name);
                    tvPhone.setText(String.valueOf(dataSnapshot.child("phone").getValue()));

                    String imageUrl = String.valueOf(dataSnapshot.child("avatar").getValue());
                    if(!mDatabase.child(mAuth.getCurrentUser().getUid()).child("avatar").toString().equals("default")) {
                        RequestOptions placeholderRequest = new RequestOptions();
                        placeholderRequest.placeholder(R.drawable.default_profile_picture);

                        Glide.with(SetupActivity.this).setDefaultRequestOptions(placeholderRequest).load(imageUrl).into(imageProfile);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

//                }
//            }
//        };

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode ==SELECTED_PIC && resultCode == RESULT_OK && data != null && data.getData()!= null) {
            mainImageURI = data.getData();
            Glide.with(this).load(mainImageURI).into(imageProfile);


            /* String[] projection = {MediaStore.Images.Media.DATA};
            Cursor cursor = getContentResolver().query(mainImageURI,projection,null,null,null);
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndex(projection[0]);
            String filepath = cursor.getString(columnIndex);

            Bitmap bitmap = BitmapFactory.decodeFile(filepath);
            Drawable drawable = new BitmapDrawable(bitmap);
            imageProfile.setImageDrawable(drawable);*/
        }
    }

    private String getFileExtension(Uri uri){
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void uploadFile(){
        if(mainImageURI !=null){
            final StorageReference image_path = mStorage.child("profile_picture").child(mAuth.getCurrentUser().getEmail()+"." + getFileExtension(mainImageURI));

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

                            Toast.makeText(SetupActivity.this,"Upload successful",Toast.LENGTH_LONG).show();
                            Uri downloadUri = task.getResult();

                            mDatabase.child(mAuth.getCurrentUser().getUid()).child("avatar").setValue(downloadUri.toString());
//                        Upload upload = new Upload(user_id.toString().trim(),taskSnapshot.getMetadata().getReference().getDownloadUrl().toString());
//                        String uploadId = mDatabase.push().getKey();
//                        mDatabase.child(uploadId).setValue(upload);

                        }
                    }
                });
            Intent saveIntent = new Intent(SetupActivity.this,MainActivity.class);
            SetupActivity.this.startActivity(saveIntent);

        }
        else{
            Toast.makeText(this,"No file selected", Toast.LENGTH_LONG).show();
        }
    }
}
