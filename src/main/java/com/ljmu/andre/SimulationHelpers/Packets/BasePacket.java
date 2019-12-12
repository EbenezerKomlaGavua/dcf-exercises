package com.ljmu.andre.SimulationHelpers.Packets;

import java.util.ArrayList;

import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;

/**
 * Created by Andre on 02/02/2017.
 */
public class BasePacket extends StorageObject {
    private static long totalPackets = 0;
    private ArrayList<Object> deregistryList = new ArrayList<Object>();
    private boolean shouldStore = true;

    BasePacket(String myid) {
        super(myid + totalPackets++);
    }

    public BasePacket(String myid, long mysize, boolean vary) {
        super(myid + totalPackets++, mysize, vary);
    }

    /**
     * Check if the supplied Object is scheduled for deregistry
     *
     * @param toDeregister - The Object to check for deregistration
     * @return - True if Object is scheduled for deregistration
     */
    public boolean shouldDeregister(Object toDeregister) {
        return deregistryList.contains(toDeregister);
    }

    public BasePacket setDeregistryList(ArrayList<Object> deregisterFrom) {
        this.deregistryList = deregisterFrom;
        return this;
    }

    /**
     * Schedule the supplied Object for Deregistry
     *
     * @param toDeregister - The Object to schedule for deregistration
     * @return This Object (For inline declaration)
     */
    public BasePacket addDeregisterObject(Object toDeregister) {
        if (!deregistryList.contains(toDeregister))
            deregistryList.add(toDeregister);

        return this;
    }

    /**
     * Get whether or not this Packet should be saved to drive during transfer
     *
     * @return True if it should be saved
     */
    public boolean getShouldStore() {
        return shouldStore;
    }

    /**
     * Set whether or not this Packet should be saved to drive during transer
     *
     * @param shouldStore - Whether or not it should be saved
     * @return This Object (For inline declaration)
     */
    public BasePacket setShouldStore(boolean shouldStore) {
        this.shouldStore = shouldStore;
        return this;
    }
}
