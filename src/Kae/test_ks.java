package Kae;

import static org.junit.Assert.*;

import org.junit.Test;

public class test_ks {

	@Test
	public void test() {
		//MapTileUtil[][] mt = new MapTileUtil[3][4];
		MapTileUtil[][] mt = {
				{ new MapTileUtil("ROCK"), new MapTileUtil("ROCK"),
						new MapTileUtil("NONE") },
				{ new MapTileUtil("ROCK"), new MapTileUtil("ROCK"),
						new MapTileUtil("ROCK") } };
	}

}
