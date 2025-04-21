package org.hse.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final static int TYPE_ITEM = 0;
    private final static int TYPE_HEADER = 1;

    private List<ScheduleItem> dataList = new ArrayList<>();
    private final OnItemClick onItemClick;

    public ItemAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
    }

    public void setDataList(List<ScheduleItem> dataList) {
        this.dataList = dataList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final OnItemClick onItemClick;
        private final TextView start;
        private final TextView end;
        private final TextView type;
        private final TextView name;
        private final TextView place;
        private final TextView teacher;

        public ViewHolder(View itemView, OnItemClick onItemClick) {
            super(itemView);
            this.onItemClick = onItemClick;

            start = itemView.findViewById(R.id.start);
            end = itemView.findViewById(R.id.end);
            type = itemView.findViewById(R.id.type);
            name = itemView.findViewById(R.id.name);
            place = itemView.findViewById(R.id.place);
            teacher = itemView.findViewById(R.id.teacher);
        }

        public void bind(final ScheduleItem data) {
            start.setText(data.getStart());
            end.setText(data.getEnd());
            type.setText(data.getType());
            name.setText(data.getName());
            place.setText(data.getPlace());
            teacher.setText(data.getTeacher());

            itemView.setOnClickListener(v -> {
                if (onItemClick != null) {
                    onItemClick.onClick(data);
                }
            });
        }
    }

    public static class ViewHolderHeader extends RecyclerView.ViewHolder {
        private final TextView title;

        public ViewHolderHeader(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
        }

        public void bind(final ScheduleItemHeader data) {
            title.setText(data.getTitle());
        }
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == TYPE_ITEM) {
            View itemView = inflater.inflate(R.layout.item_schedule, parent, false);
            return new ViewHolder(itemView, onItemClick);
        } else if (viewType == TYPE_HEADER) {
            View headerView = inflater.inflate(R.layout.item_schedule_header, parent, false);
            return new ViewHolderHeader(headerView);
        } else {
            throw new IllegalArgumentException("Invalid view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder viewHolder, int position) {
        ScheduleItem data = dataList.get(position);
        if (viewHolder instanceof ViewHolder) {
            ((ViewHolder) viewHolder).bind(data);
        } else if (viewHolder instanceof ViewHolderHeader) {
            if (data instanceof ScheduleItemHeader) {
                ((ViewHolderHeader) viewHolder).bind((ScheduleItemHeader) data);
            }
        }
    }

    @Override
    public int getItemCount() {
        return dataList == null ? 0 : dataList.size();
    }

    @Override
    public int getItemViewType(int position) {
        ScheduleItem data = dataList.get(position);
        if (data instanceof ScheduleItemHeader) {
            return TYPE_HEADER;
        } else {
            return TYPE_ITEM;
        }
    }

    interface OnItemClick {
        void onClick(ScheduleItem data);
    }
}