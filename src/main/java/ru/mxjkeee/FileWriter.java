package ru.mxjkeee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.lang.System.getProperty;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.lines;
import static java.nio.file.Files.write;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.apache.commons.lang3.StringUtils.join;
import static ru.mxjkeee.Constants.CONSOLE_DELIMITER;

@Getter
@AllArgsConstructor
public class FileWriter {

    private static final String DEFAULT_FILE_NAME = "file.txt";
    private static final String SPLIT_REGEX = "\\t|;";
    private static final String JOIN_SEPARATOR = ";";

    private final Path filePath;

    public static FileWriter getInstance() {
        return new FileWriter(DEFAULT_FILE_NAME);
    }

    public FileWriter(String fileName) {
        filePath = Paths.get(getProperty("user.dir"), "/src/main/resources/", fileName);
    }

    @SneakyThrows(IOException.class)
    public void writeNewValue(String key, String newValue) {
        List<String> lines = lines(filePath, UTF_8).limit(2).collect(toList());
        assert lines.size() == 2;
        List<String> keys = asList(lines.get(0).split(SPLIT_REGEX));
        List<String> values = asList(lines.get(1).split(SPLIT_REGEX));
        replaceValueByKey(keys, values, key, newValue);
        write(filePath, keysAndValuesToLines(keys, values), TRUNCATE_EXISTING);
    }

    private List<String> keysAndValuesToLines(final List<String> keys, final List<String> values) {
        return asList(join(keys, JOIN_SEPARATOR), join(values, JOIN_SEPARATOR));
    }

    private void replaceValueByKey(final List<String> keys, final List<String> values,
                                   final String key, final String newValue) {
        Integer valueIndex = keys.stream()
                .filter(foundKey -> foundKey.equals(key))
                .findFirst()
                .map(keys::indexOf)
                .orElseThrow(() -> new IllegalArgumentException("Unable to find key: " + key));
        values.set(valueIndex, newValue);
    }

    public static void main(String[] args) throws IOException {
        System.out.println("Starting task on writing to file");
        System.out.println(CONSOLE_DELIMITER);
        FileWriter fileWriter = FileWriter.getInstance();
        System.out.println("File before update:");
        lines(fileWriter.getFilePath()).forEachOrdered(System.out::println);
        System.out.println(CONSOLE_DELIMITER);
        fileWriter.writeNewValue("test2", randomAlphabetic(5));
        System.out.println("File after update: ");
        lines(fileWriter.getFilePath()).forEachOrdered(System.out::println);
        System.out.println(CONSOLE_DELIMITER);
        System.out.println("End task on writing to file");
    }
}
