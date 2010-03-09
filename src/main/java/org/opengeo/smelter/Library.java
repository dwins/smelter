package org.opengeo.smelter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Library {
    private File root;
    private File license;
    private List<String> first = new ArrayList<String>();
    private List<String> include = new ArrayList<String>();
    private List<String> exclude = new ArrayList<String>();

    public void setRoot(File root) {
        this.root = root;
    }

    public File getRoot() {
        return this.root;
    }

    public void setLicense(File license) {
        this.license = license;
    }

    public File getLicense() {
        return this.license;
    }

    public List<String> getFirstFiles() {
        return this.first;
    }

    public List<String> getIncludeFiles() {
        return this.include;
    }

    public List<String> getExcludeFiles() {
        return this.exclude;
    }

    public List<String> getSortedPaths() throws IOException {
        Map<String, List<String>> dependencies = buildDependencyGraph();
        List<String> unchecked = new ArrayList<String>();
        List<String> toInclude = new ArrayList<String>();
        unchecked.addAll(getFirstFiles());
        unchecked.addAll(getIncludeFiles());

        while (unchecked.size() > 0) {
            String path = unchecked.get(0);
            if (toInclude.contains(path)) {
                unchecked.remove(0);
                continue;
            } else if (toInclude.containsAll(dependencies.get(path))) {
                toInclude.add(path);
                unchecked.remove(0);
            } else {
                unchecked.addAll(0, dependencies.get(path));
            }
        }

        return toInclude;
    }

    public InputStream getConcatenatedSources() throws IOException {
        final Iterator<String> includes = getSortedPaths().iterator();
        final Enumeration<InputStream> includeStreams =
            new Enumeration<InputStream>() {
                public boolean hasMoreElements() {
                    return includes.hasNext();
                }

                public InputStream nextElement() {
                    try {
                        return new FileInputStream(
                            new File(
                                getRoot(),
                                includes.next()
                            )
                        );
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };
        return new SequenceInputStream(includeStreams);
    }

    private List<File> expandPaths(List<String> paths) {
        List<File> files = new ArrayList<File>();

        for (String path : paths) {
            files.add(new File(root, path));
        }

        return files;
    }

    private Map<String, List<String>> buildDependencyGraph() {
        Map<String, List<String>> graph = new HashMap<String, List<String>>();
        try {
            List<File> sources = searchForSources(root);

            for (File f : sources) {
                String path = f.getCanonicalPath()
                    .substring(root.getCanonicalPath().length() + 1);
                graph.put(path, findDependencies(f));
            }
        } catch (IOException ioe) {
            System.out.println("Couldn't build dependency graph due to " + ioe);
        }

        return graph;
    }

    private static List<String> findDependencies(File f) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(f));
        String line;
        List<String> dependencies = new ArrayList<String>();
        Pattern declaration = Pattern.compile(
            "\\s*\\*\\s*@(?:requires|include)\\s+(\\p{Graph}*)\\s*"
        );

        while ((line = reader.readLine()) != null) {
            Matcher matcher = declaration.matcher(line);
            if (matcher.matches()) {
                dependencies.add(matcher.group(1));
            }
        }

        return dependencies;
    }

    private List<File> searchForSources(File root) throws IOException {
        List<File> results = new ArrayList<File>();

        FileFilter jsFilter = new FileFilter() { 
            public boolean accept(File file) {
                boolean dir = file.isDirectory() && !file.getName().startsWith(".");
                boolean js = file.isFile() && file.getName().endsWith(".js");
                return dir || js;
            } 
        }; 
        
        for (File child : root.listFiles(jsFilter)) {
            if (child.isDirectory()) {
                results.addAll(searchForSources(child));
            } else {
                results.add(child);
            }
        }

        return results;
    }

    private String relativePath(File f) {
        return f.getAbsolutePath()
            .substring(root.getAbsolutePath().length() + 1);
    }

    @Override public String toString() {
        return Arrays.asList(new Object[] {
            root, license, first, include, exclude
        }).toString();
    }
}
