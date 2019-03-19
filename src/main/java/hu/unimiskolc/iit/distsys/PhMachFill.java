package hu.unimiskolc.iit.distsys;



import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.unimiskolc.iit.distsys.interfaces.FillInAllPMs;



public class PhMachFill implements FillInAllPMs {

	@Override
	public void filler(IaaSService iaas, int vmCount) {
		// TODO Auto-generated method stub
		
		VirtualAppliance va = new VirtualAppliance("va",1,0);
		
		for (int i=0;i<10;i++)
		{
			try{ 
				ResourceConstraints asd = iaas.machines.get(i).getCapacities();
				ResourceConstraints rcc = new AlterableResourceConstraints(asd.getRequiredCPUs(),asd.getRequiredProcessingPower(),asd.getRequiredMemory());
				iaas.requestVM(va,rcc,iaas.machines.get(i).localDisk,iaas.machines.size());
			}
			catch (Exception e){}
		}

		Timed.simulateUntilLastEvent();
	}

	}


