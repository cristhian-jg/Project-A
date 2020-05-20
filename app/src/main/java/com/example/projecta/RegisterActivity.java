package com.example.projecta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private EditText etName, etEmail, etTelefono, etPassword, etPasswordConfirmation;
    private Button btnRegister;
    private TextView tvLoginButton;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;

    private ProgressBar progressBar;

    private String name, email, password, passwordConfirmation, telefono;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etTelefono = findViewById(R.id.etTelefono);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirmation = findViewById(R.id.etPasswordConfirmation);

        btnRegister = findViewById(R.id.btnRegister);

        tvLoginButton = findViewById(R.id.tvLoginButton);

        progressBar = findViewById(R.id.progressBar);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                name = etName.getText().toString().trim();
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                passwordConfirmation = etPasswordConfirmation.getText().toString().trim();
                telefono = etTelefono.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    etEmail.setError("Es necesario ingresar un correo electrónico.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    etPassword.setError("Es necesario ingresar una contraseña.");
                    return;
                }

                if (TextUtils.isEmpty(passwordConfirmation)) {
                    etPasswordConfirmation.setError("Es necesario confirmar la contraseña.");
                }

                if (password.length() < 6) {
                    etPassword.setError("La contraseña debe contener más de 6 caracteres.");
                    return;
                }

                if (!password.equals(passwordConfirmation)) {
                    etPasswordConfirmation.setError("La contraseñas no coinciden.");
                    return;
                }

                progressBar.setVisibility(View.VISIBLE);

                firebaseAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    String userID = firebaseAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference = db.collection("users").document(userID);

                                    Map<String, Object> map = new HashMap<>();
                                    map.put("name", name);
                                    map.put("email", email);
                                    map.put("telefono", telefono);
                                    map.put("password", password);

                                    documentReference.set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: Se ha guradado el perfil " +
                                                    "en la base de datos.");
                                        }
                                    });

                                    Toast.makeText(RegisterActivity.this, "Usuario creado correctamente.",
                                            Toast.LENGTH_SHORT).show();

                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                } else {
                                    Toast.makeText(RegisterActivity.this, "Error has ocurred: " +
                                            task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });

        tvLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }
        });
    }
}
