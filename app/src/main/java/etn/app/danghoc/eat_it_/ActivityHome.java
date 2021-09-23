package etn.app.danghoc.eat_it_;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.google.android.gms.common.api.Status;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.gson.internal.bind.JsonTreeReader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.reactivestreams.Subscription;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import dmax.dialog.SpotsDialog;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Database.CartDataSource;
import etn.app.danghoc.eat_it_.Database.CartDatabase;
import etn.app.danghoc.eat_it_.Database.CartItem;
import etn.app.danghoc.eat_it_.Database.LocalCartDataSource;
import etn.app.danghoc.eat_it_.EventBus.BestDealItemClick;
import etn.app.danghoc.eat_it_.EventBus.CategoryClick;
import etn.app.danghoc.eat_it_.EventBus.CounterCartEvent;
import etn.app.danghoc.eat_it_.EventBus.FoodItemClick;
import etn.app.danghoc.eat_it_.EventBus.HideFABCart;
import etn.app.danghoc.eat_it_.EventBus.MenuItemBack;
import etn.app.danghoc.eat_it_.EventBus.MenuItemEvent;
import etn.app.danghoc.eat_it_.EventBus.PopularCategoryClick;
import etn.app.danghoc.eat_it_.Model.BestDealModel;
import etn.app.danghoc.eat_it_.Model.CategoryModel;
import etn.app.danghoc.eat_it_.Model.FoodModel;
import etn.app.danghoc.eat_it_.Model.PopularCategoryModel;
import etn.app.danghoc.eat_it_.Model.UserModel;
import etn.app.danghoc.eat_it_.ui.home.HomeViewModel;
import io.paperdb.Paper;
import io.reactivex.FlowableSubscriber;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ActivityHome extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private AppBarConfiguration mAppBarConfiguration;
    NavController navController;
    DrawerLayout drawer;

    private CartDataSource cartDataSource;

    private NavigationView navigationView;

    int menuClickId=-1;

    android.app.AlertDialog dialog;

    // place completion
    private Place placeSelect;
    private AutocompleteSupportFragment places_fragment;
    private PlacesClient placesClient;
    private List<Place.Field>placeFields= Arrays.asList(Place.Field.ID,
            Place.Field.NAME,
            Place.Field.ADDRESS,
            Place.Field.LAT_LNG);

    @BindView(R.id.fab)
    CounterFab fab;


    @Override
    protected void onResume() {
        super.onResume();
        countCartItem();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FirebaseMessaging.getInstance()
                .subscribeToTopic(Common.NEWS_TOPIC);
         initPlaceClient();

        dialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        ButterKnife.bind(this);

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(this).cartDAO());


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> navController.navigate(R.id.nav_cart));
        drawer = findViewById(R.id.drawer_layout);
         navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_restaurant,
                R.id.nav_home, R.id.nav_menu, R.id.nav_food_list, R.id.nav_food_detail, R.id.nav_cart,R.id.nav_view_orders)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        TextView txt_user = headerView.findViewById(R.id.txt_user);
        Common.setSpanString("Hey, ", Common.curentUser.getName(), txt_user);

        countCartItem();


    }

    private void initPlaceClient() {
        Places.initialize(this,"AIzaSyDuHZVu9CES-fDz891ZPuluH0k-JIlsrV8");
        placesClient=Places.createClient(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void categorySelect(CategoryClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_food_list);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void foodSelect(FoodItemClick event) {
        if (event.isSuccess()) {
            navController.navigate(R.id.nav_food_detail);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onCartCounter(CounterCartEvent event) {
        if (event.isSuccess()) {
            countCartItem();
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onHideFAVEvent(HideFABCart event) {
        if (event.isHidden()) {
            fab.hide();
        } else
            fab.show();
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onPopularItemClick(PopularCategoryClick event) {
        if (event.getPopularCategoryModel() != null) {
            dialog.show();

            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getPopularCategoryModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {

                                Common.categorySelected = snapshot.getValue(CategoryModel.class);

                                //load food
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getPopularCategoryModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getPopularCategoryModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.exists()) {

                                                    dialog.dismiss();
                                                    for (DataSnapshot itemSnapShot : snapshot.getChildren()) {
                                                        Common.selectedFood = itemSnapShot.getValue(FoodModel.class);
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                } else {

                                                    dialog.dismiss();
                                                    Toast.makeText(ActivityHome.this, "Item doesn't exists ", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Log.d("aaaa", "4");
                                                Toast.makeText(ActivityHome.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });
                            } else {
                                dialog.dismiss();

                                Toast.makeText(ActivityHome.this, "Item doesn't exists", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();

                            Toast.makeText(ActivityHome.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onBestDealItemClick(BestDealItemClick event) {
        if (event.getBestDealModel() != null) {
            dialog.show();

            FirebaseDatabase.getInstance()
                    .getReference("Category")
                    .child(event.getBestDealModel().getMenu_id())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {

                                Common.categorySelected = snapshot.getValue(CategoryModel.class);

                                //load food
                                FirebaseDatabase.getInstance()
                                        .getReference("Category")
                                        .child(event.getBestDealModel().getMenu_id())
                                        .child("foods")
                                        .orderByChild("id")
                                        .equalTo(event.getBestDealModel().getFood_id())
                                        .limitToLast(1)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(@NonNull DataSnapshot snapshot) {

                                                if (snapshot.exists()) {

                                                    dialog.dismiss();
                                                    for (DataSnapshot itemSnapShot : snapshot.getChildren()) {
                                                        Common.selectedFood = itemSnapShot.getValue(FoodModel.class);
                                                    }
                                                    navController.navigate(R.id.nav_food_detail);
                                                } else {

                                                          dialog.dismiss();
                                                    Toast.makeText(ActivityHome.this, "Item doesn't exists ", Toast.LENGTH_SHORT).show();
                                                }
                                                dialog.dismiss();
                                            }

                                            @Override
                                            public void onCancelled(@NonNull DatabaseError error) {
                                                dialog.dismiss();
                                                Log.d("aaaa", "4");
                                                Toast.makeText(ActivityHome.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();

                                            }
                                        });
                            } else {
                                dialog.dismiss();

                                Toast.makeText(ActivityHome.this, "Item doesn't exists", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            dialog.dismiss();

                            Toast.makeText(ActivityHome.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }


    }

    private void countCartItem() {
        cartDataSource.countItemCart(Common.curentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(Integer integer) {
                        fab.setCount(integer);
                        Log.d("aaa", "number : " + integer + "");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!e.getMessage().contains("Query returned empty"))
                            Toast.makeText(ActivityHome.this, "[COUNT CART ]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        else
                            fab.setCount(0);
                    }
                });


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        item.setChecked(true);
        drawer.close();
        switch (item.getItemId()) {
            case R.id.nav_food_detail:
                if(item.getItemId()!=menuClickId)
                navController.navigate(R.id.nav_food_detail);
                break;
            case R.id.nav_restaurant:
                if(item.getItemId()!=menuClickId)
                    navController.navigate(R.id.nav_restaurant);
                break;
            case R.id.nav_home:
                if(item.getItemId()!=menuClickId)
                navController.navigate(R.id.nav_home);
                break;
            case R.id.nav_menu:
                if(item.getItemId()!=menuClickId)
                navController.navigate(R.id.nav_menu);
                break;
            case R.id.nav_food_list:
                if(item.getItemId()!=menuClickId)
                navController.navigate(R.id.nav_food_list);
                break;
            case R.id.nav_cart:
                if(item.getItemId()!=menuClickId)
                navController.navigate(R.id.nav_cart);
                break;
            case R.id.nav_sign_out:
                if(item.getItemId()!=menuClickId)
                signOut();
                break;
            case R.id.nav_view_orders :
                if(item.getItemId()!=menuClickId)//kiem tra k goi toi ham nay nua
                navController.navigate(R.id.nav_view_orders);
                break;
            case R.id.nav_update_info:
                if(item.getItemId()!=menuClickId)
                    showUpdateInfoDialog();
                break;
            case R.id.nav_news:
                if(item.getItemId()!=menuClickId)
                    showSubscribeNews();
                break;

        }
        menuClickId=item.getItemId();
        return true;
    }

    private void showSubscribeNews() {

        Paper.init(this);

        AlertDialog.Builder builder=new AlertDialog.Builder(this,R.style.DialogTheme);

        builder.setTitle("News System");
            builder.setMessage("Do you want to subscribe news from our restaurant");

            View itemView=LayoutInflater.from(this).inflate(R.layout.layout_subscribe_news,null);
        CheckBox ckb_news=itemView.findViewById(R.id.ckb_subscribe_news);
        boolean isSubScribeNes=Paper.book().read(Common.IS_SUBSCRIBE_NEWS,false);
        if(isSubScribeNes)
            ckb_news.setChecked(true);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Send", (dialog, which) -> {

            if(ckb_news.isChecked())
            {

                Paper.book().write(Common.IS_SUBSCRIBE_NEWS,true);

                FirebaseMessaging.getInstance()
                        .subscribeToTopic(Common.NEWS_TOPIC)
                .addOnFailureListener(e -> Toast.makeText(ActivityHome.this, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(aVoid -> Toast.makeText(ActivityHome.this, "Subscribe successfully", Toast.LENGTH_SHORT).show());
            }
            else
            {
                Paper.book().write(Common.IS_SUBSCRIBE_NEWS,false);

                FirebaseMessaging.getInstance()
                        .unsubscribeFromTopic(Common.NEWS_TOPIC)
                        .addOnSuccessListener(aVoid -> Toast.makeText(ActivityHome.this, "Unsubscribe successfully", Toast.LENGTH_SHORT).show()).addOnFailureListener(e -> Toast.makeText(ActivityHome.this, e.getMessage(), Toast.LENGTH_SHORT).show());
            }

        });

        builder.setView(itemView);
        AlertDialog dialog=builder.create();
        dialog.show();

    }

    private void showUpdateInfoDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.DialogTheme);
        builder.setTitle("Update info");
        builder.setMessage("please fill information");
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
                Toast.makeText(ActivityHome.this, ""+status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        });




        //set phone number for edtPhoneNumber
     edtNumberPhone.setText(Common.curentUser.getNumberPhone());
    txt_address_detail.setText(Common.curentUser.getAddress());
    edtName.setText(Common.curentUser.getName());

        //set view
        builder.setView(view);
        AlertDialog dialog = builder.create();


        //set event button

        btnContinue.setOnClickListener(v -> {

            progressBar2.setVisibility(View.VISIBLE);

            if(placeSelect!=null)
            {
                if (TextUtils.isEmpty(edtName.getText().toString())) {
                    Toast.makeText(ActivityHome.this, "Please enter first name", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(txt_address_detail.getText().toString())) {
                    Toast.makeText(ActivityHome.this, "Please enter last name", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(edtNumberPhone.getText().toString())) {
                    Toast.makeText(ActivityHome.this, "Please enter phone number", Toast.LENGTH_SHORT).show();
                } else {


                    dialog.dismiss();
                }
                Map<String,Object>update_data=new HashMap<>();
                update_data.put("name",edtName.getText().toString());
                update_data.put("address",txt_address_detail.getText().toString());
                update_data.put("lat",placeSelect.getLatLng().latitude);
                update_data.put("lng",placeSelect.getLatLng().longitude);

                FirebaseDatabase.getInstance()
                       .getReference(Common.USER_INFO_REF)
                        .child(Common.curentUser.getUid())
                        .updateChildren(update_data)
                        .addOnFailureListener(e -> {
                            dialog.dismiss();
                            Toast.makeText(ActivityHome.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                        })
                        .addOnSuccessListener(aVoid -> {
                                 dialog.dismiss();
                            Toast.makeText(ActivityHome.this, "Update info success", Toast.LENGTH_SHORT).show();
                            Common.curentUser.setAddress(update_data.get("address").toString());
                            Common.curentUser.setName(update_data.get("name").toString());
                            Common.curentUser.setLat(Double.parseDouble(update_data.get("lat").toString()));
                            Common.curentUser.setLng(Double.parseDouble(update_data.get("lng").toString()));
                        });

            }else
            {
                Toast.makeText(this, "Please select address", Toast.LENGTH_SHORT).show();
            }


        });

        dialog.setOnDismissListener(dialog1 -> {
            FragmentTransaction fragmentTransaction=getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(places_fragment);
            fragmentTransaction.commit();
        });

        dialog.show();

    }

    private void signOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Sign Out")
                .setMessage("do you  really  want to sign out")
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Common.selectedFood = null;
                        Common.categorySelected = null;
                        Common.curentUser = null;
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(ActivityHome.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    }
                });

        AlertDialog dialog;
        dialog = builder.create();
        dialog.show();


    }


    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onMenuItemBack(MenuItemBack event)
    {
        menuClickId=-1;
        if(getSupportFragmentManager().getBackStackEntryCount()>0)
            getSupportFragmentManager().popBackStack();

    }

    @Subscribe(sticky = true,threadMode = ThreadMode.MAIN)
    public void onRestaurantClick(MenuItemEvent event)
    {
        Bundle bundle=new Bundle();
        bundle.putString("restaurant",event.getRestaurantModel().getUid());
        navController.navigate(R.id.nav_home,bundle);
        navigationView.getMenu().clear();
       navigationView.inflateMenu(R.menu.activity_main_drawer);
    }



}
















