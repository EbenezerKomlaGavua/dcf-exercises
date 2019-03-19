package hu.unimiskolc.iit.distsys;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.io.StorageObject;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.unimiskolc.iit.distsys.interfaces.VMCreationApproaches;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.unimiskolc.iit.distsys.ExercisesBase;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;

public class  VMC implements VMCreationApproaches {
	
	
	
	
	

	@Override
		public void directVMCreation() throws Exception{
	
		PhysicalMachine pmach1 = ExercisesBase.getNewPhysicalMachine();
		 VirtualAppliance va = new VirtualAppliance ("NewVir",38.0,0);
		pmach1.localDisk.registerObject(va);
		pmach1.turnon();
		
		Timed.simulateUntilLastEvent();
		
		  
		 ResourceConstraints rc = new AlterableResourceConstraints(0.25,10.0,128000);
		
		
		 pmach1.requestVM(va, rc, pmach1.localDisk,2);
		                                              
		Timed.simulateUntilLastEvent();
		
	}
		
	
	
	@Override
	public void twoPhaseVMCreation() throws Exception {
		// TODO Auto-generated method stub
		PhysicalMachine pmach1 = ExercisesBase.getNewPhysicalMachine();
		pmach1.turnon();
		Timed.simulateUntilLastEvent();
		
		VirtualAppliance va1 = new VirtualAppliance ("NewVir1",38.0,0);
		VirtualAppliance va2 = new VirtualAppliance("NewVir2", 38.0,0);
		
		VirtualMachine vm1 = new VirtualMachine(va1);
		VirtualMachine vm2 = new VirtualMachine(va2);
		
		ResourceConstraints rc1 = new ConstantConstraints(0.25, 10.0, 128000);
		ResourceConstraints rc2 = new ConstantConstraints(0.25, 10.0, 128000);

		ResourceAllocation ra1 = pmach1.allocateResources(rc1, false, 3);
		ResourceAllocation ra2 = pmach1.allocateResources(rc2, false, 3);
	
	
		
	    pmach1.localDisk.registerObject(va1);
	     pmach1.localDisk.registerObject(va2);
	     pmach1.deployVM(vm1, ra1, pmach1.localDisk);
		   pmach1.deployVM(vm2, ra2, pmach1.localDisk);
		Timed.simulateUntilLastEvent();
	
		
	}

	@Override
	public void indirectVMCreation() throws Exception {
		// TODO Auto-generated method stub
		PhysicalMachine pmach1 = ExercisesBase.getNewPhysicalMachine();
		pmach1.turnon();
		
		IaaSService iaasSvc = ExercisesBase.getNewIaaSService();
		iaasSvc.registerHost(pmach1);
		iaasSvc.registerRepository(pmach1.localDisk);
		Timed.simulateUntilLastEvent();
		
		VirtualAppliance va1 = new VirtualAppliance ("NewVir",38.0,0);
		pmach1.localDisk.registerObject(va1);
		ResourceConstraints rc = new AlterableResourceConstraints(0.25,10.0,128000);
	
		
		iaasSvc.requestVM(va1,rc,pmach1.localDisk,2);
		
		Timed.simulateUntilLastEvent();
	}

	@Override
	public void migratedVMCreation() throws Exception {
		// TODO Auto-generated method stub
		
		
		PhysicalMachine pmach1 = ExercisesBase.getNewPhysicalMachine();
		pmach1.turnon();
		PhysicalMachine pmach2 = ExercisesBase.getNewPhysicalMachine();
		pmach2.turnon();
		Timed.simulateUntilLastEvent();
		
		
		VirtualMachine vm = null;
		VirtualAppliance va1 = new VirtualAppliance ("NewVir",38.0,0);
	
		Timed.simulateUntilLastEvent();
	    ResourceConstraints rc = new AlterableResourceConstraints(0.25,10,128000);
		
		vm = pmach1.requestVM(va1, rc, pmach1.localDisk,1)[0];
		
		pmach1.localDisk.registerObject(va1);
		Timed.simulateUntilLastEvent();
		
		
		pmach2.localDisk.registerObject(va1);
		
		
		Timed.simulateUntilLastEvent();
		
		
			
		
		//ConstantConstraints MG= new ConstantConstraints(Math.min(pmach.getCapacities().getRequiredCPUs(),pmach2.getCapacities().getRequiredCPUs()),Math.min(pmach.getCapacities().getRequiredProcessingPower(),pmach2.getCapacities().getRequiredProcessingPower()),Math.min(pmach.getCapacities().getRequiredMemory(), pmach2.getCapacities().getRequiredMemory()));
		 
		 	
		
		Timed.simulateUntilLastEvent();
		
		pmach1.migrateVM(vm, pmach2);
		
	
		Timed.simulateUntilLastEvent();
	
		
		
	}
}

