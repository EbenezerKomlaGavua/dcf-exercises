package hu.unimiskolc.iit.distsys.forwarders;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.PhysicalMachineController;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.Scheduler;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

public class IaaSForwarder extends IaaSService implements ForwardingRecorder {
	private boolean reqVMcalled = false;

	public IaaSForwarder(Class<? extends Scheduler> s,
			Class<? extends PhysicalMachineController> c)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		super(s, c);
	}

	public void resetForwardingData() {
		reqVMcalled = false;
	}

	public boolean isReqVMcalled() {
		return reqVMcalled;
	}

	@Override
	public VirtualMachine[] requestVM(VirtualAppliance va,
			ResourceConstraints rc, Repository vaSource, int count)
			throws hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException,
			NetworkException {
		reqVMcalled = true;
		return super.requestVM(va, rc, vaSource, count);
	}

	@Override
	public VirtualMachine[] requestVM(VirtualAppliance va,
			ResourceConstraints rc, Repository vaSource, int count,
			HashMap<String, Object> schedulingConstraints)
			throws hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException,
			NetworkException {
		reqVMcalled = true;
		return super.requestVM(va, rc, vaSource, count, schedulingConstraints);
	}
}
