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

import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.LinkedList;
import java.util.HashSet;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import unikl.disco.dnc.server.nc.Analysis;
import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.Configuration.ArrivalBoundMethods;
import unikl.disco.dnc.shared.Configuration.MuxDiscipline;
import unikl.disco.dnc.shared.Num;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.ServiceCurve;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Link;
import unikl.disco.dnc.shared.network.Network;
import unikl.disco.dnc.shared.network.Server;
import unikl.disco.dnc.shared.results.PmooAnalysisResults;
import unikl.disco.dnc.shared.results.SeparateFlowAnalysisResults;
import unikl.disco.dnc.shared.results.TotalFlowAnalysisResults;

@RunWith(value = Parameterized.class)
/**
 * 
 * @author Steffen Bondorf
 *
 */
public class FeedForward_1SC_4Flows_1AC_4Paths
{
	Configuration config;
	
	static final ServiceCurve service_curve = ServiceCurve.createRateLatency( 20, 20 );
	static final ArrivalCurve arrival_curve = ArrivalCurve.createTokenBucket( 5, 25 );
	
	static Network network;
	static Server s0, s1, s2, s3;
	static Flow f0, f1, f2, f3;
	static Link l_s0_s1, l_s1_s3, l_s2_s0, l_s0_s3;
	
	@Parameters
	public static Collection<Object[]> data() {
		return FunctionalTests.createParameters();
	}
	 
	public FeedForward_1SC_4Flows_1AC_4Paths( HashSet<ArrivalBoundMethods> arrival_boundings, boolean iterative_ab, boolean remove_duplicates ) {
		config = FunctionalTests.printTestSettings( arrival_boundings, iterative_ab, remove_duplicates );
	}
	
