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

/**
 * 
 * @author Steffen Bondorf
 *
 */
public class Link implements Serializable {
	private int	id;
	private String alias;
	private Server src;
	private Server dest;

	@SuppressWarnings("unused")
	private Link() {}
	
	protected Link( int id, String alias, Server source, Server destination ) {
		this.id = id;
		this.alias = alias;
		src = source;
		dest = destination;
	}

	public Server getSource() {
		return src;
	}
	
	public Server getDest() {
		return dest;
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

	public boolean equals( Link e ){
		if( e == null || e.getClass() != this.getClass() ) {
			return false;
		}

		return (this.src != null? this.src.equals(e.src) : e.src == null)
				&& (this.dest != null? this.dest.equals(e.dest) : e.dest == null);
	}

	@Override
	public String toString() {
		return "(" + src.toString() + ", " + dest.toString() + ")";
	}
}
