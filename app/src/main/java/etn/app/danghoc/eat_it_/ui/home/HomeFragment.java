package etn.app.danghoc.eat_it_.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.asksira.loopingviewpager.LoopingViewPager;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eat_it_.Adapter.MyBestDealAdapter;
import etn.app.danghoc.eat_it_.Adapter.MyPopularCategoriesAdapter;
import etn.app.danghoc.eat_it_.R;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    Unbinder unbinder;

    @BindView(R.id.rey_popular)
    RecyclerView reyViewPopular;
    @BindView(R.id.viewpager)
    LoopingViewPager viewPager;

    LayoutAnimationController layoutAnimationController;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        homeViewModel= ViewModelProviders.of(this).get(HomeViewModel.class);

        View root = inflater.inflate(R.layout.fragment_home, container, false);
        unbinder = ButterKnife.bind(this, root);

        String key=getArguments().getString("restaurant");



        init();
        homeViewModel.getPopularList(key).observe(this,popularCategoryModels -> {

            //create adapter
            MyPopularCategoriesAdapter adapter=new MyPopularCategoriesAdapter(getContext(),popularCategoryModels);
            reyViewPopular.setAdapter(adapter);
            Log.d("adad","id menu : "+popularCategoryModels.get(1).getMenu_id());
            Log.d("adad","name : "+popularCategoryModels.get(1).getName());
            Log.d("adad","food id: "+popularCategoryModels.get(1).getFood_id());
            Log.d("adad","image : "+popularCategoryModels.get(1).getImage());
            reyViewPopular.setLayoutAnimation(layoutAnimationController);
        });

        homeViewModel.getBestDealList(key).observe(this,bestDealModels -> {
            MyBestDealAdapter adapter=new MyBestDealAdapter(getContext(),bestDealModels,true);
            viewPager.setAdapter(adapter);
        });

        return root;
    }

    private void init() {

        layoutAnimationController= AnimationUtils.loadLayoutAnimation(getContext(),R.anim.layout_item_from_left);

        reyViewPopular.setHasFixedSize(true);
        reyViewPopular.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));
    }

    @Override
    public void onResume() {
        super.onResume();
        viewPager.resumeAutoScroll();
    }

    @Override
    public void onPause() {
        viewPager.pauseAutoScroll();
        super.onPause();
    }
}