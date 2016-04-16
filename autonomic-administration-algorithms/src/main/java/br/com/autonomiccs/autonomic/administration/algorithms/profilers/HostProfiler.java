package br.com.autonomiccs.autonomic.administration.algorithms.profilers;

import java.util.List;

import br.com.autonomiccs.autonomic.administration.algorithms.pojos.ClusterVmProfile;
import br.com.autonomiccs.autonomic.administration.algorithms.pojos.HostProfile;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.HostResources;
import br.com.autonomiccs.autonomic.algorithms.commons.resources.VmResources;

/**
 * This class calculates Hosts and VMs profiles from a cluster, those profiles are used by
 * algorithms from LRG; thus, this class is not necessary for every algorithm. It creates host
 * {@link HostProfile} and cluster profiles {@link ClusterVmProfile}.
 */
public class HostProfiler {

    protected final static int BYTES_TO_MEGA_BYTES = 1000000;
    private List<HostResources> hosts;

    public HostProfiler(List<HostResources> hosts) {
        this.hosts = hosts;
    }

    /**
     * It divides each host resource (number of CPUs, CPU frequency and memory) by cluster's VMs
     * profile for the same resource. Gives a proportion of VMs this host can support.
     *
     * @param host
     * @param vmsProfile
     * @return {@link HostProfile}
     */
    public HostProfile hostVMsResourceProportion(HostResources host) {
        ClusterVmProfile clusterVmsProfile = createClusterVmsProfile(getHostsVmsResources());
        HostProfile hostProfile = new HostProfile();

        hostProfile.setCpusProfile(host.getCpus() / clusterVmsProfile.getCpusProfile());
        hostProfile.setCpuSpeedProfile((host.getSpeed() * host.getCpuOverprovisioning()) / clusterVmsProfile.getCpuSpeedProfile());
        hostProfile.setMemoryProfile(((host.getTotalMemoryInBytes() / BYTES_TO_MEGA_BYTES) * host.getMemoryOverprovisioning()) / clusterVmsProfile.getMemoryProfile());

        return hostProfile;
    }

    /**
     * It calculates the average cpu and memory usage of VMs.
     *
     * @param vmsProfile
     * @return {@link ClusterVmProfile}
     */
    public ClusterVmProfile createClusterVmsProfile(ClusterVmProfile vmsProfile) {
        vmsProfile.setCpusProfile(vmsProfile.getTotalCpus() / vmsProfile.getNumberOfInstances());
        vmsProfile.setCpuSpeedProfile(vmsProfile.getTotalCpuSpeed() / vmsProfile.getNumberOfInstances());
        vmsProfile.setMemoryProfile(vmsProfile.getTotalMemory() / vmsProfile.getNumberOfInstances());

        return vmsProfile;
    }

    /**
     * It calculates the sum of VMs resources from the list of hosts. It creates a
     * {@link ClusterVmProfile} summing the {@link VmResources}. It contains the number of VMs,
     * number of cpus, the cpu speed and memory size.
     *
     * @return {@link ClusterVmProfile}
     */
    public ClusterVmProfile getHostsVmsResources() {
        ClusterVmProfile vmsProfile = new ClusterVmProfile();
        for(HostResources host : hosts) {
            List<VmResources> vmsOnHost = host.getVmsResources();
            for(VmResources vmResources : vmsOnHost) {
                vmsProfile.setNumberOfInstances(vmsProfile.getNumberOfInstances() + 1);
                vmsProfile.setTotalCpus(vmsProfile.getTotalCpus() + vmResources.getNumberOfCpus());
                vmsProfile.setTotalCpuSpeed((int) (vmsProfile.getTotalCpuSpeed() + vmResources.getCpuSpeed()));
                vmsProfile.setTotalMemory((int) (vmsProfile.getTotalMemory() + vmResources.getMemoryInMegaBytes()));
            }
        }
        return vmsProfile;
    }

}