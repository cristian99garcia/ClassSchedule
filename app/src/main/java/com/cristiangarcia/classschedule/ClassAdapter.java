package com.cristiangarcia.classschedule;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class ClassAdapter extends RecyclerView.Adapter<ClassAdapter.ClassViewHolder> {

    private ArrayList<ClassData> classes;
    private EditActivity activity;

    public ClassAdapter(ArrayList<ClassData> classes, EditActivity activity) {
        this.classes = classes;
        this.activity = activity;
    }

    @Override
    public ClassViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cardview_class, parent, false);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int color = Color.TRANSPARENT;
                Drawable background = view.findViewById(R.id.vClassColor).getBackground();
                if (background instanceof ColorDrawable)
                    color = ((ColorDrawable) background).getColor();

                activity.setClassName(((TextView)view.findViewById(R.id.tvClassName)).getText().toString());
                activity.setClassColor(color);
            }
        });
        return new ClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ClassViewHolder holder, int position) {
        ClassData data = classes.get(position);
        holder.vClassColor.setBackgroundColor(data.getColor());
        holder.tvClassName.setText(data.getName());
    }

    @Override
    public int getItemCount() {
        return classes.size();
    }

    public static class ClassViewHolder extends RecyclerView.ViewHolder {

        private View vClassColor;
        private TextView tvClassName;

        public ClassViewHolder(View itemView) {
            super(itemView);

            vClassColor = itemView.findViewById(R.id.vClassColor);
            tvClassName = itemView.findViewById(R.id.tvClassName);
        }
    }
}
