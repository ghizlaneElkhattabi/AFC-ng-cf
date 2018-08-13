package DisSolver;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class AgentABT extends Agent {
	Boolean start = false;
	SimpleAgent agt;
	Object[] args;
	boolean consistent;
    ArrayList<AgentView> CPA = new ArrayList<AgentView>();
    int nbrAgents = 3;
    int counter;


    protected void setup() 
	{
    	args = getArguments();
    	agt = new SimpleAgent(this.getLocalName(), (String) args[0]);
    	initAgentView();
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName("Meeting");
		sd.setType("Meeting");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}
		agt.setMyValue(null);
		addBehaviour(new CyclicBehaviour() {
			@Override
			public void action() 
			{
				removeTimer(this);
                MessageTemplate StartTemp = MessageTemplate.MatchPerformative(Message.START);
				ACLMessage Start =  myAgent.receive(StartTemp);
				if (Start!= null) 
				{
					agt.setHigherPriorityAgents(Tools.toArrayList(Start.getProtocol()));
					agt.setLowerPriorityAgents(Tools.toArrayList(Start.getProtocol()));
					agt.getStatistics().IncrementMSGs();
					if(Start.getOntology().equals("IA"))
					{
						Assign();
					}
					addBehaviour(new MainReception());
					removeBehaviour(this);
				}
			}
		}); 
		// ---------------------------------------------------
	}
	
	
	private class MainReception extends CyclicBehaviour
	{
		MessageTemplate CPA = MessageTemplate.MatchPerformative(Message.FC_CPA); 
		MessageTemplate ngd = MessageTemplate.MatchPerformative(Message.FC_NOGOOD);
		MessageTemplate stop = MessageTemplate.MatchPerformative(Message.FC_STOP);
		MessageTemplate Stats  = MessageTemplate.MatchPerformative(Message.FC_STATISTICS);
														
		ACLMessage msgCPA, msgNOGOOD, msgSTOP,msgSTATS;

		@Override
		public void action() 
		{
			if(myAgent != null)
			{
			msgSTATS = myAgent.receive(Stats);
			if(msgSTATS != null)
			{
				ACLMessage cfp = Message.CreatMessage(Message.FC_STATISTICS, "MASTER", "silence", ""+agt.getStatistics().getMSGs()+";"+agt.getStatistics().getCstrs(),"");
				send(cfp);
				cfp.clearAllReceiver();
			}
			
			msgCPA = myAgent.receive(CPA);
			if (msgCPA != null) {
				agt.getStatistics().IncrementMSGs();
				String title = msgCPA.getContent();
				System.out.println("\n(" + msgCPA.getSender().getLocalName()	+ " --> " + getAID().getLocalName() + ") : CPA : "+ title + "\n");
				ProcessCPA(msgCPA.getSender().getLocalName(),msgCPA.getContent());
			}

			msgNOGOOD = myAgent.receive(ngd);
			if ((msgNOGOOD != null)&&(msgNOGOOD.getContent().contains(">"))) 
			{
				System.out.println("\n(" + msgNOGOOD.getSender().getLocalName()	+ " --> " + getAID().getLocalName() + ") : ngd : "+ msgNOGOOD.getContent() + "\n");
				agt.getStatistics().IncrementMSGs();
				ProcessNogood(msgNOGOOD.getContent(), msgNOGOOD.getSender().getLocalName());
			}
			
			msgSTOP = myAgent.receive(stop);
			if (msgSTOP != null) {
				agt.getStatistics().IncrementMSGs();
				System.out.println(agt.getMyValue().getAgentOwner(agt.getAgentsName())+"    --->    "+agt.getMyValue().toString()+"           {"+agt.getStatistics()+"}");
				takeDown();
				removeBehaviour(this);
			}
			}
		}
	}
	
	public void sortDomain(Solution s)
	{
		ArrayList<Solution> newDom = new ArrayList<Solution>();
		for(int i=0; i<agt.Domain.size()-1; i++)
		{
			for(int j=i+1; j<agt.Domain.size(); j++)
			{
//				hamming distance
				if(agt.Domain.get(j).getHammingDistance(s)<agt.Domain.get(i).getHammingDistance(s))
				{
					Solution tmp = agt.Domain.get(i);
					agt.Domain.set(i, agt.Domain.get(j));
					agt.Domain.set(j, tmp);
				}
				else if(agt.Domain.get(j).getHammingDistance(s)==agt.Domain.get(i).getHammingDistance(s))
				{
//					number of lower agents connected with changed variables
					if(agt.Domain.get(j).getNbOfLowerAgentsOfChangedVars(s, agt.getChildrenConstraints())<agt.Domain.get(i).getNbOfLowerAgentsOfChangedVars(s, agt.getChildrenConstraints()))
					{
						Solution tmp = agt.Domain.get(i);
						agt.Domain.set(i, agt.Domain.get(j));
						agt.Domain.set(j, tmp);
					}
					else if(agt.Domain.get(j).getNbOfLowerAgentsOfChangedVars(s, agt.getChildrenConstraints())==agt.Domain.get(i).getNbOfLowerAgentsOfChangedVars(s, agt.getChildrenConstraints()))
					{
//						number of lower constraints with changed variables
						if(agt.Domain.get(j).getNbOfLowerConstraintsOfChangedVars(s, agt.getChildrenConstraints())<agt.Domain.get(i).getNbOfLowerConstraintsOfChangedVars(s, agt.getChildrenConstraints()))
						{
							Solution tmp = agt.Domain.get(i);
							agt.Domain.set(i, agt.Domain.get(j));
							agt.Domain.set(j, tmp);
						}
						else if(agt.Domain.get(j).getNbOfLowerConstraintsOfChangedVars(s, agt.getChildrenConstraints())==agt.Domain.get(i).getNbOfLowerConstraintsOfChangedVars(s, agt.getChildrenConstraints()))
						{
//							Lower connected agents
//							System.out.println("+++++  " +s+"    "+ agt.Domain.get(i)+"  ("+agt.Domain.get(i).getNbOfLowerAgentsOfChangedVars(s, agt.getChildrenConstraints())+","+agt.Domain.get(i).getNbOfLowerConstraintsOfChangedVars(s, agt.getChildrenConstraints())+","+agt.Domain.get(i).getSortedListOfLowerAgentsOfChangedVars(s, agt.getChildrenConstraints())+")"+"     "+agt.Domain.get(j)+"  ("+agt.Domain.get(j).getNbOfLowerAgentsOfChangedVars(s, agt.getChildrenConstraints())+","+agt.Domain.get(j).getNbOfLowerConstraintsOfChangedVars(s, agt.getChildrenConstraints())+","+agt.Domain.get(j).getSortedListOfLowerAgentsOfChangedVars(s, agt.getChildrenConstraints())+")");
							if(agt.Domain.get(j).getSortedListOfLowerAgentsOfChangedVars(s, agt.getChildrenConstraints()).toString().compareTo(agt.Domain.get(i).getSortedListOfLowerAgentsOfChangedVars(s, agt.getChildrenConstraints()).toString())>0)
							{
								Solution tmp = agt.Domain.get(i);
								agt.Domain.set(i, agt.Domain.get(j));
								agt.Domain.set(j, tmp);
							}
						}
					}
				}
			}
		}
	}
	
	public void initAgentView()
	{
		for(int i= 1; i<Tools.ExtractInteger(agt.getMyName()); i++)
		{
			agt.AddToAgentView(new AgentView(agt.getAgentsName()+i, new Solution(), 0));
		}
	}
	
	public void ProcessCPA(String agent, String content)
	{
//		System.out.println("*****  " + agent+"   "+content);
		ArrayList<AgentView> CPA = toCPA(content);
		if(isStronger(toCPA(content)))
		{
			UpdateAgentView(CPA, false);
			consistent = true;
			Revise();
			if(isDomainEmpty())
			{
				Backtrack();
			}
			else
			{
				checkAssign(agent);
			}
		}
		else
		{
//			System.err.println("isn't stronger  " + content+"    "+agt.getAgentView());
		}
	}
	
	public void UpdateAgentView (ArrayList<AgentView> CPA, boolean SolIsNull)
	{
		for(AgentView av : CPA)
		{
			agt.UpdateAgentView(av.getAgent(), av.getSolution(), av.getCounter(), SolIsNull);
			agt.removeIncompatiblesNogoods(av.getSolution(), SolIsNull);
		}
		
	}
	
	public void Revise()
	{
		for(Solution v : agt.getDomain())
		{
			ArrayList<Solution> inconsistentSolutions = v.isConsistent(agt.getAgentView(), agt.getParentsConstraints());
			agt.getStatistics().IncrementCstrs(v.getNbrConstraintsTested());
			Nogood ngd1 = v.isEliminated(agt.nogoodStore);
			Nogood ngd2 = new Nogood();
			if(inconsistentSolutions != null )
			{
//				enregistrer le meilleur nogood
				Solution s = inconsistentSolutions.get(0);
				for(Solution sol : inconsistentSolutions)
				{
					if(Tools.ExtractInteger(sol.getAgentOwner(agt.getAgentsName())) < Tools.ExtractInteger(s.getAgentOwner(agt.getAgentsName())))
					{
						s = sol;
					}
				}
				ngd2.AddToLhs(s);
				if(ngd1 != null)
				{
					ArrayList<Solution> LHS = ngd1.getLHS();
					if(LHS == null)
					{
						ngd2.setLHS(null);
					}
					else
					{
						Solution s1 = LHS.get(0);
						for(Solution s2 : LHS)
						{
							if(Tools.ExtractInteger(s2.getAgentOwner(agt.getAgentsName())) < Tools.ExtractInteger(s1.getAgentOwner(agt.getAgentsName())))
							{
								s1 = s2;
							}
						}
						if(Tools.ExtractInteger(s1.getAgentOwner(agt.getAgentsName())) < Tools.ExtractInteger(s.getAgentOwner(agt.getAgentsName())))
						{
							ngd2.setLHS(LHS); 
						}
					}
				}
				
//				System.out.println(agt.getMyName()+" -----    "+s);
				ngd2.setRHS(v);
				agt.AddNPIsolutiontoNgdStore(ngd2, ngd2.getLHS().get(0).getAgentOwner(agt.AgentsName));
				agt.getNogoodStores().add(ngd2);
			}
		}
	}
	
	public ArrayList<AgentView> toCPA(String cpa)
	{
		ArrayList<AgentView> CPA = new ArrayList<AgentView>();
		cpa = cpa.substring(1, cpa.length()-1);
		for(String str : cpa.split("\\, "))
		{
			CPA.add(new AgentView(str));
		}
		return CPA;
	}
	
	public boolean isStronger(ArrayList<AgentView> CPA)
	{
		if(agt.getAgentView().size()>0)
		{
			for(int i=0; i<CPA.size(); i++)
			{
				if(CPA.get(i).isObsolet(agt.getAgentView().get(i)))
				{
					return false;
				}
			}
		}
		return true;
	}
	
	
	public void Backtrack()
	{
		agt.setNogood(agt.ConstructNogood());
		if(agt.getNogood() != null)
		{
			for(int i= Tools.ExtractInteger(agt.getNogood().getAgentTarget(agt.getAgentsName()))+1; i<Tools.ExtractInteger(agt.getMyName())-1; i++)
			{
				System.out.println(agt.myname+"    "+i+"     "+agt.AgentView);
				agt.AgentView.get(i).setSolution(new Solution());
			}
//			agt.UpdateAgentView(agt.getNogood().getAgentTarget(agt.getAgentsName()), agt.getNogood().getRHS(), 0, true);
			agt.removeIncompatiblesNogoods(agt.getNogood().getRHS(), false);
			consistent = false;
//			agt.setMyValue(new Solution());
			ACLMessage cfp = Message.CreatMessage(Message.FC_NOGOOD, agt.getNogood().getAgentTarget(agt.getAgentsName()), agt.getNogood().toString(), "","");
			send(cfp);
			cfp.clearAllReceiver();
		}
		else
		{
			ACLMessage cfp = Message.CreatMessage(Message.FC_NOGOOD, "MASTER", "", "","");
			send(cfp);
			cfp.clearAllReceiver();
		}
	}
	
	public void checkAssign(String agent)
	{
		if(agt.getPredecessor().equals(agent))
		{
			Assign();
		}
	}
	
	
	public void ProcessNogood(String Content, String agent)
	{
		Nogood ngd = new Nogood(Content);
		if(agt.isNogoodCompatibleWithAgentView(new Nogood(ngd.getLHS(), new Solution())) != null)
		{
			int i = ngd.getRHS().Eliminated(agt.getNogoodStores());
			if(i != -1)
			{
//				System.out.println("*****    "+ ngd+"   "+agt.getNogoodStores().getNogoods().get(i)+"   "+ Tools.ExtractInteger(ngd.getLHS().get(ngd.getLHS().size()-1).getAgentOwner(agt.getAgentsName())) +"       "+Tools.ExtractInteger(agt.getNogoodStores().getNogoods().get(i).getLHS().get(agt.getNogoodStores().getNogoods().get(i).getLHS().size()-1).getAgentOwner(agt.getAgentsName()))+"      ");
				if(ngd.getLHS().size()==0 
						|| 
						Tools.ExtractInteger(ngd.getLHS().get(ngd.getLHS().size()-1).getAgentOwner(agt.getAgentsName())) < Tools.ExtractInteger(agt.getNogoodStores().getNogoods().get(i).getLHS().get(agt.getNogoodStores().getNogoods().get(i).getLHS().size()-1).getAgentOwner(agt.getAgentsName()))
						||
						(Tools.ExtractInteger(ngd.getLHS().get(ngd.getLHS().size()-1).getAgentOwner(agt.getAgentsName())) == Tools.ExtractInteger(agt.getNogoodStores().getNogoods().get(i).getLHS().get(agt.getNogoodStores().getNogoods().get(i).getLHS().size()-1).getAgentOwner(agt.getAgentsName()))
						 &&
						 ngd.getLHS().size() <  agt.getNogoodStores().getNogoods().get(i).getLHS().size()
						 )
						 )
				{
					agt.getNogoodStores().set(i, ngd);
				}
			}
			else
			{
				agt.getNogoodStores().add(ngd);
			}
//			System.out.println("accepted     "+ngd);
//			agt.getNogoodStores().add(ngd);
			if(agt.getMyValue().isContain(ngd.getRHS()) )
			{
				Assign();
			}
		}
		else
		{
			System.err.println("refused   "+ngd);
		}
	}
	
	
	public void Assign()
	{
		if(agt.myvalue!= null)
		{
//			sortDomain(agt.myvalue);
		}
		if(!isDomainEmpty())
		{
			agt.setMyValue(agt.ChooseValue());
			counter++;
			CPA = new ArrayList<AgentView>();
			for(AgentView av : agt.getAgentView())
			{
				CPA.add(av);
			}
			CPA.add(new AgentView(agt.getMyName(), agt.getMyValue(), counter));
			sendCPA();
		}
		else{
			System.out.println("domain is empty "+isDomainEmpty());
			Backtrack();
		}
	}
	
	public boolean isDomainEmpty()
	{
		for(int i=0; i<agt.getDomain().size(); i++)
		{
			if(agt.getDomain().get(i).isEliminated(agt.getNogoodStores()) == null)
			{
				return false;
			}
		}
		return true;
	}
	
	public void sendCPA()
	{
//		System.out.println("+++  " +CPA.size()+"    "+nbrAgents);
		if(CPA.size() == nbrAgents)
		{
			System.out.println(" finished   ");
			for(AgentView av : CPA)
			{
				System.err.println(" * " + av);
			}
			ACLMessage cfp = Message.CreatMessage(Message.FC_STOP, "MASTER", "", "","");
			send(cfp);
			cfp.clearAllReceiver();
			return;
		}
		else
		{
			ACLMessage cfp = Message.CreatMessage(Message.FC_CPA, agt.getLowerPriorityAgents().get(0), CPA.toString(), "","");
			send(cfp);
			for (int i=0; i < agt.getChildren().size(); i++)  
			{
				if(!agt.getChildren().get(i).equals(agt.getLowerPriorityAgents().get(0)))
				{
				cfp = Message.CreatMessage(Message.FC_CPA, agt.getChildren().get(i), CPA.toString(), "","");
				send(cfp);
				}
			}
			return;
		}
	}
	
	
}
