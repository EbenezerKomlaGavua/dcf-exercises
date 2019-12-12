package com.ljmu.andre.SimulationHelpers;

import com.ljmu.andre.SimulationHelpers.Utils.Logger;
import com.sun.istack.internal.NotNull;

import org.xml.sax.SAXException;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.IaaSService.IaaSHandlingException;
import hu.mta.sztaki.lpds.cloud.simulator.iaas.PhysicalMachine;
import hu.mta.sztaki.lpds.cloud.simulator.util.CloudLoader;

/**
 * Created by Andre on 29/03/2017.
 */
public class MachineHandler {
    private static final Logger logger = new Logger(MachineHandler.class);
    private static IaaSService iaaSService;
    private static boolean isInitialized = false;

    public static void init(@NotNull String MACHINE_XML_PATH) throws ParserConfigurationException, SAXException, IOException {
        iaaSService = CloudLoader.loadNodes(MACHINE_XML_PATH);
        isInitialized = true;
    }

    /**
     * Returns the PM with the Name provided and removes it from the {@link MachineHandler#iaaSService}
     *
     * @param pmName - The name of the machine - Case Insensitive
     * @return PhysicalMachine if found / Null if not found
     */
    @NotNull public static PhysicalMachine claimPM(String pmName) {
        initCheck();
        PhysicalMachine pm = findPM(pmName);

        try {
            iaaSService.deregisterHost(pm);
        } catch (IaaSHandlingException e) {
            e.printStackTrace();
        }

        return pm;
    }

    /**
     * Check if the MachineHandler has been initialised
     * otherwise throws an exception
     */
    private static void initCheck() {
        if (!isInitialized)
            throw new IllegalStateException("Cloud is not initialized!");
    }

    /**
     * Returns the PM with the Name provided
     *
     * @param pmName - The name of the machine - Case Insensitive
     * @return PhysicalMachine if found / Null if not found
     */
    @NotNull private static PhysicalMachine findPM(String pmName) {
        initCheck();
        pmName = pmName.toLowerCase();

        for (PhysicalMachine physicalMachine : iaaSService.machines) {
            logger.log("Checking PM: " + physicalMachine.localDisk.getName());
            if (physicalMachine.localDisk.getName().toLowerCase().equals(pmName))
                return physicalMachine;
        }

        throw new IllegalArgumentException(String.format("No Physical Machine Found [Name: %s]", pmName));
    }
}
