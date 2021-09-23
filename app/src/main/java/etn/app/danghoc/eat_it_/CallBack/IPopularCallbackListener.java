package etn.app.danghoc.eat_it_.CallBack;

import java.util.List;

import etn.app.danghoc.eat_it_.Model.PopularCategoryModel;

public interface IPopularCallbackListener {

    void onPopularLoadSuccess(List<PopularCategoryModel>popularCategoryModels);
    void onPopularLoadFailed(String message);
}
