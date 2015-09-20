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
import java.util.LinkedList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Collections;

import unikl.disco.dnc.shared.SetUtils;
import unikl.disco.dnc.shared.curves.ArrivalCurve;
import unikl.disco.dnc.shared.curves.ServiceCurve;

/**
 * 
 * @author Steffen Bondorf
 *
 */
public class Network implements Serializable {
	private HashSet<Server> servers;
	private HashSet<Link> links;
	private HashSet<Flow> flows;
	
	private HashMap<Server,HashSet<Link>> map__server__in_links;
	private HashMap<Server,HashSet<Link>> map__server__out_links;
	
	private HashMap<Server,HashSet<Flow>> map__server__flows;
	private HashMap<Server,HashSet<Flow>> map__server__source_flows;
	
	private HashMap<Link,HashSet<Flow>> map__link__flows;
	
	private String server_default_name_prefix = "s";
	private int server_id_counter = 0;
	private HashMap<Integer,Server> map__id__server;

	private String link_default_name_prefix = "l";
	private int link_id_counter = 0;

	private String flow_default_name_prefix = "f";
	private int flow_id_counter = 0;
	
	public Network() {
		servers = new HashSet<Server>();
		links = new HashSet<Link>();
		flows = new HashSet<Flow>();
		
		map__id__server = new HashMap<Integer,Server>();
		
		map__server__in_links = new HashMap<Server,HashSet<Link>>();
		map__server__out_links = new HashMap<Server,HashSet<Link>>();

		map__server__flows = new HashMap<Server,HashSet<Flow>>();
		map__server__source_flows = new HashMap<Server,HashSet<Flow>>();
		
		map__link__flows = new HashMap<Link,HashSet<Flow>>();
	}
	
	// FIXME GWT error:
	// [ERROR] [ncaawa] Line 97: No source code is available for type java.io.PrintWriter; did you forget to inherit a required module?
//	public void saveAs( String output_path, String file_name ) throws Exception {
//		if ( output_path.charAt( output_path.length() - 1 ) != '/' ) {
//			output_path = output_path + "/";
//		}
//		String file_extension = ".java";
//		if ( file_name.endsWith( file_extension ) ) {
//			file_name = file_name.substring( 0, file_name.length() - 5 );
//		}
//		
//		PrintWriter file = new PrintWriter( new FileWriter( output_path + file_name + file_extension ) );
//		
//		file.println( "/*" );
//		file.println( " * This file was created with the Disco Deterministic Network Calculator (DiscoDNC) v2.0 \"Hydra\"." );
//		file.println( " *" );
//		file.println( " * The DiscoDNC is an open-source tool for deterministic network calculus analysis." );
//		file.println( " * For more information visit http://disco.cs.uni-kl.de/index.php/projects/disco-dnc" );
//		file.println( " *" );
//		file.println( " */" );
//		file.println();
//		
//		file.println( "package unikl.disco;" );
//		file.println();
//		file.println( "import java.util.LinkedList;" );
//		file.println( "import java.util.List;" );
//		file.println();
//		file.println( "import unikl.disco.curves.ServiceCurve;" );
//		file.println( "import unikl.disco.curves.ArrivalCurve;" );
//		file.println();
//		file.println( "import unikl.disco.network.Network;" );
//		file.println( "import unikl.disco.network.Server;" );
//		file.println( "import unikl.disco.network.Link;" );
//		file.println( "import unikl.disco.network.Flow;" );
//		file.println();
//		
//		file.println( "public class " + file_name + "{");
//		file.println( "\t@SuppressWarnings(\"unused\")" );
//		file.println( "\tpublic " + file_name + "() {" );
//		
//		file.println( this.toString() );
//		
//		file.println( "\t}" );
//		file.println( "}" );
//		
//		file.close();
//	}

	private void remove( HashSet<Server> servers_to_remove, HashSet<Link> links_to_remove, HashSet<Flow> flows_to_remove ) {
		// Make sure that you do not remove a map's key before the according entries:
		// (flows before servers and links) & (links before servers)
				
		// prevent ConcurrentModificationException
		HashSet<Flow> flows_to_remove_cpy = new HashSet<Flow>( flows_to_remove );

		for ( Flow f : flows_to_remove_cpy ) {
			flows.remove( f );
			
			for ( Link l : f.getPath().getLinks() )
			{
				map__link__flows.get( l ).remove( f );
			}
			
			for ( Server s : f.getPath().getServers() )
			{
				map__server__flows.get( s ).remove( f );
			}

			map__server__source_flows.get( f.getSource() ).remove( f );
		}

		// prevent ConcurrentModificationException
		HashSet<Link> links_to_remove_cpy = new HashSet<Link>( links_to_remove );
		
		for ( Link l : links_to_remove_cpy ) {
			links.remove( l );
			
			map__link__flows.remove( l );
			map__server__in_links.get( l.getDest() ).remove( l );
			map__server__out_links.get( l.getSource() ).remove( l );
		}

		// prevent ConcurrentModificationException
		HashSet<Server> servers_to_remove_cpy = new HashSet<Server>( servers_to_remove );
		
		for ( Server s : servers_to_remove_cpy ) {
			servers.remove( s );
			
			map__id__server.remove( s.getId() );

			map__server__flows.remove( s );
			
			map__server__in_links.remove( s );
			map__server__out_links.remove( s );
			map__server__source_flows.remove( s );			
		}
	}

