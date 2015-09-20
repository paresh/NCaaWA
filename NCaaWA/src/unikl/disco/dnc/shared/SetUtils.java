/*
 * This file is part of the Disco Deterministic Network Calculator v2.0.4 "Hydra".
 *
 * Copyright (C) 2005, 2006 Frank A. Zdarsky
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

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A tiny collection of convenience methods useful in dealing with sets but not
 * provided directly by Sun's set classes.
 * 
 * @author Frank A. Zdarsky
 */
public class SetUtils {
	/**
	 * Returns the set difference between the set <code>s1</code> and the set <code>s2</code>.
	 * 
	 * @param s1
	 * @param s2
	 * @return the difference set
	 */
    public static <T> HashSet<T> getDifference(Set<T> s1, Set<T> s2) {
    	HashSet<T> result = new HashSet<T>(s1);
    	result.removeAll(s2);
    	return result;
    }
    
    /**
     * Returns the intersection of set <code>s1</code> and set <code>s2</code>.
     * 
     * @param s1 a set
     * @param s2 another set
     * @return the intersection set
     */
    public static <T> HashSet<T> getIntersection(Set<T> s1, Set<T> s2) {
		if(s1==null || s2==null)
			return new HashSet<T>();

		HashSet<T> result = new HashSet<T>(s1);
    	result.retainAll(s2);
    	return result;
    }
    
    /**
     * Returns the intersection of all sets contained in the list <code>sets</code>.
     * 
     * @param sets a list of sets
     * @return the intersection of all sets
     */
    public static <T> HashSet<T> getIntersection(List<Set<T>> sets) {
    	HashSet<T> result = new HashSet<T>();    	
    	Iterator<Set<T>> iter = sets.iterator();
    	if (iter.hasNext()) {
    		result.addAll(iter.next());
        	for (; iter.hasNext(); ) {
        		result.retainAll((Set<T>) iter.next());
        	}    		
    	}
    	return result;
    }

    /**
     * Returns the union of set <code>s1</code> and set <code>s2</code>.
     * 
     * @param s1 a set
     * @param s2 another set
     * @return the union set
     */
    public static <T> HashSet<T> getUnion(Set<T> s1, Set<T> s2) {
    	HashSet<T> result = new HashSet<T>(s1);
   		result.addAll(s2);
    	return result;
    }
    
    /**
     * Returns the union of all sets contained in the list <code>sets</code>.
     * 
     * @param sets a list of sets
     * @return the union of all sets
     */
    public static <T> HashSet<T> getUnion(List<Set<T>> sets) {
    	HashSet<T> result = new HashSet<T>();
    	for(Set<T> set : sets) {
    		result.addAll(set);
    	}
    	return result;
    }
}
