package com.project.spinelloapp;

import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.MessageQueue;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    public static final String NOTIFICATION_CHANNEL_ID = "10001";
    private int count = 0;
    private BluetoothAdapter mBluetoothAdapter;
    private static final int REQUEST_ENABLE_BT = 3;
    ImageView bluetooth_state;
    TextView connectMsg;
    ImageView connectState;
    TextView serverMsg;
    TextView serverConnected;
    TextView msg;
    byte[] coords = new byte[1024];
    ConnectedTask mConnectedTask = null;
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mConversationArrayAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = (Button) findViewById(R.id.button);
        Button bluetooth = (Button) findViewById(R.id.bluetooth);
        Button server = (Button)findViewById(R.id.serverCheck);
        bluetooth_state = (ImageView) findViewById(R.id.state);
        connectMsg = (TextView)findViewById(R.id.connectMsg);

        connectState = (ImageView)findViewById(R.id.connectState);
        serverMsg = (TextView) findViewById(R.id.serverMsg);
        serverConnected = (TextView) findViewById(R.id.serverConnected);
//        msg = (TextView)findViewById(R.id.msg);

        mConversationArrayAdapter = new ArrayAdapter<>( this, android.R.layout.simple_list_item_1 );

        IntentFilter filter3 = new IntentFilter();
        filter3.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        filter3.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(mBroadcastReceiver3, filter3);



        server.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConnectThread thread = new ConnectThread();
                thread.start();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                NotificationSomethings(); // 알림 보내기.
            }
        });
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetooth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bluetoothCheck();
            }

        });

    }