	// --------------------------------------------------------------------------------------------
	// Servers
	// --------------------------------------------------------------------------------------------
	public Server addServer( ServiceCurve service_curve ) {
		return addServer( service_curve, ServiceCurve.createZeroDelayBurst(), true, false, false );
	}
	
	public Server addServer( ServiceCurve service_curve, boolean arbitrary_mux ) {
		return addServer( service_curve, ServiceCurve.createZeroDelayBurst(), arbitrary_mux, true, true );
	}
	
	public Server addServer( String alias, ServiceCurve service_curve ) {
		return addServer( alias, service_curve, ServiceCurve.createZeroDelayBurst(), true, false, false );
	}
	
	public Server addServer( String alias, ServiceCurve service_curve, boolean arbitrary_mux ) {
		return addServer( alias, service_curve, ServiceCurve.createZeroDelayBurst(), arbitrary_mux, false, false );
	}
	
	/**
	 * By default the server's use_gamma and use_extra_gamma are enabled
	 * 
	 * @param service_curve
	 * @param max_service_curve
	 * @return the added server
	 */
	public Server addServer( ServiceCurve service_curve, ServiceCurve max_service_curve ) {
		return addServer( service_curve, max_service_curve, true, true, true );
	}
	
	/**
	 * By default the server's use_gamma and use_extra_gamma are enabled
	 * 
	 * @param alias
	 * @param service_curve
	 * @param max_service_curve
	 * @return the added server
	 */
	public Server addServer( String alias, ServiceCurve service_curve, ServiceCurve max_service_curve ) {
		return addServer( alias , service_curve, max_service_curve, true, true, true );
	}

	public Server addServer( ServiceCurve service_curve, ServiceCurve max_service_curve, boolean use_gamma, boolean use_extra_gamma ) {
		return addServer( service_curve, max_service_curve, true, use_gamma, use_extra_gamma );
	}
	
	public Server addServer( String alias, ServiceCurve service_curve, ServiceCurve max_service_curve, boolean arbitrary_mux ) {
		return addServer( alias , service_curve, max_service_curve, arbitrary_mux, true, true );		
	}
	
	public Server addServer( ServiceCurve service_curve, ServiceCurve max_service_curve, boolean arbitrary_mux ) {
		return addServer( service_curve, max_service_curve, arbitrary_mux, true, true );
	}
	
	public Server addServer( String alias, ServiceCurve service_curve, ServiceCurve max_service_curve, boolean use_gamma, boolean use_extra_gamma ) {
		return addServer( alias, service_curve, max_service_curve, true, use_gamma, use_extra_gamma );
	}

	public Server addServer( String alias, ServiceCurve service_curve, boolean arbitrary_mux, boolean use_gamma, boolean use_extra_gamma ) {
		return addServer( alias, service_curve, ServiceCurve.createZeroDelayBurst(), arbitrary_mux, use_gamma, use_extra_gamma );
	}
	
	public Server addServer( ServiceCurve service_curve, ServiceCurve max_service_curve, boolean arbitrary_mux, boolean use_gamma, boolean use_extra_gamma ) {
		String alias = server_default_name_prefix + Integer.toString( server_id_counter );
		return addServer( alias, service_curve, max_service_curve, arbitrary_mux, use_gamma, use_extra_gamma );
	}
	
	public Server addServer( String alias, ServiceCurve service_curve, ServiceCurve max_service_curve, boolean arbitrary_mux, boolean use_gamma, boolean use_extra_gamma ) {
		Server new_server = new Server( server_id_counter, alias, service_curve.copy(), max_service_curve.copy(), arbitrary_mux, use_gamma, use_extra_gamma );
		
		server_id_counter++;
		
		map__server__in_links.put( new_server, new HashSet<Link>() );
		map__server__out_links.put( new_server, new HashSet<Link>() );
		
		map__server__flows.put( new_server, new HashSet<Flow>() );
		map__server__source_flows.put( new_server, new HashSet<Flow>() );
		
		servers.add( new_server );
		
		Integer integer_object = new Integer( server_id_counter );
		map__id__server.put( integer_object, new_server );
		
		return new_server;
	}
	
