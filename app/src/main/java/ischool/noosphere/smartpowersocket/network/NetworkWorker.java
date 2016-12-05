package ischool.noosphere.smartpowersocket.network;

public interface NetworkWorker {

    String NETWORK_LOG_TAG = "NETWORK";
    int SERVER_PORT = 8888;


    void sendData(final byte[] data);
    void startServer();

    interface ConnectedStatusCallBack {
        void connected();
        void disconnected();
        void dataReceived(String data);
    }

    void setConnectedStatusCallBackServer(ConnectedStatusCallBack connectedStatusCallBackServer);

}
