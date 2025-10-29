/*
 * Demo 5: Global Manager for Runtime Entity Creation
 *
 * Purpose: Demonstrates advanced CloudSim simulation with a Global Manager that:
 * - Creates DatacenterBroker entities dynamically at runtime
 * - Manages multiple brokers through a centralized global manager
 * - Coordinates resource allocation and workload distribution
 * - Provides centralized monitoring and control of all simulation entities
 *
 * Key concepts demonstrated:
 * - Global manager pattern for distributed simulation control
 * - Runtime broker creation and lifecycle management
 * - Centralized resource monitoring and allocation
 * - Coordinated multi-broker simulation architecture
 * - Dynamic workload distribution strategies
 */

package org.cloudbus.cloudsim.examples.demo5_global_manager;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

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
 * Custom event tags for global manager
 */
class GlobalManagerTags {
    public static final CloudActionTags CREATE_BROKER = CloudActionTags.VM_CREATE;
    public static final CloudActionTags MONITOR_RESOURCES = CloudActionTags.RESOURCE_CHARACTERISTICS;
    public static final CloudActionTags REDISTRIBUTE_WORKLOAD = CloudActionTags.RESOURCE_DYNAMICS;
    public static final CloudActionTags ANALYZE_PERFORMANCE = CloudActionTags.RETURN_STAT_LIST;
}

/**
 * Global Manager Entity
 * This entity acts as a centralized controller that manages the lifecycle
 * of DatacenterBroker entities and coordinates the overall simulation
 */
class GlobalBrokerManager extends SimEntity {

    // Managed brokers and their associated resources
    private final Map<Integer, DatacenterBroker> managedBrokers;
    private final Map<Integer, List<Vm>> brokerVMs;
    private final Map<Integer, List<Cloudlet>> brokerCloudlets;

    // Global simulation parameters
    private int nextBrokerId;
    private int globalVmCounter;
    private int globalCloudletCounter;

    // Resource allocation strategies
    private enum AllocationStrategy {
        ROUND_ROBIN, LOAD_BALANCED, PERFORMANCE_OPTIMIZED
    }

    private AllocationStrategy currentStrategy;

    // Performance monitoring
    private final Map<Integer, Double> brokerPerformanceMetrics;

    public GlobalBrokerManager(String name) {
        super(name);
        this.managedBrokers = new HashMap<>();
        this.brokerVMs = new HashMap<>();
        this.brokerCloudlets = new HashMap<>();
        this.brokerPerformanceMetrics = new HashMap<>();
        this.nextBrokerId = 1;
        this.globalVmCounter = 0;
        this.globalCloudletCounter = 0;
        this.currentStrategy = AllocationStrategy.ROUND_ROBIN;
    }

    @Override
    public void startEntity() {
        Log.println(CloudSim.clock() + ": " + getName() + " Global Manager is starting...");
        Log.println(CloudSim.clock() + ": Initializing global broker management system...");

        // Schedule initial broker creation
        schedule(getId(), 5.0, GlobalManagerTags.CREATE_BROKER, "InitialWorkload");

        // Schedule additional brokers at different times
        schedule(getId(), 20.0, GlobalManagerTags.CREATE_BROKER, "PeakWorkload");
        schedule(getId(), 40.0, GlobalManagerTags.CREATE_BROKER, "LateWorkload");

        // Schedule resource monitoring
        schedule(getId(), 30.0, GlobalManagerTags.MONITOR_RESOURCES);
        schedule(getId(), 60.0, GlobalManagerTags.MONITOR_RESOURCES);

        // Schedule workload redistribution
        schedule(getId(), 50.0, GlobalManagerTags.REDISTRIBUTE_WORKLOAD);

        // Schedule final performance analysis
        schedule(getId(), 90.0, GlobalManagerTags.ANALYZE_PERFORMANCE);
    }

    @Override
    public void processEvent(SimEvent ev) {
        if (ev.getTag() == GlobalManagerTags.CREATE_BROKER) {
            String workloadType = (String) ev.getData();
            createManagedBroker(workloadType);
        } else if (ev.getTag() == GlobalManagerTags.MONITOR_RESOURCES) {
            monitorGlobalResources();
        } else if (ev.getTag() == GlobalManagerTags.REDISTRIBUTE_WORKLOAD) {
            redistributeWorkload();
        } else if (ev.getTag() == GlobalManagerTags.ANALYZE_PERFORMANCE) {
            analyzeGlobalPerformance();
        } else {
            Log.println(getName() + ": Unknown event received");
        }
    }

