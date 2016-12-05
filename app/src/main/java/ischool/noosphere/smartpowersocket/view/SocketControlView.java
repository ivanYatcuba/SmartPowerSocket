package ischool.noosphere.smartpowersocket.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import ischool.noosphere.smartpowersocket.R;
import ischool.noosphere.smartpowersocket.protocol.SmartPowerSocketProtocol;

public class SocketControlView extends RelativeLayout {

    public static String currentSocket = "";

    private Switch socketControl;
    private Button requestSocketCurrent;
    private TextView socketAcDc;

    private String socketId;
    private DataSender dataSender;

    public interface DataSender {

        void sendData(byte[] data);

    }

    public SocketControlView(Context context, String socketId, DataSender dataSender) {
        super(context);
        View view = inflate(context, R.layout.socket_control, null);
        addView(view);
        this.socketId = socketId;
        this.dataSender = dataSender;
        init();
    }


    public void init() {
       socketControl = (Switch) findViewById(R.id.set_socket_status);

       socketControl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
           @Override
           public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    if(dataSender != null) {
                        dataSender.sendData(getCommand(SmartPowerSocketProtocol.ENABLE_SOCKET, socketId));
                    }
                } else {
                    if(dataSender != null) {
                        dataSender.sendData(getCommand(SmartPowerSocketProtocol.DISABLE_SOCKET, socketId));
                    }
                }
           }
       });

       requestSocketCurrent = (Button) findViewById(R.id.get_current);

        requestSocketCurrent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSocket = socketId;
                dataSender.sendData(getCommand(SmartPowerSocketProtocol.CURRENT_SOCKET, socketId));
            }
        });

       socketAcDc = (TextView) findViewById(R.id.socket_ac_dc);
    }

    private byte[] getCommand(String data, String socketId) {
        return (data + socketId + SmartPowerSocketProtocol.END_LINE).getBytes();
    }

    public void setSocketAcDc(String data) {
        socketAcDc.setText(data);
    }

}
