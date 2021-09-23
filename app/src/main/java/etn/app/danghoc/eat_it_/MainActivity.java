package etn.app.danghoc.eat_it_;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthMethodPickerLayout;
import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.BasePermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.Arrays;
import java.util.List;

import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Model.UserModel;
import etn.app.danghoc.eat_it_.Model.UserUtils;
import io.reactivex.disposables.CompositeDisposable;

public class MainActivity extends AppCompatActivity {

    private static final int LOGIN_REQUEST_CODE = 1121;
    private static int APP_REQUEST_CODE = 1233;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private FirebaseDatabase database;
    private DatabaseReference refUser;
    private List<AuthUI.IdpConfig> providers;


    private Place placeSelect;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field>placeFields=Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);
    //processbar
    private ProgressBar progressBar;


    @Override
    protected void onStart() {
        super.onStart();
        delaySplashScreen();
    }

    private void delaySplashScreen() {
        progressBar.setVisibility(View.VISIBLE);
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (firebaseAuth != null && listener != null) {
            firebaseAuth.removeAuthStateListener(listener);
        }
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init() {


        Places.initialize(this,"AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8");
        placesClient=Places.createClient(this);

        progressBar = findViewById(R.id.progressBar);

        database = FirebaseDatabase.getInstance();
        refUser = database.getReference(Common.USER_INFO_REF);
        providers = Arrays.asList(
                new AuthUI.IdpConfig.PhoneBuilder().build()
        );

        firebaseAuth = FirebaseAuth.getInstance();
        listener = myFirebaseAuth -> {
            FirebaseUser user = myFirebaseAuth.getCurrentUser();

            Dexter.withContext(this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                            if (user != null) {
                                checkUserFromFirebase();

                            } else {
                                showLoginLayout();
                            }
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText(MainActivity.this, "you must enable this permission to use app", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        }
                    }).check();



        };
    }

    private void showLoginLayout() {

        AuthMethodPickerLayout authMethodPickerLayout = new AuthMethodPickerLayout
                .Builder(R.layout.layout_sign_in)
                .setGoogleButtonId(R.id.btn_google_sign_in)
                .setPhoneButtonId(R.id.btn_phone_sign_in)
                .build();

        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAuthMethodPickerLayout(authMethodPickerLayout)
                .setIsSmartLockEnabled(false)
                .setAvailableProviders(providers)
                .setTheme(R.style.LoginTheme)
                .build(),LOGIN_REQUEST_CODE
        );
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==LOGIN_REQUEST_CODE){
            IdpResponse response=IdpResponse.fromResultIntent(data);
            if(resultCode==RESULT_OK){

            }else{
                Toast.makeText(this, response.getError().getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkUserFromFirebase() {
        refUser.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            UserModel userModel = snapshot.getValue(UserModel.class);
                            Common.curentUser = userModel;
                            Log.d("aaa","uid main : "+Common.curentUser.getUid());
                            gotoHomeActivity(userModel);
                        } else {
                            showRegisterLayout();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(MainActivity.this, "[ERROR]" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showRegisterLayout() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        TextInputEditText edtName = view.findViewById(R.id.edt_name);

        TextView txt_address_detail = view.findViewById(R.id.txt_address_detail);

        TextInputEditText edtNumberPhone = view.findViewById(R.id.edt_phone);
        Button btnContinue = view.findViewById(R.id.btn_continue);
        ProgressBar progressBar2 = view.findViewById(R.id.progressBar);

        places_fragment=(AutocompleteSupportFragment)getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelect=place;
                txt_address_detail.setText(place.getAddress());

            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(MainActivity.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });




        //set phone number for edtPhoneNumber
        if (FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber() != null &&
                !TextUtils.isEmpty(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {
            edtNumberPhone.setText(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        }

        //set view
        builder.setView(view);
        AlertDialog dialog = builder.create();


        //set event button

        btnContinue.setOnClickListener(v -> {

            progressBar2.setVisibility(View.VISIBLE);

            if(placeSelect!=null)
            {
                if (TextUtils.isEmpty(edtName.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter first name", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_address_detail.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter last name", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(edtNumberPhone.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                } else {
                    UserModel userModel = new UserModel();
                    userModel.setAddress(txt_address_detail .getText().toString());
                    userModel.setName(edtName.getText().toString());
                    userModel.setNumberPhone(edtNumberPhone.getText().toString());
                    userModel.setUid(FirebaseAuth.getInstance().getCurrentUser().getUid());
                    userModel.setLat(placeSelect.getLatLng().latitude);
                    userModel.setLng(placeSelect.getLatLng().longitude);

                    refUser.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .setValue(userModel)
                            .addOnFailureListener(e -> {
                                //
                                progressBar2.setVisibility(View.INVISIBLE);
                                Toast.makeText(MainActivity.this, "[error]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            //
                            progressBar2.setVisibility(View.INVISIBLE);
                            gotoHomeActivity(userModel);
                            Common.curentUser = userModel;
                        }
                    });

                    dialog.dismiss();
                }

            }else
            {
                Toast.makeText(this, "please select address", Toast.LENGTH_SHORT).show();
            }


        });

        dialog.setOnDismissListener(dialog1 -> {
            FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(places_fragment);
            fragmentTransaction.commit();
        });

        dialog.show();
    }

    private void gotoHomeActivity(UserModel userModel) {
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }).addOnCompleteListener(task -> Common.updateToken(MainActivity.this,task.getResult().getToken()));

        Common.curentUser = userModel;
        startActivity(new Intent(MainActivity.this,ActivityHome.class));
        finish();
    }
}