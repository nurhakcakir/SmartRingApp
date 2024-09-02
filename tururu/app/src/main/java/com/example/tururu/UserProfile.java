package com.example.tururu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

public class UserProfile extends AppCompatActivity {

    TextInputLayout fullName,email,phoneNo,password;
    TextView fullNameLabel,usernameLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //hooks
        fullName=findViewById(R.id.full_name_profile);
        email=findViewById(R.id.email_profile);
        phoneNo=findViewById(R.id.phone_no_profile);
        password=findViewById(R.id.password_profile);
        fullNameLabel=findViewById(R.id.fullname_field);
        usernameLabel=findViewById(R.id.username_field);

        //bütün verileri gösterme
        showAllUserData();
    }

    private void showAllUserData() {
        Intent intent=getIntent();
        String user_username=intent.getStringExtra("username");
        String user_name=intent.getStringExtra("name");
        String user_email=intent.getStringExtra("email");
        String user_phoneNo=intent.getStringExtra("phoneNo");
        String user_password=intent.getStringExtra("password");

        fullNameLabel.setText(user_name);
        usernameLabel.setText(user_username);
        fullName.getEditText().setText(user_name);
        email.getEditText().setText(user_email);
        phoneNo.getEditText().setText(user_phoneNo);
        password.getEditText().setText(user_password);
    }
}