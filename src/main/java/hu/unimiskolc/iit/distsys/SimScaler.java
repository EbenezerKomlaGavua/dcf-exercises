package hu.unimiskolc.iit.distsys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
//import java.util.Random;

import hu.mta.sztaki.lpds.cloud.simulator.DeferredEvent;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.State;
//import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine.StateChange;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
//import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ConstantConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ConsumptionEventAdapter;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
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
		private boolean runningInCicle = false;
		private HashMap<VirtualMachine, Job> vmsWithPurpose = new HashMap<VirtualMachine, Job>();
		private HashMap<VirtualMachine, DeferredEvent> vmPool = new HashMap<VirtualMachine, DeferredEvent>();
		private int jobCounter = 0;
		private ArrayList<Integer> array75 = new ArrayList<Integer>();
		private ArrayList<Integer> array90 = new ArrayList<Integer>();
		private ArrayList<Integer> array95 = new ArrayList<Integer>();
		private ArrayList<Integer> array99 = new ArrayList<Integer>();
		ArrayList<VirtualMachine> vms = new ArrayList<VirtualMachine>();
		ArrayList<PhysicalMachine> orderedPMs = new ArrayList<PhysicalMachine>();
		private int count = 0;
		//IaaSService IaaS;
		//VirtualMachine vms;
		//VirtualMachine vm;
		//int vmIndex = 0;
		//int machinesNum = 0;	
		//Random rand = new Random();
		
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
			
			
			
			
			VirtualMachine vm = null;
			final ComplexDCFJob job = (ComplexDCFJob) j;

			vm = getHandlingVM(job);
			
			
			
			if(vm == null)
			{
				try 
				{
					vm = createNewVM(job);
					vm.subscribeStateChange(this);

					System.out.println("----------------------");

					for(int i = 0; i < orderedPMs.size(); i++)
					{
						System.out.println(orderedPMs.get(i).getCapacities());
					}
				} 
				catch (Exception e) 
				{
					System.err.println(e.getMessage());
					System.out.println("Queueing job.");

					//Collections.sort(orderedPMs, new PMComparator());
					System.out.println("Best PM");

					if(orderedPMs.size() > 0)
					{
						System.out.println(orderedPMs.get(0));
					}
					else
					{
						System.err.println("Error?");
					}
					new DeferredEvent(10000) {
						
						@Override
						protected void eventAction() 
						{
							handleFillerJobRequestArrival(job);
						}

					};

					return;
				}
			}
			else
			{
				//System.out.println("VM found");
				///System.out.println(vm);
				//System.out.println("----------------");
				try 
				{
					job.startNowOnVM(vm, 
							new JobConsumptionEventAdapter(vm, vms, iaas));
				}
				catch (NetworkException e) 
				{
					e.printStackTrace();
				}
			}

			System.out.println("Job started " + (++count));
			System.out.println(job);
			System.out.println("VMs: " + vms.size());
			////System.out.println("-------------------");
		}
		private void handleFillerJobRequestArrival(final Job j)
		{
			for(VirtualMachine vm : vms)
			{
				if(vm.underProcessing.size() < 1)
				{
					try
					{
						vm.newComputeTask(j.getExectimeSecs(), j.nprocs, 
								new ConsumptionEvent()
								{
									@Override
									public void conComplete() 
									{
										j.completed();
									}

									@Override
									public void conCancelled(ResourceConsumption problematic) {  }
								});
						j.started();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
		
		}
		
		private VirtualMachine createNewVM(Job job) throws VMManagementException, NetworkException
		{
				AlterableResourceConstraints rc = new AlterableResourceConstraints(job.nprocs, job.perProcCPUTime, job.usedMemory);
				rc.multiply(2);
				VirtualMachine vm;

				vm = iaas.requestVM((VirtualAppliance) iaas.repositories.get(0).lookup("mainVA"), rc,
						iaas.repositories.get(0), 1)[0];

				vms.add(vm);
				return vm;
		}

		private VirtualMachine getHandlingVM(Job job)
		{
			VirtualMachine handler = null;
			AlterableResourceConstraints rc = new AlterableResourceConstraints(job.nprocs, job.perProcCPUTime, job.usedMemory);
			rc.multiply(2);

			for(VirtualMachine vm : vms)
			{
				if(vm.getResourceAllocation().allocated.compareTo(rc) >= 0
						&& vm.underProcessing.size() == 0
						&& vm.getState() == State.RUNNING)
				{
					handler = vm;
					//System.out.println("VM found");
					//System.out.println(vm);
					//System.out.println("----------------------");
					break;
				}
			}

			return handler;
		}
	
			
			
			
			/*
			ComplexDCFJob Job = (ComplexDCFJob)j;
			Repository repo = this.iaas.repositories.get(0);
			VirtualAppliance va = new VirtualAppliance(Integer.toString(vmIndex), 1, 0);
			
			try{
				repo.registerObject(va);
				this.vms = this.iaas.requestVM(va, this.iaas.machines.get(rand.nextInt(this.machinesNum)).getCapacities(), repo, 1)[0];

				StateChange consumer = new VMStateChange(Job, vms, iaas);
				vms.subscribeStateChange(consumer);

				vmIndex++;
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		*/	
		
		
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
					
						// job allocation based on vm availability
						if (Job.getAvailabilityLevel() == 0.75) {
							array75.add(1);
						}

						if (Job.getAvailabilityLevel() == 0.9) {
							array90.add(1);
						}

						if (Job.getAvailabilityLevel() == 0.95) {
							array95.add(1);
						}

						if (Job.getAvailabilityLevel() == 0.99) {
							array99.add(1);
						}
					}
					//jobCounter assigns jobs in ArrayLists per available vm
					@Override
					public void conCancelled(ResourceConsumption problematic) {
						System.out.println("job has crashed!");

						if (jobCounter < 30) {
							if (Job.getAvailabilityLevel() == 0.75) {
								array75.add(1);
								handleJobRequestArrival(Job);
								runningInCicle = true;
							}

							if (Job.getAvailabilityLevel() == 0.9) {
								array90.add(1);
								handleJobRequestArrival(Job);
								runningInCicle = true;
							}

							if (Job.getAvailabilityLevel() == 0.95) {
								array95.add(1);
								handleJobRequestArrival(Job);
								runningInCicle = true;
							}

							if (Job.getAvailabilityLevel() == 0.99) {
								array95.add(1);
								handleJobRequestArrival(Job);
								runningInCicle = true;
							}
						} else {
							if (Job.getAvailabilityLevel() == 0.75) {
								if (checkAvailability(array75) > 0.75) {
									array75.add(0);
									runningInCicle = false;
								} else {
									handleJobRequestArrival(Job);
									runningInCicle = true;
								}
							}

							if (Job.getAvailabilityLevel() == 0.9) {
								if (checkAvailability(array90) > 0.9) {
									array90.add(0);
									runningInCicle = false;
								} else {
									handleJobRequestArrival(Job);
									runningInCicle = true;
								}
							}

							if (Job.getAvailabilityLevel() == 0.95) {
								if (checkAvailability(array95) > 0.95) {
									array95.add(0);
									runningInCicle = false;
								} else {
									handleJobRequestArrival(Job);
									runningInCicle = true;
								}
							}

							if (Job.getAvailabilityLevel() == 0.99) {
								if (checkAvailability(array95) > 0.97) {
									array99.add(0);
									runningInCicle = false;
								} else {
									handleJobRequestArrival(Job);
									runningInCicle = true;
								}
							}
						}
					
								
				
					}
				});	
					
					
			} catch (Exception e) {
						e.printStackTrace();
						throw new RuntimeException(e);
							
					}
					
		}
				
					
					
		
		
		
	@Override
	public void stateChanged(VirtualMachine vm, State oldState, State newState) {
		// TODO Auto-generated method stub
		if (newState.equals(VirtualMachine.State.RUNNING)) {
			//allocateVMforJob(vm, vmsWithPurpose.remove(vm));
			vm.unsubscribeStateChange(this);
		}
	}

			
			


	@Override
	public void stateChanged(VirtualMachine vm, com.sun.glass.ui.EventLoop.State oldState,
			com.sun.glass.ui.EventLoop.State newState) {
		// TODO Auto-generated method stub
		
	}
			}
		