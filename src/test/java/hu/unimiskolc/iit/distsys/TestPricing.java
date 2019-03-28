/*
 *  ========================================================================
 *  dcf-exercises
 *  ========================================================================
 *  
 *  This file is part of dcf-exercises.
 *  
 *  dcf-exercises is free software: you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License as published
 *  by the Free Software Foundation, either version 3 of the License, or (at
 *  your option) any later version.
 *  
 *  dcf-exercises is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along
 *  with dcf-exercises.  If not, see <http://www.gnu.org/licenses/>.
 *  
 *  (C) Copyright 2015, Gabor Kecskemeti (kecskemeti@iit.uni-miskolc.hu)
 */
package hu.unimiskolc.iit.distsys;

import org.junit.Assert;
import org.junit.Test;

import hu.unimiskolc.iit.distsys.competition.SingleMatch;

public class TestPricing {

	@Test(timeout = 60000)
	public void thePricingTest() throws Exception {
		System.out.println("sadf");
		System.out.println("-> "+TestCreatorFactory.getNewProvider());
		SingleMatch match = new SingleMatch(TestCreatorFactory.getNewProvider().getClass(),
				TestCreatorFactory.getDefaultProvider().getClass());
		match.runMatch();
		Assert.assertTrue("The final balance of the provider should be positive!", match.getPointsForTeamOne() > 0);
		Assert.assertTrue(
				"The balance of the new provider should be greater than the balance of the built in provider!",
				match.getPointsForTeamOne() > 1);
	}
}
