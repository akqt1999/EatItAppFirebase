package etn.app.danghoc.eat_it_.ui.cart;

import android.Manifest;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.app.RemoteInput;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import etn.app.danghoc.eat_it_.Adapter.MyCartAdapter;
import etn.app.danghoc.eat_it_.CallBack.ILoadTimeFromFirebaseListener;
import etn.app.danghoc.eat_it_.CallBack.ISearchCategoryCallbackListener;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Common.MySwiperHelper;
import etn.app.danghoc.eat_it_.Database.CartDataSource;
import etn.app.danghoc.eat_it_.Database.CartDatabase;
import etn.app.danghoc.eat_it_.Database.CartItem;
import etn.app.danghoc.eat_it_.Database.LocalCartDataSource;
import etn.app.danghoc.eat_it_.EventBus.CounterCartEvent;
import etn.app.danghoc.eat_it_.EventBus.HideFABCart;
import etn.app.danghoc.eat_it_.EventBus.MenuItemBack;
import etn.app.danghoc.eat_it_.EventBus.UpdateItemInCart;
import etn.app.danghoc.eat_it_.Model.AddonModel;
import etn.app.danghoc.eat_it_.Model.CategoryModel;
import etn.app.danghoc.eat_it_.Model.FCMSendData;
import etn.app.danghoc.eat_it_.Model.FoodModel;
import etn.app.danghoc.eat_it_.Model.Order;
import etn.app.danghoc.eat_it_.Model.SizeModel;
import etn.app.danghoc.eat_it_.R;
import etn.app.danghoc.eat_it_.Remote.IFCMService;
import etn.app.danghoc.eat_it_.Remote.RetrofitFCMClient;
import io.reactivex.Single;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;
import retrofit2.http.GET;

public class CartFragment extends Fragment implements ILoadTimeFromFirebaseListener, ISearchCategoryCallbackListener, TextWatcher {


    //show dialog
    private BottomSheetDialog addonBottomSheetDialog;
    private ChipGroup chip_group_addon,chip_group_user_select_addon;
    private EditText edt_search;




    private ISearchCategoryCallbackListener iSearchCategoryCallbackListener;

    private CompositeDisposable compositeDisposable = new CompositeDisposable();

    private Parcelable recyclerViewState;
    private CartDataSource cartDataSource;


    LocationRequest locationRequest;
    LocationCallback locationCallback;
    FusedLocationProviderClient fusedLocationProviderClient;
    Location currentLocation;

    MyCartAdapter adapter;


    ILoadTimeFromFirebaseListener listener;

    //notification
    IFCMService ifcmService;


