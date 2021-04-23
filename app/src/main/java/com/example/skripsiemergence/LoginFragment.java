package com.example.skripsiemergence;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class LoginFragment extends Fragment {
    private EditText input_email;
    private EditText input_password;
    private Button buttonSignIn;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseUser;
    private FirebaseDatabase database;
    private static final String USER = "user";
    private boolean valid = true;

    public LoginFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View login_inflater = inflater.inflate(R.layout.fragment_login, container, false);
        input_email = (EditText) login_inflater.findViewById(R.id.et_email);
        input_password = (EditText) login_inflater.findViewById(R.id.et_password);
        buttonSignIn = (Button) login_inflater.findViewById(R.id.btn_login);
        progressDialog = new ProgressDialog(getContext());
        firebaseAuth = FirebaseAuth.getInstance();

        database = FirebaseDatabase.getInstance();
        databaseUser = database.getReference(USER);
        mAuth = FirebaseAuth.getInstance();
        //saat menekan tombol signin
        buttonSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valid = true;
                //memasukan email
                String email = input_email.getText().toString();
                //jika email kosong
                if(input_email.getText().toString().isEmpty()){
                    input_email.setError("This value cannot be empty");
                    valid = false;
                }
                //memasukan password
                String password = input_password.getText().toString();
                //jika password kosong
                if(input_password.getText().toString().isEmpty()){
                    input_password.setError("This value cannot be empty");
                    valid = false;
                }

                String generatedPassword = null;
                try {
                    // Create MessageDigest instance for MD5
                    MessageDigest md = MessageDigest.getInstance("MD5");
                    //Add password bytes to digest
                    md.update(password.getBytes());
                    //Get the hash's bytes
                    byte[] bytes = md.digest();
                    //This bytes[] has bytes in decimal format;
                    //Convert it to hexadecimal format
                    StringBuilder sb = new StringBuilder();
                    for(int i=0; i< bytes.length ;i++)
                    {
                        sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
                    }
                    //Get complete hashed password in hex format
                    generatedPassword = sb.toString();
                }
                catch (NoSuchAlgorithmException e)
                {
                    e.printStackTrace();
                }
                password = generatedPassword;
                if(valid){
                    progressDialog.setMessage("Please Wait ...");
                    progressDialog.show();
                    //jika valid sudah dimasukan smua email/pass, akan dicek dgn firebase authification
                    firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            //jika sukses, maka akan ke halaman home fragment
                            if (task.isSuccessful()) {
                                progressDialog.dismiss();
                                if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    Intent homeFragment = new Intent(getContext(), MainActivity.class);
                                    startActivity(homeFragment);
                                } else {
                                    Toast.makeText(getContext(), "Please verify your email address", Toast.LENGTH_LONG).show();
                                }
                            }
                            //jika salah/ tidak valid, maka akan dimunculkan toast invalid
                            else {
                                progressDialog.dismiss();
                                Toast.makeText(getContext(), "Invalid E-mail or Password, Please Try Again ...", Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });
        //kembali ke page login
        return login_inflater;
    }
    // hasil yang didapat akan disimpan datanya, bisa digunakan untuk menampilkan kembali di halaman profile
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
}























