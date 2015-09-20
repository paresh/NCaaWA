/*
 * This file is part of the Disco Deterministic Network Calculator v2.0.3 "Hydra".
 *
 * Copyright (C) 2005 - 2007 Frank A. Zdarsky
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Map;

import unikl.disco.dnc.server.minplus.Convolution;
import unikl.disco.dnc.server.minplus.Deconvolution;
import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.SetUtils;
import unikl.disco.dnc.shared.Configuration.GammaFlag;
import unikl.disco.dnc.shared.Configuration.MuxDiscipline;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.ServiceCurve;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Link;
import unikl.disco.dnc.shared.network.Network;
import unikl.disco.dnc.shared.network.Path;
import unikl.disco.dnc.shared.network.Server;

/**
 * 
 * @author Frank A. Zdarsky
 * @author Steffen Bondorf
 *
 */
public class PmooArrivalBound extends ArrivalBound {
	
	@SuppressWarnings("unused")
	private PmooArrivalBound() {}
	
	public PmooArrivalBound( Network network, Configuration config, HashSet<Flow> flows_to_bound, Flow flow_of_interest ) {
		super( network, config, flows_to_bound, flow_of_interest );
	}
	
	public HashSet<ArrivalCurve> computeArrivalBound( Link link ) throws Exception {
		HashSet<Flow> f_xfcaller = flows_to_bound; 
		
		HashSet<ArrivalCurve> alphas_xfcaller = new HashSet<ArrivalCurve>( Collections.singleton( ArrivalCurve.createNullArrival() ) );
		if ( f_xfcaller.size() == 0 )
		{
			return alphas_xfcaller;
		}
		
		// Get the common sub-path of f_xfcaller flows crossing the given link
		Server to = link.getDest();
		HashSet<Flow> f_to = network.getFlows( to );
		HashSet<Flow> f_xfcaller_to = SetUtils.getIntersection( f_to, f_xfcaller );
		f_xfcaller_to.remove( flow_of_interest );
		if ( f_xfcaller_to.size() == 0 )
		{
			return alphas_xfcaller;
		}
		
		if( config.multiplexingDiscipline() == MuxDiscipline.GLOBAL_FIFO 
				|| ( config.multiplexingDiscipline() == MuxDiscipline.SERVER_LOCAL & link.getSource().useFifoMultiplexing() ) )
		{
			throw new Exception( "PMOO arrival bounding is not available for FIFO multiplexing nodes" );
		}
		
		Server from = network.findSplittingServer( to, f_xfcaller_to );
		Path common_subpath;
		HashSet<ServiceCurve> betas_loxfcaller_subpath = new HashSet<ServiceCurve>();
		ServiceCurve null_service = ServiceCurve.createNullService();
		
		if ( from.equals( to ) ) { // Shortcut if the common subpath only consists of a single hop
			common_subpath = new Path( to );

			HashSet<ArrivalCurve> alphas_xf_caller = ArrivalBound.computeArrivalBound( network, config, to, f_xfcaller, flow_of_interest );
			
			for( ServiceCurve beta_loxfcaller_subpath : LeftOverService.arbMux( to.getServiceCurve(), alphas_xf_caller ) ) {
				if( !beta_loxfcaller_subpath.equals( null_service ) ) {
					betas_loxfcaller_subpath.add( beta_loxfcaller_subpath ); // Adding to the set, not adding up the curves
				}
			}
		} else {
			Flow f_representative = f_xfcaller_to.iterator().next();
			common_subpath = f_representative.getSubPath( from, link.getSource() );
			
			// Get cross-flows grouped as needed for the PMOO left-over service curve
			HashSet<Flow> f_xxfcaller_from_to = SetUtils.getUnion( f_xfcaller_to, Collections.singleton( flow_of_interest ) ); // Those flows at the server to should not be considered interference of themselves
			Map<Path,HashSet<Flow>> xtx_subpath_grouped = network.getFlowsPerSubPath( common_subpath, f_xxfcaller_from_to );
			
			// Derive the cross-flow substitutes with their arrival bound
			HashSet<LinkedList<Flow>> cross_flow_substitutes_set = new HashSet<LinkedList<Flow>>();
			cross_flow_substitutes_set.add( new LinkedList<Flow>() );
			HashSet<LinkedList<Flow>> cross_flow_substitutes_merged = new HashSet<LinkedList<Flow>>();
			
			for ( Map.Entry<Path,HashSet<Flow>> entry : xtx_subpath_grouped.entrySet() ) {
				cross_flow_substitutes_merged = new HashSet<LinkedList<Flow>>();
				
	
			// Create a single substitute flow 
		 		// Name the substitute flow
		 		String substitute_flow_alias = "subst_{";
		 		for( Flow f : entry.getValue() ) {
		 			substitute_flow_alias = substitute_flow_alias.concat( f.getAlias() + "," );
		 		}
		 		substitute_flow_alias = substitute_flow_alias.substring( 0, substitute_flow_alias.length()-1 );
		 		substitute_flow_alias = substitute_flow_alias.concat( "}" );
	
		 		// Derive the substitute flow's arrival bounds
				HashSet<ArrivalCurve> alphas_xf_group = ArrivalBound.computeArrivalBound( network, config, entry.getKey().getSource(), entry.getValue(), flow_of_interest ); // entry.getKey().getSource() because the path in the entry is the common subpath of all flows
		 		
				// Permutation with all the previously derived bounds
		 		for( ArrivalCurve alpha : alphas_xf_group ) {
		 			for( LinkedList<Flow> f_subst_list : cross_flow_substitutes_set ) {
	 					LinkedList<Flow> tmp = new LinkedList<Flow>( f_subst_list );
	 					tmp.add( Flow.createDummyFlow( substitute_flow_alias, alpha, entry.getKey() ) );
	 					cross_flow_substitutes_merged.add( tmp );
		 			}
		 		}
				cross_flow_substitutes_set = new HashSet<LinkedList<Flow>>( cross_flow_substitutes_merged );
			}
					
			// Derive the left-over service curves
			for( LinkedList<Flow> xtx_substitutes : cross_flow_substitutes_set ) {
				ServiceCurve beta_loxfcaller_subpath = PmooAnalysis.getPmooServiceCurve( common_subpath, xtx_substitutes );
				
				if( !beta_loxfcaller_subpath.equals( null_service ) ) {
					betas_loxfcaller_subpath.add( beta_loxfcaller_subpath ); // Adding to the set, not adding up the curves
				}
			}
		}
		
		// Check if there's any service left on this path. Signaled by at least one service curve in this set
		if( betas_loxfcaller_subpath.isEmpty() ) {
			throw new Exception( "No service left over during PMOO arrival bounding!" );
		}
		
		// Get arrival bound at the splitting point:
		// We need to know the arrival bound of f_xfcaller at the server 'from', i.e., at the above sub-path's source
		// in order to deconvolve it with beta_loxfcaller_subpath to get the arrival bound of the sub-path
		// Note that flows f_xfcaller that originate in 'from' are covered by this call of computeArrivalBound
		HashSet<ArrivalCurve> alpha_xfcaller_from = ArrivalBound.computeArrivalBound( network, config, from, f_xfcaller, flow_of_interest );
		
		// Convolve to get the bound.
		// See "Improving Performance Bounds in Feed-Forward Networks by Paying Multiplexing Only Once", Lemma 2
		if( config.useGamma() != GammaFlag.GLOBALLY_OFF  )
		{
			ServiceCurve gamma = ServiceCurve.createZeroDelayBurst(); 
			for ( Server s : common_subpath.getServers() ) {
				gamma = Convolution.convolve( gamma, s.getGamma() );
			}
			alphas_xfcaller = Deconvolution.deconvolve( Convolution.convolve( alpha_xfcaller_from, gamma), betas_loxfcaller_subpath );
		}
		else
		{
			alphas_xfcaller = Deconvolution.deconvolve( alpha_xfcaller_from, betas_loxfcaller_subpath );
		}
		
		if( config.useExtraGamma() != GammaFlag.GLOBALLY_OFF )
		{
			ServiceCurve extra_gamma = ServiceCurve.createZeroDelayBurst(); 
			for ( Server s : common_subpath.getServers() ) {
				extra_gamma = Convolution.convolve( extra_gamma, s.getExtraGamma() );
			}
			alphas_xfcaller = Convolution.convolve( alphas_xfcaller, extra_gamma );
		}
		
		return alphas_xfcaller;
	}
}
