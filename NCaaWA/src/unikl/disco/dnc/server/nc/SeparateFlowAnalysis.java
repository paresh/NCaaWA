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

import unikl.disco.dnc.server.minplus.Convolution;
import unikl.disco.dnc.shared.Configuration.MuxDiscipline;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
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
public class SeparateFlowAnalysis extends Analysis
{
	HashMap<Server,HashSet<ServiceCurve>> map__server__betas_lo = new HashMap<Server,HashSet<ServiceCurve>>();

	protected SeparateFlowAnalysis(){}
	
	/**
	 * Performs a separated flow analysis for the <code>flow_of_interest</code>.
	 * 
	 * This analysis first blends out the flow of interest and then computes for
	 * each server along this flow's path the left-over service curve that
	 * results if all remaining flows crossing this server receive their maximum
	 * amount of service. Then all left-over service curves are concatenated to
	 * receive the end-to-end service curve from the perspective of the flow of
	 * interest.
	 * 
	 * @param flow_of_interest
	 *            The flow for which the end-to-end service curve shall be
	 *            computed
	 */
	protected void performEnd2EndAnalysis() throws Exception {
		betas_e2e = getSfaServiceCurves();
		this.deriveBounds();
	}
		
	protected HashSet<ServiceCurve> getSfaServiceCurves() throws Exception {
		if ( flow_of_interest == null ) {
			throw new Exception( "Flow of interest not set" );
		}
		
		return getSfaServiceCurves( flow_of_interest.getPath() );
	}
	
	protected HashSet<ServiceCurve> getSfaServiceCurves( Flow flow_of_interest ) throws Exception {
		this.flow_of_interest = flow_of_interest;
		return betas_e2e = getSfaServiceCurves();
	}
	
	protected HashSet<ServiceCurve> getSfaServiceCurves( Flow flow_of_interest, Path path ) throws Exception {
		this.flow_of_interest = flow_of_interest;
		return betas_e2e = getSfaServiceCurves( path );
	}
	
	public HashSet<ServiceCurve> getSfaServiceCurves( Path path ) throws Exception {
		HashSet<ServiceCurve> betas_lofoi_path = new HashSet<ServiceCurve>();
		HashSet<ServiceCurve> betas_lofoi_s = new HashSet<ServiceCurve>();
		HashSet<ServiceCurve> betas_lofoi_path_s = new HashSet<ServiceCurve>();
		
		// Convolve all left over service curves, server by server
		for ( Server server : path.getServers() ) {
			betas_lofoi_s = new HashSet<ServiceCurve>();
			betas_lofoi_path_s = new HashSet<ServiceCurve>();
			
			HashSet<Flow> f_xfoi_server = network.getFlows( server );		
			f_xfoi_server.remove( flow_of_interest );

			ServiceCurve beta_lofoi = server.getServiceCurve();
			
			if ( f_xfoi_server.isEmpty() ) {
				betas_lofoi_s.add( beta_lofoi );
			} else {																// network, server, flows_to_bound, flows_lower_priority
				HashSet<ArrivalCurve> alpha_xfois = ArrivalBound.computeArrivalBound( network, config, server, f_xfoi_server, flow_of_interest );
				
				// Calculate the left-over service curve for the flow of interest
				if( config.multiplexingDiscipline() == MuxDiscipline.GLOBAL_FIFO
					|| ( config.multiplexingDiscipline() == MuxDiscipline.SERVER_LOCAL & server.useFifoMultiplexing() ) )
				{
					for ( ArrivalCurve alpha_xfoi : alpha_xfois ) {
						betas_lofoi_s.add( LeftOverService.fifoMux( beta_lofoi, alpha_xfoi ) );
					}
				}
				else
				{
					for ( ArrivalCurve alpha_xfoi : alpha_xfois ) {
						betas_lofoi_s.add( LeftOverService.arbMux( beta_lofoi, alpha_xfoi ) );
					}
				}
				map__server__alphas.put( server, alpha_xfois );
			}
			map__server__betas_lo.put( server, betas_lofoi_s );
			
			if( !betas_lofoi_path.isEmpty() ) {
				for( ServiceCurve sc_path : betas_lofoi_path ) {
					for( ServiceCurve sc_s : betas_lofoi_s ) {
						betas_lofoi_path_s.add( Convolution.convolve( sc_path, sc_s ) );
					}
				}
			} else {
				betas_lofoi_path_s = betas_lofoi_s;
			}
			betas_lofoi_path = betas_lofoi_path_s;
		}
		return betas_lofoi_path;
	}

	public HashMap<Server,HashSet<ServiceCurve>> getServerLeftOverBetasMap(){
		return new HashMap<Server,HashSet<ServiceCurve>>( map__server__betas_lo );
	}
}
