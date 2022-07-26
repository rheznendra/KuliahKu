package com.rz.kuliahku.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rz.kuliahku.R;
import com.rz.kuliahku.model.ParentSubject;

import java.util.List;

public class ParentList extends RecyclerView.Adapter<ParentList.ParentSubjectViewHolder> {

    Context context;
    List<ParentSubject> parentList;
    private SubjectList adapter;

    public ParentList(Context context, List<ParentSubject> parentList) {
        this.context = context;
        this.parentList = parentList;
    }

    @NonNull
    @Override
    public ParentSubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.rv_parent_subject,parent, false);
        return new ParentSubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ParentSubjectViewHolder holder, int position) {
        holder.dayTitle.setText(parentList.get(position).getDay());

        holder.rv_subject.setHasFixedSize(true);
        holder.rv_subject.setLayoutManager(new LinearLayoutManager(context));
        adapter = new SubjectList(context, parentList.get(position).getSubjectList(), true);
        holder.rv_subject.setAdapter(adapter);

    }

    @Override
    public int getItemCount() {
        return parentList.size();
    }

    public class ParentSubjectViewHolder extends RecyclerView.ViewHolder {

        TextView dayTitle;
        RecyclerView rv_subject;

        public ParentSubjectViewHolder(View itemView) {
            super(itemView);
            dayTitle = itemView.findViewById(R.id.dayTitle);
            rv_subject = itemView.findViewById(R.id.rv_subjects);
        }
    }
}
