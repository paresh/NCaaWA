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

package unikl.disco.dnc.shared.network;

import java.io.Serializable;
import java.util.LinkedList;

import unikl.disco.dnc.shared.curves.ArrivalCurve;

/**
 * Class representing flows through the network.
 *
 * @author Frank A. Zdarsky
 * @author Steffen Bondorf
 */
public class Flow implements Serializable {
	public static Flow NULL_FLOW = createDummyFlow( "null", ArrivalCurve.createNullArrival(), Path.createNullPath() ); 
//	public static final Flow NULL_FLOW = createDummyFlow( "null", ArrivalCurve.createNullArrival(), Path.createNullPath() ); 
	
	/** The flow's ID. */
	private int	id;
	/** The flow's arrival curve */
	private ArrivalCurve arrival_curve;
	
	private String alias;
	/** The link path the flow traverses incl explicit sink */
	private Path path;
	
	private Flow() {
		this.id     = -1;
		this.arrival_curve     = null;
		this.path   = null;
	}
	
	/**
	 * Creates a dummy flow with an arrival curve.
	 * @return a dummy flow
	 */
	public static Flow createDummyFlow( String alias, ArrivalCurve ac, Path path ) {
		Flow result = new Flow();
		result.alias = alias;
		result.arrival_curve = ac;
		result.path = path;
		return result;
	}
	
	/**
	 * @param source The server at which the flow originates
	 * @param sink The server at which the flow ends
	 * @param ac The flow's arrival curve
	 * @param path The link path the flow traverses
	 */
	protected Flow( int id, String alias, ArrivalCurve ac, Path path ) throws Exception {
		this.id = id;
		this.alias = alias;
		this.arrival_curve = ac;
		this.path   = path;
	}
	
	public boolean setArrivalCurve( ArrivalCurve arrival_curve ) {
		this.arrival_curve = arrival_curve;
		return true;
	}

	/**
	 * 
	 * @return A copy of the arrival curve
	 */
	public ArrivalCurve getArrivalCurve() {
		return arrival_curve.copy();
	}
	
	public int getId() {
		return id;
	}
	
	public String getAlias() {
		return alias;
	}
	
	public void setAlias( String alias ) {
		this.alias = alias;
	}

	public Path getPath() {
		return path;
	}

	/**
	 * @return A string representation of the flow
	 */
	@Override
	public String toString() {
		return "(" + alias + "," + path.toString() + "," + arrival_curve.toString() + ")";
	}

	// --------------------------------------------------------------------------------------------
	// Shortcuts to conveniently access the path's according methods  
	// --------------------------------------------------------------------------------------------
	public Server getSource() {
		return path.getSource();
	}
	
	public Server getSink() {
		return path.getSink();
	}

    public LinkedList<Server> getServersOnPath() {
    	return new LinkedList<Server>( path.getServers() );
    }

    public LinkedList<Link> getLinksOnPath() throws Exception {
    	return new LinkedList<Link>( path.getLinks() );
    }
	
    /**
     * @param from inclusive
     * @param to inclusive
     */
    public Path getSubPath( Server from, Server to ) throws Exception {
    	return path.getSubPath( from, to );
    }
    
	public Link getPrecedingLink( Server s ) throws Exception {
		return path.getPrecedingLink( s );
	}
	
 	public Link getSucceedingLink( Server s ) throws Exception {
 		return path.getSucceedingLink( s );
 	}

 	public Server getPrecedingServer( Server s ) throws Exception {
 		return path.getPrecedingServer( s );
 	}

 	public Server getSucceedingServer( Server s ) throws Exception {
 		return path.getSucceedingServer( s );
 	}
}