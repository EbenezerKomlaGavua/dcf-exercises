package hu.unimiskolc.iit.distsys;

import java.util.Collection;
import java.util.HashMap;

import com.sun.corba.se.spi.activation.Repository;

import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.unimiskolc.iit.distsys.interfaces.BasicJobScheduler;

public class SimScaler implements BasicJobScheduler,
VirtualMachine.StateChange{

	@Override
	public void stateChanged(VirtualMachine vm, State oldState, State newState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateChanged(VirtualMachine vm, com.sun.glass.ui.EventLoop.State oldState,
			com.sun.glass.ui.EventLoop.State newState) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupVMset(Collection<VirtualMachine> vms) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setupIaaS(IaaSService iaas) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void handleJobRequestArrival(Job j) {
		// TODO Auto-generated method stub
		
	}
}