	@BeforeClass
	static public void createNetwork()
	{
		network = new Network();
		s0 = network.addServer( service_curve );
		s1 = network.addServer( "s1", service_curve );
		s2 = network.addServer( service_curve );
		s3 = network.addServer( "s3", service_curve );

		try {
			l_s0_s1 = network.addLink( s0, s1 );
			l_s0_s3 = network.addLink( s0, s3 );
			l_s1_s3 = network.addLink( s1, s3 );
			l_s2_s0 = network.addLink( s2, s0 );
			network.addLink( s2, s1 );
			network.addLink( s2, s3 );
		} catch (Exception e) {
			System.out.println( e.toString() );
			assertEquals( "Unexpected exception occured", 0, 1 );
			return;
		}
		
		LinkedList<Link> f0_path = new LinkedList<Link>();
		f0_path.add( l_s0_s1 );
		f0_path.add( l_s1_s3 );
		
		LinkedList<Link> f3_path = new LinkedList<Link>();
		f3_path.add( l_s2_s0 );
		f3_path.add( l_s0_s3 );
		
		try {	
			f0 = network.addFlow( arrival_curve, f0_path );
			f1 = network.addFlow( arrival_curve, s2, s3 );
			f2 = network.addFlow( arrival_curve, s2, s1 );
			f3 = network.addFlow( arrival_curve, f3_path );
		} catch (Exception e) {
			System.out.println( e.toString() );
			assertEquals( "Unexpected exception occured", 0, 1 );
			return;
		}
	}
	
//--------------------Flow 0--------------------	
	@Test
	public void f0_tfa_fifoMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_FIFO );
		
		TotalFlowAnalysisResults tfa_results = Analysis.performTfaEnd2End( network, config, f0 );
		
		if ( tfa_results.failure == true ) {
			System.out.println( "TFA analysis failed" );
			System.out.println();
			
			if( !config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
				assertEquals( "Unexpected exception occured", 0, 1 );
			}
			
			return;
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tTotal Flow Analysis (TFA)" );
			System.out.println( "Multiplexing:\t\tFIFO" );
	
			System.out.println( "Flow of interest:\t" + f0.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
					
			System.out.println( "delay bound     : " + tfa_results.delay_bound );
			System.out.println( "     per server : " + tfa_results.map__server__D_server.toString() );
			System.out.println( "backlog bound   : " + tfa_results.backlog_bound );
			System.out.println( "     per server : " + tfa_results.map__server__B_server.toString() );
			System.out.println( "alpha per server: " + tfa_results.map__server__alphas.toString() );
		}
		
		assertEquals( "TFA FIFO delay", new Num( 3735, 32 ), tfa_results.delay_bound );
		assertEquals( "TFA FIFO backlog", new Num( 975 ), tfa_results.backlog_bound );
	}
	
	@Test
	public void f0_tfa_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		TotalFlowAnalysisResults tfa_results = Analysis.performTfaEnd2End( network, config, f0 );
		
		if ( tfa_results.failure == true ) {
			System.out.println( "TFA analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tTotal Flow Analysis (TFA)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f0.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
					
			System.out.println( "delay bound     : " + tfa_results.delay_bound );
			System.out.println( "     per server : " + tfa_results.map__server__D_server.toString() );
			System.out.println( "backlog bound   : " + tfa_results.backlog_bound );
			System.out.println( "     per server : " + tfa_results.map__server__B_server.toString() );
			System.out.println( "alpha per server: " + tfa_results.map__server__alphas.toString() );
		}

		if( config.arrivalBoundMethods().size() == 1
				&& config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
			assertEquals( "TFA ARB delay", new Num( 2765, 6 ), tfa_results.delay_bound );
			assertEquals( "TFA ARB backlog", new Num( 8525, 6 ), tfa_results.backlog_bound );
		} else {
			assertEquals( "TFA ARB delay", new Num( 1370, 3 ), tfa_results.delay_bound );
			assertEquals( "TFA ARB backlog", new Num( 1400 ), tfa_results.backlog_bound );
		}
	}

	@Test
	public void f0_sfa_fifoMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_FIFO );
		
		SeparateFlowAnalysisResults sfa_results = Analysis.performSfaEnd2End( network, config, f0 );
		
		if ( sfa_results.failure == true ) {
			System.out.println( "SFA analysis failed" );
			System.out.println();
			
			if( !config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
				assertEquals( "Unexpected exception occured", 0, 1 );
			}
			
			return;
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tSeparate Flow Analysis (SFA)" );
			System.out.println( "Multiplexing:\t\tFIFO" );
	
			System.out.println( "Flow of interest:\t" + f0.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
			
			System.out.println( "e2e SFA SCs     : " + sfa_results.betas_e2e );
			System.out.println( "     per server : " + sfa_results.map__server__betas_lo.toString() );
			System.out.println( "xtx per server  : " + sfa_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + sfa_results.delay_bound );
			System.out.println( "backlog bound   : " + sfa_results.backlog_bound );
		}

		assertEquals( "SFA FIFO delay", new Num( 95 ), sfa_results.delay_bound );
		assertEquals( "SFA FIFO backlog", new Num( 975, 2 ), sfa_results.backlog_bound );
	}

	@Test
	public void f0_sfa_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		SeparateFlowAnalysisResults sfa_results = Analysis.performSfaEnd2End( network, config, f0 );
		
		if ( sfa_results.failure == true ) {
			System.out.println( "SFA analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tSeparate Flow Analysis (SFA)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f0.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
			
			System.out.println( "e2e SFA SCs     : " + sfa_results.betas_e2e );
			System.out.println( "     per server : " + sfa_results.map__server__betas_lo.toString() );
			System.out.println( "xtx per server  : " + sfa_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + sfa_results.delay_bound );
			System.out.println( "backlog bound   : " + sfa_results.backlog_bound );
		}

		assertEquals( "SFA ARB delay", new Num( 1135, 6 ), sfa_results.delay_bound );
		assertEquals( "SFA ARB backlog", new Num( 2875, 3 ), sfa_results.backlog_bound );
	}
	
	@Test
	public void f0_pmoo_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		PmooAnalysisResults pmoo_results = Analysis.performPmooEnd2End( network, config, f0 );
		
		if ( pmoo_results.failure == true ) {
			System.out.println( "PMOO analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tPay Multiplexing Only Once (PMOO)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f0.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );

			System.out.println( "e2e PMOO SCs    : " + pmoo_results.betas_e2e );
			System.out.println( "xtx per server  : " + pmoo_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + pmoo_results.delay_bound );
			System.out.println( "backlog bound   : " + pmoo_results.backlog_bound );
		}

		assertEquals( "PMOO ARB delay", new Num( 425, 2 ), pmoo_results.delay_bound );
		assertEquals( "PMOO ARB backlog", new Num( 1075 ), pmoo_results.backlog_bound );
	}

