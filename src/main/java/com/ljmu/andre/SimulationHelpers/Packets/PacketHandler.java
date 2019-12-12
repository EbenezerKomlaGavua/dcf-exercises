package com.ljmu.andre.SimulationHelpers.Packets;

import com.ljmu.andre.SimulationHelpers.ConnectionEvent;
import com.ljmu.andre.SimulationHelpers.ConnectionEvent.State;
import com.ljmu.andre.SimulationHelpers.Utils.Logger;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.resourcemodel.ResourceConsumption.ConsumptionEvent;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode;
import hu.mta.sztaki.lpds.cloud.simulator.io.NetworkNode.NetworkException;
import hu.mta.sztaki.lpds.cloud.simulator.io.Repository;

/**
 * Created by Andre on 08/02/2017.
 */
public class PacketHandler {
    private static final Logger logger = new Logger(PacketHandler.class);

    //TODO Change limit to something a little more elegant
    private static final int limit = 10;

    /*public static ArrayList<BasePacket> sendPackets(final ConnectionEvent source, final ConnectionEvent target,
                                                    final ArrayList<BasePacket> packetList) {
        logger.log("Attempting to send: " + packetList.size() + " packets");
        ArrayList<BasePacket> failedPackets = new ArrayList<BasePacket>();

        for (BasePacket packet : packetList) {
            logger.log("Sending packet: " + packet.id);

            sendPacket(source, target, packet);
        }

        if (failedPackets.size() > 0)
            logger.log("Failed packets: " + failedPackets.size());

        return failedPackets;
    }*/

    /**
     * Send a packet to an unresolved device if directly connected or attempts to find a route
     * through connected nodes and boxes the packet in a {@link RoutingPacket}
     *
     * @param source   - The Device responsible for initiating the connection
     * @param targetID - The Unresolved Device ID that should receive the packet
     * @param packet   - The packet to be sent to the target
     * @return - True if Route is found and the connection is initiated successfully
     */
    public static boolean sendPacket(final ConnectionEvent source, final String targetID,
                                     final BasePacket packet) {
        // Check if the device is directly connected to the source \\
        for (ConnectionEvent device : source.getConnectedDevices()) {
            logger.log("Checking Device: " + device.getId());

            // If directly connected, send the packet \\
            if (device.getId().equals(targetID))
                return PacketHandler.sendPacket(source, device, packet);
        }

        // Attempt to find a route between the connected devices
        Queue<ConnectionEvent> route = getConnectionRoute(source, targetID);

        // If the route could not be found, return false and alert the Source \\
        if (route == null || route.isEmpty()) {
            logger.log("Couldn't find route between [Source:%s][Target:%s]",
                    source.getId(),
                    targetID);

            source.connectionFinished(source, State.FAILED, packet);
            return false;
        }

        // Check if the first node in the Route is the Source - If so, remove it
        if (route.peek().equals(source))
            route.remove();

        logger.log("PathSize: " + route.size());

        // Box the packet in a RoutingPacket, attaching the Route Queue to it \\
        RoutingPacket routingPacket = new RoutingPacket("Routing", packet, source, route);

        // Determine the next target and if it exists, forward the routing packet to it
        ConnectionEvent nextTarget = routingPacket.getRoute().poll();
        return nextTarget != null &&
                sendPacket(routingPacket.getSource(), nextTarget, routingPacket);
    }

    /**
     * Attempts to send a Packet from the Source to a Target Device
     * If the {@link BasePacket#getShouldStore()} returns true,
     * {@link Repository#requestContentDelivery(String, String, Repository, ConsumptionEvent)} will be used
     * otherwise, {@link NetworkNode#initTransfer(long, double, NetworkNode, NetworkNode, ConsumptionEvent)} will be used
     *
     * @param source - The Device responsible for initiating the connection
     * @param target - The Resolved Device that should receive the packet
     * @param packet - The packet to be sent to the target
     * @return - True if Route is found and the connection is initiated successfully
     */
    public static boolean sendPacket(final ConnectionEvent source, final ConnectionEvent target, BasePacket packet) {
        try {
            logger.log("Sending [From: %s][To: %s]", source.getId(), target.getId());

            // Check the connection between the Source and the Target - Return False if issues \\
            if (NetworkNode.checkConnectivity(source.getRepository(), target.getRepository()) < 0)
                return false;

            // If the packet is a RoutingPacket and the route is empty, unbox the payload \\
            if (packet instanceof RoutingPacket &&
                    ((RoutingPacket) packet).getRoute().isEmpty()) {
                logger.log("Route is empty, assuming destination achieved... Unboxing");
                packet = ((RoutingPacket) packet).getPayload();
            }

            // Signal the target that an incoming connection is being made \\
            target.connectionStarted(source);

            // Build an appropriate consumption event \\
            ConsumptionEvent consumptionEvent = getConsumptionEvent(source, target, packet);

            // Determine whether the packet should be saved to disk \\
            if (packet.getShouldStore()) {
                // Register the packet to the source \\
                if(!registerPacketIfNotExist(source, packet)) {
                    logger.err("No more drive space left on [Device: %s]", source.getId());

                    target.connectionFinished(source, State.FAILED, packet);
                    return false;
                }

                // Attempt to send and save the packet on the target \\

                boolean hasDelivered = source.getRepository()
                        .requestContentDelivery(
                                packet.id,
                                target.getRepository(),
                                consumptionEvent) != null;

                if(!hasDelivered) {
                    target.connectionFinished(source, State.FAILED, packet);
                    logger.err("Could not deliver packet [Source: %s] [Target: %s]",
                            source.getId(), target.getId());
                }

                return hasDelivered;
            } else {
                // Attempt to transfer the packet accross the network \\
                NetworkNode.initTransfer(
                        packet.size,
                        limit,
                        source.getRepository(),
                        target.getRepository(),
                        consumptionEvent);

                // If the initTransfer passed, a connection was able to be established \\
                return true;
            }
        } catch (NetworkException e) {
            logger.err("Failed to send packet [ID: %s]", packet.id);
            e.printStackTrace();

            // Check if the packet was registered before we tried to send it
            boolean packetIsRegistered = source.getRepository().lookup(packet.id) != null;

            // If it was not already registered, we can assume it was not intended to stay
            // Register the object so that we can grab it to send again later
            // Force the packet to deregister from the source once successfully transferred
            if (!packetIsRegistered &&
                    source.getRepository().registerObject(packet))
                packet.addDeregisterObject(source);

        }

        // Alert the source that the packet failed
        source.connectionFinished(source, State.FAILED, packet);
        return false;
    }

