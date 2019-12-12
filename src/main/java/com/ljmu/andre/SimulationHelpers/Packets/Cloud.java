package com.ljmu.andre.SimulationHelpers.Packets;

import java.util.List;

import com.ljmu.andre.SimulationHelpers.ConnectionEvent;
import com.ljmu.andre.SimulationHelpers.NetworkJob;
import com.ljmu.andre.SimulationHelpers.Utils.Logger;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;

public class Cloud extends Timed implements ConnectionEvent{
	private static final Logger logger = new Logger(Cloud.class);
	
	private ServerPacket serverpacket;
	 private PhysicalMachine cloudMachine;

	 
	 
	 private List<Job> jobList;
	    private int jobNumber = 0;
	 	 
	   
	   public void bindServerPacket(ServerPacket serverpacket) {
	        this.serverpacket = serverpacket;

	    } 
	    
	    @Override
		public void tick(long fires) {
			// TODO Auto-generated method stub
	    	NetworkJob currentJob = (NetworkJob) jobList.get(jobNumber);
	     PacketHandler.sendPacket(this, serverpacket, new DataPacket("Data", currentJob.getPacketSize(), false));
	    	
	     logger.log("Job: " + jobNumber + "/" + jobList.size());

	        if (++jobNumber < jobList.size()) {
	            Job nextJob = jobList.get(jobNumber);
	            long timeDiff = nextJob.getSubmittimeSecs() - currentJob.getSubmittimeSecs();
	            logger.log("TimeDiff: " + timeDiff);
	            this.updateFrequency(timeDiff);
	        } else {
	            PacketHandler.sendPacket(this, serverpacket, new SubscriptionPacket(false));
	            stop();
	        }
	    	    }
	 
	 
	 private void stop() {
	        logger.log("Stopped: " + unsubscribe());
	    }


	    public void start() {
	        logger.log("Started [Frequency: %s]", subscribe(0));
	    }
	    
	@Override
	public void connectionStarted(ConnectionEvent source) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void connectionFinished(ConnectionEvent source, State connectionState, BasePacket packet) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Repository getRepository() {
		// TODO Auto-generated method stub
		return cloudMachine.localDisk;
	}

	@Override
	public List<ConnectionEvent> getConnectedDevices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	

}
