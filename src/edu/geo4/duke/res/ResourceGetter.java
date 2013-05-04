package edu.geo4.duke.res;
import java.net.URL;


public class ResourceGetter {
    public URL getURL (String path) {
        return this.getClass().getResource(path);
    }
}