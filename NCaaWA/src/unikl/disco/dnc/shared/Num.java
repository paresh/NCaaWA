/*
 * This file is part of the Disco Deterministic Network Calculator v2.0.3 "Hydra".
 *
 * Copyright (C) 2014 Steffen Bondorf
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
 
package unikl.disco.dnc.shared;

import java.io.Serializable;

/**
 * Wrapper class around double;
 *
 * @author Steffen Bondorf
 *
 */
public class Num implements Serializable {
	public static enum RepresentationFlag { FRACTION, DOUBLE };
	public static RepresentationFlag representation = RepresentationFlag.DOUBLE;
//	public static final RepresentationFlag representation = RepresentationFlag.DOUBLE;
	
	private double value = 0.0;

	public static double EPSILON = Double.parseDouble("5E-10");
//	public static final double EPSILON = Double.parseDouble("5E-10");

	private boolean isNaN;
	private boolean isPosInfty;
	private boolean isNegInfty;

	public static Num POSITIVE_INFINITY = new Num( Double.POSITIVE_INFINITY );
	public static Num NEGATIVE_INFINITY = new Num( Double.NEGATIVE_INFINITY );
//	public static final Num POSITIVE_INFINITY = new Num( Double.POSITIVE_INFINITY );
//	public static final Num NEGATIVE_INFINITY = new Num( Double.NEGATIVE_INFINITY );

	public static Num NaN = new Num( Double.NaN );
//	public static final Num NaN = new Num( Double.NaN );

	public static Num ZERO = new Num( 0.0 );
//	public static final Num ZERO = new Num( 0.0 );
	
	@SuppressWarnings("unused")
	private Num(){}
	
	public Num( double value ) {
		this.value = value;
		checkInftyNaN();
	}
	
	public Num( String num_str ) throws Exception {
		this.value = parse( num_str ).doubleValue(); // Need to call the constructor first so the parsing went into a separate function
		checkInftyNaN();
	}
	
	public Num( int num ) {
		value = (double)num;
		checkInftyNaN();
	}
	
	public Num( int num, int den ) {
		value = ((double)num) / ((double)den);
		checkInftyNaN();
	}
	
	private void checkInftyNaN() {
		// See Java documentation on parsing Doubles. There are rounding errors up to Math.ulp(Double.MAX_VALUE)/2.
		if( value >= Double.MAX_VALUE ){
			isPosInfty = true;
		} else {
			if( value == Double.NEGATIVE_INFINITY ){
				isNegInfty = true;
			} else {
				if( value == Double.NaN ){
					isNaN = true;
				}
			}
		}
	}
	
	public Num( Num num ) {
		value = num.value;
	}
	
	public static Num getEpsilon() {
        return new Num( EPSILON );
	}
	
	public static Num add( Num num1, Num num2 ) {
		if( num1.isNaN || num2.isNaN ) {
			return NaN;
		}
		if( num1.isPosInfty || num2.isPosInfty ) {
			return POSITIVE_INFINITY;
		}
		if( num1.isNegInfty || num2.isNegInfty ) {
			return NEGATIVE_INFINITY;
		}
		
		return new Num( num1.doubleValue() + num2.doubleValue() );
	}
	
	public static Num sub( Num num1, Num num2 ) {
		if( num1.isNaN || num2.isNaN ) {
			return NaN;
		}
		if( num1.isPosInfty || num2.isPosInfty ) {
			return POSITIVE_INFINITY;
		}
		if( num1.isNegInfty || num2.isNegInfty ) {
			return NEGATIVE_INFINITY;
		}
		
		return new Num( num1.doubleValue() - num2.doubleValue() );
	}
	
	public static Num mult( Num num1, Num num2 ) {
		if( num1.isNaN || num2.isNaN ) {
			return NaN;
		}
		if( num1.isPosInfty || num2.isPosInfty ) {
			return POSITIVE_INFINITY;
		}
		if( num1.isNegInfty || num2.isNegInfty ) {
			return NEGATIVE_INFINITY;
		}
		
		return new Num( num1.doubleValue() * num2.doubleValue() );
	}

	public static Num div( Num num1, Num num2 ) {
		if( num1.isNaN || num2.isNaN ) {
			return NaN;
		}
		if( num1.isPosInfty ) {
			return POSITIVE_INFINITY;
		}
		if( num2.isPosInfty ) {
			return ZERO;
		}
		if( num1.isNegInfty ) {
			return NEGATIVE_INFINITY;
		}
		if( num2.isNegInfty ) {
			return ZERO;
		}
		
		return new Num( num1.doubleValue() / num2.doubleValue() );
	}

	public static Num diff( Num num1, Num num2 ) {
		if( num1.isNaN || num2.isNaN ) {
			return NaN;
		}
		
		if( num1.isPosInfty || num1.isNegInfty 
				 || num2.isPosInfty || num2.isNegInfty ) {
			return POSITIVE_INFINITY;
		}
		
		return new Num( Math.max( num1.doubleValue(), num2.doubleValue() )
										- Math.min( num1.doubleValue(), num2.doubleValue() ) );	
	}

	public Num copy() {
		if ( this.isNaN ) {
    		return NaN;
    	}
    	if ( this.isPosInfty ) {
    		return POSITIVE_INFINITY;
    	}
    	if ( this.isNegInfty ) {
			return NEGATIVE_INFINITY;
		}
    	
		return new Num( value );
	}

