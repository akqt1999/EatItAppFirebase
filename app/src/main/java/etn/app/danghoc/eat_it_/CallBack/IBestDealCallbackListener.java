package etn.app.danghoc.eat_it_.CallBack;

import java.util.List;

import etn.app.danghoc.eat_it_.Model.BestDealModel;

public interface IBestDealCallbackListener {
    void onBestDealLoadSuccess(List<BestDealModel> bestDealModels);

    void onBestDealLoadFailed(String message);
}
