package com.example.skripsiemergence;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import static android.widget.Toast.LENGTH_SHORT;

public class RegisterFragment extends Fragment {
    private EditText name;
    private EditText address;
    private EditText phone;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextRePassword;
    private RadioGroup genderRG;
    private RadioButton genderBtn;
    private ProgressDialog progressDialog;
    private Button buttonRegister;
    private EditText bloodType;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseUser;
    private FirebaseDatabase database;
    private static final String USER = "user";
    private User user;
    private boolean valid = true;

    public RegisterFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View Register_inflater = inflater.inflate(R.layout.fragment_register, container, false);
        progressDialog = new ProgressDialog(getContext());
        buttonRegister = (Button) Register_inflater.findViewById(R.id.btn_register);
        name = (EditText) Register_inflater.findViewById(R.id.et_name);
        address = (EditText) Register_inflater.findViewById(R.id.et_address);
        phone = (EditText) Register_inflater.findViewById(R.id.et_phone);
        editTextEmail = (EditText) Register_inflater.findViewById(R.id.et_email);
        editTextPassword = (EditText) Register_inflater.findViewById(R.id.et_password);
        editTextRePassword = (EditText) Register_inflater.findViewById(R.id.et_repassword);
        bloodType = (EditText) Register_inflater.findViewById(R.id.bloodType);
        genderRG = (RadioGroup)Register_inflater.findViewById(R.id.gender);

        //Setup firebase
        database = FirebaseDatabase.getInstance();
        databaseUser = database.getReference(USER);
        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        //Click Button Register
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valid = true;
                //get email
                String email = editTextEmail.getText().toString().trim();
                if(editTextEmail.getText().toString().isEmpty()){
                    //if email is empty
                    editTextEmail.setError("This value cannot be empty");
                    valid = false;
                }
                //input password
                String password = editTextPassword.getText().toString().trim();
                //input re-password
                String rePass = editTextRePassword.getText().toString().trim();
                //cek if pass or re-pass is empty
                if(editTextPassword.getText().toString().isEmpty()){
                    editTextPassword.setError("This value cannot be empty");
                }if(editTextRePassword.getText().toString().isEmpty()){
                    editTextRePassword.setError("This value cannot be empty");
                //cek pass tidak samadengan repassword
                }else if(!password.equals(rePass)){
                    editTextRePassword.setError("Password does not match");
                    valid = false;
                }
                //input fullname
                String fullname = name.getText().toString();
                if(name.getText().toString().isEmpty()){
                    //cek if fullname is empty
                    name.setError("This value cannot be empty");
                    valid = false;
                }
                //input address
                String addr = address.getText().toString() ;
                if(address.getText().toString().isEmpty()){
                    //cek if address is empty
                    address.setError("This value cannot be empty");
                    valid = false;
                }
                //input gender (pilihan dari radio button)
                int gender = genderRG.getCheckedRadioButtonId();
                genderBtn = (RadioButton) Register_inflater.findViewById(gender);
                //gender sudah dipilih, dan get text daripilihannya
                String gender_user = genderBtn.getText().toString();
                Log.d("MASUK","GENDER REGIS : " + gender_user);

                //input phone number
                String phne = phone.getText().toString();
                if(phone.getText().toString().isEmpty()){
                    //cek if phone number is empty
                    phone.setError("This value cannot be empty");
                    valid = false;
                }
                //input Blood Type
                String bType = bloodType.getText().toString();
                if(bloodType.getText().toString().isEmpty()){
                    //cek if bloodtype is empty
                    bloodType.setError("This value cannot be empty");
                    valid = false;
                    //Blood Type hanya bisa input A/B/O/AB dengan huruf kapital
                }if(!(bType.equals("A") || bType.equals("B") || bType.equals("O") || bType.equals("AB"))){
                    bloodType.setError("Invalid blood type, please use capital letter");
                    Log.d("MASUK","BLOOD TYPE YG MASUK  ADLAAH " + bType);
                    valid = false;
                }

                if(valid){
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
                    Log.d("MASUK ",  password);
                    //Get data register
                    user = new User(email, password, fullname, gender_user, phne ,addr, bType,"AAAAAAAAA");
                    registerUser(email, password);
                    //Regist process
                    progressDialog.setMessage("Registering User.....");
                    progressDialog.show();
                }else{
                    Toast.makeText(getActivity(), "Please complete the form", LENGTH_SHORT).show();
                }
            }
        });
        return Register_inflater;
    }

    private void registerUser(String email, String password) {
        // Auth Regist
        mAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressDialog.dismiss();
                        if (task.isSuccessful()) {
                            //Send email to verify
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                //if success, then show toast "user cek email to verify"
                                                Toast.makeText(getContext(), "Registered successfully. Please verify your email address",Toast.LENGTH_SHORT).show();
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                updateUI(user);
                                            }else{
                                                //if failed, then show toast "user input data yang benar"
                                                Toast.makeText(getContext(), "Register failed, please check your credentials",Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        } else{
                            Toast.makeText(getContext(), "Register failed, please check your credentials",Toast.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    public void updateUI(FirebaseUser currentUser){
        String keyId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d("MASUK","KEY ID " + keyId);
        databaseUser.child(keyId).setValue(user);
        //if success, then user can login
        Intent loginIntent = new Intent(getActivity(),LoginActivity.class);
        startActivity(loginIntent);
    }
}