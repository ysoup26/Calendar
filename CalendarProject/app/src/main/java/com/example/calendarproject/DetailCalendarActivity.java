package com.example.calendarproject;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class DetailCalendarActivity extends AppCompatActivity implements OnMapReadyCallback {
    GoogleMap mGoogleMap = null;
    private CalDBHelper mDbHelper;
    private boolean OldCal;
    String CAL_info;
    String HOUR_info;
    String INDEX;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_calendar);

        mDbHelper = new CalDBHelper(this);
        Dialog delDialog=new Dialog(DetailCalendarActivity.this);
        delDialog.setContentView(R.layout.del_dialog);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        initPicker(); //시간 표현하는 Picker 초기화
        Intent i = getIntent();
        CAL_info = i.getStringExtra("cal");
        HOUR_info = i.getStringExtra("hour");
        INDEX = i.getStringExtra("indexN");

        OldCal = findOneRecord(CAL_info,HOUR_info); //새로운 일정을 추가하는건지 확인

        EditText Title = findViewById(R.id.detail_title);

        if(!OldCal) {
            Title.setText(CAL_info+HOUR_info);
            //시간 설정-시작은 선택한 지금 시간으로, 종료는 +1
            newPickerSet();
        }
        Button findBtn = findViewById(R.id.findButton);

        Button saveBtn = findViewById(R.id.saveButton);
        Button cancelBtn = findViewById(R.id.cancelButton);
        Button deleteBtn = findViewById(R.id.deleteButton);


        findBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getAddress();
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(OldCal)
                    updateRecord(CAL_info,HOUR_info);
                else
                    insertRecord(CAL_info,HOUR_info);
                ResultTitleToMain();
                finish();
            }
        });
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //onClickShowAlert();
                delDialog.show();
                Button delBtn=delDialog.findViewById(R.id.del_dialog_delete);
                Button celBtn=delDialog.findViewById(R.id.del_dialog_cancel);
                delBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        deleteRecord(CAL_info,HOUR_info);
                        DeletedToMain();
                        finish();
                    }
                });
                celBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delDialog.cancel();
                    }
                });
            }
        });

    }
    public void newPickerSet(){
        String []hinfo=HOUR_info.split("시");
        int Hour= Integer.parseInt(hinfo[0]);
        int startAmPm=0;
        int endAmPm=0;
        if(Hour==11) { //정확한 am pm을 표시하기 위한 부분
            startAmPm = 0;
            endAmPm = 1;
        }else if(Hour==23){
            endAmPm=0;
            startAmPm=1;
        }else if(Hour>=12){
            endAmPm= 1;
            startAmPm=1;
        }
        setPicker((Hour%12)+" 0 "+startAmPm,((Hour+1)%12)+" 0 "+endAmPm);
    }

    private void initPicker(){
        NumberPicker startH = (NumberPicker)findViewById(R.id.start_hour);
        NumberPicker startM = (NumberPicker)findViewById(R.id.start_minute);
        NumberPicker startS = (NumberPicker)findViewById(R.id.start_set);
        NumberPicker endH = (NumberPicker)findViewById(R.id.end_hour);
        NumberPicker endM = (NumberPicker)findViewById(R.id.end_minute);
        NumberPicker endS = (NumberPicker)findViewById(R.id.end_set);
        startH.setMinValue(0);
        startH.setMaxValue(11);
        startH.setWrapSelectorWheel(false);
        startM.setMinValue(0);
        startM.setMaxValue(59);
        startM.setWrapSelectorWheel(false);
        startS.setMinValue(0);
        startS.setMaxValue(1);
        startS.setDisplayedValues(new String[]{"AM", "PM"});
        startS.setWrapSelectorWheel(false);

        endH.setMinValue(0);
        endH.setMaxValue(11);
        endH.setWrapSelectorWheel(false);
        endM.setMinValue(0);
        endM.setMaxValue(59);
        endM.setWrapSelectorWheel(false);
        endS.setMinValue(0);
        endS.setMaxValue(1);
        endS.setDisplayedValues(new String[]{"AM", "PM"});
        endS.setWrapSelectorWheel(false);
    }
    private void setPicker(String start,String end){
        String[] Start=start.split(" ");
        String[] End=end.split(" ");
        NumberPicker startH = (NumberPicker)findViewById(R.id.start_hour);
        NumberPicker startM = (NumberPicker)findViewById(R.id.start_minute);
        NumberPicker startS = (NumberPicker)findViewById(R.id.start_set);
        NumberPicker endH = (NumberPicker)findViewById(R.id.end_hour);
        NumberPicker endM = (NumberPicker)findViewById(R.id.end_minute);
        NumberPicker endS = (NumberPicker)findViewById(R.id.end_set);
        startH.setValue(Integer.parseInt(Start[0]));
        startM.setValue(Integer.parseInt(Start[1]));
        startS.setValue(Integer.parseInt(Start[2]));
        endH.setValue(Integer.parseInt(End[0]));
        endM.setValue(Integer.parseInt(End[1]));
        endS.setValue(Integer.parseInt(End[2]));
    }
    private String getPicker(String SorE){
        if(SorE.equals("start")){
            NumberPicker startH = (NumberPicker)findViewById(R.id.start_hour);
            NumberPicker startM = (NumberPicker)findViewById(R.id.start_minute);
            NumberPicker startS = (NumberPicker)findViewById(R.id.start_set);
            return startH.getValue()+" "+startM.getValue()+" "+startS.getValue()+" ";
        }else{
            NumberPicker endH = (NumberPicker)findViewById(R.id.end_hour);
            NumberPicker endM = (NumberPicker)findViewById(R.id.end_minute);
            NumberPicker endS = (NumberPicker)findViewById(R.id.end_set);
            return endH.getValue()+" "+endM.getValue()+" "+endS.getValue()+" ";
        }
    }
    private String Picker12to24(String hms){
        String []HMS=hms.split(" ");
        int hour= Integer.parseInt(HMS[0]);
        if(HMS[2].equals("1"))//PM이 1임
            hour+=12;
        return String.valueOf(hour);
    }
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        getAddress();
    }
    private void getAddress() {

        EditText Address = (EditText) findViewById(R.id.address);
        String input = String.valueOf(Address.getText());
        Geocoder geocoder = new Geocoder(DetailCalendarActivity.this, Locale.KOREA);
        try {
            List<Address> addresses = geocoder.getFromLocationName(input, 1);
            if (addresses.size() > 0) {
                Address bestResult = (Address) addresses.get(0);
                if (mGoogleMap != null) {

                    LatLng location = new LatLng(bestResult.getLatitude(), bestResult.getLongitude());
                    mGoogleMap.addMarker(
                            new MarkerOptions().
                                    position(location).
                                    title(input));
                    mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location,15));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void insertRecord(String cal,String hour) {
        EditText Title = (EditText)findViewById(R.id.detail_title);
        EditText Address = (EditText)findViewById(R.id.address);
        EditText Memo = (EditText)findViewById(R.id.memo);
        mDbHelper.insertCalBySQL(Title.getText().toString(),getPicker("start"),getPicker("end"),
        Address.getText().toString(),Memo.getText().toString(),cal,Picker12to24(getPicker("start"))+"시");
    }
    private void updateRecord(String cal,String hour) {
        EditText Title = (EditText)findViewById(R.id.detail_title);
        EditText Address = (EditText)findViewById(R.id.address);
        EditText Memo = (EditText)findViewById(R.id.memo);
        mDbHelper.updateCalBySQL(Title.getText().toString(),getPicker("start"),getPicker("end"),
                Address.getText().toString(),Memo.getText().toString(),cal,hour,Picker12to24(getPicker("start"))+"시");
    }
    private void deleteRecord(String cal,String hour) {
        mDbHelper.deleteCalBySQL(cal,hour);
    }
    private void viewAllToTextView() { //데이터 확인을 위한것
        //TextView result = (TextView)findViewById(R.id.result);

        Cursor cursor = mDbHelper.getAllCalsBySQL();
        while (cursor.moveToNext()) {
            Log.d("kk", "viewAllToTextView: "+cursor.getString(6)+cursor.getString(7));
        }
    }
    private boolean findOneRecord(String cal,String hour){
        Cursor cursor=mDbHelper.getCalHourBySQL(cal,hour);
        EditText Title = findViewById(R.id.detail_title);
        EditText Address = findViewById(R.id.address);
        EditText Memo = findViewById(R.id.memo);
        if(cursor.moveToNext()) {
            Title.setText(cursor.getString(1));
            Address.setText(cursor.getString(4));
            Memo.setText(cursor.getString(5));
            setPicker(cursor.getString(2),cursor.getString(3));
            return true;
        }
        return false;
    }
    private void ResultTitleToMain(){
        Intent resultIntent = new Intent();

        EditText Title = findViewById(R.id.detail_title);
        resultIntent.putExtra("STATE_INFO", Title.getText().toString());
        resultIntent.putExtra("indexN", INDEX);
        setResult(RESULT_OK, resultIntent);
    }
    private void DeletedToMain(){
        Intent resultIntent = new Intent();

        resultIntent.putExtra("STATE_INFO", "CAL_DELETE");
        resultIntent.putExtra("indexN", INDEX);
        setResult(RESULT_OK, resultIntent);
    }
}