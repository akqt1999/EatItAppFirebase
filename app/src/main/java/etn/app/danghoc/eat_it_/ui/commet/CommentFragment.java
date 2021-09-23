package etn.app.danghoc.eat_it_.ui.commet;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import dmax.dialog.SpotsDialog;
import etn.app.danghoc.eat_it_.Adapter.MyCommentAdapter;
import etn.app.danghoc.eat_it_.CallBack.ICommentCallListener;
import etn.app.danghoc.eat_it_.Common.Common;
import etn.app.danghoc.eat_it_.Model.CommentModel;
import etn.app.danghoc.eat_it_.R;

public class CommentFragment extends BottomSheetDialogFragment implements ICommentCallListener {

    Unbinder unbinder;
    @BindView(R.id.recycle_comment)
    RecyclerView recyclerComment;

    CommentViewModel commentViewModel;
    AlertDialog dialog;
    ICommentCallListener listener;

    public CommentFragment() {
        listener = this;
    }

    private static CommentFragment instance;

    public static CommentFragment getInstance() {
        if (instance == null)
            instance = new CommentFragment();
        return instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View itemView = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_comment_fragmet, container, false);
        unbinder = ButterKnife.bind(this, itemView);
        init();
        loadCommentFromFirebase();
        commentViewModel.getMutableLiveDataCommentList().observe(this, commentModelList -> {
            MyCommentAdapter adapter=new MyCommentAdapter(getContext(),commentModelList);
            recyclerComment.setAdapter(adapter);
        });
        return itemView;
    }

    private void loadCommentFromFirebase() {
        dialog.show();
        List<CommentModel> commentModels = new ArrayList<>();
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
                .child(Common.selectedFood.getId())
                .orderByChild("commentTimeStamp")
                .limitToLast(100)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot commentSnapshot : snapshot.getChildren()) {
                            CommentModel commentModel = commentSnapshot.getValue(CommentModel.class);
                            commentModels.add(commentModel);
                        }
                        listener.onCommentLoadSuccess(commentModels);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onCommentLoadFail(error.getMessage());
                    }
                });
    }


    private void init() {
        commentViewModel = new ViewModelProvider(this).get(CommentViewModel.class);
        dialog = new SpotsDialog.Builder().setContext(getContext()).setCancelable(false).build();

        recyclerComment.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, true);
        recyclerComment.setLayoutManager(layoutManager);
        recyclerComment.addItemDecoration(new DividerItemDecoration(getContext(), layoutManager.getOrientation()));
    }

    @Override
    public void onCommentLoadSuccess(List<CommentModel> commentModels) {
        dialog.dismiss();
            commentViewModel.setCommentModel(commentModels);
    }

    @Override
    public void onCommentLoadFail(String message) {
        dialog.dismiss();
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
