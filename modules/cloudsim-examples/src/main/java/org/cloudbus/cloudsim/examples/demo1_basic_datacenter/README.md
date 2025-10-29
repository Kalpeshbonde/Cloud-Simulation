# Demo 1: Basic Datacenter Simulation

## Overview

This demo demonstrates the creation of a simple CloudSim simulation with:
- **One datacenter** containing one host
- **One virtual machine**
- **One cloudlet** (computational task)
- **Basic simulation** execution and result printing

This is the simplest possible CloudSim simulation, perfect for understanding the fundamental concepts.

## Key Concepts Demonstrated

1. **CloudSim Initialization**: Setting up the simulation environment
2. **Datacenter Creation**: Configuring a datacenter with hosts and resources
3. **Host Setup**: Creating hosts with processing elements (PEs/CPU cores)
4. **VM Allocation**: Defining and allocating virtual machines
5. **Cloudlet Execution**: Creating and submitting computational tasks
6. **Result Collection**: Gathering and analyzing simulation results

## Components

### Datacenter Configuration
- **Name**: SimpleDatacenter
- **Hosts**: 1
- **PEs per host**: 1 (1000 MIPS)
- **RAM**: 2048 MB
- **Storage**: 1,000,000 MB
- **Bandwidth**: 10,000 Mbps

### Virtual Machine Configuration
- **VM ID**: 0
- **MIPS**: 1000
- **PEs**: 1
- **RAM**: 512 MB
- **Bandwidth**: 1000 Mbps
- **Image Size**: 10,000 MB
- **Scheduler**: Time-shared

### Cloudlet Configuration
- **Cloudlet ID**: 0
- **Length**: 400,000 MI (Million Instructions)
- **PEs Required**: 1
- **File Size**: 300 bytes
- **Output Size**: 300 bytes
- **Utilization Model**: Full (100% resource utilization)

## How to Run

### Using Maven
```bash
# From the cloudsim-examples directory
mvn clean compile exec:java -Dexec.mainClass="org.cloudbus.cloudsim.examples.demo1_basic_datacenter.BasicDatacenterDemo"
```

### Using Java directly
```bash
# Ensure CloudSim is compiled first
cd D:\Projects\cloudsim-master
mvn clean install

# Run the demo
cd modules\cloudsim-examples
java -cp target\classes;..\cloudsim\target\classes org.cloudbus.cloudsim.examples.demo1_basic_datacenter.BasicDatacenterDemo
```

### From IDE
Simply run the `BasicDatacenterDemo.java` file as a Java application.

## Expected Output

The simulation will output:
1. Initialization messages
2. Entity creation confirmations (datacenter, broker, VM, cloudlet)
3. Simulation start message
4. Execution results table showing:
   - Cloudlet ID
   - Execution status (SUCCESS/FAILED)
   - Datacenter ID
   - VM ID
   - CPU time
   - Start time
   - Finish time
5. Summary statistics

### Sample Output
```
========================================
Starting Basic Datacenter Demo
========================================
CloudSim initialized successfully
Created datacenter: SimpleDatacenter
Created broker: SimpleBroker (ID: 1)
Created VM: ID=0, MIPS=1000, RAM=512MB, PEs=1
Created Cloudlet: ID=0, Length=400000 MI, PEs=1

Starting simulation...
Simulation completed. Processing results...

========== EXECUTION RESULTS ==========
Cloudlet ID    STATUS    Datacenter    VM ID    Time    Start Time    Finish Time
    0          SUCCESS         2          0      400      0.1          400.1

Summary:
  - Total cloudlets: 1
  - Successful executions: 1
  - Total execution time: 400 seconds
=======================================
========================================
Basic Datacenter Demo finished!
========================================
```

## Code Structure

### Main Method
The `main()` method orchestrates the entire simulation:
1. Initializes CloudSim
2. Creates datacenter
3. Creates broker
4. Creates VM
5. Creates cloudlet
6. Starts simulation
7. Collects and prints results

### createDatacenter() Method
Creates a datacenter with:
- Processing elements (PEs)
- Host configuration
- Resource provisioners
- Datacenter characteristics
- VM allocation policy

### printCloudletList() Method
Formats and displays simulation results in a readable table format.

## Learning Points

1. **CloudSim.init()** must be called before creating any entities
2. **DatacenterBroker** mediates between users and cloud resources
3. **VMs** must be submitted to the broker before cloudlets
4. **Cloudlets** represent computational tasks
5. **Schedulers** (Time-shared vs Space-shared) determine resource allocation
6. **Provisioners** manage how resources are allocated to VMs

## Next Steps

After understanding this basic demo, explore:
- **Demo 2**: Network topology with multiple datacenters
- **Demo 3**: Multiple users and brokers
- **Demo 4**: Dynamic simulation with runtime changes
- **Demo 5**: Global manager for federated clouds

## Troubleshooting

### Common Issues
1. **ClassNotFoundException**: Ensure CloudSim core module is compiled
2. **NullPointerException**: Check that CloudSim.init() was called first
3. **No results**: Verify that VMs were submitted before cloudlets

### Debug Mode
Enable trace mode for detailed event logging:
```java
boolean traceFlag = true;  // Enable trace
CloudSim.init(numUsers, calendar, traceFlag);
```

## Additional Resources

- [CloudSim Documentation](http://www.cloudbus.org/cloudsim/)
- [CloudSim API Documentation](http://www.cloudbus.org/cloudsim/doc/)
- [CloudSim Examples](https://github.com/Cloudslab/cloudsim/tree/master/modules/cloudsim-examples)

