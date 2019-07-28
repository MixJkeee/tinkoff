package ru.mxjkeee;

import static java.lang.String.valueOf;
import static java.util.stream.IntStream.rangeClosed;
import static ru.mxjkeee.Constants.CONSOLE_DELIMITER;

public class NumbersFormatter {

    public void printNumbers() {
        rangeClosed(1, 100).mapToObj(this::formatNumber).forEachOrdered(System.out::println);
    }

    private String formatNumber(int number) {
        String result = "";
        if (number % 3 == 0) {
            result += "Fizz";
        }
        if (number % 5 == 0) {
            result += "Buzz";
        }
        return result.isEmpty() ? valueOf(number) : result;
    }

    public static void main(String[] args) {
        System.out.println("Start running task \"Fizz\"/\"Buzz\"");
        System.out.println(CONSOLE_DELIMITER);
        new NumbersFormatter().printNumbers();
        System.out.println(CONSOLE_DELIMITER);
        System.out.println("End running task \"Fizz\"/\"Buzz\"");
    }
}
