package com.ljmu.andre.SimulationHelpers;

import hu.mta.sztaki.lpds.cloud.simulator.helpers.job.Job;

/**
 * Created by Andre on 29/03/2017.
 */
public class NetworkJob extends Job {
    private long packetSize;
    private String source;
    private String target;
    private boolean shouldSave;

    /**
     * The generic constructor to be used by most of the trace generators and in
     * most of the use cases.
     * <p>
     * Please note the id of the job is automatically generated with this
     * constructor.
     *
     * @param id        the id of the job, if it is null, then the id will be
     *                  internally assigned.
     * @param submit    the time instance (in secs) the job is submitted
     * @param user      the user who submitted the job
     * @param group
     * @param preceding
     */
    public NetworkJob(String id, String source, String target, long submit, long packetSize, boolean shouldSave, String user, String group, Job preceding) {
        super(id, submit, 0, 0, 0, 0, 0, user, group, null, preceding, 0);
        this.packetSize = packetSize;
        this.source = source;
        this.target = target;
        this.shouldSave = shouldSave;
    }


    public long getPacketSize() {
        return packetSize;
    }

    public String getSource() {
        return source;
    }

    public String getTarget() {
        return target;
    }

    public boolean shouldSave() {
        return shouldSave;
    }

    @Override public void started() {

    }

    @Override public void completed() {

    }
}
