package com.example.projectmanagement.adapters;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmanagement.R;
import com.example.projectmanagement.databinding.ItemCommentBinding;
import com.example.projectmanagement.models.Comment;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private final List<Comment> comments;
    private OnCommentLongClickListener longClickListener;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.bind(comments.get(position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(List<Comment> newComments) {
        comments.clear();
        comments.addAll(newComments);
        notifyDataSetChanged();
    }

    public void setOnCommentLongClickListener(OnCommentLongClickListener listener) {
        this.longClickListener = listener;
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        private final ItemCommentBinding binding;

        CommentViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Comment comment) {
            // Set user name
            binding.tvName.setText(comment.getUser().getFullname());

            // Set comment content
            binding.tvContent.setText(comment.getContent());

            // Set comment time
            if (comment.getCreatedAt() != null) {
                CharSequence timeAgo = DateUtils.getRelativeTimeSpanString(
                        comment.getCreatedAt().getTime(),
                        System.currentTimeMillis(),
                        DateUtils.MINUTE_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_RELATIVE
                );
                binding.tvTime.setText(timeAgo);
            }

            // Load user avatar
            if (comment.getUser().getAvatar() != null && !comment.getUser().getAvatar().isEmpty()) {
                Glide.with(binding.ivAvatar)
                        .load(comment.getUser().getAvatar())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.ivAvatar);
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_person);
            }

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    return longClickListener.onCommentLongClick(comment, getAdapterPosition());
                }
                return false;
            });
        }
    }

    public interface OnCommentLongClickListener {
        boolean onCommentLongClick(Comment comment, int position);
    }
}
 