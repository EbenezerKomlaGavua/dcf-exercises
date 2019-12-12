package com.ljmu.andre.SimulationHelpers;

import com.ljmu.andre.SimulationHelpers.Packets.BasePacket;
import com.ljmu.andre.SimulationHelpers.Packets.DataPacket;
import com.ljmu.andre.SimulationHelpers.Packets.PacketHandler;
import com.ljmu.andre.SimulationHelpers.Packets.RoutingPacket;
import com.ljmu.andre.SimulationHelpers.Utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.JobListAnalyser;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.GenericTraceProducer;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.TraceManagementException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;

/**
 * Created by Andre on 30/03/2017.
 */
public class Device extends Timed implements ConnectionEvent {
    private static final Logger logger = new Logger(Device.class);
    private static final int SUBSCRIBE_FREQ = 100;
    List<Job> networkJobs;
    int currentJobNum = 0;
    private List<ConnectionEvent> connectedDevices = new ArrayList<ConnectionEvent>();
    private String id;
    private PhysicalMachine physicalMachine;
    private Repository repository;
    private GenericTraceProducer traceProducer;
    private String customAttributes;

    Device(String id) {
        this(id, null, null);
    }

    /**
     * Initiate a new Device and claim a PhysicalMachine for it
     *
     * @param id            - The ID of the Device
     * @param traceProducer - An OPTIONAL Trace File Reader which will load a list of jobs to run through
     */
    public Device(String id, GenericTraceProducer traceProducer, String customAttributes) {
        this.id = id;
        this.physicalMachine = MachineHandler.claimPM(id);
        this.repository = physicalMachine.localDisk;
        this.traceProducer = traceProducer;
        this.customAttributes = customAttributes;

        if (this.traceProducer != null) {
            try {
                if (traceProducer instanceof SimulationFileReader)
                    networkJobs = traceProducer.getAllJobs();
                else if (traceProducer instanceof SimulationTraceProducer)
                    networkJobs = ((SimulationTraceProducer) traceProducer).generateJobs();

                Collections.sort(networkJobs, JobListAnalyser.submitTimeComparator);
            } catch (TraceManagementException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Connect a Device
     *
     * @param device - The Device to connect to this one
     * @return True if successfully connected
     */
    public boolean connectDevice(ConnectionEvent device) {
        boolean success = connectedDevices.add(device);
        if (!success)
            logger.log("Device was already connected: " + device.getId());

        return success;
    }

    /**
     * Remove the Device from the connected devices
     *
     * @param device - The Device to be removed
     * @return True if Device was connected
     */
    public boolean disconnectDevice(ConnectionEvent device) {
        boolean success = connectedDevices.remove(device);
        if (!success)
            logger.log("Device was not connected: " + device.getId());

        return success;
    }

    public List<Job> getJobs() {
        return networkJobs;
    }

    public String getCustomAttributes() {
        return customAttributes;
    }

    /**
     * Subscribe this device with a frequency of {@link this#SUBSCRIBE_FREQ}
     */
    public void start() {
        logger.log("Started [ID: %s] [Success: %s]", id, subscribe(SUBSCRIBE_FREQ));
    }

    public PhysicalMachine getPhysicalMachine() {
        return physicalMachine;
    }

    /**
     * Signal a new incoming connection
     * Forwards the signal to the outer class for specialised handling
     *
     * @param source - The Device the connection is forming with
     */
    @Override public void connectionStarted(ConnectionEvent source) {
        logger.log("Starting connection: " + source.getId());

        // Signal the outer class that a new connection is being made \\
        handleConnectionStarted(source);
    }

    /**
     * Signal that a packet has been transferred to this Device
     * If the Packet is a RoutingPacket and is not destined for this device, it is then forwarded
     *
     * @param source          - The Device the Packet has been sent from
     * @param connectionState - The State of the connection
     * @param packet          - The Packet that was sent
     */
    @Override public void connectionFinished(ConnectionEvent source, State connectionState, BasePacket packet) {
        // Check if the packet is a RoutingPacket and if the connection has failed \\
        if (packet instanceof RoutingPacket && connectionState != State.FAILED) {
            RoutingPacket routingPacket = (RoutingPacket) packet;

            // Get the Device this packet should move to next and remove it from the queue \\
            ConnectionEvent target = routingPacket.getRoute().poll();

            // If the target is Null or the TargetID is that of this Device \\
            // Unbox the Payload and recurse this method again with the payload \\
            if (target == null || target.getId().equals(this.getId())) {
                logger.log("Found Destination [Null? %s]", target == null);
                this.connectionFinished(routingPacket.getSource(), connectionState, routingPacket.getPayload());
            } else {
                // Otherwise, forward the packet to the next target \\
                logger.log("Forwarding!");
                PacketHandler.sendPacket(this, target, routingPacket);
            }
        } else {
     ////       Application.packetTransaction(connectionState==State.SUCCESS);

            // If the packet is not a RoutingPacket or the connection FAILED \\
            // Signal the outer class that a full connection cycle has finished \\
            handleConnectionFinished(source, connectionState, packet);
        }
    }

    @Override public Repository getRepository() {
        return repository;
    }

    /**
     * Get a list of all Devices connected to this Device
     *
     * @return The list of connected devices
     */
    @Override public List<ConnectionEvent> getConnectedDevices() {
        return connectedDevices;
    }

    public String getId() {
        return id;
    }

    public void handleConnectionStarted(ConnectionEvent source) {
    }

    @Override public String toString() {
        return "Device{" +
                "networkJobs=" + networkJobs +
                ", currentJobNum=" + currentJobNum +
                ", connectedDevices=" + connectedDevices +
                ", id='" + id + '\'' +
                ", physicalMachine=" + physicalMachine +
                ", repository=" + repository +
                ", traceProducer=" + traceProducer +
                '}';
    }

    @Override public void tick(long fires) {
        // If there are no jobs for this device, stop further execution
        if (networkJobs == null || networkJobs.isEmpty()) {
            stop();
            return;
        }

        // Get the Job that should be processed \\
        NetworkJob currentJob = (NetworkJob) networkJobs.get(currentJobNum);

        // Craft the packet to be sent based on the current job \\
        BasePacket packet = new DataPacket("DeviceData", currentJob.getPacketSize(), false)
                .setShouldStore(currentJob.shouldSave());

        // Send the Job to the intended Target \\
        sendPacket(currentJob.getTarget(), packet);

        logger.log("Job: " + currentJobNum + "/" + networkJobs.size());

        // Increment the Current Job Number and check if there are more jobs to be processed \\
        if (++currentJobNum < networkJobs.size()) {
            // Get the next job, calculate the time difference, and update the frequency to wait until it's ready \\
            Job nextJob = networkJobs.get(currentJobNum);
            long timeDiff = nextJob.getSubmittimeSecs() - Timed.getFireCount();

            logger.log("Next job starts in %sms", timeDiff);
            this.updateFrequency(timeDiff);
        } else
            stop();
    }

    /**
     * Unsubscribe this device
     */
    public void stop() {
        logger.log("Stopped [ID: %s] [Success: %s]", id, unsubscribe());
    }

    /**
     * Attempt to send a packet to an Unresolved Device ID
     *
     * @param targetID - The Unresolved Device ID to send the Packet to
     * @param packet   - The Packet to be sent
     * @return True if connected was established successfully
     */
    boolean sendPacket(String targetID, BasePacket packet) {
        logger.log("Sending to: " + targetID);
        return PacketHandler.sendPacket(this, targetID, packet);
    }

    public void handleConnectionFinished(ConnectionEvent source, State connectionState, BasePacket packet) {
    }
}
