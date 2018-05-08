package tester;

public class Student extends Person {
	private int studentId;
	public Student(String n, int id) {
		super(n);
		this.studentId = id;
	}
	public int getSID(){
		return this.studentId;
	}	
	public String asString() {
		return this.getName() + ": " + this.getSID();
	}
	public static void main(String[] args) {
		Person[] p = new Person[2];
		p[0] = new Person("Tim");
		p[1] = new Student("Cara", 1234);
		for (Person person : p) {
			System.out.println(person.asString());
		}
	}
}
