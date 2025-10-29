/*
 * CloudSim Application: Runtime Entity Creation with Global Manager
 *
 * Purpose: Demonstrates dynamic creation of simulation entities (DatacenterBroker)
 * at runtime using a global manager entity (GlobalBroker).
 *
 * This application showcases:
 * - Global manager pattern for centralized control
 * - Runtime creation of DatacenterBroker entities
 * - Dynamic allocation of VMs and Cloudlets to brokers
 * - Coordinated resource management across multiple brokers
 * - Event-driven broker lifecycle management
 *
 * Architecture:
 * GlobalBroker (Manager) -> Creates -> Multiple DatacenterBrokers -> Submit -> VMs & Cloudlets -> Datacenter
 */

package org.cloudbus.cloudsim.examples;

import org.cloudbus.cloudsim.core.*;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.provisioners.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Main demonstration class for runtime entity creation using a global manager
 */
public class TestScheduledResource {

    // Global simulation components
    private static Datacenter datacenter;
    private static GlobalBroker globalManager;
    private static List<Vm> vmList;
    private static List<Cloudlet> cloudletList;

    /**
     * Main entry point for the simulation
     * Sets up CloudSim environment and initiates the global manager
     */
    public static void main(String[] args) {
        Log.println("========================================");
        Log.println("Starting Global Manager Simulation");
        Log.println("Runtime DatacenterBroker Creation Demo");
        Log.println("========================================\n");

        try {
            // Step 1: Initialize CloudSim environment
            // num_user: Number of cloud users
            // calendar: Calendar instance for simulation timing
            // trace_flag: Enable/disable trace logging
            int num_user = 1;
            Calendar calendar = Calendar.getInstance();
            boolean trace_flag = false; // Disable detailed trace for cleaner output

            Log.println("Step 1: Initializing CloudSim environment...");
            CloudSim.init(num_user, calendar, trace_flag);

            // Step 2: Create the Datacenter that will host VMs
            Log.println("Step 2: Creating Datacenter infrastructure...");
            datacenter = createDatacenter("Datacenter_0");

            // Step 3: Create the Global Manager entity
            // This entity will create DatacenterBrokers dynamically at runtime
            Log.println("Step 3: Creating Global Manager (GlobalBroker)...");
            globalManager = new GlobalBroker("GlobalBrokerManager");

            // Step 4: Start the simulation
            Log.println("Step 4: Starting simulation...\n");
            CloudSim.startSimulation();

            // Step 5: Stop simulation and print results
            CloudSim.stopSimulation();

            Log.println("\n========================================");
            Log.println("Simulation completed successfully!");
            Log.println("========================================");

            // Print final statistics from all brokers created by global manager
            printGlobalStatistics();

        } catch (Exception e) {
            Log.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a Datacenter with specified characteristics
     * The datacenter hosts physical machines (hosts) that run VMs
     *
     * @param name Name identifier for the datacenter
     * @return Configured Datacenter object
     */
    private static Datacenter createDatacenter(String name) {
        // Create a list of host machines for the datacenter
        List<Host> hostList = new ArrayList<>();

        // Create Processing Elements (PEs/CPU cores) for hosts
        // Each PE represents a CPU core with specified MIPS rating
        List<Pe> peList = new ArrayList<>();

        int mips = 2000; // MIPS (Million Instructions Per Second) rating per core

        // Create 4 CPU cores for each host
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));
        peList.add(new Pe(1, new PeProvisionerSimple(mips)));
        peList.add(new Pe(2, new PeProvisionerSimple(mips)));
        peList.add(new Pe(3, new PeProvisionerSimple(mips)));

        // Host specifications
        int hostId = 0;
        int ram = 8192; // 8 GB RAM
        long storage = 1000000; // 1 TB storage
        int bw = 10000; // 10 Gbps bandwidth

        // Create multiple hosts (physical machines) for the datacenter
        // Using 3 hosts to allow distribution of VMs across multiple machines
        for (int i = 0; i < 3; i++) {
            hostList.add(
                new Host(
                    hostId++,
                    new RamProvisionerSimple(ram),
                    new BwProvisionerSimple(bw),
                    storage,
                    peList,
                    new VmSchedulerTimeShared(peList) // Time-shared VM scheduling
                )
            );
        }

        // Datacenter characteristics
        String arch = "x86";           // System architecture
        String os = "Linux";            // Operating system
        String vmm = "Xen";             // Virtual Machine Monitor
        double time_zone = 10.0;        // Time zone offset
        double cost = 3.0;              // Cost per second of using processing
        double costPerMem = 0.05;       // Cost per MB of RAM
        double costPerStorage = 0.001;  // Cost per MB of storage
        double costPerBw = 0.0;         // Cost per byte of bandwidth

        // Storage Area Network (SAN) configuration
        LinkedList<Storage> storageList = new LinkedList<>();

        // Create datacenter characteristics object
        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
            arch, os, vmm, hostList, time_zone, cost, costPerMem, costPerStorage, costPerBw
        );