    // places complement
    private Place placeSelect;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);


    @BindView(R.id.recycler_cart)
    RecyclerView recyclerCart;
    @BindView(R.id.txtTotalPrice)
    TextView txtTotalPrice;
    @BindView(R.id.txt_empty_cart)
    TextView txt_empty_cart;
    @BindView(R.id.group_place_holder)
    CardView group_place_holder;

    @OnClick(R.id.btnPlaceOrder)
    void onPlaceOrderClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("one more step");

        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_place_order, null);


        EditText edtAddress = view.findViewById(R.id.edt_address);
        EditText edtComment = view.findViewById(R.id.edt_comment);
        RadioButton rdi_home = view.findViewById(R.id.rdi_home_address);
        RadioButton rdi_other = view.findViewById(R.id.rdi_other_address);
        RadioButton rdi_ship_to_this = view.findViewById(R.id.rdi_ship_this_address);

        RadioButton rdi_cod = view.findViewById(R.id.rdi_cod);
        RadioButton rdi_braintree = view.findViewById(R.id.rdi_braintree);


        places_fragment = (AutocompleteSupportFragment) getActivity().getSupportFragmentManager()
                .findFragmentById(R.id.places_autocomplete_fragment);
        places_fragment.setPlaceFields(placeFields);
        places_fragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                placeSelect = place;
                edtAddress.setText(place.getAddress());
            }

            @Override
            public void onError(@NonNull Status status) {
                Toast.makeText(getContext(), "" + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        //data
        edtAddress.setText(Common.curentUser.getAddress());

        //event
        rdi_home.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked)
                    edtAddress.setText(Common.curentUser.getAddress());
            }
        });

        rdi_other.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edtAddress.setText("");
                    edtAddress.setHint("Enter your address");
                }

            }
        });

        rdi_ship_to_this.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    fusedLocationProviderClient.getLastLocation()
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnCompleteListener(new OnCompleteListener<Location>() {
                                @Override
                                public void onComplete(@NonNull Task<Location> task) {
                                    StringBuilder coordinates = new StringBuilder()
                                            .append(task.getResult().getLatitude())
                                            .append("/")
                                            .append(task.getResult().getLongitude());

                                    Single<String> singleAddress = Single.just(getAddressFromLatLng(task.getResult().getLatitude(),
                                            task.getResult().getLongitude()));

                                    Disposable disposable = singleAddress.subscribeWith(new DisposableSingleObserver<String>() {
                                        @Override
                                        public void onSuccess(String s) {

                                            edtAddress.setText(s);
                                            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });


                                }
                            });
                }
            }
        });


        builder.setView(view);
        builder.setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (rdi_cod.isChecked())
                            paymentCOD(edtAddress.getText().toString(), edtComment.getText().toString());
                    }
                });

        Dialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();

    }

    private void paymentCOD(String address, String comment) {
        compositeDisposable.add(cartDataSource.getAllCart(Common.curentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> cartDataSource.sumPriceInCart(Common.curentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Double>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Double totalPrice) {
                                double finalPrice = totalPrice;
                                Order order = new Order();
                                order.setUserId(Common.curentUser.getUid());
                                order.setUserName(Common.curentUser.getName());
                                order.setUserPhone(Common.curentUser.getNumberPhone());
                                order.setShippingAddress(address);
                                order.setComment(comment);

                                if (currentLocation != null) {
                                    order.setLat(currentLocation.getLatitude());
                                    order.setLng(currentLocation.getLongitude());
                                } else {
                                    order.setLng(-0.1f);
                                    order.setLat(-0.1f);
                                }

                                order.setCartItemList(cartItems);
                                order.setTotalPayment(totalPrice);
                                order.setDiscount(0);
                                order.setFinalPayment(finalPrice);
                                order.setCod(true);
                                order.setTransactionId("Cash on Delivery");

                                //submit this order object to firebase
                                syncLocalTimeWithGlobaltime(order);
                            }

                            @Override
                            public void onError(Throwable e) {

                                if (!e.getMessage().contains("Query returned empty Result set "))
                                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();


                            }
                        }), throwable -> Toast.makeText(getContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show()));
    }

    private void syncLocalTimeWithGlobaltime(Order order) {
        DatabaseReference offsetRef = FirebaseDatabase.getInstance().getReference(".info/serverTimeOffset");
        offsetRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long offset = snapshot.getValue(Long.class);
                long estimatedServerTimeMs = System.currentTimeMillis() + offset;
                SimpleDateFormat sdf = new SimpleDateFormat("MM dd,yyyy HH:mm");
                Date resultDate = new Date(estimatedServerTimeMs);
                Log.d("test_date", "" + sdf.format(resultDate));

                listener.onLoadTimeSuccess(order, estimatedServerTimeMs);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onLoadTimeFailed(error.getMessage());
            }
        });

    }


    private void writeOrderToFirebase(Order order) {
        FirebaseDatabase.getInstance()
                .getReference(Common.ORDER_REF)
                .child(Common.createOrderNumber())  //create order number with only digit
                .setValue(order)
                .addOnCompleteListener(task -> cartDataSource.cleanCart(Common.curentUser.getUid())
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new SingleObserver<Integer>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onSuccess(Integer integer) {

                                Map<String, String> notiData = new HashMap<>();
                                notiData.put(Common.NOTI_TITILE, "New Order");
                                notiData.put(Common.NOTI_CONTENT, "You have new Order from " + Common.curentUser.getNumberPhone());

                                FCMSendData sendData = new FCMSendData(Common.createTopicOrder(), notiData);

                                compositeDisposable.add(ifcmService.sendNotification(sendData)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread())
                                        .subscribe(fcmResponse -> {
                                            Toast.makeText(getContext(), "order success", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));

                                        }, throwable -> {
                                            Toast.makeText(getContext(), "Order was sent but failure to send notification", Toast.LENGTH_SHORT).show();
                                            Log.d("avs", throwable.getMessage());
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        })
                                );

                            }

                            @Override
                            public void onError(Throwable e) {
                                Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        })).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private String getAddressFromLatLng(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String result = "";
        try {
            List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && addressList.size() > 0) {
                Address address = addressList.get(0);//always get first item
                result = address.getAddressLine(0);
            } else
                result = "address not found";

        } catch (IOException e) {
            e.printStackTrace();
            result = e.getMessage();
        }
        return result;
    }


    private Unbinder unbinder;

    CartViewModel cartViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        cartViewModel.initCartDataSource(getContext());
        cartViewModel.getMutableLiveDataCartItem().observe(this, cartItems -> {
            if (cartItems.isEmpty() || cartItems == null) {
                recyclerCart.setVisibility(View.GONE);
                group_place_holder.setVisibility(View.GONE);
                txt_empty_cart.setVisibility(View.VISIBLE);
            } else {
                recyclerCart.setVisibility(View.VISIBLE);
                group_place_holder.setVisibility(View.VISIBLE);
                txt_empty_cart.setVisibility(View.GONE);

                adapter = new MyCartAdapter(getContext(), cartItems);
                recyclerCart.setAdapter(adapter);
            }
        });

        View root = inflater.inflate(R.layout.fragment_cart, container, false);
        unbinder = ButterKnife.bind(this, root);

        ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        init();
        initLocation();

        listener = this;

        return root;
    }

    private void initLocation() {



        buildLocationRequest();
        buildLoactionCallback();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

    }

    private void buildLoactionCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                currentLocation = locationResult.getLastLocation();
            }
        };
    }

    private void buildLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setSmallestDisplacement(10f);
    }

    private void init() {

        setHasOptionsMenu(true);

        iSearchCategoryCallbackListener = this;

        initPlaceClient();

        EventBus.getDefault().isRegistered(this);
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());
        recyclerCart.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerCart.setLayoutManager(layoutManager);
        recyclerCart.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));

        calculateTotalPrice();

        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recyclerCart, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {

                buf.add(new MyButton(getContext(), "Delete", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            CartItem cartItem = adapter.getItemAtPosition(pos);
                            cartDataSource.deleteCartItem(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            adapter.notifyItemChanged(pos);
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));//update fav
                                            calculateTotalPrice();

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), "[DELETE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                ));
//1 ban đầu là ở đây khi nhấn button update nó sẽ làm gì
                buf.add(new MyButton(getContext(), "Update", 30, 0, Color.parseColor("#948281"),
                        pos -> {
                            CartItem cartItem = adapter.getItemAtPosition(pos);//2 lay cart item tai vi tri xac dinh /
                            FirebaseDatabase.getInstance()
                                    .getReference(Common.CATEGORY_REF)
                                    .child(cartItem.getCategoryId())//3 child  id từ máy để lấy giá trị từ firebase
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                CategoryModel categoryModel = snapshot.getValue(CategoryModel.class);
                                                iSearchCategoryCallbackListener.onSearchCategoryFound(categoryModel,cartItem);//4 khi tìm đc sẽ gọi cái này
                                            } else {
                                                iSearchCategoryCallbackListener.onSearchCategoryNotFound("Food not found");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            iSearchCategoryCallbackListener.onSearchCategoryNotFound(error.getMessage());
                                        }
                                    });

                        }
                ));


            }
        };

        //addon
        addonBottomSheetDialog=new BottomSheetDialog(getContext(),R.style.DialogTheme);
        View layout_addon_display=getLayoutInflater().inflate(R.layout.layout_add_on_display,null);
        chip_group_addon=layout_addon_display.findViewById(R.id.chip_group_addon);
        edt_search=layout_addon_display.findViewById(R.id.edtSearch);
        addonBottomSheetDialog.setContentView(layout_addon_display);

        addonBottomSheetDialog.setOnDismissListener(dialog -> {
                displayUserSelectAddon(chip_group_user_select_addon);
                calculateTotalPrice();
        });
    }

    private void displayUserSelectAddon(ChipGroup chip_group_select_addon) {
        if(Common.selectedFood.getUserSelectAddon()!=null&&Common.selectedFood.getUserSelectAddon().size()>0)
        {
            chip_group_select_addon.removeAllViews();
            for(AddonModel addonModel:Common.selectedFood.getUserSelectAddon())
            {
                Chip chip=(Chip)getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if(isChecked)
                    {
                        if(Common.selectedFood.getUserSelectAddon()==null)
                            Common.selectedFood.setUserSelectAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectAddon().add(addonModel);
                    }
                });
                chip_group_select_addon.addView(chip);
            }
        }
        else
            chip_group_select_addon.removeAllViews();
    }

    private void initPlaceClient() {
        Places.initialize(getContext(), "AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8");
        placesClient = Places.createClient(getContext());
    }


    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
        EventBus.getDefault().postSticky(new HideFABCart(true));
    }

    @Override
