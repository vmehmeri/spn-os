#!/usr/bin/env python

from mininet.node import OVSSwitch, RemoteController
from mininet.cli import CLI
from mininet.link import Intf
from mininet.net import Mininet
from mininet.log import lg, info, error, debug, output

def start(ip="192.168.137.150",port=6633):

    # Set up logging etc.
    lg.setLogLevel('info')
    lg.setLogLevel('output')

    ctrlr = lambda n: RemoteController(n, ip=ip, port=port, inNamespace=False)
    #ctrlr2 = RemoteController('2', ip='192.168.137.150', port=port, inNamespace=False)
    net = Mininet(switch=OVSSwitch, controller=ctrlr, autoStaticArp=True, listenPort=6634)
    c1 = net.addController('c1')
    #c2 = net.addController(ctrlr2)
    
    # Add hosts
    h1 = net.addHost('h1')
    h2 = net.addHost('h2')
    h3 = net.addHost('h3')
    h4 = net.addHost('h4')

    # Add switches
    s1 = net.addSwitch('s1', dpid='00:00:00:00:00:00:00:01', protocols='OpenFlow10')
    s2 = net.addSwitch('s2', dpid='00:00:00:00:00:00:00:02', protocols='OpenFlow10')
    s3 = net.addSwitch('s3', dpid='00:00:00:00:00:00:00:03', protocols='OpenFlow10')
    s4 = net.addSwitch('s4', dpid='00:00:00:00:00:00:00:04', protocols='OpenFlow10')

    net.addLink(h1, s1)
    net.addLink(h2, s1)
    net.addLink(s1, s2)
    net.addLink(s2, s3)
    net.addLink(s4, h3)
    net.addLink(s4, h4)
    net.addLink(s3, s4)    

    # Start the network and prime other ARP caches
    net.start()
    #net.staticArp()

    
    # Enter CLI mode
    output("Network ready\n")
    output("Press Ctrl-d or type exit to quit\n")
    CLI(net)
    net.stop()

start()