        // Create the Datacenter object
        Datacenter dc = null;
        try {
            dc = new Datacenter(
                name,
                characteristics,
                new VmAllocationPolicySimple(hostList), // Simple VM allocation policy
                storageList,
                0 // Scheduling interval
            );
            Log.println("Datacenter created: " + name + " with " + hostList.size() + " hosts");
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dc;
    }

    /**
     * Prints global statistics from all brokers managed by the global manager
     */
    private static void printGlobalStatistics() {
        Log.println("\n========================================");
        Log.println("GLOBAL SIMULATION STATISTICS");
        Log.println("========================================");

        if (globalManager != null) {
            globalManager.printFinalStatistics();
        }
    }
}

/**
 * GlobalBroker: A global manager entity that creates and manages DatacenterBroker entities at runtime
 *
 * This entity demonstrates the global manager pattern where:
 * - The GlobalBroker acts as a centralized controller
 * - It dynamically creates DatacenterBroker entities during simulation execution
 * - Each broker receives customized VMs and Cloudlets
 * - The manager coordinates resource allocation across all brokers
 */
class GlobalBroker extends SimEntity {

    // Collection of all brokers created and managed by this global manager
    private final Map<Integer, DatacenterBroker> managedBrokers;

    // VMs allocated to each broker (broker ID -> VM list)
    private final Map<Integer, List<Vm>> brokerVMMap;

    // Cloudlets assigned to each broker (broker ID -> Cloudlet list)
    private final Map<Integer, List<Cloudlet>> brokerCloudletMap;

    // Counters for generating unique IDs
    private int brokerCounter;
    private int vmIdCounter;
    private int cloudletIdCounter;

    /**
     * Constructor for GlobalBroker
     * @param name Name identifier for this global manager entity
     */
    public GlobalBroker(String name) {
        super(name);
        this.managedBrokers = new HashMap<>();
        this.brokerVMMap = new HashMap<>();
        this.brokerCloudletMap = new HashMap<>();
        this.brokerCounter = 0;
        this.vmIdCounter = 0;
        this.cloudletIdCounter = 0;
    }

    /**
     * Called when the entity starts
     * This method schedules the creation of multiple brokers at different simulation times
     */
    @Override
    public void startEntity() {
        Log.println(CloudSim.clock() + ": " + getName() + " is starting...");
        Log.println(CloudSim.clock() + ": Global Manager initializing broker creation schedule\n");

        // Schedule creation of first broker immediately (at time 0.1)
        schedule(getId(), 0.1, CloudActionTags.VM_CREATE, "LightWorkload");

        // Schedule creation of second broker at time 10.0
        schedule(getId(), 10.0, CloudActionTags.VM_CREATE, "MediumWorkload");

        // Schedule creation of third broker at time 20.0
        schedule(getId(), 20.0, CloudActionTags.VM_CREATE, "HeavyWorkload");

        // Schedule resource monitoring at time 30.0
        schedule(getId(), 30.0, CloudActionTags.RESOURCE_CHARACTERISTICS);

        // Schedule final analysis at time 50.0
        schedule(getId(), 50.0, CloudActionTags.RETURN_STAT_LIST);
    }

