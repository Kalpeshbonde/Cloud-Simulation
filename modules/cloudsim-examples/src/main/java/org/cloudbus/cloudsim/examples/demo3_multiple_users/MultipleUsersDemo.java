/*
 * Demo 3: Multiple Users with Two Datacenters
 *
 * Purpose: Demonstrates CloudSim simulation with multiple users (brokers) featuring:
 * - Two datacenters, each with one host
 * - Two separate users/brokers competing for resources
 * - Multiple cloudlets from different users
 * - Resource allocation and scheduling across multiple users
 *
 * Key concepts demonstrated:
 * - Multi-tenant cloud environment simulation
 * - Resource competition between different users
 * - Independent broker management and cloudlet submission
 * - Fair resource allocation policies
 */

package org.cloudbus.cloudsim.examples.demo3_multiple_users;

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
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

public class MultipleUsersDemo {

    public static void main(String[] args) {
        Log.println("========================================");
        Log.println("Starting Multiple Users Demo");
        Log.println("========================================");

        try {
            // Step 1: Initialize CloudSim with multiple users
            int numUsers = 2; // Two users/brokers
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUsers, calendar, traceFlag);

            // Step 2: Create Two Datacenters
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            Datacenter datacenter1 = createDatacenter("Datacenter_1");
            Log.println("Created datacenters: " + datacenter0.getName() + " and " + datacenter1.getName());

            // Step 3: Create Two Brokers (representing different users)
            DatacenterBroker broker1 = new DatacenterBroker("User1_Broker");
            DatacenterBroker broker2 = new DatacenterBroker("User2_Broker");

            int broker1Id = broker1.getId();
            int broker2Id = broker2.getId();

            Log.println("Created User 1 Broker: " + broker1.getName() + " (ID: " + broker1Id + ")");
            Log.println("Created User 2 Broker: " + broker2.getName() + " (ID: " + broker2Id + ")");

            // Step 4: Create VMs for User 1
            List<Vm> vmListUser1 = createVMsForUser(broker1Id, "User1", 2);
            broker1.submitGuestList(vmListUser1);

            // Step 5: Create VMs for User 2
            List<Vm> vmListUser2 = createVMsForUser(broker2Id, "User2", 2);
            broker2.submitGuestList(vmListUser2);

            // Step 6: Create Cloudlets for User 1
            List<Cloudlet> cloudletListUser1 = createCloudletsForUser(broker1Id, "User1", 3);
            broker1.submitCloudletList(cloudletListUser1);

            // Step 7: Create Cloudlets for User 2
            List<Cloudlet> cloudletListUser2 = createCloudletsForUser(broker2Id, "User2", 3);
            broker2.submitCloudletList(cloudletListUser2);

            // Step 8: Start Simulation
            Log.println("Starting multi-user simulation...");
            CloudSim.startSimulation();

            // Step 9: Stop simulation and collect results
            CloudSim.stopSimulation();

            List<Cloudlet> resultListUser1 = broker1.getCloudletReceivedList();
            List<Cloudlet> resultListUser2 = broker2.getCloudletReceivedList();

            Log.println("Simulation completed. Processing results...");

            // Step 10: Print results for both users
            Log.println("\n========== USER 1 RESULTS ==========");
            printCloudletList(resultListUser1, "User1");

            Log.println("\n========== USER 2 RESULTS ==========");
            printCloudletList(resultListUser2, "User2");

            // Step 11: Print summary statistics
            printSummaryStatistics(resultListUser1, resultListUser2);

            Log.println("========================================");
            Log.println("Multiple Users Demo finished!");
            Log.println("========================================");

        } catch (Exception e) {
            e.printStackTrace();
            Log.println("The simulation has been terminated due to an unexpected error");
        }
    }

    /**
     * Creates VMs for a specific user
     */
    private static List<Vm> createVMsForUser(int userId, String userName, int numVMs) {
        List<Vm> vmList = new ArrayList<>();

        // VM parameters
        int mips = 1000;
        long size = 10000; // image size (MB)
        int ram = 512;     // VM memory (MB)
        long bw = 1000;    // bandwidth
        int pesNumber = 1; // number of CPU cores
        String vmm = "Xen";

        for (int i = 0; i < numVMs; i++) {
            Vm vm = new Vm(i, userId, mips, pesNumber, ram, bw, size, vmm,
                          new CloudletSchedulerTimeShared());
            vmList.add(vm);
            Log.println("Created VM for " + userName + ": ID=" + i + ", MIPS=" + mips);
        }

        return vmList;
    }

    /**
     * Creates Cloudlets for a specific user
     */
    private static List<Cloudlet> createCloudletsForUser(int userId, String userName, int numCloudlets) {
        List<Cloudlet> cloudletList = new ArrayList<>();

        // Cloudlet parameters (different for each user to show variety)
        long baseLength = 400000; // base cloudlet length in MI
        long fileSize = 300;
        long outputSize = 300;
        UtilizationModel utilizationModel = new UtilizationModelFull();

        for (int i = 0; i < numCloudlets; i++) {
            // Vary cloudlet length for different users
            long length = baseLength + (userId * 100000) + (i * 50000);

            Cloudlet cloudlet = new Cloudlet(i, length, 1, fileSize, outputSize,
                                           utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(userId);
            cloudletList.add(cloudlet);
            Log.println("Created Cloudlet for " + userName + ": ID=" + i + ", Length=" + length + " MI");
        }

        return cloudletList;
    }

    /**
     * Creates a datacenter with enhanced capacity for multiple users
     */
    private static Datacenter createDatacenter(String name) {
        // Create host list
        List<Host> hostList = new ArrayList<>();

        // Create PE list with more processing power
        List<Pe> peList = new ArrayList<>();
        int mips = 2000; // Higher MIPS for multiple users

        // Add multiple PEs per host to handle multiple VMs
        for (int i = 0; i < 4; i++) {
            peList.add(new Pe(i, new PeProvisionerSimple(mips)));
        }

        // Create host with enhanced capacity
        int hostId = 0;
        int ram = 4096;    // More RAM for multiple VMs
        long storage = 2000000; // More storage
        int bw = 20000;    // Higher bandwidth

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

        // Create datacenter characteristics
        String arch = "x86";
        String os = "Linux";
        String vmm = "Xen";
        double time_zone = 10.0;
        double cost = 3.0;
        double costPerMem = 0.05;
        double costPerStorage = 0.001;
        double costPerBw = 0.0;
        LinkedList<Storage> storageList = new LinkedList<>();

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                arch, os, vmm, hostList, time_zone, cost, costPerMem,
                costPerStorage, costPerBw);

        // Create datacenter
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics,
                                       new VmAllocationPolicySimple(hostList),
                                       storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    /**
     * Prints cloudlet execution results for a specific user
     */
    private static void printCloudletList(List<Cloudlet> list, String userName) {
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

    /**
     * Prints summary statistics comparing both users
     */
    private static void printSummaryStatistics(List<Cloudlet> user1Results, List<Cloudlet> user2Results) {
        DecimalFormat dft = new DecimalFormat("###.##");

        Log.println();
        Log.println("========== SUMMARY STATISTICS ==========");

        // Calculate statistics for User 1
        double user1TotalTime = 0;
        double user1AvgTime = 0;
        int user1SuccessCount = 0;

        for (Cloudlet cloudlet : user1Results) {
            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                user1TotalTime += cloudlet.getActualCPUTime();
                user1SuccessCount++;
            }
        }
        if (user1SuccessCount > 0) {
            user1AvgTime = user1TotalTime / user1SuccessCount;
        }

        // Calculate statistics for User 2
        double user2TotalTime = 0;
        double user2AvgTime = 0;
        int user2SuccessCount = 0;

        for (Cloudlet cloudlet : user2Results) {
            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                user2TotalTime += cloudlet.getActualCPUTime();
                user2SuccessCount++;
            }
        }
        if (user2SuccessCount > 0) {
            user2AvgTime = user2TotalTime / user2SuccessCount;
        }

        Log.println("User 1 Statistics:");
        Log.println("  - Successful Cloudlets: " + user1SuccessCount + "/" + user1Results.size());
        Log.println("  - Total Execution Time: " + dft.format(user1TotalTime) + " seconds");
        Log.println("  - Average Execution Time: " + dft.format(user1AvgTime) + " seconds");

        Log.println("User 2 Statistics:");
        Log.println("  - Successful Cloudlets: " + user2SuccessCount + "/" + user2Results.size());
        Log.println("  - Total Execution Time: " + dft.format(user2TotalTime) + " seconds");
        Log.println("  - Average Execution Time: " + dft.format(user2AvgTime) + " seconds");

        Log.println("=========================================");
    }
}
