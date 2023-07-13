package org.techtown.a0509;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;

import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import android.graphics.Color;


import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import java.io.IOException;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import app.akexorcist.bluetotohspp.library.DeviceList;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.charts.LineChart;

import android.annotation.SuppressLint;
import android.graphics.Color;

import android.os.Bundle;
import android.app.Activity;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import android.view.View;
import androidx.annotation.RequiresApi;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

@RequiresApi(api = Build.VERSION_CODES.KITKAT)

public class MainActivity extends AppCompatActivity {

    final static String foldername = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+"/EXERCISEDATA";
    final static String datename = new SimpleDateFormat("yyyy-MM").format(new Date());
    final static String filename = datename+".txt";

    //    final static String foldername2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)+"/TIMEDATA";
    final static String datename2 = new SimpleDateFormat("MM-dd").format(new Date());
    final static String filename2 = datename2+".txt";
    final static String TodayDate = new SimpleDateFormat("dd").format(new Date());



    private ArrayList<Entry> raw_values = new ArrayList<>();
    private ArrayList<Entry> time_values = new ArrayList<>();

    TextView ultras;
    private LineChart mChart;
    private LineChart TChart;
    private LineChart FChart;
    private LineChart AChart;


    private Thread thread;
    private boolean plotData = true;

    Boolean connecting = false;

    ImageView img;

    static final int REQUEST_ENABLE_BT = 10;
    BluetoothAdapter mBluetoothAdapter;
    int mPairedDeviceCount = 0;
    Set<BluetoothDevice> pairedDevices;
    BluetoothDevice mRemoteDevice;
    BluetoothSocket mSocket = null;
    OutputStream mOutputStream = null;
    InputStream mInputStream = null;

    Thread mWorkerThread = null;

    byte[] readBuffer;
    int bufferPosition;

    int ButtonState = 0;

    Timer m_timer = new Timer();

    int j = 0;

    int Check = 0;


    private Context mContext;

//    ArrayList<String> DateLabel = new ArrayList<>();

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);

        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }


//    출처: https://wkdgusdn3.tistory.com/entry/Android-ScrollView안에-ListVIew-넣을-시-Height-문제 [장삼의 착한코딩]



