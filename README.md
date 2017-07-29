SPN OS: Software-Programmed Networking Operating System
=======================================================

### What is SPN OS?
SPN OS is a post-SDN networking platform based on "program & compile" style, where you can write network programs in high-level syntax and compile them to low-level SDN southbound protocol rules (such as OpenFlow). 

BE WARNED THAT THIS IS A PROTOTYPE, ALPHA VERSION OF THIS SOFTWARE

### Pre-requisites

* Java 8
* Maven 3
* Docker
* Mininet
* LINC-OE (for Optical Mapping example)

For Debian/Ubuntu, see the scripts/debian-ubuntu directory for utilitary scripts to install the necessary software.

### How to use it
SPN OS is based on NetKAT network programming language, which is built on top of frenetic. Our version of NetKAT has been modified and packaged to be aware of the concept of Virtual Network Objects, which represent network services. To start the NetKAT server, run:

    $ cd bin  
    $ ./frenetic.native staged-server

Browse the examples directory to see the existing examples. Run build.sh script at the root of each example directory to install it. The examples are:

## EXAMPLES
### BigSwitch  
Go to examples/big-switch directory and you will see the following structure, which is the same for all examples:

    .  
    ├── arbiter  
    ├── build.sh  
    ├── network  
    ├── scripts  
    └── tenant  

  The arbiter directory contains the Arbiter's configuration file and it's where the Arbiter application JAR will be generated.

  The network directory contains the physical network definition and Mininet files to emulate it

  The scripts directory contains utilitary scripts for this example

  The tenant directory contains the tenant configuration file (*tenant_config.xml*), Dockerfile, and virtual network and virtual mapping definitions. On the tenant configuration file you can choose between the default built-in SDN controller in spn-os, or ONOS (just write 'onos' under controller-type, and its IP address and port). If ONOS is not running from within the tenant container, you need to update the IP address on the mininet script (physical.py under network directory). 

  This example consists of a simple virtual BigSwitch with 4 connected hosts, which is mapped to a physical network (network/physical.json) consisting of 4 switches in a linear topology with 2 hosts in each of the edge switches. The mapping of the virtual ingress/egress ports are defined in *virtual_mapping1.json*. SPN OS is currently not performing virtual network embedding and therefore the mapping must be explicit, but only for the ingress/egress ports, i.e., the ports connecting switches to hosts - NetKAT will take care of the rest.

  The NetKAT program for this example (instructions on how to modify it or create your own program later!) consists of connecting host h1 to host h3, and host h2 to host h4.

Virtual Network:

                  [h1]     
                   |  
          [h1] ── vs1 ── [h3]   
                   |  
                  [h4] 

Physical Network:

         [h1]           [h3]
          |              |
          s1 - s2 - s3 - s4 
          |              |
         [h2]           [h4]
  
 NetKAT Program:
   
    filter (sw=1 and ipSrc=10.0.0.1 and ipDst=10.0.0.3); port <- 3 +
    filter (sw=1 and ipSrc=10.0.0.3 and ipDst=10.0.0.1); port <- 1 +
    filter (sw=1 and ipSrc=10.0.0.2 and ipDst=10.0.0.4); port <- 4 +
    filter (sw=1 and ipSrc=10.0.0.4 and ipDst=10.0.0.2); port <- 2 

To build this example, run the build.sh script. It will compile the code using maven, and the two generated jars, Arbiter.jar and Tenant.jar, will be copied to the arbiter and tenant directory respectively. A Docker container will be built for the tenant, therefore the first time you build this example it will take some time.

One step of the build process involves adding some IP addresses to your main network interface. The script tries to detect the network interface and do it automatically, but this has not been tested on all flavors of Linux. To be sure, run "ip addr" and check that you have 192.168.137.110 and 192.168.137.150 addresses on the list. If not, manually add them with "sudo ip addr add [ip-address] dev [interface-name]" 

To run it, open one terminal and start the Mininet network:

    $ cd network  
    $ sudo python physical.py

In another terminal, start the Arbiter application by running: 

    $ cd arbiter  
    $ java -jar Arbiter.java

Finally open another terminal or tab and run:

    $ cd scripts  
    $ ./run_tenant.sh

