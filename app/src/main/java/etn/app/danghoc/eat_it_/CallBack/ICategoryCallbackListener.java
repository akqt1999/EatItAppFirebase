package etn.app.danghoc.eat_it_.CallBack;

import java.util.List;

import etn.app.danghoc.eat_it_.Model.CategoryModel;

public interface ICategoryCallbackListener {
    void onCategoryLoadSuccess(List<CategoryModel>categoryModelList);
    void onCategoryLoadFail(String message);
}
