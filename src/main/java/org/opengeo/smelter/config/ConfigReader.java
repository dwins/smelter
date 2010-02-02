package org.opengeo.smelter.config;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

public class ConfigReader {
    private static Pattern section = Pattern.compile("\\[(\\p{Print}+)\\]");
    private static Pattern property = 
        Pattern.compile("(\\p{Graph}+?)\\p{Space}*=(\\.*)");
    private static Pattern continuation = Pattern.compile("\\p{Space}+(.*)");

    public static Map<String,Map<String, String>> parse(BufferedReader r) 
    throws IOException {
        String line; 
        Map<String, Map<String, String>> results = 
            new HashMap<String, Map<String, String>>();
        Map<String, String> currentSection = null;

        String currentPropertyName = null;
        StringBuilder currentProperty = new StringBuilder();

        while ((line = r.readLine()) != null) {
            Matcher sectionMatcher = section.matcher(line);
            if (sectionMatcher.matches()) {
                if (currentPropertyName != null && currentSection != null) {
                    currentSection.put(
                        currentPropertyName,
                        currentProperty.toString()
                    );
                }

                currentProperty = new StringBuilder();
                currentPropertyName = null;
                currentSection = new HashMap<String, String>();
                results.put(sectionMatcher.group(1), currentSection);
                continue;
            }

            Matcher propertyMatcher = property.matcher(line);
            if (propertyMatcher.matches()) {
                if (currentPropertyName != null && currentSection != null) {
                    currentSection.put(
                        currentPropertyName,
                        currentProperty.toString()
                    );
                }
                currentPropertyName = propertyMatcher.group(1);
                currentProperty = new StringBuilder();
                currentProperty.append(propertyMatcher.group(2));
                continue;
            } 

            Matcher continuationMatcher = continuation.matcher(line);
            if (continuationMatcher.matches()) {
                currentProperty.append("\n").append(continuationMatcher.group(1));
                continue;
            }
        }
        return results;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(
            parse(new BufferedReader(new FileReader(new File(args[0]))))
        );
    }
}