This will start a docker container for the tenant. Inside the container run:

    # java -jar Tenant.jar

The tenant console will show up. Run:

    >> load vno

which will load the Virtual Network Object (VNO).

    >> activate vno

will compile the VNO's program (NetKAT server must be running)

Test the successful activation of the VNO my running on Mininet CLI: 

    mininet> h1 ping h3
    mininet> h2 ping h4
(should be successful)

    mininet> h1 ping h2
    mininet> h3 ping h4
(should fail)

Finally:

    >> exit

to exit the tenant application; and

    # exit 

to exit the docker container. 

Run clean_up_docker_containers.sh script inside the scripts directory to permanently remove the container

### Packet-Mapping
In this example there are two alternative mappings to use with the same NetKAT program, and a *remap* command is added to the console to perform the switch.

The virtual network in this case is as follows:

          [h1] ── vs1 ── vs2 ── [h2]   

And the physical network:

                     s3  
                   /    \         
                  /      \       
       [h1] ── s1 ── s4 ── s2 ── [h2]
         
The first mapping, *virtual_mapping1.json*, maps the virtual vs1-vs2 endpoints to the physical endpoints corresponding to s1-s3 and s3-s2, whereas the second mapping (*virtual_mapping2.json*) maps them to s1-s4 and s4-s2. In other words, the first option uses the higher path, and the second uses the lower path between s1 and s2. 

The NetKAT program is similar to the last example, with IP connectivity between hosts h1 and h2 (10.0.0.1 and 10.0.0.2).

Follow the instructions in the last example to build and run it. Everything is the same.

After running *load* and *activate* methods, and verifying that ping works, check s1's flow-table:

    $ sudo ovs-ofctl dump-flows s1
    $ (...) priority=65533,ip,in_port=1,nw_src=10.0.0.1,nw_dst=10.0.0.2 actions=mod_vlan_vid:1,output:2

notice the output:2 action indicating s1 is outputing the packet out the higher path going through s3. Now on the tenant console run:

    >> remap vno 2

this will remap the VNO to the virtual mapping #2. Check that ping still works, and the new flow-table for s1 will have:

    $ (...) priority=65533,ip,in_port=1,nw_src=10.0.0.1,nw_dst=10.0.0.2 actions=mod_vlan_vid:1,output:3

which means output is now port 3 (path s1-s4-s2).

Finally:

    >> exit

to exit the tenant application; and

    # exit 

to exit the docker container. 

Run clean_up_docker_containers.sh script inside the scripts directory to permanently remove the container

### Optical-Mapping
This example has the same virtual network as the previous one and essentially the same NetKAT program, except this time the physical network contains optical switches and *remap* operation consists of remapping the optical circuit between two packet switches. You need to have linc-oe installed in order to run this example (check out *install-linc-oe.sh* script under the scripts directory located in the root spn-os folder).

The topology consists of two (geographically distant) packet switches connected by a simple optical ring, as follows:

                         r2 ─ r3  
                        /        \         
                       /          \       
       [h1] ── s1 ── r1           r4 ── s2 ── [h2]
                       \         /
                        \       /
                           r5

'r' means ROADM switch.

To run this example, follow the same steps as the previous one. The vno name in this case is 'vno1', and the remap has a toggle syntax, meaning you don't have to specify the mapping ID, just the vno name:

    >> remap vno1

and it will alternate between the upper (r1-r2-r3-r4) and the bottom (r1-r5-r4) path of the ring (depending on what is currently selected). In order to see that it works, you need to check the ROADM switches' flow-table. To attach to LINC's console, run *linc_attach.sh* script under the scripts directory for this example. Press ENTER until you have a prompt that looks like this:

    (linc@dev-vm)6>

then run *linc_us4_oe_flow:get_flow_table(1,0).* (the dot at the end is important!). The first number inside the parentheses is the ROADM switch ID, and the second is the OpenFlow table. We are not using multi-tables so you can always leave it zero.  