//    private TextView textView1;

    @SuppressLint("ResourceAsColor")
    @Override


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {

            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    //블루투스가 활성 상태로 변경됨
                    selectPairedDevice();
                } else if (resultCode == RESULT_CANCELED) {
                    //블루투스가 비활성 상태임
                    finish(); // 애플리케이션 종료
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

    void activateBluetooth() {
        deleteFile("rList");

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //getDefaultAdapter() 메소드는 장치가 블루투스를 지원하지 않으면 null 값을 리턴함
        if (mBluetoothAdapter == null) { // 장치가 블루투스를 지원하지 않으면
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않습니다!", Toast.LENGTH_SHORT);
            finish();
        } else { // 장치가 블루투스를 지원하면

            //블루투스가 비활성화되어있으면 isEnable()메소드는 false를 리턴한다.
            if (!mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            } else { //블루투스를 지원하고 장치가 활성 상태인 경우우

                //블루투스 페어링 된 목록을 보고 연결할 블루투스를 선택한다.

                selectPairedDevice();
            }
        }
    }

    void selectPairedDevice() {
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        mPairedDeviceCount = pairedDevices.size();

        if (mPairedDeviceCount == 0) {
            Toast.makeText(getApplicationContext(), "페어링 된 장치가 없습니다!", Toast.LENGTH_SHORT);
            finish();
        }

        final List<String> listDevices = new ArrayList<String>();
        for (BluetoothDevice device : pairedDevices) {
            listDevices.add(device.getName());
        }

        ArrayAdapter mAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, listDevices);
        final ListView listView = findViewById(R.id.listview);
        listView.setAdapter(mAdapter);
        setListViewHeightBasedOnChildren(listView);

        final String[] items = listDevices.toArray(new String[listDevices.size()]);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (connecting == false)
                    connectToBluetoothDevice(items[position]);

                connecting = true;
                TextView Bluetooth = findViewById(R.id.selectBT);
                Bluetooth.setText("블루투스가 연결되었습니다. 측정을 시작해주세요.");
                findViewById(R.id.listview).setVisibility(View.INVISIBLE);
            }
        });


    }

    void receiveData() {
        final Handler handler = new Handler();

//        ultras_values.add(0f);

        readBuffer = new byte[1024]; //수신 버퍼
        bufferPosition = 0; //버퍼 내 수신 문자 저장 위치



        //문자열 수신 쓰레드
        mWorkerThread = new Thread(new Runnable() {


            TextView ultras = findViewById(R.id.ultras);


            public void run() {
                while (!Thread.currentThread().isInterrupted()) {

                    try {
                        int bytesAvailable = mInputStream.available();
                        // 수신 데이터 크기를 bytesAvailable에 저장

                        if (bytesAvailable > 0) { //수신한 데이터가 있으면
                            byte[] packetBytes = new byte[bytesAvailable];
                            mInputStream.read(packetBytes);
                            //스트림을 사용하여 packetBytes에 데이터 넣기

                            int i = 0;
                            int flag = 0;

                            while ((flag==0) && (i < bytesAvailable)) {

                                if (packetBytes[i] == '\n') {

                                    j = j+1;

                                    flag = 1;



                                    final String data = new String(readBuffer, "US-ASCII");
                                    String[] array = data.split(",");

                                    bufferPosition = 0;

                                    handler.post(new Runnable() {
                                        @SuppressLint("ResourceAsColor")
                                        public void run() {


                                            toss(array[0]);

                                            // raw 데이터 외부 저장소에 실시간으로 txt로 저장하기
                                            String now = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                                            String contents = now+" data:"+array[0]+"kg"+"\n";

                                            WriteTextFile(foldername, filename, contents);



                                            //데이터 내부저장소에 저장 시도해보기

                                            toss(array[0]);

                                            if(ButtonState == 1) {


                                                InFileSave(datename2, array[0]);
                                                ultras.setText("현재 힘: " +array[0]+"kg");

                                            }else{
                                                ultras.setText("운동 중이 아닙니다");
                                            }

                                        }

                                    });
                                } else {
                                    readBuffer[bufferPosition++] = packetBytes[i];
                                }

                                i += 1;

                            }  //end of for
                        }
                    } catch (IOException ex) {
                        //데이터 수신 중 오류 발생
                        finish();
                    }
                }
            }
        });

        mWorkerThread.start();
    }

    void transmitData(String msg) {
        msg += "\n";

        try {

            mOutputStream.write(msg.getBytes());  //문자열 전송
        } catch (Exception e) {
            //오류가 발생한 경우
            finish(); //액티비티 종료
        }
    }

    BluetoothDevice getDeviceFromBondedList(String name) {
        BluetoothDevice selectedDevice = null;

        for (BluetoothDevice device : pairedDevices) {
            if (name.equals(device.getName())) {
                selectedDevice = device;
                break;
            }
        }

        return selectedDevice;
    }

    @Override
    protected void onDestroy() {
        try {
            mWorkerThread.interrupt();
            mInputStream.close();
            mOutputStream.close();
            mSocket.close();
        } catch (Exception e) {
        }


        super.onDestroy();
    }

    void connectToBluetoothDevice(String selectedDeviceName) {
        mRemoteDevice = getDeviceFromBondedList(selectedDeviceName);
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        try {
            //createRfcommSocketToServiceRecord를 사용하여 소켓 생성하기
            mSocket = mRemoteDevice.createRfcommSocketToServiceRecord(uuid);

            //connect 메소드를 통해 시스템이 UUID와 일치하는 장치를 찾는다.
            //소켓을 사용하여 장치에 접속한다.
            mSocket.connect();

            //데이터 송수신을 위한 스트림 객체얻기
            mOutputStream = mSocket.getOutputStream();
            mInputStream = mSocket.getInputStream();

            //데이터를 블루투스로 부터 수신할 수 있도록 하기
            receiveData();
        } catch (Exception e) {
            //블루투스 연결 중 오류 발생시
            Toast.makeText(getApplicationContext(), "connect error", Toast.LENGTH_SHORT).show();
            finish(); //앱종료
        }
    }






    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        activateBluetooth();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);


        Button btnStop = (Button) findViewById(R.id.btnStop);
        Button btnStart = (Button) findViewById(R.id.btnStart);

        Context context = this;
        String[] files = context.fileList();