public void onStop() {

        EventBus.getDefault().postSticky(new HideFABCart(false));
        cartViewModel.onStop();

        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);// xem lai cho that ky

        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        compositeDisposable.clear();
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (fusedLocationProviderClient != null)
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onUpdateItemCartEvent(UpdateItemInCart event) {

        if (event.getCartItem() != null) {
            recyclerViewState = recyclerCart.getLayoutManager().onSaveInstanceState();
            cartDataSource.updateCartItems(event.getCartItem())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {

                            Log.d("bbb", "food quantiny " + event.getCartItem().getFoodQuantity());
                            calculateTotalPrice();
                            recyclerCart.getLayoutManager().onRestoreInstanceState(recyclerViewState);//fix error refresh recycler view after update
                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void calculateTotalPrice() {

        cartDataSource.sumPriceInCart(Common.curentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Double>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Double price) {
                        txtTotalPrice.setText(new StringBuilder("Total: ")
                                .append(Common.formatPrice(price)));
                        //Log.d("bbb","success price "+price);
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty Result set "))
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

    }


    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.action_settings).setVisible(false);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.cart_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_clear_cart) {
            cartDataSource.cleanCart(Common.curentUser.getUid())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            EventBus.getDefault().postSticky(new CounterCartEvent(true));// update fab
                            calculateTotalPrice();
                        }

                        @Override
                        public void onError(Throwable e) {
                            if (!e.getMessage().contains("Query returned empty Result set "))
                                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
            return true;
        }
        return super.onOptionsItemSelected(item);

    }





    @Override
    public void onLoadTimeFailed(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }


    @Override
    public void onLoadTimeSuccess(Order order, long estimateTimeInMs) {
        order.setCreateDate(estimateTimeInMs);
        order.setOrderStatus(0);
        writeOrderToFirebase(order);
    }


    //5 khi goi xong no se lam
    @Override
    public void onSearchCategoryFound(CategoryModel categoryModel,CartItem cartItem) {
        FoodModel foodModel1=Common.findFoodInListById(categoryModel,cartItem.getFoodId());//6 lay foodModel từ category
        if(foodModel1!=null)
        {
            showUpdateDialog(cartItem,foodModel1);//7khi nó khác null nó sẽ làm tác vụ này show lên dialog
        }
        else
        {
            Toast.makeText(getContext(), "food id not found", Toast.LENGTH_SHORT).show();
        }
    }

    private void showUpdateDialog(CartItem cartItem, FoodModel foodModel) { //8 tác vụ show dialog là đây
        Common.selectedFood=foodModel;
        AlertDialog.Builder builder=new AlertDialog.Builder(getContext());
        View itemView=LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_update_cart,null); // loi o cho nay
        builder.setView(itemView);
//ok đc roi
        //view
        //9 những cái này là khởi tạo thôi
        Button btn_ok=itemView.findViewById(R.id.btn_ok);
        Button btn_cancel=itemView.findViewById(R.id.btn_cancel);


        RadioGroup rdi_group_size=(RadioGroup) itemView.findViewById(R.id.rdi_group_size);
        chip_group_user_select_addon=itemView.findViewById(R.id.chip_group_user_select_addon);
        ImageView img_addon=itemView.findViewById(R.id.imgAddAddon);

        //10 sét sự kiện cho click img
        img_addon.setOnClickListener(v -> {
            if(foodModel.getAddon()!=null)
            {
                displayAddonList();//11 khi nhấn sẽ hiện lên cái này bottomSheet sẽ hiện lên
                addonBottomSheetDialog.show();
            }
        });


        // 11 cái này sẽ hiện lên cái size
        //size
        if(foodModel.getSize()!=null) {
            for (SizeModel sizeModel : foodModel.getSize())
            {
                RadioButton radioButton=new RadioButton(getContext());
                radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if(isChecked)
                        Common.selectedFood.setUserSelectSize(sizeModel);
                    calculateTotalPrice();
                });

                LinearLayout.LayoutParams params=new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.MATCH_PARENT,1.0f);
                radioButton.setLayoutParams(params);
                radioButton.setText(sizeModel.getName());
                radioButton.setTag(sizeModel.getPrice());

                //loi la cai rdi_group_size dang bi null
                rdi_group_size.addView(radioButton);
            }

            // 12 neu lon hon khong thi mat dinh la se chon cai dau tien
            if(rdi_group_size.getChildCount()>0)
            {
                RadioButton radioButton =(RadioButton) rdi_group_size.getChildAt(0);//lay gia tri radio button dau tien
                radioButton.setChecked(true);//set mat dinh cai dau tien la true
            }

        }


        //addon
        displayAlreadySelectedAddon(chip_group_user_select_addon,cartItem);//13 se hien len tat ca addon

        //14 show len dialog da hieu
        //show Dialog
        AlertDialog dialog=builder.create();
        dialog.show();

        //custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        //15 khi nhan su kien ok thi no se dong dialog va luu
        //event
        btn_ok.setOnClickListener(v -> {

            //first , delete item in cart
            cartDataSource.deleteCartItem(cartItem)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new SingleObserver<Integer>() {
                        @Override
                        public void onSubscribe(Disposable d) {

                        }

                        @Override
                        public void onSuccess(Integer integer) {
                            //user select addon là addonModel
                            //khi delete thanh cong no se lam lai
                            if(Common.selectedFood.getUserSelectAddon()!=null)
                            cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectAddon()));
                            else
                                cartItem.setFoodAddon("Default");
                            if(Common.selectedFood.getUserSelectSize()!=null)
                            cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectSize()));
                            else
                                cartItem.setFoodSize("Default");

                            cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectSize(),
                                    Common.selectedFood.getUserSelectAddon()));

                            ///insert new
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                            .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                        EventBus.getDefault().postSticky(new CounterCartEvent(true));//count cart again
                                        calculateTotalPrice();
                                        dialog.dismiss();
                                        Toast.makeText(getContext(), "Update cart success", Toast.LENGTH_SHORT).show();
                                    },throwable -> {
                                        Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                    })
                            );

                        //




                        }

                        @Override
                        public void onError(Throwable e) {
                            Toast.makeText(getContext(), ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        });

        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    private void displayAlreadySelectedAddon(ChipGroup chip_group_select_addon, CartItem cartItem) {
        // co nghia la gia tri mat dinh khong them gi thi no se khong hien thi gi
        if(cartItem.getFoodAddon()!=null&&!cartItem.getFoodAddon().equals("Default"))
        {

            //them list addon model
            List<AddonModel>addonModels=new Gson().fromJson(
              cartItem.getFoodAddon(),new TypeToken<List<AddonModel>>(){}.getType());
            Common.selectedFood.setUserSelectAddon(addonModels);
            chip_group_select_addon.removeAllViews();

            //them tung addon model cho tung chip
            for(AddonModel addonModel:Common.selectedFood.getAddon())
            {
                Chip chip=(Chip)getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if(isChecked)
                    {
                        //nếu giá trị thì nó sẽ vao select addon
                        if(Common.selectedFood.getUserSelectAddon()==null)
                            Common.selectedFood.setUserSelectAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectAddon().add(addonModel);
                    }
                });
                chip_group_select_addon.addView(chip);
            }
        }
    }

    private void displayAddonList() {
        if(Common.selectedFood.getAddon()!=null&&Common.selectedFood.getAddon().size()>0)
        {
                chip_group_addon.clearCheck();
                chip_group_addon.removeAllViews();

                edt_search.addTextChangedListener(this);

                // add all view
            for(AddonModel addonModel:Common.selectedFood.getAddon())
            {
                Chip chip=(Chip)getLayoutInflater().inflate(R.layout.layout_add_item,null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if(isChecked)
                    {
                        if(Common.selectedFood.getUserSelectAddon()==null)
                            Common.selectedFood.setUserSelectAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectAddon().add(addonModel);
                    }
                });
                chip_group_addon.addView(chip);
            }
        }
    }


    @Override
    public void onSearchCategoryNotFound(String message) {
        Toast.makeText(getContext(), ""+message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
            chip_group_addon.clearCheck();
            chip_group_addon.removeAllViews();

            for(AddonModel addonModel:Common.selectedFood.getAddon())
            {
                if(addonModel.getName().toLowerCase().contains(s.toString().toLowerCase()))
                {
                    Chip chip=(Chip)getLayoutInflater().inflate(R.layout.layout_add_item,null);
                    chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                            .append(addonModel.getPrice()).append(")"));
                    chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if(isChecked)
                        {
                            if(Common.selectedFood.getUserSelectAddon()==null)
                                Common.selectedFood.setUserSelectAddon(new ArrayList<>());
                            Common.selectedFood.getUserSelectAddon().add(addonModel);
                        }
                    });
                    chip_group_addon.addView(chip);
                }
                }


    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}

//ong co oi con lo lang qua ong co oi , toan la view duyet qua tinh nang xem tien it qua
