package com.example.tururu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    Button callSignUp, login_btn;
    TextInputLayout username, password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        callSignUp = findViewById(R.id.signup_screen);
        username = findViewById(R.id.username);
        password = findViewById(R.id.password);
        login_btn = findViewById(R.id.login_btn);

        callSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });

        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });
    }

    private Boolean validateUsername() {
        String val = username.getEditText().getText().toString();

        if (val.isEmpty()) {
            username.setError("Kullanıcı adı boş olamaz.");
            return false;
        } else {
            username.setError(null);
            return true;
        }
    }

    private Boolean validatePassword() {
        String val = password.getEditText().getText().toString();

        if (val.isEmpty()) {
            password.setError("Parola boş olamaz.");
            return false;
        } else {
            password.setError(null);
            return true;
        }
    }

    public void loginUser() {
        if (!validateUsername() || !validatePassword()) {
            return;
        } else {
            isUser();
        }
    }

    private void isUser() {
        final String userEnteredUsername = username.getEditText().getText().toString().trim();
        final String userEnteredPassword = password.getEditText().getText().toString().trim();

        FirebaseDatabase database = FirebaseDatabase.getInstance("https://tururu-d7165-default-rtdb.europe-west1.firebasedatabase.app/");
        DatabaseReference reference = database.getReference("users");

        Query checkUser = reference.orderByChild("username").equalTo(userEnteredUsername);
        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                        String passwordFromDB = userSnapshot.child("password").getValue(String.class);

                        if (passwordFromDB != null && passwordFromDB.equals(userEnteredPassword)) {
                            String usernameFromDB = userSnapshot.child("username").getValue(String.class);
                            String emailFromDB = userSnapshot.child("email").getValue(String.class);
                            String nameFromDB = userSnapshot.child("name").getValue(String.class);
                            String phoneNoFromDB = userSnapshot.child("phoneNo").getValue(String.class);

                            Intent intent = new Intent(Login.this, Dashboard.class);
                            intent.putExtra("username", usernameFromDB);
                            intent.putExtra("email", emailFromDB);
                            intent.putExtra("name", nameFromDB);
                            intent.putExtra("phoneNo", phoneNoFromDB);
                            startActivity(intent);
                            finish();
                            return;
                        }
                    }
                    password.setError("Parola Yanlış");
                    password.requestFocus();
                } else {
                    username.setError("Kullanıcı Adı Yanlış");
                    username.requestFocus();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Hata durumlarını ele almak için kullanıcıya bilgi verileceği yer.
            }
        });
    }
}