package org.cloudbus.cloudsim.examples.network;

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
import org.cloudbus.cloudsim.NetworkTopology;
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

/**
 *  two datacenters (each with one host and one PE),
 * sets up a network topology and runs two cloudlets — one on each datacenter
 * using two separate brokers.
 */
public class NetworkTwoDatacentersExample {

    public static void main(String[] args) {
        Log.println("Starting NetworkTwoDatacentersExample...");

        try {
            int numUsers = 2; // two brokers
            Calendar calendar = Calendar.getInstance();
            boolean traceFlag = false;

            CloudSim.init(numUsers, calendar, traceFlag);

            // Create two datacenters
            Datacenter datacenter0 = createDatacenter("Datacenter_0");
            Datacenter datacenter1 = createDatacenter("Datacenter_1");
            Datacenter datacenter2 = createDatacenter("Datacenter_2");
            Datacenter datacenter3 = createDatacenter("Datacenter_3");

            // Create two brokers
            DatacenterBroker broker0 = new DatacenterBroker("Broker_0");
            DatacenterBroker broker1 = new DatacenterBroker("Broker_1");
            DatacenterBroker broker2 = new DatacenterBroker("Broker_2");
            DatacenterBroker broker3 = new DatacenterBroker("Broker_3");

            int broker0Id = broker0.getId();
            int broker1Id = broker1.getId();
            int broker2Id = broker2.getId();
            int broker3Id = broker3.getId();

            // Create one VM and one Cloudlet for broker0
            List<Vm> vmList0 = new ArrayList<>();
            int vmid = 0;
            int mips = 500;
            long size = 10000;
            int ram = 512;
            long bw = 1000;
            int pesNumber = 1;
            String vmm = "Xen";

            Vm vm0 = new Vm(vmid, broker0Id, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmList0.add(vm0);
            broker0.submitGuestList(vmList0);

            List<Cloudlet> cloudletList0 = new ArrayList<>();
            int id = 0;
            long length = 40000;
            long fileSize = 300;
            long outputSize = 300;
            UtilizationModel utilizationModel = new UtilizationModelFull();
            Cloudlet cloudlet0 = new Cloudlet(id, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet0.setUserId(broker0Id);
            cloudletList0.add(cloudlet0);
            broker0.submitCloudletList(cloudletList0);

            // Create one VM and one Cloudlet for broker1
            List<Vm> vmList1 = new ArrayList<>();
            Vm vm1 = new Vm(0, broker1Id, mips, pesNumber, ram, bw, size, vmm, new CloudletSchedulerTimeShared());
            vmList1.add(vm1);
            broker1.submitGuestList(vmList1);

            List<Cloudlet> cloudletList1 = new ArrayList<>();
            Cloudlet cloudlet1 = new Cloudlet(0, length, pesNumber, fileSize, outputSize, utilizationModel, utilizationModel, utilizationModel);
            cloudlet1.setUserId(broker1Id);
            cloudletList1.add(cloudlet1);
            broker1.submitCloudletList(cloudletList1);

            // Configure network - load topology and map nodes
            NetworkTopology.buildNetworkTopology(NetworkTwoDatacentersExample.class.getClassLoader().getResource("topology.brite").getPath());

            // Map datacenters to BRITE nodes
            NetworkTopology.mapNode(datacenter0.getId(), 0);
            NetworkTopology.mapNode(datacenter1.getId(), 1);
            NetworkTopology.mapNode(datacenter2.getId(), 2);
            NetworkTopology.mapNode(datacenter3.getId(), 3);

            // Map brokers to BRITE nodes (choose two other nodes)
            NetworkTopology.mapNode(broker0.getId(), 4);
            NetworkTopology.mapNode(broker1.getId(), 5);
            NetworkTopology.mapNode(broker0.getId(), 6);
            NetworkTopology.mapNode(broker1.getId(), 7);

            // Start simulation
            CloudSim.startSimulation();

            // Retrieve results
            List<Cloudlet> newList0 = broker0.getCloudletReceivedList();
            List<Cloudlet> newList1 = broker1.getCloudletReceivedList();

            CloudSim.stopSimulation();

            // Print results
            Log.println("Results for Broker_0:");
            printCloudletList(newList0);
            Log.println("Results for Broker_1:");
            printCloudletList(newList1);
            Log.println("Results for Broker_3:");
            printCloudletList(newList0);
            Log.println("Results for Broker_4:");
            printCloudletList(newList1);

            Log.println("NetworkTwoDatacent" +
                    "ersExample finished!");
        } catch (Exception e) {
            e.printStackTrace();
            Log.println("The simulation has been terminated due to an unexpected error");
        }
    }

    private static Datacenter createDatacenter(String name) {
        List<Host> hostList = new ArrayList<>();

        List<Pe> peList = new ArrayList<>();
        int mips = 1000;
        peList.add(new Pe(0, new PeProvisionerSimple(mips)));

        int hostId = 0;
        int ram = 2048;
        long storage = 1000000;
        int bw = 10000;

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

        Datacenter datacenter = null;
        try {
            datacenter = new Datacenter(name, characteristics, new VmAllocationPolicySimple(hostList), storageList, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return datacenter;
    }

    private static void printCloudletList(List<Cloudlet> list) {
        DecimalFormat dft = new DecimalFormat("###.##");
        String indent = "    ";
        Log.println();
        Log.println("========== OUTPUT ==========");
        Log.println("Cloudlet ID" + indent + "STATUS" + indent + "Data center ID" + indent + "VM ID" + indent + "Time" + indent + "Start Time" + indent + "Finish Time");

        for (Cloudlet cloudlet : list) {
            Log.print(indent + cloudlet.getCloudletId() + indent + indent);
            if (cloudlet.getStatus() == Cloudlet.CloudletStatus.SUCCESS) {
                Log.print("SUCCESS");
                Log.println(indent + indent + cloudlet.getResourceId() + indent + indent + indent + cloudlet.getGuestId()
                        + indent + indent + dft.format(cloudlet.getActualCPUTime()) + indent + indent + dft.format(cloudlet.getExecStartTime())
                        + indent + indent + dft.format(cloudlet.getExecFinishTime()));
            }
        }
    }
}

