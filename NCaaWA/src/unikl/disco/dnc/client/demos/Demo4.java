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
import java.util.LinkedList;

import unikl.disco.dnc.shared.Configuration;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.ServiceCurve;
import unikl.disco.dnc.shared.network.Flow;
import unikl.disco.dnc.shared.network.Link;
import unikl.disco.dnc.shared.network.Network;
import unikl.disco.dnc.shared.network.Server;

/**
 * 
 * @author Steffen Bondorf
 */
public class Demo4
{
	public Network network;
	ArrayList<String> serversList = new ArrayList<String>();
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

	public Configuration configuration;
	public Flow flow_of_interest;
	
	public Demo4() throws Exception {
		ServiceCurve service_curve = ServiceCurve.createRateLatency( 10.0e6, 0.01 );
		ServiceCurve max_service_curve = ServiceCurve.createRateLatency( 100.0e6, 0.001 );
		
		network = new Network();	
		configuration = new Configuration();
		
		int numServers = 9;
		Server[] servers = new Server[numServers];
		
		for ( int i = 0; i < numServers; i++ )
		{
			servers[i] = network.addServer( service_curve, max_service_curve );
			servers[i].setUseGamma( false );
			servers[i].setUseExtraGamma( false );
			serversList.add(servers[i].getAlias());
		}
		
		network.addLink( servers[0], servers[2] );
		network.addLink( servers[1], servers[2] );
		Link l_1_3 = network.addLink( servers[1], servers[3] );
		Link l_2_4 = network.addLink( servers[2], servers[4] );
		Link l_3_4 = network.addLink( servers[3], servers[4] );
		Link l_4_5 = network.addLink( servers[4], servers[5] );
		Link l_5_6 = network.addLink( servers[5], servers[6] );
		Link l_6_7 = network.addLink( servers[6], servers[7] );
		Link l_7_8 = network.addLink( servers[7], servers[8] );
		
		ArrivalCurve arrival_curve = ArrivalCurve.createTokenBucket( 0.1e6, 0.1 * 0.1e6 );
		
		LinkedList<Link> path0 = new LinkedList<Link>();
		
//		Links need to be ordered from source server to sink server when defining a path manually
		path0.add(l_2_4);
		path0.add(l_4_5);
		path0.add(l_5_6);
		path0.add(l_6_7);
		path0.add(l_7_8);

		network.addFlow( arrival_curve, path0 );
			
		LinkedList<Link> path1 = new LinkedList<Link>();
		path1.add(l_1_3);
		path1.add(l_3_4);
		path1.add(l_4_5);
		path1.add(l_5_6);
		
		flow_of_interest = network.addFlow( arrival_curve, path1 );
	}

	public ArrayList<String> getServersList() {
		return serversList;
	}

	public void setServersList(ArrayList<String> serversList) {
		this.serversList = serversList;
	}
}
