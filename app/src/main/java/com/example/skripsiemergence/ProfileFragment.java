package com.example.skripsiemergence;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mikhaellopez.circularimageview.CircularImageView;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;

public class ProfileFragment extends Fragment {
    private Button btnAdd_or_edit_CN;
    private CircularImageView civProfile;
    private LottieAnimationView loadingView;
    private ImageButton edit_pp;
    private TextView tvName,tvEmail,bloodType,phoneNumb,contact1,contact2,contact3,logout;;
    private EditText ePhoneNumb,econtact1,econtact2,econtact3;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseUser;
    private FirebaseDatabase database;
    private static final String user = "user";
    private String uEmail,uId;
    private String gender;
    private boolean edit = true;
    private static final int PICK_IMAGE = 1;
    private static final int MY_INTENT_REQUEST_CODE = 263;
    Uri imageUri;
    private StorageReference profilePicture;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View profile_inflater =  inflater.inflate(R.layout.fragment_profile, container, false);
        loadingView = (LottieAnimationView) profile_inflater.findViewById(R.id.loadingView);
        civProfile = (CircularImageView) profile_inflater.findViewById(R.id.civProfile);
        edit_pp = (ImageButton) profile_inflater.findViewById(R.id.edit_pp);
        tvName = (TextView) profile_inflater.findViewById(R.id.tvName);
        tvEmail = (TextView)profile_inflater.findViewById(R.id.tvEmail);
        contact1 = (TextView) profile_inflater.findViewById(R.id.contact1);
        contact2 = (TextView) profile_inflater.findViewById(R.id.contact2);
        contact3 = (TextView) profile_inflater.findViewById(R.id.contact3);
        bloodType = (TextView)profile_inflater.findViewById(R.id.bloodType);
        phoneNumb = (TextView) profile_inflater.findViewById(R.id.phoneNumb);
        econtact1 = (EditText) profile_inflater.findViewById(R.id.econtact1);
        econtact2 = (EditText) profile_inflater.findViewById(R.id.econtact2);
        econtact3 = (EditText) profile_inflater.findViewById(R.id.econtact3);
        ePhoneNumb = (EditText) profile_inflater.findViewById(R.id.ePhoneNumb);
        logout = (TextView)profile_inflater.findViewById(R.id.logout);
        //refresh page
        mSwipeRefreshLayout = profile_inflater.findViewById(R.id.swipeRefresh);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(this::refreshFragment);
        mSwipeRefreshLayout.setRefreshing(false);
       
        btnAdd_or_edit_CN = (Button)profile_inflater.findViewById(R.id.btnAdd_or_edit_CN);

        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();

        uId = firebaseAuth.getUid();
        uEmail = firebaseUser.getEmail();

        profilePicture = FirebaseStorage.getInstance().getReference().child("profilePicture/" + uId);
        Log.d("MASUK","EMAIL = " + uEmail  );