//        시작하자마자 운동하지 않은날 계산
        if(files.length >= 1){
            createLazyData();
        }else{
            Check = 1;

        }


        btnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                ButtonState = 1;
                btnStart.setVisibility(View.GONE);
                btnStop.setVisibility(View.VISIBLE);


            }
        });

        btnStop.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){

                //버튼 누르면 저장 멈춤, 계산뒤 반환해주기.


                ButtonState = 0;


                if(Check ==1) {
                    RecordCalculate();
                }

                btnStart.setVisibility(View.VISIBLE);
                btnStop.setVisibility(View.GONE);
            }
        });





    }

    public void createLazyData() {
        // 운동하지 않은 날 계산하기

        //deleteFile("rList");

        int lazy = 0;
        int check = 0;

        Context context = this;
        String[] files = context.fileList();
        List TfilesbyNum = new ArrayList<String>();

//        deleteFile("rList");
//        check = 1;
//
//
//        if (check == 1) {
//            for (int j = 0; j < files.length-1; j++) {
//                TfilesbyNum.add(files[j]);
//            }

        int valid_Flength = files.length;

        for (int j = 0; j < valid_Flength; j++) {



            if(files[j].compareTo("12-31") > 0){
                //deleteFile();
                // TfilesbyNum.add(files[j++]);
                valid_Flength = j;
                j++;
            }else{
                TfilesbyNum.add(files[j]);
            }


        }

            Collections.sort(TfilesbyNum, cmpAsc);

            String[] FilesbyNum = (String[]) TfilesbyNum.toArray(new String[TfilesbyNum.size()]);

//            Date dDate = new Date();
//            dDate = new Date(dDate.getTime() + (1000 * 60 * 60 * 24 * -1));
//            SimpleDateFormat dSdf = new SimpleDateFormat("MM-dd");
//            String yesterday = dSdf.format(dDate);

            Date d = new Date();
            SimpleDateFormat Todayd = new SimpleDateFormat("MM-dd");
            String Today = Todayd.format(d);
            String[] DateString = Today.split("-");

            int month = Integer.parseInt(DateString[0]);
            int date = Integer.parseInt(DateString[1]);

            int YestM = month;
            int YesD = date - 1;

            if (date - 1 < 1) {
                switch (month) {
                    case 1:
                        YestM = 12;
                        YesD = 31;
                        break;
                    case 2:
                        YestM = month - 1;
                        YesD = 31;
                        break;
                    case 3:
                        YestM = month - 1;
                        YesD = 28;
                        break;
                    case 4:
                        YestM = month - 1;
                        YesD = 31;
                        break;
                    case 5:
                        YestM = month - 1;
                        YesD = 30;
                        break;
                    case 6:
                        YestM = month - 1;
                        YesD = 31;
                        break;
                    case 7:
                        YestM = month - 1;
                        YesD = 30;
                        break;
                    case 8:
                        YestM = month - 1;
                        YesD = 31;
                        break;
                    case 9:
                        YestM = month - 1;
                        YesD = 31;
                        break;
                    case 10:
                        YestM = month - 1;
                        YesD = 30;
                        break;
                    case 11:
                        YestM = month - 1;
                        YesD = 31;
                        break;
                    case 12:
                        YestM = month - 1;
                        YesD = 30;
                        break;

                }
            } else {
                YestM = month;
                YesD = date - 1;
            }

            String yesterday = Integer.toString(YestM) + "-" + Integer.toString(YesD);

            if (FilesbyNum[FilesbyNum.length - 1].equals(Today)) {
                Check = 1;
            }

            else if(FilesbyNum[FilesbyNum.length - 1].equals(yesterday)){
                Check = 1;
            }

            else{

                String[] S1 = FilesbyNum[FilesbyNum.length - 1].split("-");
                String[] S2 = datename2.split("-");
                int myflag = Integer.parseInt(S1[0]);


                if (Integer.parseInt(S1[0]) == Integer.parseInt(S2[0])) {

                    lazy = Integer.parseInt(S2[1]) - Integer.parseInt(S1[1]) - 1;


                    for (int s = 0; s < lazy; s++) {
                        int adDate = Integer.parseInt(S1[1]) + s + 1;
                        if (adDate < 10) {
                            InFileSave((S2[0] + "-" + 0 + adDate), "0");
                        } else {
                            InFileSave((S2[0] + "-" + adDate), "0");
                        }

                    }
                }

                //월이 다르고
                else {
                    if (Integer.parseInt(S1[0]) != Integer.parseInt(S2[0])) {
                        if (Integer.parseInt(S1[0]) <= 7) {

                            if (Integer.parseInt(S1[0]) == 2) {
                                lazy = Integer.parseInt(S2[1]) + 27 - Integer.parseInt(S1[1]);


                                for (int s = 0; s < lazy; s++) {
                                    int adDate = Integer.parseInt(S1[1]) + s + 1;
                                    if (adDate <= 28) {
                                        if (adDate < 10) {
                                            InFileSave((S1[0] + "-" + 0 + adDate), "0");
                                        } else {
                                            InFileSave((S1[0] + "-" + adDate), "0");
                                        }
                                    } else {
                                        adDate = adDate - 28;
                                        if (adDate < 10) {
                                            InFileSave((S2[0] + "-" + 0 + adDate), "0");
                                        } else {
                                            InFileSave((S2[0] + "-" + adDate), "0");
                                        }
                                    }
//                        TextView flagView = findViewById(R.id.show3);
//                        flagView.setText("여기까진됨");
                                }

                                //월이 2, 4, 6월일때.30
                            } else if (Integer.parseInt(S1[0]) % 2 == 0) {
                                lazy = Integer.parseInt(S2[1]) + 29 - Integer.parseInt(S1[1]);

                                for (int s = 0; s < lazy; s++) {
                                    int adDate = Integer.parseInt(S1[1]) + s + 1;
                                    if (adDate <= 30) {
                                        if (adDate < 10) {
                                            InFileSave((S1[0] + "-" + 0 + adDate), "0");
                                        } else {
                                            InFileSave((S1[0] + "-" + adDate), "0");
                                        }
                                    } else {
                                        adDate = adDate - 30;
                                        if (adDate < 10) {
                                            InFileSave((S2[0] + "-" + 0 + adDate), "0");
                                        } else {
                                            InFileSave((S2[0] + "-" + adDate), "0");
                                        }
                                    }
//                        TextView flagView = findViewById(R.id.show3);
//                        flagView.setText("여기까진됨");
                                }


                            }
                            //월이 1, 3, 5, 7월일때.31
                            else {
                                lazy = Integer.parseInt(S2[1]) + 30 - Integer.parseInt(S1[1]);
                                for (int s = 0; s < lazy; s++) {
                                    int adDate = Integer.parseInt(S1[1]) + s + 1;
                                    if (adDate <= 31) {
                                        if (adDate < 10) {
                                            InFileSave((S1[0] + "-" + 0 + adDate), "0");
                                        } else {
                                            InFileSave((S1[0] + "-" + adDate), "0");
                                        }
                                    } else {
                                        adDate = adDate - 31;
                                        if (adDate < 10) {
                                            InFileSave((S2[0] + "-" + 0 + adDate), "0");
                                        } else {
                                            InFileSave((S2[0] + "-" + adDate), "0");
                                        }
                                    }
//
                                }

                            }
                        }

                        if (Integer.parseInt(S1[0]) > 7) {
                            //8, 10, 12월일때.30
                            if (Integer.parseInt(S1[0]) % 2 == 0) {
                                lazy = Integer.parseInt(S2[1]) + 30 - Integer.parseInt(S1[1]);
                                for (int s = 0; s < lazy; s++) {
                                    int adDate = Integer.parseInt(S1[1]) + s + 1;
                                    if (adDate <= 30) {
                                        if (adDate < 10) {
                                            InFileSave((S1[0] + "-" + 0 + adDate), "0");
                                        } else {
                                            InFileSave((S1[0] + "-" + adDate), "0");
                                        }
                                    } else {
                                        adDate = adDate - 30;
                                        if (adDate < 10) {
                                            InFileSave((S2[0] + "-" + 0 + adDate), "0");
                                        } else {
                                            InFileSave((S2[0] + "-" + adDate), "0");
                                        }
                                    }
//                        TextView flagView = findViewById(R.id.show3);
//                        flagView.setText("여기까진됨");
                                }

                            }
                            //9, 11월일때때.31
                            else {
                                lazy = Integer.parseInt(S2[1]) + 29 - Integer.parseInt(S1[1]);
                                for (int s = 0; s < lazy; s++) {
                                    int adDate = Integer.parseInt(S1[1]) + s + 1;
                                    if (adDate <= 31) {
                                        if (adDate < 10) {
                                            InFileSave((S1[0] + "-" + 0 + adDate), "0");
                                        } else {
                                            InFileSave((S1[0] + "-" + adDate), "0");
                                        }
                                    } else {
                                        adDate = adDate - 31;
                                        if (adDate < 10) {
                                            InFileSave((S2[0] + "-" + 0 + adDate), "0");
                                        } else {
                                            InFileSave((S2[0] + "-" + adDate), "0");
                                        }
                                    }
//                        TextView flagView = findViewById(R.id.show3);
//                        flagView.setText("여기까진됨");
                                }
                            }

                        }

                    }
                }
                //월이 같다


            }


            Check = 1;
        }
    //}



    public void RecordCalculate(){
        int flag;
        int k = 0;

        Context context = this;
        String[] files = context.fileList();
        List TfilesbyNum = new ArrayList<String>();

        Date dDate = new Date();
        dDate = new Date(dDate.getTime()+(1000*60*60*24*-1));
        SimpleDateFormat dSdf = new SimpleDateFormat("MM-dd");
        String yesterday = dSdf.format(dDate);

        //날짜별로 정렬

        for(int j = 0; j<files.length; j++)
        {
            TfilesbyNum.add(files[j]);
        }

        Collections.sort(TfilesbyNum,cmpAsc);

        String[] FilesbyNum = (String[]) TfilesbyNum.toArray(new String[TfilesbyNum.size()]);


        flag = files.length;


        //운동 시간 데이터 셋만들기

        int[] TimedataSet = new int[files.length];
        List<Entry> TimeEntryset = new ArrayList<>();
        ArrayList<String> DateLabel = new ArrayList<>();


//        운동 평균 힘 크기 데이터 셋 만들기
        float[] avgFdataSet = new float[files.length];
        List<Entry> avgFEntryset = new ArrayList<>();
//        ArrayList<String> FDateLabel = new ArrayList<>();

        //힘*시간 데이터셋
        double[] areadataSet = new double[files.length];
        List<Entry> areaEntryset = new ArrayList<>();

        for(int i = 0; i<files.length; i++)
        {
            TimedataSet[i] = getTimeRecord((String) TfilesbyNum.get(i))/10;
//            avgFdataSet[i] = getTotalF((String) TfilesbyNum.get(i))/TimedataSet[i];
            areadataSet[i] = getTotalF((String) TfilesbyNum.get(i));


            if(TimedataSet[i]==0){
                avgFdataSet[i] = 0;
            }else{
                avgFdataSet[i] = getavgF((String) TfilesbyNum.get(i));
            }


            TimeEntryset.add(new Entry(i , TimedataSet[i]));
            avgFEntryset.add(new Entry(i, avgFdataSet[i]));
            areaEntryset.add(new Entry(i, (float) areadataSet[i]));
            k = i+1;

            DateLabel.add((String) TfilesbyNum.get(i));

        }
        createDataSet(TimeEntryset, avgFEntryset, areaEntryset, DateLabel,flag);






    }



    //compare class 함수
    Comparator<String> cmpAsc = new Comparator<String>() {
        @Override
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    } ;




    public int getTimeRecord(String RFilename){
        String strTmp;
        String[] needString;
        int Time;

        try {
            FileInputStream fis = openFileInput(RFilename);
            StringBuffer sb = new StringBuffer();
            byte dataBuffer[] = new byte[1024];
            int n = 0;

            while ((n = fis.read(dataBuffer)) != -1) {
                sb.append(new String(dataBuffer));
            }

            strTmp = sb.toString();
            needString = strTmp.split("\n");
            Time = needString.length;
            fis.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return 0;

        }

        return Time;



    }

    public float getavgF (String RFilename){
        String strTmp;
        String[] needString;
        float totalF = 0;
        int Time;
        float myResult = 0;

        try {
            FileInputStream fis = openFileInput(RFilename);
            StringBuffer sb = new StringBuffer();
            byte dataBuffer[] = new byte[1024];
            int n = 0;

            while ((n = fis.read(dataBuffer)) != -1) {
                sb.append(new String(dataBuffer));
            }

            strTmp = sb.toString();
            needString = strTmp.split("\n");
            Time = needString.length;

            for(int i = 0; i<Time-1; i++){

                totalF += Float.parseFloat(needString[i]);
            }

            myResult = totalF/Time;



//            Time = needString.length;
            fis.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return 0;

        }

        return myResult;



    }

    public double getTotalF (String RFilename){
        String strTmp;
        String[] needString;
        float totalF = 0;
        int Time;
        double myResult = 0;


        try {
            FileInputStream fis = openFileInput(RFilename);
            StringBuffer sb = new StringBuffer();
            byte dataBuffer[] = new byte[1024];
            int n = 0;

            while ((n = fis.read(dataBuffer)) != -1) {
                sb.append(new String(dataBuffer));
            }

            strTmp = sb.toString();
            needString = strTmp.split("\n");
            Time = needString.length;

            for(int i = 0; i<Time-1; i++){

                totalF += Float.parseFloat(needString[i]);
            }

            myResult = totalF*0.1;



//            Time = needString.length;
            fis.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return 0;

        }

        return myResult;



    }



    private void createDataSet(List TimeEntryset, List avgFEntryset, List areaEntryset, List DateLabel, int filenum) {


        //시간
        LineDataSet TimeDataSet = new LineDataSet(TimeEntryset, "운동한 시간(s)");



        LineData TimeData = new LineData(TimeDataSet);


        TimeData.setValueTextColor(Color.BLACK);
        TimeData.setValueTextSize(13f);




        TimeDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        TimeDataSet.setColor(Color.BLUE);//라인색
        TimeDataSet.setLineWidth(2f);
        TimeDataSet.setFillColor(Color.RED);
        // set.Mode(LineDataSet.Mode.CUBIC_BEZIER);
        TimeDataSet.setCubicIntensity(1f);
//        return TimeDataSet;

        TimeChartSetting(TimeData, DateLabel, filenum);


        //평균힘
        LineDataSet avgFDataSet = new LineDataSet(avgFEntryset, "평균 운동 힘(kg)");
        LineData avgFData = new LineData(avgFDataSet);
        avgFData.setValueTextColor(Color.BLACK);
        avgFData.setValueTextSize(13f);




        avgFDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        avgFDataSet.setColor(Color.BLUE);//라인색
        avgFDataSet.setLineWidth(2f);
        avgFDataSet.setFillColor(Color.RED);
        avgFDataSet.setCubicIntensity(0.2f);

        avgFChartSetting(avgFData, DateLabel, filenum);

        //면적
        LineDataSet areaDataSet = new LineDataSet(areaEntryset, "운동강도*시간(kg*s)");
        LineData areaData = new LineData(areaDataSet);
        areaData.setValueTextSize(13f);
        areaData.setValueTextColor(Color.BLACK);
        areaDataSet.setAxisDependency(YAxis.AxisDependency.LEFT);
        areaDataSet.setColor(Color.BLUE);//라인색
        areaDataSet.setLineWidth(2f);
        areaDataSet.setFillColor(Color.RED);
        areaDataSet.setCubicIntensity(0.2f);

        areaChartSetting(areaData, DateLabel, filenum);

    }

    @SuppressLint("ResourceAsColor")
    void TimeChartSetting(LineData Timedata, List DateLabel, int filenum){

        TChart = (LineChart) findViewById(R.id.timechart);
        TChart.setData(Timedata);
        TChart.getDescription().setEnabled(false);


        //속도 센서 이외에 조작 방지
        TChart.setTouchEnabled(true); //터치
        TChart.setDragEnabled(true); //드래그
        TChart.setScaleXEnabled(false); //확장
        TChart.setDrawGridBackground(false);
        TChart.setPinchZoom(false);
        TChart.setBackgroundColor(android.R.color.white);
        TChart.notifyDataSetChanged();
        TChart.invalidate();
        TChart.setVisibleXRange(0,6);

//        TChart.setVisibleXRangeMaximum(6);

//        TChart.setMaxVisibleValueCount(7);






        //축생성
        XAxis xAxis = TChart.getXAxis(); //아래
        xAxis.setPosition((XAxis.XAxisPosition.BOTTOM));
        xAxis.setTextSize(10f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(DateLabel));
        xAxis.setDrawGridLines(false);
//        xAxis.setLabelCount(filenum,true);

//        if(filenum>=8){
//            TChart.setVisibleXRangeMaximum(6);
//        }else{
//            xAxis.setLabelCount(filenum,false);
//        }


//        if(filenum>=8){
//            xAxis.setLabelCount(8, true);
//        }else{
//            xAxis.setLabelCount(filenum,true);
//        }

//        if(filenum>=8){
//            TChart.getAxisLeft().setLabelCount(7, true);
//        }else{
//            TChart.getAxisLeft().setLabelCount(filenum, true);
//        }

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,DateLabel);







        YAxis leftAxis = TChart.getAxisLeft();//왼쪽
        //leftAxis.setDrawGridLines(false);


        YAxis rightAxis = TChart.getAxisRight(); //오른쪽
        rightAxis.setEnabled(false);//없앰

        Legend legend = TChart.getLegend();
        legend.setTextSize(14f);
    }






    @SuppressLint("ResourceAsColor")
    void avgFChartSetting(LineData avgFdata, List DateLabel, int filenum){
        FChart = (LineChart) findViewById(R.id.avgFchart);
        FChart.getDescription().setEnabled(false);
//        mChart.getDescription().setText("Real Time Distance Data");
        FChart.setData(avgFdata);

        //속도 센서 이외에 조작 방지
        FChart.setTouchEnabled(true); //터치
        FChart.setDragEnabled(true); //드래그
        FChart.setScaleXEnabled(false); //확장
        FChart.setDrawGridBackground(false);
        FChart.setPinchZoom(false);
        FChart.setBackgroundColor(android.R.color.white);
        FChart.setVisibleXRange(0,6);
//        FChart.notifyDataSetChanged();
//        FChart.invalidate();

        //축생성
        XAxis xAxis = FChart.getXAxis(); //아래
        xAxis.setPosition((XAxis.XAxisPosition.BOTTOM));

        xAxis.setTextSize(10f);

        xAxis.setValueFormatter(new IndexAxisValueFormatter(DateLabel));

        xAxis.setDrawGridLines(false);

        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,DateLabel);


        YAxis leftAxis = FChart.getAxisLeft();//왼쪽
        //leftAxis.setDrawGridLines(false);


        YAxis rightAxis = FChart.getAxisRight(); //오른쪽
        rightAxis.setEnabled(false);//없앰

        Legend legend = FChart.getLegend();
        legend.setTextSize(14f);
    }

    @SuppressLint("ResourceAsColor")
    void areaChartSetting(LineData areadata, List DateLabel, int filenum){
        AChart = (LineChart) findViewById(R.id.Areachart);
        AChart.getDescription().setEnabled(false);
        AChart.setData(areadata);
//        mChart.getDescription().setText("Real Time Distance Data");

        //속도 센서 이외에 조작 방지
        AChart.setTouchEnabled(true); //터치
        AChart.setDragEnabled(true); //드래그
        AChart.setScaleXEnabled(false); //확장
        AChart.setDrawGridBackground(false);
        AChart.setPinchZoom(false);
        AChart.setBackgroundColor(android.R.color.white);
        AChart.setVisibleXRange(0,6);

        //축생성
        XAxis xAxis = AChart.getXAxis(); //아래
        xAxis.setPosition((XAxis.XAxisPosition.BOTTOM));
        xAxis.setTextSize(10f);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(DateLabel));
        xAxis.setDrawGridLines(false);


        ArrayAdapter adapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1,DateLabel);

