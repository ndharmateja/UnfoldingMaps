package tester;

public class Person {
	private String name;
	public Person(String n) {
		super();
		this.name = n;
	}
	public String getName(){
		return name;
	}
	public void setName(String n) {
		this.name = n;
	}
	public String asString() {
		return this.getName();
	}
}
