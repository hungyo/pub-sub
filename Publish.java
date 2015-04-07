package net.floodlightcontroller.publisher;


import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFFlowRemoved;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionDataLayerDestination;
import org.openflow.protocol.action.OFActionDataLayerSource;
import org.openflow.protocol.action.OFActionNetworkLayerDestination;
import org.openflow.protocol.action.OFActionNetworkLayerSource;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.U16;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;

import net.floodlightcontroller.core.IFloodlightProviderService;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.Set;

import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;

//import org.openflow.util.HexString;
//import org.restlet.resource.ClientResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Publish implements IOFMessageListener, IFloodlightModule {
	
	protected IFloodlightProviderService floodlightProvider;
	protected Set<Long> macAddresses;
	protected static Logger logger;
	private static int lastserver;
	
    private final static int SERVER_IP = IPv4.toIPv4Address("10.0.0.5");
	private final static byte[] SERVER_MAC = Ethernet.toMACAddress("00:00:00:00:00:05");
	
	
	final static Server[] SERVERS = {
		new Server("10.0.0.1", "00:00:00:00:00:01", (short)1),
		new Server("10.0.0.2", "00:00:00:00:00:02", (short)2),
		new Server("10.0.0.3", "00:00:00:00:00:03", (short)3),
	};
	
	
	private static short IDLE_TIMEOUT = 120; // in seconds
	private static short HARD_TIMEOUT = 0; // infinite
	
	 static Server destin= new Server("0.0.0.0","00:00:00:00:00:00",(short)0);
	
	static ArrayList<Host> action_pub=new ArrayList<Host>();
	static ArrayList<Host> action_sub=new ArrayList<Host>();
	static ArrayList<Host> animation_pub=new ArrayList<Host>();
	static ArrayList<Host> animation_sub=new ArrayList<Host>();
	static ArrayList<Host> contemporary_pub=new ArrayList<Host>();
	static ArrayList<Host> contemporary_sub=new ArrayList<Host>();
	static ArrayList<Host> pub = new ArrayList<Host>();
	
	public static int iph;
	public static byte[] mach;
	public static short porth;
    
	public static String type,genre,name,path;
	public static String uptime;
	
	public static short timeout=0;
	
	Message mssg=new Message(type,genre,name,path,uptime);
	
	@Override
	public String getName() {
		return "Publishers-Subscribers";
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l =
				new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context)
			throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	    macAddresses = new ConcurrentSkipListSet<Long>();
	    logger = LoggerFactory.getLogger(Publish.class);

	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		//floodlightProvider.addOFMessageListener(OFType.FLOW_REMOVED, this);

	}

	@Override
	public net.floodlightcontroller.core.IListener.Command receive(
			IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		 
		if (msg.getType() != OFType.PACKET_IN) { 
			// Allow the next module to also process this OpenFlow message
		    return Command.CONTINUE;
		}
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
	                		IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		 
		OFPacketIn pi = (OFPacketIn) msg;
		OFMatch match = new OFMatch();	
		 		
		match.loadFromPacket(pi.getPacketData(), pi.getInPort());
		 
		System.out.println("Destination transport = "+match.getTransportDestination());
		
		System.out.println("Packet_in");
		destin=SERVERS[0];
		//Destination IP Address for each packet-in
		if(match.getNetworkDestination() == SERVER_IP){
		if(match.getNetworkProtocol() == 17 || match.getNetworkProtocol() == 6){
			
		         logger.info("Received pub_sub request");
	             System.out.print("1 :: ");
		         System.out.println(match.loadFromPacket(pi.getPacketData(), pi.getInPort()));
		         int sip = match.getNetworkSource();	
		         System.out.print("2 :: ");
		         byte[] smac = match.getDataLayerSource();
		         short sport = match.getInputPort();
         
		// Message msg1=new Message();
		 
		 Host h = new Host(sip,smac,sport);
		 System.out.println(IPv4.fromIPv4Address(match.getNetworkDestination()));
		 String s = "";
		 String[] ss= new String[10];
		 if(eth.getEtherType() == Ethernet.TYPE_IPv4){
			    IPv4 ip = (IPv4)eth.getPayload();        	
			    if(match.getNetworkProtocol() == 6){
			          TCP tcp = (TCP)ip.getPayload();
			          Data data = (Data)tcp.getPayload();
			          byte [] bytes = data.getData();
			          s = new String(bytes);
			          ss = s.split(" ");
			          
			     if(ss.length>1)
			          {
			    	  System.out.println("$$$$$$$$$$$ Message received is "+s);
			    	  System.out.print("type is  ");
			    	  mssg.type = ss[0];
			    	  mssg.genre = ss[1];
		              System.out.println(mssg.type);
			          System.out.print("genre is  ");
			          System.out.println(mssg.genre);
			          
			          if(mssg.getType().equals("advertise") || mssg.getType().equals("publish"))
			          {
			        	  mssg.name = ss[2];
			              mssg.path = ss[3];
			              
			          }
			          if(mssg.getType().equals("advertise"))
			          {
			        	  mssg.uptime = ss[4];
			        	  String a =ss[4];
			        	  System.out.println("timeout is "+mssg.getUptime());
			        	  try {
			        		  timeout= Short.parseShort(a);
			        	} catch (NumberFormatException e) {
			        	      //Will Throw exception!
			        		System.out.println("cannot parse!");
			        	      //do something! anything to handle the exception.
			        	}
			        	  
			        	  System.out.println("timeout is "+timeout);
			          }
			          System.out.println("genre is "+mssg.genre);
			          route(sw,pi,eth,h,mssg,timeout);
			        }
	     
			    }

		      }
			}
		// System.out.println(type);
	   }
		
	if (match.getDataLayerType() == Ethernet.TYPE_ARP) {
		System.out.println("Packet_in packet");		
		logger.info("Received an ARP request");
		handleARPRequest(sw, pi, cntx);		
		}
	
	return Command.CONTINUE;
	}
	
	

	private void handleARPRequest(IOFSwitch sw, OFPacketIn pin,
			FloodlightContext cntx) {
		// TODO Auto-generated method stub
		logger.debug("Handle ARP request");
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
				IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if (! (eth.getPayload() instanceof ARP))
			return;
		ARP arpRequest = (ARP) eth.getPayload();
		
		// generate ARP reply
		IPacket arpReply = new Ethernet()
			.setSourceMACAddress(Publish.destin.getMAC())
			.setDestinationMACAddress(eth.getSourceMACAddress())
			.setEtherType(Ethernet.TYPE_ARP)
			.setPriorityCode(eth.getPriorityCode())
			.setPayload(
				new ARP()
				.setHardwareType(ARP.HW_TYPE_ETHERNET)
				.setProtocolType(ARP.PROTO_TYPE_IP)
				.setOpCode(ARP.OP_REPLY)
				.setSenderHardwareAddress(Publish.destin.getMAC())
				.setSenderProtocolAddress(Publish.destin.getIP())
				.setTargetHardwareAddress(arpRequest.getSenderHardwareAddress())
				.setTargetProtocolAddress(arpRequest.getSenderProtocolAddress()));
		
		sendARPReply(arpReply, sw, OFPort.OFPP_NONE.getValue(), pin.getInPort());
		
	}

	private void sendARPReply(IPacket packet, IOFSwitch sw, short outPort,
			short inPort) {
		// TODO Auto-generated method stub
		// Initialize a packet out
				OFPacketOut po = (OFPacketOut) floodlightProvider.getOFMessageFactory()
						.getMessage(OFType.PACKET_OUT);
				po.setBufferId(OFPacketOut.BUFFER_ID_NONE);
				po.setInPort(inPort);
				
				// Set output actions
				List<OFAction> actions = new ArrayList<OFAction>();
				actions.add(new OFActionOutput(outPort, (short) 0xffff));
				po.setActions(actions);
				po.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);
				
				// Set packet data and length
				byte[] packetData = packet.serialize();
				po.setPacketData(packetData);
				po.setLength((short) (OFPacketOut.MINIMUM_LENGTH + po.getActionsLength() + packetData.length));
				
				// Send packet
				try 
				   {
					 sw.write(po, null);
					 sw.flush();
				   } 
				catch (IOException e)
				  {
					logger.error("Failure writing packet out", e);
				  }
	    }

	private void route(IOFSwitch sw, OFPacketIn pi, Ethernet eth,Host h,Message mssg, short timeout2) {
		// TODO Auto-generated method stub
		
           System.out.println("Hello " +mssg.getType());
	       
           if(mssg.type.equals("advertise"))
	         {
	    	   System.out.println("Okay!You want to advertise?");
	    	   advertise(h,mssg);//store in publishers queue
	         }
	       
           else if(mssg.type.equals("subscribe"))
	         {
        	   System.out.println("Okay!You want to subscribe?");
	    	   subscribe(h,mssg,mssg.genre); //store in subscribers queue
	         }
	       
	       else if(mssg.type.equals("publish"))
	         {
	    	   System.out.println("Okay!You want to publish?");
	    	   publish(h,mssg,sw,pi,timeout2);
	         }
	       
	       else if(mssg.type.equals("unsubscribe"))
	         {
	    	   System.out.println("Okay!Unsubscribing you in a minute...");
	    	   unsubscribe(h,mssg);
	         }
	       
	       else
	        {
	    	   logger.info("Invalid function.Type <advertise or publish or subscribe> <topic:action/animation/contemporary>"); //we dont care
	           System.out.println("Because its "+type);
	        }
        }
	
	
	private void unsubscribe(Host h, Message mssg) {
		// TODO Auto-generated method stub
		if(mssg.getGenre().equals("action"))
		{
			action_sub.remove(h);
		}
	    else if(mssg.getGenre().equals("contemporary"))
	    {
	    	contemporary_sub.remove(h);
	    }
	    else if(mssg.getGenre().equals("animation"))
	    {
	    	animation_sub.remove(h);
	    }
	    else
	       System.out.println("Invalid format"); //the rest of the undefined topics
	}

	private void publish(Host h,Message mssg,IOFSwitch sw,OFPacketIn pi, short timeout2) {
		// TODO Auto-generated method stub
		if(mssg.getGenre().equals("action"))
		{
			//use action list
			System.out.println("###########Action genre############");
			if(action_pub!=null){
			  notify_sub(h,action_sub,sw,pi,mssg,timeout2);
			}
			else
			{
				System.out.println("You havn't advertised your publication yet!!");
			}
		}
		else if(mssg.getGenre().equals("animation"))
		{
			if(animation_pub!=null)
			    notify_sub(h,animation_sub,sw,pi,mssg,timeout2);
			else
				System.out.println("You havn't advertised your publication yet!!");
		}
		else if(mssg.getGenre().equals("contemporary"))
		{
			//use contemporary list
			if(contemporary_pub!=null)
			    notify_sub(h,contemporary_sub,sw,pi,mssg,timeout2);
			else
				System.out.println("You havn't advertised your publication yet!!");
		}	   
	}

	
	private void notify_sub(Host h,
			ArrayList<Host>sub2,IOFSwitch sw,OFPacketIn pi,Message mssg, short timeout2) {
		// TODO Auto-generated method stub
		Server proxy= new Server("0.0.0.0","00:00:00:00:00:00",(short)0);
		System.out.println("Uptime is "+mssg.getUptime());
		
		System.out.println("timeout integer= "+timeout2);
		System.out.println("notify that i have published");
		if(mssg.getGenre().equals("action"))
		{
			System.out.println("Insert for action movies");
			proxy=SERVERS[0];
			pub=action_pub;
			System.out.println(pub.size());
		}
		else if(mssg.getGenre().equals("contemporary"))
			{
			   proxy=SERVERS[1];
			   pub=contemporary_pub;
			}
		else if(mssg.getGenre().equals("animation"))
			{
			   proxy=SERVERS[2];
			   pub=animation_pub;
			}
		System.out.println("number of subscribers : "+sub2.size());
		if(sub2.size()>0){
		for (int i = 0; i < sub2.size(); i++)
		{
		   send_msg(i,mssg.name,mssg.path);
		   OFFlowMod rule = new OFFlowMod();
           rule.setType(OFType.FLOW_MOD); 			
           rule.setCommand(OFFlowMod.OFPFC_ADD);
        
           Host dest=getNextPub();
           Host source=sub2.get(i);
           
           System.out.println("Destination is " +mssg.getGenre() +proxy +proxy.getIP());
           
          
           
           OFMatch match = new OFMatch();
                   match.setDataLayerDestination(dest.getMAC());
                   match.setDataLayerType(Ethernet.TYPE_IPv4);
                   //match.setDataLayerSource(source.getMAC());
                   match.setNetworkDestination(dest.getIp());
                   //match.setNetworkSource(source.getIp());
                 //  match.setNetworkProtocol(IPv4.PROTOCOL_TCP);
                  match.setTransportDestination((short) 80);
                  System.out.println(match.getTransportDestination());
                   
                   
           System.out.println("set match proto= "+match.getNetworkProtocol());
           System.out.println("set match transport port= "+match.getTransportDestination());
           System.out.println("set match sip = " +match.getNetworkSource());   
           System.out.println("set match dip = " +match.getNetworkDestination());            
                   //match.setWildcards(Wildcards.FULL.withNwSrcMask(24).withNwDstMask(8)); 
           System.out.println("Forward direction");
           // match exact match for network destination
           match.setWildcards(OFMatch.OFPFW_NW_TOS | OFMatch.OFPFW_IN_PORT | OFMatch.OFPFW_TP_SRC
        		   | OFMatch.OFPFW_DL_SRC | OFMatch.OFPFW_NW_SRC_ALL
        		   | OFMatch.OFPFW_NW_PROTO);
          // match.setWildcards(OFMatch.OFPFW_IN_PORT );
           
          // match.setWildcards(OFMatch.OFPFW_NW_TOS);
           //match.setWildcards(OFMatch.OFPFW_TP_SRC);
				   rule.setMatch(match);
		   
		System.out.println("Wildcards "+match.getWildcards());
				   
		//set priority
					//rule.setPriority((short) 1);
				   
			//	IDLE_TIMEOUT=Short.parseShort(mssg.getUptime());
				//System.out.println(IDLE_TIMEOUT);
		//set the timeouts for the rules		
				rule.setIdleTimeout(IDLE_TIMEOUT);
				rule.setHardTimeout(HARD_TIMEOUT);
		
		// Set the buffer id to NONE -- implementation artifact
				rule.setBufferId(OFPacketOut.BUFFER_ID_NONE);		

				
		//Initialize list of actions
				ArrayList<OFAction> actions = new ArrayList<OFAction>();	
				
				
		//Add action to rewrite MAC to the MAC of the chosen publisher
				OFAction rewriteMAC = new OFActionDataLayerDestination(dest.getMAC());
				actions.add(rewriteMAC);
				
		// Add action to re-write destination IP to the IP of the chosen server
			    OFAction rewriteIP = new OFActionNetworkLayerDestination(dest.getIp());
				actions.add(rewriteIP);
			
				
		// Add action to output packet
				OFAction outputTo = new OFActionOutput(dest.getPort());
				actions.add(outputTo);	
				
				
		// Add actions to rule
				rule.setActions(actions);
				short actionsLength = (short)(OFActionDataLayerDestination.MINIMUM_LENGTH
						+ OFActionNetworkLayerDestination.MINIMUM_LENGTH
						+ OFActionOutput.MINIMUM_LENGTH);
					
		// Specify the length of the rule structure
				rule.setLength((short) (OFFlowMod.MINIMUM_LENGTH + actionsLength));
				
				logger.debug("Actions length="+ (rule.getLength() - OFFlowMod.MINIMUM_LENGTH));
				
				logger.debug("Install rule for forward direction for flow: " + rule);
					
				try {
					System.out.println("writing rule....1");
					sw.write(rule, null);
				} catch (Exception e) {
					e.printStackTrace();
				}	
				System.out.println("Now push to switch.....1");
	        push(sw, pi, actions, actionsLength);
		
        
	        System.out.println("%%%%%%%%%Source ip is%%%%%%%%%%% : "+match.getNetworkSource());
	        
	        
	 
		OFFlowMod reverseRule = new OFFlowMod();
    	reverseRule.setType(OFType.FLOW_MOD); 			
    	reverseRule.setCommand(OFFlowMod.OFPFC_ADD);
			
		// Create match 
		OFMatch reverseMatch = new OFMatch()
			.setDataLayerSource(dest.getMAC())
			.setDataLayerDestination(source.getMAC())
			.setDataLayerType(Ethernet.TYPE_IPv4)
			.setNetworkSource(dest.getIp())
			.setNetworkProtocol(IPv4.PROTOCOL_TCP)
			//.setTransportDestination((short)80)
			.setNetworkDestination(source.getIp());
        
		// Set wildcards for Network protocol
		reverseMatch.setWildcards(OFMatch.OFPFW_NW_TOS | OFMatch.OFPFW_TP_SRC | OFMatch.OFPFW_TP_DST
				| OFMatch.OFPFW_IN_PORT);
		reverseRule.setMatch(reverseMatch);
			
		// Specify the timeouts for the rule
		reverseRule.setIdleTimeout(IDLE_TIMEOUT);
		reverseRule.setHardTimeout(HARD_TIMEOUT);
	        
	    // Set the buffer id to NONE -- implementation artifact
		reverseRule.setBufferId(OFPacketOut.BUFFER_ID_NONE);
	       
        // Initialize list of actions
		ArrayList<OFAction> reverseActions = new ArrayList<OFAction>();
		
		// Add action to re-write destination MAC to the MAC of the chosen server
		OFAction reverseRewriteMAC = new OFActionDataLayerDestination(source.getMAC());
		reverseActions.add(reverseRewriteMAC);
		
		// Add action to re-write destination IP to the IP of the chosen server
		OFAction reverseRewriteIP = new OFActionNetworkLayerDestination(source.getIp());
		reverseActions.add(reverseRewriteIP);
			
		// Add action to output packet
		OFAction reverseOutputTo = new OFActionOutput(source.getPort());
		reverseActions.add(reverseOutputTo);
		
		// Add actions to rule
		reverseRule.setActions(reverseActions);
		//reverseRule.setPriority((short) 2);
		
		
		// Specify the length of the rule structure
		reverseRule.setLength((short) (OFFlowMod.MINIMUM_LENGTH
				+ OFActionDataLayerSource.MINIMUM_LENGTH
				+ OFActionNetworkLayerSource.MINIMUM_LENGTH
				+ OFActionOutput.MINIMUM_LENGTH));
		
		logger.debug("Install rule for reverse direction for flow: " + reverseRule);
			
		try {
			System.out.println("Writing..rule...2");
			sw.write(reverseRule, null);
		} catch (Exception e) {
			System.out.println("Error");
			e.printStackTrace();
		}
		
		push(sw, pi, actions, actionsLength);
	
	        }
	}
		else
		{
			System.out.println("No subscribers for you!!");
		}
      }
  
        

	private Host getNextPub() {
		// TODO Auto-generated method stub
		
		lastserver = (lastserver + 1) % pub.size();
		return pub.get(lastserver);
		//return null;
	}

	private void send_msg(int i, String name2, String path2) {
		// TODO Auto-generated method stub
		//send stats to subscribers
		
	}

	private void push(IOFSwitch sw, OFPacketIn pin,
			ArrayList<OFAction> actions, short actionsLength) {
		// TODO Auto-generated method stub
		// create an OFPacketOut for the pushed packet
        OFPacketOut po = (OFPacketOut) floodlightProvider.getOFMessageFactory()
                		.getMessage(OFType.PACKET_OUT);        
        System.out.println("Hello4");
        // Update the inputPort and bufferID
        po.setInPort(pin.getInPort());
        po.setBufferId(pin.getBufferId());
                
        // Set the actions to apply for this packet		
		po.setActions(actions);
		po.setActionsLength(actionsLength);
	        
        // Set data if it is included in the packet in but buffer id is NONE
        if (pin.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
            byte[] packetData = pin.getPacketData();
            po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                    + po.getActionsLength() + packetData.length));
            po.setPacketData(packetData);
        } else {
            po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                    + po.getActionsLength()));
        }        
        
        // Push the packet to the switch
        try {
        	System.out.println("Hello5");
            sw.write(po, null);
        } catch (IOException e) {
            logger.error("failed to write packetOut: ", e);
        }
	}

	private void subscribe(Host h, Message mssg,String genre) {
		// TODO Auto-generated method stub
		iph=h.getIp();
		mach=h.getMAC();
		porth=h.getPort();
		
		System.out.println("subscriber ip "+iph);
		System.out.println("Subscriber's genre : "+mssg.getGenre());
		String compare=mssg.getGenre();
		System.out.println(compare);
		if(genre.equals("action"))
		{
			System.out.println("Kya ho raha hai?");
			System.out.println("Subscribers before subscription : "+action_sub.size());
			action_sub.add(new Host(iph,mach,porth));
			System.out.println("subscribers : "+action_sub.size());
			for(int i=0;i<action_sub.size();i++)
				System.out.println("Subscribers :"+action_sub.get(i));
		}
	    else if(mssg.getGenre().equals("contemporary"))
	    {
	    	contemporary_sub.add(new Host(iph,mach,porth));
	    }
	    else if(mssg.getGenre().equals("animation"))
	    {
	    	animation_sub.add(new Host(iph,mach,porth));
	    }
	    else
	       System.out.println("Dunno!!!!It's invalid format"); //the rest of the undefined topics
	}


	private void advertise(Host h, Message mssg) {
		// TODO Auto-generated method stub
		iph=h.getIp();
		mach=h.getMAC();
		porth=h.getPort();
		
		System.out.println("advertiser ip "+iph);
		
		if(mssg.getGenre().equals("action"))
		{
			action_pub.add(new Host(iph,mach,porth));
		    System.out.println("Publishers number : "+action_pub.size());
		//print the potential publishers
			for(int i=0;i<action_pub.size();i++)
				System.out.println("Publishers :"+action_pub.get(i));
		}
	    else if(mssg.getGenre().equals("contemporary"))
	    {
	    	contemporary_pub.add(new Host(iph,mach,porth));
	    }
	    else if(mssg.getGenre().equals("animation"))
	    {
	    	animation_pub.add(new Host(iph,mach,porth));
	    }
	    else
	       System.out.println("Invalid format");
	 }
}
