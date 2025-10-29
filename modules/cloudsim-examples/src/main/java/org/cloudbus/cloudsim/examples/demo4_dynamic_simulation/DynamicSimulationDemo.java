/*
 * Demo 4: Dynamic Simulation with Pause/Resume and Dynamic Entity Creation
 *
 * Purpose: Demonstrates advanced CloudSim simulation features including:
 * - Scalable simulation with dynamic entity creation
 * - Pause and resume simulation capabilities
 * - Runtime creation of DatacenterBroker entities
 * - Dynamic cloudlet submission during simulation
 *
 * Key concepts demonstrated:
 * - Dynamic simulation control (pause/resume)
 * - Runtime entity creation and management
 * - Scalable simulation architecture
 * - Event-driven dynamic resource allocation
 * - Time-based simulation control
 */

package org.cloudbus.cloudsim.examples.demo4_dynamic_simulation;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.CloudletSchedulerTimeShared;
import org.cloudbus.cloudsim.Datacenter;
import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudActionTags;
import org.cloudbus.cloudsim.core.SimEntity;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * Custom event tags for dynamic simulation
 */
class DynamicSimTags {
    public static final CloudActionTags CREATE_DYNAMIC_BROKER = CloudActionTags.VM_CREATE;
    public static final CloudActionTags PAUSE_SIMULATION = CloudActionTags.VM_DATACENTER_EVENT;
    public static final CloudActionTags RESUME_SIMULATION = CloudActionTags.VM_BROKER_EVENT;
}

/**
 * Dynamic Simulation Controller
 * This entity manages the dynamic aspects of the simulation including
 * pause/resume functionality and dynamic broker creation
 */
class DynamicSimulationController extends SimEntity {
    private final List<DatacenterBroker> dynamicBrokers;
    private int brokerCounter;
    private boolean simulationPaused;

    public DynamicSimulationController(String name) {
        super(name);
        this.dynamicBrokers = new ArrayList<>();
        this.brokerCounter = 0;
        this.simulationPaused = false;
    }

    @Override
    public void startEntity() {
        Log.println(CloudSim.clock() + ": " + getName() + " is starting...");

        // Send initial dynamic broker creation event
        schedule(getId(), 10.0, DynamicSimTags.CREATE_DYNAMIC_BROKER);

        // Send simulation pause event
        schedule(getId(), 50.0, DynamicSimTags.PAUSE_SIMULATION);

        // Send simulation resume event
        schedule(getId(), 70.0, DynamicSimTags.RESUME_SIMULATION);

        // Send another dynamic broker creation after resume
        schedule(getId(), 80.0, DynamicSimTags.CREATE_DYNAMIC_BROKER);
    }


    @Override
    public void processEvent(SimEvent ev) {
        if (ev.getTag().equals(DynamicSimTags.CREATE_DYNAMIC_BROKER)) {
            createDynamicBroker();
        } else if (ev.getTag().equals(DynamicSimTags.PAUSE_SIMULATION)) {
            pauseSimulation();
        } else if (ev.getTag().equals(DynamicSimTags.RESUME_SIMULATION)) {
            resumeSimulation();
        }
    }

    private void createDynamicBroker() {
        try {
            brokerCounter++;
            String brokerName = "DynamicBroker_" + brokerCounter;
            DatacenterBroker broker = new DatacenterBroker(brokerName);
            dynamicBrokers.add(broker);

            Log.println(CloudSim.clock() + ": Created dynamic broker: " + brokerName + " (ID: " + broker.getId() + ")");

            // Create VMs for the dynamic broker
            List<Vm> vmList = createVMsForDynamicBroker(broker.getId());
            broker.submitGuestList(vmList);

            // Create and submit cloudlets
            List<Cloudlet> cloudletList = createCloudletsForDynamicBroker(broker.getId());
            broker.submitCloudletList(cloudletList);

        } catch (Exception e) {
            Log.println("Error creating dynamic broker: " + e.getMessage());
        }
    }

    private void pauseSimulation() {
        if (!simulationPaused) {
            simulationPaused = true;
            Log.println(CloudSim.clock() + ": *** SIMULATION PAUSED ***");
            Log.println("Simulation paused for analysis and dynamic reconfiguration...");

            // Print current simulation state
            printSimulationState();
        }
    }

