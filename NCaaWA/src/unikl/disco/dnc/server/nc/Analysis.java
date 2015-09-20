/*
 * This file is part of the Disco Deterministic Network Calculator v2.0.3 "Hydra".
 *
 * Copyright (C) 2008 - 2010 Andreas Kiefer
 * Copyright (C) 2011 - 2014 Steffen Bondorf
 *
 * disco | Distributed Computer Systems Lab
 * University of Kaiserslautern, Germany
 *
 * http://disco.cs.uni-kl.de
 *
 *
 * The Disco Deterministic Network Calculator (DiscoDNC) is free software;
 * you can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software Foundation; 
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 */

package unikl.disco.dnc.server.nc;

import java.util.HashMap;
import java.util.HashSet;

import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.Num;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.ServiceCurve;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Network;
import unikl.disco.dnc.shared.network.Server;
import unikl.disco.dnc.shared.results.PmooAnalysisResults;
import unikl.disco.dnc.shared.results.SeparateFlowAnalysisResults;
import unikl.disco.dnc.shared.results.TotalFlowAnalysisResults;

/**
 * This class contains all members and methods that are needed for more than one
 * analysis.
 * 
 * @author Andreas Kiefer
 * @author Steffen Bondorf
 * 
 */
public abstract class Analysis
{
	protected Network network = null;
	protected Configuration config = null;
	protected Flow flow_of_interest = null;

	protected HashMap<Server,HashSet<ArrivalCurve>> map__server__alphas = new HashMap<Server,HashSet<ArrivalCurve>>();
	
	protected HashSet<ServiceCurve> betas_e2e = new HashSet<ServiceCurve>();
	
	protected Num delay_bound = Num.ZERO;
	protected Num backlog_bound = Num.ZERO;

	public static TotalFlowAnalysisResults performTfaEnd2End( Network network, Configuration config, Flow flow_of_interest ) {
		TotalFlowAnalysis tfa = new TotalFlowAnalysis();
		
		tfa.network = network;
		tfa.config = config;
		tfa.flow_of_interest = flow_of_interest;
		
		try {
			tfa.performEnd2EndAnalysis();
			
			return new TotalFlowAnalysisResults( tfa.getDelayBound(), tfa.getServerDelayBoundMap(),
												 tfa.getBacklogBound(), tfa.getServerBacklogBoundMap(),
												 tfa.getServerAlphasMap() );
		} catch (Exception e) {
			return TotalFlowAnalysisResults.analysisFailed();
		}
	}

	public static SeparateFlowAnalysisResults performSfaEnd2End( Network network, Configuration config, Flow flow_of_interest ) {
		SeparateFlowAnalysis sfa = new SeparateFlowAnalysis();
		
		sfa.network = network;
		sfa.config = config;
		sfa.flow_of_interest = flow_of_interest;
		
		try {
			sfa.performEnd2EndAnalysis();
			
			return new SeparateFlowAnalysisResults( sfa.getDelayBound(), sfa.getBacklogBound(),
													sfa.getLeftOverServiceCurves(), sfa.getServerLeftOverBetasMap(),
													sfa.getServerAlphasMap() );
		} catch (Exception e) {
			return SeparateFlowAnalysisResults.analysisFailed();
		}
	}

	public static PmooAnalysisResults performPmooEnd2End( Network network, Configuration config, Flow flow_of_interest ) {
		PmooAnalysis pmoo = new PmooAnalysis();
		
		pmoo.network = network;
		pmoo.config = config;
		pmoo.flow_of_interest = flow_of_interest;
		
		try {
			pmoo.performEnd2EndAnalysis();
			
			return new PmooAnalysisResults( pmoo.getDelayBound(), pmoo.getBacklogBound(), pmoo.getLeftOverServiceCurves(),
					 						pmoo.getServerAlphasMap() );
		} catch (Exception e) {
			return PmooAnalysisResults.analysisFailed();
		}
	}

	public Network getNetwork() {
		return network;
	}

	public Flow getFlowOfInterest() {
		return flow_of_interest;
	}

	protected HashSet<ServiceCurve> getLeftOverServiceCurves() {
		return betas_e2e;
	}
	
	/**
	 * Get the best bounds for a set of alternative end-to-end service curves
	 * 
	 * @param betas_e2e
	 */
	protected void deriveBounds() {
		Num delay_bound__beta_e2e;
		Num backlog_bound__beta_e2e;
		
		delay_bound = Num.POSITIVE_INFINITY;
		backlog_bound = Num.POSITIVE_INFINITY;
		
		for( ServiceCurve beta_e2e : betas_e2e ) {
			delay_bound__beta_e2e = DelayBound.deriveFIFO( flow_of_interest.getArrivalCurve(), beta_e2e ); // single flow of interest, i.e., fifo per micro flow holds
			if( delay_bound__beta_e2e.le( delay_bound ) ) {
				delay_bound = delay_bound__beta_e2e;
			}
			
			backlog_bound__beta_e2e = BacklogBound.derive( flow_of_interest.getArrivalCurve(), beta_e2e );
			if( backlog_bound__beta_e2e.le( backlog_bound ) ) {
				backlog_bound = backlog_bound__beta_e2e;
			}
		}
	}
	
	/**
	 * Returns the delay bound of the analysis.
	 * 
	 * @return the delay bound
	 */
	protected Num getDelayBound() {
		return delay_bound;
	};
	
	/**
	 * Returns the backlog bound of the analysis.
	 * 
	 * @return the backlog bound
	 */
	protected Num getBacklogBound() {
		return backlog_bound;
	};

	/**
	 * For TFA this is the whole traffic at a server because
	 * you do not separate the flow of interest during analysis.
	 * 
	 * For SFA and PMOO you will get the arrival bounds of
	 * the cross-traffic at every server.
	 * 
	 * @return Mapping from the server to the server's arrival bound
	 */
	protected HashMap<Server, HashSet<ArrivalCurve>> getServerAlphasMap(){
		return new HashMap<Server, HashSet<ArrivalCurve>>( map__server__alphas );
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName();
	}
}
