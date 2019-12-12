package com.ljmu.andre.SimulationHelpers.Utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import hu.mta.sztaki.lpds.cloud.simulator.Timed;

/**
 * Created by Andre on 08/02/2017.
 */
public class Logger {
    private static final SimpleDateFormat sdf = new SimpleDateFormat("EEE HH:mm:ss:SSS");
    private Class subscribedClass;

    /**
     * A logger that handles some Time/Tag formatting
     *
     * @param subscribedClass - The Class to bind to this logger
     */
    public Logger(Class subscribedClass) {
        this.subscribedClass = subscribedClass;
    }

    /**
     * Format the message with overloaded params and call {@link this#log(String)}
     *
     * @param message - The message to be printed (Containing formatting)
     * @param params  - The parameters to bind to the messages formatting
     */
    public void log(String message, Object... params) {
        message = String.format(message, params);
        log(message);
    }

    /**
     * Log a message to the console with the current time and class tag
     *
     * @param message - The message to be printed
     */
    public void log(String message) {
        // Format the current time of the simulation \\
        String time = sdf.format(new Date(Timed.getFireCount() - TimeUnit.HOURS.toMillis(1)));

        // Concatinate the time/tag and message \\
        message = String.format("[%s][%s] %s",
                time,
                subscribedClass.getSimpleName(),
                message);

        System.out.println(message);
    }


    /**
     * Format the message with overloaded params and call {@link this#err(String)}
     *
     * @param message - The message to be printed (Containing formatting)
     * @param params  - The parameters to bind to the messages formatting
     */
    public void err(String message, Object... params) {
        message = String.format(message, params);
        err(message);
    }

    /**
     * Log an error message to the console with the current time and class tag
     *
     * @param message - The message to be printed
     */
    public void err(String message) {
        // Format the current time of the simulation \\
        String time = sdf.format(new Date(Timed.getFireCount() - TimeUnit.HOURS.toMillis(1)));

        // Concatinate the time/tag and message \\
        message = String.format("[%s][%s] %s",
                time,
                subscribedClass.getSimpleName(),
                message);

        System.err.println(message);
    }
}
