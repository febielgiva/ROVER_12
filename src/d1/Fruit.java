package d1;

public class Fruit {
	String name;
	String aroma;

	public Fruit(String name, String aroma) {
		super();
		this.name = name;
		this.aroma = aroma;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAroma() {
		return aroma;
	}

	public void setAroma(String aroma) {
		this.aroma = aroma;
	}

	public void hiFruit(){
		System.out.println("I an fruit");
	}
}
