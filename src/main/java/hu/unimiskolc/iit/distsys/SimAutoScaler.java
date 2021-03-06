package hu.unimiskolc.iit.distsys;

import java.util.HashMap;
import java.util.List;

import gnu.trove.list.array.TIntArrayList;
import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.energy.specialized.IaaSEnergyMeter;
import hu.mta.sztaki.lpds.cloud.simulator.examples.jobhistoryprocessor.DCFJob;
import hu.mta.sztaki.lpds.cloud.simulator.examples.util.DCCreation;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.JobListAnalyser;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.FileBasedTraceProducerFactory;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.GenericTraceProducer;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.AlterableResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.SchedulingDependentMachines;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.FirstFitScheduler;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;



public class SimAutoScaler  {
	
	private final IaaSService cloud;
	private IaaSEnergyMeter energymeter;
	private HashMap<PhysicalMachine, Double> preProcessingRecords;
	
	//private  handleJobRequestArrival j;
	//private final GenericTraceProducer trace;
	
	
	//private static List<Job> jobs;
	
	
	
	
	
	
	public SimAutoScaler(int cores, int nodes,String traceFileLoc)throws Exception
			 {
		// Prepares the datacentre
		cloud = DCCreation.createDataCentre(FirstFitScheduler.class, SchedulingDependentMachines.class, nodes, cores);
		// Wait until the PM Controllers finish their initial activities
		Timed.simulateUntilLastEvent();
		
				
		Repository centralStorage = cloud.repositories.get(0);
		long minSize = centralStorage.getMaxStorageCapacity();
		
		VirtualAppliance va = new VirtualAppliance("mainVA", 30, 0, false, minSize / 50);
		centralStorage.registerObject(va);
		//Repository r = cloud.repositories.get(0);
		
		AlterableResourceConstraints totCaps = AlterableResourceConstraints
				.getNoResources();
		double maxNodeProcs = 0;
		for (PhysicalMachine pm : cloud.machines) {
			totCaps.singleAdd(pm.getCapacities());
			maxNodeProcs = Math.max(maxNodeProcs, pm.getCapacities()
					.getRequiredCPUs());
		}
		{			 
			// }	 	 
		// IaaS is prepared

		// Doing preevaluation of the infrastructure
		VirtualMachine test = cloud.requestVM(va, cloud.machines.get(0)
				.getCapacities(), centralStorage, 1)[0];
		long preTime = Timed.getFireCount();
		Timed.simulateUntilLastEvent();
		long pastTime = Timed.getFireCount();
		long vmCreationTime = pastTime - preTime;
		test.destroy(true);
		Timed.simulateUntilLastEvent();
		Timed.resetTimed();
			 }

		
		//Preevaluation completed
	
		
	// Preparing the jobs for the VMs
		
	
		GenericTraceProducer  trace =   (FileBasedTraceProducerFactory.getProducerFromFile(traceFileLoc, 0, 1000000,
				false, nodes * cores, DCFJob.class));
		
				final List<Job>  jobs =  trace.getAllJobs();
				 System.out.println("Number of loaded jobs: " + jobs.size());
				final long lastTermination = JobListAnalyser
				.getLastTerminationTime(jobs) * 1000 * 2;
		// Joblist is ready
		
							
		// Preparing the runtime checks
				final TIntArrayList vmCounts = new TIntArrayList();

				class MyTimed extends Timed {
					public MyTimed() {
						subscribe(5000);
					}

					@Override
					public void tick(long fires) {
						vmCounts.add(cloud.listVMs().size());
						if (lastTermination < fires) {
							unsubscribe();
						}
					}
				}
				{

		
		
		new MyTimed();
		// Runtime checks prepared

		// Preparing the scheduling
		new JobtoVMScheduler(cloud, jobs);

			
						
			// Collecting basic monitoring information
			preProcessingRecords = new HashMap<PhysicalMachine, Double>();
			for (PhysicalMachine pm : cloud.machines) {
				preProcessingRecords.put(pm, pm.getTotalProcessed());
		}
		
			
			// Set up our energy meter for the whole cloud
				energymeter = new IaaSEnergyMeter(cloud);
			// Collects energy related details in every hour
			energymeter.startMeter(3600000, true);
		}
		
}	
	


		public void simulateAndprintStatistics() {
			long before = System.currentTimeMillis();
		      long beforeSimu = Timed.getFireCount();
			// Now we can start the simulation
			Timed.simulateUntilLastEvent();
			System.exit(1);

		
		// Let's print out some basic statistics
				System.out.println("Simulation took: " + (System.currentTimeMillis() - before) + "ms");
				long simuTimespan = Timed.getFireCount() - beforeSimu;
				System.out.println("Simulated timespan: " + simuTimespan + " simulated ms");

				double totutil = 0;
				for (PhysicalMachine pm : cloud.machines) {
					totutil += (pm.getTotalProcessed() - preProcessingRecords.get(pm))
							/ (simuTimespan * pm.getPerTickProcessingPower());
				}
				System.out.println("Average utilisation of PMs: " + 100 * totutil / cloud.machines.size() + " %");
				System.out.println("Total power consumption: " + energymeter.getTotalConsumption() / 1000 / 3600000 + " kWh");
				//System.out.println("Average queue time: " + jobhandler.getAverageQueueTime() + " s");
				System.out.println("Number of virtual appliances registered at the end of the simulation: "
				+ cloud.repositories.get(0).contents().size());
							
			}

//}

	public static void main(String[] args) throws  Exception {
		// TODO Auto-generated method stub
		new SimAutoScaler(Integer.parseInt(args[1]), Integer.parseInt(args[2]), args[0]).simulateAndprintStatistics();
	
	}


	}



