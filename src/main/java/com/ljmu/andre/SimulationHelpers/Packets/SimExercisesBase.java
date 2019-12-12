package com.ljmu.andre.SimulationHelpers.Packets;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomUtils;

import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.AlwaysOnMachines;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.PhysicalMachineController;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.pmscheduling.SchedulingDependentMachines;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.FirstFitScheduler;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.NonQueueingScheduler;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.RandomScheduler;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.RoundRobinScheduler;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.Scheduler;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.vmscheduling.SmallestFirstScheduler;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.util.PowerTransitionGenerator;
import hu.mta.sztaki.lpds.cloud.simulator.util.SeedSyncer;
import hu.unimiskolc.iit.distsys.ExercisesBase;
import hu.unimiskolc.iit.distsys.forwarders.IaaSForwarder;
import hu.unimiskolc.iit.distsys.forwarders.PMForwarder;

public class SimExercisesBase {
	public static final HashMap<String, Integer> latencyMap = new HashMap<String, Integer>();
	public static final int maxCoreCount = 64;
	public static final double minProcessingCap = 4000000; // 4Mflop/ms
	public static final double maxProcessingCap = 5000000; // 5Mflop/ms
	public static final long maxMem = 2 * 10241 * 1024 * 1024; // 2 GB
	public static final long minMem = 1024l * 1024 * 1024; // 1 GB;
	public static final long maxDisk = 40l * 1024 * 1024 * 1024; // 40 GB
	public static final long minDisk = 10l * 1024 * 1024 * 1024; // 10 GB
	public static final int maxOnDelay = 1000; // s
	public static final int maxOffDelay = 1000;
	public static final double maxIdlePower = 400; // W turned on but idle max
	public static final double minIdlePower = 5; // W turned on but idle min
	public static final double maxMaxPower = 1200; // W full utilisation max
	public static final double minMaxPower = 15; // W full utilisation min
	public static final double maxMinPower = 40; // W switched off max
	public static final double minMinPower = 0.2; // W switched off min
	public static final long minPMInBW = 100l * 1024 * 1024 / 8 / 1000; // 100Mbit
	public static final long maxPMInBW = 10l * 1024 * 1024 * 1024 / 8 / 1000; // 10Gbit
	public static final int minLatency = 100;
	public static final int maxLatency = 100;
	@SuppressWarnings("unchecked")
	public static final Class<? extends Scheduler>[] vmSchClasses = new Class[] { FirstFitScheduler.class,
			NonQueueingScheduler.class, RandomScheduler.class, RoundRobinScheduler.class,
			SmallestFirstScheduler.class };
	@SuppressWarnings("unchecked")
	public static final Class<? extends PhysicalMachineController>[] pmContClasses = new Class[] {
			AlwaysOnMachines.class, SchedulingDependentMachines.class };
	private static int nameID = 0;

	private static ArrayList<PMForwarder> pmfs = new ArrayList<PMForwarder>();
	public static List<PMForwarder> pmforwarders = Collections.unmodifiableList(pmfs);

	private static ArrayList<IaaSForwarder> ifs = new ArrayList<IaaSForwarder>();
	public static List<IaaSForwarder> iaasforwarders = Collections.unmodifiableList(ifs);

	/**
	 * Generates a new name for a networked entity and registers it in the network
	 * 
	 * @param prefix
	 * @return
	 */
	public static String genNewName(String prefix) {
		String newName = prefix + "-" + nameID++;
		latencyMap.put(newName, RandomUtils.nextInt(minLatency, maxLatency));
		return newName;
	}

	/**
	 * Constructs a repository with a bandwidth and storage multiplier compared to
	 * the PM related class wide constants defined above
	 * 
	 * @param multiplier
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 */
	public static Repository getNewRepository(final long multiplier)
			throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException {
		return getNewRepository(multiplier, null);
	}

	/**
	 * Constructs a repository with a bandwidth and storage multiplier compared to
	 * the PM related class wide constants defined above
	 * 
	 * @param multiplier
	 * @param tr
	 *            if not null, these will be the transitions to be used instead of a
	 *            newly generated one
	 * @return
	 * @throws NoSuchFieldException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 */
	private static Repository getNewRepository(final long multiplier,
			EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> tr)
			throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException {
		final long networkBW = RandomUtils.nextLong(multiplier * minPMInBW, multiplier * maxPMInBW);
		EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = tr == null
				? genRealisticPowerTransitions()
				: tr;
		return new Repository(RandomUtils.nextLong(multiplier * minDisk, multiplier * maxDisk), genNewName("PM"),
				networkBW, networkBW, networkBW / 2, latencyMap,
				transitions.get(PowerTransitionGenerator.PowerStateKind.storage),
				transitions.get(PowerTransitionGenerator.PowerStateKind.network));
	}

