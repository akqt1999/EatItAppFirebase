package etn.app.danghoc.eat_it_.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.common.api.internal.LifecycleCallback;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Database.CartItem;
import etn.app.danghoc.eat_it_.EventBus.UpdateItemInCart;
import etn.app.danghoc.eat_it_.Model.AddonModel;
import etn.app.danghoc.eat_it_.Model.SizeModel;
import etn.app.danghoc.eat_it_.R;

public class MyCartAdapter extends RecyclerView.Adapter<MyCartAdapter.ViewHolder> {

    Context context;
    List<CartItem> cartItems;
    Gson gson;

    public MyCartAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
        gson=new Gson();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.
                from(context).inflate(R.layout.layout_cart_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Glide.with(context).load(cartItems.get(position).getFoodImage())
                .into(holder.imgCart);
        holder.txtFoodName.setText(cartItems.get(position).getFoodName());
        holder.txtFoodPrice.setText(new StringBuilder("")
        .append(cartItems.get(position).getFoodPrice()+cartItems.get(position).getFoodExtraPrice()));
        holder.numberBtn.setNumber(cartItems.get(position).getFoodQuantity()+"");


        if(cartItems.get(position).getFoodSize()!=null)
        {
            if(cartItems.get(position).getFoodSize().equals("Default"))
            {
                holder.txt_food_size.setText(new StringBuilder("Size: ")
                .append("Default"));
            }else
            {
                SizeModel sizeModel=gson.fromJson(cartItems.get(position).getFoodSize(),new TypeToken<SizeModel>(){}.getType());
                holder.txt_food_size.setText(new StringBuilder("Size: ").append(sizeModel.getName()));


            }

        }

        if(cartItems.get(position).getFoodAddon()!=null)
        {
            if(cartItems.get(position).getFoodAddon().equals("Default"))
                holder.txt_food_add_on.setText(new StringBuilder("Addon: ").append("Default"));
            else
            {
                List<AddonModel>addonModels=gson.fromJson(cartItems.get(position).getFoodAddon(),new TypeToken<List<AddonModel>>(){}.getType());
                holder.txt_food_add_on.setText(new StringBuilder("Addon: ").append(Common.getListAddon(addonModels)));
            }
        }

        holder.numberBtn.setNumber(cartItems.get(position).getFoodQuantity()+"");

        //event
        holder.numberBtn.setOnValueChangeListener((view, oldValue, newValue) -> {

            cartItems.get(position).setFoodQuantity(newValue);
            EventBus.getDefault().postSticky(new UpdateItemInCart(cartItems.get(position)) );
        });

    }



    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public CartItem getItemAtPosition(int pos) {
        return cartItems.get(pos);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;
        @BindView(R.id.imgCart)
        ImageView imgCart;
        @BindView(R.id.txtFoodPrice)
        TextView txtFoodPrice;
        @BindView(R.id.txt_food_add_on)
        TextView txt_food_add_on;
        @BindView(R.id.txt_food_size)
        TextView txt_food_size;
        @BindView(R.id.txtFoodName)
        TextView txtFoodName;
        @BindView(R.id.numberBtn)
        ElegantNumberButton numberBtn;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
