package etn.app.danghoc.eat_it_.CallBack;

import java.util.List;

import etn.app.danghoc.eat_it_.Model.CommentModel;

public interface ICommentCallListener {
    void onCommentLoadSuccess(List<CommentModel>commentModels);
    void onCommentLoadFail(String message);
}
