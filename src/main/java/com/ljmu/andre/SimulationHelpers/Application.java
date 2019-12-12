package com.ljmu.andre.SimulationHelpers;

import com.ljmu.andre.SimulationHelpers.Utils.Logger;
import com.ljmu.andre.SimulationHelpers.XMLModels.DeviceModel;
import com.ljmu.andre.SimulationHelpers.XMLModels.SimulationModel;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.ParserConfigurationException;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;

/**
 * Created by Andre on 30/03/2017.
 */
/**
public class Application {
    public static final String USER_DIR = System.getProperty("user.dir");
    private static final Logger logger = new Logger(Application.class);
    private static int totalPackets = 0;
    private static int successfulPackets = 0;
    private static int failedPackets = 0;
    private static Application instance;
    private boolean isInitialised;
    private List<Device> devices = new ArrayList<Device>();

    private Application() {
    }

    /**
     * Load the Simulation Data such as Machine Data and Device Models
     */
   /** 
public void loadSimData(String machine_xml, String simulation_model_xml) {
        try {
            // Initialise the Machine Handler \\
            MachineHandler.init(machine_xml);

            // Begin unmarshalling of the Simulation XML \\
            JAXBContext jc = JAXBContext.newInstance(SimulationModel.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();

            File file = new File(simulation_model_xml);

            // Convert the XML file to the representing SimulationModel object
            SimulationModel model = (SimulationModel) unmarshaller.unmarshal(file);
            logger.log("Loading %s Devices", model.deviceModels.size());

            // A cache used to quickly access loaded devices \\
            Map<String, Device> deviceMap = new HashMap<String, Device>();

            // Iterate through the Device Models \\
            for (DeviceModel deviceModel : model.deviceModels) {
                // If the DeviceModel ID has already been used, throw an exception \\
                if (deviceMap.containsKey(deviceModel.id))
                    throw new IllegalArgumentException(String.format("Device [ID: %s] already in use!", deviceModel.id));

                // Create the appropriate Device Type based on the DeviceModel type \\
                Device device = deviceModel.generateDevice();

                // Add the device to the system so that it can be connected \\
                devices.add(device);
                deviceMap.put(device.getId(), device);
            }
/**
            // Connect the devices after they're initialised \\
            for (Device device : devices) {
                // Get the Device created from the DeviceModel \\
                for (String connectedID : device.getRepository().getLatencies().keySet()) {
                    Device connectedDevice = deviceMap.get(connectedID);
                    logger.log("Connecting %s to %s", device.getId(), connectedID);

                    if (connectedDevice != null)
                        device.connectDevice(connectedDevice);
                    else {
                        logger.err("Couldn't connect devices [Source: %s] [Target: %s]",
                                device.getId(), connectedID);
                    }
                }
            }

            isInitialised = true;
        } catch (JAXBException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
  }

    public static void packetTransaction(boolean successful) {
        if(successful)
            successfulPackets++;
        else
            failedPackets++;

        totalPackets++;
    }

    /**
     * Start the simulation
     */
/**
    public void startSim() {
        initCheck();
        // Subscribe all the built devices \\
        for (Device device : devices)
            device.start();

        // Begin the simulation until all devices are unsubscribed
        Timed.simulateUntilLastEvent();

        // Print the total number of packets that were sent \\
        logger.log("Simulation Packet Stats [Total: %s] [Successful: %s] [Failed: %s]",
                totalPackets, successfulPackets, failedPackets);
    }

    public void initCheck() {
        if(!isInitialised)
            throw new IllegalStateException("Application is not initialised! Load the Simulation Model first!");
    }
    public static Application getInstance() {
        if (instance == null)
            instance = new Application();

        logger.log("Starting Application");
        return instance;
    }
}
**/