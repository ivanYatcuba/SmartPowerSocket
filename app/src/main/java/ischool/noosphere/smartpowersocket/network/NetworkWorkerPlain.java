package ischool.noosphere.smartpowersocket.network;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NetworkWorkerPlain implements NetworkWorker {

    private static NetworkWorkerPlain ourInstance = new NetworkWorkerPlain();
    private ServerSocket serverSocket;

    private Map<String, ClientWorker> socketMap;

    private NetworkWorker.ConnectedStatusCallBack connectedStatusCallBackServer;

    public static NetworkWorkerPlain getInstance() {
        return ourInstance;
    }

    private NetworkWorkerPlain() {
        socketMap = new HashMap<>();
    }

    @Override
    public void sendData(byte[] data) {
        Log.d("SEND DATA", new String(data));
        for(String uid: socketMap.keySet()) {
            try {
                DataOutputStream outToClient = new DataOutputStream(socketMap.get(uid).getOutputStream());
                outToClient.write(data);
            } catch (IOException e) {
                Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
            }
        }
    }

    @Override
    public void startServer() {
        try {
            serverSocket = new ServerSocket(NetworkWorker.SERVER_PORT);
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true)
                {
                    Socket connectionSocket;
                    try {
                        connectionSocket = serverSocket.accept();
                        final String uid = UUID.randomUUID().toString();
                        final ClientWorker clientWorker = new ClientWorker(uid, connectionSocket, new MessageDispatcher() {
                            @Override
                            public void dispatchMessage(String clientId, String clientData) {
                                if(connectedStatusCallBackServer != null) {
                                    connectedStatusCallBackServer.dataReceived(clientData);
                                }
                            }
                        });
                        socketMap.put(uid, clientWorker);
                        clientWorker.start();
                        connectedStatusCallBackServer.connected();

                        Log.d(NETWORK_LOG_TAG, "client with connected!");
                    } catch (IOException e) {
                        Log.e(getClass().getSimpleName(), Log.getStackTraceString(e));
                    }
                }
            }
        }).start();
        Log.d(NETWORK_LOG_TAG, "server started!");
    }

    @Override
    public void setConnectedStatusCallBackServer(ConnectedStatusCallBack connectedStatusCallBackServer) {
        this.connectedStatusCallBackServer = connectedStatusCallBackServer;
    }

    public class ClientWorker extends Thread {

        private String id;
        private Socket socket;
        private MessageDispatcher messageDispatcher;
        private BufferedReader mIn;

        public ClientWorker(String id, Socket socket, MessageDispatcher messageDispatcher) throws IOException {
            this.id = id;
            this.socket = socket;
            this.messageDispatcher = messageDispatcher;
            mIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            try {
                while (!isInterrupted()) {
                    String message = mIn.readLine();
                    if (message == null)
                        break;
                    messageDispatcher.dispatchMessage(id, message);
                }
            } catch (IOException ioex) {
                // Problem reading from socket (communication is broken)
            }
        }

        public OutputStream getOutputStream() throws IOException {
            return socket.getOutputStream();
        }
    }

    public interface MessageDispatcher {

        void dispatchMessage(String clientId, String clientData);
    }
}