LINC switch software unfortunately doesn't give a very user-friendly print of the flow-table, but here's what you get for r1 (the relevant bits are highlighted with **): 

    (linc@dev-vm)6> linc_us4_oe_flow:get_flow_table(1,0).
    [{flow_entry,{65535,#Ref<0.0.0.47503>},
             65535,
             {ofp_match,[{ofp_field,openflow_basic,**in_port**,false,
                                    <<0,0,0,**1**>>,
                                    undefined}]},
             <<0,77,0,0,74,18,49,251>>,
             [send_flow_rem],
             {1500,628662,685589},
             {infinity,0,0},
             {infinity,0,0},
             [{ofp_instruction_apply_actions,2,
                                             [{ofp_action_experimenter,99,7636849,
                                                                       {**ofp_action_set_field**,13,
                                                                                             {ofp_field,infoblox,och_sigid,false,<<**1**,...>>,undefined}}},
                                              {**ofp_action_output**,16,**2**,0}]}]},

this means incoming traffic on port 1 will have the wavelength (ofp_action_set_field,13) set to 1, and output on port 2.

After remapping:

    (linc@dev-vm)7> linc_us4_oe_flow:get_flow_table(1,0).
    [{flow_entry,{65535,#Ref<0.0.0.47746>},
             65535,
             {ofp_match,[{ofp_field,openflow_basic,in_port,false,
                                    <<0,0,0,1>>,
                                    undefined}]},
             <<0,77,0,0,74,18,49,251>>,
             [send_flow_rem],
             {1500,628709,453055},
             {infinity,0,0},
             {infinity,0,0},
             [{ofp_instruction_apply_actions,2,
                                             [{ofp_action_experimenter,99,7636849,
                                                                       {ofp_action_set_field,13,
                                                                                             {ofp_field,infoblox,och_sigid,false,<<1,...>>,undefined}}},
                                              {ofp_action_output,16,**3**,0}]}]},

all is the same, except this time output is on port 3 (link r1-r5).

If you run remap again:

    >> remap vno1

it will switch back to the first path.

Run exit to quit the tenant console, then exit again on the docker command line.

Run clean_up_docker_containers.sh script inside the scripts directory to permanently remove the container.

## CREATING YOUR OWN PROGRAMS
You can modify any example or create a new one by studying the examples' source code under spn.examples Java package. In particular, the methods:

getVirtualPolicy(): determines the NetKAT virtual program; and
getVirtualIngressPolicy(): determines how virtual ingress ports map to physical ones.

We have a Java-wrapper for NetKAT syntax. For example, on BigSwitchTenant.java:

    protected Policy getVirtualPolicy() {
        Policy vs1;
        vs1 = new Sequence(new Filter(new Test("switch", "1")),
                           new Union(
                                   new Sequence(new Filter(new And(
                                           new Test("ipDst", "10.0.0.3"),
                                           new Test("ipSrc", "10.0.0.1"))),
                                                new Modification("port", "3")),
                                   new Sequence(new Filter(new And(
                                           new Test("ipDst", "10.0.0.1"),
                                           new Test("ipSrc", "10.0.0.3"))),
                                                new Modification("port", "1")),
                                   new Sequence(new Filter(new And(
                                           new Test("ipDst", "10.0.0.4"),
                                           new Test("ipSrc", "10.0.0.2"))),
                                                new Modification("port", "4")),
                                   new Sequence(new Filter(new And(
                                           new Test("ipDst", "10.0.0.2"),
                                           new Test("ipSrc", "10.0.0.4"))),
                                                new Modification("port", "2"))
                           ));

        return vs1;
    }

In this case have a Union of filter/modification pairs, the filter acting on IP source and destination, the modification on output port.

For a deeper understanding of NetKAT syntax, check out the original paper: http://www.cs.cornell.edu/~jnfoster/papers/frenetic-netkat.pdf


## TROUBLESHOOTING

* If after activating the VNO you don't get any ping, try activating again. This can happen often in the optical mapping example, as for some reason the connection between the controller and the LINC switches is a little unstable.
* If something goes wrong, trying restarting everything in this order: first, run mininet network; then, arbiter; then, tenant application.
* If, when trying to start the tenant's docker container, you get the following error message: *Error starting userland proxy: listen tcp 192.168.137.150:6633: bind: cannot assign requested address.*, it's because your network interface no longer has the IP address 192.168.137.150 (happens if you have rebooted the machine or if it went on sleep mode). Just add it back by running config_ip_addresses.sh under the scripts directory of the examples.

