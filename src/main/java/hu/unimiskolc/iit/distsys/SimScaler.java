package hu.unimiskolc.iit.distsys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
//import java.util.Random;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.unimiskolc.iit.distsys.interfaces.BasicJobScheduler;

public class SimScaler implements BasicJobScheduler,
VirtualMachine.StateChange{

	
	//Declaration of variables and initialization of ArrayLists for jobs
		private IaaSService iaas;
		private Repository repo;
		VirtualAppliance va;
		private HashMap<VirtualMachine, DeferredEvent> vmPool = new HashMap<VirtualMachine, DeferredEvent>();
		final ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		 ArrayList<PhysicalMachine> orderedPMs = new ArrayList<PhysicalMachine>();
		 private HashMap<VirtualMachine, Job> vmsWithPurpose = new HashMap<VirtualMachine, Job>();
		private int count = 0;
				
		boolean scaler = true;
	
		
		
		
		@Override
		public void setupVMset(Collection<VirtualMachine> vms) {
			// TODO Auto-generated method stub
			//ignore
			vms = new ArrayList<VirtualMachine>(vms);
			scaler = false;
		}

		@Override
		public void setupIaaS(IaaSService iaas) {
			// TODO Auto-generated method stub
			this.iaas = iaas;
		
			repo = iaas.repositories.get(0);
			va = (VirtualAppliance) repo.contents().iterator().next();
			
			
			orderedPMs = new ArrayList<PhysicalMachine>(iaas.machines);
			Collections.sort(orderedPMs, new PMComparator());
		
			double max = 0;
			
			for(int i = 0; i < orderedPMs.size(); i++)
			{
				double temp = orderedPMs.get(i).freeCapacities.getTotalProcessingPower();
				if(temp > max)
				{
					max = temp;
				}
			}
	
			System.out.println("Max PM: " + max);
	
			scaler = true;
		
		}
	       // Declaring a method to handle job requests
		
		@Override
		public void handleJobRequestArrival(Job j) {
			// TODO Auto-generated method stub
			
			if(scaler)
			{
				handleScalerJobRequestArrival(j);
			}
			else
			{
				handleFillerJobRequestArrival(j);
			}
		}
			
			
			
		
	private void handleScalerJobRequestArrival(final Job j)
		{	
			
			//Providing vm for all requests  and ensuring that they are properly allocated
			try {
				ConstantConstraints rc = new ConstantConstraints(j.nprocs, ExercisesBase.minProcessingCap,
					ExercisesBase.minMem / j.nprocs);

				for (VirtualMachine vm : vmPool.keySet()) {
					if (vm.getState().toString() == "DESTROYED"
							|| vm.getResourceAllocation().allocated.getRequiredCPUs() >= j.nprocs) {
						vmPool.remove(vm).cancel();
						allocateVMforJob(vm, j);
						return;
					}
				}
				// let's create a new job instance for the vm with requisite resources
				VirtualMachine vm = iaas.requestVM(va, rc, repo, 1)[0];
				vm.subscribeStateChange(this);
				vmsWithPurpose.put(vm, j);
				System.out.println(j);
				
} catch (Exception e) {
	e.printStackTrace();
	throw new RuntimeException(e);
}
		}			
			
			
		private void handleFillerJobRequestArrival(final Job j)
	{
		for(VirtualMachine vm : vms)
		{
			// determining if the VM is actually doing something or not
			if (vm.underProcessing.isEmpty() && vm.toBeAdded.isEmpty()) 
			{
				try 
				{
					// sending the task
					vm.newComputeTask(
							
							// the task will last exactly the amount of secs
							// specified in the job independently from the
							// actual resource requirements of the job
							
							j.getExectimeSecs() * vm.getPerTickProcessingPower() * 5000, 
							ResourceConsumption.unlimitedProcessing,
							new ConsumptionEventAdapter() 
							{
								@Override
								public void conComplete() 
								{
									// marking the end of the job so the final
									
									// job completed   on time
									j.completed();
								}

								@Override
								public void conCancelled(ResourceConsumption problematic) {  }
							});
					
					// Marking the start time of the job (again for the test
					// case)
					j.started();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
		//Create VM with the requisite capabilities to handle the jobs
		private VirtualMachine createNewVM(Job job) throws VMManagementException, NetworkException
		{
				
			double required = job.nprocs * ExercisesBase.minProcessingCap;
			Collections.sort(orderedPMs, new PMComparator());
			
			
			int availableIndex = 0;
			
			for(int i = 0; i < orderedPMs.size(); i++)
			{
				double temp = orderedPMs.get(i).freeCapacities.getTotalProcessingPower();
				if(temp > orderedPMs.get(availableIndex).freeCapacities.getTotalProcessingPower())
				{
					availableIndex = i;
				}
			}
	
			System.out.println("Job requires: " + required);
			System.out.println("Available: " + orderedPMs.get(availableIndex).freeCapacities.getTotalProcessingPower());
			System.out.println(orderedPMs.get(availableIndex).freeCapacities);
		
			double cpu = required / orderedPMs.get(availableIndex).freeCapacities.getTotalProcessingPower();
	
			System.out.println("CPU: " + cpu);
			System.out.println("--------------");
			
			
			
			AlterableResourceConstraints rc = new AlterableResourceConstraints(job.nprocs, job.perProcCPUTime, job.usedMemory);
				//rc.multiply(2);
				
				VirtualMachine vm;

				vm = iaas.requestVM(va, rc,	repo, 1)[0];
				vm.subscribeStateChange(this);
				vmsWithPurpose.put(vm, job);
				vms.add(vm);
				return vm;
		}

	public VirtualMachine getHandlingVM(Job job){
		
			VirtualMachine handler = null;
			AlterableResourceConstraints rc = new AlterableResourceConstraints(job.nprocs, job.perProcCPUTime, job.usedMemory);
			//rc.multiply(2);

			for(VirtualMachine vm : vms)
			{
				
				
				if(vm.getResourceAllocation().allocated.getRequiredCPUs() * vm.getResourceAllocation().allocated.getRequiredProcessingPower()
						> rc.getRequiredCPUs() * rc.getRequiredProcessingPower()
						&& vm.underProcessing.size() == 0
						&& vm.getState() == State.RUNNING)
				{
					handler = vm;
					
					break;
				
				}
			}
	
			return handler;
			}
				
	//Allocate the VM for the jobs
	private void allocateVMforJob(final VirtualMachine vm, Job j) 
	{
		try 
		{
			final ComplexDCFJob myJob = new ComplexDCFJob((ComplexDCFJob)j);
			
			((ComplexDCFJob) j).startNowOnVM(vm, new ConsumptionEventAdapter() 
			{
				@Override
				public void conComplete() 
				{
					super.conComplete();
					vmPool.put(vm, new DeferredEvent(ComplexDCFJob.noJobVMMaxLife - 1000) 
					{
						protected void eventAction() 
						{
							try 
							{
								vmPool.remove(vm);
								vm.destroy(false);
							} 
							catch (Exception e) 
							{
								throw new RuntimeException(e);
							}
						
						}
					});
					
					System.out.println(count+". job -> Original job has finished!");
					count++;
				}
					@Override
					public void conCancelled(ResourceConsumption problematic) {
						System.out.println(count+". job has crashed!");
						count++;
						handleJobRequestArrival(myJob);
										
				}
			});
		} catch (Exception e) 
		{
			throw new RuntimeException(e);
		}

	}	
			
			
	@Override
	public void stateChanged(VirtualMachine vm, State oldState, State newState) {
		// TODO Auto-generated method stub
		if (newState.equals(VirtualMachine.State.RUNNING)) {
			allocateVMforJob(vm, vmsWithPurpose.remove(vm));
			vm.unsubscribeStateChange(this);
		}
	}

	
	/*
	@Override
	public void stateChanged(VirtualMachine vm, com.sun.glass.ui.EventLoop.State oldState,
			com.sun.glass.ui.EventLoop.State newState) {
		// TODO Auto-generated method stub
		
	}
		*/	}
		