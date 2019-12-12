package com.ljmu.andre.SimulationHelpers;

import com.ljmu.andre.SimulationHelpers.Packets.BasePacket;

import java.util.List;

import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;

/**
 * Created by Andre on 02/02/2017.
 */
public interface ConnectionEvent {
    void connectionStarted(ConnectionEvent source);

    void connectionFinished(ConnectionEvent source, State connectionState, BasePacket packet);

    Repository getRepository();

    List<ConnectionEvent> getConnectedDevices();

    String getId();

    enum State {
        SUCCESS, FAILED, IN_PROGRESS
    }
}
