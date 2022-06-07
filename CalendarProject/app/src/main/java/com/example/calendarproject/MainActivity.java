package com.example.calendarproject;

import androidx.appcompat.app.AppCompatActivity;


import android.app.Dialog;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;


import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MonthFragment.OnDetailSelectListener {
    private static final int MAIN_ACTIVITY_REQUEST_CODE = 0;
    ViewPager2 vp;
    WeekPagerAdapter WFA;
    MonthPagerAdapter MFA;
    String selectYMD = null;
    String selectH=null;
    int selectPos = 0;
    boolean menu_month=true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CalDBHelper mDbHelper = new CalDBHelper(this);

        vp = findViewById(R.id.vpPager);
        //처음 시작할때 월간달력이 보이도록 설정해두었음
        setMonthPager(vp);//액티비티 시작시 weeKFragmentAdater와 연결함(아래 함수있음)

        //월간달력의 세부 일정을 표시하는 Dialog 설정
        Dialog calDialog=new Dialog(MainActivity.this);
        calDialog.setContentView(R.layout.cal_dialog);

        //일정 추가 액티비티를 실행하는 floating 버튼 설정
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startDetailActivity(mDbHelper,calDialog);
            }
        });
    }
    //detailActivity에서 전달받은 내용 수행-저장되었으면 즉시 표시,삭제 되었다면 표시 삭제
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 결과를 반환하는 액티비티가 FIRST_ACTIVITY_REQUEST_CODE 요청코드로 시작된 경우가 아니거나
        // 결과 데이터가 빈 경우라면, 메소드 수행을 바로 반환함.
        if (requestCode != MAIN_ACTIVITY_REQUEST_CODE || data == null)
            return;
        String re=data.getStringExtra("STATE_INFO");
        GridView g;
        TextView childC = null;
        if(menu_month){
            int index = Integer.parseInt(data.getStringExtra("indexN"));
            childC=getIndextoView(index);
            if(re.equals("CAL_DELETE")){    //삭제 코드
                if(childC!=null) {
                    childC.setBackgroundColor(Color.WHITE);
                    childC.setText(" ");
                }
            }else{      //저장 또는 업데이트
                if(childC!=null) {
                    if(index==0)
                        childC.setBackgroundColor(getResources().getColor(R.color.orange));
                    else
                        childC.setBackgroundColor(getResources().getColor(R.color.yellow));
                    childC.setText(re);
                }
            }
        }else{ //주간 달력
            if(re.equals("CAL_DELETE")){
                g = findViewById(R.id.WEEK_daygrid);
                childC = (TextView) g.getChildAt(selectPos);
                if(childC!=null)
                    childC.setText(" ");
            }else{
                g = findViewById(R.id.WEEK_daygrid);
                childC = (TextView) g.getChildAt(selectPos);
                if(childC!=null)
                    childC.setText(re);
            }
        }
    }
    //인덱스로 특정 텍스트뷰를 반환하는 함수
    public TextView getIndextoView(int index){
        GridView g = findViewById(R.id.MONTH_monthgrid);
        LinearLayout childL = (LinearLayout) g.getChildAt(selectPos);
        if(index==0) {
            TextView childC = childL.findViewById(R.id.month_item_cal1);
            return childC;
        }else if(index==1) {
            TextView childC = childL.findViewById(R.id.month_item_cal2);
            return childC;
        }
        return null;
    }
    //메뉴바를 동적 추가하는 부분
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //메뉴바의 아이템이 클릭되었을때-이 부분에서 월별/주별 달력 전환함
    public boolean onOptionsItemSelected(MenuItem item) {
        //클릭한 메뉴 item id를 읽어옴
        switch (item.getItemId()) {
            //02. 월간 달력 프레그먼트
            case R.id.action_monthActivity:
                setMonthPager(vp);
                return true;
            //03. 주간 달력 프레그먼트
            case R.id.action_weekActivity:
                setWeekPager(vp);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    //DetailCalendarActivity 시작 => 월간-주간이 다르게 동작,
    // 월간은 선택 일에 이미 일정이 존재하면 세부 다이얼로그 실행
    public void startDetailActivity(CalDBHelper mDbHelper,Dialog CD) {
        Intent i = new Intent(MainActivity.this, DetailCalendarActivity.class);
        if (menu_month && selectYMD != null) {
            GridView grid = findViewById(R.id.MONTH_monthgrid);
            LinearLayout linear = (LinearLayout) grid.getChildAt(selectPos);
            TextView tv = linear.findViewById(R.id.month_item_cal1);
            if (!tv.getText().equals(" ")) { //해당 일에 다른 일정이 있을때-세부 일정으로
                checkDetailCal(mDbHelper, CD);
                CD.show();
            }else{
                i.putExtra("cal", selectYMD);
                i.putExtra("hour", selectH);
                i.putExtra("indexN","0");
                startActivityForResult(i, MAIN_ACTIVITY_REQUEST_CODE);
            }
        } else {
            if (selectYMD == null) { //앱 시작후 아이템 클릭이 없어 선택 날짜를 모르는 경우
                onDetailSelect(0, "1", 0);
            }
            //주간 달력은 세부 지정 사항 불필요
            i.putExtra("cal", selectYMD);
            i.putExtra("hour", selectH);
            startActivityForResult(i, MAIN_ACTIVITY_REQUEST_CODE);
        }
    }
    //선택 일에 대한 DB를 확인해 리스트를 만듬(다이얼로그 실행을 위해)
    public void checkDetailCal(CalDBHelper mDbHelper, Dialog CD){
        ListView list=CD.findViewById(R.id.cal_dialog_list);
        TextView dialogTitle=CD.findViewById(R.id.dialog_cal);
        dialogTitle.setText(selectYMD);
        ArrayList<String> calItem = new ArrayList<>();
        ArrayList<String> hourItem = new ArrayList<>();
        Cursor cursor = mDbHelper.getCalBySQL(selectYMD);
        while(cursor.moveToNext()){
            calItem.add(cursor.getString(1) );
            hourItem.add(cursor.getString(7) );
        }
        ArrayAdapter calAdapter = new ArrayAdapter(MainActivity.this,
                        android.R.layout.simple_list_item_1, calItem);
        list.setAdapter(calAdapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int index, long l) {
                Intent i = new Intent(MainActivity.this, DetailCalendarActivity.class);
                i.putExtra("cal", selectYMD);
                i.putExtra("hour", hourItem.get(index));
                i.putExtra("indexN",Integer.toString(index));
                CD.cancel();
                startActivityForResult(i, MAIN_ACTIVITY_REQUEST_CODE);
            }
        });
    }

    //월간 페이지 어댑터 설정 함수
    private void setMonthPager(ViewPager2 vp) {
        ViewPager2 monthPager = vp;
        MFA = new MonthPagerAdapter(this);
        monthPager.setAdapter(MFA);
        monthPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                //메뉴바의 글자를 바꾸는 부분(연도-월 표시 변경)
                MainActivity.this.getSupportActionBar().setTitle(MFA.toString(position));
            }
        });
        //월간-주간에 대해 요일표시 부분의 길이를 다르게 하기 위함
        TextView blank = this.findViewById(R.id.dates_text);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        blank.setLayoutParams(pp);
        monthPager.setCurrentItem(MFA.ItemCenter);
        menu_month = true;
    }

    //주간 페이지 어댑터 설정 함수
    private void setWeekPager(ViewPager2 vp) {
        ViewPager2 weekPager = vp;
        WFA = new WeekPagerAdapter(this);
        weekPager.setAdapter(WFA);
        //페이지 변경 이벤트 리스너
        weekPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                //메뉴바의 글자를 바꾸는 부분(연도-월 표시 변경)
                MainActivity.this.getSupportActionBar().setTitle(WFA.toString(position));
            }
        });
        TextView blank = this.findViewById(R.id.dates_text);
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(getResources().getDimensionPixelSize(R.dimen.size_50dp), LinearLayout.LayoutParams.WRAP_CONTENT);
        blank.setLayoutParams(pp);
        weekPager.setCurrentItem(WFA.ItemCenter); //중간 페이지에서 시작
        menu_month = false;
    }
    //월간-주간 달력의 아이템이 선택되었을 때, 해당 연월일시 정보를 갱신하는 부분
    @Override
    public void onDetailSelect(int position, String day, int time) {
        if (menu_month)
            selectYMD = MFA.toString(vp.getCurrentItem()) + day + "일 " ;   //월간은 클릭시 시간 정보를 모름 디폴트 0
        else
            selectYMD = WFA.weekC.getYMD(vp.getCurrentItem(), position);
        selectPos =position;
        selectH=time+"시";
    }
};