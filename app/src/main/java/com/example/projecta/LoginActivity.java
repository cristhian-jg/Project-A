package com.example.projecta;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
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

public class LoginActivity extends AppCompatActivity {

    private final static String TAG = "LoginActivity";

    private FirebaseAuth fAuth;

    private Button btnLogin;
    private TextView btnRegisterSwap;
    private TextView btnForgetPassword;

    private EditText etEmail;
    private EditText etPassword;

    private String email;
    private String password;

    private ProgressBar progressBarLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        fAuth = FirebaseAuth.getInstance();

        // Si el usuario ya está logueado lo redigirá a la actividad principal.
        if (fAuth.getCurrentUser() != null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        }

        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);

        btnLogin = findViewById(R.id.btnLogin);
        btnRegisterSwap = findViewById(R.id.tvRegisterButton);
        btnForgetPassword = findViewById(R.id.tvForgetButton);

        progressBarLogin = findViewById(R.id.progressBarLogin);

        // Al pulsar sobre este botón hace las comprobaciones pertinentes para comprobar
        // que el usuario ha ingresado los datos correctos en los EditText.
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    etEmail.setError("Es necesario ingresar un correo electrónico.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    etPassword.setError("Es necesario ingresar una contraseña.");
                    return;
                }

                if (password.length() < 6) {
                    etPassword.setError("La contraseña debe contener más de 6 caracteres.");
                    return;
                }

                // Hago la progressBar visible y mediante la instancia de Firebase Authentication
                // se intenta loguear al usuario.
                progressBarLogin.setVisibility(View.VISIBLE);

                // Al completarse la operación si la tarea es exitosa se redigirá al usuario a la
                // actividad principal limpiando la pila de activiades.
                fAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(LoginActivity.this, "Se ha iniciado sesión correctamente.",
                                    Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class)
                                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                            finish();
                        } else {
                            // En caso de que no sea exitosa se mostrará un mensaje de error y la
                            // barrá de progreso se ocultará de nuevo.
                            Toast.makeText(LoginActivity.this,
                                    task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            progressBarLogin.setVisibility(View.GONE);
                        }
                    }
                });
            }
        });


        // Al pulsar sobre este botón iniciará la nueva actividad para registrarse, limpiando
        // la pila de actividades.
        btnRegisterSwap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                finish();
            }
        });

        // Este botón servirá para que el usuario pueda recuperar su contraseña en caso de olvidarla.
        btnForgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final EditText resetPassword = new EditText(v.getContext());

                // He construido un dialogo de alerta que servirá para que el usuario indique el correo
                // del que desea recuperar la contraseña.
                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("He olvidado mi contraseña");
                passwordResetDialog.setMessage("Por favor, introduzca su correo eléctronico: ");
                passwordResetDialog.setView(resetPassword);

                // Además a este dialogo se le han añadido tanto un botón positivo y negativo, en el cual
                // se pulse hará una acción u otra.
                passwordResetDialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String mail = resetPassword.getText().toString();
                        // Mediante la instancia de Firebase Authentication ejecuto el metodo
                        // sendPasswordResetEmail() que lo que hará es enviar un correo a nuestro email
                        // donde podremos cambiar la contraseña.
                        fAuth.sendPasswordResetEmail(mail).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(LoginActivity.this, "Se ha enviado un correo al email indicado para cambiar tu contraseña", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(LoginActivity.this, "Algo salió mal, inténtelo de nuevo", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });

                passwordResetDialog.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                passwordResetDialog.create().show();

            }
        });
    }
}
