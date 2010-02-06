package org.opengeo.smelter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
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
        return this.first;
    }

    public List<String> getExcludeFiles() {
        return this.exclude;
    }

    public List<File> getSortedFiles() throws IOException {
        List<String> inputPaths = new ArrayList<String>();
        inputPaths.addAll(first);
        inputPaths.addAll(include);
        List<File> inputFiles = expandPaths(inputPaths);

        List<String> excludePaths = new ArrayList<String>();
        excludePaths.addAll(exclude);
        List<File> excludeFiles = expandPaths(excludePaths);

        List<Node<File>> dependencies = extractDependencies(inputFiles);

        return null;
    }

    private List<File> expandPaths(List<String> paths) {
        List<File> files = new ArrayList<File>();
        for (String path : paths) {
            files.add(new File(root, path));
        }
        return files;
    }

    private List<Node<File>> extractDependencies(List<File> sources) 
    throws IOException {
        List<Node<File>> queue = new ArrayList<Node<File>>();
        Map<File, Node<File>> deps = new HashMap<File, Node<File>>();

        for (File f : sources) {
            queue.add(new Node<File>(f));
        }

        while (queue.size() > 0) {
            scan(queue.remove(0), queue, deps);
        }

        return new ArrayList<Node<File>>(deps.values());
    }

    private Node<File> nodeFor(
        File f,
        List<Node<File>> queue,
        Map<File, Node<File>> cache
    ) {
        if (cache.containsKey(f)) {
            return cache.get(f);
        } else {
            Node<File> node = new Node<File>(f);
            queue.add(node);
            cache.put(f, node);
            return node;
        }
    }

    private void scan(
        Node<File> source,
        List<Node<File>> queue,
        Map<File, Node<File>> cache
    ) throws IOException {
        Pattern include =
            Pattern.compile("@(?:includes|requires)\\s+\\p{Graph}*");
        BufferedReader reader =
            new BufferedReader(new FileReader(source.payload));
        String line;
        while ((line = reader.readLine()) != null) {
            Matcher m = include.matcher(line);
            if (m.matches()) {
                source.dependencies.add(
                    nodeFor(new File(root, m.group(1)), queue, cache)
                );
            }
        }
    }

    private class Node<A> {
        public final A payload;
        public final List<Node<A>> dependencies = new ArrayList<Node<A>>();

        public Node(A payload) {
            this.payload = payload;
        }

        @Override public boolean equals(Object that) {
            if (that == null) return false;

            if (that instanceof Node) {
                Object payload = ((Node)that).payload;
                return payload == null 
                    ? this.payload == null 
                    : payload.equals(this.payload);
            }

            return false;
        }
    }

    @Override public String toString() {
        return Arrays.asList(new Object[] {
            root, license, first, include, exclude
        }).toString();
    }
}
