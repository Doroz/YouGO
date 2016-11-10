package com.shedulerforevents;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.shedulerforevents.model.Event;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Usuario on 01/11/16.
 */

public class MyEventsFragment extends Fragment implements OnMapReadyCallback, EventsAdapter.OnEventClickListener {

    private GoogleMap map;
    private DataBaseHelper helper;
    private EventDAO eventDao;
    private RecyclerView mRecyclerView;
    private EventsAdapter adapter;
    private List<Event> events = new ArrayList<>();
    private ActionMode actionMode;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        helper = new DataBaseHelper(getActivity());
        try {
            eventDao = new EventDAO(helper.getConnectionSource());
            events = eventDao.queryForAll();
            adapter = new EventsAdapter(getActivity(), events);
            adapter.setOnEventClickListener(this);
            mRecyclerView.setAdapter(adapter);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        SupportMapFragment frag = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_fragment);
        frag.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        LatLngBounds bounds = null;

        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        if (events != null && events.size() != 0) {
            for (Event event : events) {
                LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
                Marker marker = map.addMarker(new MarkerOptions()
                        .title(event.getTitle()).snippet(event.getAddress()).position(location));
                marker.showInfoWindow();
                builder.include(marker.getPosition());
            }
            bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 60);
            map.animateCamera(cu);
        }

    }

    @Override
    public void onClickEvent(View view, int position) {
        Event event = events.get(position);
        if (actionMode == null) {
            LatLng location = new LatLng(event.getLatitude(), event.getLongitude());
            CameraUpdate cu = CameraUpdateFactory.newLatLng(location);
            map.animateCamera(cu);
        } else {
            event.setSelected(!event.isSelected());
            updateActionModeTitle();
            mRecyclerView.getAdapter().notifyDataSetChanged();
        }
    }

    private void updateActionModeTitle() {

        List<Event> selectedEvents = getSelectedEvents();

        if (actionMode != null) {
            actionMode.setTitle("Selecione os Eventos.");
                 actionMode.setSubtitle(null);

            if (selectedEvents.size() == 1) {
                actionMode.setSubtitle("1 Evento Selecionado");
            } else if (selectedEvents.size() > 1) {
                actionMode.setSubtitle(selectedEvents.size() + " Eventos Selecionados");
            }
        }
    }

    private List<Event> getSelectedEvents() {
        List<Event> list = new ArrayList<>();
        for (Event e : events) {
            if (e.isSelected()) {
                list.add(e);
            }
        }
        return list;
    }

    @Override
    public void onLongClickEvent(View view, int position) {
        if (actionMode != null) {
            return;
        }

        actionMode = getActivity().startActionMode(getActionModeCallback());

        Event e = events.get(position);
        e.setSelected(true);
        mRecyclerView.getAdapter().notifyDataSetChanged();

        updateActionModeTitle();
    }

    public android.view.ActionMode.Callback getActionModeCallback() {
        return new android.view.ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(android.view.ActionMode actionMode, Menu menu) {
                MenuInflater inflater = getActivity().getMenuInflater();
                inflater.inflate(R.menu.context_menu, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(android.view.ActionMode actionMode, Menu menu) {
                return true;
            }

            @Override
            public boolean onActionItemClicked(android.view.ActionMode actionMode, MenuItem menuItem) {
                List<Event> selectedEvent = getSelectedEvents();
                if (menuItem.getItemId() == R.id.action_trash) {
                    try {
                        for (Event e: selectedEvent) {
                            eventDao.delete(e);
                            events.remove(e);
                            Toast.makeText(getActivity(), "Eventos Excluídos com Sucesso!", Toast.LENGTH_SHORT).show();
                        }
                    } catch (SQLException e){
                        Toast.makeText(getActivity(), "Erro ao excluir o Evento", Toast.LENGTH_SHORT).show();
                    }
                }
                actionMode.finish();
                return true;
            }

            @Override
            public void onDestroyActionMode(android.view.ActionMode actionmode) {
                actionMode = null;
                for (Event e : events) {
                    e.setSelected(false);
                }

                mRecyclerView.getAdapter().notifyDataSetChanged();
            }

        };
    }
}
