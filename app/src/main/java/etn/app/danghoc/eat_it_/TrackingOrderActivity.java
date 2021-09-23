package etn.app.danghoc.eat_it_;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Database;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.ButterKnife;
import butterknife.OnClick;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Common.MyCustomMarkerAdapter;
import etn.app.danghoc.eat_it_.Model.ShippingOrderModel;
import etn.app.danghoc.eat_it_.Remote.IGoogleAPI;
import etn.app.danghoc.eat_it_.Remote.RetrofitGoogleAPIClient;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class TrackingOrderActivity extends FragmentActivity implements OnMapReadyCallback, ValueEventListener {

    private GoogleMap mMap;
    private Marker shipperMaker;

    private PolylineOptions polylineOptions, blackPolylineOption;
    private List<LatLng> polylineList;
    private Polyline yellowPolyline, grayPolyline, blackPolyline;
    // hua khonf thu dma nuya met maas, di oing comoi coj met qau di thi dam ed djif cai dusuc khoea con mbinh thuiiong maf con dien ua do thoi
    private IGoogleAPI iGoogleAPI;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();


    private DatabaseReference shipperRef;

    //move marker
    private Handler handler;
    private int index, next;
    private LatLng start, end;
    private float v;
    private double lat, lng;

    private boolean isInit = false;


    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracking_order_actitity);

        ButterKnife.bind(this);

        iGoogleAPI = RetrofitGoogleAPIClient.getInstance().create(IGoogleAPI.class);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        subscribeShipperMover();


    }

    private void subscribeShipperMover() {
        shipperRef = FirebaseDatabase.getInstance()
                .getReference(Common.SHiPER_ORDER_REF)
                .child(Common.curentShippingOrder.getKey());

        // su dung addValueEvenListener : de lang nghe su kien khi co su thay doi doi du lieu tu cai khac
        shipperRef.addValueEventListener(this);
    }

    @OnClick(R.id.btn_call)
    void onCallClick() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

            //request permission
            Dexter.withContext(this)
                    .withPermission(Manifest.permission.CALL_PHONE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {

                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                            Toast.makeText(TrackingOrderActivity.this, "you must enable this permission to CALL", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                        }
                    }).check();

            return;
        }

        Intent intent = new Intent(Intent.ACTION_CALL);
        intent.setData(Uri.parse(new StringBuilder("tel:").append(Common.curentShippingOrder.getShipperPhone()).toString()));

        startActivity(intent);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);

        mMap.setInfoWindowAdapter(new MyCustomMarkerAdapter(getLayoutInflater()));

        // Add a marker in Sydney and move the camera
        LatLng locationShipper = new LatLng(Common.curentShippingOrder.getCurrentLat(),
                Common.curentShippingOrder.getCurrentLng());
        Log.d("sss","ready_map");
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 18));

        drawRoutes();
    }

    private void drawRoutes() {
        LatLng locationOrder = new LatLng(Common.curentShippingOrder.getOrderModel().getLat(),
                Common.curentShippingOrder.getOrderModel().getLng());
        LatLng locationShipper = new LatLng(Common.curentShippingOrder.getCurrentLat(),
                Common.curentShippingOrder.getCurrentLng());


        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 18));

        //add shipper
        if (shipperMaker == null) {
            int height, width;
            height = width = 80;
            BitmapDrawable bitmapDrawable = (BitmapDrawable) ContextCompat
                    .getDrawable(TrackingOrderActivity.this, R.drawable.shipper_new);
            Bitmap resized = Bitmap.createScaledBitmap(bitmapDrawable.getBitmap(), width, height, false);

            shipperMaker = mMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.fromBitmap(resized))
                    .title(new StringBuilder("Shipper: ").append(Common.curentShippingOrder.getShipperName()).toString())
                    .snippet(new StringBuilder("Phone: ").append(Common.curentShippingOrder.getShipperPhone())
                            .append("\n")
                    .append("Estimate Time Delivery: ")
                    .append(Common.curentShippingOrder.getEstimateTime()).toString())
                    .position(locationShipper)
            );
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(locationShipper, 18));


            //always show information
            shipperMaker.showInfoWindow();

        } else {
            shipperMaker.setPosition(locationShipper);

        }

        //draw router
        String to = new StringBuilder()
                .append(Common.curentShippingOrder.getOrderModel().getLat())
                .append(",")
                .append(Common.curentShippingOrder.getOrderModel().getLng())
                .toString();
        String from = new StringBuilder()
                .append(Common.curentShippingOrder.getCurrentLat())
                .append(",")
                .append(Common.curentShippingOrder.getCurrentLng())
                .toString();

        // api google
        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                from, to,
                "AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {

                    try {
                        JSONObject jsonObject = new JSONObject(s);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polylineList = Common.decodePoly(polyline);
                        }
                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.RED);//mau do 12
                        polylineOptions.width(12);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polylineList);
                        yellowPolyline = mMap.addPolyline(polylineOptions);

                    } catch (Exception e) {
                        Toast.makeText(TrackingOrderActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                    ;
                }
                        , new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable throwable) throws Exception {
                                Toast.makeText(TrackingOrderActivity.this, "" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                )
        );

    }


    @Override
    protected void onDestroy() {
        shipperRef.removeEventListener(this);
        isInit=false;
        super.onDestroy();
    }

    @Override
    public void onDataChange(@NonNull DataSnapshot snapshot) {

        String from=new StringBuilder()
                .append(Common.curentShippingOrder.getCurrentLat())
                .append(",")
                .append(Common.curentShippingOrder.getCurrentLng())
                .toString();

        //update position
        Common.curentShippingOrder=snapshot.getValue(ShippingOrderModel.class);
        Common.curentShippingOrder.setKey(snapshot.getKey());

        //save new position
        String to=new StringBuilder()
                .append(Common.curentShippingOrder.getCurrentLat())
                .append(",")
                .append(Common.curentShippingOrder.getCurrentLng())
                .toString();


        if(snapshot.exists())
        {
            if(isInit)
                moveMakerAnimation(shipperMaker,from,to);
            else
                isInit=true;
        }


    }

    private void moveMakerAnimation(Marker shipperMaker, String from, String to) {
        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                from, to,"AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8"
                ).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( s->
                        {

                            try {
                                JSONObject jsonObject = new JSONObject(s);
                                JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject route = jsonArray.getJSONObject(i);
                                    JSONObject poly = route.getJSONObject("overview_polyline");
                                    String polyline = poly.getString("points");
                                    polylineList = Common.decodePoly(polyline);
                                }
                                polylineOptions = new PolylineOptions();
                                polylineOptions.color(Color.GRAY);
                                polylineOptions.width(12);
                                polylineOptions.startCap(new SquareCap());
                                polylineOptions.jointType(JointType.ROUND);
                                polylineOptions.addAll(polylineList);

                                grayPolyline = mMap.addPolyline(polylineOptions);

                                blackPolylineOption = new PolylineOptions();
                                blackPolylineOption.color(Color.BLACK);
                                blackPolylineOption.width(5);
                                blackPolylineOption.startCap(new SquareCap());
                                blackPolylineOption.jointType(JointType.ROUND);
                                blackPolylineOption.addAll(polylineList);

                                blackPolyline=mMap.addPolyline(blackPolylineOption);

                                //animator
                                ValueAnimator polylineAnimator=ValueAnimator.ofInt(0,100);
                                polylineAnimator.setDuration(2000);
                                polylineAnimator.setInterpolator(new LinearInterpolator());
                                polylineAnimator.addUpdateListener(animation -> {
                                        List<LatLng>points=grayPolyline.getPoints();
                                        int percentValue=(int)animation.getAnimatedValue();
                                        int size=points.size();
                                        int newPoints=(int)(size*(percentValue/100.0f));
                                        List<LatLng>p=points.subList(0,newPoints);
                                        blackPolyline.setPoints(p);
                                });

                                polylineAnimator.start();

                                //bike moving
                                handler=new Handler();
                                index=-1;
                                next=1;

                                handler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {

                                        if(index<polylineList.size()-1)
                                        {
                                            index++;
                                            next=index+1;
                                            start=polylineList.get(index);
                                            end=polylineList.get(next);
                                        }

                                        ValueAnimator valueAnimator=ValueAnimator.ofInt(0,1);
                                        valueAnimator.setDuration(1500);
                                        valueAnimator.setInterpolator(new LinearInterpolator());
                                        valueAnimator.addUpdateListener(animation -> {
                                            v=valueAnimator.getAnimatedFraction();
                                            lng=v*end.longitude+(1-v)
                                                    *start.longitude;
                                            lat=v*end.latitude+(1-v)
                                                    *start.latitude;
                                            LatLng newPos=new LatLng(lat,lng);
                                            shipperMaker.setPosition(newPos);
                                            shipperMaker.setAnchor(0.5f,0.5f);
                                            shipperMaker.setRotation(Common.getBearing(start,newPos));

                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPos,10f));

                                        });

                                        valueAnimator.start();
                                        if(index<polylineList.size()-2)//reach destination
                                            handler.postDelayed(this,1500);
                                    }
                                },1500);


                            } catch (Exception e) {
                                Toast.makeText(TrackingOrderActivity.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }



                        },throwable -> {
                    Toast.makeText(this, ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                }));
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }


}

/*
lý thuyết mình đặt ra
cái như cũ khi chạy thì location callback se hoat dong khi no hoat dong se lam tra cac gia tri location hien tai
khi ra gia tri hien tai thi no se goi get poline se tao ra icon theo huongpolile do

bay gio neu 1 cai location callback thi  no sẽ làm child cai gfia trokj món ăn lên firebase get xuống
 goi phu8wong thức tuong tự

luc co view con lo lang ve viet mat kenh , luc khong lo lang ve gay nua , thi lai lo lang ve viet khong co view

 */



