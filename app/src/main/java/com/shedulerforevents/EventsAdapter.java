package com.shedulerforevents;

import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.shedulerforevents.model.Event;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Usuario on 01/11/16.
 */

class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder> {

    private List<Event> events = new ArrayList<>();
    private Context context;
    private Locale locale;
    private OnEventClickListener onEventClickListener;

    public interface OnEventClickListener {

        void onClickEvent(View view, int position);

        void onLongClickEvent(View view, int position);

    }

    EventsAdapter(Context context, List<Event> events) {
        this.events = events;
        this.context = context;
    }

    public void setOnEventClickListener(OnEventClickListener onEventClickListener){
        this.onEventClickListener = onEventClickListener;
    }


    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.event_item_view, parent, false);
        EventViewHolder holder = new EventViewHolder(view);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = view.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = view.getResources().getConfiguration().locale;
        }

        return holder;
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, final int position) {
        final Event event = events.get(position);

        DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        String date = format.format(event.getDate());

        SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm");
        String time = localDateFormat.format(event.getDate());

        holder.textTitle.setText(event.getTitle());
        holder.textAdress.setText(event.getAddress());
        holder.textTime.setText(time);
        holder.textDate.setText(date);

        if (onEventClickListener != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onEventClickListener.onClickEvent(view, position);
                }
            });
             holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                 @Override
                 public boolean onLongClick(View view) {
                     onEventClickListener.onLongClickEvent(view, position);
                    return true;
                 }
             });
        }

        int corFundo =  ContextCompat.getColor(context, event.isSelected()? R.color.blue : android.R.color.white);
        holder.itemView.setBackgroundColor(corFundo);
    }

    @Override
    public int getItemCount() {
        return events != null ? events.size() : 0;
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        TextView textDate;
        TextView textTime;
        TextView textAdress;

        EventViewHolder(View view) {
            super(view);

            textTitle = (TextView) view.findViewById(R.id.txt_event_title);
            textDate = (TextView) view.findViewById(R.id.txt_event_date);
            textTime = (TextView) view.findViewById(R.id.txt_event_time);
            textAdress = (TextView) view.findViewById(R.id.txt_event_address);

        }
    }
}
