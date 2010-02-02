package org.opengeo.smelter;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class Home extends ServerResource {
    @Get
    public String toString() {
        return "Hello World";
    }
}
