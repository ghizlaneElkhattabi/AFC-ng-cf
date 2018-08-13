package DisSolver;

import java.util.ArrayList;
import java.util.HashMap;

public class Solution {
	int nbVariables;
	Variable[] variables ;
//	Justification justification ;
	int nbrCstrsTested;
	
	public Solution() {
		nbrCstrsTested = 0;
	}
	
	public Solution(int nbVar)
	{
		this.nbVariables = nbVar;
		this.variables = new Variable[nbVar];
		nbrCstrsTested = 0;
	}
	
//	sol = (x1.1:1 , x1.2:2)
	public Solution(String sol)
	{
		if(sol.contains("(") && sol.contains(")"))
		sol = sol.substring(1, sol.length()-1);
		this.nbVariables = sol.split("\\,").length;
		this.variables = new Variable[this.nbVariables];
		
		for(int i = 0; i<this.nbVariables; i++)
		{
			this.variables[i] = new Variable(sol.split("\\,")[i]);
		}
		nbrCstrsTested = 0;
	}
	
	public Solution(Solution sol)
	{
		nbrCstrsTested = 0;
		this.nbVariables = sol.getnbVariables();
		this.variables = new Variable[this.nbVariables];
		for(int i = 0; i<this.nbVariables; i++)
		{
			this.variables[i] = sol.getVariables()[i];
		}
	}
	public int getnbVariables()
	{
		return nbVariables;
	}
	
	public Variable[] getVariables()
	{
		return variables;
	}
	
	public int getInstanciation(String nameOfVariable)
	{
		for(int i=0; i<this.variables.length; i++)
		{
			if(variables[i].getName().equals(nameOfVariable))
			{
				return variables[i].getValue();
			}
		}
		return -1;
	}
	
	public Variable getVariable(String name)
	{
		for(Variable str : this.getVariables())
		{
			if(str.getName().equals(name))
			{
				return str;
			}
		}
		return null;
	
	}
	
	public int getNbrConstraintsTested()
	{
		return nbrCstrsTested;
	}
	
	public int getHammingDistance(Solution s)
	{
		int N=0;
		if(this.isEqual(s))
		{
			return this.getnbVariables()+1;
		}
		for(int i=0; i<this.getnbVariables(); i++)
		{
			if(this.getVariables()[i].getValue() != s.getVariables()[i].getValue())
			{
				N++;
			}
		}
		return N;
	}
	
	public int getNbOfLowerAgentsOfChangedVars(Solution s, HashMap<String, ArrayList<jConstraint>> LowerAgentConstraints)
	{
		int N=0;
		for(int i=0; i< this.getVariables().length; i++)
		{
			if(!this.getVariables()[i].isEqual(s.getVariables()[i]))
			{
				N += this.getVariables()[i].getNumberOfConnectedLowerAgents(LowerAgentConstraints);
			}
		}
		return N;
	}
	
	public int getNbOfLowerConstraintsOfChangedVars(Solution s, HashMap<String, ArrayList<jConstraint>> LowerAgentConstraints)
	{
		int N=0;
		for(int i=0; i< this.getVariables().length; i++)
		{
			if(!this.getVariables()[i].isEqual(s.getVariables()[i]))
			{
				N += this.getVariables()[i].getNumberOfLowerConstraints(LowerAgentConstraints);
			}
		}
		return N;
	}
	
	public ArrayList<String> getSortedListOfLowerAgentsOfChangedVars(Solution s, HashMap<String, ArrayList<jConstraint>> LowerAgentConstraints){
		ArrayList<String> Agents = new ArrayList<String>();
		for(int i=0; i< this.getVariables().length; i++)
		{
			if(!this.getVariables()[i].isEqual(s.getVariables()[i]))
			{
				for(String agt : this.getVariables()[i].getListOfLowerConnectedAgents(LowerAgentConstraints))
				{
					if(!Tools.ListcontainExact(Agents, agt))
					{
						Agents.add(agt);
					}
				}
			}
		}
		Agents = Tools.SortStrings(Agents);
		return Agents;
	}
	
	public void ClearNbrConstraints()
	{
		nbrCstrsTested = 0;
	}
	
	public void setVariables(Variable[] var)
	{
		
		this.variables = new Variable[var.length];
		
		for(int i=0; i<var.length; i++)
		{
			this.variables[i] = var[i];
		}
	}
	
	public void setVariables(String[] var)
	{
		
		this.variables = new Variable[var.length];
		
		for(int i=0; i<var.length; i++)
		{
			this.variables[i] = new Variable(var[i]);
		}
	}
	
	public ArrayList<Variable>getExternalVariables()
	{
		
		ArrayList<Variable> ExternalVars = new ArrayList<Variable>();
		for(Variable var : this.getVariables())
		{
			if(var.isExternal())
			{
				ExternalVars.add(var);
			}
		}
		Tools.Sort(ExternalVars);
		return ExternalVars;
	}
	
	public boolean containSameExternalVariableValues(Solution sol)
	{
		if(this.getExternalVariables().size() != sol.getExternalVariables().size())
		{
			return false;
		}
		for(int i = 0; i<this.getExternalVariables().size(); i++)
		{
			if(!this.getExternalVariables().get(i).isEqual(sol.getExternalVariables().get(i)))
			{
				return false;
			}
		}
		
		return true;
	}
	
