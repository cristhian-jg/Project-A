package com.example.projecta.loginin.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.projecta.loginin.EditProfileActivity;
import com.example.projecta.init.LoginActivity;
import com.example.projecta.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Fragmento que muestra la información del usuario actualmente conectado, además de
 * permiterle ajustar su perfil.
 */
public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";

    private FirebaseAuth fAuth;
    private FirebaseFirestore fFirestore;
    private FirebaseUser fUser;
    private StorageReference sReference;

    private Button btnSignOut;
    private Button btnResetPassword;
    private Button btnChangeProfile;

    private TextView tvName;
    private TextView tvEmail;
    private TextView tvPhone;

    private CircleImageView civAvatar;

    private View v;
    private String sUserID;

    public ProfileFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_profile, container, false);

        btnSignOut = v.findViewById(R.id.btnSignOut);
        btnResetPassword = v.findViewById(R.id.btnResetPassword);
        btnChangeProfile = v.findViewById(R.id.btnChangeProfile);

        fAuth = FirebaseAuth.getInstance();
        fFirestore = FirebaseFirestore.getInstance();
        fUser = fAuth.getCurrentUser();
        sReference = FirebaseStorage.getInstance().getReference();

        sUserID = fAuth.getCurrentUser().getUid();

        tvName = v.findViewById(R.id.tvName);
        tvEmail = v.findViewById(R.id.tvEmail);
        tvPhone = v.findViewById(R.id.tvPhone);

        civAvatar = v.findViewById(R.id.civAvatar);

        // Mediante Firebase Storage trabajo con las fotos de perfil, tengo una base de datos
        // de imagenes donde cada usuario tiene asignada su foto de perfil, anteriormente todos
        // los usuarios tenían la misma imagen, pero eso lo he solucionado almacenando una diferente en diferentes carpetas
        // para cada usuario referenciado mediante su UID.
        final StorageReference profileReference = sReference.child("users/"+ fAuth.getCurrentUser().getUid() +"/profile.jpg");

        // Descarga la imagen de Firebase y la carga en el correspondiente ImageView. (Utilizo CircleImageView así que automaticamente
        // ) convertirá que se seleccione en un circulo y no se descuadrará.
        profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(civAvatar);
            }
        });

        // Mediante Firebase Firestore obtengo la información del usuario actual y
        // los muestro en los pertinentes TextView
        DocumentReference documentReference = fFirestore.collection("users").document(sUserID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                tvPhone.setText(documentSnapshot.getString("telefono"));
                tvName.setText(documentSnapshot.getString("name"));
                tvEmail.setText(documentSnapshot.getString("email"));
            }
        });

        // Mediante este botón el usuario podrá cerrar su sesión, mientras no cierre la sesión
        // el usuario permanecerá conectado aunque cierre la aplicación.
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fAuth.signOut();
                startActivity(new Intent(getContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                getActivity().finish();
            }
        });

        // La misma opción vista en la ventana de Login, por si el usuario quiere cambiar su
        // contraseña de manera voluntaria.
        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText resetPassword = new EditText(v.getContext());

                AlertDialog.Builder passwordResetDialog = new AlertDialog.Builder(v.getContext());
                passwordResetDialog.setTitle("Cambiar mi contraseña");
                passwordResetDialog.setMessage("Introduzca su nueva contraseña: ");
                passwordResetDialog.setView(resetPassword);

                passwordResetDialog.setPositiveButton("Enviar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        String newPassword = resetPassword.getText().toString();
                        fUser.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Toast.makeText(getContext(), "Contraseña cambiada correctamente.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getContext(), "Hubo un error al cambiar la contraseña.", Toast.LENGTH_SHORT).show();
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

        // Abre una nueva actividad donde podremos cambiar la información de la cuenta, necesito
        // pasarle la información actual mediante Intent para mostrarla en los EditText de dicha actividad.
        btnChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(v.getContext(), EditProfileActivity.class);
                i.putExtra("name", tvName.getText().toString());
                i.putExtra("email", tvEmail.getText().toString());
                i.putExtra("phone", tvPhone.getText().toString());
                startActivity(i);
            }
        });

        return v;
    }

}
