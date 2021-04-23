package com.example.skripsiemergence;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;


public class MainActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    final Context context = this;
    ImageView icon_close;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                firebaseAuth = FirebaseAuth.getInstance();
        // get user from firebase, jika user sudah login
        if(firebaseAuth.getCurrentUser() != null){
            //page utk munculin bottom nav
            setContentView(R.layout.activity_main);
            BottomNavigationView bottomNav = findViewById(R.id.btnNavigation);
            bottomNav.setOnNavigationItemSelectedListener(navListener);

            //set page home di frame layout activity_main
            HomeFragment mHomeFragment = new HomeFragment();
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container,mHomeFragment).commit();
        }else{
            //akan diarahkan ke halaman login, jika user belum melakukan login
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.alert_dialog);
        dialog.show();
        icon_close = (ImageView) dialog.findViewById(R.id.icon_close);
        icon_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    //BottomNavigationView, terdapat 2 menu
    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;
                    switch (item.getItemId()){
                        case R.id.nav_home:
                            selectedFragment = new HomeFragment();
                            break;
                        case R.id.nav_profile:
                            selectedFragment = new ProfileFragment();
                            break;
                    }
                    //replace di activity_main
                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,selectedFragment).commit();
                return true;
                }
            };
    //back to home
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        HomeFragment mHomeFragment = new HomeFragment();
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.fragment_container,mHomeFragment).commit();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }
}
