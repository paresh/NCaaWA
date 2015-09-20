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

package unikl.disco.dnc.shared.curves;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;

import unikl.disco.dnc.shared.Num;

/**
 * Class representing a piecewise linear curve, defined on [0,inf).<br/>
 * The curve is stored as an array of <code>LinearSegment</code>
 * objects. Each of these objects defines a linear piece of the
 * curve from one inflection point up to, but not including, the
 * next. It is possible to define discontinuities by defining two
 * subsequent <code>LinearSegment</code> instances which start at
 * the same inflection point. In this case, the second segment
 * needs to have <code>leftopen</code> set to <code>true</code> to
 * indicate that the inflection point is excluded from the second
 * segment.<br/>
 * All arithmetic operations on a curve return a new instance of class
 * <code>Curve</code>.<br/>
 *
 * @author Frank A. Zdarsky
 * @author Steffen Bondorf
 * 
 */
public class Curve implements Serializable {
	public static Num EPSILON = Num.getEpsilon();
//	public static final Num EPSILON = Num.getEpsilon();

	private enum Operator { OPERATOR_ADD, OPERATOR_SUB, OPERATOR_MIN, OPERATOR_MAX }
//	protected static final int OPERATOR_ADD = 0;
//	protected static final int OPERATOR_SUB = 1;
//	protected static final int OPERATOR_MIN = 2;
//	protected static final int OPERATOR_MAX = 3;

	protected LinearSegment[] segments;

	private boolean has_token_bucket_meta_info = false;
	protected boolean is_token_bucket = false;
	protected LinkedList<Curve> token_buckets = new LinkedList<Curve>();

	private boolean has_rate_latency_meta_info = false;
	protected boolean is_rate_latency = false;
	protected LinkedList<Curve> rate_latencies = new LinkedList<Curve>();

	protected static Curve NULL_CURVE = createHorizontal( Num.ZERO );
	protected static Curve ZERO_DELAY_BURST_CURVE = createBurstDelay( Num.ZERO );
//	protected static final Curve NULL_CURVE = createHorizontal( Num.ZERO );
//	protected static final Curve ZERO_DELAY_BURST_CURVE = createBurstDelay( Num.ZERO );
	
	/**
	 * Creates a null curve.
	 * 
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createNullCurve() {
		return NULL_CURVE.copy();
	}
	
	/**
	 * Creates a burst curve with zero delay.
	 * 
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createZeroDelayBurst() {
		return ZERO_DELAY_BURST_CURVE.copy();
	}
	
	/**
	 * Creates a horizontal curve.
	 * 
	 * @param y the y-intercept of the curve
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createHorizontal( Num y ) {
		Curve c = new Curve(1);
		c.getSegment(0).y = y;
		return c;
	}

	/**
	 * Creates a horizontal curve.
	 * 
	 * @param y the y-intercept of the curve
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createHorizontal( double y ) {
		return createHorizontal( new Num( y ) );
	}

	/**
	 * Creates a burst delay curve.
	 * 
	 * @param t the delay, which must be >= 0.0
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createBurstDelay( Num t ) {
		Curve c = new Curve(2);

		c.getSegment(0).x       = Num.ZERO;
		c.getSegment(0).y       = Num.ZERO;
		c.getSegment(0).grad     = Num.ZERO;
		c.getSegment(0).leftopen = false;
		
		c.getSegment(1).x       = Num.max( Num.ZERO, t );
		c.getSegment(1).y       = Num.POSITIVE_INFINITY;
		c.getSegment(1).grad     = Num.ZERO;
		c.getSegment(1).leftopen = true;
		
		return c;
	}
	
	/**
	 * Creates a burst delay curve.
	 * 
	 * @param t the delay, which must be >= 0.0
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createBurstDelay( double t ) {
		return createBurstDelay( new Num( t ) );
	}

	/**
	 * Creates a new token bucket curve.
	 * 
	 * @param r the rate
	 * @param b the burstiness
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createTokenBucket( Num r, Num b ) {
		Curve c = new Curve(2);

		c.getSegment(0).x       = Num.ZERO;
		c.getSegment(0).y       = Num.ZERO;
		c.getSegment(0).grad     = Num.ZERO;
		c.getSegment(0).leftopen = false;
		
		c.getSegment(1).x       = Num.ZERO;
		c.getSegment(1).y       = b;
		c.getSegment(1).grad     = r;
		c.getSegment(1).leftopen = true;
		
		return c;
	}

	/**
	 * Creates a new token bucket curve.
	 * 
	 * @param r the rate
	 * @param b the burstiness
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createTokenBucket( double r, double b ) {
		return createTokenBucket( new Num( r ), new Num( b ) ) ;
	}

	/**
	 * Creates a new rate latency curve.
	 * 
	 * @param r the rate
	 * @param t the latency
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createRateLatency( Num r, Num t ) {
		Curve c;
		if ( t.equals( Num.ZERO ) ) {
			c = new Curve(1);

			c.getSegment(0).x       = Num.ZERO;
			c.getSegment(0).y       = Num.ZERO;
			c.getSegment(0).grad     = r;
			c.getSegment(0).leftopen = false;
		} else {
			c = new Curve(2);
			
			c.getSegment(0).x       = Num.ZERO;
			c.getSegment(0).y       = Num.ZERO;
			c.getSegment(0).grad     = Num.ZERO;
			c.getSegment(0).leftopen = false;

			c.getSegment(1).x       = t;
			c.getSegment(1).y       = Num.ZERO;
			c.getSegment(1).grad     = r;
			c.getSegment(1).leftopen = false;
		}
		
		return c;
	}

	/**
	 * Creates a new rate latency curve.
	 * 
	 * @param r the rate
	 * @param t the latency
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createRateLatency( double r, double t ) {
		return createRateLatency( new Num( r ), new Num( t ) );
	}

	/**
	 * Creates a new curve from a list of token bucket curves.
	 * 
	 * @param token_buckets a list of token bucket curves
	 * @return a <code>Curve</code> instance
	 */
	public static Curve createFromTokenBuckets( LinkedList<Curve> token_buckets ) {
		Curve c = ZERO_DELAY_BURST_CURVE.copy();
		for (Iterator<Curve> tb_iter = token_buckets.iterator(); tb_iter.hasNext(); ) {
			c = min( c, (Curve) tb_iter.next() );
		}
		return c;
	}
	
	/**
	 * Creates an empty <code>Curve</code> instance.
	 */
	protected Curve() {
		segments = new LinearSegment[0];
	}
	
	/**
	 * Creates a <code>Curve</code> instance with <code>segment_count</code>
	 * empty <code>LinearSegment</code> instances.
	 * 
	 * @param segment_count the number of segments
	 */
	public Curve( int segment_count ) {
		segments = new LinearSegment[segment_count];
		for (int i = 0; i < segment_count; i++) {
			segments[i] = LinearSegment.createHorizontalLine( 0.0 );
		}
	}
	
