import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    private static final Pattern EXTERNAL_ID_PATTERN = Pattern.compile("(\\d{4}-\\d{2,3})");
    private static final Map<String, String> INSTRUTIONS = new HashMap<>();
    private static final String ORIGIN_PATH;

    static {
        ORIGIN_PATH = System.getProperty("user.dir");
        INSTRUTIONS.put("archive ||", "ARCHIVE_FOUND");
        INSTRUTIONS.put("live ||", "LIVE_FOUND");
        INSTRUTIONS.put("unplanned event - ", "UNPLANNED_FOUND");
    }

    public static void main(String[] args) throws IOException {
        Path originPath, destinationPath;
        File directory = new File(ORIGIN_PATH);
        System.out.println("FILES TO LOOK THROUGH: " + directory.listFiles().length);
        for (Map.Entry<String, String> instruction : INSTRUTIONS.entrySet()) {
            Integer filesCounter = 0;
            for (File file : directory.listFiles()) {
                int specificFileOccurences = 1;
                if (file.isFile()) {
                    String fileName = file.getName();
                    String fileContent = readFromInputStream(new FileInputStream(file));
                    String keyWord = instruction.getKey();
                    if (fileContent.toLowerCase().contains(keyWord)) {
                        String externalId = getExternalId(fileContent);
                        originPath = Paths.get(ORIGIN_PATH + "\\" + fileName);
                        destinationPath = getDestinationPath(instruction, specificFileOccurences, externalId);
                        try {
                            while (new File(destinationPath.toString()).isFile()) {
                                specificFileOccurences++;
                                destinationPath = getDestinationPath(instruction, specificFileOccurences, externalId);
                            }
                            Files.move(originPath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        } catch (NoSuchFileException directoryDoesNotExist) {
                            Files.createDirectory(Paths.get(ORIGIN_PATH + "\\" + instruction.getValue()));
                            Files.move(originPath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                        }
                        filesCounter++;
                    }
                }
            }
            System.out.println(filesCounter + " " + instruction.getKey() + " files moved to " + instruction.getValue());
        }
    }

    private static Path getDestinationPath(Map.Entry<String, String> instruction, int fileOccurence, String externalId) {
        return Paths.get(ORIGIN_PATH + "\\"
                + instruction.getValue() + "\\"
                + externalId + "_"
                + fileOccurence
                + ".html");
    }

    private static String getExternalId(String fileContent) {
        Matcher matcher = EXTERNAL_ID_PATTERN.matcher(fileContent);
        if (matcher.find()) {
            return matcher.group(0);
        }
        return null;
    }

    static private String readFromInputStream(InputStream inputStream) throws IOException {
        StringBuilder resultStringBuilder = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                resultStringBuilder.append(line);
                resultStringBuilder.append("\n");
            }
        }
        return resultStringBuilder.toString();
    }
}


