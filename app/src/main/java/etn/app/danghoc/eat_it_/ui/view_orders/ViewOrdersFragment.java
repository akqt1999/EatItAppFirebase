package etn.app.danghoc.eat_it_.ui.view_orders;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.androidwidgets.formatedittext.widgets.FormatEditText;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import etn.app.danghoc.eat_it_.Adapter.MyCategoryAdapter;
import etn.app.danghoc.eat_it_.Adapter.MyOrderAdapter;
import etn.app.danghoc.eat_it_.CallBack.ILoadOrderCallbackListener;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Common.MySwiperHelper;
import etn.app.danghoc.eat_it_.Common.SpacesItemDecoration;
import etn.app.danghoc.eat_it_.Database.CartDataSource;
import etn.app.danghoc.eat_it_.Database.CartDatabase;
import etn.app.danghoc.eat_it_.Database.CartItem;
import etn.app.danghoc.eat_it_.Database.LocalCartDataSource;
import etn.app.danghoc.eat_it_.EventBus.CounterCartEvent;
import etn.app.danghoc.eat_it_.Model.CategoryModel;
import etn.app.danghoc.eat_it_.Model.Order;
import etn.app.danghoc.eat_it_.Model.RefundRequestModel;
import etn.app.danghoc.eat_it_.Model.ShippingOrderModel;
import etn.app.danghoc.eat_it_.R;
import etn.app.danghoc.eat_it_.TrackingOrderActivity;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class ViewOrdersFragment extends Fragment implements ILoadOrderCallbackListener {

    private ViewOrderViewModel menuViewModel;
    CompositeDisposable compositeDisposable=new CompositeDisposable();

    CartDataSource cartDataSource;

    private ViewOrderViewModel viewOrderViewModel;

    private ILoadOrderCallbackListener listener;

    Unbinder unbinder;
    @BindView(R.id.recycler_orders)
    RecyclerView recycler_orders;

    AlertDialog dialog;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewOrderViewModel =
                ViewModelProviders.of(this).get(ViewOrderViewModel.class);
        View root = inflater.inflate(R.layout.fragment_view_order, container, false);
        unbinder = ButterKnife.bind(this, root);

        initView();

        loadOrderFromFirebase();

        viewOrderViewModel.getMutableLiveDataOrderList().observe(this, orderList -> {
            Collections.reverse(orderList);
            MyOrderAdapter adapter = new MyOrderAdapter(getContext(), orderList);
            recycler_orders.setAdapter(adapter);
        });

        return root;
    }

    private void loadOrderFromFirebase() {
        List<Order> orderList = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                .orderByChild("userId")
                .equalTo(Common.curentUser.getUid())
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot orderSnapshot : snapshot.getChildren()) {
                            Order order = orderSnapshot.getValue(Order.class);
                            order.setOrderNumber(orderSnapshot.getKey());//
                            orderList.add(order);
                        }
                        listener.onLoadOrderSuccess(orderList);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onLoadOrderFailed(error.getMessage());
                    }
                });
    }

    private void initView() {

        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();
        recycler_orders.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recycler_orders.setLayoutManager(linearLayoutManager);
        recycler_orders.addItemDecoration(new DividerItemDecoration(getContext(), linearLayoutManager.getOrientation()));

        listener = this;


        MySwiperHelper mySwiperHelper = new MySwiperHelper(getContext(), recycler_orders, 200) {
            @Override
            public void instantiateMyButton(RecyclerView.ViewHolder viewHolder, List<MyButton> buf) {
                buf.add(new MyButton(getContext(), "Cancel Order", 30, 0, Color.parseColor("#FF3C30"),
                        pos -> {
                            Order orderModel = ((MyOrderAdapter) recycler_orders.getAdapter()).getItemAtPosition(pos);
                            if (orderModel.getOrderStatus() == 0) {
                                if (orderModel.isCod()) {
                                    // xoa la xoa lun khong noi nhieu
                                    Map<String, Object> update_data = new HashMap<>();
                                    update_data.put("OrderStatus", -1);//-1 is cancel

                                    FirebaseDatabase.getInstance()
                                            .getReference(Common.ORDER_REF)
                                            .child(orderModel.getOrderNumber())
                                            .updateChildren(update_data)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    orderModel.setOrderStatus(-1);
                                                    ((MyOrderAdapter) recycler_orders.getAdapter()).setITemAtPosition(pos, orderModel);
                                                    recycler_orders.getAdapter().notifyItemChanged(pos);
                                                    Toast.makeText(getContext(), "Cancel order successfully", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show());

                                } else//not cod(thanh toan khi nhạn hang)
                                {

                                    androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());


                                    View layout_refund_request = LayoutInflater.from(getContext())
                                            .inflate(R.layout.layout_refund_request, null);
                                    EditText edt_card_name = layout_refund_request.findViewById(R.id.edt_cart_name);
                                    FormatEditText edt_card_number = layout_refund_request.findViewById(R.id.edt_card_number);
                                    FormatEditText edt_card_exp = layout_refund_request.findViewById(R.id.edt_exp);

                                    //forMAT credit card
                                    edt_card_number.setFormat("---- ---- ---- ----");
                                    edt_card_exp.setFormat("--/--");

                                    builder.setTitle("Cancel Order")
                                            .setMessage("please fill info your card")
                                            .setView(layout_refund_request)
                                            .setNegativeButton("NO", (dialog, which) -> dialog.dismiss())
                                            .setPositiveButton("YES", (dialog, which) -> {
                                                RefundRequestModel refundRequestModel = new RefundRequestModel();
                                                refundRequestModel.setName(Common.curentUser.getName());
                                                refundRequestModel.setPhone(Common.curentUser.getNumberPhone());
                                                refundRequestModel.setCartName(edt_card_name.getText().toString());
                                                refundRequestModel.setCardExp(edt_card_exp.getText().toString());
                                                refundRequestModel.setCartName(edt_card_number.getText().toString());
                                                refundRequestModel.setAmount(orderModel.getFinalPayment());


                                                FirebaseDatabase.getInstance()
                                                        .getReference(Common.REQUEST_REFUND_MODEL)
                                                        .child(orderModel.getOrderNumber())
                                                        .setValue(refundRequestModel)
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Map<String, Object> update_data = new HashMap<>();
                                                                update_data.put("orderStatus", -1);//-1 is cancel

                                                                FirebaseDatabase.getInstance()
                                                                        .getReference(Common.ORDER_REF)
                                                                        .child(orderModel.getOrderNumber())
                                                                        .updateChildren(update_data)
                                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                            @Override
                                                                            public void onSuccess(Void aVoid) {
                                                                                orderModel.setOrderStatus(-1);
                                                                                ((MyOrderAdapter) recycler_orders.getAdapter()).setITemAtPosition(pos, orderModel);
                                                                                recycler_orders.getAdapter().notifyItemChanged(pos);
                                                                                Toast.makeText(getContext(), "Cancel order successfully", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        })
                                                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show());
                                                            }
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show());

                                            });


                                    androidx.appcompat.app.AlertDialog dialog = builder.create();
                                    dialog.show();

                                }
                            } else {
                                Toast.makeText(getContext(), "" + new StringBuilder("Your order was changed to ")
                                        .append(Common.convertStatusToText(orderModel.getOrderStatus()))
                                        .append(", so you can't cancel it!"), Toast.LENGTH_SHORT).show();
                            }

                        }));


                // button tracking order
                buf.add(new MyButton(getContext(), "Tracking Order", 30, 0, Color.parseColor("#001970"),
                        pos -> {
                            Order orderModel = ((MyOrderAdapter) recycler_orders.getAdapter()).getItemAtPosition(pos);

                            //get data from
                            FirebaseDatabase.getInstance()
                                    .getReference(Common.SHiPER_ORDER_REF)
                                    .child(orderModel.getOrderNumber())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.exists()) {
                                                //
                                                Common.curentShippingOrder = snapshot.getValue(ShippingOrderModel.class);
                                                Common.curentShippingOrder.setKey(snapshot.getKey());
                                                if (Common.curentShippingOrder.getCurrentLat() != 1 && Common.curentShippingOrder.getCurrentLng() != -1) {
                                                    startActivity(new Intent(getContext(), TrackingOrderActivity.class));
                                                } else {
                                                    Toast.makeText(getContext(), "Shipper not start ship your order, just wait", Toast.LENGTH_SHORT).show();
                                                }
                                            } else {
                                                ShippingOrderModel shippingOrderModel = snapshot.getValue(ShippingOrderModel.class);
                                                Toast.makeText(getContext(), "your order just placed , you must wait it shipping", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Toast.makeText(getContext(), "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }));


                // button repeat order
                buf.add(new MyButton(getContext(), "Repeat Order", 30, 0, Color.parseColor("#736d7a"),
                        pos -> {

                            Order orderModel = ((MyOrderAdapter) recycler_orders.getAdapter()).getItemAtPosition(pos);
                            Toast.makeText(getContext(), "Repeat order", Toast.LENGTH_SHORT).show();
                            dialog.show();


                            //clear all cart
                            cartDataSource.cleanCart(Common.curentUser.getUid())
                                    .subscribeOn(Schedulers.io())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            // co nghia la xoa toan bo cai cu them cai moi vao
                                            CartItem[]cartItems=orderModel
                                                    .getCartItemList().toArray(new CartItem[orderModel.getCartItemList().size()]);
                                                //insert new
                                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItems)
                                            .subscribeOn(Schedulers.io())
                                            .observeOn(AndroidSchedulers.mainThread())
                                            .subscribe(()->
                                            {
                                                dialog.dismiss();
                                                Toast.makeText(getContext(), "Add all item in order to cart success", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));//count fab

                                            },throwable -> {
                                                dialog.dismiss();
                                                Toast.makeText(getContext(), ""+throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            })
                                            );

                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            dialog.dismiss();
                                            Toast.makeText(getContext(), "[error]"+e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                        }));


            }
        };

    }


    @Override
    public void onLoadOrderSuccess(List<Order> orderList) {
        dialog.dismiss();

        viewOrderViewModel.setMutableLiveDataOrderList(orderList);
    }

    @Override
    public void onLoadOrderFailed(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }
}

