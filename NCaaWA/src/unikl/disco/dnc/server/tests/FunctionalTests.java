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

package unikl.disco.dnc.server.tests;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.Configuration.ArrivalBoundMethods;

@RunWith(Suite.class)
@SuiteClasses({
		Single_1Flow.class,
		Single_2Flows_1AC.class,
		Single_2Flows_2ACs.class,
		Single_10Flows_10ACs.class,
		Tandem_1SC_1Flow.class,
		Tandem_2SCs_1Flow.class,
		Tandem_1SC_2Flows_1AC_1Path.class,
		Tandem_2SCs_2Flows_1ACs_1Path.class,
		Tandem_1SC_2Flows_1AC_1Path_v2.class,
		Tandem_1SC_2Flows_1AC_2Paths.class,
		Tandem_1SC_2Flows_1AC_2Paths_v2.class,
		Tandem_1SC_3Flows_1AC_3Paths.class,
		Tandem_1SC_4Flows_1AC_1Path.class,
		Tree_1SC_2Flows_1AC_2Paths.class,
		Tree_1SC_3Flows_1AC_3Paths.class,
		FeedForward_1SC_2Flows_1AC_2Paths.class,
		FeedForward_1SC_3Flows_1AC_3Paths.class,
		FeedForward_1SC_4Flows_1AC_4Paths.class
		})
/**
 * 
 * @author Steffen Bondorf
 *
 */
public class FunctionalTests {
	private static boolean console_output = true;
	
	public static boolean fullConsoleOutput() { // Exceptions only
		return console_output;
	}
	
	//@Parameters(name = "Arrival bounding methods, Use iterative version?, Remove duplicate arrival bounds?")
	public static Collection<Object[]> createParameters(){
		Set<ArrivalBoundMethods> single_1 = new HashSet<ArrivalBoundMethods>();
		single_1.add( ArrivalBoundMethods.PBOO_CONCATENATION );

		Set<ArrivalBoundMethods> single_2 = new HashSet<ArrivalBoundMethods>();
		single_2.add( ArrivalBoundMethods.PBOO_PER_HOP );
		
		Set<ArrivalBoundMethods> single_3 = new HashSet<ArrivalBoundMethods>();
		single_3.add( ArrivalBoundMethods.PMOO );

		Set<ArrivalBoundMethods> pair_1 = new HashSet<ArrivalBoundMethods>();
		pair_1.add( ArrivalBoundMethods.PBOO_PER_HOP );
		pair_1.add( ArrivalBoundMethods.PBOO_CONCATENATION );
		
		Set<ArrivalBoundMethods> pair_2 = new HashSet<ArrivalBoundMethods>();
		pair_2.add( ArrivalBoundMethods.PBOO_PER_HOP );
		pair_2.add( ArrivalBoundMethods.PMOO );
		
		Set<ArrivalBoundMethods> pair_3 = new HashSet<ArrivalBoundMethods>();
		pair_3.add( ArrivalBoundMethods.PBOO_CONCATENATION );
		pair_3.add( ArrivalBoundMethods.PMOO );

		Set<ArrivalBoundMethods> triplet_1 = new HashSet<ArrivalBoundMethods>();
		triplet_1.add( ArrivalBoundMethods.PMOO );
		triplet_1.add( ArrivalBoundMethods.PBOO_PER_HOP );
		triplet_1.add( ArrivalBoundMethods.PBOO_CONCATENATION );
		
		Object[][] data = new Object[][] {
			{ single_1, false, false },
			{ single_1, true, false },
			
			{ single_2, false, false },

			{ single_3, false, false },

			{ pair_1, false, false },
			{ pair_1, true, false },
			{ pair_1, false, true },
			{ pair_1, true, true },
			
			{ pair_2, false, false },
			{ pair_2, true, false },
			{ pair_2, false, true },
			{ pair_2, true, true },
			
			{ pair_3, false, false },
			{ pair_3, true, false },
			{ pair_3, false, true },
			{ pair_3, true, true },
		
			{ triplet_1, false, false },
			{ triplet_1, true, false },
			{ triplet_1, false, true },
			{ triplet_1, true, true },
		};
		return Arrays.asList(data);
	}
	
	public static Configuration printTestSettings( HashSet<ArrivalBoundMethods> arrival_boundings, boolean iterative_ab, boolean remove_duplicates ) {
		Configuration config_part = new Configuration();
		
		config_part.setPerformServiceCurveChecks( false );
		config_part.setPerformArrivalCurveChecks( false );
		config_part.setPerformFifoMultiplexingChecks( false );
		config_part.setPerformDeconvolutionChecks( false );
		
		if ( console_output ) {
			System.out.println( "--------------------------------------------------------------" );
			System.out.println();
			
			config_part.setArrivalBoundMethods( arrival_boundings );
			System.out.println( "Arrival Boundings:\t" + arrival_boundings.toString() );
			
			config_part.setRemoveDuplicateArrivalBounds( remove_duplicates );
			System.out.println( "Remove duplicate ABs:\t" + Boolean.toString( remove_duplicates ) );
		}
		
		return config_part;
	}
}
