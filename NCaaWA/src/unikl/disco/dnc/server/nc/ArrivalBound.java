/*
 * This file is part of the Disco Deterministic Network Calculator v2.0.3 "Hydra".
 *
 * Copyright (C) 2013, 2014 Steffen Bondorf
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

import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.SetUtils;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Link;
import unikl.disco.dnc.shared.network.Network;
import unikl.disco.dnc.shared.network.Server;

/**
 * 
 * @author Steffen Bondorf
 *
 */
public abstract class ArrivalBound {
	protected Network network;
	protected Configuration config;
	protected HashSet<Flow> flows_to_bound;
	protected Flow flow_of_interest;
	
	protected ArrivalBound() {}
	
	protected ArrivalBound( Network network, Configuration config, HashSet<Flow> flows_to_bound, Flow flow_of_interest ) {
		this.network = network;
		this.config = config;
		this.flows_to_bound = flows_to_bound;
		this.flow_of_interest = flow_of_interest;
	}
	
	public static HashSet<ArrivalCurve> computeArrivalBound( Network network, Configuration config, Server server ) throws Exception {
		return computeArrivalBound( network, config, server, network.getFlows( server ), Flow.NULL_FLOW );
	}
	
	public static HashSet<ArrivalCurve> computeArrivalBound( Network network, Configuration config, Server server, HashSet<Flow> flows_to_bound ) throws Exception {
		return computeArrivalBound( network, config, server, flows_to_bound, Flow.NULL_FLOW );
	}
	
	public static HashSet<ArrivalCurve> computeArrivalBound( Network network, Configuration config, Link link ) throws Exception {
		return computeArrivalBound( network, config, link.getDest(), network.getFlows( link ), Flow.NULL_FLOW );
	}
	
	public static HashSet<ArrivalCurve> computeArrivalBound( Network network, Configuration config, Link link, HashSet<Flow> flows_to_bound ) throws Exception {
		HashSet<Flow> flows_on_l_to_bound = SetUtils.getIntersection( flows_to_bound, network.getFlows( link ) );
		return computeArrivalBound( network, config, link.getDest(), flows_on_l_to_bound, Flow.NULL_FLOW );
	}
	
	/**
	 * Calculates the alternative arrival curves for a the flows in flows_to_bound that arrive at the given server
	 * while considering the lower priority of the flow_of_interest
	 * 
	 * @param network
	 * @param server
	 * @param flows_to_bound
	 * @return the arrival bound at the specified server
	 * @throws Exception
	 */
	public static HashSet<ArrivalCurve> computeArrivalBound( Network network, Configuration config, Server server, HashSet<Flow> flows_to_bound, Flow flow_of_interest ) throws Exception {
		flows_to_bound.remove( flow_of_interest );
		
		HashSet<ArrivalCurve> arrival_bounds = new HashSet<ArrivalCurve>( Collections.singleton( ArrivalCurve.createNullArrival() ) );
		
		if ( flows_to_bound.size() == 0 )
		{
			return arrival_bounds;
		}
		
		HashSet<Flow> f_server = network.getFlows( server );
		HashSet<Flow> f_xfcaller_server = SetUtils.getIntersection( f_server, flows_to_bound );
		if ( f_xfcaller_server.size() == 0 )
		{
			return arrival_bounds;
		}
		
		HashSet<ArrivalCurve> arrival_bounds_link = new HashSet<ArrivalCurve>();
		HashSet<ArrivalCurve> arrival_bounds_link_merged = new HashSet<ArrivalCurve>();
	
		// Get cross-traffic originating in server
		HashSet<Flow> f_xfcaller_sourceflows_server = SetUtils.getIntersection( f_xfcaller_server, network.getSourceFlows( server ) );
		f_xfcaller_sourceflows_server.remove( flow_of_interest );
		ArrivalCurve alpha_xfcaller_sourceflows_server = network.getSourceFlowArrivalCurve( server, f_xfcaller_sourceflows_server );
		arrival_bounds = new HashSet<ArrivalCurve>( Collections.singleton( alpha_xfcaller_sourceflows_server ) );
		
		// Get cross-traffic from each predecessor. Call per link in order to get splitting points
		HashSet<Flow> f_xfcaller_server_bounded = f_xfcaller_sourceflows_server;
		if ( !f_xfcaller_server_bounded.containsAll( f_xfcaller_server ) ) {
			for ( Link in_l : network.getInLinks( server ) ) {
				arrival_bounds_link = new HashSet<ArrivalCurve>();
				arrival_bounds_link_merged = new HashSet<ArrivalCurve>();
				
				HashSet<Flow> f_xfcaller_in_l = SetUtils.getIntersection( network.getFlows( in_l ), f_xfcaller_server );
				f_xfcaller_in_l.remove( flow_of_interest );
				
				if( f_xfcaller_in_l.isEmpty() ) { // Do not check links without flows of interest
					continue;
				}
				
				arrival_bounds_link = computeArrivalBound( network, config, in_l, f_xfcaller_in_l, flow_of_interest );
				
				if ( !arrival_bounds.isEmpty() ) {
					for ( ArrivalCurve arrival_bound_other_link : arrival_bounds ) {
						for ( ArrivalCurve arrival_bound_link : arrival_bounds_link ) {
							arrival_bound_link.beautify();
							arrival_bounds_link_merged.add( ArrivalCurve.add( arrival_bound_other_link, arrival_bound_link ) );
						}
					}
				} else {
					arrival_bounds_link_merged.addAll( arrival_bounds_link );
				}
				arrival_bounds = arrival_bounds_link_merged;
				
				f_xfcaller_server_bounded.addAll( f_xfcaller_in_l ); // Stop as soon as all flows are bounded
				if ( f_xfcaller_server_bounded.containsAll( f_xfcaller_server ) ) {
					break;
				}
			}
		}
		
		return arrival_bounds;
	}

