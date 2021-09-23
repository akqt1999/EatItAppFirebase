package etn.app.danghoc.eat_it_.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.asksira.loopingviewpager.LoopingPagerAdapter;
import com.asksira.loopingviewpager.LoopingViewPager;
import com.bumptech.glide.Glide;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eat_it_.EventBus.BestDealItemClick;
import etn.app.danghoc.eat_it_.Model.BestDealModel;
import etn.app.danghoc.eat_it_.R;

public class MyBestDealAdapter extends LoopingPagerAdapter<BestDealModel> {

    Context context;
    List<BestDealModel> itemList;

    @BindView(R.id.imgBestDeal)
    ImageView imgBestDeal;
    @BindView(R.id.txtBestCategories)
    TextView txtBestDeal;

    Unbinder unbinder;

    public MyBestDealAdapter(Context context, List<BestDealModel> itemList, boolean isInfinite) {
        super(context, itemList, isInfinite);
        this.context=context;
        this.itemList=itemList;
    }

    @Override
    protected void bindView(View view, int listPosition, int i1) {

        unbinder = ButterKnife.bind(this, view);

        //set data
        Glide.with(view).load(itemList.get(listPosition).getImage()).into(imgBestDeal);
        txtBestDeal.setText(itemList.get(listPosition).getName());

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getDefault().postSticky(new BestDealItemClick(itemList.get(listPosition)));
            }
        });

    }

    @Override
    protected View inflateView(int i, ViewGroup viewGroup, int i1) {
        return LayoutInflater.from(context).inflate(R.layout.layout_best_deal_item, viewGroup, false);
    }
}
