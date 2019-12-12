package com.ljmu.andre.SimulationHelpers.XMLModels;

import com.ljmu.andre.SimulationHelpers.Utils.Logger;

import java.util.List;
import java.util.Random;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.random.DistributionSpecifier;

/**
 * Created by Andre on 09/04/2017.
 */
public class DistributionModel {
    private static final Logger logger = new Logger(DistributionModel.class);
    @XmlElement(name="Range")
    public List<RangeModel> ranges;

    public DistributionSpecifier generateDistribution() {
        DistributionSpecifier distribution = new DistributionSpecifier(new Random());

        for(RangeModel rangeModel : ranges) {
            logger.log("DistributionRange [Upper: %s] [Lower: %s] [Probability: %s]", rangeModel.upper, rangeModel.lower, rangeModel.probability);
            distribution.addRange(rangeModel.lower, rangeModel.upper, rangeModel.probability);
        }

        distribution.finalizeDistribution();
        return distribution;
    }

    public static class RangeModel {
        @XmlElement(name="Upper")
        public double upper;

        @XmlElement(name="Lower")
        public double lower;

        @XmlElement(name="Probability")
        public double probability;
    }
}
