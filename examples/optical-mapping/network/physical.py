#!/usr/bin/env python
from optical import LINCNet, LINCSwitch, LINCLink

from mininet.node import OVSSwitch, RemoteController
from mininet.cli import CLI
from mininet.link import Intf
from mininet.net import Mininet
from mininet.log import lg, info, error, debug, output

import os

def start(ip="192.168.137.150",port=6633):

    #classifier1_intfName = 'eth1'
    #classifier2_intfName = 'eth2'
    #sff1_intfName = 'eth3'
    #sff2_intfName = 'eth4'

    # Set up logging etc.
    lg.setLogLevel('info')
    lg.setLogLevel('output')

    ctrlr = lambda n: RemoteController(n, ip=ip, port=port, inNamespace=False)
    #ctrlr2 = RemoteController('2', ip='192.168.137.160', port=port, inNamespace=False)
    net = LINCNet(switch=OVSSwitch, controller=ctrlr, autoStaticArp=True, listenPort=6634)
    c1 = net.addController('c1')
    #c2 = net.addController('c2',controller=RemoteController, ip='192.168.137.62',port=6633)
    #c2 = net.addController(ctrlr2)

    # Add hosts
    h1 = net.addHost('h1')
    h2 = net.addHost('h2')

    # Add packet switches to connect hosts to the optical network
    s1 = net.addSwitch('s1', dpid='00:00:00:00:00:00:00:01', protocols='OpenFlow10')
    s2 = net.addSwitch('s2', dpid='00:00:00:00:00:00:00:02', protocols='OpenFlow10')

    # Add optical switches
    r1 = net.addSwitch('r1', dpid='00:00:00:00:00:00:00:11', cls=LINCSwitch)
    r2 = net.addSwitch('r2', dpid='00:00:00:00:00:00:00:12', cls=LINCSwitch)
    r3 = net.addSwitch('r3', dpid='00:00:00:00:00:00:00:13', cls=LINCSwitch)
    r4 = net.addSwitch('r4', dpid='00:00:00:00:00:00:00:14', cls=LINCSwitch)
    r5 = net.addSwitch('r5', dpid='00:00:00:00:00:00:00:15', cls=LINCSwitch)

    # Connect hosts to packet switches
    #print "Adding physical hosts to mininet network..."
    #_intf1 = Intf( classifier1_intfName, node=s1, port=1 )
    #_intf2 = Intf( sff1_intfName, node=s1, port=2 )
    #_intf3 = Intf( classifier2_intfName, node=s2, port=1 )
    #_intf4 = Intf( sff2_intfName, node=s2, port=2 )

    net.addLink(h1, s1)
    net.addLink(h2, s2)

    # Connect packet switches to optical switches
    net.addLink(s1, r1, 2, 1, cls=LINCLink)
    net.addLink(s2, r4, 2, 1, cls=LINCLink)

    # Connect optical switches to each other
    net.addLink(r1, r2, 2, 1, cls=LINCLink)
    net.addLink(r1, r5, 3, 1, cls=LINCLink)
    net.addLink(r2, r3, 2, 1, cls=LINCLink)
    net.addLink(r3, r4, 2, 2, cls=LINCLink)
    net.addLink(r5, r4, 2, 3, cls=LINCLink)
    

    # Start the network and prime other ARP caches
    net.start()
    # If using physical hosts this should be done manually
    net.staticArp()

    # Enter CLI mode
    output("Network ready\n")
    output("Press Ctrl-d or type exit to quit\n")
    CLI(net)
    net.stop()

try:
	start()
finally:
	os.system("sudo killall epmd")