package com.ljmu.andre.SimulationHelpers.Packets;

import com.ljmu.andre.SimulationHelpers.Utils.Logger;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;

public class Scenario extends Timed {
	private static final Logger logger = new Logger(Scenario.class);
	

	
	 private ClientPacket clientpacket;
	    private ServerPacket serverpacket;
	    private Cloud cloud;
	
	
	    Scenario() throws Exception {

	        logger.log("Starting Scenario");

	       

	        //VirtualMachine vm = Cloud.getVM();





	        clientpacket.bindServerPacket(serverpacket);

	       // serverpacket.bindClientPacket(clientpacket);

	        cloud.bindServerPacket(serverpacket);

	        clientpacket.start();

	        serverpacket.start();

	        cloud.start();



	        subscribe(1000);

	        simulateUntilLastEvent();

	    }  
	    
	    
	    
	    
	
	@Override
	public void tick(long fires) {
		// TODO Auto-generated method stub
		
		 if (!serverpacket.isSubscribed() && !clientpacket.isSubscribed() && !cloud.isSubscribed()) {
	            unsubscribe();
	            logger.log("No more subscribers... ENDING");
	        }

	        try {

	            Thread.sleep(1);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
		}
}
