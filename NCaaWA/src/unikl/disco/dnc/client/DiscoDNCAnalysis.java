package unikl.disco.dnc.client;

import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Network;
import unikl.disco.dnc.shared.results.PmooAnalysisResults;
import unikl.disco.dnc.shared.results.SeparateFlowAnalysisResults;
import unikl.disco.dnc.shared.results.TotalFlowAnalysisResults;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * The client-side stub for the RPC service.
 */
@RemoteServiceRelativePath("greet")
public interface DiscoDNCAnalysis extends RemoteService {
	SeparateFlowAnalysisResults sfaAnalysis( Network network, Configuration config, Flow flow_of_interest ) throws IllegalArgumentException;
	
	PmooAnalysisResults pmooAnalysis( Network network, Configuration config, Flow flow_of_interest ) throws IllegalArgumentException;
	TotalFlowAnalysisResults tfaAnalysis(Network network, Configuration config,Flow flow_of_interest) throws IllegalArgumentException;
}
