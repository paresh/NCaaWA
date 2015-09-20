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

package unikl.disco.dnc.shared;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;

/**
 * 
 * @author Steffen Bondorf
 *
 */
//public final class Configuration implements Serializable {
public class Configuration implements Serializable {
	private boolean use_short_alias = true;

	private boolean arrival_curve_checks = true;
	private boolean service_curve_checks = true;
	private boolean fifo_mux_checks = true;
	private boolean deconvolution_checks = true;

	public enum MuxDiscipline { SERVER_LOCAL, GLOBAL_ARBITRARY, GLOBAL_FIFO }

	public enum GammaFlag { SERVER_LOCAL, GLOBALLY_ON, GLOBALLY_OFF }

	private MuxDiscipline multiplexing_discipline = MuxDiscipline.SERVER_LOCAL;

	private GammaFlag use_gamma = GammaFlag.SERVER_LOCAL;

	private GammaFlag use_extra_gamma = GammaFlag.SERVER_LOCAL;

	public enum AnalysisMethod { TFA, SFA, PMOO }
	
	public AnalysisMethod analysis_method = AnalysisMethod.PMOO;
	
	public enum ArrivalBoundMethods 
	{ PBOO_CONCATENATION, PBOO_PER_HOP, PMOO,
		PER_FLOW_SFA, PER_FLOW_PMOO }

	private HashSet<ArrivalBoundMethods> arrival_bound_methods = new HashSet<ArrivalBoundMethods>( Collections.singleton( ArrivalBoundMethods.PBOO_CONCATENATION ) );

	private boolean remove_duplicate_arrival_bounds = true;
	
	public Configuration() {}
	
	public boolean useShortAliases()
	{
		return use_short_alias;
	}

	public void setUseShortAliases( boolean short_alias )
	{
		use_short_alias = short_alias;
	}

	public boolean performArrivalCurveChecks()
	{
		return arrival_curve_checks;
	}

	public void setPerformArrivalCurveChecks( boolean checks )
	{
		arrival_curve_checks = checks;
	}

	public boolean performServiceCurveChecks()
	{
		return service_curve_checks;
	}

	public void setPerformServiceCurveChecks( boolean checks )
	{
		service_curve_checks = checks;
	}

	public boolean performFifoMultiplexingChecks()
	{
		return fifo_mux_checks;
	}

	public void setPerformFifoMultiplexingChecks( boolean checks )
	{
		fifo_mux_checks = checks;
	}

	public boolean performDeconvolutionChecks()
	{
		return deconvolution_checks;
	}

	public void setPerformDeconvolutionChecks( boolean checks )
	{
		deconvolution_checks = checks;
	}
	
	public MuxDiscipline multiplexingDiscipline()
	{
		return multiplexing_discipline;
	}
	
	public void setMultiplexingDiscipline( MuxDiscipline mux_discipline )
	{
		multiplexing_discipline = mux_discipline;
	}	
	
	public GammaFlag useGamma()
	{
		return use_gamma;
	}
	
	public void setUseGamma( GammaFlag use_gamma_flag )
	{
		use_gamma = use_gamma_flag;
	}
	
	public GammaFlag useExtraGamma()
	{
		return use_extra_gamma;
	}
	
	public void setUseExtraGamma( GammaFlag use_extra_gamma_flag )
	{
		use_extra_gamma = use_extra_gamma_flag;
	}
	
	public HashSet<ArrivalBoundMethods> arrivalBoundMethods()
	{
		return arrival_bound_methods;
	}
	
	public void setArrivalBoundMethod( ArrivalBoundMethods arrival_bound_method )
	{
		arrival_bound_methods.clear();
		arrival_bound_methods.add( arrival_bound_method );
	}

	public void setArrivalBoundMethods( HashSet<ArrivalBoundMethods> arrival_bound_methods_set )
	{
		arrival_bound_methods.clear();
		arrival_bound_methods.addAll( arrival_bound_methods_set );
	}

	public void addArrivalBoundMethod( ArrivalBoundMethods arrival_bound_method )
	{
		arrival_bound_methods.add( arrival_bound_method );
	}
	
	public void addArrivalBoundMethods( HashSet<ArrivalBoundMethods> arrival_bound_methods_set )
	{
		arrival_bound_methods.addAll( arrival_bound_methods_set );
	}
	
	public boolean removeDuplicateArrivalBounds()
	{
		return remove_duplicate_arrival_bounds;
	}
	
	public void setRemoveDuplicateArrivalBounds( boolean remove_duplicate_arrival_bounds_flag )
	{
		remove_duplicate_arrival_bounds = remove_duplicate_arrival_bounds_flag;
	}
}
