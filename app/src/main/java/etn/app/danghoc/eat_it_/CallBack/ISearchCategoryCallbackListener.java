package etn.app.danghoc.eat_it_.CallBack;

import etn.app.danghoc.eat_it_.Database.CartItem;
import etn.app.danghoc.eat_it_.Model.CategoryModel;
import etn.app.danghoc.eat_it_.Model.FoodModel;

public interface ISearchCategoryCallbackListener {
    void onSearchCategoryFound(CategoryModel foodModel, CartItem cartItem);
    void onSearchCategoryNotFound(String message);
}
