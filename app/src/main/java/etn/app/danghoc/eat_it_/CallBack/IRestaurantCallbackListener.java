package etn.app.danghoc.eat_it_.CallBack;

import java.util.List;

import etn.app.danghoc.eat_it_.Model.RestaurantModel;

public interface IRestaurantCallbackListener {
    void onRestaurantLoadSuccess(List<RestaurantModel> restaurantModelList);

    void onRestaurantLoadFailed(String message);
}
