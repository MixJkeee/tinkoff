package ru.mxjkeee;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static ru.mxjkeee.Constants.CONSOLE_DELIMITER;

public class StringsFormatter {

    public String formatString(String initialString) {
        Pattern pattern = compile("(\\w)(\\1+)");
        Matcher matcher = pattern.matcher(initialString);
        while (matcher.find()) {
            String duplicatedSymbols = matcher.group(2);
            initialString = initialString.replace(duplicatedSymbols, EMPTY);
            matcher = pattern.matcher(initialString);
        }
        return initialString;
    }

    public static void main(String[] args) {
        System.out.println("Starting task with deleting repeated symbols...");
        System.out.println(CONSOLE_DELIMITER);
        StringsFormatter stringsFormatter = new StringsFormatter();
        String initialString = "aaabccddd";
        System.out.println(initialString + " => " + stringsFormatter.formatString(initialString));
        initialString = "baab";
        System.out.println(initialString + " => " + stringsFormatter.formatString(initialString));
        System.out.println(CONSOLE_DELIMITER);
        System.out.println("End of task with deleting repeated symbols!");
    }
}
