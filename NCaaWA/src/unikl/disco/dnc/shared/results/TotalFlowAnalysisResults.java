package unikl.disco.dnc.shared.results;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import unikl.disco.dnc.shared.Num;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.network.Server;

public class TotalFlowAnalysisResults implements Serializable {
	public boolean failure = false;
	
	public Num delay_bound;
	public HashMap<Server, HashSet<Num>> map__server__D_server;
	public Num backlog_bound;
	public HashMap<Server, HashSet<Num>> map__server__B_server;
	public HashMap<Server, HashSet<ArrivalCurve>> map__server__alphas;
	
	private TotalFlowAnalysisResults(){}
	
	public TotalFlowAnalysisResults( Num delay_bound, HashMap<Server, HashSet<Num>> map__server__D_server,
									 Num backlog_bound, HashMap<Server, HashSet<Num>> map__server__B_server,
									 HashMap<Server, HashSet<ArrivalCurve>> map__server__alphas ) {
		this.delay_bound = delay_bound;
		this.map__server__D_server = map__server__D_server;
		this.backlog_bound = backlog_bound;
		this.map__server__B_server = map__server__B_server;
		this.map__server__alphas = map__server__alphas;
	}
	
	public static TotalFlowAnalysisResults analysisFailed() {
		TotalFlowAnalysisResults result = new TotalFlowAnalysisResults();
		result.failure = true;
		return result;
	}
}
