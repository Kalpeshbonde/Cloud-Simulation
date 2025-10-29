# CloudSim Demo Applications

This project contains 5 comprehensive CloudSim demonstration applications showcasing different aspects of cloud simulation. Each demo is organized in its own logical directory with complete, well-documented code.

## Project Structure

```
cloudsim-examples/
├── src/main/java/org/cloudbus/cloudsim/examples/
│   ├── demo1_basic_datacenter/
│   │   └── BasicDatacenterDemo.java
│   ├── demo2_network_topology/
│   │   └── NetworkTopologyDemo.java
│   ├── demo3_multiple_users/
│   │   └── MultipleUsersDemo.java
│   ├── demo4_dynamic_simulation/
│   │   └── DynamicSimulationDemo.java
│   └── demo5_global_manager/
│       └── GlobalManagerDemo.java
└── src/main/resources/
    └── topology.brite
```

## Demo Descriptions

### Demo 1: Basic Datacenter Simulation
**File:** `demo1_basic_datacenter/BasicDatacenterDemo.java`
**Purpose:** Demonstrates the simplest possible CloudSim simulation

**Features:**
- Single datacenter with one host
- One virtual machine
- One cloudlet (computational task)
- Basic result collection and analysis

**Key Learning Points:**
- Fundamental CloudSim concepts
- Datacenter, Host, VM, and Cloudlet creation
- Basic simulation workflow

### Demo 2: Network Topology with Two Datacenters
**File:** `demo2_network_topology/NetworkTopologyDemo.java`
**Purpose:** Shows network topology effects on cloud simulation

**Features:**
- Two datacenters, each with one host
- BRITE network topology configuration
- Network latency and bandwidth effects
- Distributed cloudlet execution

**Key Learning Points:**
- Network topology setup
- Inter-datacenter communication
- Network performance impact on simulation

### Demo 3: Multiple Users with Two Datacenters
**File:** `demo3_multiple_users/MultipleUsersDemo.java`
**Purpose:** Demonstrates multi-tenant cloud environment

**Features:**
- Two independent users/brokers
- Resource competition and fair allocation
- Enhanced datacenter capacity
- Comparative performance analysis

**Key Learning Points:**
- Multi-user cloud simulation
- Resource sharing and competition
- Performance comparison between users

### Demo 4: Dynamic Simulation with Pause/Resume
**File:** `demo4_dynamic_simulation/DynamicSimulationDemo.java`
**Purpose:** Advanced simulation control and dynamic entity creation

**Features:**
- Dynamic broker creation at runtime
- Simulation pause and resume capabilities
- Scalable datacenter infrastructure
- Event-driven entity management

**Key Learning Points:**
- Dynamic simulation control
- Runtime entity creation
- Advanced event handling
- Scalable simulation architecture

### Demo 5: Global Manager for Runtime Entity Creation
**File:** `demo5_global_manager/GlobalManagerDemo.java`
**Purpose:** Centralized management of multiple brokers

**Features:**
- Global manager entity for centralized control
- Runtime broker creation with different workload profiles
- Resource monitoring and workload redistribution
- Comprehensive performance analysis

**Key Learning Points:**
- Global management patterns
- Centralized resource coordination
- Dynamic workload distribution
- Advanced performance monitoring

## How to Run the Demos

### Prerequisites
- Java 21 or higher
- Maven 3.6 or higher
- The CloudSim project properly built

### Building the Project
```bash
# Navigate to the project root
cd D:\Projects\cloudsim-master

# Build the entire project
mvn clean install
```

### Running Individual Demos

#### Method 1: Using Maven Exec Plugin (Recommended)
```bash
# Navigate to the examples module
cd modules/cloudsim-examples

# Run Demo 1: Basic Datacenter
mvn exec:java -Dexec.mainClass="org.cloudbus.cloudsim.examples.demo1_basic_datacenter.BasicDatacenterDemo"

# Run Demo 2: Network Topology
mvn exec:java -Dexec.mainClass="org.cloudbus.cloudsim.examples.demo2_network_topology.NetworkTopologyDemo"

# Run Demo 3: Multiple Users
mvn exec:java -Dexec.mainClass="org.cloudbus.cloudsim.examples.demo3_multiple_users.MultipleUsersDemo"

# Run Demo 4: Dynamic Simulation
mvn exec:java -Dexec.mainClass="org.cloudbus.cloudsim.examples.demo4_dynamic_simulation.DynamicSimulationDemo"

# Run Demo 5: Global Manager
mvn exec:java -Dexec.mainClass="org.cloudbus.cloudsim.examples.demo5_global_manager.GlobalManagerDemo"
```

