package com.ljmu.andre.SimulationHelpers.Packets;

import com.ljmu.andre.SimulationHelpers.Utils.Logger;

public class Main {
	
	private static final Logger logger = new Logger(Main.class);
    public static Scenario scenario;
        public static void main(String[] args) {

        try {
            scenario = new Scenario();
        } catch (Throwable e) {
            e.printStackTrace();
        }

    }

}
