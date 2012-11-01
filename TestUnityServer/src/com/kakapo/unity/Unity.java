package com.kakapo.unity;

import com.kakapo.unity.network.Listener;
import com.kakapo.unity.server.MemoryServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Properties;

public class Unity {

    private static final String UNITY_PROPERTIES = "unity.properties";
    private static int DEFAULT_BUFFER_SIZE = 512;
    private static String CHECKSUM_KEY;

    public static void main(String[] args)
            throws IOException {
        //TODO  - Change unity.properties to database.properties
        //TODO  - Read server settings from DB instead of unity.properties
        String path = UNITY_PROPERTIES;
        if (args.length > 0) {
            path = args[0];
        }

        File file = new File(path);
        if (!file.exists()) {
            //TODO - File not found exception
        }

        FileInputStream stream = new FileInputStream(file);

        Properties properties = new Properties();
        properties.load(stream);

        int port = Integer.parseInt(properties.getProperty("port"));
        String host = properties.getProperty("host");
        InetSocketAddress address;
        if (host != null) {
            address = new InetSocketAddress(host, port);
        } else {
            address = new InetSocketAddress(port);
        }

        int bufferSize = DEFAULT_BUFFER_SIZE;
        String bufferSizeText = properties.getProperty("buffers.size");
        if (bufferSizeText != null) {
            bufferSize = Integer.parseInt(bufferSizeText);
        }

        System.out.println("Unity: Listening on " + address);
        MemoryServer server = new MemoryServer();

        //TODO - Get ports from DB and call that many Listeners
        Listener listener = new Listener(address, bufferSize, 1000, server);


        String redirectServer = properties.getProperty("failover");
        if (redirectServer != null) {
            listener.getServer().setRedirect(redirectServer);
        }

        listener.start();
    }

    public static String getCHECKSUM_KEY() {
        return CHECKSUM_KEY;
    }
    
    private void serverStartup()
    {
    //TODO - Server startup
    }
       
}