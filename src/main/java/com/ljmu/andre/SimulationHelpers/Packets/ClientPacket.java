package com.ljmu.andre.SimulationHelpers.Packets;


import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

import com.ljmu.andre.SimulationHelpers.ConnectionEvent;
import com.ljmu.andre.SimulationHelpers.Packets.BasePacket;
import com.ljmu.andre.SimulationHelpers.Packets.SubscriptionPacket;
import com.ljmu.andre.SimulationHelpers.Packets.RoutingPacket;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import com.ljmu.andre.SimulationHelpers.Packets.*;


public class ClientPacket  extends Timed implements ConnectionEvent{

	private final int connectionCap;
   // private int connectionAttempts = 0;
   // private boolean isOnCooldown = false;
    
    private ServerPacket serverpacket;
    private PhysicalMachine clientpacketMachine;
    
    private ClientPacketData clientpacketData;
    private List<String> failedPacketIds = new ArrayList<String>();
    
    private int dataCollection = 0;
    private long lastSentTime = 0;
	
   // public ClientPacket(PhysicalMachine clientpacketMachine, ClientPacketData clientpacketData) {
	//this(clientpacketMachine,null,clientpacketData);
 //   }
    
    
    public ClientPacket (PhysicalMachine clientpacketMachine, ServerPacket serverpacket,ClientPacketData clientpacketData) {
    this.clientpacketMachine = clientpacketMachine;
    this.serverpacket = serverpacket;
    this.clientpacketData = clientpacketData;
    this.connectionCap = clientpacketData.getConnectionCap();
    	
    }
    public void start() {

        subscribe(clientpacketData.getFrequency());
    }
    	
  //Establishing a route for data transfer
    public void getServerPacket(ServerPacket serverpacket,String route,BasePacket payload,Queue<ConnectionEvent>  routePacket) {
 	   this.serverpacket = serverpacket;
 	  //String route = null;
	Queue<ConnectionEvent>  routePack = new RoutingPacket(route, payload, serverpacket, routePacket).getRoute();
 	   PacketHandler.sendPacket(serverpacket, route, payload);
    }
     
    
    // Binding the client to the server to establish a connection. The client must subscribe for server to accept
    public void bindServerPacket(ServerPacket serverpacket) {
    	this.serverpacket = serverpacket;
    	BasePacket subPacket = new SubscriptionPacket(true).setShouldStore(isSubscribed());
    	PacketHandler.sendPacket(this,serverpacket, subPacket);
    }
    
  
    
    //If the connection is established, the Id of the server will be obtained
    public String getId() {
        return getRepository().getName();
    }
    
    
    
    
    // If the connection fails , packet transfer will fail
     @Override
    public void tick(long fires) {
        if(failedPacketIds.size() >= connectionCap)
            stop();
        
            if (fires >= clientpacketData.getStopTime()) {
            System.out.println("Unsubscribing");
            stop();
               
            BasePacket unsubPacket = new SubscriptionPacket(false).setShouldStore(false);
           
            if (unsubPacket instanceof SubscriptionPacket)
                System.out.println("isUnsub");
            PacketHandler.sendPacket(this,serverpacket, unsubPacket);
    }
        if (fires >= clientpacketData.getStartTime()) {
            System.out.println("Fires: " + fires);
           double dataSize = clientpacketData.getRandomDataPerTick();
            dataCollection += dataSize;
        }
           
        //System.out.println("Value: " + (fires - lastSentTime))
        if (fires - lastSentTime >= clientpacketData.getSendDelay()) {

            System.out.println("SEND DATA");
            
            ArrayList<BasePacket> sendList = new ArrayList<BasePacket>();



            for (String failedPacketId : failedPacketIds) {
                System.out.println("FailedPacketId: " + failedPacketId);
                BasePacket failedObject = (BasePacket) getRepository().lookup(failedPacketId);
                System.out.println("FailedObject: " + failedObject);
               sendList.add(failedObject);
                    }
           
            failedPacketIds.clear();



          BasePacket basePacket = new DataPacket("clientpacketData",dataCollection, false);
            
            sendList.add(basePacket);

          //  ArrayList<BasePacket> failedPackets = PacketHandler.sendPacket(this, serverpacket, basePacket);
            dataCollection = 0;
            lastSentTime = fires;
        }
    }

  
    public void stop() {
        unsubscribe();
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
		return clientpacketMachine.localDisk;
	}

	@Override
	public List<ConnectionEvent> getConnectedDevices() {
		// TODO Auto-generated method stub
		return null;
	}
}