	public Curve( String curve_str ) throws Exception {
		if( curve_str.charAt( 0 ) != '{' || curve_str.charAt( curve_str.length()-1 ) != '}' ) {
			throw new RuntimeException( "Invalid string representation of a curve." );
		}
		
		// Remove enclosing curly brackets
		String curve_str_internal = curve_str.substring( 1, curve_str.length()-1 );
		
		String[] segments_to_parse = curve_str_internal.split( ";" );
		segments = new LinearSegment[ segments_to_parse.length ];
		
		for( int i = 0; i<segments_to_parse.length; i++ ) {
			segments[i] = new LinearSegment( segments_to_parse[i] );
		}
	}

	/**
	 * Creates a <code>Curve</code> instance with inflection points at
	 * the x-coordinates <code>ip_xs</code> and with slopes defined
	 * by <code>ip_ms</code>. The y-coordinates are computed automatically.
	 * 
	 * @param ip_xs
	 * @param ip_ms
	 */
	public Curve( Num[] ip_xs, Num[] ip_ms ) {
		this(ip_xs.length);
		for (int i = 0; i < segments.length; i++) {
			segments[i].x   = ip_xs[i];
			segments[i].grad = ip_ms[i];
		}
		markDiscontinuities();
		computeYs(1);
	}

	/**
	 * Creates a <code>Curve</code> instance with inflection points at
	 * the x-coordinates <code>ip_xs</code> and with slopes defined
	 * by <code>ip_ms</code>. This constructor accepts an array of
	 * y-coordinates so that discontinuities can be created. The
	 * parameter <code>compute_y_start</code> indicates from which
	 * inflection point (starting with 0), the y-coordinate is again
	 * computed automatically. Note that all arrays need to have the
	 * same length.
	 * 
	 * @param ip_xs
	 * @param ip_ms
	 */
	public Curve( Num[] ip_xs, Num[] ip_ys, Num[] ip_ms,
			int compute_y_start) {
		this(ip_xs.length);
		for (int i = 0; i < segments.length; i++) {
			segments[i].x   = ip_xs[i];
			segments[i].y   = ip_ys[i];
			segments[i].grad = ip_ms[i];
		}
		markDiscontinuities();
		if (compute_y_start >= 1) {
			computeYs(compute_y_start);
		}
	}
	
	protected void initializeFrom( Curve curve ) {
		segments = new LinearSegment[curve.getSegmentCount()];

		has_token_bucket_meta_info = curve.has_token_bucket_meta_info;
		is_token_bucket = curve.is_token_bucket;
		token_buckets = curve.token_buckets;

		has_rate_latency_meta_info = curve.has_rate_latency_meta_info;
		is_rate_latency = curve.is_rate_latency;
		rate_latencies = curve.rate_latencies;
		
		for ( int i = 0; i < curve.getSegmentCount(); i++ ) {
			segments[i] = curve.getSegment( i ).copy();
		}
	}
	
	/**
	 * Returns a copy of this instance.
	 * 
	 * @return a copy of this instance.
	 */
	public Curve copy() {
		Curve c_copy = new Curve();
		c_copy.initializeFrom( this );
		return c_copy;
	}

	private void clearMetaInfo() {
		has_token_bucket_meta_info = false;
		is_token_bucket = false;
		token_buckets = new LinkedList<Curve>();

		has_rate_latency_meta_info = false;
		is_rate_latency = false;
		rate_latencies = new LinkedList<Curve>();
	}
	
	/**
	 * Decomposes this curve into a list of token bucket curves and
	 * stores this list in the curve's <code>token_buckets</code> field.<br/>
	 * Note: Curve must be concave.
	 */
	private void decomposeIntoTokenBuckets() {
		if ( has_token_bucket_meta_info == true ) {
			return;
		}

		// TODO I lost that ability, unfortunately
//		if( Configuration.performArrivalCurveChecks() && !this.isConcave() ) {
//			throw new RuntimeException("Can only decompose concave arrival curves into token buckets.");
//		}

		token_buckets = new LinkedList<Curve>();
		for (int i = 0; i < segments.length; i++) {
			if (isDiscontinuity(i)) {
				continue;
			}
			Num r = segments[i].grad;
			Num b = Num.sub( segments[i].y, Num.mult( segments[i].x, segments[i].grad ) );
			token_buckets.add( createTokenBucket( r, b ) );
		}
		
		if ( token_buckets.size() == 1 ) {
			is_token_bucket = true;
		} else {
			is_token_bucket = false;
		}
		
		has_token_bucket_meta_info = true;
	}

	public boolean isTokenBucket() {
		decomposeIntoTokenBuckets();
		return is_token_bucket;		
	}

	/**
	 * Returns the number of token buckets the curve can be decomposed into.
	 * 
	 * @return the number of token buckets
	 */
	public int getTBComponentCount() {
		decomposeIntoTokenBuckets();
		return token_buckets.size();
	}
	
	/**
	 * Returns a list of token bucket curves that this curve can be
	 * decomposed into.
	 * 
	 * @return the list of token buckets
	 */
	public LinkedList<Curve> getTBComponents() {
//	public final LinkedList<Curve> getTBComponents() {
		decomposeIntoTokenBuckets();
		return token_buckets;
	}
	
	/**
	 * Returns the <code>i</code>the token bucket curve that this curve can be
	 * decomposed into.
	 * 
	 * @param i the number of the token bucket
	 * @return the token bucket
	 */
	public Curve getTBComponent(int i) {
		decomposeIntoTokenBuckets();
		return token_buckets.get(i);
	}
	
	/**
	 * Decomposes this curve into a list of rate latency curves and
	 * stores this list in the curve's <code>rate_latencies</code> field.<br/>
	 * Note: Curve must be convex.
	 */
	private void decomposeIntoRateLatencies() {
		if ( has_rate_latency_meta_info == true ) {
			return;
		}

		// TODO I lost that ability, unfortunately
//		if( Configuration.performServiceCurveChecks() && !this.isConvex() ) {
//			if ( this.equals( ServiceCurve.createZeroDelayBurst() ) ) {
//				rate_latencies = new LinkedList<Curve>();
//				rate_latencies.add( createRateLatency( Num.POSITIVE_INFINITY, Num.ZERO ) );
//			} else {
//				throw new RuntimeException("Can only decompose convex service curves into rate latency curves.");
//			}
//		} else {
			rate_latencies = new LinkedList<Curve>();
			for (int i = 0; i < segments.length; i++) {
				if (segments[i].y.equals( 0.0 ) && segments[i].grad.equals( 0.0 ) ) {
					continue;
				}
				Num r = segments[i].grad;
				Num l = Num.sub( segments[i].x, Num.div( segments[i].y, segments[i].grad ) );
				rate_latencies.add( createRateLatency( r, l ) );
			}
//		}
		
		if( rate_latencies.size() == 1 ) {
			is_rate_latency = true;
		} else {
			is_rate_latency = false;
		}
		
		has_rate_latency_meta_info = true;
	}

	public boolean isRateLatency() {
		decomposeIntoRateLatencies();
		return is_rate_latency;
	}