	public void removeServer( Server s ) throws Exception {
		if ( !servers.contains( s ) ) {
			throw new Exception( "Server to be removed is not in this network's list of servers" );
		}
		
		remove( new HashSet<Server>( Collections.singleton( s ) ), getIncidentLinks( s ), map__server__flows.get( s ) );
	}
	
	public HashSet<Flow> getSourceFlows( Server source ) {
		return new HashSet<Flow>( map__server__source_flows.get( source ) );
	}
	
	public Server getServer( int id ) throws Exception {
		if ( id < 0 || id > map__id__server.size() - 1 ) {
			throw new Exception( "No server with id " + Integer.toString(id) + " found" );
		}

		Server server = map__id__server.get( new Integer( id ) );
		
		if ( server == null ) {
			throw new Exception( "No server with id " + Integer.toString(id) + " found" );
		}
		
		return server;
	}
	
	public HashSet<Server> getServers() {
		return new HashSet<Server>( servers );
	}
	
	public int numServers() {
		return servers.size();
	}

	public int degree( Server s ) {
		return inDegree( s ) + outDegree( s );
	}

	public int inDegree( Server s ) {
		return getInLinks( s ).size();
	}

	public int outDegree( Server s ) {
		return getOutLinks( s ).size();
	}

	/**
	 * Returns a new set consisting of references to the servers.
	 * 
	 * @param s
	 * @return the incoming links of s
	 */
	public HashSet<Link> getInLinks( Server s ) {
		return new HashSet<Link>( map__server__in_links.get( s ) );
	}
	
	/**
	 * Returns a new set consisting of references to the servers.
	 * 
	 * @param s
	 * @return the outgoing links of s
	 */
	public HashSet<Link> getOutLinks( Server s ) {
		return new HashSet<Link>( map__server__out_links.get( s ) );
	}
	
	/**
	 * Returns a new set consisting of references to the links.
	 * 
	 * @param s
	 * @return the incident links
	 */
	public HashSet<Link> getIncidentLinks( Server s ) {
		return SetUtils.getUnion( getInLinks( s ), getOutLinks( s ) );
	}
	
	/**
	 * Returns a new set consisting of references to the servers.
	 * 
	 * @param s
	 * @return the neighboring servers of s
	 */
	public HashSet<Server> getNeighbors( Server s ) {
		return SetUtils.getUnion( getSuccessors( s ), getPredecessors( s ) );
	}
	
	/**
	 * Returns a new set consisting of references to the servers.
	 * 
	 * @param s
	 * @return the source servers of incoming links of s
	 */
	public HashSet<Server> getPredecessors( Server s ) {		
		HashSet<Server> predecessors = new HashSet<Server>();
		for( Link l : getInLinks( s ) ) {
			predecessors.add( l.getSource() );
		}
		return predecessors;
	}
	
	/**
	 * Returns a new set consisting of references to the servers.
	 * 
	 * @param s
	 * @return the sink servers of outgoing links of s
	 */
	public HashSet<Server> getSuccessors( Server s ) {
		HashSet<Server> successors = new HashSet<Server>();
		for( Link l : getOutLinks( s ) ) {
			successors.add( l.getDest() );
		}
		return successors;
	}

	// --------------------------------------------------------------------------------------------
	// Links
	// --------------------------------------------------------------------------------------------
	public Link addLink( Server source, Server destination ) throws Exception {
		String alias = link_default_name_prefix + Integer.toString( link_id_counter );
		return addLink( alias, source, destination ); 
	}
	
	public Link addLink( String alias, Server source, Server destination ) throws Exception {
		if( !servers.contains( source ) ){
			throw new Exception( "link's source not present in the network" );
		}
		if ( !servers.contains( destination ) ) {
			throw new Exception( "link's destination not present in the network" );
		}
		
		try {
			// This implicitly signals the caller that the link was already present in the network 
			// by returning a link with a name different to the given one
			return findLink( source, destination );
		} catch (Exception e) {
			Link new_link = new Link( link_id_counter, alias, source, destination );
			link_id_counter++;
			
			map__link__flows.put( new_link, new HashSet<Flow>() );
			
			map__server__in_links.get( destination ).add( new_link );
			map__server__out_links.get( source ).add( new_link );
			
			links.add( new_link );
			return new_link;
		}
	}
	
	public void removeLink( Link l ) throws Exception {
		if( !links.contains( l ) ){
			throw new Exception( "Link to be removed is not in this network's list of links" );
		}		

		remove( new HashSet<Server>(), new HashSet<Link>( Collections.singleton( l ) ), map__link__flows.get( l ) );
	}

	public HashSet<Link> getLinks() {
		return new HashSet<Link>( links );
	}