    private void resumeSimulation() {
        if (simulationPaused) {
            simulationPaused = false;
            Log.println(CloudSim.clock() + ": *** SIMULATION RESUMED ***");
            Log.println("Continuing simulation with enhanced configuration...");
        }
    }

    private void printSimulationState() {
        Log.println("--- Current Simulation State ---");
        Log.println("Dynamic Brokers Created: " + dynamicBrokers.size());
        Log.println("Current Simulation Time: " + CloudSim.clock());
        Log.println("-------------------------------");
    }

    private List<Vm> createVMsForDynamicBroker(int brokerId) {
        List<Vm> vmList = new ArrayList<>();

        // Enhanced VM parameters for dynamic brokers
        int mips = 1500; // Higher performance
        long size = 15000;
        int ram = 1024;
        long bw = 2000;
        int pesNumber = 2; // Multiple cores
        String vmm = "Xen";

        for (int i = 0; i < 2; i++) {
            Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm,
                          new CloudletSchedulerTimeShared());
            vmList.add(vm);
            Log.println(CloudSim.clock() + ": Created dynamic VM: ID=" + i + " for broker " + brokerId);
        }

        return vmList;
    }

    private List<Cloudlet> createCloudletsForDynamicBroker(int brokerId) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        long baseLength = 300000;
        long fileSize = 400;
        long outputSize = 400;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < 3; i++) {
            long length = baseLength + (i * 100000L);
            Cloudlet cloudlet = new Cloudlet(i, length, 2, fileSize, outputSize,
                                           utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
            Log.println(CloudSim.clock() + ": Created dynamic cloudlet: ID=" + i + " for broker " + brokerId);
        }

        return cloudletList;
    }

    @Override
    public void shutdownEntity() {
        Log.println(getName() + " is shutting down...");

        // Print final results for dynamic brokers
        Log.println("\n========== DYNAMIC BROKERS RESULTS ==========");
        for (DatacenterBroker broker : dynamicBrokers) {
            List<Cloudlet> results = broker.getCloudletReceivedList();
            Log.println("Results for " + broker.getName() + ":");
            printCloudletList(results);
        }
    }

    private void printCloudletList(List<Cloudlet> list) {
        DecimalFormat dft = new DecimalFormat("###.##");
        String indent = "    ";

        for (Cloudlet cloudlet : list) {
            Log.print(indent + cloudlet.getCloudletId() + indent);
            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                Log.println("SUCCESS - Time: " + dft.format(cloudlet.getActualCPUTime()) +
                          ", Datacenter: " + cloudlet.getResourceId() +
                          ", VM: " + cloudlet.getGuestId());
            } else {
                Log.println("FAILED");
            }
        }
    }
}

public class DynamicSimulationDemo {