    /**
     * Creates a new DatacenterBroker entity at runtime with specific workload characteristics
     */
    private void createManagedBroker(String workloadType) {
        try {
            String brokerName = "GlobalManagedBroker_" + nextBrokerId + "_" + workloadType;
            DatacenterBroker broker = new DatacenterBroker(brokerName);

            int brokerId = broker.getId();
            managedBrokers.put(brokerId, broker);
            brokerPerformanceMetrics.put(brokerId, 0.0);

            Log.println(CloudSim.clock() + ": Global Manager created broker: " + brokerName +
                       " (ID: " + brokerId + ") for " + workloadType);

            // Create customized VMs based on workload type
            List<Vm> vmList = createCustomizedVMs(brokerId, workloadType);
            brokerVMs.put(brokerId, vmList);
            broker.submitGuestList(vmList);

            // Create customized cloudlets based on workload type
            List<Cloudlet> cloudletList = createCustomizedCloudlets(brokerId, workloadType);
            brokerCloudlets.put(brokerId, cloudletList);
            broker.submitCloudletList(cloudletList);

            nextBrokerId++;

        } catch (Exception e) {
            Log.println("Global Manager: Error creating broker - " + e.getMessage());
        }
    }

    /**
     * Creates VMs with specifications tailored to workload type
     */
    private List<Vm> createCustomizedVMs(int brokerId, String workloadType) {
        List<Vm> vmList = new ArrayList<>();

        // VM specifications vary by workload type
        int mips, ram, numVMs, pesNumber;
        long size, bw;

        switch (workloadType) {
            case "InitialWorkload":
                mips = 1000; ram = 512; numVMs = 2; pesNumber = 1; size = 10000; bw = 1000;
                break;
            case "PeakWorkload":
                mips = 2000; ram = 1024; numVMs = 4; pesNumber = 2; size = 20000; bw = 2000;
                break;
            case "LateWorkload":
                mips = 1500; ram = 768; numVMs = 3; pesNumber = 1; size = 15000; bw = 1500;
                break;
            default:
                mips = 1000; ram = 512; numVMs = 2; pesNumber = 1; size = 10000; bw = 1000;
        }

        String vmm = "Xen";

        for (int i = 0; i < numVMs; i++) {
            Vm vm = new Vm(globalVmCounter++, brokerId, mips, pesNumber, ram, bw, size, vmm,
                          new CloudletSchedulerTimeShared());
            vmList.add(vm);
            Log.println(CloudSim.clock() + ": Created customized VM for " + workloadType +
                       " - ID: " + vm.getId() + ", MIPS: " + mips + ", RAM: " + ram + "MB");
        }

        return vmList;
    }

    /**
     * Creates cloudlets with characteristics tailored to workload type
     */
    private List<Cloudlet> createCustomizedCloudlets(int brokerId, String workloadType) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        // Cloudlet specifications vary by workload type
        long baseLength, fileSize, outputSize;
        int numCloudlets, pesRequired;

