package com.ljmu.andre.SimulationHelpers.Packets;

import java.util.ArrayList;
import java.util.List;

import com.ljmu.andre.SimulationHelpers.ConnectionEvent;
import com.ljmu.andre.SimulationHelpers.ConnectionEvent.State;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.unimiskolc.iit.distsys.ExercisesBase;

public class ClientMachine implements ConsumptionEvent,ConnectionEvent{
	PhysicalMachine client;
	private String Address;
	private int Port;
	private ServerMachine server;
	private List<String> failedPacketIds = new ArrayList<String>();
	private BasePacket packet;
	
	public ClientMachine() throws Exception {
		client = ExercisesBase.getNewPhysicalMachine();
		Address = "193.6.5.76";
		Port 	= 5000;
		client.turnon();
	}
	
	
	public ClientMachine(PhysicalMachine client,ServerMachine server,int Port,String Address) {
		this.client= client;
		this.server= server;
		this.Address= Address;
		this.Port= Port;
	}
	
    // Binding the client to the server to establish a connection. The client must subscribe for server to accept
	public void bindServerMachine(ServerMachine server) {
		this.server = server;
		BasePacket bindPacket= new SubscriptionPacket(true).setShouldStore(true);
		PacketHandler.sendPacket(this, server, bindPacket);
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




	@Override
	public void connectionStarted(ConnectionEvent source) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void connectionFinished(ConnectionEvent source, State connectionState, BasePacket packet) {
		// TODO Auto-generated method stub
		 if (connectionState == State.FAILED && source == this) {
	            failedPacketIds.add(packet.id);
	            System.out.println("Added failed packet");
	        }
	}



	@Override
	public Repository getRepository() {
		// TODO Auto-generated method stub
		return client.localDisk;
	}


	@Override
	public List<ConnectionEvent> getConnectedDevices() {
		// TODO Auto-generated method stub
		return null;
	}

	  //If the connection is established, the Id of the server will be obtained
	@Override
	public String getId() {
		// TODO Auto-generated method stub
		return getRepository().getName();
	}

}
