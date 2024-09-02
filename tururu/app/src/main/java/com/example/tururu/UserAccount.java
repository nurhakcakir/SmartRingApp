package com.example.tururu;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class UserAccount extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
    }

    // "Cihazlarınız" TextView'ine tıklanınca çalışacak metod
    public void cihazlariniz_onclick(View view) {
        Intent intent = new Intent(UserAccount.this, YourDevices.class);
        startActivity(intent);
    }

    public void kisisel_bilgiler_onclick(View view) {
        Intent intent = new Intent(UserAccount.this, UserProfile.class);
        startActivity(intent);
    }
}
