package DisSolver;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;



public class Message {
	String Sender;
	String Type;
	String Content;
	MessageTemplate Msg;
	ACLMessage ACLMsg;
	
	//ABT messages
	public static final int OK = ACLMessage.INFORM;
	public static final int NOGOOD = ACLMessage.CANCEL;
	public static final int ADL = ACLMessage.REQUEST;
	public static final int START = ACLMessage.PROPAGATE;
	public static final int STOP = ACLMessage.UNKNOWN;
	public static final int STATISTICS = ACLMessage.PROXY;
	public static final int SILENCE = ACLMessage.CONFIRM;
	public static final int DISCONFIRMSILENCE = ACLMessage.DISCONFIRM;
	
	//AFC messages 
	public static final int NOT_OK = ACLMessage.NOT_UNDERSTOOD;
	public static final int FC_NOGOOD = ACLMessage.FAILURE;
	public static final int FC_CPA = ACLMessage.PROPOSE;
	public static final int FC_STOP = ACLMessage.AGREE;
	public static final int FC_STATISTICS = ACLMessage.REQUEST_WHEN;
	
	public Message(int type)
	{
		Msg = MessageTemplate.MatchPerformative(type);
	}
	
	public static ACLMessage CreatMessage(int type, String Agent, String Content, String Ontology, String MoreInformations)
	{
		ACLMessage cfp = new ACLMessage(type);
		cfp.setConversationId("Meeting Information");
		cfp.setReplyWith("cfp" + System.currentTimeMillis());
		cfp.addReceiver(new AID(Agent, AID.ISLOCALNAME));
		cfp.setContent(Content);
		cfp.setOntology(Ontology);
		cfp.setProtocol(MoreInformations);
		return cfp;
	}
	
}
