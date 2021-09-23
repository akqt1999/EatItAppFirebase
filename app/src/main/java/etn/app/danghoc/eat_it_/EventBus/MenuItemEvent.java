package etn.app.danghoc.eat_it_.EventBus;

import etn.app.danghoc.eat_it_.Model.RestaurantModel;

public class MenuItemEvent {
    private boolean success;
    private RestaurantModel restaurantModel;

    public MenuItemEvent(boolean success, RestaurantModel restaurantModel) {
        this.success = success;
        this.restaurantModel = restaurantModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public RestaurantModel getRestaurantModel() {
        return restaurantModel;
    }

    public void setRestaurantModel(RestaurantModel restaurantModel) {
        this.restaurantModel = restaurantModel;
    }


}
/*
    1-khi nhấn vào item thì nó sẽ gọi event
    2- event sẽ lấy uid đưa vào bundle  từ cái  item đang gọi sẽ sẽ chuyển sang home fragment
    3- fragment sẽ gọi dữ liệu từ bundle child cái uid để lấy dữ liệu , load category
 */