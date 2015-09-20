package unikl.disco.dnc.client;

import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Network;
import unikl.disco.dnc.shared.results.SeparateFlowAnalysisResults;
import unikl.disco.dnc.shared.results.PmooAnalysisResults;
import unikl.disco.dnc.shared.results.TotalFlowAnalysisResults;

import com.google.gwt.user.client.rpc.AsyncCallback;

public interface DiscoDNCAnalysisAsync {
	void sfaAnalysis( Network network, Configuration config, Flow flow_of_interest, AsyncCallback<SeparateFlowAnalysisResults> callback)
			throws IllegalArgumentException;

	void pmooAnalysis( Network network, Configuration config, Flow flow_of_interest, AsyncCallback<PmooAnalysisResults> callback)
			throws IllegalArgumentException;

	void tfaAnalysis(Network network, Configuration config,
			Flow flow_of_interest,
			AsyncCallback<TotalFlowAnalysisResults> callback);
}
