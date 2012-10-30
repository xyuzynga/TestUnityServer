package com.kakapo.unity;

import com.kakapo.unity.client.MockClient;
import com.kakapo.unity.network.Listener;
import com.kakapo.unity.server.MemoryServer;
import com.kakapo.unity.message.client.RegisterMessage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.Properties;

public class Unity {

    private static final String UNITY_PROPERTIES = "unity.properties";
    private static final String UNITY_DEFAULT_PROPERTIES = "/unity-default.properties";
    private static int DEFAULT_BUFFER_SIZE = 512;
    private static final int DEFAULT_MAX_CONNECTIONS = 0;

    public static void main(String[] args)
            throws IOException {
        String path = UNITY_PROPERTIES;
        if (args.length > 0) {
            path = args[0];
        }

        File file = new File(path);
        if (!file.exists()) {
            URL template = Listener.class.getResource(UNITY_DEFAULT_PROPERTIES);
            copyFile(file, template);
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
        Listener listener = new Listener(address, bufferSize, 1000, server);

        int maxConnections = DEFAULT_MAX_CONNECTIONS;
        String maxConnectionsText = properties.getProperty("clients.max");
        if (maxConnectionsText != null) {
            maxConnections = Integer.parseInt(maxConnectionsText);
        }

        listener.getServer().setMaxConnections(maxConnections);

        String redirectServer = properties.getProperty("failover");
        if (redirectServer != null) {
            listener.getServer().setRedirect(redirectServer);
        }

        listener.start();
        
//        MockClient mc = new MockClient(server,"drd.co.in", "felix@drd.co.in");
//        RegisterMessage rm = new RegisterMessage("drd.co.in", "felix@drd.co.in", true);
//        mc.send(rm);
    }

    private static void copyFile(File file, URL template)
            throws IOException, FileNotFoundException {
        if (file.createNewFile()) {
            FileOutputStream out = new FileOutputStream(file);
            InputStream in = template.openStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        } else {
            System.err.println("Could not create properties file in " + file);
            System.exit(1);
        }
    }
}