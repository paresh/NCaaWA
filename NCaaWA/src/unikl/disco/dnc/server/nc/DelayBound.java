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
public class DelayBound {
	public static Num deriveARB( ArrivalCurve arrival_curve, ServiceCurve service_curve ) {
		return Curve.getXIntersection( arrival_curve, service_curve );
	}

	// Single flow to be bound, i.e., fifo per micro flow holds
	public static Num deriveFIFO( ArrivalCurve arrival_curve, ServiceCurve service_curve ) {
		if ( arrival_curve.getSustainedRate().greater( service_curve.getSustainedRate() ) ) {
			return Num.POSITIVE_INFINITY;
		}

		Num result = Num.NEGATIVE_INFINITY;
		for( int i = 0; i < arrival_curve.getSegmentCount(); i++ ) {
			Num ip_y = arrival_curve.getSegment( i ).getY();

			Num delay = Num.sub( service_curve.f_inv( ip_y, true ), arrival_curve.f_inv( ip_y, false ) );
			result = Num.max( result, delay );
		}
		for( int i = 0; i < service_curve.getSegmentCount(); i++ ) {
			Num ip_y = service_curve.getSegment( i ).getY();

			Num delay = Num.sub( service_curve.f_inv( ip_y, true ), arrival_curve.f_inv( ip_y, false ) );
			result = Num.max( result, delay );
		}
		
		return Num.max( Num.ZERO, result );
	}
}
