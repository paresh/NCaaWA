package unikl.disco.dnc.server;

import unikl.disco.dnc.client.DiscoDNCAnalysis;
import unikl.disco.dnc.server.nc.Analysis;
import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Network;
import unikl.disco.dnc.shared.results.SeparateFlowAnalysisResults;
import unikl.disco.dnc.shared.results.PmooAnalysisResults;
import unikl.disco.dnc.shared.results.TotalFlowAnalysisResults;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class DiscoDNCAnalysisImpl extends RemoteServiceServlet implements
		DiscoDNCAnalysis {
	

	public SeparateFlowAnalysisResults sfaAnalysis( Network network, Configuration config, Flow flow_of_interest ) throws IllegalArgumentException {
		try {
			SeparateFlowAnalysisResults results = Analysis.performSfaEnd2End( network, config, flow_of_interest );
			return results;
		} catch (Exception e) {
			return null; // TODO
		}
	}

	public PmooAnalysisResults pmooAnalysis( Network network, Configuration config, Flow flow_of_interest ) throws IllegalArgumentException {
		try {
			PmooAnalysisResults results = Analysis.performPmooEnd2End( network, config, flow_of_interest );
			return results;
		} catch (Exception e) {
			return null; // TODO
		}
	}
	public TotalFlowAnalysisResults tfaAnalysis(Network network, Configuration config, Flow flow_of_interest) throws IllegalArgumentException{
		try{
			TotalFlowAnalysisResults results = Analysis.performTfaEnd2End(network, config, flow_of_interest);
			return results;
		}catch(Exception e){
			System.out.print(e);
			return null;
		}
		
	}
}
		



	