        switch (workloadType) {
            case "InitialWorkload":
                baseLength = 300000; numCloudlets = 3; pesRequired = 1; fileSize = 300; outputSize = 300;
                break;
            case "PeakWorkload":
                baseLength = 600000; numCloudlets = 6; pesRequired = 2; fileSize = 500; outputSize = 500;
                break;
            case "LateWorkload":
                baseLength = 450000; numCloudlets = 4; pesRequired = 1; fileSize = 400; outputSize = 400;
                break;
            default:
                baseLength = 300000; numCloudlets = 3; pesRequired = 1; fileSize = 300; outputSize = 300;
        }

        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < numCloudlets; i++) {
            long length = baseLength + (i * 100000L);
            Cloudlet cloudlet = new Cloudlet(globalCloudletCounter++, length, pesRequired,
                                           fileSize, outputSize, utilizationModel,
                                           utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
            Log.println(CloudSim.clock() + ": Created customized cloudlet for " + workloadType +
                       " - ID: " + cloudlet.getCloudletId() + ", Length: " + length + " MI");
        }

        return cloudletList;
    }

    /**
     * Monitors global resource utilization across all managed brokers
     */
    private void monitorGlobalResources() {
        Log.println(CloudSim.clock() + ": === GLOBAL RESOURCE MONITORING ===");
        Log.println("Active Brokers: " + managedBrokers.size());

        int totalVMs = 0;
        int totalCloudlets = 0;

        for (Map.Entry<Integer, List<Vm>> entry : brokerVMs.entrySet()) {
            int brokerId = entry.getKey();
            List<Vm> vms = entry.getValue();
            List<Cloudlet> cloudlets = brokerCloudlets.get(brokerId);

            totalVMs += vms.size();
            totalCloudlets += cloudlets.size();

            Log.println("Broker " + brokerId + ": " + vms.size() + " VMs, " + cloudlets.size() + " Cloudlets");
        }

        Log.println("Total Global Resources: " + totalVMs + " VMs, " + totalCloudlets + " Cloudlets");
        Log.println("======================================");
    }

    /**
     * Redistributes workload based on current allocation strategy
     */
    private void redistributeWorkload() {
        Log.println(CloudSim.clock() + ": === WORKLOAD REDISTRIBUTION ===");
        Log.println("Current Strategy: " + currentStrategy);

        // Switch to a different strategy for demonstration
        switch (currentStrategy) {
            case ROUND_ROBIN:
                currentStrategy = AllocationStrategy.LOAD_BALANCED;
                break;
            case LOAD_BALANCED:
                currentStrategy = AllocationStrategy.PERFORMANCE_OPTIMIZED;
                break;
            case PERFORMANCE_OPTIMIZED:
                currentStrategy = AllocationStrategy.ROUND_ROBIN;
                break;
        }

        Log.println("Switched to: " + currentStrategy);
        Log.println("Redistributing workload among " + managedBrokers.size() + " brokers...");

        // In a real implementation, this would involve migrating VMs or reassigning cloudlets
        // For demonstration, we just log the strategy change
        for (Integer brokerId : managedBrokers.keySet()) {
            Log.println("Applying " + currentStrategy + " strategy to Broker " + brokerId);
        }

        Log.println("=====================================");
    }

    /**
     * Performs comprehensive performance analysis of all managed brokers
     */
    private void analyzeGlobalPerformance() {
        Log.println(CloudSim.clock() + ": === GLOBAL PERFORMANCE ANALYSIS ===");

        double totalExecutionTime = 0.0;
        int totalSuccessfulCloudlets = 0;
        int totalFailedCloudlets = 0;

        for (Map.Entry<Integer, DatacenterBroker> entry : managedBrokers.entrySet()) {
            int brokerId = entry.getKey();
            DatacenterBroker broker = entry.getValue();

            List<Cloudlet> completedCloudlets = broker.getCloudletReceivedList();

            double brokerExecutionTime = 0.0;
            int brokerSuccessful = 0;
            int brokerFailed = 0;

            for (Cloudlet cloudlet : completedCloudlets) {
                if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                    brokerExecutionTime += cloudlet.getActualCPUTime();
                    brokerSuccessful++;
                    totalSuccessfulCloudlets++;
                } else {
                    brokerFailed++;
                    totalFailedCloudlets++;
                }
            }

            totalExecutionTime += brokerExecutionTime;
            brokerPerformanceMetrics.put(brokerId, brokerExecutionTime);

            Log.println("Broker " + brokerId + " Performance:");
            Log.println("  - Successful Cloudlets: " + brokerSuccessful);
            Log.println("  - Failed Cloudlets: " + brokerFailed);
            Log.println("  - Total Execution Time: " + String.format("%.2f", brokerExecutionTime) + " seconds");
        }

        // Calculate global metrics
        double avgExecutionTime = totalExecutionTime / Math.max(managedBrokers.size(), 1);
        double successRate = (double) totalSuccessfulCloudlets /
                           Math.max(totalSuccessfulCloudlets + totalFailedCloudlets, 1) * 100;

        Log.println("\nGlobal Performance Summary:");
        Log.println("  - Total Managed Brokers: " + managedBrokers.size());
        Log.println("  - Total Successful Cloudlets: " + totalSuccessfulCloudlets);
        Log.println("  - Total Failed Cloudlets: " + totalFailedCloudlets);
        Log.println("  - Global Success Rate: " + String.format("%.2f", successRate) + "%");
        Log.println("  - Average Execution Time per Broker: " + String.format("%.2f", avgExecutionTime) + " seconds");
        Log.println("  - Total Global Execution Time: " + String.format("%.2f", totalExecutionTime) + " seconds");

        Log.println("==========================================");
    }

    @Override
    public void shutdownEntity() {
        Log.println(getName() + " Global Manager is shutting down...");

        // Print final summary of all managed brokers
        Log.println("\n========== FINAL GLOBAL SUMMARY ==========");
        Log.println("Total Brokers Managed: " + managedBrokers.size());

        for (Map.Entry<Integer, DatacenterBroker> entry : managedBrokers.entrySet()) {
            int brokerId = entry.getKey();
            DatacenterBroker broker = entry.getValue();
            List<Cloudlet> results = broker.getCloudletReceivedList();

            Log.println("\nBroker " + brokerId + " (" + broker.getName() + ") Final Results:");
            printCloudletResults(results);
        }

        Log.println("==========================================");
    }

    private void printCloudletResults(List<Cloudlet> list) {
        DecimalFormat dft = new DecimalFormat("###.##");

        for (Cloudlet cloudlet : list) {
            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                Log.println("  Cloudlet " + cloudlet.getCloudletId() + ": SUCCESS - Time: " +
                          dft.format(cloudlet.getActualCPUTime()) + "s, Datacenter: " +
                          cloudlet.getResourceId() + ", VM: " + cloudlet.getGuestId());
            } else {
                Log.println("  Cloudlet " + cloudlet.getCloudletId() + ": FAILED");
            }
        }
    }
}

