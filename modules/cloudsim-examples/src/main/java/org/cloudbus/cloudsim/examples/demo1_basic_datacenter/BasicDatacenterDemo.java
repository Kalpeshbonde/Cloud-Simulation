/*
 * Demo 1: Basic Datacenter Simulation
 *
 * Purpose: Demonstrates the creation of a simple CloudSim simulation with:
 * - One datacenter containing one host
 * - One virtual machine
 * - One cloudlet (computational task)
 * - Basic simulation execution and result printing
 *
 * This is the simplest possible CloudSim simulation showing fundamental concepts:
 * - Datacenter creation and configuration
 * - Host setup with processing elements (PEs)
 * - VM allocation and cloudlet execution
 * - Result collection and analysis
 */

package org.cloudbus.cloudsim.examples.demo1_basic_datacenter;

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
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.UtilizationModelFull;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicySimple;
import org.cloudbus.cloudsim.VmSchedulerTimeShared;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.BwProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.PeProvisionerSimple;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;

/**
 * A basic example demonstrating how to create a simple datacenter simulation
 * with one datacenter, one host, one VM, and one cloudlet.
 */
public class BasicDatacenterDemo {

    /**
     * Main method to run the basic datacenter simulation
     */
    public static void main(String[] args) {
        Log.println("========================================");
        Log.println("Starting Basic Datacenter Demo");
        Log.println("========================================");

        try {
            // Step 1: Initialize the CloudSim library
            // This must be called before creating any CloudSim entities
            int numUsers = 1;           // Number of cloud users/brokers
            Calendar calendar = Calendar.getInstance();  // Calendar for time management
            boolean traceFlag = false;  // Trace events? (false = disable)

            // Initialize CloudSim
            CloudSim.init(numUsers, calendar, traceFlag);
            Log.println("CloudSim initialized successfully");

            // Step 2: Create Datacenter
            // A datacenter is a collection of hosts that provides resources
            Datacenter datacenter = createDatacenter("SimpleDatacenter");
            Log.println("Created datacenter: " + datacenter.getName());

            // Step 3: Create Broker
            // The broker is responsible for mediating between users and cloud resources
            DatacenterBroker broker = new DatacenterBroker("SimpleBroker");
            int brokerId = broker.getId();
            Log.println("Created broker: " + broker.getName() + " (ID: " + brokerId + ")");

            // Step 4: Create one Virtual Machine (VM)
            List<Vm> vmList = new ArrayList<>();

            // VM specifications
            int vmId = 0;
            int mips = 1000;           // Million Instructions Per Second
            long size = 10000;         // Image size (MB)
            int ram = 512;             // VM memory (MB)
            long bandwidth = 1000;     // Network bandwidth
            int pesNumber = 1;         // Number of CPU cores
            String vmm = "Xen";        // Virtual Machine Monitor (hypervisor)

            // Create VM with time-shared cloudlet scheduler
            Vm vm = new Vm(vmId, brokerId, mips, pesNumber, ram, bandwidth, size, vmm,
                          new CloudletSchedulerTimeShared());
            vmList.add(vm);
            Log.println("Created VM: ID=" + vmId + ", MIPS=" + mips + ", RAM=" + ram + "MB, PEs=" + pesNumber);

            // Submit VM to the broker
            broker.submitGuestList(vmList);

            // Step 5: Create one Cloudlet (computational task)
            List<Cloudlet> cloudletList = new ArrayList<>();

            // Cloudlet specifications
            int cloudletId = 0;
            long length = 400000;      // Cloudlet length in Million Instructions (MI)
            long fileSize = 300;       // Input file size (bytes)
            long outputSize = 300;     // Output file size (bytes)
            int pesCount = 1;          // Number of PEs required

            // Utilization model defines how the cloudlet uses resources (CPU, RAM, bandwidth)
            UtilizationModel utilizationModel = new UtilizationModelFull();

            // Create cloudlet
            Cloudlet cloudlet = new Cloudlet(cloudletId, length, pesCount, fileSize, outputSize,
                                           utilizationModel, utilizationModel, utilizationModel);
            cloudlet.setUserId(brokerId);
            cloudletList.add(cloudlet);
            Log.println("Created Cloudlet: ID=" + cloudletId + ", Length=" + length + " MI, PEs=" + pesCount);

            // Submit cloudlet to the broker
            broker.submitCloudletList(cloudletList);

            // Step 6: Start the simulation
            Log.println();
            Log.println("Starting simulation...");
            CloudSim.startSimulation();

            // Step 7: Stop simulation and collect results
            CloudSim.stopSimulation();

            // Get the list of completed cloudlets from the broker
            List<Cloudlet> completedCloudlets = broker.getCloudletReceivedList();
            Log.println("Simulation completed. Processing results...");

            // Step 8: Print the results
            printCloudletList(completedCloudlets);

            Log.println("========================================");
            Log.println("Basic Datacenter Demo finished!");
            Log.println("========================================");

        } catch (Exception e) {
            Log.println("ERROR: The simulation has been terminated due to an unexpected error");
            Log.println("ERROR: " + e.getMessage());
        }
    }