//--------------------Flow 1--------------------	
	@Test
	public void f1_tfa_fifoMux()
	{		
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_FIFO );
		
		TotalFlowAnalysisResults tfa_results = Analysis.performTfaEnd2End( network, config, f1 );
		
		if ( tfa_results.failure == true ) {
			System.out.println( "TFA analysis failed" );
			System.out.println();
			
			if( !config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
				assertEquals( "Unexpected exception occured", 0, 1 );
			}
			
			return;
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tTotal Flow Analysis (TFA)" );
			System.out.println( "Multiplexing:\t\tFIFO" );
	
			System.out.println( "Flow of interest:\t" + f1.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
					
			System.out.println( "delay bound     : " + tfa_results.delay_bound );
			System.out.println( "     per server : " + tfa_results.map__server__D_server.toString() );
			System.out.println( "backlog bound   : " + tfa_results.backlog_bound );
			System.out.println( "     per server : " + tfa_results.map__server__B_server.toString() );
			System.out.println( "alpha per server: " + tfa_results.map__server__alphas.toString() );
		}
		
		assertEquals( "TFA FIFO delay", new Num( 77.5 ), tfa_results.delay_bound );
		assertEquals( "TFA FIFO backlog", new Num( 975 ), tfa_results.backlog_bound );
	}
	
	@Test
	public void f1_tfa_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		TotalFlowAnalysisResults tfa_results = Analysis.performTfaEnd2End( network, config, f1 );
		
		if ( tfa_results.failure == true ) {
			System.out.println( "TFA analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tTotal Flow Analysis (TFA)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f1.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
					
			System.out.println( "delay bound     : " + tfa_results.delay_bound );
			System.out.println( "     per server : " + tfa_results.map__server__D_server.toString() );
			System.out.println( "backlog bound   : " + tfa_results.backlog_bound );
			System.out.println( "     per server : " + tfa_results.map__server__B_server.toString() );
			System.out.println( "alpha per server: " + tfa_results.map__server__alphas.toString() );
		}
		
		if( config.arrivalBoundMethods().size() == 1
				&& config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
			assertEquals( "TFA ARB delay", new Num( 2395, 6 ), tfa_results.delay_bound );
			assertEquals( "TFA ARB backlog", new Num( 8525, 6 ), tfa_results.backlog_bound );
		} else {
			assertEquals( "TFA ARB delay", new Num( 395 ), tfa_results.delay_bound );
			assertEquals( "TFA ARB backlog", new Num( 1400 ), tfa_results.backlog_bound );
		}
	}
	
	@Test
	public void f1_sfa_fifoMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_FIFO );
		
		SeparateFlowAnalysisResults sfa_results = Analysis.performSfaEnd2End( network, config, f1 );
		
		if ( sfa_results.failure == true ) {
			System.out.println( "SFA analysis failed" );
			System.out.println();
			
			if( !config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
				assertEquals( "Unexpected exception occured", 0, 1 );
			}
			
			return;
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tSeparate Flow Analysis (SFA)" );
			System.out.println( "Multiplexing:\t\tFIFO" );
	
			System.out.println( "Flow of interest:\t" + f1.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
			
			System.out.println( "e2e SFA SCs     : " + sfa_results.betas_e2e );
			System.out.println( "     per server : " + sfa_results.map__server__betas_lo.toString() );
			System.out.println( "xtx per server  : " + sfa_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + sfa_results.delay_bound );
			System.out.println( "backlog bound   : " + sfa_results.backlog_bound );
		}

		assertEquals( "SFA FIFO delay", new Num( 2285, 32 ), sfa_results.delay_bound );
		assertEquals( "SFA FIFO backlog", new Num( 11825, 32 ), sfa_results.backlog_bound );
	}
	
	@Test
	public void f1_sfa_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		SeparateFlowAnalysisResults sfa_results = Analysis.performSfaEnd2End( network, config, f1 );
		
		if ( sfa_results.failure == true ) {
			System.out.println( "SFA analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tSeparate Flow Analysis (SFA)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f1.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
			
			System.out.println( "e2e SFA SCs     : " + sfa_results.betas_e2e );
			System.out.println( "     per server : " + sfa_results.map__server__betas_lo.toString() );
			System.out.println( "xtx per server  : " + sfa_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + sfa_results.delay_bound );
			System.out.println( "backlog bound   : " + sfa_results.backlog_bound );
		}
		
		assertEquals( "SFA ARB delay", new Num( 2855, 18 ), sfa_results.delay_bound );
		assertEquals( "SFA ARB backlog", new Num( 7250, 9 ), sfa_results.backlog_bound );
	}
	
	@Test
	public void f1_pmoo_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		PmooAnalysisResults pmoo_results = Analysis.performPmooEnd2End( network, config, f1 );
		
		if ( pmoo_results.failure == true ) {
			System.out.println( "PMOO analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tPay Multiplexing Only Once (PMOO)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f1.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );

			System.out.println( "e2e PMOO SCs    : " + pmoo_results.betas_e2e );
			System.out.println( "xtx per server  : " + pmoo_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + pmoo_results.delay_bound );
			System.out.println( "backlog bound   : " + pmoo_results.backlog_bound );
		}
		
		assertEquals( "PMOO ARB delay", new Num( 2855, 18 ), pmoo_results.delay_bound );
		assertEquals( "PMOO ARB backlog", new Num( 7250, 9 ), pmoo_results.backlog_bound );
	}
	
