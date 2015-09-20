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

package unikl.disco.dnc.client.demos;

import java.util.ArrayList;

import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.Configuration.GammaFlag;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.ServiceCurve;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Network;
import unikl.disco.dnc.shared.network.Server;

/**
 * 
 * @author Steffen Bondorf
 */
public class Demo1
{
	public Network network;
	public Configuration configuration;
	public Flow flow_of_interest;
	ArrayList<String> servers= new ArrayList<String>();
	public Demo1() throws Exception {
		ArrivalCurve arrival_curve = ArrivalCurve.createTokenBucket( 0.1e6, 0.1 * 0.1e6 );
		
		ServiceCurve service_curve = ServiceCurve.createRateLatency( 10.0e6, 0.01 );
		ServiceCurve max_service_curve = ServiceCurve.createRateLatency( 100.0e6, 0.001 );
		
		network = new Network();
		configuration = new Configuration();
		
		Server s0 = network.addServer( service_curve, max_service_curve );
		servers.add(s0.getAlias());
		// Creating a server with a maximum service curve automatically triggers
//			s0.setUseGamma( true );
//			s0.setUseExtraGamma( true );
		// , however, it can be disabled globally by setting
		configuration.setUseGamma( GammaFlag.GLOBALLY_ON );
		configuration.setUseExtraGamma( GammaFlag.GLOBALLY_ON );
		
		flow_of_interest = network.addFlow( arrival_curve, s0 );
	}

	public ArrayList<String> getServers() {
		return servers;
	}

	public void setServers(ArrayList<String> servers) {
		this.servers = servers;
	}

	public Network getNetwork() {
		return network;
	}

	public void setNetwork(Network network) {
		this.network = network;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public Flow getFlow_of_interest() {
		return flow_of_interest;
	}

	public void setFlow_of_interest(Flow flow_of_interest) {
		this.flow_of_interest = flow_of_interest;
	}
}
