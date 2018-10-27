package com.example.rupal.naarad;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,LocationListener {

    private static final String TAG = "MainActivity";
    private static final String POST_MESSAGE_URL = "http://52.163.84.66/score";
    private static final String MAIL_SENDING_URL = "https://naarad-mailer.herokuapp.com/predict";
    private static final int HTTP_INITIAL_TIME_OUT = 10000;
    private static final int HTTP_RETRIES = 0;
    private static final int PERMISSION_ACCESS_FINE_LOCATION = 100;
    private static final int PERMISSION_ACCESS_COARSE_LOCATION = 101;
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 102;
    private static final long UPDATE_INTERVAL = 10000;
    private static final long FASTEST_INTERVAL = 10000;
    private Boolean isSafe = Boolean.TRUE;
    private Double latitude;
    private Double longitude;

    @BindView(R.id.convert_btn)     FloatingActionButton btn_convert;
    @BindView(R.id.et_msg)          TextInputEditText msg_edittext;
    @BindView(R.id.msg_send_button) Button send_msg;
    @BindView(R.id.relative_name)   TextInputEditText name_relative_et;
    @BindView(R.id.relative_find_button)    Button submit_rel_name;
    @BindView(R.id.rel_find_progressBar)    ProgressBar rel_progress;
    @BindView(R.id.rel_address_et)  TextView rel_address;
    @BindView(R.id.found_txtview)   TextView found_text;
    @BindView(R.id.status_tv)   TextView status_safe;
    @BindView(R.id.status_progressBar)  ProgressBar stat_progress;
    @BindView(R.id.rel_status_tv)   TextView relative_status_tv;

    private final int REQ_CODE_SPEECH_OUTPUT = 100;
    private RequestQueue requestQueue;
    private GoogleApiClient googleApiClient;
    private LocationRequest locationRequest;
    private Location location;
    private String user_address;
    private String result_text;
    private FirebaseUser firebaseUser;
    private FirebaseFirestore db;
    private String relative_address;
    private String relative_status;
    private String obtained_class;

    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        googleApiClient = new GoogleApiClient.Builder(this).
                addApi(LocationServices.API).
                addConnectionCallbacks(this).
                addOnConnectionFailedListener(this).build();


        requestQueue = Volley.newRequestQueue(this);
        db = FirebaseFirestore.getInstance();

        //getting user info

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser != null) {
            username = firebaseUser.getDisplayName();
        }
        else {
            Toast.makeText(this, "You are not signed in", Toast.LENGTH_SHORT).show();
        }

        // Sending user info to firestore
        // Access a Cloud Firestore instance from your Activity
        checkLocationPermissions();
        getAddress();

        btn_convert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                convert();
            }
        });

        send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send_msg.setBackgroundColor(Color.DKGRAY);
                result_text = msg_edittext.getText().toString();
                if (result_text.equals("")) {
                    Toast.makeText(MainActivity.this, "Message cannot be empty!", Toast.LENGTH_LONG).show();
                    send_msg.setBackgroundColor(Color.RED);
                }
                else {

                    checkLocationPermissions();
                    sendTextToServer(new VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {

                            Map<String, String> params = new HashMap<>();
                            params.put("username", username);
                            params.put("line", result_text);
                            params.put("address", user_address);
                            params.put("class", result.replaceAll("[^0-9]", ""));

                            Log.e(TAG, "gfvb ccvb " + params.toString());

                            JsonObjectRequest jsonObjectRequest1 = new JsonObjectRequest(Request.Method.POST,
                                    MAIL_SENDING_URL,
                                    new JSONObject(params),
                                    new Response.Listener<JSONObject>() {
                                        @Override
                                        public void onResponse(JSONObject response) {
                                            try {
                                                if (response.getString("status").equals("OK")) {
                                                    Log.e(TAG, "Mail sent probably");
                                                    send_msg.setBackgroundColor(Color.RED);
                                                }
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                                send_msg.setBackgroundColor(Color.RED);
                                            }
                                        }
                                    }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(MainActivity.this, "Check your internet connection please", Toast.LENGTH_LONG).show();
                                    error.printStackTrace();
                                    send_msg.setBackgroundColor(Color.RED);
                                }
                            });

                            jsonObjectRequest1.setRetryPolicy(new DefaultRetryPolicy(
                                    HTTP_INITIAL_TIME_OUT,
                                    HTTP_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                            ));

                            requestQueue.add(jsonObjectRequest1);
                            send_msg.setBackgroundColor(Color.RED);

                        }
                    });
                }
            }
        });

        submit_rel_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                submit_rel_name.setVisibility(View.GONE);
                rel_progress.setVisibility(View.VISIBLE);

                Editable relative_edtable = name_relative_et.getText();

                if (relative_edtable != null) {
                    String relative = relative_edtable.toString();

                    DocumentReference rel_user = db.collection("users").document(relative);
                    rel_user.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            Object relative_add = documentSnapshot.get("address");
                            Object relative_stat = documentSnapshot.get("status");

                            if (relative_add != null && relative_stat != null) {
                                relative_address = relative_add.toString();
                                relative_status = relative_stat.toString();
                                rel_address.setText(relative_address);

                                if (relative_status.equals("false")) {
                                    relative_status = getResources().getString(R.string.danger_relative);
                                }
                                else {
                                    relative_status = getResources().getString(R.string.safe_relative);
                                }
                                relative_status_tv.setText(relative_status);
                                relative_status_tv.setVisibility(View.VISIBLE);

                                found_text.setVisibility(View.VISIBLE);
                                rel_progress.setVisibility(View.GONE);
                                submit_rel_name.setVisibility(View.VISIBLE);
                                rel_address.setVisibility(View.VISIBLE);
                            }
                            else {
                                Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                                relative_status_tv.setVisibility(View.GONE);
                                rel_progress.setVisibility(View.GONE);
                                submit_rel_name.setVisibility(View.VISIBLE);
                                found_text.setVisibility(View.GONE);
                                rel_address.setVisibility(View.GONE);
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(MainActivity.this, "Specified user not found", Toast.LENGTH_LONG).show();
                            rel_progress.setVisibility(View.GONE);
                            submit_rel_name.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
        });

        status_safe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isSafe = !isSafe;

                status_safe.setVisibility(View.GONE);
                stat_progress.setVisibility(View.VISIBLE);

                sendDataToFirestore();

                colorizeTV();

                status_safe.setVisibility(View.VISIBLE);
                stat_progress.setVisibility(View.GONE);
            }
        });

    }

    private void colorizeTV() {
        if (isSafe) {
            status_safe.setText(getResources().getString(R.string.safe_text));
            status_safe.setBackgroundColor(Color.GREEN);
        }
        else {
            status_safe.setText(getResources().getString(R.string.danger_text));
            status_safe.setBackgroundColor(Color.RED);
        }
    }

    private void getAddress() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (googleApiClient.isConnected()) {
                location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

                if (location != null) {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    getDetailsFromCoords();
                    Log.e(TAG, ( Double.toString(location.getLatitude())) + " " + Double.toString(location.getLongitude()));
                }
            }
            else {
                googleApiClient.connect();
            }
        }

    }

    private void checkLocationPermissions() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_ACCESS_FINE_LOCATION);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[] {Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSION_ACCESS_COARSE_LOCATION);
        }

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_FINE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                    return;
                }

            case PERMISSION_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(this, "Need your location!", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
        getAddress();

    }

    private void getDetailsFromCoords() {

        Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null) {
                Address address = addresses.get(0);
                String add = address.getAddressLine(0);
                user_address = add;
                Log.e(TAG, address.toString());

                sendDataToFirestore();

                Log.e(TAG, add);
            }
            else {
                Toast.makeText(this, "Address is empty!", Toast.LENGTH_LONG).show();
            }
        }
        catch (IOException e) {
            Toast.makeText(this, "IOException!!!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    private void sendDataToFirestore() {


        Map<String, String> userInfo = new HashMap<>();
        Log.e(TAG, user_address);
        userInfo.put("username", username);
        userInfo.put("address", user_address);
        userInfo.put("status", isSafe.toString());

        db.collection("users")
                .document(username)
                .set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.e(TAG, "Successfully Added to firestore");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failure in Firestore");
                    }
                });


    }

    private void convert() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say your message!");

        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_OUTPUT);
        }
        catch (ActivityNotFoundException e) {
            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_CODE_SPEECH_OUTPUT) {
            if (resultCode == RESULT_OK && data!=null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                result_text = result.get(0);

                Log.e(TAG, result_text);
                msg_edittext.setText(result_text);

//                sendTextToServer(result_text);

            }
        }

    }

    private void sendTextToServer(final VolleyCallback callback) {

        getAddress();

        try {
            final JSONObject jsonBody = new JSONObject();
            result_text = msg_edittext.getText().toString();
            jsonBody.put("line", result_text);

            StringRequest stringRequest = new StringRequest(Request.Method.POST,
                    POST_MESSAGE_URL,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            String status = response;
                            Log.e(TAG, status);
                            callback.onSuccess(status);
                            send_msg.setBackgroundColor(Color.RED);

                            Log.e(TAG, response);
                            Toast.makeText(MainActivity.this, "Your message was sent!", Toast.LENGTH_LONG).show();

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                            Log.e(TAG, error.toString());
                            Toast.makeText(MainActivity.this, "Please check your Internet Connection and try again!", Toast.LENGTH_LONG).show();
                            send_msg.setBackgroundColor(Color.RED);

                        }
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    Log.e(TAG, jsonBody.toString());
                    return jsonBody.toString().getBytes();
                }
            };
