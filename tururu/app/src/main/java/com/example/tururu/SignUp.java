package com.example.tururu;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class SignUp extends AppCompatActivity {
    TextInputLayout regName, regUsername, regEmail, regPhoneNo, regPassword;
    Button regBtn, regToLoginBtn;
    private static final String TAG = "SignUp";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        regName = findViewById(R.id.reg_name);
        regUsername = findViewById(R.id.reg_username);
        regEmail = findViewById(R.id.reg_email);
        regPhoneNo = findViewById(R.id.reg_phoneNo);
        regPassword = findViewById(R.id.reg_password);
        regBtn = findViewById(R.id.reg_btn);
        regToLoginBtn = findViewById(R.id.reg_login_btn);

        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = regName.getEditText().getText().toString();
                String username = regUsername.getEditText().getText().toString();
                String email = regEmail.getEditText().getText().toString();
                String phoneNo = regPhoneNo.getEditText().getText().toString();
                String password = regPassword.getEditText().getText().toString();

                if (name.isEmpty() || username.isEmpty() || email.isEmpty() || phoneNo.isEmpty() || password.isEmpty()) {
                    Log.e(TAG, "Alanlar boş olamaz");
                    Toast.makeText(SignUp.this, "Lütfen tüm alanları doldurun.", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseDatabase database = FirebaseDatabase.getInstance("https://tururu-d7165-default-rtdb.europe-west1.firebasedatabase.app/");
                DatabaseReference reference = database.getReference("users");

                UserHelperClass helperClass = new UserHelperClass(name, username, email, phoneNo, password);
                reference.child(username).setValue(helperClass)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.d(TAG, "Veri başarıyla kaydedildi.");
                                Toast.makeText(SignUp.this, "Kayıt başarılı! Giriş yapabilirsiniz.", Toast.LENGTH_LONG).show();
                                Intent intent = new Intent(SignUp.this, Login.class);
                                startActivity(intent);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.e(TAG, "Veri kaydedilemedi: " + e.getMessage());
                                Toast.makeText(SignUp.this, "Kayıt başarısız! Lütfen tekrar deneyin.", Toast.LENGTH_LONG).show();
                            }
                        });
            }
        });

        regToLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
                finish();
            }
        });
    }
}
