package com.example.projecta.loginin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.example.projecta.R;

import de.hdodenhof.circleimageview.CircleImageView;

public class AddActivity extends AppCompatActivity {

    private Spinner spinnerCategory;
    private ArrayAdapter<CharSequence> arrayAdapter;

    private CircleImageView civAvatarUbi;

    private EditText etNameUbi;
    private EditText etDescriptionUbi;

    private Button btnAccept;
    private Button btnCancel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        spinnerCategory = findViewById(R.id.spinnerCategory);
        arrayAdapter = ArrayAdapter.createFromResource(this, R.array.categories, android.R.layout.simple_spinner_item);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCategory.setAdapter(arrayAdapter);

        civAvatarUbi = findViewById(R.id.civAvatarUbi);

        etNameUbi = findViewById(R.id.etNameUbi);
        etDescriptionUbi = findViewById(R.id.etDescriptionUbi);

        btnAccept = findViewById(R.id.btnAccept);
        btnCancel = findViewById(R.id.btnCancel);

        btnAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }
}
