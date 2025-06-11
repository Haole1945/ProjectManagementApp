package com.example.projectmanagement.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.projectmanagement.R;
import com.example.projectmanagement.models.SubTask;

import java.util.List;

public class SubTaskAdapter extends RecyclerView.Adapter<SubTaskAdapter.SubTaskViewHolder> {

    private List<SubTask> subTasks;
    private OnSubTaskCheckedChangeListener listener;

    public interface OnSubTaskCheckedChangeListener {
        void onSubTaskCheckedChanged(SubTask subTask, boolean isChecked);
    }

    public SubTaskAdapter(List<SubTask> subTasks, OnSubTaskCheckedChangeListener listener) {
        this.subTasks = subTasks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SubTaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sub_task, parent, false);
        return new SubTaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubTaskViewHolder holder, int position) {
        SubTask subTask = subTasks.get(position);
        holder.cbSubTask.setText(subTask.getSub_title());
        holder.cbSubTask.setChecked(subTask.getIsChecked());

        holder.cbSubTask.setOnCheckedChangeListener(null); // Clear previous listener
        holder.cbSubTask.setChecked(subTask.getIsChecked());
        holder.cbSubTask.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (listener != null) {
                listener.onSubTaskCheckedChanged(subTask, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return subTasks.size();
    }

    static class SubTaskViewHolder extends RecyclerView.ViewHolder {
        CheckBox cbSubTask;

        public SubTaskViewHolder(@NonNull View itemView) {
            super(itemView);
            cbSubTask = itemView.findViewById(R.id.cbSubTask);
        }
    }
} 