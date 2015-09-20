/*
 * This file is part of the Disco Deterministic Network Calculator v2.0.3 "Hydra".
 *
 * Copyright (C) 2005 - 2007 Frank A. Zdarsky
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

import java.util.Collections;
import java.util.List;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.HashSet;
import java.util.Map;

import unikl.disco.dnc.server.minplus.Convolution;
import unikl.disco.dnc.shared.Configuration.MuxDiscipline;
import unikl.disco.dnc.shared.Num;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.Curve;
import unikl.disco.dnc.shared.curves.ServiceCurve;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Path;
import unikl.disco.dnc.shared.network.Server;

/**
 * 
 * @author Frank A. Zdarsky
 * @author Andreas Kiefer
 * @author Steffen Bondorf
 * 
 */
public class PmooAnalysis extends Analysis
{	
	protected PmooAnalysis() {}
	
	/**
	 * Performs a pay-multiplexing-only-once (PMOO) analysis for the <code>flow_of_interest</code>.
	 * 
	 * @param flow_of_interest
	 *            The flow for which the end-to-end service curve shall be
	 *            computed
	 */
	protected void performEnd2EndAnalysis() throws Exception {
		betas_e2e = getPmooServiceCurves();
		this.deriveBounds();
	}

	protected HashSet<ServiceCurve> getPmooServiceCurves() throws Exception {
		if ( flow_of_interest == null ) {
			throw new Exception( "Flow of interest not set" );
		}
		
		return getPmooServiceCurves( flow_of_interest.getPath() );
	}
	
	protected HashSet<ServiceCurve> getPmooServiceCurves( Flow flow_of_interest ) throws Exception {
		this.flow_of_interest = flow_of_interest;
		return betas_e2e = getPmooServiceCurves();
	}
	
	protected HashSet<ServiceCurve> getPmooServiceCurves( Flow flow_of_interest, Path path ) throws Exception {
		this.flow_of_interest = flow_of_interest;
		return betas_e2e = getPmooServiceCurves( path );
	}
	
	protected HashSet<ServiceCurve> getPmooServiceCurves( Path path ) throws Exception {
		if( config.multiplexingDiscipline() == MuxDiscipline.SERVER_LOCAL ) {
			for( Server s : path.getServers() ) {
				if( s.useFifoMultiplexing() ) {
					throw new Exception( "PMOO analysis is not available for FIFO multiplexing nodes" );
				}
			}
		}
		
		HashSet<ServiceCurve> betas_e2e = new HashSet<ServiceCurve>();
		
		// Get cross-flows grouped as needed for the PMOO left-over service curve
		Map<Path,HashSet<Flow>> xtx_subpath_grouped = network.getFlowsPerSubPath( path, new HashSet<Flow>( Collections.singleton( flow_of_interest ) ) );

		if( xtx_subpath_grouped.isEmpty() ) {
			ServiceCurve service_curve = ServiceCurve.createZeroDelayBurst(); 
			for ( Server s : path.getServers() ) {
				service_curve = Convolution.convolve( service_curve, s.getServiceCurve() );
			}
			return new HashSet<ServiceCurve>( Collections.singleton( service_curve ) );
		}
		
		// Derive the cross-flow substitutes with their arrival bound
		HashSet<List<Flow>> cross_flow_substitutes_set = new HashSet<List<Flow>>();
		cross_flow_substitutes_set.add( new LinkedList<Flow>() );
		HashSet<List<Flow>> cross_flow_substitutes_merged = new HashSet<List<Flow>>();

		for ( Map.Entry<Path,HashSet<Flow>> entry : xtx_subpath_grouped.entrySet() ) {
			cross_flow_substitutes_merged = new HashSet<List<Flow>>();
		
		// Create a single substitute flow 
	 		// Name the substitute flow
	 		String substitute_flow_alias = "subst_{";
	 		for( Flow f : entry.getValue() ) {
	 			substitute_flow_alias = substitute_flow_alias.concat( f.getAlias() + "," );
	 		}
	 		substitute_flow_alias = substitute_flow_alias.substring( 0, substitute_flow_alias.length()-1 );
	 		substitute_flow_alias = substitute_flow_alias.concat( "}" );

	 		// Derive the substitute flow's arrival bound
	 		HashSet<ArrivalCurve> alphas_xf_group = ArrivalBound.computeArrivalBound( network, config, entry.getKey().getSource(), entry.getValue(), flow_of_interest ); // entry.getKey().getSource() because the path in the entry is the common subpath of all flows
	 		
	 		for( ArrivalCurve alpha : alphas_xf_group ) {
	 			for( List<Flow> f_subst_list : cross_flow_substitutes_set ) {
 					List<Flow> tmp = new LinkedList<Flow>( f_subst_list );
 					tmp.add( Flow.createDummyFlow( substitute_flow_alias, alpha, entry.getKey() ) );
 					cross_flow_substitutes_merged.add( tmp );
	 			}
	 		} 
			cross_flow_substitutes_set = cross_flow_substitutes_merged;
			
			if ( map__server__alphas.get( entry.getKey().getSource() ) == null ) {
				map__server__alphas.put( entry.getKey().getSource(), alphas_xf_group );
			} else {
				map__server__alphas.get( entry.getKey().getSource() ).addAll( alphas_xf_group );
			}
		}
		
		// Derive the left-over service curves
		for( List<Flow> xtx_substitutes : cross_flow_substitutes_set ) {
			betas_e2e.add( getPmooServiceCurve( path, xtx_substitutes ) );
		}
		return betas_e2e;
	}
	
