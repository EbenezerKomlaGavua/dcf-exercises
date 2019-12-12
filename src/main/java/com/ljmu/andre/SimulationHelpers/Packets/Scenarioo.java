package com.ljmu.andre.SimulationHelpers.Packets;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.unimiskolc.iit.distsys.ExercisesBase;

public class Scenarioo {
	// The gap between packet transfer
		public static int timeIncrement = 100;

		public static void logMessage(String message) {
			System.out.println("@ T+" + Timed.getFireCount() + "ms " + message);

}
		public static void main(String[] args) throws Exception {
			
			
		}
}
