package com.example.wificonnect;

import android.annotation.SuppressLint;
import android.app.ListActivity;
import android.app.Dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.*;
import java.io.*;

public class MainActivity extends ListActivity {

    class WifiReceiver extends BroadcastReceiver {
        @SuppressLint("UseValueOf")
        public void onReceive(Context c, Intent intent) {
            List<ScanResult> wifiScanList = wifi.getScanResults();
            wifis = new String[wifiScanList.size()];
            Set<String> set = new HashSet<>();
            if (wifiScanList != null) {
//                int i=0;
                for (ScanResult network : wifiScanList)
                {
                    String Capabilities =  network.capabilities;
//                    set.add(network.SSID + " MAC: " + network.BSSID);
                    set.add(network.SSID);
//                    wifis[i] = network.SSID + " capabilities : " + Capabilities;
//                    i++;
                }
            }
//            for(int i = 0; i < wifiScanList.size(); i++){
//                wifis[i] = ((wifiScanList.get(i)).toString());
//            }
//            String filtered[] = new String[wifiScanList.size()];
//            int counter = 0;
//            for (String eachWifi : wifis) {
//                String[] temp = eachWifi.split(",");
//
//                filtered[counter] = temp[0].substring(5).trim();//+"\n" + temp[2].substring(12).trim()+"\n" +temp[3].substring(6).trim();//0->SSID, 2->Key Management 3-> Strength
//
//                counter++;
//
//            }
            set.remove("");
            String[] arr = new String[set.size()+1];
            arr[0] = "Total WiFi Access Points: " + set.size();
            int i=1;
            for(String s : set){
                arr[i] = s;
                i++;
            }

            list.setAdapter(new ArrayAdapter<String>(getApplicationContext(),R.layout.list_item,R.id.label, arr));


        }
    }

    TextView tv;
    WifiManager wifi;
    WifiReceiver receiver;
    List<ScanResult> wifiList;
    StringBuilder sb = new StringBuilder();
    ListView list;
    EditText pass;
    EditText hours;
    String wifis[];
    static final int READ_BLOCK_SIZE = 100;
    HashMap<String, String> hash;
    int userCoins=30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeApp();
    }

    public void initializeApp(){
        setContentView(R.layout.activity_main);
        wifiUserPassWrite();
        wifiUserPassRead();
        list=getListView();
        //tv = (TextView) findViewById(R.id.mainText);
        wifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(!wifi.isWifiEnabled()){
            Toast.makeText(getApplicationContext(), "wifi is disable.. enable it", Toast.LENGTH_LONG);
            wifi.setWifiEnabled(true);

        }

        List<WifiConfiguration> wifiList = wifi.getConfiguredNetworks();
        for( WifiConfiguration www : wifiList ) {
            wifi.removeNetwork(www.networkId);
            wifi.saveConfiguration();
        }

        receiver = new WifiReceiver();
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifi.startScan();
        //tv.setText("Starting Scan..");

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                // selected item
                String ssid = ((TextView) view).getText().toString();
                connectToWifi(ssid);
                Toast.makeText(MainActivity.this,"Wifi SSID : "+ssid,Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onResume(){
        registerReceiver(receiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        super.onResume();
    }

    @Override
    protected void onPause(){
        unregisterReceiver(receiver);
        super.onPause();
    }

    private void finallyConnect(String networkPass, String networkSSID) {
        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", networkSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", networkPass);

        // remember id
        int netId = wifi.addNetwork(wifiConfig);
        wifi.disconnect();
        wifi.enableNetwork(netId, true);
        wifi.reconnect();

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"\"" + networkSSID + "\"\"";
        conf.preSharedKey = "\"" + networkPass + "\"";
        wifi.addNetwork(conf);
    }

    private void connectToWifi(final String wifiSSID) {
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.connect);
        dialog.setTitle("Connect to Network");
        TextView textSSID = (TextView) dialog.findViewById(R.id.textSSID1);
        textSSID.setText("Access Point: " + wifiSSID);

        TextView textCoin = (TextView) dialog.findViewById(R.id.textCoin);
        textCoin.setText("Your Account Balance: "+ userCoins+" Coins\n" +
                            "Half Hour Price: 5 Coins");

        TextView enterHours = (TextView) dialog.findViewById(R.id.enterHours);
        enterHours.setText("Enter Total Hours:");

        hours = (EditText) dialog.findViewById(R.id.hours);

        Button dialogButton = (Button) dialog.findViewById(R.id.okButton);


        // if button is clicked, connect to the network;
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int deductForHour = 1;
                try{
                    Integer.parseInt(hours.getText().toString());
                    deductForHour = Integer.parseInt(hours.getText().toString());
                }
                catch(Exception e){

                }
                int totalDeduction = deductForHour*5;
                if(userCoins - totalDeduction >= 0){
                    userCoins -= totalDeduction;
//                String checkPassword = pass.getText().toString();
                    String checkPassword = hash.get(wifiSSID);
                    finallyConnect(checkPassword, wifiSSID);
                    Toast.makeText(MainActivity.this,"Coins deducted : "+totalDeduction+"\nNew Balance: "+userCoins,Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(MainActivity.this,"Not Enough Coins to make the purchase",Toast.LENGTH_SHORT).show();
                }

                dialog.dismiss();
            }
        });
        dialog.show();
    }

    public void wifiUserPassWrite() {
        // add-write text into file
        try {
            FileOutputStream fileout=openFileOutput("wifiUserPass.txt", MODE_PRIVATE);
            OutputStreamWriter outputWriter=new OutputStreamWriter(fileout);
            String keyValue = "iPhone=23f5xywbpssta\n" +
                    "Jericho's iPhone=mag3nt132r1mehdr\n";
            outputWriter.write(keyValue);
            outputWriter.close();

            //display file saved message
//            Toast.makeText(getBaseContext(), "File saved successfully!",
//                    Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Read text from file
    public void wifiUserPassRead() {
        //reading text from file
        try {
            FileInputStream fileIn=openFileInput("wifiUserPass.txt");
            InputStreamReader InputRead= new InputStreamReader(fileIn);

            char[] inputBuffer= new char[READ_BLOCK_SIZE];
            String s="";
            int charRead;

            while ((charRead=InputRead.read(inputBuffer))>0) {
                // char to string conversion
                String readstring=String.copyValueOf(inputBuffer,0,charRead);
                s +=readstring;
            }
            InputRead.close();
            String[] str = s.split("\n");
            hash = new HashMap<>();
            for(String ss : str){
                hash.put(ss.split("=")[0], ss.split("=")[1]);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