	/**
	 * Returns the number of rate latency curves the curve can be decomposed into.
	 * 
	 * @return the number of rate latency curves
	 */
	public int getRLComponentCount() {
		decomposeIntoRateLatencies();
		return rate_latencies.size();
	}

	/**
	 * Returns a list of rate latency curves that this curve can be
	 * decomposed into.
	 * 
	 * @return the list of rate latency curves
	 */
	public LinkedList<Curve> getRLComponents() {
//	public final LinkedList<Curve> getRLComponents() {
		decomposeIntoRateLatencies();
		return rate_latencies;
	}

	/**
	 * Returns the <code>i</code>the rate latency curve that this curve can be
	 * decomposed into.
	 * 
	 * @param i the number of the rate latency curve
	 * @return the rate latency curve
	 */
	public Curve getRLComponent(int i) {
		decomposeIntoRateLatencies();
		return rate_latencies.get( i );
	}

	/**
	 * Returns the number of segments in this curve.
	 * 
	 * @return the number of segments
	 */
	public int getSegmentCount() {
		return segments.length;
	}

	/**
	 * Returns the x-coordinate of inflection point <code>i</code>.
	 * 
	 * @param i the index of the IP.
	 * @return the x-coordinate of the inflection point.
	 */
	public Num getIPX(int i) {
		return segments[i].x;
	}

	/**
	 * Returns the y-coordinate of inflection point <code>i</code>.
	 * 
	 * @param i the index of the IP.
	 * @return the y-coordinate of the inflection point.
	 */
	public Num getIPY(int i) {
		return segments[i].y;
	}

	/**
	 * Returns the sustained rate (the gradient of the last segment).
	 * 
	 * @return the sustained rate.
	 */
	public Num getSustainedRate() {
		return segments[segments.length-1].grad;
	}

	/**
	 * Returns the highest rate (the steepest gradient of any segment).
	 * 
	 * @return the highest rate.
	 */
	public Num getHighestRate() {
		Num max_rate = Num.NEGATIVE_INFINITY;
		for (int i = 0; i < segments.length; i++) {
			if ( !isDiscontinuity(i) && ( segments[i].grad ).greater( max_rate ) ) {
				max_rate = segments[i].grad;
			}
		}
		return max_rate;
	}

	/**
	 * Sets the sustained rate (the gradient of the last segment).
	 * Note: No checks are performed when setting the sustained rate.
	 * The caller has to ensure that <code>r</code> is larger (smaller) than
	 * the gradient of the 2nd-to-last segment to keep the curve convex (concave).
	 * 
	 * @param r the new sustained rate
	 */
	public void setSustainedRate( Num r ) {
		segments[segments.length-1].grad = r;
		clearMetaInfo();
	}

	/**
	 * Returns whether the inflection point <code>i</code> is excluded
	 * from the segment or not.
	 * 
	 * @param i the index of the IP.
	 * @return <code>true</code> if the IP is excluded, <code>false</code> if not.
	 */
	public boolean isLeftopen(int i) {
		return segments[i].leftopen;
	}

	/**
	 * Returns whether the inflection point is a (real or unreal) discontinuity.
	 * 
	 * @param i the index of the IP
	 * @return <code>true</code> if the IP is a discontinuity, <code>false</code> if not.
	 */
	public boolean isDiscontinuity(int i) {
		return (i+1 < segments.length && ( Num.abs( Num.sub( segments[i+1].x, segments[i].x ) ) ).less( EPSILON ) );
	}

	/**
	 * Returns whether the inflection point is a real discontinuity, i.e. the y0
	 * of the leftopen segment differs from the previous one.
	 * 
	 * @param i the index of the IP
	 * @return <code>true</code> if the IP is a real discontinuity, <code>false</code> if not.
	 */
	public boolean isRealDiscontinuity(int i) {
		return (isDiscontinuity(i) && ( Num.abs( Num.sub( segments[i+1].y, segments[i].y ) ) ).ge( EPSILON ) );
	}

	/**
	 * Returns whether the inflection point is an unreal discontinuity, i.e. the y0
	 * of the leftopen segment is coincident with the y0 of the previous segment
	 * and therefore the unreal discontinuity may safely be removed.
	 * 
	 * @param i the index of the IP
	 * @return <code>true</code> if the IP is an unreal discontinuity, <code>false</code> if not.
	 */
	public boolean isUnrealDiscontinuity(int i) {
		return (isDiscontinuity(i) && ( Num.abs( Num.sub(segments[i+1].y, segments[i].y ) ) ).less( EPSILON ) );
	}

	/**
	 * Returns whether the current curve is a burst delay curve.
	 * 
	 * @return <code>true</code> if the curve is burst delay, <code>false</code> otherwise
	 */
	public boolean isBurstDelay() {
		return (segments.length == 2
				&& segments[0].x.equals( Num.ZERO ) && segments[0].y.equals( Num.ZERO )
				&& segments[0].grad.equals( Num.ZERO ) && !segments[0].leftopen
				&& segments[1].x.ge( Num.ZERO ) && segments[1].y.equals( Num.POSITIVE_INFINITY )
				&& segments[1].grad.equals( Num.ZERO ) && segments[1].leftopen);
	}

	/**
	 * Adds a <code>LinearSegment</code> to the end of the curve.<br/>
	 * Note: It is the user's responsibility to add segments in the
	 * order of increasing x-coordinates.
	 * 
	 * @param s the segment to be added.
	 */
	public void addSegment(LinearSegment s) {
		addSegment(segments.length, s);
	}

	/**
	 * Adds a <code>LinearSegment</code> at the location <code>pos</code>
	 * of the curve.<br/>
	 * Note: It is the user's responsibility to add segments in the
	 * order of increasing x-coordinates.
	 * 
	 * @param pos the index into the segment array to add the new segment.
	 * @param s the segment to be added.
	 */
	public void addSegment(int pos, LinearSegment s) {
		if (pos < 0 || pos > segments.length) {
			throw new IllegalArgumentException("Index out of bounds (pos=" + pos + ")!");
		}
		if (s == null) {
			throw new IllegalArgumentException("Tried to insert null!");
		}
		LinearSegment[] old_segments = segments;
		segments = new LinearSegment[old_segments.length + 1];
		segments[pos] = s;
		if (pos > 0) {
			System.arraycopy(old_segments, 0, segments, 0, pos);
		}
		if (pos < old_segments.length) {
			System.arraycopy(old_segments, pos, segments, pos+1, old_segments.length-pos);
		}
		clearMetaInfo();
	}

	/**
	 * Removes the segment at position <code>pos</code>.
	 * 
	 * @param pos the index of the segment to be removed.
	 */
	public void removeSegment(int pos) {
		if (pos < 0 || pos >= segments.length) {
			throw new IllegalArgumentException("Index out of bounds (pos=" + pos + ")!");
		}
		LinearSegment[] old_segments = segments;
		segments = new LinearSegment[old_segments.length - 1];
		System.arraycopy(old_segments, 0, segments, 0, pos);
		System.arraycopy(old_segments, pos+1, segments, pos, old_segments.length-pos-1);
		
		clearMetaInfo();
	}