#### Method 2: Using Java Directly
```bash
# After building, navigate to the examples target directory
cd modules/cloudsim-examples/target/classes

# Run any demo using java command
java -cp ".:../../../cloudsim/target/classes:../../../cloudsim/target/dependency/*" org.cloudbus.cloudsim.examples.demo1_basic_datacenter.BasicDatacenterDemo
```

### Running All Demos Sequentially
```bash
# Navigate to examples module
cd modules/cloudsim-examples

# Run all demos one by one
for demo in demo1_basic_datacenter.BasicDatacenterDemo demo2_network_topology.NetworkTopologyDemo demo3_multiple_users.MultipleUsersDemo demo4_dynamic_simulation.DynamicSimulationDemo demo5_global_manager.GlobalManagerDemo; do
    echo "Running $demo..."
    mvn exec:java -Dexec.mainClass="org.cloudbus.cloudsim.examples.$demo"
    echo "Completed $demo"
    echo "----------------------------------------"
done
```

## Expected Output

### Demo 1 Output
- Datacenter and broker creation logs
- VM and cloudlet submission confirmations
- Simple execution results table

### Demo 2 Output
- Network topology configuration messages
- Datacenter-to-BRITE node mapping
- Execution results with network effects

### Demo 3 Output
- Multiple broker creation and resource allocation
- Separate result tables for each user
- Comparative performance statistics

### Demo 4 Output
- Dynamic broker creation timestamps
- Simulation pause/resume notifications
- Real-time simulation state monitoring

### Demo 5 Output
- Global manager initialization
- Runtime broker creation with workload profiles
- Resource monitoring reports
- Comprehensive performance analysis

## Customization

### Modifying Simulation Parameters
Each demo contains configurable parameters at the top of the main method:
- Number of VMs and cloudlets
- Resource specifications (MIPS, RAM, bandwidth)
- Simulation timing parameters

### Adding New Features
The demos are designed to be extensible. You can:
- Add new scheduling policies
- Implement custom VM allocation strategies
- Create additional monitoring and analysis features
- Extend the global manager with new capabilities

## Troubleshooting

### Common Issues
1. **ClassNotFoundException**: Ensure the project is properly built with `mvn clean install`
2. **Resource Loading Errors**: Verify `topology.brite` file exists in resources
3. **Memory Issues**: Increase JVM heap size with `-Xmx2g` flag
4. **Port Conflicts**: Ensure no other CloudSim instances are running

### Debug Mode
Add the following JVM arguments for detailed logging:
```bash
-Djava.util.logging.config.file=custom_log.properties -Dcloudbus.cloudsim.log.level=FINE
```

## Architecture Overview

Each demo follows the standard CloudSim simulation pattern:
1. **Initialization**: Set up CloudSim environment
2. **Entity Creation**: Create datacenters, brokers, VMs, and cloudlets
3. **Configuration**: Set up relationships and policies
4. **Execution**: Run the simulation
5. **Analysis**: Collect and analyze results

## Performance Metrics

The demos collect various performance metrics:
- **Execution Time**: Total time for cloudlet completion
- **Resource Utilization**: VM and host usage statistics
- **Network Performance**: Latency and bandwidth utilization (Demo 2)
- **Multi-user Metrics**: Fairness and competition analysis (Demo 3)
- **Dynamic Metrics**: Runtime entity creation performance (Demos 4 & 5)

## Contributing

When adding new demos or modifying existing ones:
1. Follow the established naming conventions
2. Include comprehensive comments and documentation
3. Add appropriate error handling
4. Update this README with new demo descriptions

## License

This project follows the CloudSim toolkit license (GPL v3).

## Support

For issues related to:
- **CloudSim Core**: Refer to the official CloudSim documentation
- **Demo Applications**: Check the inline comments and this README
- **Build Issues**: Verify Maven and Java versions match requirements
