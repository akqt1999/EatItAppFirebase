package etn.app.danghoc.eat_it_.ui.foodList;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eat_it_.Adapter.MyFoodListAdapter;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.EventBus.MenuItemBack;
import etn.app.danghoc.eat_it_.Model.CategoryModel;
import etn.app.danghoc.eat_it_.Model.FoodModel;
import etn.app.danghoc.eat_it_.R;

public class FoodListFragment extends Fragment {

    private FoodListViewModel foodListViewModel;

    Unbinder unbinder;
    LayoutAnimationController layoutAnimationController;
    MyFoodListAdapter adapter;

    @BindView(R.id.recycler_food_list)
    RecyclerView recyclerFoodList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        foodListViewModel =
                ViewModelProviders.of(this).get(FoodListViewModel.class);
        View root = inflater.inflate(R.layout.fragment_food_list, container, false);
        unbinder = ButterKnife.bind(this, root);
        init();
        foodListViewModel.getMutableLiveDataFoodList().observe(this, foodModels -> {
            //
            adapter = new MyFoodListAdapter(getContext(), foodModels);
            recyclerFoodList.setAdapter(adapter);
            recyclerFoodList.setLayoutAnimation(layoutAnimationController);
        });
        return root;
    }

    private void init() {

        setHasOptionsMenu(true);

        ((AppCompatActivity)getActivity())
                .getSupportActionBar()
                .setTitle(Common.categorySelected.getName());

        recyclerFoodList.setHasFixedSize(true);
        recyclerFoodList.setLayoutManager(new LinearLayoutManager(getContext()));
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);


    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.search_menu,menu);

        MenuItem menuItem=menu.findItem(R.id.action_search);

        SearchManager searchManager= (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView= (SearchView) menuItem.getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

        //event
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                startSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //clear text when click to clear button on search view
        ImageView closeButton=searchView.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText ed=searchView.findViewById(R.id.search_src_text);
                //clear text
                ed.setText("");
                //clear query
                searchView.setQuery("",false);
                //collapse the action view
                searchView.onActionViewCollapsed();
                //collapse the search widget
                menuItem.collapseActionView();
                //restore result to original
                foodListViewModel.getMutableLiveDataFoodList();
            }
        });


    }

    private void startSearch(String s) {
        List<FoodModel>resultList=new ArrayList<>();
        for(int i=0;i<Common.categorySelected.getFoods().size();i++)
        {
            FoodModel foodMode=Common.categorySelected.getFoods().get(i);
            if(foodMode.getName().toLowerCase().contains(s))
                resultList.add(foodMode);
        }

        foodListViewModel.getMutableLiveDataFoodList().setValue(resultList);


    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }
}