        if (firebaseUser != null) {
            if(!isNetworkConnected(getContext())){
                Toast.makeText(getContext(), "There's no internet, please turn on data internet!",Toast.LENGTH_LONG).show();
            }else{
                database = FirebaseDatabase.getInstance();
                databaseUser = database.getReference(user);
                // INI NGAMBIL DATANYAA DARI FIREBASE LIVE DATABASE
                loadUserInfo();
            }

            // GOOGLE PROFILE
//            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getContext());
//            if(account !=  null){
////                String personName = account.getDisplayName();
//                String personGivenName = account.getGivenName();
////                String personFamilyName = account.getFamilyName();
//                String personEmail = account.getEmail();
////                String personId = account.getId();
//                Uri personPhoto = account.getPhotoUrl();
//
//                Picasso.get().load(personPhoto).into(civProfile);
//                tvName.setText(personGivenName);
//                tvEmail.setText(personEmail);
//            }

            //edit photo profile
            edit_pp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent gallery = new Intent();
                    gallery.setType("image/*");
                    gallery.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(gallery,"Select Profile Picture"),PICK_IMAGE);
                }
            });

            logout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }
            });
        } else {
            // GAADA USER YG LAGI LOGIN
            startActivity(new Intent(getActivity(),LoginActivity.class));
        }
        return profile_inflater;
    }

    public void refreshFragment(){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (Build.VERSION.SDK_INT >= 26) {
            ft.setReorderingAllowed(false);
        }
        ft.detach(ProfileFragment.this).attach(ProfileFragment.this).commit();
    }
    public boolean isNetworkConnected (Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netinfo = cm.getActiveNetworkInfo();
        if (netinfo != null && netinfo.isConnectedOrConnecting()) {
            android.net.NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            android.net.NetworkInfo mobile = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if((mobile != null && mobile.isConnectedOrConnecting()) || (wifi != null && wifi.isConnectedOrConnecting())) return true;
            else return false;
        } else
            return false;
    }
    public void loadUserInfo(){
        loadingView.setBackgroundResource(R.drawable.loading);
        AnimationDrawable frameAnimation = (AnimationDrawable) loadingView.getBackground();
        frameAnimation.start();
        databaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if(ds.child("email").getValue().equals(uEmail)){
                        if(ds.child("gender").getValue().equals("Pria") && !(ds.hasChild("profilePicture"))){
                            civProfile.setBackgroundResource(R.drawable.ic_boy);
                        }else{
                            Glide.with(getContext()).load(ds.child("profilePicture").child("imageurl").getValue()).into(civProfile);
                        }

                        if(ds.child("gender").getValue().equals("Wanita") && !(ds.hasChild("profilePicture"))){
                            civProfile.setBackgroundResource(R.drawable.ic_girl);
                        }else{
                            Glide.with(getContext()).load(ds.child("profilePicture").child("imageurl").getValue()).into(civProfile);
                        }
                        tvName.setText(ds.child("fullname").getValue(String.class));
                        tvEmail.setText(uEmail);
                        phoneNumb.setText(ds.child("phone").getValue(String.class));
                        bloodType.setText(ds.child("bloodType").getValue(String.class));

                        // EDIT Number
                        if( ds.hasChild("CNumber1") && ds.hasChild("CNumber2") && ds.hasChild("CNumber3") ){
                            contact1.setText(ds.child("CNumber1").getValue(String.class));
                            contact2.setText(ds.child("CNumber2").getValue(String.class));
                            contact3.setText(ds.child("CNumber3").getValue(String.class));
                            btnAdd_or_edit_CN.setText("Edit Contact number");
                            btnAdd_or_edit_CN.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(edit){
                                        btnAdd_or_edit_CN.setText("Save");
                                        // BIKIN EDIT TEXT NYA VISIBLE //
                                        econtact1.setVisibility(View.VISIBLE);
                                        econtact2.setVisibility(View.VISIBLE);
                                        econtact3.setVisibility(View.VISIBLE);

                                        // BIKIN TEXT VIEW NYA INVISIBLE DULU
                                        contact1.setVisibility(View.GONE);
                                        contact2.setVisibility(View.GONE);
                                        contact3.setVisibility(View.GONE);
                                        edit = false;
                                    }else{
                                        // BIAR SETIDAKNY DIA MASUKIN 1 FAM NUMBER
                                        if(econtact1.getText().toString().isEmpty()){
                                            Toast.makeText(getContext(), "Please fill at least 1 phone number",Toast.LENGTH_SHORT).show();
                                        }else{
                                            // CHECK KALO GA EMPTY MASUKIN NO telp yg di input
                                            if(!(econtact1.getText().toString().isEmpty())){
                                                databaseUser.child(uId).child("CNumber1").setValue(econtact1.getText().toString());
                                            }// KALO EMPTY MASUKIN  "-"
                                            else{
                                                databaseUser.child(uId).child("CNumber1").setValue("-");
                                            }

                                            if(!(econtact2.getText().toString().isEmpty())){
                                                databaseUser.child(uId).child("CNumber2").setValue(econtact2.getText().toString());
                                            }else{
                                                databaseUser.child(uId).child("CNumber2").setValue("-");
                                            }

                                            if(!(econtact3.getText().toString().isEmpty())){
                                                databaseUser.child(uId).child("CNumber3").setValue(econtact3.getText().toString());
                                            }else{
                                                databaseUser.child(uId).child("CNumber3").setValue("-");
                                            }

                                            // BIKIN EDIT TEXT NYA INVISIBLE
                                            econtact1.setVisibility(View.GONE);
                                            econtact2.setVisibility(View.GONE);
                                            econtact3.setVisibility(View.GONE);

                                            // BIKIN TEXT VIEW NYA VISIBLE
                                            contact1.setVisibility(View.VISIBLE);
                                            contact2.setVisibility(View.VISIBLE);
                                            contact3.setVisibility(View.VISIBLE);
                                            btnAdd_or_edit_CN.setText("Edit Contact number");
                                            edit = true;
                                        }
                                    }
                                }
                            });
                        }
                        // ADD number
                        else{
                            contact1.setText("Haven't add any contact number");
                            contact2.setText("Haven't add any contact number");
                            contact3.setText("Haven't add any contact number");
                            btnAdd_or_edit_CN.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    if(edit){
                                        btnAdd_or_edit_CN.setText("Save Added phone number");
                                        // BIKIN EDIT TEXT NYA VISIBLE
                                        econtact1.setVisibility(View.VISIBLE);
                                        econtact2.setVisibility(View.VISIBLE);
                                        econtact3.setVisibility(View.VISIBLE);

                                        // BIKIN TEXT VIEW NYA INVISIBLE DULU
                                        contact1.setVisibility(View.GONE);
                                        contact2.setVisibility(View.GONE);
                                        contact3.setVisibility(View.GONE);
                                        edit = false;
                                    }else{
                                        // BIAR SETIDAKNY DIA MASUKIN 1 FAM NUMBER
                                        if(econtact1.getText().toString().isEmpty()){
                                            Toast.makeText(getContext(), "Please fill at least 1 phone number",Toast.LENGTH_SHORT).show();
                                        }else{

                                            // CHECK KALO GA EMPTY MASUKIN NO telp yg di input
                                            if(!(econtact1.getText().toString().isEmpty())){
                                                databaseUser.child(uId).child("CNumber1").setValue(econtact1.getText().toString());
                                            }// KALO EMPTY MASUKIN  "-"
                                            else{
                                                databaseUser.child(uId).child("CNumber1").setValue("-");
                                            }

                                            if(!(econtact2.getText().toString().isEmpty())){
                                                databaseUser.child(uId).child("CNumber2").setValue(econtact2.getText().toString());
                                            }else{
                                                databaseUser.child(uId).child("CNumber2").setValue("-");
                                            }

                                            if(!(econtact3.getText().toString().isEmpty())){
                                                databaseUser.child(uId).child("CNumber3").setValue(econtact3.getText().toString());
                                            }else{
                                                databaseUser.child(uId).child("CNumber3").setValue("-");
                                            }

                                            // BIKIN EDIT TEXT NYA INVISIBLE
                                            econtact1.setVisibility(View.GONE);
                                            econtact2.setVisibility(View.GONE);
                                            econtact3.setVisibility(View.GONE);

                                            // BIKIN TEXT VIEW NYA VISIBLE
                                            contact1.setVisibility(View.VISIBLE);
                                            contact2.setVisibility(View.VISIBLE);
                                            contact3.setVisibility(View.VISIBLE);
                                            btnAdd_or_edit_CN.setText("Edit contact number");
                                            edit = true;
                                        }

                                    }
                                }
                            });

                        }
                    }
                }
                loadingView.setVisibility(View.INVISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE && resultCode == RESULT_OK){
            final Uri ImageData = data.getData();
            civProfile.setImageURI(data.getData());

            final StorageReference imageName = profilePicture.child("image"+ImageData.getLastPathSegment());

            imageName.putFile(ImageData).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    imageName.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            HashMap<String,String> hashMap = new HashMap<>();
                            hashMap.put("imageurl",String.valueOf(uri));

                            databaseUser.child(uId).child("profilePicture").setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(getContext(), "Successfully change profile picture",Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                }
            });
        }
    }
}
