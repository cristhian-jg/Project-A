package com.example.projecta.loginin;

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

import com.example.projecta.R;
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

/**
 * Actividad que permitirá al usuario cambiar la
 * configuración de su perfil.
 */

public class EditProfileActivity extends AppCompatActivity {

    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private FirebaseUser fUser;
    private StorageReference sReference;

    private CircleImageView civAvatarConfig;

    private EditText etNameConfig;
    private EditText etEmailConfig;
    private EditText etPhoneConfig;

    private Button btnSave;

    private String sName;
    private String sEmail;
    private String sPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Obtengo los datos pasados desde la actividad anterior para posteriormente mostrarlos.
        Intent data = getIntent();
        sName = data.getStringExtra("name");
        sEmail = data.getStringExtra("email");
        sPhone = data.getStringExtra("phone");

        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        sReference = FirebaseStorage.getInstance().getReference();

        civAvatarConfig = findViewById(R.id.civAvatarConfig);

        etNameConfig = findViewById(R.id.etNameConfig);
        etEmailConfig = findViewById(R.id.etEmailConfig);
        etPhoneConfig = findViewById(R.id.etPhoneConfig);

        btnSave = findViewById(R.id.btnSave);

        // Escribo la información obtenida desde la otra actividad en los EditText.
        writeCurrentInformation(sName, sEmail, sPhone);

        // Utilizado en la actividad anterior, descarga la imagen desde el Storage
        // y la muestra en el ImageView.
        final StorageReference profileReference = sReference.child("users/"+ fAuth.getCurrentUser().getUid() +"/profile.jpg");
        profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(civAvatarConfig);
            }
        });

        // Al pulsar sobre la imagen el usuario tiene acceso a su galería de imagenes para elegir la
        // que quiera.
        civAvatarConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mediante un intent accedemos a la galería de imagenes y lo iniciamos con startActivityForResult() para
                // posteriormete hacer uso del metodo onActivityResult();
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

    /**
     * Activamos esta actividad al intentar cambiar la imagen de perfil, hará unas comprobaciones y
     * lo que hará es subirá la imagen que seleccionemos a FirebaseStorage mediante otro metodo.
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1000) {
            if (resultCode == Activity.RESULT_OK) {
                Uri imageURI = data.getData();
                uploadImageToStorage(imageURI);
            }
        }

    }

    /**
     * Metodo que permite subir imagenes a nuestro Firebase Storage.
     * @param imageURI
     */
    private void uploadImageToStorage(Uri imageURI) {
        final StorageReference fileReference = sReference.child("users/"+ fAuth.getCurrentUser().getUid()+"/profile.jpg");
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

    /**
     * Metodo que rellena con la información actual del usuario los EditText.
     * @param name
     * @param email
     * @param phone
     */
    public void writeCurrentInformation(String name, String email, String phone) {
        etNameConfig.setText(name);
        etEmailConfig.setText(email);
        etPhoneConfig.setText(phone);
    }
}