	public static HashSet<ArrivalCurve> computeArrivalBound( Network network, Configuration config, Link link, HashSet<Flow> flows_to_bound, Flow flow_of_interest ) throws Exception {
		HashSet<ArrivalCurve> arrival_bounds_xfcaller = new HashSet<ArrivalCurve>();
		
		for( Configuration.ArrivalBoundMethods arrival_bound_method : config.arrivalBoundMethods() ) {
			
			HashSet<ArrivalCurve> arrival_bounds_tmp = new HashSet<ArrivalCurve>();

			switch( arrival_bound_method ) {

				case PBOO_PER_HOP:
				default:
					PbooArrivalBound_PerHop pboo_per_hop = new PbooArrivalBound_PerHop( network, config, flows_to_bound, flow_of_interest );
					arrival_bounds_tmp = pboo_per_hop.computeArrivalBound( link );
	
					addArrivalBounds( arrival_bounds_tmp, config.removeDuplicateArrivalBounds(), arrival_bounds_xfcaller );
				break;
				
				case PBOO_CONCATENATION:
					PbooArrivalBound_Concatenation pboo_concatenation = new PbooArrivalBound_Concatenation( network, config, flows_to_bound, flow_of_interest );
					arrival_bounds_tmp = pboo_concatenation.computeArrivalBound( link );
					
					addArrivalBounds( arrival_bounds_tmp, config.removeDuplicateArrivalBounds(), arrival_bounds_xfcaller );
				break;
	
				case PMOO:
					PmooArrivalBound pmoo_arrival_bound = new PmooArrivalBound( network, config, flows_to_bound, flow_of_interest );
					arrival_bounds_tmp = pmoo_arrival_bound.computeArrivalBound( link );
					
					addArrivalBounds( arrival_bounds_tmp, config.removeDuplicateArrivalBounds(), arrival_bounds_xfcaller );
				break;
				
			}
		}

		return arrival_bounds_xfcaller;
	}
	
	private static void addArrivalBounds( HashSet<ArrivalCurve> arrival_bounds_to_merge, boolean remove_duplicates, HashSet<ArrivalCurve> arrival_bounds ) {
		// TODO I lost this ability, unfortunately
//		if( Configuration.arrivalBoundMethods().size() == 1 ) { // In this case there can only be one arrival bound
//			arrival_bounds.addAll( arrival_bounds_to_merge );
//		} else {
			for( ArrivalCurve arrival_bound : arrival_bounds_to_merge ) {
				addArrivalBound( arrival_bound, remove_duplicates, arrival_bounds );
			}
//		}
	}
	
	private static void addArrivalBound( ArrivalCurve arrival_bound_to_merge, boolean remove_duplicates, HashSet<ArrivalCurve> arrival_bounds ) {
		// TODO I lost this ability, unfortunately
//		if( Configuration.arrivalBoundMethods().size() == 1 ) { // In this case there can only be one arrival bound
//			arrival_bounds.add( arrival_bound_to_merge );
//		} else {
			if( !remove_duplicates 
					|| ( remove_duplicates && !isDuplicate( arrival_bound_to_merge, arrival_bounds )  ) ) {
				arrival_bounds.add( arrival_bound_to_merge );
			}
//		}
	}
	
	private static boolean isDuplicate( ArrivalCurve arrival_bound_to_check, HashSet<ArrivalCurve> arrival_bounds ) {
		for ( ArrivalCurve arrival_bound_existing : arrival_bounds ) {
			if( arrival_bound_to_check.equals( arrival_bound_existing ) ) {
				return true;
			}
		}
		return false;
	}
}