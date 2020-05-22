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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase que permite registrar nuevos usuarios y guardarlos
 * en la base de datos de Firebase.
 */

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private FirebaseAuth fAuth;
    private FirebaseFirestore fFirestore;

    private ProgressBar progressBar;

    private TextView btnLoginSwap;
    private Button btnRegister;

    private EditText etName;
    private EditText etEmail;
    private EditText etPhone;
    private EditText etPassword;
    private EditText etPasswordConfirmation;

    private String sName;
    private String sEmail;
    private String sPhone;
    private String sPassword;
    private String sPasswordConfirmation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        fAuth = FirebaseAuth.getInstance();
        fFirestore = FirebaseFirestore.getInstance();

        etName = findViewById(R.id.etNameRegister);
        etEmail = findViewById(R.id.etEmailRegister);
        etPhone = findViewById(R.id.etPhoneRegister);
        etPassword = findViewById(R.id.etPasswordRegister);
        etPasswordConfirmation = findViewById(R.id.etPasswordConfirmation);

        btnRegister = findViewById(R.id.btnRegister);
        btnLoginSwap = findViewById(R.id.btnLoginSwap);

        progressBar = findViewById(R.id.progressBarRegister);

        // Al pulsar sobre "REGISTRARSE" registra al nuevo usuario y lo almacena en Firebase.
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Almaceno lo que ha ingresado el usuario en strings para poder
                // hacer posteriormente comprobaciones.
                sName = etName.getText().toString().trim();
                sEmail = etEmail.getText().toString().trim();
                sPassword = etPassword.getText().toString().trim();
                sPasswordConfirmation = etPasswordConfirmation.getText().toString().trim();
                sPhone = etPhone.getText().toString().trim();

                if (TextUtils.isEmpty(sEmail)) {
                    etEmail.setError("Es necesario ingresar un correo electrónico.");
                    return;
                }

                if (TextUtils.isEmpty(sPassword)) {
                    etPassword.setError("Es necesario ingresar una contraseña.");
                    return;
                }

                if (TextUtils.isEmpty(sPasswordConfirmation)) {
                    etPasswordConfirmation.setError("Es necesario confirmar la contraseña.");
                }

                if (sPassword.length() < 6) {
                    etPassword.setError("La contraseña debe contener más de 6 caracteres.");
                    return;
                }

                if (!sPassword.equals(sPasswordConfirmation)) {
                    etPasswordConfirmation.setError("La contraseñas no coinciden.");
                    return;
                }

                // Si no se incumple ninguna de las condiciones anteriores se muestra la progressBar
                // que hasta ahora ha estado oculta.
                progressBar.setVisibility(View.VISIBLE);

                // Mediante la instancia de Firebase Authentication creo un nuevo usuario con su
                // email y contraseña, y al completarse esta acción ejecuta lo siguiente.
                fAuth.createUserWithEmailAndPassword(sEmail, sPassword)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Si la tarea ha sido exitosa obtendrá el UID del usuario actual
                                // y añadirlo a la colección "Users" de Firestore, además de referenciarlo.
                                if (task.isSuccessful()) {
                                    String userID = fAuth.getCurrentUser().getUid();
                                    DocumentReference documentReference =
                                            fFirestore.collection("users").document(userID);

                                    // Con el usuario referenciado creo un HashMap para guardar la
                                    // información del usuario actual.
                                    Map<String, Object> map = new HashMap<>();
                                    map.put("name", sName);
                                    map.put("email", sEmail);
                                    map.put("phone", sPhone);
                                    map.put("password", sPassword);

                                    documentReference.set(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "onSuccess: Se ha guardado el perfil " +
                                                    "en la base de datos.");
                                        }
                                    });

                                    // Si ha ido bien se mostrará el siguiente mensaje y se
                                    // iniciará la actividad principal.
                                    Toast.makeText(RegisterActivity.this, "Usuario creado correctamente.",
                                            Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                } else {
                                    // En caso de que algo vaya mal mostrará un mensaje de error
                                    // y retirará/quitará la barra de progreso.
                                    Toast.makeText(RegisterActivity.this, task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }
        });

        // Si el usuario ya tiene una cuenta podrá cambiar a la ventana de
        // login pulsando en este botón.
        btnLoginSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finalizo esta actividad y arranco la actividad de Login pero limpiando las
                // actividades anteriores, pasa a estar primero en la pila de actividades.
                startActivity(new Intent(getApplicationContext(), LoginActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }
        });
    }
}
