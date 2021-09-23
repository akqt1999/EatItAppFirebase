package etn.app.danghoc.eat_it_.ui.restaurant;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LayoutAnimationController;
import android.widget.Toast;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eat_it_.Adapter.MyRestaurantAdapter;
import etn.app.danghoc.eat_it_.Model.RestaurantModel;
import etn.app.danghoc.eat_it_.R;

public class RestaurantFragment extends Fragment {

    private RestaurantViewModel mViewModel;

    Unbinder unbinder;

    @BindView(R.id.recycle_restaurant)
    RecyclerView recycle_restaurant;
    AlertDialog dialog;

    MyRestaurantAdapter adapter;


    public static RestaurantFragment newInstance() {
        return new RestaurantFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mViewModel= ViewModelProviders.of(this).get(RestaurantViewModel.class);
        View root= inflater.inflate(R.layout.restaurant_fragment, container, false);
        unbinder= ButterKnife.bind(this,root);
        init();

        mViewModel.getMessageError().observe(this, s -> {
            dialog.dismiss();
            Toast.makeText(getContext(), s, Toast.LENGTH_SHORT).show();
        });

        mViewModel.getRestaurantListMutable().observe(this, new Observer<List<RestaurantModel>>() {
            @Override
            public void onChanged(List<RestaurantModel> restaurantModelList) {
                dialog.dismiss();
                adapter=new MyRestaurantAdapter(getContext(),restaurantModelList);
                recycle_restaurant.setAdapter(adapter);
            }
        });

        return root;

    }

    private void init() {
         setHasOptionsMenu(true);
         dialog=new AlertDialog.Builder(getContext()).setCancelable(false).setMessage("waiting...").create();

         dialog.show();

        LinearLayoutManager layoutManager=new LinearLayoutManager(getContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        recycle_restaurant.setLayoutManager(layoutManager);

    }




}