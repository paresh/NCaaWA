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

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({
		Tandem_1SC_1Flow.class,
		Tandem_2SCs_1Flow.class,
		Tandem_1SC_2Flows_1AC_1Path.class,
		Tandem_2SCs_2Flows_1ACs_1Path.class,
		Tandem_1SC_2Flows_1AC_1Path_v2.class,
		Tandem_1SC_2Flows_1AC_2Paths.class,
		Tandem_1SC_2Flows_1AC_2Paths_v2.class,
		Tandem_1SC_3Flows_1AC_3Paths.class,
		Tandem_1SC_4Flows_1AC_1Path.class
		})
/**
 * 
 * @author Steffen Bondorf
 *
 */
public class TandemTests {

}
