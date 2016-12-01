package ischool.noosphere.smartpowersocket.network;

import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

public class NetworkWorker {

    private static NetworkWorker networkWorker;

    private static final String NETWORK_LOG_TAG = "NETWORK";

    private static final int SERVER_PORT = 8080;
    private static final int CLIENT_PORT = 8081;

    private Server server;
    private Client client;

    private ConnectedStatusCallBack connectedStatusCallBackServer;
    private ConnectedStatusCallBack connectedStatusCallBackClient;

    public interface ConnectedStatusCallBack {
        void connected();
    }

    private NetworkWorker() {

    }

    public static NetworkWorker getInstance() {
        if(networkWorker == null) {
            networkWorker = new NetworkWorker();
        }
        return networkWorker;
    }

    public void startServer() {
        Log.d(NETWORK_LOG_TAG, "starting server");
        server = new Server();

        Kryo kryo = server.getKryo();
        kryo.register(byte[].class);
        server.start();

        try {
            server.bind(SERVER_PORT);
        } catch (IOException e) {
            Log.e(NETWORK_LOG_TAG, Log.getStackTraceString(e));
        }

        addDefaultServerListener();
        Log.d(NETWORK_LOG_TAG, "server started");
    }


    public void startClient(final String serverIp, final int serverPort) {
        Log.d(NETWORK_LOG_TAG, "starting client");
        client = new Client();
        Kryo kryo = client.getKryo();
        kryo.register(byte[].class);
        client.start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    client.connect(CLIENT_PORT, serverIp, serverPort);
                    if(connectedStatusCallBackClient != null) {
                        connectedStatusCallBackClient.connected();
                    }
                } catch (IOException e) {
                    Log.e(NETWORK_LOG_TAG, Log.getStackTraceString(e));
                }

                addDefaultClientListener();
                Log.d(NETWORK_LOG_TAG, "client started");
            }
        }).start();

    }

    public void sendData(final byte[] data) {
        if(server == null) {
            throw new IllegalArgumentException("server not started!");
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                server.sendToAllTCP(data);
            }
        }).start();

    }

    private void addDefaultServerListener() {
        server.addListener(new Listener() {

            public void received (Connection connection, Object object) {
                Log.d(NETWORK_LOG_TAG, "server received data! from " + connection.getRemoteAddressTCP().toString());
            }

            @Override
            public void connected(Connection connection) {
                super.connected(connection);
                Log.d(NETWORK_LOG_TAG, "client connected! " + connection.getRemoteAddressTCP().toString());
                if(connectedStatusCallBackServer != null) {
                    connectedStatusCallBackServer.connected();
                }
            }
        });

    }

    private void addDefaultClientListener() {
        client.addListener(new Listener() {
            public void received (Connection connection, Object object) {
                Log.d(NETWORK_LOG_TAG, "client received data! from " + connection.getRemoteAddressTCP().toString());
            }
        });
    }

    public void setConnectedStatusCallBackServer(ConnectedStatusCallBack connectedStatusCallBackServer) {
        this.connectedStatusCallBackServer = connectedStatusCallBackServer;
    }

    public void setConnectedStatusCallBackClient(ConnectedStatusCallBack connectedStatusCallBackClient) {
        this.connectedStatusCallBackClient = connectedStatusCallBackClient;
    }
}