	/**
	 * Concatenates the service curves along the given path <code>path</code>
	 * according to the PMOO approach and returns the result.
	 * 
	 * It first decomposes all arrival curves (service curves) into token
	 * buckets (rate latency curves), enumerates over all combinations of token
	 * buckets and rate latency curves, and calls
	 * <code>computePartialPMOOServiceCurve()</code> for each combination. The
	 * total PMOO service curve is the maximum of all partial service curves.
	 * 
	 * @param path
	 *            The Path traversed for which a PMOO left-over service curve will be computed.
	 * @param cross_flow_substitutes
	 *            Flow substitutes according to PMOO's needs and abstracting from the actual cross-flows.
	 * @return The PMOO service curve
	 */
	protected static ServiceCurve getPmooServiceCurve( Path path, List<Flow> cross_flow_substitutes ) {
		// Create a flow-->tb_iter map
		HashMap<Flow, Integer> flow_tb_iter_map = new HashMap<Flow, Integer>();
		for ( Flow f : cross_flow_substitutes )
		{
			flow_tb_iter_map.put( f, Integer.valueOf( 0 ) );
		}
		// Create a list of rl_iters
		int number_servers = path.getServers().size();
		ServiceCurve[] service_curves = new ServiceCurve[number_servers];
		int[] server_rl_iters = new int[number_servers];
		int[] server_rl_counts = new int[number_servers];
		int i = 0;
		for ( Server server : path.getServers() )
		{
			ServiceCurve service_curve = server.getServiceCurve();
			service_curves[i] = service_curve;
			server_rl_iters[i] = 0;
			server_rl_counts[i] = service_curve.getRLComponentCount();
			i++;
		}

		ServiceCurve beta_total = ServiceCurve.createNullService();

		boolean more_combinations = true;
		while ( more_combinations )
		{
			// Compute service curve for this combination
			ServiceCurve beta = computePartialPMOOServiceCurve(	path,
															service_curves,
															cross_flow_substitutes,
															flow_tb_iter_map,
															server_rl_iters );
			if ( !beta.equals( ServiceCurve.createNullService() ) )
			{
				beta_total = ServiceCurve.max( beta_total, beta );
			}

			// First check whether there are more combinations of flow TBs
			more_combinations = false;
			for ( Flow f : flow_tb_iter_map.keySet() )
			{
				ArrivalCurve f_bound = f.getArrivalCurve();

				i = flow_tb_iter_map.get( f ).intValue();
				if ( i + 1 < f_bound.getTBComponentCount() )
				{
					flow_tb_iter_map.put( f, Integer.valueOf( i + 1 ) );
					more_combinations = true;
					break;
				}
				else
				{
					flow_tb_iter_map.put( f, Integer.valueOf( 0 ) );
				}
			}

			// If not, check whether there are more combinations of server RLs
			if ( !more_combinations )
			{
				for ( i = 0; i < server_rl_iters.length; i++ )
				{
					int j = server_rl_iters[i];
					if ( j + 1 < server_rl_counts[i] )
					{
						server_rl_iters[i] = j;
						more_combinations = true;
						break;
					}
					else
					{
						server_rl_iters[i] = 0;
					}
				}
			}
		}

		return beta_total;
	}

