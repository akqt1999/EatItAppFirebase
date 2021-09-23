package etn.app.danghoc.eat_it_.EventBus;

import etn.app.danghoc.eat_it_.Model.BestDealModel;
import etn.app.danghoc.eat_it_.Model.PopularCategoryModel;

public class PopularCategoryClick {
    PopularCategoryModel popularCategoryModel;

    public PopularCategoryClick(PopularCategoryModel popularCategoryModel) {
        this.popularCategoryModel = popularCategoryModel;
    }

    public PopularCategoryModel getPopularCategoryModel() {
        return popularCategoryModel;
    }

    public void setPopularCategoryModel(PopularCategoryModel popularCategoryModel) {
        this.popularCategoryModel = popularCategoryModel;
    }
}
