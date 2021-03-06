package ischool.noosphere.smartpowersocket;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

import ischool.noosphere.smartpowersocket.network.NetworkWorkerKryo;
import ischool.noosphere.smartpowersocket.network.NetworkWorkerPlain;
import ischool.noosphere.smartpowersocket.view.SocketControlView;

import static android.content.Context.WIFI_SERVICE;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static final String SOCKET_ID_1 = "1";
    public static final String SOCKET_ID_2 = "2";
    public static final String SOCKET_ID_3 = "3";

    public static final String SOCKET_NAME_1 = "A";
    public static final String SOCKET_NAME_2 = "B";
    public static final String SOCKET_NAME_3 = "C";

    private TextView myIp;
    private LinearLayout viewContainer;

    private Map<String, SocketControlView> stringSocketControlViewMap;


    public MainActivityFragment() {
    }

    private SocketControlView.DataSender getDataSender() {
        return new SocketControlView.DataSender() {
            @Override
            public void sendData(byte[] data) {
                NetworkWorkerPlain.getInstance().sendData(data);
            }
        };
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_main, container, false);

        stringSocketControlViewMap  = new HashMap<>();

        myIp = (TextView) view.findViewById(R.id.my_ip);

        viewContainer = (LinearLayout) view.findViewById(R.id.view_container);

        final SocketControlView socket1 = new SocketControlView(getActivity(), SOCKET_ID_1, SOCKET_NAME_1, getDataSender());
        final SocketControlView socket2 = new SocketControlView(getActivity(), SOCKET_ID_2, SOCKET_NAME_2, getDataSender());
        final SocketControlView socket3 = new SocketControlView(getActivity(), SOCKET_ID_3, SOCKET_NAME_3, getDataSender());

        stringSocketControlViewMap.put(SOCKET_ID_1, socket1);
        stringSocketControlViewMap.put(SOCKET_ID_2, socket2);
        stringSocketControlViewMap.put(SOCKET_ID_3, socket3);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.setMargins(0, 30, 0, 0);

        viewContainer.addView(socket1, layoutParams);
        viewContainer.addView(socket2, layoutParams);
        viewContainer.addView(socket3, layoutParams);

        myIp.setText(getMyIpAddress());

        NetworkWorkerPlain.getInstance().setConnectedStatusCallBackServer(new NetworkWorkerKryo.ConnectedStatusCallBack() {
            @Override
            public void connected() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity)(getActivity())).setConnected(R.drawable.ic_ok);
                    }
                });

            }

            @Override
            public void disconnected() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ((MainActivity)(getActivity())).setConnected(R.drawable.ic_error);
                    }
                });
            }

            @Override
            public void dataReceived(final String data) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(data.equals("A_lim_over") || data.equals("B_lim_over") || data.equals("C_lim_over")) {
                            String socketName = data.substring(0, 1);
                            //todo show warning

                            new AlertDialog.Builder(getActivity())
                                    .setTitle("WARNING")
                                    .setMessage("Phase " + socketName + "\nPreset limit has exceeded!\nPlease heck the connection or set another limit")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show();

                            for (SocketControlView view1: stringSocketControlViewMap.values()) {
                                if(view1.getSocketName().equals(socketName)) {
                                    view1.turnOffSwitch();
                                }
                            }

                        } else {
                            SocketControlView socketControlView = stringSocketControlViewMap.get(SocketControlView.currentSocket);
                            if(socketControlView != null) {
                                socketControlView.setSocketAcDc(data);
                            }
                        }
                    }
                });
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
