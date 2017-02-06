package ischool.noosphere.smartpowersocket.network;

import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

public class NetworkWorkerKryo implements NetworkWorker {

    private static NetworkWorkerKryo networkWorker;

    private Server server;
    private NetworkWorker.ConnectedStatusCallBack connectedStatusCallBackServer;


    private NetworkWorkerKryo() {
    }

    public static NetworkWorkerKryo getInstance() {
        if(networkWorker == null) {
            networkWorker = new NetworkWorkerKryo();
        }
        return networkWorker;
    }

    @Override
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

    @Override
    public void sendData(final byte[] data) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if(server != null) {
                    server.sendToAllTCP(data);
                }
            }
        }).start();

    }

    private void addDefaultServerListener() {
        server.addListener(new Listener() {

            public void received (Connection connection, Object object) {
                Log.d(NETWORK_LOG_TAG, "server received data! from " + connection.getRemoteAddressTCP().toString());

                if(object instanceof byte[]) {
                    String str = new String((byte[]) object);

                    connectedStatusCallBackServer.dataReceived(str);


                }
            }

            @Override
            public void connected(Connection connection) {
                super.connected(connection);
                Log.d(NETWORK_LOG_TAG, "client connected! " + connection.getRemoteAddressTCP().toString());
                if(connectedStatusCallBackServer != null) {
                    connectedStatusCallBackServer.connected();
                }
            }

            @Override
            public void disconnected(Connection connection) {
                super.disconnected(connection);
                Log.d(NETWORK_LOG_TAG, "client disconnected! ");
                if(connectedStatusCallBackServer != null) {
                    connectedStatusCallBackServer.disconnected();
                }
            }
        });

    }

    @Override
    public void setConnectedStatusCallBackServer(NetworkWorker.ConnectedStatusCallBack connectedStatusCallBackServer) {
        this.connectedStatusCallBackServer = connectedStatusCallBackServer;
    }

}
