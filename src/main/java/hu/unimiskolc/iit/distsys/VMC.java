package hu.unimiskolc.iit.distsys;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.unimiskolc.iit.distsys.interfaces.VMCreationApproaches;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.unimiskolc.iit.ExercisesBase;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;


public class  VMC implements VMCreationApproaches {
	
	
	
	@Override
		public void directVMCreation() throws Exception{
	
		PhysicalMachine pmach = ExercisesBase.getNewPhysicalMachine();
		
		VirtualAppliance va = new VirtualAppliance ("NewVir",1,0);
		pmach.localDisk.registerObject(va);
		pmach.turnon();
		ResourceConstraints rc = new AlterableResourceConstraints(0.1,0.1,18);
		pmach.requestVM(va, rc, pmach.localDisk,2);
		Timed.simulateUntilLastEvent();
		
	}
		
	
	
	@Override
	public void twoPhaseVMCreation() throws Exception {
		// TODO Auto-generated method stub
		PhysicalMachine pmach = ExercisesBase.getNewPhysicalMachine();
		VirtualAppliance va = new VirtualAppliance ("NewVir",1,0);
		pmach.turnon();
		pmach.localDisk.registerObject(va);
		
		ResourceConstraints rc = new AlterableResourceConstraints(0.1,0.1,18);
		ResourceAllocation res = pmach.allocateResources(rc, true, 100);
		VirtualMachine vm = new VirtualMachine(va);
		pmach.requestVM(va, rc, pmach.localDisk,2);
		pmach.deployVM(vm, res, pmach.localDisk);
		//ResourceAllocation res2 = pmach.allocateResources(rc, true, 100);
		//virtualMachine vm2= new virtualMachine(va);
		//pmach.deployvm2(vm2, res2, pmach.localDisk);
		
		Timed.simulateUntilLastEvent();
	
		
	}

	@Override
	public void indirectVMCreation() throws Exception {
		// TODO Auto-generated method stub
		PhysicalMachine pmach = ExercisesBase.getNewPhysicalMachine();
		pmach.turnon();
		IaaSService iaasSvc = ExercisesBase.getNewIaaSService();
		iaasSvc.registerHost(pmach);
		iaasSvc.registerRepository(pmach.localDisk);
		Timed.simulateUntilLastEvent();
		VirtualAppliance va = new VirtualAppliance ("NewVir",1,0);
		pmach.localDisk.registerObject(va);
		ResourceConstraints rc = new AlterableResourceConstraints(0.1,0.1,18);
		pmach.requestVM(va, rc, pmach.localDisk,2);
		Timed.simulateUntilLastEvent();
	}

	@Override
	public void migratedVMCreation() throws Exception {
		// TODO Auto-generated method stub
		PhysicalMachine pmach = ExercisesBase.getNewPhysicalMachine();
		pmach.turnon();
		PhysicalMachine pmach2 = ExercisesBase.getNewPhysicalMachine();
		pmach2.turnon();
		Timed.simulateUntilLastEvent();
		VirtualAppliance va = new VirtualAppliance ("NewVir",1,0);
		pmach.localDisk.registerObject(va);
		pmach2.localDisk.registerObject(va);
		ConstantConstraints MG= new ConstantConstraints(Math.min(pmach.getCapacities().getRequiredCPUs(),pmach2.getCapacities().getRequiredCPUs()),Math.min(pmach.getCapacities().getRequiredProcessingPower(),pmach2.getCapacities().getRequiredProcessingPower()),Math.min(pmach.getCapacities().getRequiredMemory(), pmach2.getCapacities().getRequiredMemory()));
		ResourceAllocation ra1 = pmach.allocateResources(MG, true, 10);
		ResourceAllocation ra2 = pmach.allocateResources(MG, true, 10);
		VirtualMachine vm1 = new VirtualMachine(va);
		VirtualMachine vm2 = new VirtualMachine (va);
		pmach.deployVM(vm1, ra1, pmach.localDisk);
		pmach.deployVM(vm2, ra2, pmach.localDisk);
		Timed.simulateUntilLastEvent();
		
		
		ResourceConstraints rc = new AlterableResourceConstraints(0.1,0.1,18);
		VirtualMachine[] vm = pmach.requestVM(va, rc, pmach.localDisk,1);
		Timed.simulateUntilLastEvent();
		pmach.migrateVM(vm, pmach2);
		
	}
}

