/*
 * This file is part of the Disco Deterministic Network Calculator v2.0.3 "Hydra".
 *
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

package unikl.disco.dnc.shared.network;

import java.io.Serializable;

import unikl.disco.dnc.shared.curves.ServiceCurve;

/**
 * 
 * @author Steffen Bondorf
 *
 */
public class Server implements Serializable {
	private int id;
	private String alias;
	
	private ServiceCurve service_curve = ServiceCurve.createNullService();
	/**
	 * A zero delay burst curve lets the influence of the maximum service curve vanish
	 */
	private ServiceCurve max_service_curve = ServiceCurve.createZeroDelayBurst();
	
	/** Whether to use maximum service curves in output bound computation */
	private boolean	use_gamma = false;
	/**
	 * Whether to constrain the output bound further through convolution with
	 * the maximum service curve
	 */
	private boolean	use_extra_gamma = false;
	
	// true = arbitrary multiplexing, false = FIFO multiplexing
	private boolean arbitrary_multiplexing = true;
	
	@SuppressWarnings("unused")
	private Server(){}

	/**
	 * @param alias
	 * @param sc
	 * @param msc
	 * @param use_gamma
	 * @param use_extra_gamma
	 */
	protected Server( int id, String alias, ServiceCurve sc, ServiceCurve msc, boolean arbitrary_mux, boolean use_gamma, boolean use_extra_gamma ){
		this.id = id;
		this.alias = alias;
		service_curve = sc;
		max_service_curve = msc;
		
		arbitrary_multiplexing = arbitrary_mux;
		
		this.use_gamma = use_gamma;
		this.use_extra_gamma = use_extra_gamma;
	}
	
	public int getId() {
		return id;
	}
	
	public boolean setServiceCurve( ServiceCurve service_curve ) {
		this.service_curve = service_curve;
		return true;
	}

	/**
	 * 
	 * @return A copy of the service curve
	 */
	public ServiceCurve getServiceCurve() {
		return service_curve.copy();
	}
	
	/**
	 * Lists the maximum service curve for the specified server.
	 * Setting a maximum service curve also enables useGamma and useExtraGamma.
	 * 
	 * @param max_service_curve	the service curve
	 */
	public boolean setMaxServiceCurve( ServiceCurve max_service_curve ) {
		return setMaxServiceCurve( max_service_curve, true, true );
	}

	public boolean setMaxServiceCurve( ServiceCurve max_service_curve, boolean use_gamma, boolean use_extra_gamma ) {
		this.max_service_curve = max_service_curve;

		use_gamma = true;
		use_extra_gamma = true;
		
		return true;
	}
	
	public boolean removeMaxServiceCurve() {
		max_service_curve = ServiceCurve.createZeroDelayBurst();
		
		use_gamma = false;
		use_extra_gamma = false;
		
		return true;
	}

	/**
	 * 
	 * @return A copy of the maximum service curve
	 */
	public ServiceCurve getMaxServiceCurve() {
		return max_service_curve;
	}
	
	/**
	 * In contrast to <code>getMaxServiceCurve()</code> this function
	 * always returns the default zero delay burst curve if
	 * the useGamma flag is not set.
	 * 
	 * @return The gamma curve
	 */
	public ServiceCurve getGamma(){
		if( use_gamma == false ){
			return ServiceCurve.createZeroDelayBurst();
		} else {
			return max_service_curve;
		}
	}
	
	/**
	 * In contrast to <code>getMaxServiceCurve()</code> this function
	 * always returns the default zero delay burst curve if
	 * the useExtraGamma flag is not set.
	 * 
	 * @return The gamma curve
	 */
	public ServiceCurve getExtraGamma(){
		if( use_gamma == false ){
			return ServiceCurve.createZeroDelayBurst();
		} else {
			return max_service_curve;
		}
	}
	
	public boolean useGamma()
	{
		return use_gamma;
	}

	public void setUseGamma( boolean use_gamma )
	{
		this.use_gamma = use_gamma;
	}

	public boolean useExtraGamma()
	{
		return use_extra_gamma;
	}

	public void setUseExtraGamma( boolean use_extra_gamma )
	{
		this.use_extra_gamma = use_extra_gamma;
	}
	
	public boolean useArbitraryMultiplexing(){
		return arbitrary_multiplexing;
	}
	
	public boolean useFifoMultiplexing(){
		return !arbitrary_multiplexing;
	}
	
	public void setUseArbitraryMultiplexing( boolean arb_mux ){
		arbitrary_multiplexing = arb_mux;
	}
	
	public void setUseFifoMultiplexing( boolean fifo_mux ){
		arbitrary_multiplexing = !fifo_mux;
	}

	public String getAlias() {
		return alias;
	}
	
	public void setAlias( String alias ) {
		this.alias = alias;
	}

	@Override
	public String toString() {
		String mux;
		if ( arbitrary_multiplexing ) {
			mux = "ARB_MUX";
		} else {
			mux = "FIFO_MUX";
		}
		
		// TODO I lost that ability, unfortunately
//		if ( Configuration.useShortAliases() ) {
//			return alias;
//		} else {
			return "(" + alias + ", " + service_curve.toString() + ", " + max_service_curve.toString() + ", "
					+ mux + ", " + Boolean.toString( use_gamma ) + ", " + Boolean.toString( use_extra_gamma ) + ")";
//		}		
	}
}
