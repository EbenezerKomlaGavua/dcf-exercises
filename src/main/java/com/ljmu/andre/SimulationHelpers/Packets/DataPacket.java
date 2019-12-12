package com.ljmu.andre.SimulationHelpers.Packets;

/**
 * Created by Andre on 29/03/2017.
 */
public class DataPacket extends BasePacket {
   // public Object sendList;

	public DataPacket(String myid, long mysize, boolean vary) {
        super(myid, mysize, vary);
    }
}
