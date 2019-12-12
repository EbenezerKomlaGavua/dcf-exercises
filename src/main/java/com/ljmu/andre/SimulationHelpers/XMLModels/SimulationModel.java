package com.ljmu.andre.SimulationHelpers.XMLModels;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by Andre on 30/03/2017.
 */
@XmlRootElement(name = "Simulation")
@XmlAccessorType(XmlAccessType.FIELD)
public class SimulationModel {

    @XmlElementWrapper(name = "Devices")
    @XmlElement(name="Device")
    public List<DeviceModel> deviceModels;
}