	/**
	 * Marks discontinuities (subsequent segments having the same
	 * x-coordinate) by setting <code>leftopen</code> of the second segment.
	 */
	public void markDiscontinuities() {
		for (int i = 0; i < segments.length; i++) {
			segments[i].leftopen = false;
		}
		for (int i = 1; i < segments.length; i++) {
			if (segments[i-1].x == segments[i].x) {
				segments[i].leftopen = true;
			}
		}
	}

	/**
	 * Computes the y-coordinates of inflection points starting with
	 * inflection point <code>start</code>.
	 * 
	 * @param start the IP at which to start computing y-coordinates.
	 */
	public void computeYs(int start) {
		if (start < 1) {
			throw new IllegalArgumentException("Value of 'start' must be >= 1!");
		}
		for (int i = start; i < segments.length; i++) {
			Num dx = Num.sub( segments[i].x, segments[i-1].x );
			segments[i].y = Num.add( segments[i-1].y, Num.mult( dx, segments[i-1].grad ) );
		}
	}

	/**
	 * Returns the burstiness of this token bucket curve.<br/>
	 * Note: For performance reasons there are no sanity checks! Only
	 *       call this method on a valid token bucket curve!
	 *       
	 * @return the burstiness
	 */
	public Num getTBBurst() {
		return segments[1].y;
	}

	/**
	 * Sets the burstiness of this token bucket curve.<br/>
	 * Note: For performance reasons there are no sanity checks! Only
	 *       call this method on a valid token bucket curve!
	 *       
	 * @param b the burstiness
	 */
	public void setTBBurst( Num b ) {
		segments[1].y = b;
		clearMetaInfo();
	}

	/**
	 * Makes the token bucket curve right-continuous.<br/>
	 * Note: For performance reasons there are no sanity checks! Only
	 *       call this method on a valid token bucket curve!
	 */
	public void makeTBRightContinuous() {
		segments[0].y = segments[1].y;
		clearMetaInfo();
	}

	/**
	 * Undoes the makeTBRightContinuous() operation.<br/>
	 * Note: For performance reasons there are no sanity checks! Only
	 *       call this method on a valid token bucket curve!
	 */
	public void undoMakeTBRightContinuous() {
		segments[0].y = Num.ZERO;
		clearMetaInfo();
	}

	/**
	 * Removes unnecessary segments.
	 */
	public void beautify() {
		int i = 0;
		while( i < segments.length-1 ) {
			// Remove unreal discontinuity
			if ( isUnrealDiscontinuity( i ) ) {
				segments[i+1].leftopen = segments[i].leftopen;
				removeSegment(i);
				continue;
			}
			i++;
		}

		i = 0;
		while(i < segments.length-1) {
			// Join colinear segments
			Num firstArg = Num.sub( segments[i+1].grad, segments[i].grad );
			
			Num secondArg = Num.sub( segments[i+1].x, segments[i].x );
			secondArg = Num.mult( secondArg, segments[i].grad );
			secondArg = Num.add( segments[i].y, secondArg);
			secondArg = Num.sub( segments[i+1].y, secondArg );
			
			if ( Num.abs( firstArg ).less( EPSILON )
					&& Num.abs( secondArg ).less( EPSILON ) ){
				
				removeSegment(i+1);
				if (i+1 < segments.length && !segments[i+1].leftopen) {
					Num resultPt1 = Num.sub( segments[i+1].y, segments[i].y );
					Num resultPt2 = Num.sub( segments[i+1].x, segments[i].x );
					
					segments[i].grad = Num.div( resultPt1, resultPt2 );
				}
				continue;
			}
			i++;
		}

		for (i = 0; i < segments.length-1; i++) {
			if (segments[i].x.equals( segments[i+1].x ) ) {
				segments[i].grad = Num.ZERO;
			}
		}
		
		// Remove rate of tb arrival curves' first segment
		if ( segments.length > 1 
				&& segments[0].x == Num.ZERO 
				&& segments[0].y != Num.ZERO
				&& segments[1].x == Num.ZERO 
				&& segments[1].y != Num.ZERO ) {
			segments[0].grad = Num.ZERO;
		}
		clearMetaInfo();
	}

	/**
	 * Returns the number of the segment that defines the function
	 * value at x-coordinate <code>x</code>. The
	 * number of the segment is usually the same as the one
	 * returned by <code>getSegmentLimitRight(x)</code>, except for
	 * if a segment starts at <code>x</code> and is left-open.
	 * In this case the function returns the previous segment,
	 * rather than the current segment, as the previous segment
	 * defines <code>x</code>.
	 * 
	 * @param x the x-coordinate
	 * @return the index of the segment into the array.
	 */
	public int getSegmentDefining( Num x ) {
		for (int i = segments.length - 1; i >= 0; i--) {
			if (segments[i].leftopen) {
				if ( segments[i].x.less( x ) ) {
					return i;
				}
			} else {
				if ( segments[i].x.le( x ) ) {
					return i;
				}
			}
		}
		return -1;
	}
	
	public LinearSegment[] getSegments() {
		return segments;
	}
	
	public LinearSegment getSegment(int i) {
		if(i>=0) {
			return segments[i];
		} else {
			return segments[segments.length + i];
		}
	}

	/**
	 * Returns the function value at x-coordinate <code>x</code>, if
	 * <code>x>=0</code>, and <code>NaN</code> if not.
	 * 
	 * @param x the x-coordinate
	 * @return the function value
	 */
	public Num f( Num x ) {
		int i = getSegmentDefining(x);
		if (i < 0) {
			return Num.NaN;
		}
		return Num.add( Num.mult( Num.sub( x, segments[i].x ), segments[i].grad ), segments[i].y );
	}

	/**
	 * Returns the curve's gradient at x-coordinate <code>x</code>, if
	 * <code>x>=0</code>, and <code>NaN</code> if not. Note that the
	 * gradient returned at discontinuities is <code>0.0</code>, the
	 * gradient returned at an inflection point is the gradient of the
	 * linear segment right of the inflection point, but only if the
	 * segment is not left-open.
	 * 
	 * @param x the x-coordinate
	 * @return the gradient
	 */
	public Num getGradientAt( Num x ) {
		int i = getSegmentDefining(x);
		if (i < 0) {
			return Num.NaN;
		}
		return segments[i].grad;
	}

