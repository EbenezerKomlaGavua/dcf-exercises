package com.ljmu.andre.SimulationHelpers.Packets;

import com.ljmu.andre.SimulationHelpers.ConnectionEvent;

import java.util.Queue;

/**
 * Created by Andre on 30/03/2017.
 */
public class RoutingPacket extends BasePacket {
    private BasePacket payload;
    private Queue<ConnectionEvent> route;
    private ConnectionEvent source;

    RoutingPacket(String myid, BasePacket payload, ConnectionEvent source, Queue<ConnectionEvent> route) {
        super(myid, payload.size, false);
        this.payload = payload;
        this.source = source;
        this.route = route;
        setShouldStore(false);
    }

    public BasePacket getPayload() {
        return payload;
    }

    public ConnectionEvent getSource() {
        return source;
    }

    public Queue<ConnectionEvent> getRoute() {
        return route;
    }
}
