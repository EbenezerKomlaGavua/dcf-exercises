package com.ljmu.andre.SimulationHelpers.Packets;

/**
 * Created by Andre on 07/02/2017.
 */
public class SubscriptionPacket extends BasePacket {
    private boolean subState;

    public SubscriptionPacket(boolean subState) {
        super(subState ? "Subscribe" : "Unsubscribe", 16, false);
        this.subState = subState;
        setShouldStore(false);
    }

    public boolean getSubState() {
        return subState;
    }
}