    public static void main(String[] args) {
        Log.println("========================================");
        Log.println("Starting Dynamic Simulation Demo");
        Log.println("========================================");

        try {
            // Step 1: Initialize CloudSim for scalable simulation
            int numUsers = 10; // Allow for multiple dynamic users
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUsers, calendar, traceFlag);

            // Step 2: Create Scalable Datacenters
            Datacenter datacenter0 = createScalableDatacenter("ScalableDatacenter_0");
            Datacenter datacenter1 = createScalableDatacenter("ScalableDatacenter_1");
            Log.println("Created scalable datacenters: " + datacenter0.getName() + " and " + datacenter1.getName());

            // Step 3: Create Initial Broker
            DatacenterBroker initialBroker = new DatacenterBroker("InitialBroker");
            int initialBrokerId = initialBroker.getId();
            Log.println("Created initial broker: " + initialBroker.getName() + " (ID: " + initialBrokerId + ")");

            // Step 4: Create Initial VMs and Cloudlets
            List<Vm> initialVmList = createInitialVMs(initialBrokerId);
            initialBroker.submitGuestList(initialVmList);

            List<Cloudlet> initialCloudletList = createInitialCloudlets(initialBrokerId);
            initialBroker.submitCloudletList(initialCloudletList);

            // Step 5: Create Dynamic Simulation Controller
            DynamicSimulationController controller = new DynamicSimulationController("DynamicController");
            Log.println("Created dynamic simulation controller: " + controller.getName());

            // Step 6: Start Simulation
            Log.println("Starting dynamic simulation...");
            Log.println("The simulation will demonstrate:");
            Log.println("- Dynamic broker creation at time 10.0");
            Log.println("- Simulation pause at time 50.0");
            Log.println("- Simulation resume at time 70.0");
            Log.println("- Another dynamic broker creation at time 80.0");

            CloudSim.startSimulation();

            // Step 7: Stop simulation and collect results
            CloudSim.stopSimulation();

            List<Cloudlet> initialResults = initialBroker.getCloudletReceivedList();
            Log.println("Dynamic simulation completed. Processing results...");

            // Step 8: Print initial broker results
            Log.println("\n========== INITIAL BROKER RESULTS ==========");
            printCloudletList(initialResults);

            Log.println("========================================");
            Log.println("Dynamic Simulation Demo finished!");
            Log.println("========================================");

        } catch (Exception e) {
            Log.println("The simulation has been terminated due to an unexpected error: " + e.getMessage());
        }
    }

    /**
     * Creates a scalable datacenter with multiple hosts for dynamic workloads
     */
    private static Datacenter createScalableDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();

        // Create multiple hosts for scalability
        for (int hostId = 0; hostId < 3; hostId++) {
            List<Pe> peList = new ArrayList<>();
            int mips = 2000;

            // Multiple PEs per host for parallel processing
            for (int peId = 0; peId < 4; peId++) {
                peList.add(new Pe(peId, new PeProvisionerSimple(mips)));
            }

            int ram = 8192;    // 8GB RAM per host
            long storage = 5000000; // 5TB storage per host
            int bw = 50000;    // 50 Gbps bandwidth

            hostList.add(
                    new Host(
                            hostId,
                            new RamProvisionerSimple(ram),
                            new BwProvisionerSimple(bw),
                            storage,
                            peList,
                            new VmSchedulerTimeShared(peList)
                    )
            );
        }

        // Enhanced datacenter characteristics for scalability
        String arch = "x86_64";
        String os = "Linux";
        String vmm = "KVM";
        double time_zone = 10.0;
        double cost = 2.0;
        double costPerMem = 0.03;
        double costPerStorage = 0.0005;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics,
                                       new VmAllocationPolicySimple(hostList),
                                       storageList, 0);
        } catch (Exception e) {
            Log.println("Error creating datacenter: " + e.getMessage());
        }

        return datacenter;
    }

    /**
     * Creates initial VMs for the simulation
     */
    private static List<Vm> createInitialVMs(int brokerId) {
        List<Vm> vmList = new ArrayList<>();

        int mips = 1000;
        long size = 10000;
        int ram = 512;
        long bw = 1000;
        int pesNumber = 1;
        String vmm = "Xen";

        for (int i = 0; i < 2; i++) {
            Vm vm = new Vm(i, brokerId, mips, pesNumber, ram, bw, size, vmm,
                          new CloudletSchedulerTimeShared());
            vmList.add(vm);
            Log.println("Created initial VM: ID=" + i);
        }

        return vmList;
    }

    /**
     * Creates initial cloudlets for the simulation
     */
    private static List<Cloudlet> createInitialCloudlets(int brokerId) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        long length = 400000;
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < 4; i++) {
            Cloudlet cloudlet = new Cloudlet(i, length, 1, fileSize, outputSize,
                                           utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
            Log.println("Created initial cloudlet: ID=" + i + ", Length=" + length + " MI");
        }

        return cloudletList;
    }

    /**
     * Prints cloudlet execution results
     */
    private static void printCloudletList(List<Cloudlet> list) {
        DecimalFormat dft = new DecimalFormat("###.##");
        String indent = "    ";

        Log.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" +
                   indent + "VM ID" + indent + "Time" + indent + "Start Time" +
                   indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);

            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                Log.print("SUCCESS");
                Log.println(indent + indent + cloudlet.getResourceId() +
                          indent + indent + indent + cloudlet.getGuestId() +
                          indent + indent + dft.format(cloudlet.getActualCPUTime()) +
                          indent + indent + dft.format(cloudlet.getExecStartTime()) +
                          indent + indent + dft.format(cloudlet.getExecFinishTime()));
            } else {
                Log.println("FAILED");
            }
        }
    }
}
