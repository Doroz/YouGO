package com.shedulerforevents;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.support.ConnectionSource;
import com.shedulerforevents.model.Event;

import java.sql.SQLException;

/**
 * Created by Usuario on 01/11/16.
 */

public class EventDAO  extends BaseDaoImpl<Event, Integer> {


    protected EventDAO(ConnectionSource connectionSource) throws SQLException {
        super(Event.class);
        setConnectionSource(connectionSource);
        initialize();
    }
}
