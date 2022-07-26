package com.rz.kuliahku.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.rz.kuliahku.EditSubjectActivity;
import com.rz.kuliahku.R;
import com.rz.kuliahku.model.Subject;
import com.techiness.progressdialoglibrary.ProgressDialog;
import com.thecode.aestheticdialogs.AestheticDialog;
import com.thecode.aestheticdialogs.DialogStyle;
import com.thecode.aestheticdialogs.DialogType;

import java.util.List;

import dev.shreyaspatil.MaterialDialog.MaterialDialog;

public class SubjectList extends RecyclerView.Adapter<SubjectList.SubjectListViewHolder> {

    private List<Subject> subjectList;
    private Context context;
    private Boolean menu;
    private ProgressDialog pDialog;

    public SubjectList(Context context, List<Subject> subjectList, boolean menu) {
        this.context = context;
        this.subjectList = subjectList;
        this.menu = menu;
    }

    @NonNull
    @Override
    public SubjectListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rv_subject_item, parent, false);
        return new SubjectListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SubjectListViewHolder holder, int position) {
        Subject subject = subjectList.get(position);
        holder.subjectName.setText(subject.getName());
        holder.classCode.setText(subject.getCode());
        holder.startTime.setText(subject.getStartTime());
        holder.endTime.setText(subject.getEndTime());
    }

    @Override
    public int getItemCount() {
        return subjectList.size();
    }

    public class SubjectListViewHolder extends RecyclerView.ViewHolder {

        private TextView subjectName, classCode, startTime, endTime;

        public SubjectListViewHolder(View itemView) {
            super(itemView);
            pDialog = new ProgressDialog(context);
            subjectName = itemView.findViewById(R.id.subjectName);
            classCode = itemView.findViewById(R.id.classCode);
            startTime = itemView.findViewById(R.id.startTime);
            endTime = itemView.findViewById(R.id.endTime);

            if (menu) {

                itemView.setOnClickListener(v -> {
                    String id = subjectList.get(getBindingAdapterPosition()).getId();
                    int index = getBindingAdapterPosition();
                    AlertDialog.Builder ad = new AlertDialog.Builder(context);
                    ad.setTitle("Menu");
                    String[] menu = {"Edit", "Delete"};
                    ad.setItems(menu, (dialog, which) -> {
                        switch (which) {
                            case 0:
                                Intent intent = new Intent(context, EditSubjectActivity.class);
                                intent.putExtra("id", id);
                                context.startActivity(intent);
                                break;
                            case 1:
                                confirmDelete(id, index);
                                break;
                        }
                    });
                    AlertDialog dialog = ad.create();
                    dialog.show();
                });
            }
        }
    }

    private void confirmDelete(String id, int index) {
        MaterialDialog mDialog = new MaterialDialog.Builder((Activity) context)
                .setTitle("Delete?")
                .setMessage("Are you sure want to delete this file?")
                .setCancelable(false)
                .setPositiveButton("Delete", R.drawable.ic_baseline_delete_24, (MaterialDialog.OnClickListener) (dialogInterface, which) -> {
                    dialogInterface.dismiss();
                    deleteSubject(id, index);
                })
                .setNegativeButton("Cancel", R.drawable.ic_baseline_close_24, (MaterialDialog.OnClickListener) (dialogInterface, which) -> dialogInterface.dismiss())
                .build();
        mDialog.show();
    }

    private void deleteSubject(String id, int index) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        pDialog.show();
        db.collection("schedule")
                .document(id)
                .delete()
                .addOnCompleteListener(task -> {
                    pDialog.dismiss();
                })
                .addOnSuccessListener(unused -> {
                    new AestheticDialog.Builder((Activity) context, DialogStyle.TOASTER, DialogType.SUCCESS)
                            .setTitle("Action Success")
                            .setMessage("Subject succesfully deleted")
                            .show();
                    subjectList.remove(index);
                    notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    new AestheticDialog.Builder((Activity) context, DialogStyle.TOASTER, DialogType.ERROR)
                            .setTitle("Action Failed")
                            .setMessage("Failed to delete subject")
                            .show();
                });
    }
}
