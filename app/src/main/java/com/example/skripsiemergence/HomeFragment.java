package com.example.skripsiemergence;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.widget.Toast.LENGTH_SHORT;

public class HomeFragment extends Fragment {
    private static final int MY_PERMISSIONS_REQUEST_ALL_PERMISSION = 200;
    Button btncancel;
    FloatingActionButton menu,polisi,rumahSakit,pemadamKebakaran;
    Animation fabOpen, fabClose, rotateFoward, rotateBakcward;
    TextView alertText,tvcallfirefighters,tvcallpolice,tvcallambulance,longtitude,latitude,gmaps;
    ImageButton btngetlocation;
    boolean isOpen = false;

    //Nomor layanan darurat
    String noPolisi = "110";
    String noAmbulance = "118";
    String noPemadam = "113";

    String msgbody;
//    private Double CurrentLatitude,CurrentLongitude;
    private Double getCurrentLatitude,getCurrentLongitude;
    boolean isNotCancel= false;
    boolean isRecording = false;
    protected LocationManager locationManager;
    protected LocationListener locationListener;
    private LocationCallback locationCallback;
    private Object mLocationResult;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private boolean requestingLocationUpdates;
    //animasi button emergence
    private ImageView pulseAnim1, pulseAnim2;
    //handler animasi button emergence
    private Handler pulseAnimHandler;
    private LocationManager mLocationManager;
    private MediaRecorder recorder = null;
    private static String mAudiofileName = null;
    private StorageReference mAudioRef;
    FusedLocationProviderClient fusedLocationProviderClient;
    FirebaseAuth auth;
    String currdir;
    String mHashesCurrentLocation;
    String currentloc;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View home_inflater = inflater.inflate(R.layout.fragment_home, container, false);
        menu = (FloatingActionButton)home_inflater.findViewById(R.id.menu);
        polisi = (FloatingActionButton)home_inflater.findViewById(R.id.polisi);
        rumahSakit = (FloatingActionButton)home_inflater.findViewById(R.id.rumahSakit);
        tvcallfirefighters = (TextView)home_inflater.findViewById(R.id.tvcallfirefighters);
        tvcallpolice = (TextView)home_inflater.findViewById(R.id.tvcallpolice);
        tvcallambulance = (TextView)home_inflater.findViewById(R.id.tvcallambulance);
        pemadamKebakaran = (FloatingActionButton)home_inflater.findViewById(R.id.pemadamKebakaran);
        fabOpen = AnimationUtils.loadAnimation(getActivity(),R.anim.fab_open);
        fabClose = AnimationUtils.loadAnimation(getActivity(),R.anim.fab_close);
        rotateBakcward =  AnimationUtils.loadAnimation(getActivity(),R.anim.rotate_backward);
        rotateFoward =  AnimationUtils.loadAnimation(getActivity(),R.anim.rotate_foward);
        pulseAnim1 = (ImageView)home_inflater.findViewById(R.id.pulseAnim1);
        pulseAnim2 = (ImageView)home_inflater.findViewById(R.id.pulseAnim2);
        btncancel = (Button)home_inflater.findViewById(R.id.btncancel);
        alertText = (TextView)home_inflater.findViewById(R.id.textAlert);
//        btngetlocation = (ImageButton)home_inflater.findViewById(R.id.btngetlocation);
        gmaps = (TextView) home_inflater.findViewById(R.id.gmaps);
        pulseAnimHandler = new Handler();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(home_inflater.getContext());

//        btngetlocation.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (ActivityCompat.checkSelfPermission(home_inflater.getContext(),
//                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
//                    && ActivityCompat.checkSelfPermission(home_inflater.getContext(),
//                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//                    getCurrentLocation();
//                }
//                else {
////                    ActivityCompat.requestPermissions(home_inflater.getContext()
////                            ,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
////                            Manifest.permission.ACCESS_COARSE_LOCATION},100);
//                }
//            }
//        });
        final MediaPlayer EmergencySirene = MediaPlayer.create(getActivity(),R.raw.emergency_siren);
        // Get access to the custom Image Button
        ImageButton iv_info = home_inflater.findViewById(R.id.iv_info);
        iv_info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater)
                        view.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                View layoutPopup = inflater.inflate(R.layout.alert_dialog, null);
                int width = LinearLayout.LayoutParams.WRAP_CONTENT;
                int height = LinearLayout.LayoutParams.WRAP_CONTENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(layoutPopup, width, height, focusable);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
                View container = popupWindow.getContentView().getRootView();
                if(container != null) {
                    WindowManager wm = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
                    WindowManager.LayoutParams p = (WindowManager.LayoutParams)container.getLayoutParams();
                    p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                    p.dimAmount = 0.4f;
                    if(wm != null) {
                        wm.updateViewLayout(container, p);
                    }
                }
                ImageView icon_close = layoutPopup.findViewById(R.id.icon_close);

                // dismiss the popup window when touched
                icon_close.setOnClickListener(view1 -> popupWindow.dismiss());
            }
        });
        //set audio filename to current date
        currdir = getActivity().getExternalFilesDir(null).getAbsolutePath();
        String date = new SimpleDateFormat("yyyy-MM-dd, HH:mm:ss", Locale.getDefault()).format(new Date());

        //penyimpanan audio dalam bentuk file .mp3
        mAudiofileName = "/"+date+".mp3";

        //permission
        ask_Location_permission();
        ask_call_permission();

        //setup firebase
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = mAuth.getCurrentUser();

        // KALO USER NYA BLM LOG OUT
        if(firebaseUser != null ){
            Log.d("MASUK","USER DI HOME  ADLAAH" + firebaseUser);

            //click tombol alert, akan keluar animasi 3 petugas emergence
            menu.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFab();
                }
            });

            // Main menu long press
            menu.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ask_sms_audio_permission();
                    ask_location__permission();
                    pulseRunnable.run(); // Ini run animasi pulse-nya
                    EmergencySirene.start();
                    getCurrentLocation();
                    btncancel.setVisibility(View.VISIBLE); //setelah pencet menu, maka kluar button safe
                    alertText.setText("We are currently requesting for help and Record audio from microphone for your safety");
                    menu.setEnabled(false); //  biar gabisa di klik lagi supaya main menu ga kebuka
                    isNotCancel = true;
                    //Delay Sent SMS , biar nunggu lokasi dapet dulu//
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if(isNotCancel) {
                                startRecording();
                            }
                            GetEmergencyPhoneNumber();
                            GetNearbyUserPhoneNumber();
                        }
