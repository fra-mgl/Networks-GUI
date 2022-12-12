from ryu.app.wsgi import WSGIApplication
from ryu.base import app_manager
from ryu.controller import ofp_event
from ryu.controller.handler import set_ev_cls
from ryu.controller.handler import MAIN_DISPATCHER, CONFIG_DISPATCHER
from ryu.ofproto import ofproto_v1_3
# DON'T REMOVE THIS IMPORT
from ryu.topology.api import get_switch, get_link, get_host
from ryu.lib import hub
from http import server
from l2_controller import L2Controller
from l3_controller import L3Controller
from topology_controller import TopologyController
from ofctl import add_flow
from http_client import HTTPClient

NETCONF_BACKEND_URL = 'http://localhost:4000/'
IP_ADDRESSES_ENDPOINT = NETCONF_BACKEND_URL + 'allDataPathsIps'
IP_TABLES_ENDPOINT = NETCONF_BACKEND_URL + 'allIpTables'
NOTIFICATION_CONSUMER_ENDPOINT = ('127.0.0.1', 8000)

class App(app_manager.RyuApp):

    OFP_VERSIONS = [ofproto_v1_3.OFP_VERSION]

    _CONTEXTS = {'wsgi': WSGIApplication}

    def __init__(self, *args, **kwargs):
        super(App, self).__init__(*args, **kwargs)
        # When the ryu application starts, the controller
        # waits for L3 configuration from the network configuration service
        self.configured = False
        self.waiting_l3_configuration = True
        hub.spawn(self.notification_consumer_server)

        # A data structure to keep track of switches that are
        # waiting to be configured
        self.not_configured_datapaths = dict()

        # The topology REST API
        wsgi = kwargs['wsgi']
        wsgi.register(TopologyController, {'app': self})

        # The controller responsible of configuring L2 switches
        self.l2_controller = L2Controller()

        # The controller responsible of configuring L3 switches
        self.l3_controller = L3Controller()

    @set_ev_cls(ofp_event.EventOFPStateChange,
                 [MAIN_DISPATCHER])
    def _state_change_handler(self, ev):
        # For the time being, the controller does not handle the
        # situation where a new switch is added to the physical network
        if self.configured:
            raise Exception(f"A new switch entered the network, dpid: {ev.datapath.id}")
        else:
            self.not_configured_datapaths[ev.datapath.id] = ev.datapath

    
    @set_ev_cls(ofp_event.EventOFPSwitchFeatures, CONFIG_DISPATCHER)
    def switch_features_handler(self, ev):
        # Install table-miss flow entry as the default rule, so that when
        # an OFSwitch receives a packet that it doesn't know how to handle, 
        # it sends it to the controller.
        datapath = ev.msg.datapath
        ofproto = datapath.ofproto
        parser = datapath.ofproto_parser
        match = parser.OFPMatch()
        actions = [parser.OFPActionOutput(ofproto.OFPP_CONTROLLER,
                                          ofproto.OFPCML_NO_BUFFER)]

        # The rule is added with priority 0, applying to all packets.
        # Because 0 is the lowest priority level, any other available rule is
        # applied first. If no rule is available for the current packet, the
        # switch will contact the controller.
        add_flow(datapath, 0, match, actions)

    @set_ev_cls(ofp_event.EventOFPPacketIn, MAIN_DISPATCHER)
    def packet_in_handler(self, ev):
        # while the network has yet to be configured at layer 3,
        # all packets are dropped
        if self.configured:

            # if the l3 configuration is now available, retrieve it
            # from the network configuration service
            if self.waiting_l3_configuration:
                self.waiting_l3_configuration = False
                ip_addresses = HTTPClient.get(IP_ADDRESSES_ENDPOINT)
                ip_tables = HTTPClient.get(IP_TABLES_ENDPOINT)

                for (dpid, datapath) in ip_addresses.items():
                    # The switch is registered as a L3 switch
                    dp_ips = ip_addresses[dpid]
                    dp_ip_table = ip_tables[dpid]
                    self.l3_controller.register_datapath(datapath, dp_ips, dp_ip_table)
                    del self.not_configured_datapaths[dpid]
                
                # switches that did not get ip addresses assigned are
                # configured as L3 switches
                for (dpid, datapath) in self.not_configured_datapaths.items():
                    self.l2_controller.register_datapath(datapath)
                    del self.not_configured_datapaths[dpid]

            else:

                if ev.msg.datapath.id in self.l2_controller.datapaths:
                    self.l2_controller.packet_in_handler(ev.msg)
                elif ev.msg.datapath.id in self.l3_controller.datapaths:
                    self.l3_controller.packet_in_handler(ev.msg)
                else:
                    raise Exception(f"Unknown datapath {ev.msg.datapath.id}\n")

    # Simple HTTP server thread that exposes an endpoint to allow the main
    # controller thread to receive notifications from the network 
    # configuration service

    def notification_consumer_server(app):

        # When a notification is pushed by the network configuration
        # service, the thread sets signals the main controller
        class Handler(server.BaseHTTPRequestHandler):
            def do_GET(self):
                if self.path == '/notification':
                    nonlocal app
                    app.configured = True
                    self.send_response(200)
                    self.send_header('Content-type','text/html')
                    self.end_headers()
                    message = "Hello world!"
                    self.wfile.write(bytes(message, "utf8"))
                else:
                    self.send_response(404)

        s = server.HTTPServer(NOTIFICATION_CONSUMER_ENDPOINT, Handler)
        s.serve_forever()

    # HTTP server that allows clients to subscribe to notifications
    # that ryu pushes when the physical network configuration changes

    def notification_producer_server(self):
        pass