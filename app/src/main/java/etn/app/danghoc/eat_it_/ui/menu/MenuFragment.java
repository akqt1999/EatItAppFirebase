package etn.app.danghoc.eat_it_.ui.menu;

import android.app.AlertDialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import etn.app.danghoc.eat_it_.Adapter.MyCategoryAdapter;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Common.SpacesItemDecoration;
import etn.app.danghoc.eat_it_.EventBus.MenuItemBack;
import etn.app.danghoc.eat_it_.Model.CategoryModel;
import etn.app.danghoc.eat_it_.R;

public class MenuFragment extends Fragment {

    private MenuViewModel menuViewModel;

    Unbinder unbinder;
    @BindView(R.id.recycler_menu)
    RecyclerView recyclerViewMenu;

    AlertDialog dialog;
    LayoutAnimationController layoutAnimationController;
    MyCategoryAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        menuViewModel =
                ViewModelProviders.of(this).get(MenuViewModel.class);
        View root = inflater.inflate(R.layout.fragment_menu, container, false);

        unbinder = ButterKnife.bind(this, root);

        menuViewModel.getMessageError().observe(this, s -> {
            //
            Toast.makeText(getContext(), s+"", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        menuViewModel.getCategoryListMultable().observe(this, new Observer<List<CategoryModel>>() {
            @Override
            public void onChanged(List<CategoryModel> categoryModelList) {
                dialog.dismiss();
                adapter=new MyCategoryAdapter(getContext(),categoryModelList);
                recyclerViewMenu.setAdapter(adapter);
                recyclerViewMenu.setLayoutAnimation(layoutAnimationController);
            }
        });

        init();
        return root;
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
                 menuViewModel.loadCategories();
             }
         });


    }

    private void startSearch(String s) {
        List<CategoryModel>resultList=new ArrayList<>();
        for(int i=0;i<adapter.getListCategory().size();i++)
        {
            CategoryModel categoryMode=adapter.getListCategory().get(i);
            if(categoryMode.getName().toLowerCase().contains(s))
                resultList.add(categoryMode);
        }

        menuViewModel.getCategoryListMultable().setValue(resultList);


    }

    private void init() {

        setHasOptionsMenu(true);

        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();
        dialog.show();
        layoutAnimationController = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_item_from_left);
        GridLayoutManager layoutManager=new GridLayoutManager(getContext(),2);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(adapter!=null)
                {
                    switch (adapter.getItemViewType(position))
                    {
                        case Common.DEFAULT_COLUMN_COUNT:return 1;
                        case Common.FULL_WIDTH_COLUMN:return 2;
                        default: return -1;
                    }
                }

                return -1;
            }
        });

        recyclerViewMenu.setLayoutManager(layoutManager);
        recyclerViewMenu.addItemDecoration(new SpacesItemDecoration(8));
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().postSticky(new MenuItemBack());
        super.onDestroy();
    }



}