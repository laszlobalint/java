package DesingPatterns.composite;

public class Director extends Subordinating {


    public Director(String name, double salary, int year, int month, int day) {
        super(name, salary, year, month, day);
    }

    @Override
    public void work() {

    }

    @Override
    public void goHoliday() {

    }
}
