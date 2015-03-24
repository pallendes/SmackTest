package com.smack.connectionsmanagers;

import android.util.Log;

import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

/**
 * Created by Usuario on 15-03-2015.
 */
public class SmackXMPPConnection {

    private static final String SERVER_IP = "192.168.43.100";
    private static final int SERVER_PORT = 5222;

    private static AbstractXMPPConnection connection = null;

    private static void openConnection(){

        try {

            XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
            config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
            config.setServiceName("indrainventari");
            config.setHost(SERVER_IP);
            config.setPort(SERVER_PORT);

            connection = new XMPPTCPConnection(config.build());
            connection.connect();

            connection.login("pablo2@indrainventari", "123");

            Presence presence = new Presence(Presence.Type.available);
            presence.setStatus("This is My Status!");

            connection.login();
            connection.sendPacket(presence);

            Log.i("XMPP", "Conectado!");
            Log.i("XMPP", "isAuthenticated? " + connection.isAuthenticated());

        }catch (IOException | XMPPException | SmackException e){
            Log.e("XMPP", e.getMessage());
        }

    }

    public static XMPPTCPConnection getConnection() {

        if (connection == null) {
            openConnection();
        }

        return (XMPPTCPConnection) connection;
    }

}
