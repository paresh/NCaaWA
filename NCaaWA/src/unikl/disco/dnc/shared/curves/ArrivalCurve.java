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

import java.util.LinkedList;

import unikl.disco.dnc.shared.Num;

/**
 * 
 * @author Frank A. Zdarsky
 * @author Steffen Bondorf
 *
 */
public class ArrivalCurve extends Curve {
	private ArrivalCurve() {
		super();
	}
	
	public ArrivalCurve( int segment_count ) {
		super( segment_count );
	}
	
	// Accepts string representations of Curve, ArrivalCurve and ServiceCurve
	public ArrivalCurve( String arrival_curve_str ) throws Exception {
		if( arrival_curve_str == null || arrival_curve_str.isEmpty() || arrival_curve_str.length() < 11 ) { // smallest service curve in terms of characters: AC{(0,0),0}
			throw new RuntimeException("Invalid string representation of a service curve.");
		}
		
		String curve_str;
		if ( ( arrival_curve_str.charAt( 0 ) == 'A' || arrival_curve_str.charAt( 0 ) == 'S' )
				&& arrival_curve_str.charAt( 1 ) == 'C' ) {
			curve_str = arrival_curve_str.substring( 2 );
		} else {
			curve_str = arrival_curve_str;
		}
		
		initializeFrom( new Curve( curve_str ) );
		
		// TODO I lost that ability, unfortunately
//		if( Configuration.performArrivalCurveChecks() && !this.isWideSenseIncreasing() ) {
//			throw new RuntimeException("Arrival curves can only be created from wide sense increasing curves.");
//		}
		
		makeArrivalCurve();
	}
	
	public ArrivalCurve( Curve curve ) {
		initializeFrom( curve );

		// TODO I lost that ability, unfortunately
//		if( Configuration.performArrivalCurveChecks() && !this.isWideSenseIncreasing() ) {
//			throw new RuntimeException("Arrival curves can only be created from wide sense increasing curves.");
//		}
		
		makeArrivalCurve();
	}

	private void makeArrivalCurve() {
		if ( this.getSegment(0).y.greater( Num.ZERO ) ) {
			LinearSegment[] segments_new = new LinearSegment[segments.length+1];
			segments_new[0] = LinearSegment.createNullSegment();
			
			System.arraycopy( this.segments, 0, segments_new, 1, this.segments.length );
			segments_new[1].leftopen = true;
			
			this.segments = segments_new;
		}
	}
	
	/**
	 * Creates a null arrival curve.
	 * 
	 * @return a <code>Curve</code> instance
	 */
	public static ArrivalCurve createNullArrival() {
		ArrivalCurve ac_result = new ArrivalCurve();
		ac_result.initializeFrom( Curve.NULL_CURVE );
		
		return ac_result;
	}
	
	/**
	 * Creates a burst curve with zero delay.
	 * 
	 * @return a <code>Curve</code> instance
	 */
	public static ArrivalCurve createZeroDelayBurst() {
		ArrivalCurve ac_result = new ArrivalCurve();
		ac_result.initializeFrom( Curve.ZERO_DELAY_BURST_CURVE );
		
		return ac_result;
	}

	/**
	 * Creates a new token bucket curve.
	 * 
	 * @param r the rate
	 * @param b the burstiness
	 * @return a <code>Curve</code> instance
	 */
	public static ArrivalCurve createTokenBucket( Num r, Num b ) {
		ArrivalCurve ac_result = new ArrivalCurve();
		ac_result.initializeFrom( Curve.createTokenBucket(r, b) );

		ac_result.token_buckets = new LinkedList<Curve>();
		ac_result.token_buckets.add( ac_result.copy() );
		ac_result.is_token_bucket = true;
		
		return ac_result;
	}

	/**
	 * Creates a new token bucket curve.
	 * 
	 * @param r the rate
	 * @param b the burstiness
	 * @return a <code>Curve</code> instance
	 */
	public static ArrivalCurve createTokenBucket( double r, double b ) {
		ArrivalCurve ac_result = new ArrivalCurve();
		ac_result.initializeFrom( Curve.createTokenBucket(r, b) );
		
		ac_result.token_buckets = new LinkedList<Curve>();
		ac_result.token_buckets.add( ac_result.copy() );
		ac_result.is_token_bucket = true;
		
		return ac_result;
	}

	/**
	 * Creates a new curve from a list of token bucket curves.
	 * 
	 * @param token_buckets a list of token bucket curves
	 * @return a <code>Curve</code> instance
	 */
	public static ArrivalCurve createFromTokenBuckets( LinkedList<Curve> token_buckets ) {
		ArrivalCurve ac_result = new ArrivalCurve();
		ac_result.initializeFrom( Curve.createFromTokenBuckets( token_buckets ) );
		
		return ac_result;
	}
	
	public Num getBurst() {
		return fLimitRight( Num.ZERO );
	}
	
	public ArrivalCurve copy() {
		ArrivalCurve ac_copy = new ArrivalCurve();
		ac_copy.initializeFrom( this );
		
		return ac_copy;
	}

	public static ArrivalCurve add( ArrivalCurve c1, ArrivalCurve c2 ) {
		return new ArrivalCurve( Curve.add( c1, c2 ) );
	}

	public static ArrivalCurve add( ArrivalCurve c1, Num dy ) {
		return new ArrivalCurve( Curve.add( c1, dy ) );
	}
	
	/**
	 * Returns a copy of this curve that is shifted to the right by <code>dx</code>,
	 * i.e. g(x) = f(x-dx).
	 * 
	 * @param dx the offset to shift the curve.
	 * @return the shifted curve.
	 */
	public ArrivalCurve shiftRight( Num dx ) {
		return new ArrivalCurve( Curve.shiftRight( this, dx ) );
	}
	
	/**
	 * Returns a copy of this curve that is shifted to the left by <code>dx</code>,
	 * i.e. g(x) = f(x+dx). Note that the new curve is clipped at the y-axis so
	 * that in most cases <code>c.shiftLeftClipping(dx).shiftRight(dx) != c</code>!
	 * 
	 * @param dx the offset to shift the curve.
	 * @return the shifted curve.
	 */
	public ArrivalCurve shiftLeftClipping( Num dx ) {
		return new ArrivalCurve( Curve.shiftLeftClipping( this, dx ) );
	}
	
	/**
	 * Returns a string representation of this curve.
	 * 
	 * @return the curve represented as a string.
	 */
	@Override
	public String toString() {
		return "AC" + super.toString();
	}
}
