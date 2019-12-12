package uk.ac.ljmu.fet.cs.cmpgkecs;

//import com.ljmu.andre.SimulationHelpers.Packets.SimExercisesBase;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.unimiskolc.iit.distsys.ExercisesBase;

public class BrowserOnLaptop implements ConsumptionEvent {
	PhysicalMachine laptop;
	private String Address;
	private int Port;

	public BrowserOnLaptop() throws Exception {
		laptop = ExercisesBase.getNewPhysicalMachine();
		laptop.turnon();
	}

	public NetworkNode getNIC() {
		return laptop.localDisk;
	}

	//public void setNIC(String Address, int Port) {
		//this.Address = Address;
		//this.Port = Port;
		//return null;
		
	//}
	
	public void visualiseAWebPage(final WebServer fromTheServer) {
		if (fromTheServer.getNIC() == null) {
			SecondDCFCode.logMessage(hashCode() + " Server is not responding, let's wait!");
			// The webserver is not yet ready, let's wait a minute.
			new DeferredEvent(60 * 1000) {
				@Override
				protected void eventAction() {
					visualiseAWebPage(fromTheServer);
				}
			};
		} else {
			// Here we can communicate to the LAMP VM of ours by starting a
			// typical HTTP request message:
			// http://stackoverflow.com/questions/5358109/what-is-the-average-size-of-an-http-request-response-header
			try {
				SecondDCFCode.logMessage(hashCode() + " Finally we are sending our request!");
				NetworkNode.initTransfer(800, ResourceConsumption.unlimitedProcessing, getNIC(), fromTheServer.getNIC(),
						fromTheServer.getRequestHandler(this));
			} catch (NetworkException someBadNews) {
				System.err.println("This is not really happening!?!");
				System.exit(3);
			}
		}
	}

	@Override
	public void conComplete() {
		SecondDCFCode.logMessage(hashCode() + " The webpage has arrived!");
	}

	@Override
	public void conCancelled(ResourceConsumption problematic) {

	}
}