//
                    }, 5000);   //5 seconds
                    return true;
                }
            });

            polisi.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFab();
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + noPolisi));
                    startActivity(intent);
                    Toast.makeText(getActivity(), "Calling Police Station", LENGTH_SHORT).show();
                }
            });

            rumahSakit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFab();
                    // Permission has already been granted
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + noAmbulance));
                    startActivity(intent);
                    Toast.makeText(getActivity(), "Calling Ambulance", LENGTH_SHORT).show();
                }
            });

            pemadamKebakaran.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    animateFab();
                    // Permission has already been granted
                    Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + noPemadam));
                    startActivity(intent);
                    Toast.makeText(getActivity(), "Calling Pemadam Kebakaran", LENGTH_SHORT).show();
                }
            });

            btncancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isRecording){
                        stopRecording();
                    }
                    EmergencySirene.stop();
                    pulseAnimHandler.removeCallbacks(pulseRunnable);
                    btncancel.setVisibility(View.INVISIBLE);
                    alertText.setText("Press once to call emergency service or Long press to alert your contact");
                    isNotCancel = false;
                    menu.setEnabled(true);
                }
            });

        }
        // GAAADA USER YG LAGI LOGIN //
        else {
            startActivity(new Intent(getActivity(),LoginActivity.class));
        }
        return home_inflater;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED)){
            getCurrentLocation();
        }
        else {
            Toast.makeText(getActivity(),"permission denied", LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation(){
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    Location location = task.getResult();
                    if(location != null){
//                        longtitude.setText(String.valueOf(location.getLongitude()));
//                        latitude.setText(String.valueOf(location.getLatitude()));
                        gmaps.setText(String.valueOf("https://maps.google.com?q="+location.getLatitude()+","+location.getLongitude()));
                        Update_Location_FireBase(gmaps.toString());
                    }
                    else {
                        LocationRequest locationRequest = new LocationRequest()
                                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                                .setInterval(10000)
                                .setFastestInterval(1000)
                                .setNumUpdates(1);
                        LocationCallback locationCallback = new LocationCallback() {
                            @Override
                            public void onLocationResult(@NonNull LocationResult locationResult) {
//                                Location location1 = locationResult.getLastLocation();
//                                longtitude.setText(String.valueOf(location.getLongitude()));
//                                latitude.setText(String.valueOf(location.getLatitude()));
//                                gmaps.setText(String.valueOf("https://maps.google.com?q="+location.getLatitude()+","+location.getLongitude()));
                            }
                        };
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper());
                    }
                }
            });
        }
        else {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }
    public void ask_location__permission() {
        if (ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            getCurrentLocation();
        }
        else {
        }
    }
    // ANIMASI MENU + ERROR HANDLING //
    private void animateFab(){
        if(isOpen){
            //invisible animation & text emergency service call
            tvcallfirefighters.setVisibility(View.INVISIBLE);
            tvcallpolice.setVisibility(View.INVISIBLE);
            tvcallambulance.setVisibility(View.INVISIBLE);
            polisi.startAnimation(fabClose);
            rumahSakit.startAnimation(fabClose);
            pemadamKebakaran.startAnimation(fabClose);
            polisi.setClickable(false);
            rumahSakit.setClickable(false);
            pemadamKebakaran.setClickable(false);
            isOpen = false;
            final MediaPlayer EmergencySirene = MediaPlayer.create(getActivity(),R.raw.emergency_siren);

            menu.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    // INI RUN ANIMASI PULSE NYA //
                    ask_sms_audio_permission();
                    ask_location__permission();
                    pulseRunnable.run();
                    EmergencySirene.start();
                    getCurrentLocation();
                    btncancel.setVisibility(View.VISIBLE);
                    alertText.setText("We are currently requesting for help");
                    menu.setEnabled(false); //  biar gabisa di klik lagi supaya main menu ga kebuka
                    isNotCancel = true;
                    //Delay Sent SMS , biar nunggu lokasi dapet dulu//
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            if(isNotCancel) {startRecording();}
                            GetEmergencyPhoneNumber();
                            GetNearbyUserPhoneNumber();
                        }
                    }, 5000);   //delay for 5 seconds
                    return true;
                }
            });
        }
        else{
            //visible animation & text emergency service call
            tvcallfirefighters.setVisibility(View.VISIBLE);
            tvcallpolice.setVisibility(View.VISIBLE);
            tvcallambulance.setVisibility(View.VISIBLE);
            polisi.startAnimation(fabOpen);
            rumahSakit.startAnimation(fabOpen);
            pemadamKebakaran.startAnimation(fabOpen);
            polisi.setClickable(true);
            rumahSakit.setClickable(true);
            pemadamKebakaran.setClickable(true);
            isOpen = true;
            menu.setOnLongClickListener(null);
        }
    }

    // Ini Function buat pulse animation pas long press alert button
    private final Runnable pulseRunnable = new Runnable() {
        @Override
        public void run() {
            pulseAnim1.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(1000).withEndAction(new Runnable() {
                @Override
                public void run() {
                    pulseAnim1.setScaleX(1f);
                    pulseAnim1.setScaleY(1f);
                    pulseAnim1.setAlpha(1f);
                }
            });
            pulseAnim2.animate().scaleX(4f).scaleY(4f).alpha(0f).setDuration(700).withEndAction(new Runnable() {
                @Override
                public void run() {
                    pulseAnim2.setScaleX(1f);
                    pulseAnim2.setScaleY(1f);
                    pulseAnim2.setAlpha(1f);
                }
            });
            pulseAnimHandler.postDelayed(pulseRunnable, 1500);
        }
    };

    // Ask Permission for first time Manifest.permission.CALL_PHONE
    public void ask_call_permission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CALL_PHONE)) {
                // sees the explanation, try again to request the permission.
                Toast.makeText(getActivity(), "We Need Your Permission to Fully Operating", LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CALL_PHONE}, MY_PERMISSIONS_REQUEST_ALL_PERMISSION);
            }
        } else {
            // Permission has already been granted
        }
    }
    // Ask Permission for first time Manifest.permission.RECORD_AUDIO
    public void ask_sms_audio_permission() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.RECORD_AUDIO) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),Manifest.permission.SEND_SMS)) {
                // sees the explanation, try again to request the permission.
                Toast.makeText(getActivity(), "We Need Your Permission to Fully Operating", LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO,Manifest.permission.SEND_SMS}, MY_PERMISSIONS_REQUEST_ALL_PERMISSION);
            }
        } else {
            // Permission has already been granted
        }
    }
    // Ask Permission for first time Manifest.permission.ACCESS_FINE_LOCATION, ACCESS_COARSE_LOCATION
    public void ask_Location_permission() {
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_ALL_PERMISSION);
        }
        mLocationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE );
        boolean statusOfGPS = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if(!statusOfGPS){
            Toast.makeText(getActivity(), "We Cant Fully Operated without Location", LENGTH_SHORT).show();
            Intent intentAskGPS = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intentAskGPS);
        }

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                getCurrentLatitude = location.getLatitude();
                getCurrentLongitude = location.getLongitude();
                GeoHash hash = GeoHash.fromLocation(location, 9);
                mHashesCurrentLocation = hash.toString();
                Update_Location_FireBase(hash.toString());
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.d("Latitude","disable");
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("Latitude","enable");
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("Latitude","status");
            }
        });
    }

    //Kirim SMS yang udah di delay 10 detik (kirim sms ketika user berada dalam bahaya)
    private void Send_SMS(String DestPNumber){
        try {
            if(isNotCancel==true){
                msgbody = "Ini adalah pesan OTOMATIS, jika anda menerima pesan ini berarti pemilik nomor hp ini sedang dalam BAHAYA. Anda dapat melihat lokasi pemilik nomor saat ini dengan menekan link : ";
                msgbody+=(gmaps);
                SmsManager manager;
                SmsManager smsM = SmsManager.getDefault();
                ArrayList<String> divideBody = smsM.divideMessage(msgbody);
                Log.d("sms","SMS ke sini: "+DestPNumber);
                smsM.sendMultipartTextMessage(DestPNumber, null, divideBody, null, null);
                Toast.makeText(getActivity(), "SMS Sent Successfully", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "SMS Failed to Send, Please try again", Toast.LENGTH_SHORT).show();
        }
    }

    //function untuk mulai record
    private void startRecording() {
        isRecording = true;
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        recorder.setOutputFile(currdir+mAudiofileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e("Start Recording Audio", "prepare() failed");
        }
        recorder.start();
        Log.d("Start Recording Audio","Lagi recording bareng ariel");
    }

    //function untuk berhenti record
    private void stopRecording() {
        isRecording = false;
        recorder.stop();
        recorder.release();
        recorder = null;
        Log.d("Stop Recording Audio","Cut Tari nya udahan");
        Log.d("Stop Recording Audio","Lokasi File = "+currdir+mAudiofileName);
        String mAudioFullPath  = currdir+mAudiofileName;
        Upload_to_Firebase_Storage(mAudioFullPath);
    }

    //ini function untuk upload recorder suara ke firebase
    private void Upload_to_Firebase_Storage(String mAudioFullPath) {
        Uri file = Uri.fromFile(new File(mAudioFullPath));
        UploadTask uploadTask;
        auth = FirebaseAuth.getInstance();
        FirebaseStorage storage = FirebaseStorage.getInstance();
        Log.d("FireBaseUID","UID = "+auth.getCurrentUser().getUid());
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();
        mAudioRef = storageRef.child("/audios/" + uid + "/"+mAudiofileName);
        uploadTask = mAudioRef.putFile(file);
        // Register observers to listen for when the download is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });
        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }

                // Continue with the task to get the download URL
                return mAudioRef.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downloadUri = task.getResult();
                    Log.d("SU_FB_STR","URI Download : "+downloadUri);
                } else {
                    // Handle failures
                    // ...
                }
            }
        });
        Log.d("SU_FB_STR","Audio files name : "+mAudiofileName);
        Log.d("SU_FB_STR","Audio Storage Ref : "+mAudioRef);
    }

    //update location ke firebase
    private void Update_Location_FireBase(final String mGeoHash){
        if(!(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(null))){
            FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                     @Override
                     public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("email")) {
                            Map<String, Object> postValues = new HashMap<String, Object>();
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            postValues.put(snapshot.getKey(), snapshot.getValue());
                            }
                            Log.d("update location","Location Updating");
                            postValues.put("location", mGeoHash);
                            FirebaseDatabase.getInstance().getReference().child("user").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(postValues);
                        } else {
                        }
                     }
                     @Override
                     public void onCancelled(DatabaseError databaseError) {}
                     });
        }
    }

    //Get nearby user phone number
    private void GetNearbyUserPhoneNumber(){
        if(mHashesCurrentLocation != null){
            FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Log.d("narik data","mHashesCurrentLocation "+mHashesCurrentLocation);
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        Log.d("narik data","Location DB "+ds.child("location").getValue().toString());
                        if(ds.child("location").getValue().toString().substring(0,5).equals(mHashesCurrentLocation.substring(0,5)) && !(
                                ds.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))){
//                            Log.d("narik data","SMS ke sini : "+ds.child("phone").getValue().toString());
                            Send_SMS(ds.child("phone").getValue().toString());
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                    // ...
                }
            });
        }
    }

    //dapetin nomer dari kontak
    private void GetEmergencyPhoneNumber(){
        FirebaseDatabase.getInstance().getReference("user").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds:dataSnapshot.getChildren()){
                    if(ds.getKey().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())){
                        if(ds.child("CNumber1").exists() && !(ds.child("CNumber1").getValue().toString().equals("-"))){
                            Send_SMS(ds.child("CNumber1").getValue().toString());
                        }
                        if(ds.child("CNumber2").exists()&& !(ds.child("CNumber2").getValue().toString().equals("-"))){
                            Send_SMS(ds.child("CNumber2").getValue().toString());
                        }if(ds.child("CNumber3").exists() && !(ds.child("CNumber3").getValue().toString().equals("-"))){
                            Send_SMS(ds.child("CNumber3").getValue().toString());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // ...
            }
        });
    }
}