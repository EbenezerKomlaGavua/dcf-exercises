package hu.unimiskolc.iit.distsys;

import java.util.Collection;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;

public class JobConsumptionEventAdapter implements ConsumptionEvent {

	public JobConsumptionEventAdapter(VirtualMachine vm, Collection<VirtualMachine> vms, IaaSService iaas) {
		 //TODO Auto-generated constructor stub
	
	}
	

	

//	public JobConsumptionEventAdapter(VirtualMachine vm, VirtualMachine vms, IaaSService iaas) {
		// TODO Auto-generated constructor stub
	//}




	public JobConsumptionEventAdapter(VirtualMachine vm, VirtualMachine vms, IaaSService iaas) {
		// TODO Auto-generated constructor stub
	}




	@Override
	public void conComplete() {
		// TODO Auto-generated method stub

	}

	@Override
	public void conCancelled(ResourceConsumption problematic) {
		// TODO Auto-generated method stub

	}

}
