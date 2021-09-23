package etn.app.danghoc.eat_it_.CallBack;

import etn.app.danghoc.eat_it_.Model.Order;

public interface ILoadTimeFromFirebaseListener {
    void onLoadTimeSuccess(Order order,long estimateTimeInMs);
    void onLoadTimeFailed(String message);

}
