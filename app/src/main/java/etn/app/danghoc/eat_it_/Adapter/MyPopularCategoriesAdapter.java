package etn.app.danghoc.eat_it_.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import de.hdodenhof.circleimageview.CircleImageView;
import etn.app.danghoc.eat_it_.CallBack.IRecyclerClickListener;
import etn.app.danghoc.eat_it_.EventBus.PopularCategoryClick;
import etn.app.danghoc.eat_it_.Model.BestDealModel;
import etn.app.danghoc.eat_it_.Model.PopularCategoryModel;
import etn.app.danghoc.eat_it_.R;

public class MyPopularCategoriesAdapter extends RecyclerView.Adapter<MyPopularCategoriesAdapter.MyViewHolder> {

    Context context;
    List<PopularCategoryModel> popularCategoryModelList;

    public MyPopularCategoriesAdapter(Context context, List<PopularCategoryModel> popularCategoryModelList) {
        this.context = context;
        this.popularCategoryModelList = popularCategoryModelList;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MyViewHolder(LayoutInflater.from(context)
                .inflate(R.layout.layout_popular_categories_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Glide.with(context).load(popularCategoryModelList.get(position).getImage())
                .into(holder.categoryImage);

        holder.txtCategoryName.setText(popularCategoryModelList.get(position).getName());

        holder.setListener((view, pos) -> {
            //
            EventBus.getDefault().postSticky(new PopularCategoryClick(popularCategoryModelList.get(pos)));
        });
    }

    @Override
    public int getItemCount() {
        return popularCategoryModelList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Unbinder unbinder;

        @BindView(R.id.categoryName)
        TextView txtCategoryName;
        @BindView(R.id.categoryImage)
        CircleImageView categoryImage;

        IRecyclerClickListener listener;

        public void setListener(IRecyclerClickListener listener) {
            this.listener = listener;
        }

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            listener.onItemClickListener(v,getAdapterPosition());
            Toast.makeText(context, popularCategoryModelList.get(getAdapterPosition()).getName(), Toast.LENGTH_SHORT).show();
        }
    }
}
