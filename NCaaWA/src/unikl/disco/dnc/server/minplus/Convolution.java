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
import unikl.disco.dnc.shared.curves.Curve;
import unikl.disco.dnc.shared.curves.LinearSegment;
import unikl.disco.dnc.shared.curves.ServiceCurve;

/**
 * 
 * @author Frank A. Zdarsky
 * @author Steffen Bondorf
 *
 */
public class Convolution {
	public static HashSet<ArrivalCurve> convolve( HashSet<ArrivalCurve> arrival_curves, HashSet<ServiceCurve> service_curves ) {
		HashSet<ArrivalCurve> results = new HashSet<ArrivalCurve>();
		
		for ( ServiceCurve beta : service_curves ) {
			for ( ArrivalCurve alpha : arrival_curves ) {
				results.add( convolve( alpha, beta ) );
			}
		}
		
		return results;
	}
	
	public static HashSet<ArrivalCurve> convolve( HashSet<ArrivalCurve> arrival_curves, ServiceCurve service_curve ) {
		HashSet<ArrivalCurve> results = new HashSet<ArrivalCurve>();
		
		for ( ArrivalCurve alpha : arrival_curves ) {
			results.add( convolve( alpha, service_curve ) );
		}
		
		return results;
	}
	
	public static ArrivalCurve convolve( ArrivalCurve arrival_curve, ServiceCurve service_curve ) {
		if ( service_curve.getLatency().equals( Num.POSITIVE_INFINITY ) ) {
			return ArrivalCurve.createNullArrival();
		}

		ArrivalCurve result = new ArrivalCurve( Curve.min( service_curve.removeLatency(), arrival_curve ) );
		result.shiftRight( service_curve.getLatency() );
		
		result.beautify();
		return result;
	}
	
	// Java won't let me call this method "convolve" because it does not care about the Sets' types; tells that there's already another method taking the same arguments.
	public static HashSet<ServiceCurve> convolveAllPermutations( HashSet<ServiceCurve> service_curves_1, HashSet<ServiceCurve> service_curves_2 ) {
		
		if ( service_curves_1.isEmpty() ) {
			return service_curves_2;
		}
		if ( service_curves_2.isEmpty() ) {
			return service_curves_1;
		}
		
		HashSet<ServiceCurve> results = new HashSet<ServiceCurve>();
		
		for ( ServiceCurve beta_1 : service_curves_1 ) {
			for ( ServiceCurve beta_2 : service_curves_2 ) {
				results.add( convolve( beta_1, beta_2 ) );
			}
		}
		
		return results;
	}
	
	/**
	 * Returns the convolution of two curve, which must be convex
	 * 
	 * @param service_curve_1 The first curve to convolve with.
	 * @param service_curve_2 The second curve to convolve with.
	 * @return The convolved curve.
	 */
	public static ServiceCurve convolve( ServiceCurve service_curve_1, ServiceCurve service_curve_2 ) {
		if ( service_curve_1.equals( ServiceCurve.createZeroDelayBurst() ) ) {
			return service_curve_2.copy();
		} else {
			if ( service_curve_2.equals( ServiceCurve.createZeroDelayBurst() ) ) {
				return service_curve_1.copy();
			}
		}
		
		if ( service_curve_1.isBurstDelay() && service_curve_1.getSegment(1).getX().equals( Num.ZERO ) ) {
			return service_curve_2.copy();
		}
		if ( service_curve_2.isBurstDelay() && service_curve_2.getSegment(1).getX().equals( Num.ZERO ) ) {
			return service_curve_1.copy();
		}

		ServiceCurve result = new ServiceCurve();
		
		Num x = Num.ZERO;
		Num y = Num.add( service_curve_1.f( Num.ZERO ), service_curve_2.f( Num.ZERO ) );
		Num grad = Num.ZERO;
		LinearSegment s = new LinearSegment( x, y, grad, false );
		result.addSegment(s);

		int i1 = (service_curve_1.isRealDiscontinuity(0)) ? 1 : 0;
		int i2 = (service_curve_2.isRealDiscontinuity(0)) ? 1 : 0;
		if (i1 > 0 || i2 > 0) {
			x = Num.ZERO;
			y = Num.add( service_curve_1.fLimitRight( Num.ZERO ), service_curve_2.fLimitRight( Num.ZERO ) );
			grad = Num.ZERO;
			s = new LinearSegment( x, y, grad, true );
			
			result.addSegment(s);
		}

		while(i1 < service_curve_1.getSegmentCount() || i2 < service_curve_2.getSegmentCount()) {
			if ( service_curve_1.getSegment(i1).getGrad().less( service_curve_2.getSegment(i2).getGrad() ) ) {
				if (i1+1 >= service_curve_1.getSegmentCount()) {
					result.getSegment(result.getSegmentCount()-1).setGrad( service_curve_1.getSegment(i1).getGrad() );
					break;
				}

				x = Num.add( result.getSegment( result.getSegmentCount()-1 ).getX(),
						( Num.sub( service_curve_1.getSegment( i1+1 ).getX(), service_curve_1.getSegment( i1 ).getX() ) ) );
				y = Num.add( result.getSegment( result.getSegmentCount()-1 ).getY(),
						( Num.sub( service_curve_1.getSegment( i1+1 ).getY(), service_curve_1.getSegment( i1 ).getY() ) ) );
				grad = Num.ZERO;
				s = new LinearSegment( x, y, grad, true );

				result.getSegment(result.getSegmentCount()-1).setGrad( service_curve_1.getSegment(i1).getGrad() );
				result.addSegment(s);
				
				i1++;
			} else {
				if (i2+1 >= service_curve_2.getSegmentCount()) {
					result.getSegment(result.getSegmentCount()-1).setGrad( service_curve_2.getSegment(i2).getGrad() );
					break;
				}
				
				x = Num.add( result.getSegment( result.getSegmentCount()-1 ).getX(),
						( Num.sub( service_curve_2.getSegment( i2+1 ).getX(), service_curve_2.getSegment( i2 ).getX() ) ) );
				y = Num.add( result.getSegment( result.getSegmentCount()-1 ).getY(),
						( Num.sub( service_curve_2.getSegment( i2+1 ).getY(), service_curve_2.getSegment( i2 ).getY() ) ) );
				grad = Num.ZERO;
				s = new LinearSegment( x, y, grad, true );
				
				result.getSegment(result.getSegmentCount()-1).setGrad( service_curve_2.getSegment(i2).getGrad() );
				result.addSegment(s);
				
				i2++;
			}
		}
		
		result.beautify();
		return result;
	}
}