    /**
     * Attempt to find a Route between a Source device and an Unresolved Device ID
     * This function uses a Depth First Search - Not the most efficient but it works
     *
     * @param source   - The Device to start the search from
     * @param targetID - The Unresolved Device ID to find a route to
     * @return A Queue for the Route between two Devices or Null if no Route could be found
     */
    private static Queue<ConnectionEvent> getConnectionRoute(ConnectionEvent source, String targetID) {
        Stack<ConnectionEvent> route = new Stack<ConnectionEvent>();
        Set<String> visited = new HashSet<String>();

        // Add the source to the stack and mark as visited \\
        route.push(source);
        visited.add(source.getId());

        // Iterate through the stack until empty \\
        while (!route.isEmpty()) {
            // Get the current node to be advanced from \\
            ConnectionEvent node = route.peek();
            // Using the current node, check if there are any unvisited branches \\
            ConnectionEvent child = getUnvisited(node, visited);

            // If the current node has any unvisited branches, add it to the stack to be processed next \\
            // Otherwise, pop the stack and check the previous node for more branches \\
            if (child != null) {
                // Mark the child as visited so that it's not checked again \\
                visited.add(child.getId());
                route.push(child);
                logger.log("Route Node: " + child.getId());

                // If the target is reached, return the Stack as a Queue \\
                if (child.getId().equals(targetID))
                    return new LinkedList<ConnectionEvent>(route);
            }
        }

        // Return Null if no Route could be established \\
        return null;
    }

    /**
     * Build a consumption event so that the appropriate callbacks can be made on completion
     *
     * @param source - The Device that the connection is initiated from
     * @param target - The Device that the connection is destined
     * @param packet - The Packet that is being sent
     * @return A ConsumptionEvent with the appropriate callback triggers
     */
    private static ConsumptionEvent getConsumptionEvent(final ConnectionEvent source,
                                                        final ConnectionEvent target,
                                                        final BasePacket packet) {
        return new ConsumptionEvent() {
            @Override public void conComplete() {
                logger.log("Packet[" + packet.id + "] successfully sent");

                //source.connectionFinished(source, State.SUCCESS, packet);
                target.connectionFinished(source, State.SUCCESS, packet);

                // Packets that are not intended to stay after transfer are deregistered here
                // This allows for failed packets to be wiped
                if (packet.shouldDeregister(source))
                    source.getRepository().deregisterObject(packet);
                if (packet.shouldDeregister(target))
                    target.getRepository().deregisterObject(packet);
            }

            @Override public void conCancelled(ResourceConsumption problematic) {
                source.connectionFinished(source, State.FAILED, packet);
                logger.log("Cancelled: " + problematic.toString());
            }
        };
    }

    /**
     * Check if the Device already contains the packet, otherwise register it
     *
     * @param source - The Device to Check/Register the Packet with
     * @param packet - The Packet to Check/Register
     */
    private static boolean registerPacketIfNotExist(ConnectionEvent source, BasePacket packet) {
        if (source.getRepository().lookup(packet.id) == null) {
            logger.log("Registering packet: " + packet);
            return source.getRepository().registerObject(packet);
        }

        return true;
    }

    /**
     * Check a Node for any unvisited children
     *
     * @param node    - The Device to get the Connections from
     * @param visited - The Set of all visited Nodes
     * @return Unvisited Device or Null if none are found
     */
    private static ConnectionEvent getUnvisited(ConnectionEvent node, Set<String> visited) {
        logger.log("Checking [Node: %s] for unvisited children [Potential: %s]",
                node.getId(),
                node.getConnectedDevices().size());


        // Iterate through all of the node's children \\
        for (ConnectionEvent child : node.getConnectedDevices()) {

            // If the Set does not contain the child, it has not been visited \\
            if (!visited.contains(child.getId())) {
                logger.log("Found unvisited child [ID: %s]", child.getId());
                return child;
            }
        }

        logger.log("No unvisited children");
        return null;
    }
}
