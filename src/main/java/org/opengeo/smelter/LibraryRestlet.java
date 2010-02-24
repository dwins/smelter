package org.opengeo.smelter;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import freemarker.template.Configuration;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.StreamRepresentation;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Finder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.opengeo.smelter.util.JSMin;

public class LibraryRestlet extends Finder {
    private String prefix;
    public Library library;

    private static final Configuration freemarker;

    static {
        freemarker = new Configuration();
        freemarker.setClassForTemplateLoading(LibraryRestlet.class, "");
    }

    static enum Mode { Concatenated, Minified, Loader }
    private Mode mode = Mode.Loader;
    
    public LibraryRestlet(String prefix, Library library) {
        this.prefix = prefix;
        this.library = library;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Mode getMode() { 
        return mode;
    }

    @Override
    public ServerResource find(Request request, Response response) {
        return new LibraryResource();
    }

    public class LibraryResource extends ServerResource {
        @Get
        public Representation get() {
            switch (mode) {
                case Concatenated: 
                    return getConcatenated();
                case Minified: 
                    return getMinified();
                case Loader:
                    return getLoader();
                default:
                    return new StringRepresentation(
                        "Unexpected application state...",
                        MediaType.TEXT_PLAIN
                    );
            }
        }

        private Representation getConcatenated() {
            try {
                final InputStream sources = library.getConcatenatedSources();

                return new StreamRepresentation(MediaType.TEXT_JAVASCRIPT) {
                    @Override public void write(OutputStream out)
                    throws IOException {
                      copyStream(sources, out);
                    }

                    @Override public InputStream getStream() 
                    throws IOException {
                        throw new UnsupportedOperationException();
                    }
                }; 
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private Representation getMinified() {
            try {
                final InputStream sources = library.getConcatenatedSources();

                return new StreamRepresentation(MediaType.TEXT_JAVASCRIPT) {
                    @Override public void write(OutputStream out)
                    throws IOException {
                        try {
                            new JSMin(sources, out).jsmin();
                        } catch (IOException ioe) {
                             throw ioe;
                        } catch (Exception e) {
                             // skip jsmin's exceptions?
                        }
                    }

                    @Override public InputStream getStream() 
                    throws IOException {
                        throw new UnsupportedOperationException();
                    }
                }; 
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private Representation getLoader() {
            Map<String, Object> configMap = new HashMap<String, Object>();
            List<String> includes = new ArrayList<String>();

            try {
                includes.addAll(library.getSortedPaths());
            } catch (IOException ioe) {
                System.out.println(ioe);
            }

            configMap.put("includes", includes);
            configMap.put("prefix", prefix);
            freemarker.setClassForTemplateLoading(LibraryRestlet.class, "");

            return new TemplateRepresentation(
                "loader.js.ftl",
                freemarker, 
                configMap,
                MediaType.TEXT_JAVASCRIPT
            );
        }
    }

    private static void copyStream(InputStream in, OutputStream out)
    throws IOException {
        byte[] buff = new byte[4096];
        int length = 0;
        while ((length = in.read(buff)) != -1) {
            out.write(buff, 0, length);
        }
    }
}
