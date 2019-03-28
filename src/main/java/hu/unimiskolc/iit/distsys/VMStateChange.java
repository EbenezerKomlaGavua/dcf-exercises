package hu.unimiskolc.iit.distsys;

import java.util.ArrayList;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChange;

public class VMStateChange implements StateChange {

	public VMStateChange(ComplexDCFJob job, ArrayList<VirtualMachine> vms, IaaSService iaas) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void stateChanged(VirtualMachine vm, State oldState, State newState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stateChanged(VirtualMachine vm, com.sun.glass.ui.EventLoop.State oldState,
			com.sun.glass.ui.EventLoop.State newState) {
		// TODO Auto-generated method stub

	}

}