	/**
	 * Returns the number of the segment that defines the value
	 * of the function when computing the limit to the right
	 * of the function at x-coordinate <code>x</code>. The
	 * number of the segment is usually the same as the one
	 * returned by <code>getSegmentDefining(x)</code>, except for
	 * if a segment starts at <code>x</code> and is left-open.
	 * In this case the function returns the current segment,
	 * rather than the previous segment.
	 * 
	 * @param x the x-coordinate
	 * @return the index of the segment into the array.
	 */
	public int getSegmentLimitRight( Num x ) {
		for (int i = segments.length - 1; i >= 0; i--) {
			if ( segments[i].x.le( x ) ) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Returns the limit to the right of the function value at
	 * x-coordinate <code>x</code>, if <code>x>=0</code>, and
	 * <code>NaN</code> if not.
	 * 
	 * @param x the x-coordinate
	 * @return the function value
	 */
	public Num fLimitRight( Num x ) {
		int i = getSegmentLimitRight(x);
		if (i < 0) {
			return Num.NaN;
		}
		return Num.add( Num.mult( Num.sub( x, segments[i].x ), segments[i].grad ), segments[i].y ).copy();
	}

	/**
	 * Returns the gradient to the right of the function value at
	 * x-coordinate <code>x</code>, if <code>x>=0</code>, and
	 * <code>NaN</code> if not.
	 * 
	 * @param x the x-coordinate
	 * @return the function value
	 */
	public Num getGradientLimitRight( Num x ) {
		int i = getSegmentLimitRight(x);
		if (i < 0) {
			return Num.NaN;
		}
		return segments[i].grad;
	}

	/**
	 * Returns the first segment at which the function reaches the
	 * value <code>y</code>. It returns -1 if the curve never
	 * reaches this value.
	 * 
	 * @param y the y-coordinate
	 * @return the segment number
	 */
	public int getSegmentFirstAtValue( Num y ) {
		if ( segments.length == 0 || segments[0].y.greater( y ) ) {
			return -1;
		}
		for (int i = 0; i < segments.length; i++) {
			if (i < segments.length-1) {
				if ( segments[i+1].y.ge( y ) ) {
					return i;
				}
			} else {
				if ( segments[i].grad.greater( Num.ZERO ) ) {
					return i;
				}
			}
		}
		return -1;
	}

	/**
	 * Returns the smallest x value at which the function value is
	 * equal to <code>y</code>.
	 * 
	 * @param y the y-coordinate
	 * @return the smallest x value
	 */
	public Num f_inv( Num y ) {
		return f_inv(y, false);
	}

	/**
	 * Returns the x value at which the function value is
	 * equal to <code>y</code>. If <code>rightmost</code> is
	 * <code>true</code>, returns the rightmost x-coordinate,
	 * otherwise the leftmost coordinate.
	 * 
	 * @param y the y-coordinate
	 * @return the smallest x value
	 */
	public Num f_inv( Num y, boolean rightmost) {
		int i = getSegmentFirstAtValue(y);
		if (i < 0) {
			return Num.NaN;
		}
		if (rightmost) {
			while(i < segments.length && segments[i].grad.equals( Num.ZERO ) ) {
				i++;
			}
			if (i >= segments.length) {
				return Num.POSITIVE_INFINITY;
			}
		}
		if ( !segments[i].grad.equals( Num.ZERO ) ) {
			return Num.add( segments[i].x, Num.div( Num.sub( y, segments[i].y ), segments[i].grad ) );
		} else {
			return segments[i].x;
		}
	}

	/**
	 * Returns the smallest x for which the curve is defined.
	 * 
	 * @return the lower bound of the definition interval.
	 */
	public double getMinX() {
		return 0.0;
	}

	/**
	 * Returns the largest x for which the curve is defined.
	 * 
	 * @return the upper bound of the definition interval.
	 */
	public Num getMaxX() {
		return Num.POSITIVE_INFINITY;
	}

	public Num getMaxY( Num a, Num b ) {
		int sa = getSegmentDefining(a);
		int sb = getSegmentDefining(b);

		Num ret = Num.ZERO;
		for(int i=sa; i<=sb; ++i) {
			Num end = ( i+1 < segments.length ? Num.min(getSegment(i+1).x, b) : b );
			ret = Num.max( ret, Num.max( f( Num.max( a, getSegment(i).x ) ), f( end ) ) );
		}

		return ret;
	}

	/**
	 * Tests whether the curve is wide-sense increasing.
	 * 
	 * @return whether the curve is wide-sense increasing.
	 */
	public boolean isWideSenseIncreasing() {
		Num y = Num.NEGATIVE_INFINITY;
		for (int i = 0; i < segments.length; i++) {
			if ( segments[i].y.less( y ) || segments[i].grad.less( Num.ZERO ) ) {
				return false;
			}
			y = segments[i].y;
		}
		return true;
	}

	/**
	 * Tests whether the curve is convex.
	 * 
	 * @return whether the curve is convex.
	 */
	public boolean isConvex() {
		return isConvexIn( Num.ZERO, Num.POSITIVE_INFINITY );
	}

	/**
	 * Tests whether the curve is convex in [a,b].
	 * 
	 * @param a the lower bound of the test interval.
	 * @param b the upper bound of the test interval.
	 * @return whether the curve is convex
	 */
	public boolean isConvexIn( Num a, Num b ) {
		Num last_gradient = Num.NEGATIVE_INFINITY;

		int i_start = getSegmentDefining(a);
		int i_end   = getSegmentDefining(b);
		for (int i = i_start; i <= i_end; i++) {
			if (i_start < 0) {
				break;
			}
			if (i == i_end && segments[i].x == b) {
				break;
			}
			Num gradient;
			if (i < segments.length-1) {
				gradient = Num.div( Num.sub( segments[i+1].y, segments[i].y ),
											Num.sub( segments[i+1].x, segments[i].x ) );
			} else {
				gradient = segments[i].grad;
			}
			if ( gradient.less( last_gradient ) ) {
				return false;
			}
			last_gradient = gradient;
		}
		return true;
	}

	/**
	 * Tests whether the curve is concave.
	 * 
	 * @return whether the curve is concave.
	 */
	public boolean isConcave() {
		return isConcaveIn( Num.ZERO, Num.POSITIVE_INFINITY );
	}

	/**
	 * Tests whether the curve is concave in [a,b].
	 * 
	 * @param a the lower bound of the test interval.
	 * @param b the upper bound of the test interval.
	 * @return whether the curve is concave.
	 */
	public boolean isConcaveIn( Num a, Num b ) {
		Num last_gradient = Num.POSITIVE_INFINITY;

		int i_start = getSegmentDefining( a );
		int i_end   = getSegmentDefining( b );
		for ( int i = i_start; i <= i_end; i++ ) {
			if ( i == i_end && segments[i].x == b ) {
				break;
			}
			Num gradient;
			// Handles discontinuities
			if ( i < segments.length-1 ) {
				gradient = Num.div( Num.sub( segments[i+1].y, segments[i].y ),
											Num.sub( segments[i+1].x, segments[i].x ) );
			} else {
				gradient = segments[i].grad;
			}
			if ( gradient.greater( last_gradient ) ) {
				return false;
			}
			last_gradient = gradient;
		}
		return true;
	}

	/**
	 * Tests whether the curve is almost concave, i.e. it is concave once its
	 * function value is larger than 0.
	 * 
	 * @return whether the curve is almost concave.
	 */
	public boolean isAlmostConcave() {
		Num last_gradient = Num.POSITIVE_INFINITY;

		for (int i = 0; i < segments.length; i++) {
			// Skip the horizontal part at the beginning
			if ( last_gradient.equals( Num.POSITIVE_INFINITY ) && segments[i].grad.equals( Num.ZERO ) ) {
				continue;
			}

			Num gradient;
			if (i < segments.length-1) {
				gradient = Num.div( Num.sub( segments[i+1].y, segments[i].y ),
											Num.sub( segments[i+1].x, segments[i].x ) );
			} else {
				gradient = segments[i].grad;
			}
			if ( gradient.greater( last_gradient ) ) {
				return false;
			}
			last_gradient = gradient;
		}
		return true;
	}
	
	/**
	 * Returns a copy of this curve that is shifted to the right by <code>dx</code>,
	 * i.e. g(x) = f(x-dx).
	 * 
	 * @param dx the offset to shift the curve.
	 * @return the shifted curve.
	 */
	public static Curve shiftRight( Curve curve, Num dx ) {
		Curve curve_copy = curve.copy();
		if ( !( ( curve_copy.getSegment(0).y ).equals( Num.ZERO ) ) ) {
			throw new RuntimeException("Curve to shift right must pass through origin!");
		}
		
		Curve result = new Curve( curve_copy.getSegmentCount()+1 );
		result.segments[0] = LinearSegment.createHorizontalLine( 0.0 );
		result.segments[0].y = curve_copy.getSegment(0).y; // Decide what to do if pass thru origin req. dropped...
		for (int i = 0; i < curve_copy.getSegmentCount(); i++) {
			result.segments[i+1] = curve_copy.getSegment(i);
			result.segments[i+1].x = Num.add( result.segments[i+1].x, dx );
		}
		
		result.beautify();
		
		return result;
	}
	
	/**
	 * Returns a copy of this curve that is shifted to the left by <code>dx</code>,
	 * i.e. g(x) = f(x+dx). Note that the new curve is clipped at the y-axis so
	 * that in most cases <code>c.shiftLeftClipping(dx).shiftRight(dx) != c</code>!
	 * 
	 * @param dx the offset to shift the curve.
	 * @return the shifted curve.
	 */
	public static Curve shiftLeftClipping( Curve curve, Num dx ) {
		Curve result = curve.copy();
		int i = result.getSegmentDefining(dx);
		if ( result.segments[i].x.less( dx ) ) {
			result.segments[i].y = Num.add( result.segments[i].y, 
									Num.mult( Num.sub( dx, result.segments[i].x ), result.segments[i].grad ) );
			result.segments[i].x = dx;
			result.segments[i].leftopen = false;
		}
		if (i > 0) {
			LinearSegment[] old_segments = result.segments;
			result.segments = new LinearSegment[old_segments.length - i];
			System.arraycopy(old_segments, i, result.segments, 0, result.segments.length);
		}
		for (i = 0; i < result.segments.length; i++) {
			result.segments[i].x = Num.sub( result.segments[i].x, dx );
		}

		return result;
	}
	
	/**
	 * Returns a copy of this curve with latency removed, i.e. shifted left by
	 * the latency.
	 * 
	 * @return a copy of this curve without latency
	 */
	public static Curve removeLatency( Curve curve ) {
		Curve result = curve.copy();

		// Remove all segment(s) with y0==0.0 and grad==0.0
		while(result.segments.length > 0) {
			if ( result.segments[0].y.greater( Num.ZERO ) || result.segments[0].grad.greater( Num.ZERO ) ) {
				break;
			}
			if ( curve.getSegment(0).y.less( Num.ZERO ) || curve.getSegment(0).grad.less( Num.ZERO ) ) {
				throw new RuntimeException("Should have avoided neg. gradients elsewhere...");
			}
			result.removeSegment(0);
		}

		// In case that we've removed everything, the curve had infinite latency, so return the NULL curve.
		if (result.segments.length == 0) {
			return createNullCurve();
		}

		// Shift remaining segments left by latency
		Num L = result.segments[0].x;
		for (int i = 0; i < result.segments.length; i++) {
			result.segments[i].x = Num.sub( result.segments[i].x, L );
		}
		if (result.segments[0].leftopen) {
			result.addSegment(0, LinearSegment.createHorizontalLine( 0.0 ));
		}
		
		return result;
	}

	/**
	 * Returns a copy of this curve shifted vertically by <code>dy</code>.
	 * 
	 * @param dy the offset to shift the curve.
	 * @return the shifted curve.
	 */
	public static Curve add( Curve curve, Num dy ) {
		Curve result = curve.copy();
		for (int i = 0; i < curve.getSegmentCount(); i++) {
			result.segments[i].y = Num.add( result.segments[i].y, dy );
		}
		
		return result;
	}

	/**
	 * Returns the x-coordinate of the inflection point after which the function
	 * values are greater than zero.
	 * 
	 * @return the latency of this curve.
	 */
	public Num getLatency() {
		if ( segments[0].y.greater( Num.ZERO ) ) {
			return Num.ZERO;
		}
		for (int i = 0; i < segments.length; i++) {
			Num y0 = segments[i].y;
			if (y0.less( Num.ZERO ) && y0.greater( Num.negate( EPSILON ) ) ) {
				y0 = Num.ZERO;
			}
			if ( y0.greater( Num.ZERO )
					|| ( y0.ge( Num.ZERO ) && segments[i].grad.greater( Num.ZERO ) )
				) {
				return segments[i].x;
			}
			if (y0.less( Num.ZERO ) || segments[i].grad.less( Num.ZERO ) ) {
				throw new RuntimeException("Should have avoided neg. gradients elsewhere...");
			}
		}
		
		return Num.POSITIVE_INFINITY;
	}

	/**
	 * Returns a list containing each segment's height when projected onto
	 * the y-axis.
	 * 
	 * @return a list of doubles containing the segment heights
	 */
	public LinkedList<Num> getYIntervals() {
		LinkedList<Num> y_intervals = new LinkedList<Num>();
		for (int i = 1; i < segments.length; i++) {
			if (isDiscontinuity(i-1)) {
				continue;
			}
			y_intervals.add( Num.sub( segments[i].y, segments[i-1].y ) );
		}
		
		return y_intervals;
	}

	public static Num getXIntersection( Curve curve1, Curve curve2 ){
		Num x_int = Num.POSITIVE_INFINITY;

		for( int i = 0; i < curve1.getSegmentCount(); i++ ){
			boolean curve1_last = (i == curve1.getSegmentCount()-1);
					
			for( int j = 0; j < curve2.getSegmentCount(); j++ ){
				boolean curve2_last = (j == curve2.getSegmentCount()-1);
				
				Num x_int_tmp = curve1.segments[i].getXIntersectionWith( curve2.segments[j] );
				
				if( x_int_tmp.equals( Num.NaN ) ) {
					break;
				}
				
				if( x_int_tmp.greater( Num.ZERO ) ){
					if( !curve1_last ){
						if( !curve2_last ){
							if( x_int_tmp.less( curve1.segments[i+1].x )
								&& x_int_tmp.less( curve1.segments[j+1].x )
								&& x_int_tmp.less( x_int ) ) {				
										
									x_int = x_int_tmp;
							}
						} else {
							if( x_int_tmp.less( curve1.segments[i+1].x )
								&& x_int_tmp.less( x_int ) ) {				
										
									x_int = x_int_tmp;
							}
						}
					} else {
						if( !curve2_last ){
							if( x_int_tmp.less( curve1.segments[j+1].x )
								&& x_int_tmp.less( x_int ) ) {				
										
									x_int = x_int_tmp;
							}
						} else {
							if( x_int_tmp.less( x_int )) {				
										
									x_int = x_int_tmp;
							}
						}
					}
				}
			}
		}
		return x_int;
	}

	/**
	 * Returns a string representation of this curve.
	 * 
	 * @return the curve represented as a string.
	 */
	@Override
	public String toString() {
		String result = "{";
		for (int i = 0; i < segments.length; i++) {
			if (i > 0) {
				result += ";";
			}
			result += segments[i].toString();
		}
		result += "}";
		return result;
	}
	
	/**
	 * Common helper for computing a new curve.
	 * 
	 * @param c1
	 * @param c2
	 * @param operator
	 * @return
	 */
	private static Curve computeResultingCurve( Curve c1, Curve c2, Operator operator ) {
		switch( operator ) {
		case OPERATOR_ADD:
			if( c1.equals( ZERO_DELAY_BURST_CURVE ) || c2.equals( ZERO_DELAY_BURST_CURVE ) ){
				return ZERO_DELAY_BURST_CURVE.copy();
			}
			break;
		case OPERATOR_SUB:
			if( c1.equals( ZERO_DELAY_BURST_CURVE ) || c2.equals( ZERO_DELAY_BURST_CURVE ) ){
				return ZERO_DELAY_BURST_CURVE.copy();
			}
			break;
		case OPERATOR_MIN:
			if( c1.equals( ZERO_DELAY_BURST_CURVE ) ){
				return c2.copy();
			}
			if( c2.equals( ZERO_DELAY_BURST_CURVE ) ){
				return c1.copy();
			}
			break;
		case OPERATOR_MAX:
			if( c1.equals( ZERO_DELAY_BURST_CURVE ) || c2.equals( ZERO_DELAY_BURST_CURVE ) ){
				return ZERO_DELAY_BURST_CURVE.copy();
			}
			break;
		default:
		}
		
		Curve result = new Curve();

		Num x = Num.ZERO;

		Num x_cross;

		boolean leftopen;

		int i1 = 0;
		int i2 = 0;
		while( i1 < c1.getSegmentCount() || i2 < c2.getSegmentCount() ) {
			Num x_next1 = ( i1+1 < c1.getSegmentCount() ) ?
								c1.getSegment( i1+1 ).x : Num.POSITIVE_INFINITY;
			Num x_next2 = ( i2+1 < c2.getSegmentCount() ) ?
								c2.getSegment( i2+1 ).x : Num.POSITIVE_INFINITY;
			Num x_next  = Num.min( x_next1, x_next2 );
								
			leftopen = c1.getSegment( i1 ).leftopen || c2.getSegment( i2 ).leftopen;

			switch( operator ) {
			case OPERATOR_ADD:
				result.addSegment( LinearSegment.add( c1.getSegment( i1 ), c2.getSegment( i2 ), x, leftopen ) );
				break;
			case OPERATOR_SUB:
				result.addSegment( LinearSegment.sub( c1.getSegment( i1 ), c2.getSegment( i2 ), x, leftopen ) );
				break;
			case OPERATOR_MIN:
				x_cross = c1.getSegment( i1 ).getXIntersectionWith( c2.getSegment( i2 ) );
				if ( x_cross.equals( Num.NaN ) ) {
					x_cross = Num.POSITIVE_INFINITY;
				}
				if ( x.less( x_cross ) && x_cross.less( x_next ) ) {
					result.addSegment( LinearSegment.min( c1.getSegment( i1 ), c2.getSegment( i2 ), x, leftopen, false ) );
					result.addSegment( LinearSegment.min( c1.getSegment( i1 ), c2.getSegment( i2 ), x_cross, false, true ) );
				} else {
					result.addSegment( LinearSegment.min( c1.getSegment( i1 ), c2.getSegment( i2 ), x, leftopen, false ) );
				}
				break;
			case OPERATOR_MAX:
				x_cross = c1.getSegment( i1 ).getXIntersectionWith( c2.getSegment( i2 ) );
				if ( x_cross.equals( Num.NaN ) ) {
					x_cross = Num.POSITIVE_INFINITY;
				}
				if ( x.less( x_cross ) && x_cross.less( x_next ) ) {
					result.addSegment( LinearSegment.max( c1.getSegment( i1 ), c2.getSegment( i2 ), x, leftopen, false ) );
					result.addSegment( LinearSegment.max( c1.getSegment( i1 ), c2.getSegment( i2 ), x_cross, false, true ) );
				} else {
					result.addSegment( LinearSegment.max( c1.getSegment( i1 ), c2.getSegment( i2 ), x, leftopen, false ) );
				}
				break;
			default:
			}

			if ( x_next1.equals( x_next ) ) {
				i1++;
			}
			if ( x_next2.equals( x_next ) ) {
				i2++;
			}
			x = x_next;
		}

		result.beautify();

		return result;
	}
	
	/**
	 * Returns a curve that is the sum of this curve and the given curve.
	 * 
	 * @param curve2 the given curve.
	 * @return the sum of curves
	 */
	public static Curve add( Curve curve1, Curve curve2 ) {
		return computeResultingCurve( curve1, curve2, Operator.OPERATOR_ADD );
	}

	/**
	 * Returns a curve that is the difference between this curve and the given curve.
	 * 
	 * @param curve2 the given curve.
	 * @return the difference between curves
	 */
	public static Curve sub( Curve curve1, Curve curve2 ) {
		return computeResultingCurve( curve1, curve2, Operator.OPERATOR_SUB );
	}

	/**
	 * Returns a curve that is the minimum of this curve and the given curve.
	 * 
	 * @param curve2 the given curve.
	 * @return the minimum of curves
	 */
	public static Curve min( Curve curve1, Curve curve2 ) {
		return computeResultingCurve( curve1, curve2, Operator.OPERATOR_MIN );
	}

	/**
	 * Returns a curve that is the maximum of this curve and the given curve.
	 * 
	 * @param curve2 the given curve.
	 * @return the maximum of curves
	 */
	public static Curve max( Curve curve1, Curve curve2 ) {
		return computeResultingCurve( curve1, curve2, Operator.OPERATOR_MAX );
	}
	
	/**
	 * Returns a copy of curve bounded at the x-axis.
	 * 
	 * @return the bounded curve.
	 */
	public static Curve boundAtXAxis( Curve curve ) {
		Curve curve_copy = curve.copy();
		Curve result = new Curve();

		LinearSegment s;
		for ( int i = 0; i < curve_copy.getSegmentCount(); i++ ) {
			if ( curve_copy.getSegment( i ).y.greater( Num.ZERO ) ) {
				result.addSegment( curve_copy.getSegment( i ) );

				if ( curve_copy.getSegment(i).grad.less( Num.ZERO ) ) {
					Num x_cross = curve_copy.getSegment( i ).getXIntersectionWith( LinearSegment.X_AXIS );
					if ( i+1 >= curve_copy.getSegmentCount() || x_cross.less( curve_copy.getSegment( i+1 ).x ) ) {
						s = LinearSegment.createHorizontalLine( 0.0 );
						s.x = x_cross;
						result.addSegment( s );
					}
				}
			} else {
				s = LinearSegment.createHorizontalLine( 0.0 );
				s.x       = curve_copy.getSegment( i ).x;
				s.leftopen = curve_copy.getSegment( i ).leftopen;
				result.addSegment( s );

				if ( curve_copy.getSegment(i).grad.greater( Num.ZERO ) ) {
					Num x_cross = curve_copy.getSegment( i ).getXIntersectionWith( LinearSegment.X_AXIS );
					if ( i+1 >= curve_copy.getSegmentCount() || x_cross.less( curve_copy.getSegment(i+1).x ) ) {
						s = LinearSegment.createHorizontalLine( 0.0 );
						s.x   = x_cross;
						s.grad = curve_copy.getSegment( i ).grad;
						result.addSegment( s );
					}
				}
			}
		}
		
		result.beautify();
		
		return result;
	}	

	/**
	 * Returns the maximum vertical deviation between the given two curves.
	 * 
	 * @param c1 the first curve.
	 * @param c2 the second curve.
	 * @return the value of the vertical deviation.
	 */
	public static Num getMaxVerticalDeviation( Curve c1, Curve c2 ) {
		if ( c1.getSustainedRate().greater( c2.getSustainedRate() ) ) {
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
		// Start with the burst as minimum of all possible solutions for the deviation instead of negative infinity.
		
		Num burst_c1 = c1.fLimitRight( Num.ZERO );
		Num burst_c2 = c2.fLimitRight( Num.ZERO );
		Num result = Num.diff( burst_c1, burst_c2 );
		
		LinkedList<Num> xcoords = computeInflectionPointsX( c1, c2 );	
		for( int i = 0; i < xcoords.size(); i++ ) {
			Num ip_x = ( (Num) xcoords.get( i ) );

			Num backlog = Num.sub( c1.f( ip_x ), c2.f( ip_x ) );
			result = Num.max( result, backlog );
		}
		return result;
	}

	/**
	 * Returns the maximum horizontal deviation between the given two curves.
	 * 
	 * @param c1 the first curve.
	 * @param c2 the second curve.
	 * @return the value of the horizontal deviation.
	 */
	public static Num getMaxHorizontalDeviation( Curve c1, Curve c2 ) {
		if ( c1.getSustainedRate().greater( c2.getSustainedRate() ) ) {
			return Num.POSITIVE_INFINITY;
		}

		Num result = Num.NEGATIVE_INFINITY;
		for( int i = 0; i < c1.getSegmentCount(); i++ ) {
			Num ip_y = c1.getSegment( i ).y;

			Num delay = Num.sub( c2.f_inv( ip_y, true ), c1.f_inv( ip_y, false ) );
			result = Num.max( result, delay );
		}
		for( int i = 0; i < c2.getSegmentCount(); i++ ) {
			Num ip_y = c2.getSegment( i ).y;

			Num delay = Num.sub( c2.f_inv( ip_y, true ), c1.f_inv( ip_y, false ) );
			result = Num.max( result, delay );
		}
		return result;
	}

	/**
	 * Returns an <code>LinkedList</code> instance of those x-coordinates
	 * at which either c1 or c2 or both have an inflection point. There
	 * will be multiple occurences of an x-coordinate, if at least one
	 * curve has a discontinuity at that x-coordinate.
	 * 
	 * @param c1 the first curve.
	 * @param c2 the second curve.
	 * @return an <code>LinkedList</code> of <code>Double</code> objects
	 * containing the x-coordinates of the respective inflection point.
	 */
	public static LinkedList<Num> computeInflectionPointsX( Curve c1, Curve c2 ) {
		LinkedList<Num> xcoords = new LinkedList<Num>();

		int i1 = 0;
		int i2 = 0;
		while( i1 < c1.getSegmentCount() || i2 < c2.getSegmentCount() ) {
			Num x1 = ( i1 < c1.getSegmentCount() ) ?
						c1.getSegment( i1 ).x : Num.POSITIVE_INFINITY;
			Num x2 = ( i2 < c2.getSegmentCount() ) ?
						c2.getSegment( i2 ).x : Num.POSITIVE_INFINITY;
			if ( x1.less( x2 ) ) {
				xcoords.add( x1.copy() );
				i1++;
			} else if ( x1.greater( x2 ) ) {
				xcoords.add( x2.copy() );
				i2++;
			} else {
				xcoords.add( x1.copy() );
				i1++;
				i2++;
			}
		}
		return xcoords;
	}

	/**
	 * Returns an <code>LinkedList</code> instance of those y-coordinates
	 * at which either c1 or c2 or both have an inflection point.
	 * 
	 * @param c1 the first curve.
	 * @param c2 the second curve.
	 * @return an <code>LinkedList</code> of <code>Double</code> objects
	 * containing the x-coordinates of the respective inflection point.
	 */
	public static LinkedList<Num> computeInflectionPointsY( Curve c1, Curve c2 ) {
		LinkedList<Num> ycoords = new LinkedList<Num>();

		int i1 = 0;
		int i2 = 0;
		while( i1 < c1.getSegmentCount() || i2 < c2.getSegmentCount() ) {
			Num y1 = ( i1 < c1.getSegmentCount() ) ?
						c1.getSegment( i1 ).y : Num.POSITIVE_INFINITY;
			Num y2 = ( i2 < c2.getSegmentCount() ) ?
						c2.getSegment( i2 ).y : Num.POSITIVE_INFINITY;
			if ( y1.less( y2 ) ) {
				ycoords.add( y1.copy() );
				i1++;
			} else if ( y1.greater( y2 ) ) {
				ycoords.add( y2.copy() );
				i2++;
			} else {
				ycoords.add( y1.copy() );
				i1++;
				i2++;
			}
		}

		return ycoords;
	}
	
	public boolean equals( Curve other ) {
		Curve this_cpy = this.copy();
		Curve other_cpy = other.copy();
		
		this_cpy.beautify();
		other_cpy.beautify();
		
 		if( this_cpy.getLatency() == Num.POSITIVE_INFINITY ) {
 			this_cpy = createNullCurve();
 		}
 		if( other_cpy.getLatency() == Num.POSITIVE_INFINITY ) {
 			other_cpy = createNullCurve();
 		}
		
		int this_segment_length = this_cpy.segments.length;
		
		if( this_segment_length != other_cpy.segments.length ){
			return false;
		}
		
		for( int i = 0; i < this_segment_length; i++ ) {
			if( !this_cpy.segments[i].equals( other_cpy.segments[i] ) ){
				return false;
			}
		}
			
		return true;
	}
}