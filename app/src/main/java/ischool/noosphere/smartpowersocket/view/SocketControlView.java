package ischool.noosphere.smartpowersocket.view;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.NumberPicker;
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
    private TextView socketNameView;
    private Button socketAcDc;
    private NumberPicker powerLimit;
    private Button setPowerLimit;

    private String socketId;
    private String socketName;

    private DataSender dataSender;

    public interface DataSender {

        void sendData(byte[] data);

    }

    public SocketControlView(Context context, String socketId, String socketName, DataSender dataSender) {
        super(context);
        View view = inflate(context, R.layout.socket_control, null);
        addView(view, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        this.socketName = socketName;
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

        socketNameView = (TextView) findViewById(R.id.socket_name);
        socketNameView.setText(socketName);

        socketAcDc = (Button) findViewById(R.id.socket_ac_dc);
        socketAcDc.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentSocket = socketId;
                dataSender.sendData(getCommand(SmartPowerSocketProtocol.CURRENT_SOCKET, socketId));
            }
        });

        powerLimit = (NumberPicker) findViewById(R.id.power_limit);

        powerLimit.setMinValue(1);
        powerLimit.setMaxValue(16);
        powerLimit.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        setPowerLimit = (Button) findViewById(R.id.set_power_limit);

        setPowerLimit.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Integer powerLimit = SocketControlView.this.powerLimit.getValue();
                    dataSender.sendData((socketName + formatPowerValue(powerLimit)  + SmartPowerSocketProtocol.END_LINE).getBytes());
                } catch (NumberFormatException e) {
                    //ignored
                }

            }
        });
    }

    public void turnOffSwitch() {
        socketControl.setChecked(false);
    }

    public String getSocketName() {
        return socketName;
    }

    private String formatPowerValue(int powerLimit) {
        if(powerLimit < 0) throw new IllegalArgumentException("wrong time!");
        if(powerLimit < 10) {
            return "0" + powerLimit; //adding leading zero
        } else{
            return String.valueOf(powerLimit);
        }
    }

    private byte[] getCommand(String data, String socketId) {
        return (data + socketId + SmartPowerSocketProtocol.END_LINE).getBytes();
    }

    public void setSocketAcDc(String data) {
        try {
            int i = Integer.parseInt(data);
            Double calculatedData = 0.074 * (i - 520);

            if(calculatedData < 0) {
                socketAcDc.setText(String.valueOf(0));
            } else {
                Double truncatedDouble = BigDecimal.valueOf(calculatedData)
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue();
                socketAcDc.setText(String.valueOf(truncatedDouble));
            }

        } catch (Exception e) {
            socketAcDc.setText("");
        }
    }

}
