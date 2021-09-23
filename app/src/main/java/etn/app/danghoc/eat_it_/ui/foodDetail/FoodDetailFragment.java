package etn.app.danghoc.eat_it_.ui.foodDetail;

import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.andremion.counterfab.CounterFab;
import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import etn.app.danghoc.eat_it_.Adapter.MyFoodListAdapter;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Database.CartDataSource;
import etn.app.danghoc.eat_it_.Database.CartDatabase;
import etn.app.danghoc.eat_it_.Database.CartItem;
import etn.app.danghoc.eat_it_.Database.LocalCartDataSource;
import etn.app.danghoc.eat_it_.EventBus.CounterCartEvent;
import etn.app.danghoc.eat_it_.EventBus.MenuItemBack;
import etn.app.danghoc.eat_it_.Model.AddonModel;
import etn.app.danghoc.eat_it_.Model.CommentModel;
import etn.app.danghoc.eat_it_.Model.FoodModel;
import etn.app.danghoc.eat_it_.Model.SizeModel;
import etn.app.danghoc.eat_it_.R;
import etn.app.danghoc.eat_it_.ui.commet.CommentFragment;
import io.reactivex.SingleObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class FoodDetailFragment extends Fragment implements TextWatcher {

    private FoodDetailViewModel foodDetaiViewModel;
    private BottomSheetDialog addOnBottomSheetDialog;
    FoodDetailViewModel foodDetailViewModel;
    android.app.AlertDialog waitingDialog;

    private CartDataSource cartDataSource;
    private CompositeDisposable compositeDisposable=new CompositeDisposable();

    //view need inflate
    ChipGroup chipGroupAddon;
    EditText edtSearch;

    Unbinder unbinder;
    @BindView(R.id.imgFood)
    ImageView imgFood;
    @BindView(R.id.txtFoodName)
    TextView txtFoodName;
    @BindView(R.id.txtFoodPrice)
    TextView txtFoodPrice;
    @BindView(R.id.txtFoodDescription)
    TextView txtFoodDescription;
    @BindView(R.id.btnShowComment)
    Button btnShowComment;
    @BindView(R.id.btnRating)
    FloatingActionButton btnRating;
    @BindView(R.id.btnCart)
    CounterFab btnCart;
    @BindView(R.id.ratingBar)
    RatingBar ratingBar;
    @BindView(R.id.radGroupSize)
    RadioGroup radGroupSize;
    @BindView(R.id.btnNumberButton)
    ElegantNumberButton btnNumberButton;
    @BindView(R.id.imgAddAddon)
    ImageView imgAddOn;
    @BindView(R.id.chip_group_user_select_addon)
    ChipGroup chip_group_user_select_addon;

    @OnClick(R.id.btnRating)
    void onRatingButtonClick() {
        showDialogRating();
    }

    @OnClick(R.id.btnShowComment)
    void onShowRatingClick() {
        CommentFragment commentFragment = CommentFragment.getInstance();
        commentFragment.show(getActivity().getSupportFragmentManager(), "CommentFragment");
    }

    @OnClick(R.id.imgAddAddon)
    void onAddonClick() {
        if (Common.selectedFood.getAddon() != null) {
            displayAddonList();
            addOnBottomSheetDialog.show();

        }
    }

    @OnClick(R.id.btnCart)
    void onCartItemAdd() {
        CartItem cartItem = new CartItem();

        cartItem.setUid(Common.curentUser.getUid());
        cartItem.setUserPhone(Common.curentUser.getNumberPhone());

        cartItem.setCategoryId(Common.categorySelected.getMenuId());
        cartItem.setFoodId(Common.selectedFood.getId());
        cartItem.setFoodName(Common.selectedFood.getName());
        cartItem.setFoodImage(Common.selectedFood.getImage());
        cartItem.setFoodPrice((double) Common.selectedFood.getPrice());
        cartItem.setFoodQuantity(Integer.valueOf( btnNumberButton.getNumber()));
        cartItem.setFoodExtraPrice(Common.calculateExtraPrice(Common.selectedFood.getUserSelectSize(),Common.selectedFood.getUserSelectAddon()));  //default we not choose size & addon

        Log.d("aaa"," btnNumberButton :"+btnNumberButton.getNumber());

        if(Common.selectedFood.getUserSelectAddon()!=null)
            cartItem.setFoodAddon(new Gson().toJson(Common.selectedFood.getUserSelectAddon()));
        else
            cartItem.setFoodAddon("Default");

        if(Common.selectedFood.getUserSelectSize()!=null)
            cartItem.setFoodSize(new Gson().toJson(Common.selectedFood.getUserSelectSize()));
        else
            cartItem.setFoodSize("Default");


        cartDataSource.getItemWithAllOptionsInCart(Common.curentUser.getUid(),
                cartItem.getFoodId(),
                cartItem.getFoodSize(),
                cartItem.getFoodAddon())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SingleObserver<CartItem>() {
                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onSuccess(CartItem cartItemFromDB) {
                        if (cartItemFromDB.equals(cartItem)) {

                            cartItemFromDB.setFoodExtraPrice(cartItem.getFoodExtraPrice());
                            cartItemFromDB.setFoodAddon(cartItem.getFoodAddon());
                            cartItemFromDB.setFoodSize(cartItem.getFoodSize());
                            cartItem.setFoodQuantity(cartItemFromDB.getFoodQuantity() + cartItem.getFoodQuantity());

                            cartDataSource.updateCartItems(cartItemFromDB)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new SingleObserver<Integer>() {
                                        @Override
                                        public void onSubscribe(Disposable d) {

                                        }

                                        @Override
                                        public void onSuccess(Integer integer) {
                                            Toast.makeText(getContext(), "update cart success", Toast.LENGTH_SHORT).show();
                                            EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                        }

                                        @Override
                                        public void onError(Throwable e) {
                                            Toast.makeText(getContext(), "[UPDATE CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            //item not available in cart before , insert new
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                                Toast.makeText(getContext(), "add to cart success", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }, throwable -> {
                                                Toast.makeText(getContext(), "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                            }

                                    ));
                        }


                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getMessage().contains("empty")) {
                            compositeDisposable.add(cartDataSource.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(() -> {
                                                Toast.makeText(getContext(), "add to cart success", Toast.LENGTH_SHORT).show();
                                                EventBus.getDefault().postSticky(new CounterCartEvent(true));
                                            }, throwable -> {
                                                Toast.makeText(getContext(), "[CART ERROR]" + throwable.getMessage(), Toast.LENGTH_SHORT).show();
                                                Log.d("aaa","cart error: "+throwable.getMessage()+"");
                                            }

                                    ));

                        }
                        Toast.makeText(getContext(), "[GET CART]" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("aaa","get cart : "+e.getMessage()+"");

                    }
                });


    }

    private void displayAddonList() {
        if (Common.selectedFood.getAddon().size() > 0) {
            chipGroupAddon.clearCheck();
            chipGroupAddon.removeAllViews();

            edtSearch.addTextChangedListener(this);


            //add all view
            for (AddonModel addonModel : Common.selectedFood.getAddon()) {

                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_add_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        if (Common.selectedFood.getUserSelectAddon() == null)
                            Common.selectedFood.setUserSelectAddon(new ArrayList<>());
                        Common.selectedFood.getUserSelectAddon().add(addonModel);
                    }

                });
                chipGroupAddon.addView(chip);

            }
        }
    }


    private void showDialogRating() {
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(getContext());
        builder.setTitle("Rating food");
        builder.setCancelable(false);

        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.layout_rating, null);

        RatingBar ratingBar = itemView.findViewById(R.id.ratingBar);
        EditText edtComment = itemView.findViewById(R.id.edtComment);

        builder.setView(itemView);

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.setPositiveButton("OK", (dialog, which) -> {
            CommentModel commentModel = new CommentModel();
            commentModel.setName(Common.curentUser.getName());
            commentModel.setComment(edtComment.getText().toString());
            commentModel.setUid(Common.curentUser.getUid());
            commentModel.setRatingValue((double) ratingBar.getRating());
            Map<String, Object> serverTimeStamp = new HashMap<>();
            serverTimeStamp.put("timeStamp", ServerValue.TIMESTAMP);
            commentModel.setCommentTimeStamp(serverTimeStamp);

            foodDetailViewModel.setCommentModel(commentModel);
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodDetailViewModel =
                ViewModelProviders.of(this).get(FoodDetailViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_detail, container, false);
        unbinder = ButterKnife.bind(this, root);

        foodDetailViewModel.getMutableLiveDataFood().observe(this, foodModel -> {
            displayInfo(foodModel); //live data
        });
        foodDetailViewModel.getMutableLiveDataComment().observe(this, commentModel -> {
            submitRatingToFirebase(commentModel);
        });


        init();

        return root;
    }

    private void submitRatingToFirebase(CommentModel commentModel) {

        waitingDialog.show();

        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .push()
                .setValue(commentModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        addRatingToFood(commentModel.getRatingValue());
                    }
                    waitingDialog.dismiss();
                })
                .addOnSuccessListener(aVoid -> {

                }).addOnFailureListener(e -> Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void addRatingToFood(Double ratingValue) {

        FirebaseDatabase.getInstance()
                .getReference(Common.CATEGORY_REF)
                .child(Common.categorySelected.getMenuId())//select category
                .child("foods")//select array list food of this category
                .child(Common.selectedFood.getKey())//because food item is array list so key is index of arraylist
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot != null) {
                            FoodModel foodModel = snapshot.getValue(FoodModel.class);
                            foodModel.setKey(Common.selectedFood.getKey());

                            //apply rating
                            if (foodModel.getRatingCount() == null)
                                foodModel.setRatingCount(0l); // l > long
                            if (foodModel.getRatingValue() == null)
                                foodModel.setRatingValue(0d);//d > double

                            Double sumRating = foodModel.getRatingValue() + ratingValue;
                            Long ratingCount = foodModel.getRatingCount() + 1;


                            Map<String, Object> updateData = new HashMap<>();
                            updateData.put("ratingValue", sumRating); //la sum
                            updateData.put("ratingCount", ratingCount);

                            //update data
                            foodModel.setRatingValue(sumRating);
                            foodModel.setRatingCount(ratingCount);

                            snapshot.getRef()
                                    .updateChildren(updateData)
                                    .addOnCompleteListener(task -> {
                                        Toast.makeText(getContext(), "Thank you!!", Toast.LENGTH_SHORT).show();
                                        Common.selectedFood = foodModel;
                                        foodDetailViewModel.setFoodModel(foodModel); //call refresh


                                    });


                        } else {
                            waitingDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        waitingDialog.dismiss();
                        Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void displayInfo(FoodModel foodModel) {
        Glide.with(getContext()).load(foodModel.getImage()).into(imgFood);
        txtFoodName.setText(foodModel.getName());
        txtFoodPrice.setText(foodModel.getPrice() + "");
        txtFoodDescription.setText(foodModel.getDescription());

        if (foodModel.getRatingValue() != null) {
            Double result = foodModel.getRatingValue() / foodModel.getRatingCount();
            ratingBar.setRating(result.floatValue());
        }

        ((AppCompatActivity) getActivity())
                .getSupportActionBar().setTitle(foodModel.getName());

        for (SizeModel sizeModel : Common.selectedFood.getSize()) {
            RadioButton radioButton = new RadioButton(getContext());
            radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {

                if (isChecked)
                    Common.selectedFood.setUserSelectSize(sizeModel);
                calculateTotalPrice();//update price


            });

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1.0f);
            radioButton.setText(sizeModel.getName()); // name la loai size //
            radioButton.setLayoutParams(params);
            radioButton.setTag(sizeModel.getPrice());

            radGroupSize.addView(radioButton);
        }

        if (radGroupSize.getChildCount() > 0) {
            RadioButton radioButton = (RadioButton) radGroupSize.getChildAt(0);
            radioButton.setChecked(true);
        }
        calculateTotalPrice();

        btnNumberButton.setOnValueChangeListener((view, oldValue, newValue) -> calculateTotalPrice());

    }

    private void calculateTotalPrice() {
        double totalPrice = Double.parseDouble(Common.selectedFood.getPrice() + ""), displayPrice = 0.0;

        //addon
        if (Common.selectedFood.getUserSelectAddon() != null && Common.selectedFood.getUserSelectAddon().size() > 0)
            for (AddonModel addonModel : Common.selectedFood.getUserSelectAddon())
                totalPrice += Double.parseDouble(addonModel.getPrice().toString());

        // size

        if(Common.selectedFood.getUserSelectSize()!=null)
        totalPrice += Double.parseDouble(Common.selectedFood.getUserSelectSize().getPrice()//price la loai gi
                .toString());


        displayPrice = totalPrice * (Integer.parseInt(btnNumberButton.getNumber()));
        displayPrice = Math.round(displayPrice * 100.0 / 100.0);
        txtFoodPrice.setText(new StringBuilder().append(Common.formatPrice(displayPrice)));
    }


    private void init() {

        cartDataSource=new LocalCartDataSource(CartDatabase.getInstance(getContext()).cartDAO());

        waitingDialog = new SpotsDialog.Builder().setCancelable(false).setContext(getContext()).build();

        addOnBottomSheetDialog = new BottomSheetDialog(getContext(), R.style.DialogStyle);
        View layout_add_on_display = getLayoutInflater().inflate(R.layout.layout_add_on_display, null);
        chipGroupAddon = layout_add_on_display.findViewById(R.id.chip_group_addon);
        edtSearch = layout_add_on_display.findViewById(R.id.edtSearch);
        addOnBottomSheetDialog.setContentView(layout_add_on_display);

        addOnBottomSheetDialog.setOnDismissListener(dialog -> {
            displayUserSelectAddon();
            calculateTotalPrice();
        });
    }

    private void displayUserSelectAddon() {
        if (Common.selectedFood.getUserSelectAddon() != null && Common.selectedFood.getUserSelectAddon().size() > 0) {
            chip_group_user_select_addon.removeAllViews();
            for (AddonModel addonModel : Common.selectedFood.getUserSelectAddon()) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_chip_with_delete_icon, null);
                chip.setText(new StringBuilder((addonModel.getName())).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setCheckable(false);
                chip.setOnCloseIconClickListener(v -> {
                    chip_group_user_select_addon.removeView(v);
                    Common.selectedFood.getUserSelectAddon().remove(addonModel);
                    calculateTotalPrice();
                });
                chip_group_user_select_addon.addView(chip);
            }
        } else
            chip_group_user_select_addon.removeAllViews();


    }


    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        chipGroupAddon.clearCheck();
        chipGroupAddon.removeAllViews();

        for (AddonModel addonModel : Common.selectedFood.getAddon()) {
            if (addonModel.getName().toLowerCase().contains(s.toString().toLowerCase())) {
                Chip chip = (Chip) getLayoutInflater().inflate(R.layout.layout_add_item, null);
                chip.setText(new StringBuilder(addonModel.getName()).append("(+$")
                        .append(addonModel.getPrice()).append(")"));
                chip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (isChecked) {
                            if (Common.selectedFood.getUserSelectAddon() == null)
                                Common.selectedFood.setUserSelectAddon(new ArrayList<>());
                            Common.selectedFood.getUserSelectAddon().add(addonModel);
                        }

                    }
                });
                chipGroupAddon.addView(chip);
            }
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}