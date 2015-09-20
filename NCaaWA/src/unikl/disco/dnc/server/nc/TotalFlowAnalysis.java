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

import java.util.HashSet;
import java.util.HashMap;

import unikl.disco.dnc.shared.Configuration.MuxDiscipline;
import unikl.disco.dnc.shared.Num;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.ServiceCurve;
import unikl.disco.dnc.shared.network.Server;

/**
 * 
 * @author Frank A. Zdarsky
 * @author Andreas Kiefer
 * @author Steffen Bondorf
 * 
 */
public class TotalFlowAnalysis extends Analysis
{
	HashMap<Server,HashSet<Num>> map__server__D_server = new HashMap<Server,HashSet<Num>>();
	HashMap<Server,HashSet<Num>> map__server__B_server = new HashMap<Server,HashSet<Num>>();
	
	protected void performEnd2EndAnalysis() throws Exception {
		for ( Server server : flow_of_interest.getPath().getServers() ) {
			// Here's the difference to SFA:
			// TFA needs the arrival bound of all flows at the server, including the flow of interest
			// and thus calls computeArrivalBound with all flows present at the server and without a flow of interest.
			HashSet<ArrivalCurve> alphas_server = ArrivalBound.computeArrivalBound( network, config, server, network.getFlows( server ) );
			
			HashSet<Num> delay_bounds_server = new HashSet<Num>();
			HashSet<Num> backlog_bounds_server = new HashSet<Num>();
			
			Num delay_bound_s__min = Num.POSITIVE_INFINITY;
			Num backlog_bound_s__min = Num.POSITIVE_INFINITY;
			for ( ArrivalCurve alpha_candidate : alphas_server ) {
				// According to the call of computeOutputBound there's no left-over service curve calculation
				ServiceCurve beta_server = server.getServiceCurve();
				
				Num backlog_bound_server_alpha = BacklogBound.derive( alpha_candidate, beta_server );
				backlog_bounds_server.add( backlog_bound_server_alpha );
				
				if( backlog_bound_server_alpha.le( backlog_bound_s__min ) ) {
					backlog_bound_s__min = backlog_bound_server_alpha;
				}
				
				// Is this a single flow, i.e., does fifo per micro flow hold?
				boolean fifo_per_micro_flow = false;
				if ( network.getFlows( server ).size() == 1 ) {
					fifo_per_micro_flow = true;
				}

				Num delay_bound_server_alpha;
				if( config.multiplexingDiscipline() == MuxDiscipline.GLOBAL_FIFO
					|| ( config.multiplexingDiscipline() == MuxDiscipline.SERVER_LOCAL & server.useFifoMultiplexing() )
					|| fifo_per_micro_flow )
				{
					delay_bound_server_alpha = DelayBound.deriveFIFO( alpha_candidate, beta_server );	
				} else {
					delay_bound_server_alpha = DelayBound.deriveARB( alpha_candidate, beta_server );
				}
				delay_bounds_server.add( delay_bound_server_alpha );
				
				if( delay_bound_server_alpha.le( delay_bound_s__min ) ) {
					delay_bound_s__min = delay_bound_server_alpha;
				}
			}
			map__server__alphas.put( server, alphas_server );
			map__server__D_server.put( server, delay_bounds_server );
			map__server__B_server.put( server, backlog_bounds_server );
			
			// The delay bound is the sum of the minimum bound at each server on the path.
			delay_bound = Num.add( delay_bound, delay_bound_s__min );

			// The back bound is the maximum of the minimum bound at each server on the path.
			backlog_bound = Num.max( backlog_bound, backlog_bound_s__min );
		}
	}
	
	public HashMap<Server, HashSet<Num>> getServerDelayBoundMap(){
		return new HashMap<Server,HashSet<Num>>( map__server__D_server );
	}

	public HashMap<Server,HashSet<Num>> getServerBacklogBoundMap(){
		return new HashMap<Server,HashSet<Num>>( map__server__B_server );
	}
}
