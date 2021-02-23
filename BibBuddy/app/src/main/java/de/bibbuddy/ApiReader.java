package de.bibbuddy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

public class ApiReader implements Runnable {

    private String metadata = null;
    private String url;

    /**
     * The ApiReader class reads and returns data from the API
     *
     * @author Luis Moßburger
     *
     * @param url        concrete url that should be sent to API
     */
    ApiReader(String url) {
        this.url = url;
    }

    public void run() {
        URL metadataUrl;
        BufferedReader in;
        String metadata = "";
        System.out.println(url);
        try {
            metadataUrl = new URL(url);
            in = new BufferedReader(new InputStreamReader(metadataUrl.openStream()));
            metadata = in.lines().collect(Collectors.joining());
            in.close();
            this.metadata = metadata;
        } catch(Exception e) {
            System.out.println("*** ERROR ***");
            System.out.println(e);
        }
    }

    public String getMetadata() {
        return metadata;
    }

}
