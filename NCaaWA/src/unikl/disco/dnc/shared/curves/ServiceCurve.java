/*
 * This file is part of the Disco Deterministic Network Calculator v2.0.3 "Hydra".
 *
 * Copyright (C) 2005 - 2007 Frank A. Zdarsky
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

package unikl.disco.dnc.shared.curves;

import java.io.Serializable;
import java.util.LinkedList;

import unikl.disco.dnc.shared.Num;

/**
 * 
 * @author Frank A. Zdarsky
 * @author Steffen Bondorf
 *
 */
public class ServiceCurve extends Curve implements Serializable {
	public ServiceCurve() {
		super();
	}

	public ServiceCurve( int segment_count ) {
		super( segment_count );
	}
	
	// Accepts string representations of Curve, ArrivalCurve and ServiceCurve
	public ServiceCurve( String service_curve_str ) throws Exception {
		if( service_curve_str == null || service_curve_str.isEmpty() || service_curve_str.length() < 11 ) { // smallest service curve in terms of characters: SC{(0,0),0}
			throw new RuntimeException("Invalid string representation of a service curve.");
		}
		
		String curve_str;
		if ( ( service_curve_str.charAt( 0 ) == 'A' || service_curve_str.charAt( 0 ) == 'S' )
				&& service_curve_str.charAt( 1 ) == 'C' ) {
			curve_str = service_curve_str.substring( 2 );
		} else {
			curve_str = service_curve_str;
		}
		
		initializeFrom( new Curve( curve_str ) );
	}
	
	public ServiceCurve( Curve curve ) {
		initializeFrom( curve );

		// Dead code in v2.0.1 because there !this.isWideSenseIncreasing() is called after super( curve.getSegmentCount() ).
		// That cannot lead to a reasonable check.
		// Changing !this.isWideSenseIncreasing() to !curve.isWideSenseIncreasing() produces the same errors as here
//		if( Configuration.performServiceCurveChecks() && !this.isWideSenseIncreasing() ) {
//			throw new RuntimeException("Service curves can only be created from wide sense increasing curves.");
//		}
	}
	
	/**
	 * Creates a null arrival curve.
	 * 
	 * @return a <code>Curve</code> instance
	 */
	public static ServiceCurve createNullService() {
		ServiceCurve sc_result = new ServiceCurve();
		sc_result.initializeFrom( NULL_CURVE );
		
		return sc_result;
	}
	
	/**
	 * Creates a burst curve with zero delay.
	 * 
	 * @return a <code>Curve</code> instance
	 */
	public static ServiceCurve createZeroDelayBurst() {
		ServiceCurve sc_result = new ServiceCurve();
		sc_result.initializeFrom( ZERO_DELAY_BURST_CURVE );
		
		return sc_result;
	}

	/**
	 * Creates a burst delay curve.
	 * 
	 * @param t the delay, which must be >= 0.0
	 * @return a <code>Curve</code> instance
	 */
	public static ServiceCurve createBurstDelay( Num t ) {
		ServiceCurve sc_result = new ServiceCurve();
		sc_result.initializeFrom( Curve.createBurstDelay( t ) );
		
		return sc_result;
	}
	
	/**
	 * Creates a new rate latency curve.
	 * 
	 * @param r the rate
	 * @param t the latency
	 * @return a <code>Curve</code> instance
	 */
	public static ServiceCurve createRateLatency( Num r, Num t ) {
		ServiceCurve sc_result = new ServiceCurve();
		sc_result.initializeFrom( Curve.createRateLatency( r, t ) );
		
		sc_result.rate_latencies = new LinkedList<Curve>();
		sc_result.rate_latencies.add( sc_result.copy() );
		sc_result.is_rate_latency = new Boolean( true );

		return sc_result;
	}

	/**
	 * Creates a new rate latency curve.
	 * 
	 * @param r the rate
	 * @param t the latency
	 * @return a <code>Curve</code> instance
	 */
	public static ServiceCurve createRateLatency( double r, double t ) {
		ServiceCurve sc_result = new ServiceCurve();
		sc_result.initializeFrom( Curve.createRateLatency( new Num( r ), new Num( t ) ) );
		
		sc_result.rate_latencies = new LinkedList<Curve>();
		sc_result.rate_latencies.add( sc_result.copy() );
		sc_result.is_rate_latency = new Boolean( true );
		
		return sc_result;
	}

	public ServiceCurve copy() {
		ServiceCurve sc_copy = new ServiceCurve();
		sc_copy.initializeFrom( this );
		
		return sc_copy;
	}
	
	public static ServiceCurve add( ServiceCurve c1, ServiceCurve c2 ) {
		return new ServiceCurve( Curve.add(c1, c2) );
	}
	
	public static ServiceCurve sub( ServiceCurve c1, ArrivalCurve c2 ) {
		return new ServiceCurve( Curve.sub(c1, c2) );
	}
	
	public static ServiceCurve min( ServiceCurve c1, ServiceCurve c2 ) {
		return new ServiceCurve( Curve.min(c1, c2) );
	}
	
	public static ServiceCurve max( ServiceCurve c1, ServiceCurve c2 ) {
		return new ServiceCurve( Curve.max(c1, c2) );
	}
	
	/**
	 * Returns a copy of curve bounded at the x-axis.
	 * 
	 * @return the bounded curve.
	 */
	public static ServiceCurve boundAtXAxis( ServiceCurve service_curve ) {
		return new ServiceCurve( Curve.boundAtXAxis( service_curve ) ); 
	}
	
	/**
	 * Returns a copy of this curve with latency removed, i.e. shifted left by
	 * the latency.
	 * 
	 * @return a copy of this curve without latency
	 */
	public ServiceCurve removeLatency() {
		return new ServiceCurve( Curve.removeLatency( this ) );
	}
	
	public boolean equals( ServiceCurve other ) {
		return ( (Curve)this ).equals( other );
	}
	
	/**
	 * Returns a string representation of this curve.
	 * 
	 * @return the curve represented as a string.
	 */
	@Override
	public String toString() {
		return "SC" + super.toString();
	}
}
