package etn.app.danghoc.eat_it_.Model;

public class BestDealModel {
    private String menu_id,food_id,ne,hinh;

    public BestDealModel() {
    }

    public BestDealModel(String menuId, String foodId, String name1, String image) {
        this.menu_id = menuId;
        this.food_id = foodId;
        this.ne = name1;
        this.hinh = image;
    }

    public String getMenu_id() {
        return menu_id;
    }

    public void setMenu_id(String menuId) {
        this.menu_id = menuId;
    }

    public String getFood_id() {
        return food_id;
    }

    public void setFood_id(String foodId) {
        this.food_id = foodId;
    }

    public String getName() {
        return ne;
    }

    public void setName(String name) {
        this.ne = name;
    }

    public String getImage() {
        return hinh ;
    }

    public void setImage(String image) {
        this.hinh= image;
    }
}
