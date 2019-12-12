package com.ljmu.andre.SimulationHelpers.Packets;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.PhysicalMachineController;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.Scheduler;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.notifications.SingleNotificationHandler;
import hu.mta.sztaki.lpds.cloud.simulator.notifications.StateDependentEventHandler;
//import hu.unimiskolc.iit.distsys.forwarders.ForwardingRecorder;
//import hu.unimiskolc.iit.distsys.forwarders.IaaSForwarder.QuoteProvider;
//import hu.unimiskolc.iit.distsys.forwarders.IaaSForwarder.VMListener;

public class IaaSForwarder extends IaaSService implements ForwardingRecorder{
	public interface VMListener {
		void newVMadded(VirtualMachine[] vms);
	}

	public interface QuoteProvider {
		/**
		 * 
		 * @param rc
		 *            if null then the default instance price should be returned
		 * @return
		 */
		double getPerTickQuote(ResourceConstraints rc);
	}

	private boolean reqVMcalled = false;
	private final StateDependentEventHandler<VMListener, VirtualMachine[]> notifyMe = new StateDependentEventHandler<VMListener, VirtualMachine[]>(
			new SingleNotificationHandler<VMListener, VirtualMachine[]>() {
				@Override
				public void sendNotification(VMListener onObject, VirtualMachine[] payload) {
					onObject.newVMadded(payload);
				}
			});
	private QuoteProvider qp = new QuoteProvider() {
		@Override
		public double getPerTickQuote(ResourceConstraints rc) {
			return 1;
		}
	};

	public IaaSForwarder(Class<? extends Scheduler> s, Class<? extends PhysicalMachineController> c)
			throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		super(s, c);
	}

	public void setVMListener(VMListener newListener) {
		notifyMe.subscribeToEvents(newListener);
	}

	public void setQuoteProvider(QuoteProvider qp) {
		this.qp = qp;
	}

	public double getResourceQuote(ResourceConstraints rc) {
		return qp.getPerTickQuote(rc);
	}

	public void resetForwardingData() {
		reqVMcalled = false;
	}

	public boolean isReqVMcalled() {
		return reqVMcalled;
	}

	@Override
	public VirtualMachine[] requestVM(VirtualAppliance va, ResourceConstraints rc, Repository vaSource, int count)
			throws hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException, NetworkException {
		reqVMcalled = true;
		return notifyVMListeners(super.requestVM(va, rc, vaSource, count));
	}

	@Override
	public VirtualMachine[] requestVM(VirtualAppliance va, ResourceConstraints rc, Repository vaSource, int count,
			HashMap<String, Object> schedulingConstraints)
					throws hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException, NetworkException {
		reqVMcalled = true;
		return notifyVMListeners(super.requestVM(va, rc, vaSource, count, schedulingConstraints));
	}

	private VirtualMachine[] notifyVMListeners(VirtualMachine[] received) {
		notifyMe.notifyListeners(received);
		return received;
	}
}
