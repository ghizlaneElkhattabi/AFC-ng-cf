package DisSolver;

import jade.content.lang.Codec;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.Ontology;
import jade.content.onto.basic.Action;
import jade.content.onto.basic.Result;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.JADEAgentManagement.JADEManagementOntology;
import jade.domain.JADEAgentManagement.ShutdownPlatform;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.tools.sniffer.Sniffer;
import jade.util.leap.Iterator;
import jade.util.leap.List;

import java.util.ArrayList;
import java.util.HashMap;



public class MasterABT extends Agent
{
	public String wsdlOperation = "";
	HashMap<String, Statistic> nbrmsg;
	Statistic st = new Statistic();
	int k =0;
	int nbrAgent;
	DFAgentDescription template = new DFAgentDescription();
	ServiceDescription sd = new ServiceDescription();
	
	DFAgentDescription[] result = null;
	MessageTemplate nogood = MessageTemplate.MatchPerformative(Message.FC_NOGOOD);//message with type NOgoodd
	MessageTemplate stop = MessageTemplate.MatchPerformative(Message.FC_STOP);
	ACLMessage msgNOGOOD, MsgStop;
	long tempsT1, tempsT2;
	
	protected void setup()
	{
		tempsT1 = System.currentTimeMillis();
		nbrAgent  = Integer.parseInt((String)getArguments()[0]);
		sd.setType("Meeting");
		template.addServices(sd);
		nbrmsg = new HashMap<String, Statistic>();
		System.out.println("\n\n Solving AFC_ng...");
		addBehaviour(new TickerBehaviour(this, 3000) {
			protected void onTick() {
				System.out.println(".");
				try {
					result = DFService.search(myAgent, template);
				} catch (FIPAException e) {
					e.printStackTrace();
				}
				if(nbrAgent <= result.length)
				{
					ArrayList<String> agents = new ArrayList<String>();
					ArrayList<String> agentsCopy = new ArrayList<String>();
					for (int i = 0; i < result.length ; i++) 
					{
						agents.add(result[i].getName().getLocalName());
						agentsCopy.add(result[i].getName().getLocalName());
					}
					for (int i = 0; i < result.length ; i++) 
					{
						st.IncrementMSGs();
						ACLMessage start;
						agents.remove(i);
						if(Tools.ExtractInteger(result[i].getName().getLocalName()) == 1)
						{
							start =   Message.CreatMessage(Message.START, result[i].getName().getLocalName(), "Start AFC", "IA",agents.toString());
						}
						else 
						{
							start =   Message.CreatMessage(Message.START, result[i].getName().getLocalName(), "Start AFC", "",agents.toString());
						}
						agents.clear();
						for(String agt : agentsCopy)
						{
							agents.add(agt);
						}
						send(start);
						start.clearAllReceiver(); 
					}
					myAgent.addBehaviour(new linking());
					myAgent.removeBehaviour(this);
				}
				
			}
		} );
	}
	
	protected void takeDown() //on delete
	{
		Codec codec = new SLCodec();    
		Ontology jmo = JADEManagementOntology.getInstance();
		getContentManager().registerLanguage(codec);
		getContentManager().registerOntology(jmo);
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(getAMS());
		msg.setLanguage(codec.getName());
		msg.setOntology(jmo.getName());
		try {
			getContentManager().fillContent(msg, new Action(getAID(), new ShutdownPlatform()));
		    send(msg);
		}
		catch (Exception e) {}
	}
	
	
	
	
	
	private class linking extends CyclicBehaviour 
	{
		@Override
		public void action() {
		//	myAgent.removeTimer(this);
			if(myAgent != null)
			{
			msgNOGOOD = myAgent.receive(nogood);
			MsgStop = myAgent.receive(stop);
			if (msgNOGOOD!= null || MsgStop!= null) {
				try {
					addBehaviour(new states());
					result = DFService.search(myAgent, template);
					ACLMessage stop ;//message with type stop // from master!!
					for (int i = 0; i < result.length ; i++) //le dernier n'a pas des fils
					{
						st.IncrementMSGs();
						stop = Message.CreatMessage(Message.FC_STATISTICS, result[i].getName().getLocalName(), "Stop : Pas de Solution.!!\n", "","");
						send(stop);
						stop.clearAllReceiver();
					}
				} catch (FIPAException e) {
					e.printStackTrace();
				}
			}
			}
	}
	

	public int Listcontain(ArrayList<String> table, String value) {
		int i = 0;
		for (String s : table) {
			if (s.contains(value)) {
				return i;
			}
			i++;
		}
		return -1;
	}
	
	private class states extends CyclicBehaviour 
	{
		MessageTemplate statistics = MessageTemplate.MatchPerformative(Message.FC_STATISTICS);//message with type NOgoodd
		ACLMessage msgStatistics;
		HashMap<String, Statistic> nbrmsg = new HashMap<String, Statistic>();
		public void action() {
			msgStatistics = myAgent.receive(statistics);
			if(msgStatistics!=null)
			{	
				nbrmsg.put(msgStatistics.getSender().getLocalName(), new Statistic(msgStatistics.getOntology()));
				if(nbrmsg.size() >= result.length)
				{
					k = st.getMSGs();
					st.setCstrs(0);
					for(String agt : nbrmsg.keySet())
					{
						st.setMSGs(st.getMSGs()+nbrmsg.get(agt).getMSGs());
						st.setCstrs(st.getCstrs()+nbrmsg.get(agt).getCstrs());
					}
//					System.err.println("No solution found!\n");
					for(String agt : nbrmsg.keySet())
					{
						System.out.println(agt+"   :    " + nbrmsg.get(agt));
					}
					System.out.println("\n\nTime of resolution in millisecond "+(System.currentTimeMillis()-tempsT1)+"  ms ");
					System.out.println("Number of messages :       "+st.getMSGs());
					System.out.println("Number of tested constraints : "+st.getCstrs()+"\n");
					takeDown();
					removeBehaviour(this);
				}
			}
		}
	}
	}
}

