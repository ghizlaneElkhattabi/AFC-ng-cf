package DisSolver;
import java.util.ArrayList;
import java.util.HashMap;

import choco.Choco;
import choco.cp.model.CPModel;
import choco.cp.solver.CPSolver;
import choco.kernel.model.Model;
import choco.kernel.model.constraints.Constraint;
import choco.kernel.model.variables.integer.IntegerVariable;
import choco.kernel.solver.Solver;
import choco.kernel.solver.constraints.integer.extension.BinRelation;

public class Chocoo {
	int nbvar, index;
	Model m;
	IntegerVariable[] var;
	Solver s;
	ArrayList<Domaiin> domain;
	Parser parser;
	int MaxIndex;
	String Filename;
	Solution sol;
	ArrayList<Solution> Solutions;
	ArrayList<Solution> SolutionsWithoutInterchangeability;

	public Solver getSolver()
	{
		return s;
	}
	
	public Chocoo(String Filename) {
		this.Filename = Filename;
		parser = new Parser(Filename); // récupérer les données à partir du							// fichier xml
		nbvar = parser.getVariables().size();
		domain = parser.getDomaines(); // récupérer les domaines des variables
		index = 0; // l'indice de la solution courante pour y revenir
		MaxIndex = 0; // le nombre de solutions du problème
		Solutions = new ArrayList<Solution>();
		SolutionsWithoutInterchangeability = new ArrayList<Solution>();
		m = new CPModel();
		s = new CPSolver();
		sol = new Solution(nbvar);
		var = new IntegerVariable[nbvar]; // var contient les variables du
											// probl�me
		for (int i = 0; i < nbvar; i++) {
			var[i] = Choco.makeIntVar(
					parser.getVariables().get(i).split("/")[0], getDomain(parser
							.getVariables().get(i).split("\\/")[0]));
			m.addVariable(var[i]);
		}
//		ArrayList<String> contrainte = new ArrayList<String>();
//		contrainte = parser.getContraintes();
		for (jConstraint constraint : parser.getContraintes()) {
			
			// ge(abs(sub(Mi,Mj)), DD)/int Mi,int Mj,int DD/M2.1 M1.1 5
			if (constraint.getType().equals("ArrivalTime")) {
//				String[] varsName = constraints.split("/")[1].split(" ");
				IntegerVariable[] VarsVars = new IntegerVariable[constraint.getVariables().length];

				int i = 0;
				while (i < constraint.getVariables().length) {
					boolean exite = false;
					for (IntegerVariable vv : var) {
						if (vv.getName().equals(constraint.getVariables()[i])) {
							VarsVars[i] = vv;
							i++;
							exite = true;
							break;
						}
					}
					if (!exite) {
						VarsVars[i] = Choco.makeIntVar("",
								Integer.valueOf(constraint.getVariables()[i]),
								Integer.valueOf(constraint.getVariables()[i]));
						i++;
					}

				}

				Constraint c = Choco.geq(
						Choco.abs(Choco.minus(VarsVars[0], VarsVars[1])),
						VarsVars[2]);
				m.addConstraint(c);
			}
			if (constraint.getType().equals("neq")) {
//				String[] varsName = constraints.split("/")[1].split(" ");
				IntegerVariable[] VarsVars = new IntegerVariable[constraint.getVariables().length];

				int i = 0;
				while (i < constraint.getVariables().length) {
					boolean exite = false;
					for (IntegerVariable vv : var) {
						if (vv.getName().equals(constraint.getVariables()[i])) {
							VarsVars[i] = vv;
							i++;
							exite = true;
							break;
						}
					}
					if (!exite) {
						VarsVars[i] = Choco.makeIntVar("",
								Integer.valueOf(constraint.getVariables()[i]),
								Integer.valueOf(constraint.getVariables()[i]));
						i++;
					}
				}
				Constraint c = Choco.neq(VarsVars[0], VarsVars[1]);
				m.addConstraint(c);
			}
			else if(constraint.getType().equals("conflicts"))
			{
				IntegerVariable[] VarsVars = new IntegerVariable[constraint.getVariables().length];
				int i = 0;
				while (i < constraint.getVariables().length) {
					boolean exite = false;
					for (IntegerVariable vv : var) {
						if (vv.getName().equals(constraint.getVariables()[i])) {
							VarsVars[i] = vv;
							i++;
							exite = true;
							break;
						}
					}
					if (!exite) {
						VarsVars[i] = Choco.makeIntVar("",
								Integer.valueOf(constraint.getVariables()[i]),
								Integer.valueOf(constraint.getVariables()[i]));
						i++;
					}

				}
				ArrayList<int[]> fbd = new ArrayList<int[]>();
				int []min = new int[constraint.getTuples().size()];
				int []max = new int[constraint.getTuples().size()];
				for(int ii=0; ii<constraint.getTuples().size(); ii++)
				{
					min[ii] = 0;
					max[ii] = 100;
					int []bb = new int [2];
					for(int j =0; j<2; j++)
					{
						bb[j]=constraint.getTuples().get(ii)[j];
					}
					fbd.add(bb);
				}
				BinRelation r = Choco.makeBinRelation(min, max, fbd, false);
				m.addConstraint(Choco.relationPairAC(VarsVars[0], VarsVars[1], r));
			}
			else if(constraint.getType().equals("support"))
			{
				IntegerVariable[] VarsVars = new IntegerVariable[constraint.getVariables().length];

				int i = 0;
				while (i < constraint.getVariables().length) {
					boolean exite = false;
					for (IntegerVariable vv : var) {
						if (vv.getName().equals(constraint.getVariables()[i])) {
							VarsVars[i] = vv;
							i++;
							exite = true;
							break;
						}
					}
					if (!exite) {
						VarsVars[i] = Choco.makeIntVar("",
								Integer.valueOf(constraint.getVariables()[i]),
								Integer.valueOf(constraint.getVariables()[i]));
						i++;
					}

				}
				ArrayList<int[]> prm = new ArrayList<int[]>();
				int []min = new int[constraint.getTuples().size()];
				int []max = new int[constraint.getTuples().size()];
				for(int ii=0; ii<constraint.getTuples().size(); ii++)
				{
					min[ii] = 0;
					max[ii] = 100;
					int []bb = new int [2];
					for(int j =0; j<2; j++)
					{
						bb[j]=constraint.getTuples().get(ii)[j];
					}
					prm.add(bb);
				}
				BinRelation r = Choco.makeBinRelation(min, max, prm, true);
				m.addConstraint(Choco.relationPairAC(VarsVars[0], VarsVars[1], r));
		}
		}
		 s.read(m);
		 s.solve();
		
//		System.out.println("NBR constraints "+m.getNbConstraints()+"  "+s.getNbConstraints());
		 Solutions = new ArrayList<Solution>();
		 Solution solution = null;
		boolean next = s.checkSolution();
		while (next) {
			boolean exist = false;
			solution = new Solution(var.length);
			for (int i = 0; i < var.length; i++) {
				solution.getVariables()[i] = new Variable(s.getVar(var[i]).toString(), getType(var[i].getName()));
			}
			SolutionsWithoutInterchangeability.add(solution);
			for(int i=0; i<Solutions.size(); i++)
			{
				if(Solutions.get(i).containSameExternalVariableValues(solution))
				{
					exist = true;
					break;
				}
			}
			if(!exist)
			{
//				System.out.println("***  " + solution);
				Solutions.add(solution);
			}
			else
			{
				
			}
			next = s.nextSolution();
		}

		// --------------------------------
		
	}
	
