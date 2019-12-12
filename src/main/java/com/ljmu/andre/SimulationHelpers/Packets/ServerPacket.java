package com.ljmu.andre.SimulationHelpers.Packets;

import java.util.List;

import com.ljmu.andre.SimulationHelpers.ConnectionEvent;
import com.ljmu.andre.SimulationHelpers.Utils.Logger;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;

public class ServerPacket extends Timed implements ConnectionEvent {
	  private static final Logger logger = new Logger(ServerPacket.class);
	
	
	private PhysicalMachine physicalMachine;
	  private ServerPacket serverData;


	private List<ConnectionEvent> clientpacketList;


	private List<ConnectionEvent> clientPacketList;
	

	  
	  public ServerPacket(PhysicalMachine physicalMachine, ServerPacket serverData) {
	        this.physicalMachine = physicalMachine;
	        this.serverData = serverData;

	    }

  
	  
	@Override
	public void tick(long fires) {
		// TODO Auto-generated method stub
		logger.log("Tick: " + fires);
	}

	@Override
	public void connectionStarted(ConnectionEvent source) {
		// TODO Auto-generated method stub
		logger.log("Received connection init: " + source.getRepository().getName());
	}

	
	@Override
	public void connectionFinished(ConnectionEvent source, State connectionState, BasePacket packet) {
		// TODO Auto-generated method stub
		logger.log("Connection finished: " + connectionState);
        printStorageMetrics();

        
       if (connectionState == State.SUCCESS);
            handleSuccess(source, packet);
	}

	
	
	@Override
	public Repository getRepository() {
		// TODO Auto-generated method stub
		return physicalMachine.localDisk;
	}

	private void printStorageMetrics() {
        long freeCap = getRepository().getFreeStorageCapacity();
        long maxCap = getRepository().getMaxStorageCapacity();
        logger.log("Disk: " + freeCap + "/" + maxCap);

    }
	
	private void handleSuccess(ConnectionEvent source, BasePacket packet) {
        logger.log("Successfully received packet: " + packet.getClass().getSimpleName());
        if (packet instanceof SubscriptionPacket) {
            SubscriptionPacket subPacket = (SubscriptionPacket) packet;



            if (subPacket.getSubState()) {
                if (source instanceof ClientPacket);
                    addClientPacket((ClientPacket) source);


            } else {

                if (source instanceof ClientPacket)
                    removeClientPacket((ClientPacket) source);
            }

        } else if (packet instanceof DataPacket) {

        }

    }
	
	private void addClientPacket(ClientPacket clientpacket) {
        boolean result = clientpacketList.add(clientpacket);
        logger.log("Added watch: " + result);
       if (result && !isSubscribed());
           start();

    }



    private void removeClientPacket(ClientPacket clientpacket) {
    	clientPacketList.remove(clientpacket);
       if (clientpacketList.size() <= 0 && isSubscribed());
           stop();

    }
	
	
    public void start() {
        logger.log("Started [Frequency: " + subscribe(serverData.getFrequency()) + "]");

    }


    private void stop() {
        logger.log("Stopped: " + unsubscribe());

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



	public void bindClientPacket(ClientPacket clientpacket) {
		// TODO Auto-generated method stub
		
	}



	
	
	
	

}