	/**
	 * 
	 * @param src
	 * @param dest
	 * @return the link from src to dest 
	 */
	public Link findLink( Server src, Server dest ) throws Exception {
		HashSet<Link> connecting_link_as_set = SetUtils.getIntersection( getInLinks( dest ), getOutLinks( src ) );
		if ( connecting_link_as_set.isEmpty() ) {
			throw new Exception ( "No link between " + src.toString() + " and " + dest.toString() + " found." );
		}else{
			if ( connecting_link_as_set.size() > 1 ) {
				throw new Exception ( "Too many links between " + src.toString() + " and " + dest.toString() + " found." );
			} else {
				return connecting_link_as_set.iterator().next();
			}
		}
	}
	
	// ---------------------------------------------------------------------------------------------
	// Flows
	//
	// INFO: It is not allowed to change the path of a flow,
	// 		 i.e., the user needs to remove the old flow and insert a new one with the modified path
	// ---------------------------------------------------------------------------------------------
	@SuppressWarnings("rawtypes")
	public Flow addFlow( ArrivalCurve arrival_curve, LinkedList path ) throws Exception {
		String alias = flow_default_name_prefix + Integer.toString( flow_id_counter );
		return addFlow( alias, arrival_curve, path );
	}
	
	/**
	 * Creates a flow and adds it to the network.
	 * 
	 * @param arrival_curve
	 * @param source
	 * @param sink
	 * @return the flow
	 * @throws Exception
	 */
	public Flow addFlow( ArrivalCurve arrival_curve, Server source, Server sink ) throws Exception {
		String alias = flow_default_name_prefix + Integer.toString( flow_id_counter );
		return addFlow( alias, arrival_curve, source, sink );
	}
	
	/**
	 * Creates a flow and adds it to the network.
	 * 
	 * @param alias
	 * @param arrival_curve
	 * @param source
	 * @param sink
	 * @return the flow
	 * @throws Exception
	 */
	public Flow addFlow( String alias, ArrivalCurve arrival_curve, Server source, Server sink ) throws Exception {
		if( source == sink ) {
			return addFlow( arrival_curve, sink );
		}
		
		return addFlowToNetwork( alias, arrival_curve, getShortestPath( source, sink ) );
	}

	/**
	 * Creates a flow and adds it to the network.
	 * 
	 * @param alias
	 * @param arrival_curve
	 * @param path
	 * @return the flow
	 * @throws Exception
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Flow addFlow( String alias, ArrivalCurve arrival_curve, LinkedList path ) throws Exception {
		if( path == null || path.isEmpty() ) {
			throw new Exception( "The path of a flow cannot be empty" );
		}
		
		Object element_of_path = path.get( 0 );

		if( element_of_path instanceof Server ) {
			return addFlowToNetwork( alias, arrival_curve, createPathFromServers( path ) );
		}
		if( element_of_path instanceof Link ) {
			return addFlowToNetwork( alias, arrival_curve, createPathFromLinks( path ) );
		}
		
		throw new Exception( "Could not create the path for flow " + alias );
	}

	/**
	 * Creates a flow and adds it to the network.
	 * 
	 * @param arrival_curve
	 * @param single_hop
	 * @return the flow
	 * @throws Exception
	 */
	public Flow addFlow( ArrivalCurve arrival_curve, Server single_hop ) throws Exception {		
		String alias = flow_default_name_prefix + Integer.toString( flow_id_counter );
		return addFlow( alias, arrival_curve, single_hop );
	}
	
	/**
	 * Creates a flow and adds it to the network.
	 * 
	 * @param alias
	 * @param arrival_curve
	 * @param single_hop
	 * @return the flow
	 * @throws Exception
	 */
	public Flow addFlow( String alias, ArrivalCurve arrival_curve, Server single_hop ) throws Exception {
		return addFlowToNetwork( alias, arrival_curve, createPath( single_hop ) );
	}
	
	// Needed to be named differently due to a collision of the method's signature with a user visible one's
	private Flow addFlowToNetwork( String alias, ArrivalCurve arrival_curve, Path path ) throws Exception {
//		if ( !servers.containsAll( path.getServers() ) ) {
//			throw new Exception( "Some servers on the given flow's path are not present in the network" );
//		}
//		if ( !path.getLinks().isEmpty() ) {
//			if ( !links.containsAll( path.getLinks() ) ) {
//				throw new Exception( "Some links on the given flow's path are not present in the network" );
//			}
//		}

		Flow new_flow = new Flow( flow_id_counter, alias, arrival_curve.copy(), path );
		flow_id_counter++;
		
		flows.add( new_flow );
		map__server__source_flows.get( path.getSource() ).add( new_flow );
		
		for ( Link l : path.getLinks() )
		{
			map__link__flows.get( l ).add( new_flow );
		}
		for ( Server s : path.getServers() )
		{
			map__server__flows.get( s ).add( new_flow );
		}
		
		return new_flow;
	}
	