//    Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
//    final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

    private final BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        //블루투스 연결 상태에 따라 UI 변경 //TODO: 라즈베리파이와 연결되었을때 UI가 변경되도록 설정하기.
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action){
                case BluetoothDevice.ACTION_ACL_CONNECTED:
                    //블루투스가 연결되어 있을 경우
                    bluetooth_state.setImageResource(R.drawable.ic_baseline_check_circle_24);
                    bluetooth_state.setBackgroundResource(R.drawable.bluetooth_state_connected);
                    connectMsg.setVisibility(View.VISIBLE);
                    break;
                case BluetoothDevice.ACTION_ACL_DISCONNECTED:
                    //블루투스가 연결되어 있지 않은 경우
                    bluetooth_state.setImageResource(R.drawable.ic_baseline_add_circle_24);
                    bluetooth_state.setBackgroundResource(R.drawable.bluetooth_state);
                    connectMsg.setVisibility(View.INVISIBLE);
                    break;
            }
        }
    };
    private void bluetoothCheck() {

        // 블루투스를 지원하지 않는다면 어플을 종료시킨다.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "이 기기는 블루투스 기능을 지원하지 않습니다.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        } else {
            if (!mBluetoothAdapter.isEnabled()) {
                // 블루투스를 지원하지만 비활성 상태인 경우
                // 블루투스를 활성 상태로 바꾸기 위해 사용자 동의 요첨
                Intent enableBtIntent = new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }else if(mBluetoothAdapter.isEnabled()){ //블루투스를 지원하고 현재, 사용가능한 경우
//                AcceptThread BTthread = new AcceptThread(pairedDevices[0]);
//                BTthread.start(); //블루투스 소켓 생성 쓰레드 실행
                showPairedDevicesListDialog();
            }
        }
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 5:
                //블루투스 활성화
                if (resultCode == RESULT_OK) {
//                    AcceptThread BTthread = new AcceptThread(pairedDevices[0]);
//                    BTthread.start();
                    showPairedDevicesListDialog();
                } else if (resultCode == RESULT_CANCELED) {

                }
        }
    }


    public void NotificationSomethings() {


        NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent = new Intent(this, SplashActivity.class);
        notificationIntent.putExtra("notificationId", count); //전달할 값
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK) ;
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,  PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher_foreground)) //BitMap 이미지 요구
                .setContentTitle("SPINELLO")
                .setContentText("🚨 자세 교정이 필요합니다!")
                // 더 많은 내용이라서 일부만 보여줘야 하는 경우 아래 주석을 제거하면 setContentText에 있는 문자열 대신 아래 문자열을 보여줌
                //.setStyle(new NotificationCompat.BigTextStyle().bigText("더 많은 내용을 보여줘야 하는 경우..."))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent) // 사용자가 노티피케이션을 탭시 ResultActivity로 이동하도록 설정
                .setAutoCancel(true);

        //OREO API 26 이상에서는 채널 필요
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            builder.setSmallIcon(R.drawable.ic_launcher_foreground); //mipmap 사용시 Oreo 이상에서 시스템 UI 에러남
            CharSequence channelName  = "노티페케이션 채널";
            String description = "오레오 이상을 위한 것임";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName , importance);
            channel.setDescription(description);

            // 노티피케이션 채널을 시스템에 등록
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);

        }else builder.setSmallIcon(R.mipmap.ic_launcher); // Oreo 이하에서 mipmap 사용하지 않으면 Couldn't create icon: StatusBarIcon 에러남

        assert notificationManager != null;
        notificationManager.notify(1234, builder.build()); // 고유숫자로 노티피케이션 동작시킴

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //설정 메뉴 만들기
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //설정 값 저장하기
            case R.id.settings:
                Intent intent = new Intent(MainActivity.this,SettingActivity.class);
                startActivity(intent);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(mConnectedTask!= null){
            mConnectedTask.cancel(true);
        }

        unregisterReceiver(mBroadcastReceiver3);
    }
    ////////////////////////////////////블루투스 연결 소켓 생성하기 (CLIENT)//////////////////////////////////////
    private class ConnectTask extends AsyncTask<Void, Void, Boolean> {

        private BluetoothSocket mBluetoothSocket = null;
        private BluetoothDevice mBluetoothDevice = null;

        ConnectTask(BluetoothDevice bluetoothDevice) {
            mBluetoothDevice = bluetoothDevice;
            mConnectedDeviceName = bluetoothDevice.getName();

            //SPP
            UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

            try {
                mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                Log.d( "TAG", "create socket for "+mConnectedDeviceName);

            } catch (IOException e) {
                Log.e( "TAG", "socket create failed " + e.getMessage());
            }

        }


        @Override
        protected Boolean doInBackground(Void... params) {

            // Always cancel discovery because it will slow down a connection
            mBluetoothAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mBluetoothSocket.connect();
            } catch (IOException e) {
                // Close the socket
                try {
                    mBluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e("TAG", "unable to close() " +
                            " socket during connection failure", e2);
                }

                return false;
            }

            return true;
        }


        @Override
        protected void onPostExecute(Boolean isSucess) {

            if ( isSucess ) {
                connected(mBluetoothSocket);
            }
            else{

                Log.d( "TAG",  "Unable to connect device");
            }
        }
    }


    public void connected( BluetoothSocket socket ) {
        mConnectedTask = new ConnectedTask(socket);
        mConnectedTask.execute();
    }



    private class ConnectedTask extends AsyncTask<Void, String, Boolean> {

        private InputStream mInputStream = null;
        private OutputStream mOutputStream = null;
        private BluetoothSocket mBluetoothSocket = null;

        ConnectedTask(BluetoothSocket socket){

            mBluetoothSocket = socket;
            try {
                mInputStream = mBluetoothSocket.getInputStream();
                mOutputStream = mBluetoothSocket.getOutputStream();
            } catch (IOException e) {
                Log.e("TAG", "socket not created", e );
            }

            Log.d( "TAG", "connected to "+mConnectedDeviceName);

        }


        @Override
        protected Boolean doInBackground(Void... params) {

            byte [] readBuffer = new byte[1024];
            int readBufferPosition = 0;


            while (true) {

                if ( isCancelled() ) return false;

                try {

                    int bytesAvailable = mInputStream.available();

                    if(bytesAvailable > 0) {

                        byte[] packetBytes = new byte[bytesAvailable];

                        mInputStream.read(packetBytes);

                        for(int i=0;i<bytesAvailable;i++) {

                            byte b = packetBytes[i];
                            if(b == '\n')
                            {
                                byte[] encodedBytes = new byte[readBufferPosition];
                                System.arraycopy(readBuffer, 0, encodedBytes, 0,
                                        encodedBytes.length);
                                String recvMessage = new String(encodedBytes, "UTF-8");

                                readBufferPosition = 0;

                                Log.d("msg", "recv message: " + recvMessage);
                                publishProgress(recvMessage);
                            }
                            else
                            {
                                readBuffer[readBufferPosition++] = b;
                            }
                        }
                    }
                } catch (IOException e) {

                    Log.e("TAG", "disconnected", e);
                    return false;
                }
            }

        }

        @Override
        protected void onProgressUpdate(String... recvMessage) {

            mConversationArrayAdapter.insert(mConnectedDeviceName + ": " + recvMessage[0], 0);
        }

        @Override
        protected void onPostExecute(Boolean isSucess) {
            super.onPostExecute(isSucess);

            if ( !isSucess ) {


                closeSocket();
                Log.d("TAG", "Device connection was lost");
            }
        }

        @Override
        protected void onCancelled(Boolean aBoolean) {
            super.onCancelled(aBoolean);

            closeSocket();
        }

        void closeSocket(){

            try {

                mBluetoothSocket.close();
                Log.d("TAG", "close socket()");

            } catch (IOException e2) {

                Log.e("TAG", "unable to close() " +
                        " socket during connection failure", e2);
            }
        }

        void write(String msg){

            msg += "\n";

            try {
                mOutputStream.write(msg.getBytes());
                mOutputStream.flush();
            } catch (IOException e) {
                Log.e("TAG", "Exception during send", e );
            }

        }
    }


    public void showPairedDevicesListDialog()
    {
        Set<BluetoothDevice> devices = mBluetoothAdapter.getBondedDevices();
        final BluetoothDevice[] pairedDevices = devices.toArray(new BluetoothDevice[0]);

        if ( pairedDevices.length == 0 ){
            Toast.makeText( this,"No devices have been paired.\n" +"You must pair it with another device.",Toast.LENGTH_LONG).show();
            return;
        }

        String[] items;
        items = new String[pairedDevices.length];
        for (int i=0;i<pairedDevices.length;i++) {
            items[i] = pairedDevices[i].getName();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select device");
        builder.setCancelable(false);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                ConnectTask task = new ConnectTask(pairedDevices[which]);
                task.execute();
            }
        });
        builder.create().show();
    }


    ///////////////////////////////////SERVER///////////////////////////////////
    class ConnectThread  extends Thread{
        SharedPreferences pref = getSharedPreferences("IP", 0);

        public void run(){
            String host =pref.getString("IP", String.valueOf(0)).toString();
            Log.d("host",host);

            int port = 5555;

            try {

                Socket socket = new Socket(host, port);
                connectState.setBackgroundResource(R.drawable.bluetooth_state_connected);
                serverMsg.setVisibility(View.INVISIBLE);
                serverConnected.setVisibility(View.VISIBLE);

                SocketAddress socketAddress = new InetSocketAddress(host,port);

                ObjectOutputStream outputStream = new ObjectOutputStream(socket.getOutputStream());
//                float[] coords = new float[6]; //xyz 2
                for (int i = 0; i < 1024; i++) {

                    outputStream.writeByte(coords[i]); //라즈베리로부터 받아온 byte 그대로 넘김.
                }
                outputStream.flush();

                ObjectInputStream inputStream = new ObjectInputStream(socket.getInputStream());
                float[] recCoords = new float[12];
                for (int i = 0; i < recCoords.length; i++) {



                    recCoords[i] = inputStream.readFloat();
                }

                inputStream.close();
                outputStream.close();
//                socket.close();
                socket.connect(socketAddress,100); //delay 설정.


            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}