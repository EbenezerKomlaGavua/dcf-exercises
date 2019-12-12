package uk.ac.ljmu.fet.cs.cmpgkecs;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;

public class WebServer {
	private class RequestTranslator implements ConsumptionEvent {
		private BrowserOnLaptop browser;

		private RequestTranslator(BrowserOnLaptop browser) {
			this.browser = browser;
		}

		@Override
		public void conCancelled(ResourceConsumption problematic) {
			// do nothing
		}

		@Override
		public void conComplete() {
			handleReceivedMessage(browser);
		}
	}

	private final VirtualMachine ourLampVMinCloud;

	public WebServer(IaaSService theCloud) throws VMManagementException, NetworkException {
		// We ask the cloud where is its storage:
		Repository cloudStorage = theCloud.repositories.get(0);
		// We want our future virtual machine to have 2 cores, 4GFlops/core
		// performance and 1GBytes of memory:
		ResourceConstraints vmSize = new ConstantConstraints(2, 4000000, 1024 * 1024 * 1024l);

		// We create a virtual machine image (i.e., its disk) with our LAMP
		// server on it:
		// 30*1000*xx => the number of instructions our vm could possibly
		// execute in half a minute worth of miliseconds
		VirtualAppliance webServerAndOSImage = new VirtualAppliance("LAMPServer",
				30 * 1000 * vmSize.getTotalProcessingPower(), 0);

		// We upload it to the cloud storage:
		cloudStorage.registerObject(webServerAndOSImage);

		// Now we launch a single (1) virtual machine of our LAMP image from the
		// cloud storage on the cloud:
		ourLampVMinCloud = theCloud.requestVM(webServerAndOSImage, vmSize, cloudStorage, 1)[0];
	}

	public NetworkNode getNIC() {
		if (ourLampVMinCloud.getState().equals(VirtualMachine.State.RUNNING)) {
			return ourLampVMinCloud.getResourceAllocation().getHost().localDisk;
		}
		return null;
	}

	public void handleReceivedMessage(final BrowserOnLaptop fromBrowser) {
		// When did the request arrive?
		final long reqStart = Timed.getFireCount();
		SecondDCFCode.logMessage("The LAMP server has received the request, it is now working on the response");
		// browser's request arrived
		if (ourLampVMinCloud.getState().equals(VirtualMachine.State.RUNNING)) {
			try {
				// Let's do some instructions to compute the response
				// Let's assume the response is calculated in just a little
				// longer time we receive the requests
				ourLampVMinCloud.newComputeTask(
						ourLampVMinCloud.getPerTickProcessingPower() * SecondDCFCode.timeIncrement * 1.1,
						ResourceConsumption.unlimitedProcessing, new ConsumptionEventAdapter() {
							@Override
							public void conComplete() {
								// When did we finish the request processing?
								final long reqEnd = Timed.getFireCount();
								SecondDCFCode.logMessage("The response is worked out (it took " + (reqEnd - reqStart)
										+ " ms), let's send back to the browser");
								// The response is calculated, lets' send it:
								super.conComplete();
								// https://www.keycdn.com/support/the-growth-of-web-page-size/
								try {
									// Sending the average webpage:
									NetworkNode.initTransfer((long) (2332 * 1024),
											ResourceConsumption.unlimitedProcessing, getNIC(), fromBrowser.getNIC(),
											fromBrowser);
								} catch (NetworkException moreBadThings) {
									System.err.println("Unexpected error");
									System.exit(1);
								}
							}
						});
			} catch (NetworkException badThing) {
				System.err.println("Unexpected error");
				System.exit(1);
			}
		}

	}

	public ConsumptionEvent getRequestHandler(BrowserOnLaptop forBrowser) {
		return new RequestTranslator(forBrowser);
	}
}