	/**
	 * Removes a flow from the network.
	 * 
	 * @param f the flow to be removed
	 */
	public void removeFlow( Flow f ) throws Exception {
		if ( !flows.contains( f ) ) {
			throw new Exception( "Flow to be removed is not in this network's list of flows" );
		}
		
		remove( new HashSet<Server>(), new HashSet<Link>(), new HashSet<Flow>( Collections.singleton( f ) ) );
	}
	
	public HashSet<Flow> getFlows() {
		return new HashSet<Flow>( flows );
	}
	
	public HashSet<Flow> getFlows( Link l ) {
		return new HashSet<Flow>( map__link__flows.get( l ) );
	}

	public HashSet<Flow> getFlows( HashSet<Link> links ) {
		HashSet<Flow> flows = new HashSet<Flow>();
		
		for ( Link l : links ) {
			flows.addAll( map__link__flows.get( l ) );
		}
		
		return flows;
	}
	
	public HashSet<Flow> getFlows( Server s ) {
		return new HashSet<Flow>( map__server__flows.get( s ) );
	}

	public HashSet<Flow> getFlows( Path p ) throws Exception {
		HashSet<Flow> result = new HashSet<Flow>();
		for ( HashSet<Flow> flows : getFlowsPerServer( p, new HashSet<Flow>() ).values() ) {
			result.addAll( flows );
		}
		return result;
	}

	/**
	 * Do not confuse with getServerJoiningFlowsMap
	 * 
	 */
	public HashMap<Server,HashSet<Flow>> getFlowsPerServer( Path p, HashSet<Flow> excluded_flows ) throws Exception {
		HashMap<Server,HashSet<Flow>> map__server__set_flows = new HashMap<Server,HashSet<Flow>>();
		
		HashSet<Flow> set_set_flows;
		for ( Server s : p.getServers() ) {
			set_set_flows = getFlows( s ); // No need to create another new instance of HashMap
			set_set_flows.removeAll( excluded_flows );
			map__server__set_flows.put( s, set_set_flows );
		}
		return map__server__set_flows;
	}
	
	/**
	 * For every distinct sub-path of p
	 * this method returns the flows taking it entirely.
	 * 
	 */
	public HashMap<Path,HashSet<Flow>> getFlowsPerSubPath( Path p ) throws Exception {
		return getFlowsPerSubPath( p, new HashSet<Flow>() );
	}
	
	/**
	 * For every distinct sub-path of p
	 * this method returns the flows taking it entirely.
	 * 
	 */
	public HashMap<Path,HashSet<Flow>> getFlowsPerSubPath( Path p, HashSet<Flow> excluded_flows ) throws Exception {
		HashMap<Path,HashSet<Flow>> map__path__set_flows = new HashMap<Path,HashSet<Flow>>();
				
		HashMap<Server,HashSet<Flow>> map__s_i__joining_flows = getServerJoiningFlowsMap( p, excluded_flows );
		HashSet<Flow> all_interfering_flows = new HashSet<Flow>();
		for ( Server s : map__s_i__joining_flows.keySet() ) {
			all_interfering_flows.addAll( map__s_i__joining_flows.get( s ) );
		}
		all_interfering_flows.removeAll( excluded_flows ); // Should have been done by the map__server__joining_flows construction
		if ( all_interfering_flows.isEmpty() ) { // If there are no interfering flows to be considered, return the convolution of a ll service curves on the path
			return map__path__set_flows;
		}
		
		// Iterate over the servers s on the path. Use indices to easily determine egress servers 
		LinkedList<Server> servers = p.getServers(); 
		int n = servers.size();
		
		// Flows with the same egress server (independent of the out link) can be aggregated for PMOO's and OBA's arrival bound calculation.
		// This still preserves the demultiplexing considerations. Note that the last server contains all remaining flows by default.
		HashMap<Server,HashSet<Flow>> map__server__leaving_flows = getServerLeavingFlowsMap( p );
		
		for( int i = 0; i<n; i++ ) {
			Server s_i = servers.get( i );
			
			HashSet<Flow> s_i_ingress = map__s_i__joining_flows.get( s_i );
			if ( s_i_ingress.isEmpty() ) {
				continue;
			}

	 		for( int j = i; j<n; j++ ) {
	 			Server s_j_egress = servers.get( j );
	 			
	 			HashSet<Flow> s_i_ingress__s_j_egress = SetUtils.getIntersection( s_i_ingress, map__server__leaving_flows.get( s_j_egress ) ); // Intersection with the remaining joining_flows prevents rejoining flows to be considered multiple times
	 			s_i_ingress__s_j_egress.removeAll( excluded_flows );
	 			
	 			if ( s_i_ingress__s_j_egress.isEmpty() ) { // No such flows to bound
	 				continue;
	 			}

		 		map__path__set_flows.put( p.getSubPath(s_i, s_j_egress), s_i_ingress__s_j_egress );
				
				// Remove the flows otherwise rejoining flows will occur multiple times with wrong paths
				s_i_ingress.removeAll( s_i_ingress__s_j_egress );
			}
		}
		return map__path__set_flows;
	}
	
