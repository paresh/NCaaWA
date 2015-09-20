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

import java.util.LinkedList;

import unikl.disco.dnc.shared.Num;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.Curve;
import unikl.disco.dnc.shared.curves.ServiceCurve;

/**
 * 
 * @author Frank A. Zdarsky
 * @author Steffen Bondorf
 *
 */
public class BacklogBound {
	public static Num derive( ArrivalCurve arrival_curve, ServiceCurve service_curve ) {
		if ( service_curve.equals( ServiceCurve.createZeroDelayBurst() ) ) {
			return Num.ZERO;
		}
		if ( arrival_curve.getSustainedRate().greater( service_curve.getSustainedRate() ) ) {
			return Num.POSITIVE_INFINITY;
		}
		
		// The computeInflectionPoints based method does not work for 
		// single rate service curves (without latency)
		// in conjunction with token bucket arrival curves
		// because their common inflection point is in zero, 
		// where the arrival curve is 0.0 by definition.
		// This leads to a vertical deviation of 0 the arrival curve's burst
		// (or infinity which is already handled by the first if-statement)
		
		// Solution: 
		// Start with the burst as minimum vertical deviation
		
		Num result = arrival_curve.fLimitRight( Num.ZERO );
				
		LinkedList<Num> xcoords = Curve.computeInflectionPointsX( arrival_curve, service_curve );	
		for( int i = 0; i < xcoords.size(); i++ ) {
			Num ip_x = ( (Num) xcoords.get( i ) );

			Num backlog = Num.sub( arrival_curve.f( ip_x ), service_curve.f( ip_x ) );
			result = Num.max( result, backlog );
		}
		return result;
	}
}
