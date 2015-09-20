package unikl.disco.dnc.shared.results;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;

import unikl.disco.dnc.shared.Num;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.ServiceCurve;
import unikl.disco.dnc.shared.network.Server;

public class PmooAnalysisResults implements Serializable {
	public boolean failure = false;
	
	public Num delay_bound;
	public Num backlog_bound;
	public HashSet<ServiceCurve> betas_e2e;
	public HashMap<Server, HashSet<ArrivalCurve>> map__server__alphas;
	
	private PmooAnalysisResults() {}
	
	public PmooAnalysisResults( Num delay_bound, Num backlog_bound, HashSet<ServiceCurve> betas_e2e,
								HashMap<Server,HashSet<ArrivalCurve>> map__server__alphas ) {
		this.delay_bound = delay_bound;
		this.backlog_bound = backlog_bound;
		this.betas_e2e = betas_e2e;
		this.map__server__alphas = map__server__alphas;
	}
	
	public static PmooAnalysisResults analysisFailed() {
		PmooAnalysisResults result = new PmooAnalysisResults();
		result.failure = true;
		return result;
	}
}
