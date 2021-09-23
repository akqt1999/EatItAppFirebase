package etn.app.danghoc.eat_it_.Adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import etn.app.danghoc.eat_it_.Model.CommentModel;
import etn.app.danghoc.eat_it_.R;

public class MyCommentAdapter extends RecyclerView.Adapter<MyCommentAdapter.ViewHolder> {
    Context context;
    List<CommentModel>commentModels;

    public MyCommentAdapter(Context context, List<CommentModel> commentModels) {
        this.context = context;
        this.commentModels = commentModels;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_comment_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.txtCommentName.setText(commentModels.get(position).getName());
        holder.txtComment.setText(commentModels.get(position).getComment());

        holder.ratingBar.setRating(commentModels.get(position).getRatingValue().floatValue());

        Long timeStamp=Long.valueOf(commentModels.get(position).getCommentTimeStamp().get("timeStamp").toString());
        holder.txtCommentDate.setText(DateUtils.getRelativeTimeSpanString(timeStamp));
    }

    @Override
    public int getItemCount() {
        return commentModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private Unbinder unbinder;
        @BindView(R.id.txtComment)
        TextView txtComment;
        @BindView(R.id.txtCommentDate)
        TextView txtCommentDate;
        @BindView(R.id.txtCommentName)
        TextView txtCommentName;
        @BindView(R.id.ratingBar)
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            unbinder = ButterKnife.bind(this, itemView);
        }
    }
}