public class GlobalManagerDemo {

    public static void main(String[] args) {
        Log.println("========================================");
        Log.println("Starting Global Manager Demo");
        Log.println("========================================");

        try {
            // Step 1: Initialize CloudSim for large-scale simulation
            int numUsers = 20; // Allow for many dynamic brokers
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUsers, calendar, traceFlag);

            // Step 2: Create High-Capacity Datacenters
            Datacenter datacenter0 = createHighCapacityDatacenter("GlobalDatacenter_0");
            Datacenter datacenter1 = createHighCapacityDatacenter("GlobalDatacenter_1");
            Datacenter datacenter2 = createHighCapacityDatacenter("GlobalDatacenter_2");

            Log.println("Created high-capacity datacenters:");
            Log.println("  - " + datacenter0.getName());
            Log.println("  - " + datacenter1.getName());
            Log.println("  - " + datacenter2.getName());

            // Step 3: Create Global Broker Manager
            GlobalBrokerManager globalManager = new GlobalBrokerManager("GlobalBrokerManager");
            Log.println("Created Global Broker Manager: " + globalManager.getName());

            // Step 4: Start Simulation
            Log.println("\nStarting global manager simulation...");
            Log.println("The Global Manager will:");
            Log.println("- Create brokers dynamically at times 5.0, 20.0, and 40.0");
            Log.println("- Monitor resources at times 30.0 and 60.0");
            Log.println("- Redistribute workload at time 50.0");
            Log.println("- Analyze performance at time 90.0");

            CloudSim.startSimulation();

            // Step 5: Stop simulation
            CloudSim.stopSimulation();

            Log.println("\n========================================");
            Log.println("Global Manager Demo completed successfully!");
            Log.println("All brokers were managed by the Global Manager.");
            Log.println("Check the output above for detailed execution results.");
            Log.println("========================================");

        } catch (Exception e) {
            Log.println("The simulation has been terminated due to an unexpected error: " + e.getMessage());
        }
    }

    /**
     * Creates a high-capacity datacenter for global management scenarios
     */
    private static Datacenter createHighCapacityDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();

        // Create multiple high-performance hosts
        for (int hostId = 0; hostId < 5; hostId++) {
            List<Pe> peList = new ArrayList<>();
            int mips = 3000; // High-performance processors

            // Multiple PEs per host for maximum parallel processing
            for (int peId = 0; peId < 8; peId++) {
                peList.add(new Pe(peId, new PeProvisionerSimple(mips)));
            }

            int ram = 16384;   // 16GB RAM per host
            long storage = 10000000; // 10TB storage per host
            int bw = 100000;   // 100 Gbps bandwidth

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

        // Enterprise-grade datacenter characteristics
        String arch = "x86_64";
        String os = "Linux";
        String vmm = "KVM";
        double time_zone = 10.0;
        double cost = 1.5;
        double costPerMem = 0.02;
        double costPerStorage = 0.0003;
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
}