    /**
     * Creates a datacenter with one host and one processing element (PE)
     *
     * @param name Name of the datacenter
     * @return Created datacenter object
     */
    private static Datacenter createDatacenter(String name) {
        // Step 1: Create a list to store processing elements (PEs/CPU cores)
        List<Pe> peList = new ArrayList<>();

        int mips = 1000;  // MIPS (Million Instructions Per Second) rating of each PE

        // Create one PE (CPU core) with the specified MIPS rating
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        // Step 2: Create a host with the PE list
        List<Host> hostList = new ArrayList<>();

        int hostId = 0;
        int ram = 2048;             // Host memory in MB
        long storage = 1000000;     // Host storage in MB
        int bandwidth = 10000;      // Host bandwidth in Mbps

        // Create host with resource provisioners
        Host host = new Host(
                hostId,
                new RamProvisionerSimple(ram),
                new BwProvisionerSimple(bandwidth),
                storage,
                peList,
                new VmSchedulerTimeShared(peList)  // Time-shared scheduling for VMs
        );
        hostList.add(host);

        // Step 3: Create datacenter characteristics
        String architecture = "x86";        // System architecture
        String os = "Linux";                // Operating system
        String vmm = "Xen";                 // Virtual Machine Monitor
        double timeZone = 10.0;             // Time zone (GMT+10)
        double costPerSec = 3.0;            // Cost of using processing per second
        double costPerMem = 0.05;           // Cost of using memory per MB
        double costPerStorage = 0.001;      // Cost of using storage per MB
        double costPerBw = 0.0;             // Cost of using bandwidth per MB

        DatacenterCharacteristics characteristics = new DatacenterCharacteristics(
                architecture, os, vmm, hostList, timeZone, costPerSec,
                costPerMem, costPerStorage, costPerBw);

        // Step 4: Create datacenter with the characteristics and policies
        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(
                    name,
                    characteristics,
                    new VmAllocationPolicySimple(hostList),  // VM allocation policy
                    new LinkedList<>(),                       // Storage list (empty)
                    0                                         // Scheduling interval
            );
        } catch (Exception e) {
            Log.println("ERROR: Failed to create datacenter: " + e.getMessage());
        }

        return datacenter;
    }

    /**
     * Prints the execution results of completed cloudlets
     *
     * @param cloudletList List of cloudlets to print
     */
    private static void printCloudletList(List<Cloudlet> cloudletList) {
        int size = cloudletList.size();
        Cloudlet cloudlet;

        String indent = "    ";
        DecimalFormat dft = new DecimalFormat("###.##");

        Log.println();
        Log.println("========== EXECUTION RESULTS ==========");
        Log.println("Cloudlet ID" + indent + "STATUS" + indent + "Datacenter" +
                   indent + "VM ID" + indent + "Time" + indent + "Start Time" +
                   indent + "Finish Time");

        for (int i = 0; i < size; i++) {
            cloudlet = cloudletList.get(i);
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

        Log.println();
        Log.println("Summary:");
        Log.println("  - Total cloudlets: " + size);
        if (size > 0) {
            int successCount = 0;
            for (Cloudlet c : cloudletList) {
                if (c.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                    successCount++;
                }
            }
            Log.println("  - Successful executions: " + successCount);
            if (successCount > 0) {
                Log.println("  - Total execution time: " + dft.format(cloudletList.get(0).getActualCPUTime()) + " seconds");
            }
        } else {
            Log.println("  - No cloudlets executed");
        }
        Log.println("=======================================");
    }
}