	/**
	 * Returns an aggregate arrival curve for all flows originating in <code>source</code>.
	 * 
	 * Returns a null curve if there are no source flows at <code>source</code>.
	 * 
	 * @param source
	 *            the source of all flows to be aggregated
	 * @return an aggregate arrival curve
	 */
	public ArrivalCurve getSourceFlowArrivalCurve( Server source )
	{
		return getSourceFlowArrivalCurve( source, getSourceFlows( source ) );
	}
	
	/**
	 * Returns an aggregate arrival curve for all flows originating in
	 * <code>source</code> and contained in the set <code>outgoing_flows</code>.
	 * 
	 * Returns a null curve if the intersection of those sets of curves is empty.
	 * 
	 * @param source
	 *            the source of all flows to be aggregated
	 * @param outgoing_flows
	 *            the set of all flows to be aggregated
	 * @return an aggregate arrival curve
	 */
	public ArrivalCurve getSourceFlowArrivalCurve( Server source, HashSet<Flow> outgoing_flows )
	{
		ArrivalCurve a_out = ArrivalCurve.createNullArrival();
		
		// Returns an empty set if one of the arguments is null
		HashSet<Flow> source_flows = SetUtils.getIntersection( map__server__source_flows.get( source ), outgoing_flows );
		if ( source_flows.isEmpty() ) {
			return a_out;
		} else {
			if ( source_flows != null ) {
				for ( Flow f : source_flows ) 
				{
					a_out = ArrivalCurve.add( a_out, f.getArrivalCurve() );
				}
			}			
		}
		return a_out;
	}

	// --------------------------------------------------------------------------------------------
	// Paths
	// --------------------------------------------------------------------------------------------
	public Path createPath( Server server ) throws Exception {
		if ( !servers.contains( server ) ) {
			throw new Exception( "Server to create path from is not in the network");
		}
		
		return new Path( new LinkedList<Server>( Collections.singleton( server ) ), new LinkedList<Link>() );
	}
	
