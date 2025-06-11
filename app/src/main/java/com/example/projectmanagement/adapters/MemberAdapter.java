package com.example.projectmanagement.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.projectmanagement.databinding.ItemMemberBinding;
import com.example.projectmanagement.models.User;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
    private List<User> members;

    public MemberAdapter(List<User> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMemberBinding binding = ItemMemberBinding.inflate(
            LayoutInflater.from(parent.getContext()),
            parent,
            false
        );
        return new MemberViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        holder.bind(members.get(position));
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        private ItemMemberBinding binding;

        MemberViewHolder(ItemMemberBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(User user) {
            binding.tvMemberName.setText(user.getFullname());
            binding.tvMemberEmail.setText(user.getEmail());
            binding.tvMemberRole.setText(user.getRole());

            // Load avatar using Glide
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(binding.ivAvatar)
                    .load(user.getAvatar())
                    .circleCrop()
                    .into(binding.ivAvatar);
            }
        }
    }
} 