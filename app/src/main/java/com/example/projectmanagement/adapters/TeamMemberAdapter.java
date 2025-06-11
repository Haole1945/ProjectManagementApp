package com.example.projectmanagement.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmanagement.R;
import com.example.projectmanagement.databinding.ItemTeamMemberBinding;
import com.example.projectmanagement.models.User;

import java.util.List;

public class TeamMemberAdapter extends RecyclerView.Adapter<TeamMemberAdapter.TeamMemberViewHolder> {
    private List<User> members;
    private OnRemoveMemberListener onRemoveMemberListener;

    public TeamMemberAdapter(List<User> members, OnRemoveMemberListener onRemoveMemberListener) {
        this.members = members;
        this.onRemoveMemberListener = onRemoveMemberListener;
    }

    @NonNull
    @Override
    public TeamMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTeamMemberBinding binding = ItemTeamMemberBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new TeamMemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamMemberViewHolder holder, int position) {
        holder.bind(members.get(position), onRemoveMemberListener);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    public void updateMembers(List<User> newMembers) {
        this.members = newMembers;
        notifyDataSetChanged();
    }

    // Interface for removing a member
    public interface OnRemoveMemberListener {
        void onRemoveCurrentProjectMember(User member);
    }

    static class TeamMemberViewHolder extends RecyclerView.ViewHolder {
        private final ItemTeamMemberBinding binding;

        TeamMemberViewHolder(ItemTeamMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User member, OnRemoveMemberListener listener) {
            binding.tvName.setText(member.getFullname());
            binding.tvEmail.setText(member.getEmail());
            binding.tvRole.setText(member.getRole());

            // Show/hide owner tag
            if (member.isOwner()) {
                binding.tvOwnerTag.setVisibility(View.VISIBLE);
            } else {
                binding.tvOwnerTag.setVisibility(View.GONE);
            }

            // Load avatar if available
            if (member.getAvatar() != null && !member.getAvatar().isEmpty()) {
                Glide.with(binding.ivAvatar)
                        .load(member.getAvatar())
                        .placeholder(R.drawable.ic_person)
                        .error(R.drawable.ic_person)
                        .into(binding.ivAvatar);
            } else {
                binding.ivAvatar.setImageResource(R.drawable.ic_person);
            }
        }
    }
} 