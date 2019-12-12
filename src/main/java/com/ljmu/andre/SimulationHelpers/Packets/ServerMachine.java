package com.ljmu.andre.SimulationHelpers.Packets;

import java.util.List;

import com.ljmu.andre.SimulationHelpers.ConnectionEvent;
import com.ljmu.andre.SimulationHelpers.ConnectionEvent.State;
import com.ljmu.andre.SimulationHelpers.Utils.Logger;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.unimiskolc.iit.distsys.ExercisesBase;

public class ServerMachine implements ConsumptionEvent, ConnectionEvent{
	 private static final Logger logger = new Logger(ServerMachine.class);
	
	PhysicalMachine server;
	private String Address;
	private ClientMachine client;
	private int Port;
	private BasePacket packet;
	
	public ServerMachine() throws Exception {
		server = ExercisesBase.getNewPhysicalMachine();
		Address = "193.6.5.222";
		Port 	= 5000;
		server.turnon();
	}
	
	
	public ServerMachine(PhysicalMachine server,int Port,String Address,ClientMachine client) {
		this.server= server;
		this.Address= Address;
		this.Port= Port;
		this.client = client;
	}

	public void tick(long fires) {
		// TODO Auto-generated method stub
		logger.log("Tick: " + fires);
	}
	
	@Override
	public void connectionStarted(ConnectionEvent client) {
		// TODO Auto-generated method stub
		logger.log("Received connection init: " + client.getRepository().getName());
	}
	
	
	
	@Override
	public void conComplete() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void conCancelled(ResourceConsumption problematic) {
		// TODO Auto-generated method stub
		
	}

	public String getAddress() {
		return Address;
	}

	public void setAddress(String address) {
		Address = address;
	}

	public int getPort() {
		return Port;
	}

	public void setPort(int port) {
		Port = port;
	}


// close connection and print storage metrics
	@Override
	public void connectionFinished(ConnectionEvent client, State connectionState, BasePacket packet) {
		// TODO Auto-generated method stub
		logger.log("Connection finished: " + connectionState);
        printStorageMetrics();

        
       if (connectionState == State.SUCCESS);
            handleSuccess(client, packet);
	}
	
	private void printStorageMetrics() {
        long freeCap = getRepository().getFreeStorageCapacity();
        long maxCap = getRepository().getMaxStorageCapacity();
        logger.log("Disk: " + freeCap + "/" + maxCap);
	
	}
	
	
// handle received subscription  packets
	private void handleSuccess(ConnectionEvent client, BasePacket packet) {
		
	}
	
	
	@Override
	public Repository getRepository() {
		// TODO Auto-generated method stub
		return server.localDisk;
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
