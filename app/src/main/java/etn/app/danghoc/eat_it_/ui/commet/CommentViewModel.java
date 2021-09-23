package etn.app.danghoc.eat_it_.ui.commet;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import etn.app.danghoc.eat_it_.Model.CommentModel;
import etn.app.danghoc.eat_it_.Model.FoodModel;

public class CommentViewModel extends ViewModel {

    private MutableLiveData<List<CommentModel>> mutableLiveDataCommentList;

    public CommentViewModel() {
        mutableLiveDataCommentList=new MutableLiveData<>();
    }

    public MutableLiveData<List<CommentModel>> getMutableLiveDataCommentList(){
        if(mutableLiveDataCommentList!=null)
            mutableLiveDataCommentList=new MutableLiveData<>();
        return mutableLiveDataCommentList;
    }

    public void setCommentModel(List<CommentModel> commentModelList){
        mutableLiveDataCommentList.setValue(commentModelList);
    }
}