	public static Num max( Num num1, Num num2 ) {
		if( num1.isNaN || num2.isNaN ) {
			return NaN;
		}
		if( num1.isPosInfty ) {
			return num1;
		}
		if( num1.isNegInfty ) {
			return num2;
		}
		
		return new Num( Math.max( num1.doubleValue(), num2.doubleValue() ) );
	}

	public static Num min( Num num1, Num num2 ) {
		if( num1.isNaN || num2.isNaN ) {
			return NaN;
		}
		if( num1.isPosInfty ) {
			return num2;
		}
		if( num1.isNegInfty ) {
			return num1;
		}
		
		return new Num( Math.min( num1.doubleValue(), num2.doubleValue() ) );
	}
	
	public boolean equals( double num2 ) {
		if( num2 == Double.NaN ){
			return this.isNaN;
		}
		if( num2 == Double.POSITIVE_INFINITY ){
			return this.isPosInfty;
		}
		if( num2 == Double.NEGATIVE_INFINITY ){
			return this.isNegInfty;
		}
		
		if( Math.abs( value - num2 ) <= EPSILON ) {
			return true;
		} else {
			return false;
		}
	}

	public boolean equals( Num num2 ) {
		return equals( num2.value );
	}

	@Override
	public boolean equals( Object num2 ) {
		Num num2_Num;
		try {
			num2_Num = (Num) num2;
			return equals( num2_Num.value );
		} catch( ClassCastException e ) {
			return false;
		}
	}

	public boolean greater( Num num2 ) {
		if( this.isNaN || num2.isNaN ){
			return false;
		}
		
		if( num2.isPosInfty ){
			return false;
		}
		if( this.isPosInfty ){
			return true;
		}
		
		if( this.isNegInfty ){
			return false;
		}
		if( num2.isNegInfty ){
			return true;
		}
		
		return value > num2.doubleValue();
	}

	public boolean ge( Num num2 ) {
		if( this.isNaN || num2.isNaN ){
			return false;
		}
		
		if( this.isPosInfty ){
			return true;
		}
		if( num2.isPosInfty ){
			return false;
		}

		if( num2.isNegInfty ){
			return true;
		}
		if( this.isNegInfty ){
			return false;
		}
		
		return value >= num2.doubleValue();
	}

	public boolean less( Num num2 ) {
		if( this.isNaN || num2.isNaN ){
			return false;
		}

		if( this.isPosInfty ){
			return false;
		}
		if( num2.isPosInfty ){
			return true;
		}
		
		if( num2.isNegInfty ){
			return false;
		}
		if( this.isNegInfty ){
			return true;
		}
		
		return value < num2.doubleValue();
	}

	public boolean le( Num num2 ) {
		if( this.isNaN || num2.isNaN ){
			return false;
		}

		if( num2.isPosInfty ){
			return true;
		}
		if( this.isPosInfty ){
			return false;
		}

		if( this.isNegInfty ){
			return true;
		}
		if( num2.isNegInfty ){
			return false;
		}
		
		return value <= num2.doubleValue();
	}
	
	public static Num abs( Num num ) {
		if ( num.isNaN ) {
    		return NaN;
    	}
    	if ( num.isPosInfty ) {
    		return POSITIVE_INFINITY;
    	}
    	if ( num.isNegInfty ) {
			return NEGATIVE_INFINITY;
		}

		return new Num( Math.abs( num.doubleValue() ) );
	}

	public static Num negate( Num num ) {
		if ( num.isNaN ) {
    		return NaN;
    	}
    	if ( num.isPosInfty ) {
    		return NEGATIVE_INFINITY;
    	}
    	if ( num.isNegInfty ) {
			return POSITIVE_INFINITY;
		}
    	
	    return new Num( num.doubleValue() * -1 );
	}
	
	public double doubleValue() {
	    return value;
	}

	public float floatValue() {
		if ( this.isNaN ) {
    		return Float.NaN;
    	}
    	if ( this.isPosInfty ) {
    		return Float.POSITIVE_INFINITY;
    	}
    	if ( this.isNegInfty ) {
			return Float.NEGATIVE_INFINITY;
		}
    	
	    return new Float( value );
	}
	
	@Override
	public String toString(){
		if ( this.isNaN ) {
    		return "NaN";
    	}
    	if ( this.isPosInfty ) {
    		return "Infinity";
    	}
    	if ( this.isNegInfty ) {
			return "-Infinity";
		}
    	
		return Double.toString( value );
	}
	
	private static Num parse( String num_str ) throws Exception {
		boolean fraction_indicator = num_str.contains( " / " );
		boolean double_based = num_str.contains( "." );
		
		if ( fraction_indicator && double_based ) {
			throw new Exception( "Invalid string representation of a number based on " + representation.toString() );
		}
		
		try {
			// Either an integer of something strange
			if ( !fraction_indicator && !double_based ) {
				return new Num( Integer.parseInt( num_str ) );
			}
			
			if ( fraction_indicator ) {
				String[] num_den = num_str.split( " / " ); // ["num","den"]
				if( num_den.length != 2 ) {
					throw new Exception( "Invalid string representation of a number based on " + representation.toString() );
				}
				return new Num( Integer.parseInt( num_den[0] ), Integer.parseInt( num_den[1] ) );
			}
			
			if ( double_based ) {
				return new Num( Double.parseDouble( num_str ) );
			}
		} catch (Exception e) {
			throw new Exception( "Invalid string representation of a number based on " + representation.toString() );
		}
		
		// This code should not be reachable because all the operations above either succeed such that we can return a number
		// of raise an exception of some kind. Yet, Java does not get this and thus complains if there's no "finalizing statement". 
		throw new Exception( "Invalid string representation of a number based on " + representation.toString() );
	}
}
