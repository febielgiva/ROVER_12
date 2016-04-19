package testUtillities;

import static org.junit.Assert.*;

import org.junit.Test;

import swarmBots.ROVER_12_Kae;

public class KS_test {

	@Test
	public void testRandomNumGenStr() {

		ROVER_12_Kae ks = new ROVER_12_Kae();

		for (int i = 0; i < 20; i++) {
			System.out.print(ks.randomNumberGenerator(0, 3) + " ");
		}

	}

}
