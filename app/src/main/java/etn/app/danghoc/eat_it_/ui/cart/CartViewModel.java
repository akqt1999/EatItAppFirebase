package etn.app.danghoc.eat_it_.ui.cart;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Database.CartDataSource;
import etn.app.danghoc.eat_it_.Database.CartDatabase;
import etn.app.danghoc.eat_it_.Database.CartItem;
import etn.app.danghoc.eat_it_.Database.LocalCartDataSource;
import etn.app.danghoc.eat_it_.Model.CommentModel;
import etn.app.danghoc.eat_it_.Model.FoodModel;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class CartViewModel extends ViewModel {

    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private CartDataSource cartDataSource;
    private MutableLiveData<List<CartItem>> mutableLiveDataCartItem;

    public CartViewModel() {
        compositeDisposable = new CompositeDisposable();
    }

    public void initCartDataSource(Context context) {
        cartDataSource = new LocalCartDataSource(CartDatabase.getInstance(context).cartDAO());
    }

    public void onStop() {
        compositeDisposable.clear();
    }

    public MutableLiveData<List<CartItem>> getMutableLiveDataCartItem() {
        if (mutableLiveDataCartItem == null)
            mutableLiveDataCartItem = new MutableLiveData<>();
        getAllCartItems();
        return mutableLiveDataCartItem;
    }



    // gio hang la luu tren may
    private void getAllCartItems() {
        compositeDisposable.add(cartDataSource.getAllCart(Common.curentUser.getUid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(cartItems -> {
                    mutableLiveDataCartItem.setValue(cartItems);
                }, throwable -> mutableLiveDataCartItem.setValue(null))
        );
    }
}