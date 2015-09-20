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
 * @author Steffen Bondorf
 *
 */
public class PbooArrivalBound_Concatenation extends ArrivalBound {
	
	@SuppressWarnings("unused")
	private PbooArrivalBound_Concatenation() {}
	
	public PbooArrivalBound_Concatenation( Network network, Configuration config, HashSet<Flow> flows_to_bound, Flow flow_of_interest ) {
		super( network, config, flows_to_bound, flow_of_interest );
	}

	public HashSet<ArrivalCurve> computeArrivalBound( Link link ) throws Exception {
		HashSet<Flow> f_xfcaller = flows_to_bound;
		
		HashSet<ArrivalCurve> alphas_xfcaller = new HashSet<ArrivalCurve>( Collections.singleton( ArrivalCurve.createNullArrival() ) );
		if ( f_xfcaller.size() == 0 )
		{
			return alphas_xfcaller;
		}
		
		// Get the servers on common sub-path of f_xfcaller flows crossing link
		Server to = link.getDest();
		HashSet<Flow> f_to = network.getFlows( to );
		HashSet<Flow> f_xfcaller_to = SetUtils.getIntersection( f_to, f_xfcaller );
		f_xfcaller_to.remove( flow_of_interest );
		if ( f_xfcaller_to.size() == 0 )
		{
			return alphas_xfcaller;
		}
		Server from = network.findSplittingServer( to, f_xfcaller_to );
		Flow f_representative = f_xfcaller_to.iterator().next();
		
		Path common_subpath = f_representative.getSubPath( from, link.getSource() );
		
		// Calculate the left-over service curves on this sub-path by convolution of the individual left over service curves
		HashSet<ServiceCurve> betas_lo_subpath = new HashSet<ServiceCurve>();
		for ( Server s : common_subpath.getServers() )
		{
			HashSet<ServiceCurve> betas_lo_s;
			
			HashSet<Flow> f_xxfcaller_s = SetUtils.getDifference( network.getFlows( s ), f_xfcaller );
			f_xxfcaller_s.remove( flow_of_interest );

			HashSet<ArrivalCurve> alpha_xxfcaller_s = ArrivalBound.computeArrivalBound( network, config, s, f_xxfcaller_s, flow_of_interest );
			
			// Calculate the left-over service curve for this single server
			if( config.multiplexingDiscipline() == MuxDiscipline.GLOBAL_FIFO
					|| ( config.multiplexingDiscipline() == MuxDiscipline.SERVER_LOCAL & s.useFifoMultiplexing() ) )
			{
				betas_lo_s = LeftOverService.fifoMux( s.getServiceCurve(), alpha_xxfcaller_s );
			}
			else
			{
				betas_lo_s = LeftOverService.arbMux( s.getServiceCurve(), alpha_xxfcaller_s );				
			}
			
			// Check if there's any service left on this path. If not, the set only contains a null-service curve.
			if( betas_lo_s.size() == 1 && betas_lo_s.iterator().next().equals( ServiceCurve.createNullService() ) ) {
				throw new Exception( "No service left over during PBOO arrival bounding!" );
			}
			
			// Combine into the sub-path's left-over service curve
			betas_lo_subpath = Convolution.convolveAllPermutations( betas_lo_subpath, betas_lo_s );
		}
		
		// Next we need to know the arrival bound of f_xfcaller at the server 'from', i.e., at the above sub-path's source
		// in order to deconvolve it with beta_lo_s to get the arrival bound of the sub-path
		// Note that flows f_xfcaller that originate in 'from' are covered by this call of computeArrivalBound
		HashSet<ArrivalCurve> alpha_xfcaller_from = ArrivalBound.computeArrivalBound( network, config, from, f_xfcaller, flow_of_interest );

		if( config.useGamma() != GammaFlag.GLOBALLY_OFF  )
		{
			ServiceCurve gamma = ServiceCurve.createZeroDelayBurst(); 
			for ( Server s1 : common_subpath.getServers() ) {
				gamma = Convolution.convolve( gamma, s1.getGamma() );
			}
			alphas_xfcaller = Deconvolution.deconvolve( Convolution.convolve( alpha_xfcaller_from, gamma), betas_lo_subpath );
		}
		else
		{
			alphas_xfcaller = Deconvolution.deconvolve( alpha_xfcaller_from, betas_lo_subpath );
		}
		
		if( config.useExtraGamma() != GammaFlag.GLOBALLY_OFF )
		{
			ServiceCurve extra_gamma = ServiceCurve.createZeroDelayBurst(); 
			for ( Server s1 : common_subpath.getServers() ) {
				extra_gamma = Convolution.convolve( extra_gamma, s1.getExtraGamma() );
			}
			alphas_xfcaller = Convolution.convolve( alphas_xfcaller, extra_gamma );
		}

		return alphas_xfcaller;
	}
}
