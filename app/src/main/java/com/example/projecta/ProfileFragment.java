package com.example.projecta;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.concurrent.Executor;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ProfileFragment extends Fragment {

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore db;
    private StorageReference storageReference;

    private Button btnSignOut, btnResetPassword, btnChangeProfile;
    private TextView tvNombre, tvEmail, tvTelefono;
    private CircleImageView ivProfile;

    private View fragmentView;
    private FirebaseUser user;
    private String userID;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        fragmentView = inflater.inflate(R.layout.fragment_profile, container, false);

        btnSignOut = fragmentView.findViewById(R.id.btnSignOut);
        btnResetPassword = fragmentView.findViewById(R.id.btnResetPassword);
        btnChangeProfile = fragmentView.findViewById(R.id.btnChangeProfileImage);

        firebaseAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = firebaseAuth.getCurrentUser();
        storageReference = FirebaseStorage.getInstance().getReference();

        userID = firebaseAuth.getCurrentUser().getUid();

        tvNombre = fragmentView.findViewById(R.id.tvNombre);
        tvEmail = fragmentView.findViewById(R.id.tvEmail);
        tvTelefono = fragmentView.findViewById(R.id.tvTelefono);

        ivProfile = fragmentView.findViewById(R.id.civAvatarConfig);

        final StorageReference profileReference = storageReference.child("users/"+ firebaseAuth.getCurrentUser().getUid() +"/profile.jpg");
        profileReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(ivProfile);
            }
        });

        DocumentReference documentReference = db.collection("users").document(userID);
        documentReference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                tvTelefono.setText(documentSnapshot.getString("telefono"));
                tvNombre.setText(documentSnapshot.getString("name"));
                tvEmail.setText(documentSnapshot.getString("email"));
            }
        });

        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseAuth.signOut();
                startActivity(new Intent(getContext(), LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
                getActivity().finish();
            }
        });

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
                        user.updatePassword(newPassword).addOnSuccessListener(new OnSuccessListener<Void>() {
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

        btnChangeProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent i = new Intent(v.getContext(), EditProfileActivity.class);
                i.putExtra("nombre", tvNombre.getText().toString());
                i.putExtra("email", tvEmail.getText().toString());
                i.putExtra("telefono", tvTelefono.getText().toString());

                startActivity(i);
                //Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                //startActivityForResult(openGalleryIntent, 1000);
            }
        });

        return fragmentView;
    }

}