//        {
//            @Override
//            protected Map<String,String> getParams(){
//                Map<String,String> params = new HashMap<>();
//                params.put("address", user_address);
//                params.put("line", result_text);
//                Log.e(TAG, params.toString());
//
//                return params;
//            }
//        }

            stringRequest.setRetryPolicy(new DefaultRetryPolicy(
                    HTTP_INITIAL_TIME_OUT,
                    HTTP_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            requestQueue.add(stringRequest);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Permissions ok, we get last location
        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);

        if (location != null) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            getDetailsFromCoords();
            Log.e(TAG, ( Double.toString(location.getLatitude())) + " " + Double.toString(location.getLongitude()));
//            locationTv.setText("Latitude : " + location.getLatitude() + "\nLongitude : " + location.getLongitude());
        }

        startLocationUpdates();

    }

    private void startLocationUpdates() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                &&  ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "You need to enable permissions to display location !", Toast.LENGTH_SHORT).show();
        }

        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
    }


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (googleApiClient != null) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkPlayServices()) {
//            locationTv.setText("You need to install Google Play Services to use the App properly");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // stop location updates
        if (googleApiClient != null  &&  googleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            googleApiClient.disconnect();
        }
    }

    private boolean checkPlayServices() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST);
            } else {
                finish();
            }

            return false;
        }

        return true;
    }

    private interface VolleyCallback {
        void onSuccess(String result);
    }


}