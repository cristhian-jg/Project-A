package com.example.projecta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private FirebaseUser fUser;
    private StorageReference storageReference;

    private CircleImageView civAvatarConfig;

    private EditText etNameConfig;
    private EditText etEmailConfig;
    private EditText etPhoneConfig;

    private Button btnSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        Intent data = getIntent();
        final String name = data.getStringExtra("nombre");
        String email = data.getStringExtra("email");
        String phone = data.getStringExtra("telefono");

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        civAvatarConfig = findViewById(R.id.civAvatarConfig);

        etNameConfig = findViewById(R.id.etNameConfig);
        etEmailConfig = findViewById(R.id.etEmailConfig);
        etPhoneConfig = findViewById(R.id.etPhoneConfig);

        btnSave = findViewById(R.id.btnSave);

        writeCurrentInformation(name, email, phone);

        final StorageReference profileReference = storageReference.child("users/"+ fAuth.getCurrentUser().getUid() +"/profile.jpg");
        profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(civAvatarConfig);
            }
        });

        civAvatarConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EditProfileActivity.this, "Cambiar foto perfil", Toast.LENGTH_SHORT).show();

                Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(openGalleryIntent, 1000);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String email = etEmailConfig.getText().toString();
                fUser.updateEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        DocumentReference documentReference = fStore.collection("users").document(fUser.getUid());

                        Map<String, Object> changes = new HashMap<>();
                        changes.put("email", email);
                        changes.put("name", etNameConfig.getText().toString());
                        changes.put("phone", etPhoneConfig.getText().toString());

                        documentReference.update(changes).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(EditProfileActivity.this, "Se ha actualizado la información correctamente.", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(intent);
                                finish();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(EditProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageURI = data.getData();
                uploadImageToFirebase(imageURI);
            }
        }

    }

    private void uploadImageToFirebase(Uri imageURI) {
        final StorageReference fileReference = storageReference.child("users/"+ fAuth.getCurrentUser().getUid()+"/profile.jpg");
        fileReference.putFile(imageURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                fileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(civAvatarConfig);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), "Hubo un error al subir la imagen.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void writeCurrentInformation(String name, String email, String phone) {
        etNameConfig.setText(name);
        etEmailConfig.setText(email);
        etPhoneConfig.setText(phone);
    }
}
