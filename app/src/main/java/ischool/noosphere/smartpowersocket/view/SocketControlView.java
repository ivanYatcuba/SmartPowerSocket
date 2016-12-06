package ischool.noosphere.smartpowersocket.view;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import java.math.BigDecimal;
import java.math.RoundingMode;

import ischool.noosphere.smartpowersocket.R;
import ischool.noosphere.smartpowersocket.protocol.SmartPowerSocketProtocol;

public class SocketControlView extends RelativeLayout {

    public static String currentSocket = "";

    private Switch socketControl;
    private ImageButton requestSocketCurrent;
    private TextView socketAcDc;

    private String socketId;
    private DataSender dataSender;

    public interface DataSender {

        void sendData(byte[] data);

    }

    public SocketControlView(Context context, String socketId, DataSender dataSender) {
        super(context);
        View view = inflate(context, R.layout.socket_control, null);
        addView(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
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

       requestSocketCurrent = (ImageButton) findViewById(R.id.get_current);

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
        try {
            int i = Integer.parseInt(data);
            Double calculatedData = 37.873 - (0.0742 * i);
            Double truncatedDouble = BigDecimal.valueOf(calculatedData)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
            socketAcDc.setText(String.valueOf(truncatedDouble));
        } catch (Exception e) {
            socketAcDc.setText("");
        }
    }

}