    /**
     * Processes incoming events for this entity
     * Handles different types of events including broker creation, monitoring, and analysis
     *
     * @param ev The simulation event to process
     */
    @Override
    public void processEvent(SimEvent ev) {
        // Check event type using equals() for enum comparison
        if (ev.getTag().equals(CloudActionTags.VM_CREATE)) {
            // Event to create a new DatacenterBroker at runtime
            String workloadType = (String) ev.getData();
            createBrokerAtRuntime(workloadType);

        } else if (ev.getTag().equals(CloudActionTags.RESOURCE_CHARACTERISTICS)) {
            // Event to monitor resource utilization
            monitorResources();

        } else if (ev.getTag().equals(CloudActionTags.RETURN_STAT_LIST)) {
            // Event to analyze performance statistics
            analyzePerformance();

        } else {
            Log.println(CloudSim.clock() + ": " + getName() + " received unknown event");
        }
    }

    /**
     * Creates a new DatacenterBroker entity at runtime with customized resources
     * This is the core functionality demonstrating runtime entity creation
     *
     * @param workloadType Type of workload to configure (Light/Medium/Heavy)
     */
    private void createBrokerAtRuntime(String workloadType) {
        try {
            // Increment broker counter and create unique name
            brokerCounter++;
            String brokerName = "RuntimeBroker_" + brokerCounter + "_" + workloadType;

            Log.println("\n" + CloudSim.clock() + ": ========================================");
            Log.println(CloudSim.clock() + ": Global Manager creating broker at RUNTIME");
            Log.println(CloudSim.clock() + ": Broker Name: " + brokerName);
            Log.println(CloudSim.clock() + ": Workload Type: " + workloadType);

            // Create the DatacenterBroker entity dynamically
            DatacenterBroker broker = new DatacenterBroker(brokerName);
            int brokerId = broker.getId();

            // Store reference to the newly created broker
            managedBrokers.put(brokerId, broker);

            Log.println(CloudSim.clock() + ": Broker created with ID: " + brokerId);

            // Create and assign VMs to this broker based on workload type
            List<Vm> vms = createVMsForBroker(brokerId, workloadType);
            brokerVMMap.put(brokerId, vms);
            broker.submitGuestList(vms); // Submit VMs to broker

            Log.println(CloudSim.clock() + ": Assigned " + vms.size() + " VMs to broker");

            // Create and assign Cloudlets to this broker based on workload type
            List<Cloudlet> cloudlets = createCloudletsForBroker(brokerId, workloadType);
            brokerCloudletMap.put(brokerId, cloudlets);
            broker.submitCloudletList(cloudlets); // Submit cloudlets to broker

            Log.println(CloudSim.clock() + ": Assigned " + cloudlets.size() + " Cloudlets to broker");
            Log.println(CloudSim.clock() + ": ========================================\n");

        } catch (Exception e) {
            Log.println("ERROR: Failed to create broker at runtime - " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates customized VMs based on workload type
     * Different workloads require different VM configurations
     *
     * @param brokerId ID of the broker that will own these VMs
     * @param workloadType Type of workload (Light/Medium/Heavy)
     * @return List of configured VM objects
     */
    private List<Vm> createVMsForBroker(int brokerId, String workloadType) {
        List<Vm> vmList = new ArrayList<>();

        // VM specifications vary by workload type
        int mips, ram, numVMs, pesNumber;
        long size, bw;

        // Configure VM parameters based on workload intensity
        switch (workloadType) {
            case "LightWorkload":
                mips = 1000;      // 1 GHz processing speed
                ram = 512;        // 512 MB RAM
                numVMs = 2;       // 2 VMs
                pesNumber = 1;    // Single CPU core
                size = 10000;     // 10 GB disk
                bw = 1000;        // 1 Gbps bandwidth
                break;

            case "MediumWorkload":
                mips = 1500;      // 1.5 GHz processing speed
                ram = 1024;       // 1 GB RAM
                numVMs = 3;       // 3 VMs
                pesNumber = 2;    // Dual CPU cores
                size = 20000;     // 20 GB disk
                bw = 2000;        // 2 Gbps bandwidth
                break;

            case "HeavyWorkload":
                mips = 2000;      // 2 GHz processing speed
                ram = 2048;       // 2 GB RAM
                numVMs = 4;       // 4 VMs
                pesNumber = 2;    // Dual CPU cores
                size = 30000;     // 30 GB disk
                bw = 3000;        // 3 Gbps bandwidth
                break;

            default:
                mips = 1000; ram = 512; numVMs = 2; pesNumber = 1; size = 10000; bw = 1000;
        }

        String vmm = "Xen"; // Virtual Machine Monitor type

        // Create the specified number of VMs with calculated specifications
        for (int i = 0; i < numVMs; i++) {
            Vm vm = new Vm(
                vmIdCounter++,                          // Unique VM ID
                brokerId,                               // Owner broker ID
                mips,                                   // Processing speed
                pesNumber,                              // Number of CPU cores
                ram,                                    // RAM amount
                bw,                                     // Bandwidth
                size,                                   // Storage size
                vmm,                                    // VM Monitor
                new CloudletSchedulerTimeShared()      // Cloudlet scheduler
            );
            vmList.add(vm);

            Log.println(CloudSim.clock() + ":   - Created VM #" + vm.getId() +
                       " (MIPS: " + mips + ", RAM: " + ram + " MB, PEs: " + pesNumber + ")");
        }

        return vmList;
    }

    /**
     * Creates customized Cloudlets (tasks) based on workload type
     * Different workloads have different computational requirements
     *
     * @param brokerId ID of the broker that will own these cloudlets
     * @param workloadType Type of workload (Light/Medium/Heavy)
     * @return List of configured Cloudlet objects
     */
    private List<Cloudlet> createCloudletsForBroker(int brokerId, String workloadType) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        // Cloudlet specifications vary by workload type
        long baseLength, fileSize, outputSize;
        int numCloudlets, pesRequired;

        // Configure cloudlet parameters based on workload intensity
        switch (workloadType) {
            case "LightWorkload":
                baseLength = 250000;    // 250K MI (Million Instructions)
                numCloudlets = 3;       // 3 tasks
                pesRequired = 1;        // Single core required
                fileSize = 300;         // 300 bytes input file
                outputSize = 300;       // 300 bytes output file
                break;

            case "MediumWorkload":
                baseLength = 500000;    // 500K MI
                numCloudlets = 5;       // 5 tasks
                pesRequired = 2;        // Dual core required
                fileSize = 500;         // 500 bytes input file
                outputSize = 500;       // 500 bytes output file
                break;

            case "HeavyWorkload":
                baseLength = 1000000;   // 1M MI
                numCloudlets = 7;       // 7 tasks
                pesRequired = 2;        // Dual core required
                fileSize = 800;         // 800 bytes input file
                outputSize = 800;       // 800 bytes output file
                break;

            default:
                baseLength = 250000; numCloudlets = 3; pesRequired = 1; fileSize = 300; outputSize = 300;
        }

        // Utilization models define how cloudlets use resources
        UtilizationModel utilizationModel = new UtilizationModelFull(); // 100% utilization

        // Create the specified number of cloudlets with calculated specifications
        for (int i = 0; i < numCloudlets; i++) {
            long length = baseLength + (i * 50000L); // Vary length slightly for each cloudlet

            Cloudlet cloudlet = new Cloudlet(
                cloudletIdCounter++,    // Unique cloudlet ID
                length,                 // Computational length in MI
                pesRequired,            // Number of CPU cores needed
                fileSize,               // Input file size
                outputSize,             // Output file size
                utilizationModel,       // CPU utilization model
                utilizationModel,       // RAM utilization model
                utilizationModel        // Bandwidth utilization model
            );

            cloudlet.setUserId(brokerId); // Set owner broker
            cloudletList.add(cloudlet);

            Log.println(CloudSim.clock() + ":   - Created Cloudlet #" + cloudlet.getCloudletId() +
                       " (Length: " + length + " MI, PEs: " + pesRequired + ")");
        }

        return cloudletList;
    }

    /**
     * Monitors current resource utilization across all managed brokers
     * Provides visibility into the global state of the simulation
     */
    private void monitorResources() {
        Log.println("\n" + CloudSim.clock() + ": ========================================");
        Log.println(CloudSim.clock() + ": RESOURCE MONITORING REPORT");
        Log.println(CloudSim.clock() + ": ========================================");
        Log.println(CloudSim.clock() + ": Total Brokers Created: " + managedBrokers.size());

        int totalVMs = 0;
        int totalCloudlets = 0;

        // Iterate through all managed brokers and sum up resources
        for (Map.Entry<Integer, DatacenterBroker> entry : managedBrokers.entrySet()) {
            int brokerId = entry.getKey();
            DatacenterBroker broker = entry.getValue();
            List<Vm> vms = brokerVMMap.get(brokerId);
            List<Cloudlet> cloudlets = brokerCloudletMap.get(brokerId);

            totalVMs += vms.size();
            totalCloudlets += cloudlets.size();

            Log.println(CloudSim.clock() + ": - " + broker.getName() +
                       ": " + vms.size() + " VMs, " + cloudlets.size() + " Cloudlets");
        }

        Log.println(CloudSim.clock() + ": ----------------------------------------");
        Log.println(CloudSim.clock() + ": Total Resources: " + totalVMs + " VMs, " +
                   totalCloudlets + " Cloudlets");
        Log.println(CloudSim.clock() + ": ========================================\n");
    }

    /**
     * Analyzes performance metrics across all managed brokers
     * Provides insights into execution efficiency and resource utilization
     */
    private void analyzePerformance() {
        Log.println("\n" + CloudSim.clock() + ": ========================================");
        Log.println(CloudSim.clock() + ": PERFORMANCE ANALYSIS");
        Log.println(CloudSim.clock() + ": ========================================");
        Log.println(CloudSim.clock() + ": Analyzing performance across " +
                   managedBrokers.size() + " brokers...");

        // In a real implementation, this would calculate actual performance metrics
        // such as average execution time, resource utilization, cost, etc.

        for (Map.Entry<Integer, DatacenterBroker> entry : managedBrokers.entrySet()) {
            int brokerId = entry.getKey();
            DatacenterBroker broker = entry.getValue();

            Log.println(CloudSim.clock() + ": - " + broker.getName() +
                       " Status: Running/Completed");
        }

        Log.println(CloudSim.clock() + ": ========================================\n");
    }

    /**
     * Prints final statistics from all managed brokers
     * Called at the end of simulation to display results
     */
    public void printFinalStatistics() {
        DecimalFormat dft = new DecimalFormat("###.##");

        Log.println("Total Brokers Created by Global Manager: " + managedBrokers.size());
        Log.println("Total VMs Allocated: " + vmIdCounter);
        Log.println("Total Cloudlets Executed: " + cloudletIdCounter);
        Log.println("\nDetailed Results per Broker:");
        Log.println("========================================");

        // Print statistics for each broker
        for (Map.Entry<Integer, DatacenterBroker> entry : managedBrokers.entrySet()) {
            int brokerId = entry.getKey();
            DatacenterBroker broker = entry.getValue();
            List<Cloudlet> cloudlets = broker.getCloudletReceivedList();

            Log.println("\n" + broker.getName() + " (ID: " + brokerId + "):");
            Log.println("----------------------------------------");

            if (cloudlets.size() > 0) {
                // Print cloudlet execution details
                for (Cloudlet cloudlet : cloudlets) {
                    Log.print("  Cloudlet ID: " + cloudlet.getCloudletId());
                    Log.print(" | Status: " + getCloudletStatus(cloudlet.getStatus()));

                    if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                        Log.print(" | VM ID: " + cloudlet.getGuestId());
                        Log.print(" | Exec Time: " + dft.format(cloudlet.getActualCPUTime()));
                        Log.print(" | Start: " + dft.format(cloudlet.getExecStartTime()));
                        Log.println(" | Finish: " + dft.format(cloudlet.getFinishTime()));
                    } else {
                        Log.println();
                    }
                }
            } else {
                Log.println("  No cloudlets completed yet.");
            }
        }
    }

    /**
     * Converts cloudlet status enum to readable string
     * @param status Cloudlet status enum
     * @return Human-readable status string
     */
    private String getCloudletStatus(Cloudlet.CloudletStatus status) {
        switch (status) {
            case SUCCESS: return "SUCCESS";
            case FAILED: return "FAILED";
            case CANCELED: return "CANCELED";
            case PAUSED: return "PAUSED";
            case RESUMED: return "RESUMED";
            case CREATED: return "CREATED";
            default: return "UNKNOWN";
        }
    }

    /**
     * Called when the entity is shutting down
     * Cleanup operations can be performed here
     */
    @Override
    public void shutdownEntity() {
        Log.println(CloudSim.clock() + ": " + getName() + " is shutting down...");
    }
}