	/**
	 * Generates a random physical machine (and registers it in the network), the
	 * generated PM is going to have resources within the limits of the class wide
	 * constants
	 * 
	 * @return
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static PhysicalMachine getNewPhysicalMachine()
			throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException {
		return getNewPhysicalMachine(1);
	}

	/**
	 * Generates a random physical machine (and registers it in the network), the
	 * generated PM is going to have resources within the limits of the class wide
	 * constants
	 * 
	 * @param reliMult
	 *            Determines the level of reliability the particular Physical
	 *            machine is expected to show.
	 * @return
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws NoSuchFieldException
	 */
	public static PhysicalMachine getNewPhysicalMachine(final double reliMult)
			throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException {
		EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> transitions = genRealisticPowerTransitions();
		PMForwarder f = new PMForwarder((double) RandomUtils.nextInt(1, maxCoreCount),
				RandomUtils.nextDouble(minProcessingCap, maxProcessingCap), RandomUtils.nextLong(minMem, maxMem),
				getNewRepository(1, transitions), SeedSyncer.centralRnd.nextInt(maxOnDelay),
				SeedSyncer.centralRnd.nextInt(maxOffDelay),
				transitions.get(PowerTransitionGenerator.PowerStateKind.host), reliMult);
		pmfs.add(f);
		return f;
	}

	public static EnumMap<PowerTransitionGenerator.PowerStateKind, Map<String, PowerState>> genRealisticPowerTransitions()
			throws SecurityException, InstantiationException, IllegalAccessException, NoSuchFieldException {
		double idlePower = RandomUtils.nextDouble(minIdlePower, maxIdlePower);
		double realMinMaxPower = Math.max(idlePower, minMaxPower);
		return PowerTransitionGenerator.generateTransitions(RandomUtils.nextDouble(minMinPower, maxMinPower), idlePower,
				RandomUtils.nextDouble(realMinMaxPower, maxMaxPower), 30, 40);
	}

	/**
	 * If an automatically generated PM is no longer needed, it can be told to
	 * ExercisesBase via this method.
	 * 
	 * @param pm
	 *            The PM that is not needed anymore.
	 */
	public static void dropPM(PhysicalMachine pm) {
		if (pm instanceof PMForwarder) {
			pmfs.remove((PMForwarder) pm);
			latencyMap.remove(pm.localDisk.getName());
		}
	}

	/**
	 * Creates an IaaS service that uses an arbitrary PM and VM scheduler
	 * 
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	
	public static IaaSService getNewIaaSService() throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		IaaSForwarder f = new IaaSForwarder(vmSchClasses[RandomUtils.nextInt(0, vmSchClasses.length)],
				pmContClasses[RandomUtils.nextInt(0, pmContClasses.length)]);
		ifs.add(f);
		return f;
	}

	/**
	 * Creates a complex IaaS infrastructure with a predefined amount of random
	 * physical machines. The IaaS will use undefined schedulers (randomly chosen
	 * according to getNewIaaSService.
	 * 
	 * @param pmCount
	 * @return
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 * @throws NoSuchFieldException
	 */
	
	public static IaaSService getComplexInfrastructure(final int pmCount)
			throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
		IaaSService iaas = ExercisesBase.getNewIaaSService();
		Repository centralStorage = ExercisesBase.getNewRepository(pmCount);
		iaas.registerRepository(centralStorage);
		ArrayList<PhysicalMachine> pmlist = new ArrayList<PhysicalMachine>();
		long minSize = centralStorage.getMaxStorageCapacity();
		for (int i = 0; i < pmCount; i++) {
			PhysicalMachine curr = ExercisesBase.getNewPhysicalMachine();
			pmlist.add(curr);
			minSize = Math.min(minSize, curr.localDisk.getMaxStorageCapacity());
		}
		iaas.bulkHostRegistration(pmlist);
		VirtualAppliance va = new VirtualAppliance("mainVA", 30, 0, false, minSize / 50);
		centralStorage.registerObject(va);
		return iaas;
	}

	/**
	 * Reinitialise ExercisesBase, this is really useful to avoid memory issues with
	 * the repeated use of the helpers offered by ExercisesBase 
	 */
	public static void reset() {
		latencyMap.clear();
		pmfs.clear();
		ifs.clear();
	}

}