	public boolean isNeighbourhoodPartialInterchangeable(Solution sol, ArrayList<String> variablesPart)
	{
//		if(this.getExternalVariables().size() != sol.getExternalVariables().size())
//		{
//			return false;
//		}
		for(int i = 0; i<this.getExternalVariables().size(); i++)
		{
			if(Tools.StringListContain(variablesPart, this.getExternalVariables().get(i).getName()))
			{
				if(!this.getExternalVariables().get(i).isEqual(sol.getExternalVariables().get(i)))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	public Boolean ContainNameOfAndChanged(Solution s, String name)
	{
		if(!this.getAgentOwner(name).equals(s.getAgentOwner(name)))
		{
			return false;
		}
		if(this.getAgentOwner(name).equals(s.getAgentOwner(name)) && !this.isContain(s))
		{
			return true;
		}
//		for(int i=0; i<nbVariables; i++)
//		{
//			if(this.getVariables()[i].getName().equals(s.getVariables()[i].getName()) && (this.getVariables()[i].getValue()!= s.getVariables()[i].getValue()))
//			{
//				return true;
//			}
//		}
		return false;
	}
	
//	public ArrayList<Variable> getAllArguments()
//	{
//		ArrayList<Variable> argements = new ArrayList<Variable>();
//		for(Variable v : justification.getJustifOK())
//		{
//			argements.add(v);
//		}
//		for(Variable v : justification.getJustifNGD())
//		{
//			argements.add(v);
//		}
//		return argements;
//	}
	
	
//	public ArrayList<Variable> addArgumentstoNogoodList(ArrayList<Variable> NogoodList)
//	{
//		for(Variable v : justification.getJustifOK())
//		{
//			if (!Tools.ListContainExactV(NogoodList, v))
//			{
//				NogoodList.add(v);
//			}
//		}
//		for(Variable v : justification.getJustifNGD())
//		{
//			if (!Tools.ListContainExactV(NogoodList, v))
//			{
//				NogoodList.add(v);
//			}
//		}
////		System.out.println("********************   " + NogoodList);
//		return NogoodList;
//	}
//	
	
	public String JustVarstoString()
	{
		String str="(";
		for(int i=0; i<nbVariables; i++)
		{
			str+=this.variables[i]+",";
		}
		return str.substring(0, str.length()-1)+")";
	}
	
	public boolean isEqual(Solution sol)
	{
		if(this.getnbVariables() != sol.getnbVariables())
		{
			return false;
		}
		for(int i=0; i<this.getnbVariables(); i++)
		{
			if(sol.getVariable(this.getVariables()[i].getName()) == null || !this.getVariables()[i].isEqual(sol.getVariable(this.getVariables()[i].getName())))
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean isContain(Solution sol)
	{
		for(int i=0; i<sol.getnbVariables(); i++)
		{
			boolean exist = false;
			for(int j=0; j<this.getnbVariables(); j++)
			{
				if(sol.getVariables()[i].isEqual(this.getVariables()[j]) )
				{
					exist = true;
					break;
				}
			}
			if(!exist)
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean hasSameNamesOfVariables(Solution sol)
	{
//		if(this.getnbVariables() != sol.getnbVariables())
//		{
//			return false;
//		}
		for(int i=0; i<this.getnbVariables(); i++)
		{
			if(!this.getVariables()[i].getName().equals(sol.getVariables()[i].getName()))
			{
				return false;
			}
		}
		return true;
	}
	
	public boolean isChanged(Solution sol)
	{
		for(int i=0; i<this.getnbVariables(); i++)
		{
			if(this.getVariables()[i].getName().equals(sol.getVariables()[i].getName()))
			{
				if(this.getVariables()[i].getValue() != sol.getVariables()[i].getValue())
				{
					return true;
				}
			}
			else
			{
				System.err.println("isn't the same");
			}
		}
		return false;
	}
	
	public String getAgentOwner(String name)
	{
		return this.getVariables()[0].getAgentOwner(name);
	}
	
	public ArrayList<Solution> isConsistent(ArrayList<AgentView> agentView, HashMap<String, ArrayList<jConstraint>> ParentConstrainte)
	{
		nbrCstrsTested = 0;
		ArrayList<Solution> solInconsistents = new ArrayList<Solution>();
		for(AgentView agtView : agentView)
		{
			if((ParentConstrainte.containsKey(agtView.getAgent())) && (ParentConstrainte.get(agtView.getAgent()).size()>0))
			{
					for (jConstraint cons : ParentConstrainte.get(agtView.getAgent())){
						nbrCstrsTested++;
					    Variable variable1 = new Variable(), variable2 = new Variable();
					    for(int k = 0;k<cons.getVariables().length;k++)
					    {
					    	if(Tools.contain(this.getVariables(), cons.getVariables()[k]))
					    	{
					    		variable2 = new Variable(cons.getVariables()[k], getInstanciation(cons.getVariables()[k]));
					    	}
					    	else
					    	{
					    		variable1 = agtView.getSolution().getVariable(cons.getVariables()[k]);
					    	}
					    }
					    if(variable1 != null && variable2 != null && !cons.isSatisfied(variable1, variable2))
					    {
					    	solInconsistents.add(agtView.getSolution());
					    }
					}
			}
		}
		if(solInconsistents.size() == 0)
		{
			return null;
		}
		else
		{
			return solInconsistents;
		}
		
	}
	
	public Nogood isEliminated(NogoodStore ns)
	{
		for(Nogood ngd : ns.getNogoods())
		{
			nbrCstrsTested++;
			if(this.isContain(ngd.getRHS()))
			{
				return ngd;
			}
		}
		return null;
	}
	
	public String toString()
	{
		String str = "";
		for (Variable Instans : this.getVariables()) 
		{
			str += Instans.toString() + ",";
		}
		if(str.length()>0)
		str = str.substring(0, str.length() - 1);
		return str;
	}
	public int Eliminated(NogoodStore ns)
	{
		for(int i=0; i<ns.getNogoods().size(); i++)
		{
			if(this.isContain(ns.getNogoods().get(i).getRHS()))
			{
				return i;
			}
		}
		return -1;
	}
	
}
