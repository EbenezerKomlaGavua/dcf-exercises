package hu.unimiskolc.iit.distsys;



import java.util.Collection;

import com.sun.corba.se.spi.activation.Repository;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.unimiskolc.iit.distsys.interfaces.FillInAllPMs;



public class PhMachFill implements FillInAllPMs {

	ConstantConstraints cc1;
	ResourceConstraints rc1;
	VirtualMachine[] vms1;

	@Override
	public void filler1(IaaSService iaas, int vmCount){
		double maxCPU = 0.0;
		double maxprocessing = 0.0;
		long maxmemory = 0;
		hu.mta.sztaki.lpds.cloud.simulator.io.Repository repo = iaas.repositories.get(0);
		Collection<StorageObject> sos = iaas.repositories.get(0).contents();
		StorageObject so = sos.iterator().next();
		VirtualAppliance va = (VirtualAppliance)so;

		for(PhysicalMachine pm : iaas.machines){
			System.out.println(pm.freeCapacities.getRequiredCPUs());
		}


		System.out.println("----------");

		for(int i=0;i<vmCount;i++){
			maxCPU = 0.0;
			for(PhysicalMachine pm : iaas.machines){
				if(pm.freeCapacities.getRequiredCPUs()>maxCPU){
					maxCPU = pm.freeCapacities.getRequiredCPUs();
					maxmemory = pm.freeCapacities.getRequiredMemory();
					maxprocessing = pm.freeCapacities.getRequiredProcessingPower();
				}
			}

			System.out.println(maxCPU);

			if(i<vmCount-iaas.machines.size()){
				cc1 = new ConstantConstraints(maxCPU/10.0, maxprocessing, maxmemory/10);
			}else{
				cc1 = new ConstantConstraints(maxCPU, maxprocessing, maxmemory);
			}

			try{
				vms1 = iaas.requestVM(va, cc1, repo, 1);
				System.out.println(vms1[0].getState());
				Timed.simulateUntilLastEvent();
				System.out.println(vms1[0].getState());
			}catch(Exception e){
				System.out.println(e);
			}
		}
	}

	@Override
	public void filler(IaaSService iaas, int vmCount) throws NetworkException {
		// TODO Auto-generated method stub
		
	}
}