//                                            xAxis.setLabelCount(300,true);
        //xAxis.setDrawGridLines(false);

        YAxis leftAxis = AChart.getAxisLeft();//왼쪽
        //leftAxis.setDrawGridLines(false);


        YAxis rightAxis = AChart.getAxisRight(); //오른쪽
        rightAxis.setEnabled(false);//없앰

        Legend legend = AChart.getLegend();
        legend.setTextSize(14f);
    }




    //텍스트내용을 경로의 텍스트 파일에 쓰기
    public void WriteTextFile(String foldername, String filename, String contents){
        try{
            File dir = new File (foldername);
            //디렉토리 폴더가 없으면 생성함
            if(!dir.exists()){
                dir.mkdir();
            }
            //파일 output stream 생성
            FileOutputStream fos = new FileOutputStream(foldername+"/"+filename, true);
            //파일쓰기
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(fos));
            writer.write(contents);
            writer.flush();

            writer.close();
            fos.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void InFileSave(String date, String data) {
        try {
            FileOutputStream fos = openFileOutput(date, Context.MODE_APPEND);
            String strText = data + "\n";
            fos.write(strText.getBytes());
            fos.close();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
            return;
        }

//        Toast.makeText(getApplicationContext(), "파일 저장이 완료 되었습니다.", Toast.LENGTH_SHORT).show();
    }




    public String toss(String data){

        return data;
    }




}
