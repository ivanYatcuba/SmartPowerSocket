package ischool.noosphere.smartpowersocket;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.esotericsoftware.kryonet.Client;

import java.io.IOException;

import ischool.noosphere.smartpowersocket.network.NetworkWorker;

import static android.content.Context.WIFI_SERVICE;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    private static final String MESSAGE_TO_SEND = "c1*";

    private TextView myIp;
    private EditText ipToConnect;
    private EditText portToConnect;

    private Button connectToSocket;
    private Button sendData;

    private ImageView isSocketConnected;
    private ImageView isClientConnected;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        myIp = (TextView) view.findViewById(R.id.my_ip);

        ipToConnect = (EditText) view.findViewById(R.id.ip_to_connect);
        portToConnect = (EditText) view.findViewById(R.id.port_to_connect);
        connectToSocket = (Button) view.findViewById(R.id.connect_to_socket);

        sendData = (Button) view.findViewById(R.id.send_data);

        isSocketConnected = (ImageView) view.findViewById(R.id.is_socket_connected);
        isClientConnected = (ImageView) view.findViewById(R.id.is_client_connected);


        myIp.setText(getMyIpAddress());

        connectToSocket.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    NetworkWorker.getInstance().startClient(ipToConnect.getText().toString(), Integer.parseInt(portToConnect.getText().toString()));
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), "failed connect to server", e);
                }

            }
        });

        NetworkWorker.getInstance().setConnectedStatusCallBackClient(new NetworkWorker.ConnectedStatusCallBack() {
            @Override
            public void connected() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isSocketConnected.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                    }
                });
            }
        });

        NetworkWorker.getInstance().setConnectedStatusCallBackServer(new NetworkWorker.ConnectedStatusCallBack() {
            @Override
            public void connected() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        isClientConnected.setImageDrawable(getResources().getDrawable(R.drawable.ok));
                    }
                });

            }
        });

        sendData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NetworkWorker.getInstance().sendData(MESSAGE_TO_SEND.getBytes());
            }
        });

        return view;
    }

    private String getMyIpAddress() {
        WifiManager wifiMgr = (WifiManager) getActivity().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
        int ip = wifiInfo.getIpAddress();
        return Formatter.formatIpAddress(ip);
    }

}
