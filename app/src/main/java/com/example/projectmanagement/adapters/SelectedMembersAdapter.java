package com.example.projectmanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.models.User;

import java.util.List;

public class SelectedMembersAdapter extends RecyclerView.Adapter<SelectedMembersAdapter.ViewHolder> {

    private List<User> selectedMembers;
    private OnRemoveMemberListener listener;

    public SelectedMembersAdapter(List<User> selectedMembers, OnRemoveMemberListener listener) {
        this.selectedMembers = selectedMembers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team_member_selected, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User member = selectedMembers.get(position);
        holder.tvMemberEmail.setText(member.getEmail());
        holder.ivRemoveMember.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemoveMember(member);
            }
        });
    }

    @Override
    public int getItemCount() {
        return selectedMembers.size();
    }

    public void updateSelectedMembers() {
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberEmail;
        ImageView ivRemoveMember;

        ViewHolder(View itemView) {
            super(itemView);
            tvMemberEmail = itemView.findViewById(R.id.tvMemberEmail);
            ivRemoveMember = itemView.findViewById(R.id.ivRemoveMember);
        }
    }

    public interface OnRemoveMemberListener {
        void onRemoveMember(User member);
    }
} 