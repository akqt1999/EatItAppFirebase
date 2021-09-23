package etn.app.danghoc.eat_it_.EventBus;

public class HideFABCart {
    private boolean hidden;

    public HideFABCart(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }
}