//--------------------Flow 2--------------------	
	@Test
	public void f2_tfa_fifoMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_FIFO );
		
		TotalFlowAnalysisResults tfa_results = Analysis.performTfaEnd2End( network, config, f2 );
		
		if ( tfa_results.failure == true ) {
			System.out.println( "TFA analysis failed" );
			System.out.println();
			
			if( !config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
				assertEquals( "Unexpected exception occured", 0, 1 );
			}
			
			return;
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tTotal Flow Analysis (TFA)" );
			System.out.println( "Multiplexing:\t\tFIFO" );
	
			System.out.println( "Flow of interest:\t" + f2.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
					
			System.out.println( "delay bound     : " + tfa_results.delay_bound );
			System.out.println( "     per server : " + tfa_results.map__server__D_server.toString() );
			System.out.println( "backlog bound   : " + tfa_results.backlog_bound );
			System.out.println( "     per server : " + tfa_results.map__server__B_server.toString() );
			System.out.println( "alpha per server: " + tfa_results.map__server__alphas.toString() );
		}
		
		assertEquals( "TFA FIFO delay", new Num( 1875, 32 ), tfa_results.delay_bound );
		assertEquals( "TFA FIFO backlog", new Num( 3975, 8 ), tfa_results.backlog_bound );
	}
	
	@Test
	public void f2_tfa_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		TotalFlowAnalysisResults tfa_results = Analysis.performTfaEnd2End( network, config, f2 );
		
		if ( tfa_results.failure == true ) {
			System.out.println( "TFA analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tTotal Flow Analysis (TFA)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f2.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
					
			System.out.println( "delay bound     : " + tfa_results.delay_bound );
			System.out.println( "     per server : " + tfa_results.map__server__D_server.toString() );
			System.out.println( "backlog bound   : " + tfa_results.backlog_bound );
			System.out.println( "     per server : " + tfa_results.map__server__B_server.toString() );
			System.out.println( "alpha per server: " + tfa_results.map__server__alphas.toString() );
		}
		
		assertEquals( "TFA ARB delay", new Num( 1105, 6 ), tfa_results.delay_bound );
		assertEquals( "TFA ARB backlog", new Num( 2075, 3 ), tfa_results.backlog_bound );
	}
	
	@Test
	public void f2_sfa_fifoMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_FIFO );
		
		SeparateFlowAnalysisResults sfa_results = Analysis.performSfaEnd2End( network, config, f2 );
		
		if ( sfa_results.failure == true ) {
			System.out.println( "SFA analysis failed" );
			System.out.println();
			
			if( !config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
				assertEquals( "Unexpected exception occured", 0, 1 );
			}
			
			return;
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tSeparate Flow Analysis (SFA)" );
			System.out.println( "Multiplexing:\t\tFIFO" );
	
			System.out.println( "Flow of interest:\t" + f2.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
			
			System.out.println( "e2e SFA SCs     : " + sfa_results.betas_e2e );
			System.out.println( "     per server : " + sfa_results.map__server__betas_lo.toString() );
			System.out.println( "xtx per server  : " + sfa_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + sfa_results.delay_bound );
			System.out.println( "backlog bound   : " + sfa_results.backlog_bound );
		}

		assertEquals( "SFA FIFO delay", new Num( 3385, 64 ), sfa_results.delay_bound );
		assertEquals( "SFA FIFO backlog", new Num( 17725, 64 ), sfa_results.backlog_bound );
	}
	
	@Test
	public void f2_sfa_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		SeparateFlowAnalysisResults sfa_results = Analysis.performSfaEnd2End( network, config, f2 );
		
		if ( sfa_results.failure == true ) {
			System.out.println( "SFA analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tSeparate Flow Analysis (SFA)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f2.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
			
			System.out.println( "e2e SFA SCs     : " + sfa_results.betas_e2e );
			System.out.println( "     per server : " + sfa_results.map__server__betas_lo.toString() );
			System.out.println( "xtx per server  : " + sfa_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + sfa_results.delay_bound );
			System.out.println( "backlog bound   : " + sfa_results.backlog_bound );
		}

		assertEquals( "SFA ARB delay", new Num( 4775, 54 ), sfa_results.delay_bound );
		assertEquals( "SFA ARB backlog", new Num( 12275, 27 ), sfa_results.backlog_bound );
	}
	
	@Test
	public void f2_pmoo_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		PmooAnalysisResults pmoo_results = Analysis.performPmooEnd2End( network, config, f2 );
		
		if ( pmoo_results.failure == true ) {
			System.out.println( "PMOO analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tPay Multiplexing Only Once (PMOO)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f2.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );

			System.out.println( "e2e PMOO SCs    : " + pmoo_results.betas_e2e );
			System.out.println( "xtx per server  : " + pmoo_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + pmoo_results.delay_bound );
			System.out.println( "backlog bound   : " + pmoo_results.backlog_bound );
		}

		assertEquals( "PMOO ARB delay", new Num( 890, 9 ), pmoo_results.delay_bound );
		assertEquals( "PMOO ARB backlog", new Num( 9125, 18 ), pmoo_results.backlog_bound );
	}
		
