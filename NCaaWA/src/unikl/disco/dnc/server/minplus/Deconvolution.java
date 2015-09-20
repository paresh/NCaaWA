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

package unikl.disco.dnc.server.minplus;

import java.util.HashSet;

import unikl.disco.dnc.shared.Num;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.ServiceCurve;

/**
 * 
 * @author Frank A. Zdarsky
 * @author Steffen Bondorf
 *
 */
public class Deconvolution {
	public static HashSet<ArrivalCurve> deconvolve( HashSet<ArrivalCurve> arrival_curves, HashSet<ServiceCurve> service_curves ) {
		HashSet<ArrivalCurve> results = new HashSet<ArrivalCurve>();
		
		for ( ServiceCurve beta : service_curves ) {
			for ( ArrivalCurve alpha : arrival_curves ) {
				results.add( deconvolve( alpha, beta ) );
			}
		}
		
		return results;
	}

	public static HashSet<ArrivalCurve> deconvolve( HashSet<ArrivalCurve> arrival_curves, ServiceCurve service_curve ) {
		HashSet<ArrivalCurve> results = new HashSet<ArrivalCurve>();
		
		for ( ArrivalCurve alpha : arrival_curves ) {
			results.add( deconvolve( alpha, service_curve ) );
		}
		
		return results;
	}
	
	/**
	 * Returns the deconvolution of an (almost) concave arrival curve and
	 * a convex service curve.
	 * 
	 * @param arrival_curve The (almost) concave arrival curve.
	 * @param service_curve The convex service curve.
	 * @return The deconvolved curve, an arrival curve.
	 */
	public static ArrivalCurve deconvolve( ArrivalCurve arrival_curve, ServiceCurve service_curve ) {
		return deconvolve( arrival_curve, service_curve, false );
	}
		
	public static ArrivalCurve deconvolve( ArrivalCurve arrival_curve, ServiceCurve service_curve, boolean perform_checks ) {
		if( service_curve.equals( ServiceCurve.createZeroDelayBurst() ) ) {
			return arrival_curve;
		}
		if( arrival_curve.equals( ArrivalCurve.createNullArrival() ) ) {
			return arrival_curve;
		}
		if( service_curve.equals( ServiceCurve.createNullService() )
				|| service_curve.getLatency().equals( Num.POSITIVE_INFINITY )
				|| ( service_curve.getSustainedRate().equals( Num.ZERO ) && service_curve.getSegment( service_curve.getSegmentCount() - 1 ).getY().equals( Num.ZERO ) ) ) {
			return ArrivalCurve.createNullArrival();
		}
		
		if ( perform_checks ) {
			if( !arrival_curve.isAlmostConcave() ) {
				throw new IllegalArgumentException("Arrival curve of deconvolution must be almost concave.");
			}
			if ( !service_curve.isConvex() ) {
				throw new IllegalArgumentException("Service curve of deconvolution must be convex.");
			}
		}
		
		int i_this  = 0;
		int i_other = 0;

		// Advance this_iterator to concave part
		while(i_this < arrival_curve.getSegmentCount()) {
			if ( arrival_curve.getSegment( i_this ).getGrad().greater( Num.ZERO ) ) {
				break;
			}
			i_this++;
		}
		// Advance other_iterator to first IP after the start of this's concave part
		while(i_other < service_curve.getSegmentCount()) {
			if ( service_curve.getSegment( i_other ).getX().ge( arrival_curve.getSegment( i_this ).getX() ) ) {
				break;
			}
			i_other++;
		}

		// From here on, search both curve's IPs until grad(other)>=grad(this)
		Num kj = Num.POSITIVE_INFINITY;
		while(i_this < arrival_curve.getSegmentCount() || i_other < service_curve.getSegmentCount()) {
			Num x_this = ( i_this < arrival_curve.getSegmentCount() ) ?
					arrival_curve.getSegment( i_this ).getX() : Num.POSITIVE_INFINITY;
			Num x_other = (i_other < service_curve.getSegmentCount()) ?
					service_curve.getSegment(i_other).getX() : Num.POSITIVE_INFINITY;

			if ( x_other.le( x_this ) ) {
				if ( service_curve.getSegment(i_other).getGrad().ge
						( arrival_curve.getGradientLimitRight( service_curve.getSegment( i_other ).getX() ) ) ) {
					kj = service_curve.getSegment(i_other).getX();
					break;
				}
				i_other++;
			} else {
				if ( service_curve.getGradientLimitRight( arrival_curve.getSegment( i_this ).getX() ).ge
						( arrival_curve.getSegment( i_this ).getGrad() ) ) {
					kj = arrival_curve.getSegment(i_this).getX();
					break;
				}
				i_this++;
			}
		}

		if ( kj.less( Num.POSITIVE_INFINITY ) ) {
			ArrivalCurve result = ArrivalCurve.add( arrival_curve.shiftLeftClipping( kj ), Num.negate( service_curve.f( kj ) ) );
			result.beautify();
			return result;
		} else {
			return ArrivalCurve.createZeroDelayBurst();
		}
	}
}
