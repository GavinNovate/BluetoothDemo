package cn.autoio.bluetoothdemo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * 数据收发界面 负责发送数据到对方蓝牙设备和从对方蓝牙设备接收数据
 */
public class DataActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);
    }
}