	public Path createPath( Link link ) throws Exception {
		if ( !links.contains( link ) ) { // Implicitly contains the case that at least one server the link connects is not in the network
			throw new Exception( "Link to create path from is not in the network" );
		}
		
		LinkedList<Server> path_servers = new LinkedList<Server>();
		path_servers.add( link.getSource() );
		path_servers.add( link.getDest() );
		
		return new Path( path_servers, new LinkedList<Link>( Collections.singleton( link ) ) );
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Path createPath( LinkedList path ) throws Exception {
		if( path == null || path.isEmpty() ) {
			throw new Exception( "The path of a flow cannot be empty" );
		}
		
		Object element_of_path = path.get( 0 );

		if( element_of_path instanceof Server ) {
			return createPathFromServers( path );
		}
		if( element_of_path instanceof Link ) {
			return createPathFromLinks( path );
		}
		
		throw new Exception( "Could not create path" );
	}
	
	private Path createPathFromServers( LinkedList<Server> path_servers ) throws Exception {
		LinkedList<Link> path_links = new LinkedList<Link>();
		
		// Sanity check + path_links construction:
		if( path_servers.size() > 1 ) {
			for ( int i = 0; i < path_servers.size()-1; i++ ) {
				try{
					path_links.add( findLink( path_servers.get( i ), path_servers.get( i+1 ) ) );
				} catch (Exception e) {
					throw new Exception( "At least two consecutive servers to create the path from are not connected by a link in the network" );
				}
			}			
		}
		
		return createPath( path_servers, path_links );
	}
	
	private Path createPathFromLinks( LinkedList<Link> path_links ) throws Exception {
		LinkedList<Server> path_servers = new LinkedList<Server>();
		
		// Sanity checks + path_servers construction:
		for( int i = 0; i < path_links.size()-1; i++ ) {
			Link l_i = path_links.get( i );
			if ( !links.contains( l_i ) ) {
				throw new Exception( "At least one link to create path from is not in the network" );
			}
			if ( l_i.getDest() != path_links.get( i+1 ).getSource() ) {
				throw new Exception( "At least two consecutive links to create the path from are not connected" );
			}
			path_servers.add( l_i.getSource() );
		}
		Link l_last = path_links.get( path_links.size()-1 );
		if ( !links.contains( l_last ) ) {
			throw new Exception( "Last link to create path from is not in the network" );
		}
		path_servers.add( l_last.getSource() );
		path_servers.add( l_last.getDest() );
		
		return new Path( path_servers, path_links );
	}
	
	public Path createPath( LinkedList<Server> path_servers, LinkedList<Link> path_links ) throws Exception {
		// Sanity checks:
		for( int i = 0; i < path_links.size()-1; i++ ) {
			Link l_i = path_links.get( i );
			
			if ( !links.contains( l_i ) ) { // Implicitly contains the case that at least one server the link connects is not in the network
				throw new Exception( "At least one link to create path from is not in the network" );
			}
			if ( l_i.getDest() != path_links.get( i+1 ).getSource() ) {
				throw new Exception( "At least two consecutive links to create the path from are not connected" );
			}
			
			if ( path_servers.get( i ) != l_i.getSource() || path_servers.get( i+1 ) != l_i.getDest() ) {
				throw new Exception( "At least two consecutive servers to create the path from are not connected by the corresponding link of the given list" );
			}
		}
		Link l_last = path_links.get( path_links.size()-1 );
		if ( !links.contains( l_last ) ) { // Implicitly contains the case that at least one server the link connects is not in the network
			throw new Exception( "Last link to create path from is not in the network" );
		}
		
		return new Path( path_servers, path_links );
	}
	
	/**
	 * 
	 * @param path
	 * @param flows_to_join null will be handled as empty setf flows
	 * @return
	 * @throws Exception
	 */
	public HashMap<Server,HashSet<Flow>> getServerJoiningFlowsMap( Path path, HashSet<Flow> flows_to_join ) throws Exception {
		if ( flows_to_join == null ) {
			flows_to_join = new HashSet<Flow>();
		}
		
		HashMap<Server,HashSet<Flow>> map__server__joining_flows = new HashMap<Server,HashSet<Flow>>();
		
		Server path_source = path.getSource();
		LinkedList<Server> servers_iteration = path.getServers();
		servers_iteration.remove( path_source );
		
		// Default for first server
		HashSet<Flow> flows_joining = getFlows( path_source );
		flows_joining.removeAll( flows_to_join );
		map__server__joining_flows.put( path_source, flows_joining );
		
		for ( Server s : servers_iteration ) {
			flows_joining = SetUtils.getDifference( getFlows( s ), getFlows( path.getPrecedingLink( s ) ) );
			flows_joining.removeAll( flows_to_join );
			
			// Results in an empty set if there a no joining flow at server s
			map__server__joining_flows.put( s, flows_joining );
		}

		return map__server__joining_flows;
	}
	
	// All flows that leave the path at the server
	public HashMap<Server,HashSet<Flow>> getServerLeavingFlowsMap( Path path ) throws Exception {
		HashMap<Server,HashSet<Flow>> map__server__leaving_flows = new HashMap<Server,HashSet<Flow>>();
		
		Server path_sink = path.getSink();
		LinkedList<Server> servers_iteration = path.getServers();
		servers_iteration.remove( path_sink );
		
		for ( Server s : servers_iteration ) {
			// Results in an empty set if there a no joining flow at server s
			map__server__leaving_flows.put( s, SetUtils.getDifference( getFlows( s ), getFlows( path.getSucceedingLink( s ) ) ) );
		}
		
		// Default for last server
		map__server__leaving_flows.put( path_sink, getFlows( path_sink ) );
		
		return map__server__leaving_flows;
	}
 
	/**
	 * Calculates the shortest path between src and snk
	 * according to Dijkstra's algorithm
	 * 
	 * @param src
	 * @param snk
	 * @return Dijkstra shortest path
	 * @throws Exception 
	 */
    public Path getShortestPath( Server src, Server snk ) throws Exception {
    	HashSet<Server> visited = new HashSet<Server>();

    	HashMap<Server,LinkedList<Link>> paths_links = new HashMap<Server,LinkedList<Link>>();
    	HashMap<Server,LinkedList<Server>> paths_servers = new HashMap<Server,LinkedList<Server>>();

		paths_links.put( src, new LinkedList<Link>() );
		paths_servers.put( src, new LinkedList<Server>( Collections.singleton( src ) ) );
		
		LinkedList<Server> queue = new LinkedList<Server>();
		queue.add( src );
		visited.add( src );
		
		while ( !queue.isEmpty() ) {
			Server s = queue.getLast();
			queue.remove( s );
			
			HashSet<Server> successors_s = getSuccessors( s ); 
			for( Server successor : successors_s ) {

				LinkedList<Link> path_links_tmp = new LinkedList<Link>( paths_links.get( s ) );
				
				LinkedList<Server> path_servers_tmp;
				if ( paths_servers.containsKey( s ) ) {
					path_servers_tmp = new LinkedList<Server>( paths_servers.get( s ) );
				} else {
					path_servers_tmp = new LinkedList<Server>( Collections.singleton( src ) );
				}

				path_links_tmp.add( findLink( s, successor ) );
				path_servers_tmp.add( successor );
				
				if ( !visited.contains( successor ) ) {
					paths_links.put( successor, path_links_tmp );
					paths_servers.put( successor, path_servers_tmp );
					
					queue.add( successor );
					visited.add( successor );
				} else {
					if( paths_links.get( successor ).size() > path_links_tmp.size() ) {
						paths_links.put( successor, path_links_tmp );
						paths_servers.put( successor, path_servers_tmp );
						
						queue.add( successor );
					}
				}				
			}
		}
		
		if ( paths_links.get( snk ) == null ) {
			throw new Exception( "No path from server " + src.getId() + " to server " + snk.getId() + " found" );
		}
		
		// No sanity checks needed after a shortest path calculation, so you can create a new path directly instead of calling 'createPath'
		return new Path( paths_servers.get( snk ), paths_links.get( snk ) );
	}
 	
 	/**
 	 * Returns the server at which the flows in <code>flows_of_interest</code>
 	 * first all meet each other (when viewed from the source). When viewed from
 	 * <code>server</code> towards the sink, this is the last server where all
 	 * flows are still together.
 	 * 
 	 * @param server_common_dest
 	 * @param flows_of_interest
 	 * @return the splitting point server
 	 */
 	public Server findSplittingServer( Server server_common_dest, HashSet<Flow> flows_of_interest ) throws Exception {
 		Flow f = flows_of_interest.iterator().next();
 		
 		if ( flows_of_interest.size() == 1 ) {
 			return f.getSource();
 		}
 		
 		Path f_path = f.getPath();
 		int common_dest_index_f = f_path.getServers().indexOf( server_common_dest );
 		
 		Server split = server_common_dest;
 		// Iterate in reverse order starting from server_common_dest, stop as soon as at least one of the flows of interest is missing
 		for( int i = common_dest_index_f - 1; i >= 0; i-- ) { // -1 excludes server_common_dest
 			Server split_candidate = f_path.getServers().get( i );
 			
 			if ( getFlows( split_candidate ).containsAll( flows_of_interest ) ) {
 				split = split_candidate;
 			} else {
 				break;
 			}
 		}
 		
 		if ( split == server_common_dest ) { // No splitting point found
 	 		throw new Exception( "Could not find splitting point" );
 		} else {
 			return split;
 		}
 	}

	// --------------------------------------------------------------------------------------------
	// Other helper functions
	// --------------------------------------------------------------------------------------------
	
	@Override
	public String toString() {
		String network_str = "";
		
		network_str += "\n" + "\t\tNetwork network = new Network();";
		network_str += "\n" + "";
		network_str += "\n" + "\t\ttry {";

		network_str += "\n" + "\t\t\tServiceCurve service_curve;";
		network_str += "\n" + "\t\t\tServiceCurve max_service_curve;";
		for ( Server s : servers ) {
			network_str += "\n";
			
			network_str += "\n" + "\t\t\tservice_curve = new ServiceCurve( \"" + s.getServiceCurve().toString() + "\" );";
			network_str += "\n" + "\t\t\tmax_service_curve = new ServiceCurve( \"" + s.getMaxServiceCurve().toString() + "\" );";
			
			network_str += "\n" + "\t\t\tServer " + s.getAlias() + " = network.addServer( " 
						+ "\"" + s.getAlias() + "\"" + ", "
						+ "service_curve, " 
						+ "max_service_curve, "
						+ s.useArbitraryMultiplexing() + ", "
						+ s.useGamma() + ", "
						+ s.useExtraGamma() + " );";
		}

		network_str += "\n";
		network_str += "\n";
		
		for ( Link l : links ) {
			network_str += "\n" + "\t\t\tLink " + l.getAlias() + " = network.addLink( " 
					 	+ "\"" + l.getAlias() + "\", "
					 	+ l.getSource().getAlias() + ", " 
					 	+ l.getDest().getAlias() + " );";
		}

		network_str += "\n";
		network_str += "\n";

		network_str += "\n" + "\t\t\tArrivalCurve arrival_curve;";
		network_str += "\n" + "\t\t\tList<Server> servers_on_path_s = new LinkedList<Server>();";
		for ( Flow f : flows ) {
			network_str += "\n";

			network_str += "\n" + "\t\t\tarrival_curve = new ArrivalCurve( \"" + f.getArrivalCurve().toString() + "\" );";
			
			for ( Server s : f.getServersOnPath() ) {
				network_str += "\n" + "\t\t\tservers_on_path_s.add( " 
							+ s.getAlias()
							+ " );";
			}

			network_str += "\n" + "\t\t\tFlow " + f.getAlias() + " = network.addFlow( "
							+ "\"" + f.getAlias() + "\"" + ", "
							+ "arrival_curve, "
							+ "servers_on_path_s );";
			network_str += "\n" + "\t\t\tservers_on_path_s.clear();";
		}

		network_str += "\n";
		network_str += "\n" + "\t\t} catch (Exception e) {";
		network_str += "\n" + "\t\t\tSystem.out.println( e.toString() );";
		network_str += "\n" + "\t\t}";
		
		return network_str;
	}
}
