package uk.ac.ljmu.fet.cs.cmpgkecs;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.unimiskolc.iit.distsys.ExercisesBase;

public class SecondDCFCode {
	// The gap between browser requests
	public static int timeIncrement = 100;

	public static void logMessage(String message) {
		System.out.println("@ T+" + Timed.getFireCount() + "ms " + message);
	}

	public static void main(String[] args) throws Exception {
		// A cloud with 2 computers in it
		IaaSService theCloud = ExercisesBase.getComplexInfrastructure(2);
		// We set up a webserver on our cloud:
		final WebServer ourLampServer = new WebServer(theCloud);
		// Then we ask for our laptop to check what the server is offering:
		for (int i = 0; i < 10; i++) {
			new DeferredEvent(i * timeIncrement) {

				@Override
				protected void eventAction() {
					try {
						// We create our laptop:
						final BrowserOnLaptop ourBrowser = new BrowserOnLaptop();
						ourBrowser.visualiseAWebPage(ourLampServer);
					} catch (Exception other) {
						System.err.println("Unexpected exception");
						System.exit(5);
					}
				}
			};
		}

		// Let's actually do everything:
		Timed.simulateUntilLastEvent();
	}
}
