package com.ljmu.andre.SimulationHelpers.Packets;

import java.util.HashMap;
import java.util.Map;

import hu.mta.sztaki.lpds.cloud.simulator.energy.powermodelling.PowerState;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.VirtualMachine;
//import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine.ResourceAllocation;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.constraints.ResourceConstraints;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;
import hu.mta.sztaki.lpds.cloud.simulator.io.VirtualAppliance;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
//import hu.unimiskolc.iit.distsys.forwarders.ForwardingRecorder;
//import hu.unimiskolc.iit.distsys.forwarders.ForwardingRecorder;

public class PMForwarder extends PhysicalMachine implements ForwardingRecorder {
	private boolean reqVMCalled = false;
	private boolean allocVMCalled = false;
	private boolean deployVMCalled = false;
	private final double reliMult;
	private final double maxConsumption;

	/**
	 * 
	 * @param cores
	 * @param perCorePocessing
	 * @param memory
	 * @param disk
	 * @param onD
	 * @param offD
	 * @param powerTransitions
	 * @param reliMult
	 *            The higher the value the less reliable the machine is
	 */
	public PMForwarder(double cores, double perCorePocessing, long memory, Repository disk, int onD, int offD,
			Map<String, PowerState> powerTransitions, double reliMult) {
		super(cores, perCorePocessing, memory, disk, onD, offD, powerTransitions);
		this.reliMult = reliMult;
		PowerState maxConsumingState = powerTransitions.get(PhysicalMachine.State.RUNNING.toString());
		maxConsumption = maxConsumingState.getConsumptionRange() + maxConsumingState.getMinConsumption();
	}

	public void resetForwardingData() {
		reqVMCalled = true;
		allocVMCalled = false;
		deployVMCalled = false;
	}

	public boolean isReqVMCalled() {
		return reqVMCalled;
	}

	public boolean isDeployVMCalled() {
		return deployVMCalled;
	}

	public boolean isAllocVMCalled() {
		return allocVMCalled;
	}

	public double getReliMult() {
		return reliMult;
	}

	public double getMaxConsumption() {
		return maxConsumption;
	}

	@Override
	public VirtualMachine[] requestVM(VirtualAppliance va, ResourceConstraints rc, Repository vaSource, int count)
			throws hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException, NetworkException {
		reqVMCalled = true;
		return super.requestVM(va, rc, vaSource, count);
	}

	@Override
	public VirtualMachine[] requestVM(VirtualAppliance va, ResourceConstraints rc, Repository vaSource, int count,
			HashMap<String, Object> schedulingConstraints)
			throws hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException, NetworkException {
		reqVMCalled = true;
		return super.requestVM(va, rc, vaSource, count, schedulingConstraints);
	}

	@Override
	public void deployVM(VirtualMachine vm, ResourceAllocation ra, Repository vaSource)
			throws hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException, NetworkException {
		deployVMCalled = true;
		super.deployVM(vm, ra, vaSource);
	}

	@Override
	public ResourceAllocation allocateResources(ResourceConstraints requested, boolean strict,
			int allocationValidityLength)
			throws hu.mta.sztaki.lpds.cloud.simulator.iaas.VMManager.VMManagementException {
		allocVMCalled = true;
		return super.allocateResources(requested, strict, allocationValidityLength);
	}

	@Override
	public String toString() {
		return "(PMForwarder: reli=" + reliMult + " totPower=" + maxConsumption + " " + super.toString() + ")";
	}
}
