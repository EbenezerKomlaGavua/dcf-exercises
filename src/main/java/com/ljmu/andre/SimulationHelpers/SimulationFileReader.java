package com.ljmu.andre.SimulationHelpers;

import com.ljmu.andre.SimulationHelpers.Utils.Logger;

import java.lang.reflect.InvocationTargetException;

import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;
import hu.mta.sztaki.lpds.cloud.simulator.helpers.trace.file.TraceFileReaderFoundation;

/**
 * Created by Andre on 28/03/2017.
 */
public class SimulationFileReader extends TraceFileReaderFoundation {
    private static final Logger logger = new Logger(SimulationFileReader.class);
    private static final boolean bypassBreakdowns = true;
    private static final boolean shouldStore = false;
    private String jobName;
    private Job previousJob;

    /**
     * Initializes the generic fields of all line based trace file readers.
     *
     * @param traceKind           Used to mark which kind of trace file parser has derived this
     *                            class. If the trace file is read, in the beginning of each
     *                            reading cycle the name of the trace file parser is reported on
     *                            the standard error.
     * @param fileName            The full path to the file that should act as the source of the
     *                            jobs produced by this trace producer.
     * @param from
     * @param to                  The last job in the file that should be still in the job
     *                            listing output.
     * @param allowReadingFurther If true the previously listed "to" parameter is ignored if the
     *                            "getJobs" function is called on this trace producer.
     * @throws NoSuchMethodException If the class of the jobType does not hold one of the expected
     *                               constructors.
     */
    public SimulationFileReader(String traceKind, String fileName, long from, long to, boolean allowReadingFurther) throws SecurityException, NoSuchMethodException {
        super(traceKind, fileName, (int) from, (int) to, allowReadingFurther, Job.class);
        jobName = traceKind;
    }

    @Override protected boolean isTraceLine(String line) {
        return basicTraceLineDetector("#", line);
    }

    @Override protected void metaDataCollector(String line) {

    }

    @Override protected Job createJobFromLine(String line) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String[] fragments = line.split(",");
        String group = fragments[0];

        if(bypassBreakdowns && group.equalsIgnoreCase("breakdown"))
            return null;

        String source = fragments[1];
        String target = fragments[2];

        long packet = Long.parseLong(fragments[3]);
        long submit = Long.parseLong(fragments[4]);
        //logger.log("Submit: " + submit);

        Job job = new NetworkJob(null, source, target, submit, packet, shouldStore, jobName, group, previousJob);
        previousJob = job;
        return job;
    }
}
