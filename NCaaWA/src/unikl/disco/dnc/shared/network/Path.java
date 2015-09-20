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

package unikl.disco.dnc.shared.network;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;

/**
 * 
 * @author Steffen Bondorf
 *
 */
public class Path implements Serializable {
	private LinkedList<Server> path_servers = new LinkedList<Server>();
	private LinkedList<Link> path_links = new LinkedList<Link>();

	public static Path createNullPath() {
		return new Path();
	}
	
	private Path() {};

	protected Path( LinkedList<Server> path_servers, LinkedList<Link> path_links ) {
		// Sanity check should have been done by the network
		this.path_servers = new LinkedList<Server>( path_servers );
		this.path_links = new LinkedList<Link>( path_links );
	}
	
	protected Path( Path path ) {
		this.path_servers = path.getServers();
		this.path_links = path.getLinks();
	}

	// Can be visible.
	// There's no way to create a single hop path not possible to take in a network.
	public Path( Server single_hop ) {
		this.path_servers.add( single_hop );
	}
	
	public Server getSource(){
		return path_servers.get( 0 );
	}
	
	public Server getSink(){
		return path_servers.get( path_servers.size()-1 );
	}
	
	public int numServers(){
		return path_servers.size();
	}
	
	public int numLinks(){
		return path_links.size();
	}
	
	public LinkedList<Link> getLinks() {
		return new LinkedList<Link>( path_links );
	}
	
	public LinkedList<Server> getServers() {
		return new LinkedList<Server>( path_servers );
	}
	
    /**
     * @param from inclusive
     * @param to inclusive
     */
    public Path getSubPath( Server from, Server to ) throws Exception {
    	// All other sanity check should have been passed when this object was created
    	if( !path_servers.contains( from ) ) {
    		throw new Exception( "Source of the path servers are collected for is not crossed by this flow" );
    	}
    	if( !path_servers.contains( to ) ) {
    		throw new Exception( "Sink of the path servers are collected for is not crossed by this flow" );
    	}

    	if( from == to ) {
    		return new Path( new LinkedList<Server>( Collections.singleton( from ) ) , new LinkedList<Link>() );
    	}
    	
    	int from_index = path_servers.indexOf( from );
    	int to_index = path_servers.indexOf( to );
    	if ( from_index >= to_index ) {
    		throw new Exception( "Cannot create sub-path from " + from.toString() + " to " + to.toString() );
    	}
																	// subList: 'from' is inclusive but 'to' is exclusive
    	LinkedList<Server> subpath_servers = new LinkedList<Server>( path_servers.subList( from_index, to_index ) );
    	subpath_servers.add( to );
    	
    	LinkedList<Link> subpath_links = new LinkedList<Link>();
    	if ( subpath_servers.size() > 1 ) {
        	for ( Link l : path_links ) {
        		Server src_l = l.getSource();
        		Server snk_l = l.getDest();
        		if ( subpath_servers.contains( src_l ) && subpath_servers.contains( snk_l ) ) {
        			subpath_links.add( l );
        		}
        	}
    	}

    	return new Path( subpath_servers, subpath_links );
	}

 	public Link getPrecedingLink( Server s ) throws Exception {
 		for( Link l: path_links ) {
 			if ( l.getDest().equals( s ) ) {
 				return l;
 			}
 		}
 		throw new Exception( "No preceding link on the path found" );	
 	}

 	public Link getSucceedingLink( Server s ) throws Exception {
 		for( Link l: path_links ) {
 			if ( l.getSource().equals( s ) ) {
 				return l;
 			}
 		}
 		throw new Exception( "No succeeding link on the path found" );	
 	}

 	public Server getPrecedingServer( Server s ) throws Exception {
 		try{
 			return getPrecedingLink( s ).getSource();
 		} catch ( Exception e ) {
 			throw new Exception( "No preceding server on the path found" );
 		}
 	}

 	public Server getSucceedingServer( Server s ) throws Exception {
 		try{
 			return getSucceedingLink( s ).getDest();
 		} catch ( Exception e ) {
 			throw new Exception( "No succeeding server on the path found" );
 		}
 	}
 	
	@Override
	public String toString() {
		if ( path_links.isEmpty() && path_servers.isEmpty() ) {
			return "{}";
		}
		
		String str = "{";
		
		if ( path_links.isEmpty() && !path_servers.isEmpty() ) {
			return str = str.concat( "(" + path_servers.getFirst().toString() + ")}" );
		} else {
			for( Link l : path_links ) {
				str = str.concat( l.toString() + "," );
			}
			str = str.substring( 0, str.length()-1 );
			return str.concat( "}" );
		}
	}
}
