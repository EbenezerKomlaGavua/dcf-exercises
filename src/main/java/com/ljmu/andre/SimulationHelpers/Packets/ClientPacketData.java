package com.ljmu.andre.SimulationHelpers.Packets;

import java.io.Serializable;
import java.util.concurrent.ThreadLocalRandom;

@SuppressWarnings("serial")
public class ClientPacketData implements Serializable{

	  @SerializedName("Id")
	    private String id;

	    @SerializedName("Simulation Duration")
	    private long simDuration;

	    @SerializedName("Start Time")
	    private long startTime;

	    @SerializedName("Frequency")
	   	    private long frequency;

	    @SerializedName("Min Data/Sec")
	    private double minDataPerSec;


	    @SerializedName("Max Data/Sec")
	    private double maxDataPerSec;


	    @SerializedName("Send Delay")
	    private long sendDelay;


	    @SerializedName("Max Connection Attempts")
	    private int connectionCap;
	    
	    
	public ClientPacketData( String id,

            long simDuration,
            long startTime,
            long frequency,
            double minDataPerSec,
            double maxDataPerSec,
            long sendDelay,
            int connectionCap){
        this.id = id;
        this.simDuration = simDuration;
        this.startTime = startTime;
        this.frequency = frequency;
        this.minDataPerSec = minDataPerSec;
        this.maxDataPerSec = maxDataPerSec;
        this.sendDelay = sendDelay;
        this.connectionCap = connectionCap;

    }
	 public String getId() {
	        return id;
	    }


	    public void setId(String id) {
	        this.id = id;
	    }

	    public long getStopTime() {
	        return getStartTime() + getSimDuration();
	    }


	    public long getStartTime() {
	        return startTime;
	    }

	    public long getSimDuration() {
	        return simDuration;
	    }

	    public void setSimDuration(long simDuration) {
	        this.simDuration = simDuration;
	    }

	    public void setStartTime(long startTime) {
	        this.startTime = startTime;
	    }

	    public void setStopTime(long stopTime) {
	    }

	    public long getFrequency() {
	        return frequency;
	    }

	    public void setFrequency(long frequency) {
	        this.frequency = frequency;
	    }

	    public double getMinDataPerSec() {
	        return minDataPerSec;

	    }

	    public void setMinDataPerSec(int minDataPerSec) {
	        this.minDataPerSec = minDataPerSec;
	    }

	   
	    public double getMaxDataPerSec() {
	        return maxDataPerSec;
	    }

	    public void setMaxDataPerSec(int maxDataPerSec) {
	        this.maxDataPerSec = maxDataPerSec;
	    }

	    public double getRandomDataPerTick() {
	        return ( generateData() / 1000 ) * getFrequency();
	    }

	    private double generateData() {
	        if (minDataPerSec == maxDataPerSec)
	            return minDataPerSec;
	        else if (minDataPerSec == -1 || maxDataPerSec == -1)
	            return Math.max(minDataPerSec, maxDataPerSec);
	        return ThreadLocalRandom.current().nextDouble(minDataPerSec, maxDataPerSec);

	    }
	    public long getSendDelay() {
	        return sendDelay;
	    }
	    
	    public void setSendDelay(long sendDelay) {
	        this.sendDelay = sendDelay;
	    }
	    public int getConnectionCap() {
	        return connectionCap;
	    }
	    
	    public void setConnectionCap(int connectionCap) {
	        this.connectionCap = connectionCap;

	    }

	    @Override public String toString() {
	        return "ClientPacketData{" +
	                "id='" + id + '\'' +
	                ", simDuration=" + simDuration +
	                ", startTime=" + startTime +
	                ", frequency=" + frequency +
	                ", minDataPerSec=" + minDataPerSec +
	                ", maxDataPerSec=" + maxDataPerSec +
	                ", sendDelay=" + sendDelay +
	                ", connectionCap=" + connectionCap +
	                '}';
	    }
}
