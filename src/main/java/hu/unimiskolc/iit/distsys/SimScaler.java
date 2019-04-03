package hu.unimiskolc.iit.distsys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.State;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.unimiskolc.iit.distsys.interfaces.BasicJobScheduler;

public class SimScaler implements BasicJobScheduler,
VirtualMachine.StateChange{

	
	//Declaration of variables and initialization of ArrayLists for jobs
		private IaaSService iaas;
		private Repository repo;
		private VirtualAppliance va;
		private boolean runningInCicle = false;
		private HashMap<VirtualMachine, Job> vmsWithPurpose = new HashMap<VirtualMachine, Job>();
		private HashMap<VirtualMachine, DeferredEvent> vmPool = new HashMap<VirtualMachine, DeferredEvent>();
		private int jobCounter = 0;
			
		
		public double checkAvailability(ArrayList<Integer> pArray) {
			int count0 = 0;
			int count1 = 0;

			for (int i = 0; i < pArray.size(); i++) {
				if (pArray.get(i).intValue() == 0) {
					count0++;
				} else {
					count1++;
				}
			}

			return (double) count1 / (count1 + count0);
		}
		
		
				
		
		@Override
		public void setupVMset(Collection<VirtualMachine> vms) {
			// TODO Auto-generated method stub
			//ignore
		}

		@Override
		public void setupIaaS(IaaSService iaas) {
			// TODO Auto-generated method stub
			this.iaas = iaas;
			repo = iaas.repositories.get(0);
			va = (VirtualAppliance) repo.contents().iterator().next();
		}
	       // Declaring a method to handle job requests
		@Override
		public void handleJobRequestArrival(Job j) {
			// TODO Auto-generated method stub
			
			//Providing vm for all requests  and ensuring that they are properly allocated
			try {
				ConstantConstraints cc = new ConstantConstraints(j.nprocs, ExercisesBase.minProcessingCap,
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
				VirtualMachine vm = iaas.requestVM(va, cc, repo, 1)[0];
				vm.subscribeStateChange(this);
				vmsWithPurpose.put(vm, j);


				if (!runningInCicle) {
					runningInCicle = true;
					ComplexDCFJob newJob = new ComplexDCFJob((ComplexDCFJob) j);

					for (int i = 0; i < 7; i++) {
						handleJobRequestArrival(newJob);
					}

					runningInCicle = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new RuntimeException(e);
			}

		}
	// Allocate Vm for Jobs 
		private void allocateVMforJob(final VirtualMachine vm, final Job j) {
			try {

				final ComplexDCFJob Job = new ComplexDCFJob((ComplexDCFJob) j);

				jobCounter++;

				((ComplexDCFJob) j).startNowOnVM(vm, new ConsumptionEventAdapter() {

					@Override
					public void conComplete() {
						super.conComplete();
						vmPool.put(vm, new DeferredEvent(ComplexDCFJob.noJobVMMaxLife - 10000) {
							protected void eventAction() {
								try {
									vmPool.remove(vm);
									vm.destroy(false);
								} catch (Exception e) {
									e.printStackTrace();
									throw new RuntimeException(e);
								}
							
							}
						});
							// jobCounter is utilized to displayed the specific jobs in the arrayList completed
						System.out.println(jobCounter + ".job -> Original job has finished! -> group: "
								+ ((ComplexDCFJob) j).getAvailabilityLevel());
					
					
								
				
					}
					});
					
					}
					 catch (Exception e) {
						e.printStackTrace();
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

			
			


	@Override
	public void stateChanged(VirtualMachine vm, com.sun.glass.ui.EventLoop.State oldState,
			com.sun.glass.ui.EventLoop.State newState) {
		// TODO Auto-generated method stub
		
	}
			}
		