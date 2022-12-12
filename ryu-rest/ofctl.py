from ryu.ofproto import ether
from ryu.lib.packet import packet
from ryu.lib.packet import arp
from ryu.lib.packet import ethernet

# The controller sends an ARP request to the given switch, which
# forwards the packet without changes based on the controller's instructions

def send_arp(datapath, arp_opcode, src_mac,
             src_ip, dst_mac, dst_ip, output_port):
    ether_proto = ether.ETH_TYPE_ARP
    hwtype = 1
    arp_proto = ether.ETH_TYPE_IP
    hlen = 6
    plen = 4

    pkt = packet.Packet()
    e = ethernet.ethernet(dst_mac, src_mac, ether_proto)
    a = arp.arp(hwtype, arp_proto, hlen, plen, arp_opcode,
                src_mac, src_ip, dst_mac, dst_ip)
    pkt.add_protocol(e)
    pkt.add_protocol(a)
    pkt.serialize()
    send_packet(datapath, output_port, pkt)

# Function to send a packet from the controller to an OF switch

def send_packet(datapath, output_port, pkt):
    actions = [datapath.ofproto_parser.OFPActionOutput(output_port, 0)]
    datapath.send_packet_out(buffer_id=datapath.ofproto.OFP_NO_BUFFER,
                             in_port=datapath.ofproto.OFPP_CONTROLLER,
                             actions=actions, data=pkt.data)

# Function for the controller to write a rule in the flow table 
# of the given switch

def add_flow(datapath, priority, match, actions, buffer_id=None):
        ofproto = datapath.ofproto
        parser = datapath.ofproto_parser

        inst = [parser.OFPInstructionActions(ofproto.OFPIT_APPLY_ACTIONS,
                                             actions)]
        if buffer_id:
            mod = parser.OFPFlowMod(datapath=datapath, buffer_id=buffer_id,
                                    priority=priority, match=match,
                                    instructions=inst)
        else:
            mod = parser.OFPFlowMod(datapath=datapath, priority=priority,
                                    match=match, instructions=inst)
        datapath.send_msg(mod)