	/**
	 * Calculates the partial PMOO service curve for the given flow set by
	 * combining all servers having an outgoing link contained in the given
	 * link-path. For each flow considers only one of its token bucket
	 * components (selected via the flow_tb_iter_map) and for each service curve
	 * considers only one rate latency curve (selected via the server_rl_iters).
	 * 
	 * @return A partial PMOO service curve
	 */
	protected static ServiceCurve computePartialPMOOServiceCurve(	Path path,
															ServiceCurve[] service_curves,
															List<Flow> cross_flow_substitutes,
															HashMap<Flow,Integer> flow_tb_iter_map,
															int[] server_rl_iters )
	{
		Num T = Num.ZERO;
		Num R = Num.POSITIVE_INFINITY;
		Num sum_bursts = Num.ZERO;
		Num sum_latencyterms = Num.ZERO;
		
		HashSet<Flow> present_flows = new HashSet<Flow>();
		for ( Server s : path.getServers() )
		{
			int i = path.getServers().indexOf( s );
			// Add incoming flows
			for ( Flow f : cross_flow_substitutes ) {
				if ( f.getPath().getServers().contains( s ) ) { // The exact path of the substitute does not matter, only the shared servers with the flow of interest do
					present_flows.add( f );
				}
			}

			Curve current_rl = service_curves[i].getRLComponent( server_rl_iters[i] );

			// Sum up latencies
			T = Num.add( T, current_rl.getLatency() );

			// Compute and store sum of rates of all passing flows
			Num sum_r = Num.ZERO;
			for ( Flow f : present_flows )
			{
				ArrivalCurve bound = f.getArrivalCurve();
				Curve current_tb = bound.getTBComponent( ((Integer) flow_tb_iter_map.get( f )).intValue() );
				sum_r = Num.add( sum_r, current_tb.getSustainedRate() );
			}

			// Update latency terms (increments)
			sum_latencyterms = Num.add( sum_latencyterms, Num.mult( sum_r, current_rl.getLatency() ) );

			// Compute left-over rate; update min
			Num Ri = Num.sub( current_rl.getSustainedRate(), sum_r );
			if ( Ri.le( Num.ZERO ) )
			{
				return ServiceCurve.createNullService();
			}
			R = Num.min( R, Ri );

			// Remove all outgoing flows from the set of present flows
			HashSet<Flow> leaving_flows = new HashSet<Flow>();
			for ( Flow f : present_flows ) {
				if ( path.getServers().indexOf( f.getSink() ) <= i ) {
					leaving_flows.add( f );
				}
			}
			present_flows.removeAll( leaving_flows );
		}

		// Compute sum of bursts
		for ( Flow f : cross_flow_substitutes )
		{
			ArrivalCurve bound = f.getArrivalCurve();
			Curve current_tb = bound.getTBComponent( ((Integer) flow_tb_iter_map.get( f )).intValue() );
			sum_bursts = Num.add( sum_bursts, current_tb.getTBBurst() );
		}

 		T = Num.add( T,Num.div( Num.add( sum_bursts, sum_latencyterms ), R ) );

 		if( T == Num.POSITIVE_INFINITY ) {
 			return ServiceCurve.createNullService();
 		}
 		if( R == Num.POSITIVE_INFINITY ) {
 			return ServiceCurve.createBurstDelay( T );
 		}

		return ServiceCurve.createRateLatency( R, T );
	}
}