//--------------------Flow 3--------------------	
	@Test
	public void f3_tfa_fifoMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_FIFO );
		
		TotalFlowAnalysisResults tfa_results = Analysis.performTfaEnd2End( network, config, f3 );
		
		if ( tfa_results.failure == true ) {
			System.out.println( "TFA analysis failed" );
			System.out.println();
			
			if( !config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
				assertEquals( "Unexpected exception occured", 0, 1 );
			}
			
			return;
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tTotal Flow Analysis (TFA)" );
			System.out.println( "Multiplexing:\t\tFIFO" );
	
			System.out.println( "Flow of interest:\t" + f3.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
					
			System.out.println( "delay bound     : " + tfa_results.delay_bound );
			System.out.println( "     per server : " + tfa_results.map__server__D_server.toString() );
			System.out.println( "backlog bound   : " + tfa_results.backlog_bound );
			System.out.println( "     per server : " + tfa_results.map__server__B_server.toString() );
			System.out.println( "alpha per server: " + tfa_results.map__server__alphas.toString() );
		}

		assertEquals( "TFA FIFO delay", new Num( 845, 8 ), tfa_results.delay_bound );
		assertEquals( "TFA FIFO backlog", new Num( 975 ), tfa_results.backlog_bound );
	}
	
	@Test
	public void f3_tfa_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		TotalFlowAnalysisResults tfa_results = Analysis.performTfaEnd2End( network, config, f3 );
		
		if ( tfa_results.failure == true ) {
			System.out.println( "TFA analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tTotal Flow Analysis (TFA)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f3.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
					
			System.out.println( "delay bound     : " + tfa_results.delay_bound );
			System.out.println( "     per server : " + tfa_results.map__server__D_server.toString() );
			System.out.println( "backlog bound   : " + tfa_results.backlog_bound );
			System.out.println( "     per server : " + tfa_results.map__server__B_server.toString() );
			System.out.println( "alpha per server: " + tfa_results.map__server__alphas.toString() );
		}
		
		if( config.arrivalBoundMethods().size() == 1
				&& config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
			assertEquals( "TFA ARB delay", new Num( 1400, 3 ), tfa_results.delay_bound );
			assertEquals( "TFA ARB backlog", new Num( 8525, 6 ), tfa_results.backlog_bound );
		} else {
			assertEquals( "TFA ARB delay", new Num( 462.5 ), tfa_results.delay_bound );
			assertEquals( "TFA ARB backlog", new Num( 1400 ), tfa_results.backlog_bound );
		}
	}
	
	@Test
	public void f3_sfa_fifoMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_FIFO );
		
		SeparateFlowAnalysisResults sfa_results = Analysis.performSfaEnd2End( network, config, f3 );
		
		if ( sfa_results.failure == true ) {
			System.out.println( "SFA analysis failed" );
			System.out.println();
			
			if( !config.arrivalBoundMethods().contains( ArrivalBoundMethods.PMOO ) ) {
				assertEquals( "Unexpected exception occured", 0, 1 );
			}
			
			return;
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tSeparate Flow Analysis (SFA)" );
			System.out.println( "Multiplexing:\t\tFIFO" );
	
			System.out.println( "Flow of interest:\t" + f3.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
			
			System.out.println( "e2e SFA SCs     : " + sfa_results.betas_e2e );
			System.out.println( "     per server : " + sfa_results.map__server__betas_lo.toString() );
			System.out.println( "xtx per server  : " + sfa_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + sfa_results.delay_bound );
			System.out.println( "backlog bound   : " + sfa_results.backlog_bound );
		}

		assertEquals( "SFA FIFO delay", new Num( 5485, 64 ), sfa_results.delay_bound );
		assertEquals( "SFA FIFO backlog", new Num( 28225, 64 ), sfa_results.backlog_bound );
	}
	
	@Test
	public void f3_sfa_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		SeparateFlowAnalysisResults sfa_results = Analysis.performSfaEnd2End( network, config, f3 );
		
		if ( sfa_results.failure == true ) {
			System.out.println( "SFA analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tSeparate Flow Analysis (SFA)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f3.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );
			
			System.out.println( "e2e SFA SCs     : " + sfa_results.betas_e2e );
			System.out.println( "     per server : " + sfa_results.map__server__betas_lo.toString() );
			System.out.println( "xtx per server  : " + sfa_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + sfa_results.delay_bound );
			System.out.println( "backlog bound   : " + sfa_results.backlog_bound );
		}
		
		assertEquals( "SFA ARB delay", new Num( 1475, 9 ), sfa_results.delay_bound );
		assertEquals( "SFA ARB backlog", new Num( 14975, 18 ), sfa_results.backlog_bound );
	}
	
	@Test
	public void f3_pmoo_arbMux()
	{
		config.setMultiplexingDiscipline( MuxDiscipline.GLOBAL_ARBITRARY );
		
		PmooAnalysisResults pmoo_results = Analysis.performPmooEnd2End( network, config, f3 );
		
		if ( pmoo_results.failure == true ) {
			System.out.println( "PMOO analysis failed" );
			System.out.println();
			
			assertEquals( "Unexpected exception occured", 0, 1 );
		}
	
		if( FunctionalTests.fullConsoleOutput() ) {
			System.out.println( "Analysis:\t\tPay Multiplexing Only Once (PMOO)" );
			System.out.println( "Multiplexing:\t\tArbitrary" );
	
			System.out.println( "Flow of interest:\t" + f3.toString() );
			System.out.println();
			
			System.out.println( "--- Results: ---" );

			System.out.println( "e2e PMOO SCs    : " + pmoo_results.betas_e2e );
			System.out.println( "xtx per server  : " + pmoo_results.map__server__alphas.toString() );
			System.out.println( "delay bound     : " + pmoo_results.delay_bound );
			System.out.println( "backlog bound   : " + pmoo_results.backlog_bound );
		}
		
		assertEquals( "PMOO ARB delay", new Num( 3025, 18 ), pmoo_results.delay_bound );
		assertEquals( "PMOO ARB backlog", new Num( 7675, 9 ), pmoo_results.backlog_bound );
	}
}