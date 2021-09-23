package etn.app.danghoc.eat_it_.ui.home;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import etn.app.danghoc.eat_it_.CallBack.IBestDealCallbackListener;
import etn.app.danghoc.eat_it_.CallBack.IPopularCallbackListener;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Model.BestDealModel;
import etn.app.danghoc.eat_it_.Model.PopularCategoryModel;
import etn.app.danghoc.eat_it_.R;

public class HomeViewModel extends ViewModel implements IPopularCallbackListener, IBestDealCallbackListener {
    private MutableLiveData<List<PopularCategoryModel>> popularList;
    private MutableLiveData<List<BestDealModel>> bestDealList;
    private MutableLiveData<String> messageError;
    private IPopularCallbackListener popularCallbackListener;
    private IBestDealCallbackListener bestDealCallbackListener;


    public HomeViewModel() {
        popularCallbackListener = this;
        bestDealCallbackListener = this;
    }

    public MutableLiveData<List<BestDealModel>> getBestDealList(String key) {
        if (bestDealList == null) {
            bestDealList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadBestDealList(key);
        }

        return bestDealList;
    }

    // toi chi la mot tro dua cho nguoi khac , ok , sao cung duoc, chang phai suy gi , tat ca la phu du chang co gi phai suy nghi ,  quan trong la ban than phai nhu the nao
    private void loadBestDealList(String key) {
        List<BestDealModel> tempList = new ArrayList<>();
        DatabaseReference bestDealRef = FirebaseDatabase.getInstance().getReference(Common.RESTAURANT_REF)
                .child(key)
                .child(Common.BEST_DEALS_REF);
        bestDealRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    BestDealModel model = itemSnapshot.getValue(BestDealModel.class);
                    tempList.add(model);
                    //Log.d("acsc" , "food id deal : "+ model.getFoodId());
                    Log.d("acsc", " id deal : " + model.getMenu_id());

                }

                bestDealCallbackListener.onBestDealLoadSuccess(tempList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                bestDealCallbackListener.onBestDealLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<List<PopularCategoryModel>> getPopularList(String key) {
        if (popularList == null) {
            popularList = new MutableLiveData<>();
            messageError = new MutableLiveData<>();
            loadPopularList(key);
        }
        return popularList;
    }

    private void loadPopularList(String key) {
        List<PopularCategoryModel> temList = new ArrayList<>();
        DatabaseReference popularRef = FirebaseDatabase.getInstance().
                getReference(Common.RESTAURANT_REF)
                .child(key)
                .child(Common.POPULAR_CATEGORY_REF);

        popularRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    PopularCategoryModel model = itemSnapshot.getValue(PopularCategoryModel.class);
                    temList.add(model);
                    Log.d("acsc", "food id popular : " + model.getFood_id());
                }
                popularCallbackListener.onPopularLoadSuccess(temList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                popularCallbackListener.onPopularLoadFailed(error.getMessage());
            }
        });
    }

    public MutableLiveData<String> getMessageError() {
        return messageError;
    }

    @Override
    public void onPopularLoadSuccess(List<PopularCategoryModel> popularCategoryModels) {
        popularList.setValue(popularCategoryModels);
    }

    @Override
    public void onPopularLoadFailed(String message) {
        messageError.setValue(message);
    }

    @Override
    public void onBestDealLoadSuccess(List<BestDealModel> bestDealModels) {
        bestDealList.setValue(bestDealModels);
    }

    @Override
    public void onBestDealLoadFailed(String message) {
        messageError.setValue(message);
    }
}