	public String getType(String VarName)
	{
		for (int i = 0; i < nbvar; i++) {
			if(parser.getVariables().get(i).split("\\/")[0].equals(VarName))
			{
				return parser.getVariables().get(i).split("\\/")[2];
			}
		}
		return "";
	}
	
	public ArrayList<Solution> getSolutions()
	{
//		System.out.println(Solutions);
//		Solution solution = new Solution(var.length);
//		for (int i = 0; i < var.length; i++) {
////			System.out.println("////   "+var[]);
//			solution.getVariables()[i] = new Variable(var[i].getName(), s.getVar(var[i]).getValue());
//		}
		return Solutions;
	}
	
	  public int getNbConstraints()
	  {
		  return s.getNbConstraints();
	  }
  
	  public Boolean hasNextSolution()
	  {
		  return(s.nextSolution() && s.isFeasible());
	  }
  
	// fonction qui retourne un tableau (variable) contenant la solution trouvée
//	public String[] getLocalSolution() {
//		String[] variable;
//		variable = new String[nbvar];
//
//		if (s.getNbSolutions() > 0) {
//			for (int i = 0; i < nbvar; i++) {
//				variable[i] = s.getVar(var[i]).toString();
//			}
//		} else {
//			System.err.println("Pas de solution locale!! ");
//		}
//		return variable;
//	}

	public String getDomainName(String VarName)
	{
		for (int i = 0; i < nbvar; i++) {
			if(parser.getVariables().get(i).split("\\/")[0].equals(VarName))
			{
				return parser.getVariables().get(i).split("\\/")[1];
			}
		}
		return "";
	}
	
	// fonction qui retourne un tableau contenant le domaine d'une variable
	// pass�e en argument
	public int[] getDomain(String var) {
		domain = new ArrayList<Domaiin>();
		domain = parser.getDomaines();
		int i = 0;
		while (!(domain.get(i).getName().equals(getDomainName(var)))) {
			i++;
		}
		int[] domval = new int[domain.get(i).size];
		for (int j = 0; j < domain.get(i).size; j++) {

			domval[j] = domain.get(i).getElements().get(j);
		}
		return domval;
	}

	public void NextSolution() {
		s.nextSolution();
	}

//	public void resolve(NogoodStore ngdStore, boolean start, Statistic st) {
////		System.out.println("-----   " + ngdStore.getNogoods());
//		ArrayList<Variable> Var = new ArrayList<Variable>();
//		ArrayList<int[]> domain = new ArrayList<int[]>();
//		ArrayList<jConstraint> constraints = new ArrayList<jConstraint>();
//		for(IntegerVariable v : var)
//		{
//			Var.add(new Variable(v.getName(), -1));
//			domain.add(v.getValues());
//		}
//		
//		for(jConstraint c : parser.getContraintes())//constraints
//		{
//			constraints.add(c);
//		}
//		hs_mpp hs = new hs_mpp();
//		sol = hs.remplirTableau(Var, domain, constraints, ngdStore, start, st);
//	}
	
	// setter & getter
	public void setIndex(int i) {
		index = i;
	}

	public int getIndex() {
		return index;
	}

	public int getMaxIndex() {
		return MaxIndex;
	}
}