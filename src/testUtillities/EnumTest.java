package testUtillities;

import enums.RoverName;

public class EnumTest {

	public static void main(String[] args) {
		

		System.out.println("ENUM_TEST: starting");
		
		System.out.println("ENUM_TEST: toString RoverName.ROVER_00 " + RoverName.ROVER_00.toString());	
		
		
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
	}

}
