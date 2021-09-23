package etn.app.danghoc.eat_it_.Adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eat_it_.CallBack.IRecyclerClickListener;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Database.CartItem;
import etn.app.danghoc.eat_it_.Model.Order;
import etn.app.danghoc.eat_it_.R;

public class MyOrderAdapter extends RecyclerView.Adapter<MyOrderAdapter.MyViewHolder> {

    private Context context;
    private List<Order> orderList;
    private Calendar calendar;
    private SimpleDateFormat simpleDateFormat;

    public MyOrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
        calendar = Calendar.getInstance();
        simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
    }


    public Order getItemAtPosition(int pos)
    {
       return orderList.get(pos);
    }

    public void setITemAtPosition(int pos,Order item)
    {
        orderList.set(pos,item);
    }
            //


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context).from(context).inflate(R.layout.layout_order_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(orderList.get(position).getCartItemList().get(0).getFoodImage())
                .into(holder.imgOrder);//load default image in cart

        calendar.setTimeInMillis(orderList.get(position).getCreateDate());
        Date date = new Date(orderList.get(position).getCreateDate());
        holder.txtOrderDate.setText(new StringBuilder(Common.getDateOfWeek(Calendar.DAY_OF_WEEK))
                .append(" ")
                .append(simpleDateFormat.format(date)));
        holder.txtOrderNumber.setText(new StringBuilder("Order No: ").append(orderList.get(position).getOrderNumber()));
        holder.txtOrderComment.setText(new StringBuilder("Comment: ").append(orderList.get(position).getComment()));
        holder.txtOrderStatus.setText(new StringBuilder("Status: ").append(Common.convertStatusToText(orderList.get(position).getOrderStatus())));

        holder.setiRecyclerClickListener((view, pos) -> {
                showDialog(orderList.get(pos).getCartItemList());
        });

    }

    private void showDialog(List<CartItem> cartItemList) {
        View layout_dialog=LayoutInflater.from(context).inflate(R.layout.layout_dialog_order_detail,null);
        AlertDialog.Builder builder=new AlertDialog.Builder(context);
        builder.setView(layout_dialog);
        Button btn_ok=layout_dialog.findViewById(R.id.btn_ok);
        RecyclerView recycler_order_detail=layout_dialog.findViewById(R.id.recycler_order_detail);
        recycler_order_detail.setHasFixedSize(true);
        LinearLayoutManager layoutManager=new LinearLayoutManager(context);
        recycler_order_detail.setLayoutManager(layoutManager);

        MyOrderDetailAdapter myOrderDetailAdapter=new MyOrderDetailAdapter(context,cartItemList);
        recycler_order_detail.setAdapter(myOrderDetailAdapter);

        //show dialog
        AlertDialog  dialog=builder.create();
        dialog.show();


        //custom dialog
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);

        btn_ok.setOnClickListener(v -> dialog.dismiss());





    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.txtOrderComment)
        TextView txtOrderComment;
        @BindView(R.id.txtOrderDate)
        TextView txtOrderDate;
        @BindView(R.id.txtOrderNumber)
        TextView txtOrderNumber;
        @BindView(R.id.txtOrderStatus)
        TextView txtOrderStatus;
        @BindView(R.id.imgOrder)
        ImageView imgOrder;

        Unbinder unbinder;

        IRecyclerClickListener iRecyclerClickListener;

        public void setiRecyclerClickListener(IRecyclerClickListener iRecyclerClickListener) {
            this.iRecyclerClickListener = iRecyclerClickListener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            iRecyclerClickListener.onItemClickListener(v,getAdapterPosition());
        }
    }
}
