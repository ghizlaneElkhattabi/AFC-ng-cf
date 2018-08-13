package DisSolver;

import java.util.ArrayList;
import java.util.HashMap;

public class Variable {
	String name="";
	int value;
	boolean external = false;
	
	public Variable()
	{
		name = "A0.0";
		value = 0;
	}
	
	public Variable(String var)
	{
		this.name = var.split("\\:")[0];
		this.value = Integer.parseInt(var.split("\\:")[1]);
	}
	
	public Variable(String name, int value)
	{
		this.name = name;
		this.value = value;
	}
	
	public Variable(String variable, String external)
	{
		this.name = variable.split("\\:")[0];
		this.value = Integer.parseInt(variable.split("\\:")[1]);
		if(external.equals("true"))
		{
			this.external = true;
		}
		else
		{
			this.external = false;
		}
		
	}
	
	public Variable(String name, int value, boolean external)
	{
		this.name = name;
		this.value = value;
		this.external = external;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getValue()
	{
		return value;
	}
	
	public int getNumberOfConnectedLowerAgents(HashMap<String, ArrayList<jConstraint>> LowerAgentConstraints){
		int N=0;
		for(String agent : LowerAgentConstraints.keySet())
		{
			for(jConstraint c : LowerAgentConstraints.get(agent))
			{
				if(Tools.StringListContain(c.getVariables(), this.getName()))
				{
					N++;
					break;
				}
			}
		}
		return N;
	}
	
	public int getNumberOfLowerConstraints(HashMap<String, ArrayList<jConstraint>> LowerAgentConstraints){
		int N=0;
		for(String agent : LowerAgentConstraints.keySet())
		{
			for(jConstraint c : LowerAgentConstraints.get(agent))
			{
				if(Tools.StringListContain(c.getVariables(), this.getName()))
				{
					N++;
				}
			}
		}
		return N;
	}

	public ArrayList<String> getListOfLowerConnectedAgents(HashMap<String, ArrayList<jConstraint>> LowerAgentConstraints){
		ArrayList<String> Agents = new ArrayList<String>();
		for(String agent : LowerAgentConstraints.keySet())
		{
			for(jConstraint c : LowerAgentConstraints.get(agent))
			{
				if(Tools.StringListContain(c.getVariables(), this.getName()))
				{
					Agents.add(agent);
					break;
				}
			}
		}
		return Agents;
	}
	
	public Boolean isEqual(Variable v)
	{
		return ((this.getName().equals(v.getName())) && (this.getValue() == v.getValue()));
	}
	
	public String getAgentOwner(String name)
	{
		name += Tools.ExtractInteger(this.getName());
		return name;
	}
	
	public boolean isExternal()
	{
		return external;
	}
	
	public String toString()
	{
		return this.name+":"+this.value;
	}
}
