package DisSolver;

public class AgentView {
	String agent;
	Solution solution;
	int counter; 
	
	AgentView()
	{
		this.agent = "";
		this.solution = new Solution();
		counter = 0;
	}
	
	
	AgentView(String agt, Solution s)
	{
		this.agent = agt;
		this.solution = new Solution(s);
		counter = 0;
	}
	
	AgentView(String agt, Solution s, int counter)
	{
		this.agent = agt;
		this.solution = new Solution(s);
		this.counter = counter;
	}
	
//	AgentView = agent;(solution){counter}
	AgentView(String AgentView)
	{
		this.agent = AgentView.split("\\;")[0];
		if(AgentView.split("\\;")[1].split("\\{")[0].equals(""))
		{
			this.solution = new Solution();
		}
		else
		{
			this.solution = new Solution(AgentView.split("\\;")[1].split("\\{")[0]);
		}
		counter = Integer.parseInt(AgentView.split("\\{")[1].substring(0, AgentView.split("\\{")[1].length()-1));
	}
	
	public String getAgent()
	{
		return this.agent;
	}
	
	public Solution getSolution()
	{
		return this.solution;
	}
	
	public int getCounter()
	{
		return counter;
	}
	
	public void setSolution(Solution sol)
	{
		this.solution = new Solution(sol);
	}
	
	public void setSolution(String sol)
	{
		this.solution = new Solution(sol);
	}
	
	public void setSolution(Variable[] vars)
	{
		this.solution = new Solution(vars.length);
		for(int i=0; i<vars.length; i++)
		{
			this.solution.getVariables()[i] = vars[i];
		}
	}
	
	public void setCounter(int counter)
	{
		this.counter = counter;
	}
	
	public String toStringWithoutCounter()
	{
		return agent+";"+solution.toString();
	}
	
	public String toString()
	{
		return agent+";"+solution.toString()+"{"+counter+"}";
	}
	
	public boolean isObsolet(AgentView cpa)
	{
		return ((this.getCounter() < cpa.getCounter()) || (this.getSolution().getnbVariables() == 0 && (this.getCounter() <= cpa.getCounter())));
	}
}
