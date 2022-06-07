package com.example.calendarproject;

import static java.util.Calendar.DATE;
import static java.util.Calendar.DAY_OF_WEEK;
import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;


public class MonthFragment extends Fragment {

    ArrayList<String> daysList = new ArrayList<String>();
    Calendar now = getInstance();
    Calendar cal = getInstance();
    int y;
    int m;
    MainActivity main;
    int PagePos;
    CalDBHelper mDbHelper;

    public MonthFragment(int position, int ItemCenter,MainActivity main) {
        // Required empty public constructor
        m=now.get(MONTH); //매번 get해줘야 현재 월의 정보가 담김
        m=m+(position-ItemCenter);
        now.set(MONTH, m); //현재 날짜에서 월정보 변경
        y=now.get(YEAR);
        m=now.get(MONTH);
        this.main= main;
        mDbHelper = new CalDBHelper(main);
        PagePos=position;
    }
//
//    // TODO: Rename and change types and number of parameters
//    public MonthFragment newInstance(String param1, String param2) {
//        MonthFragment fragment = new MonthFragment(y, m);
//        Bundle args = new Bundle();
//        fragment.setArguments(args);
//        return fragment;
//    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_month, container, false);

        int curYear = now.get(YEAR);
        int curMonth = now.get(MONTH) + 1; //MONTH는 0부터 시작한다(1월:0 ~ 12월:11)
        int lastDate = now.getActualMaximum(DATE);
        cal.set(curYear, now.get(MONTH), 1); //DAY_OF_MONTH를 1로 설정 (월의 첫날)
        int startDay = cal.get(DAY_OF_WEEK); //그 주의 요일 반환 (일:1 ~ 토:7)
        cal.set(DATE, 1); //DAY_OF_MONTH를 1로 설정 (월의 첫날)
        int curDay=cal.get(DATE);

        //daysList에 날짜 채워넣기
        for (int i = 1; i <= 42; i++) { //7*6칸이 되도록
            //달의 첫일(1일)의 요일보다 작을 시 공백 채워넣기
            if (i < startDay||i>=lastDate+startDay) { //날짜가 아닌 부분은 공백처리
                daysList.add(" ");
            } else {
                daysList.add(String.valueOf(i - startDay + 1));
                //공백 채우는 과정에서 i가 증가해서 첫일만큼 빼준다
                //(요일은 1부터 시작>0일을 만들지 않기 위해 +1)
            }
        }
        MonthAdapter adapt=new MonthAdapter(getActivity(), R.layout.month_item, daysList,
                startDay-1,main,PagePos);
        //회전시 크기에 맞게 뷰를 조정하기 위해 getActivity전달, 시작 요일은 1부터 시작하는데 뷰의 포지션은 0부터라서
        // id를 바탕으로 화면 레이아웃에 정의된 GridView 객체 로딩
        GridView gridview = (GridView) rootView.findViewById(R.id.MONTH_monthgrid);
        // 어댑터를 GridView 객체에 연결
        gridview.setAdapter(adapt);

        //기능 4. Toast 메세지
        // 항목 선택 이벤트 처리
        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                //position은 0부터 시작, 첫요일만큼 빼준다
                int curDay = position + 1 - startDay + 1;

                if (curDay > 0) { //공백부분은 토스트 메세지 없도록
                    Toast.makeText(getActivity(),
                            curYear + "." + curMonth + "." + curDay,
                            Toast.LENGTH_SHORT).show();
                    for(int i=0;i<adapt.getCount();i++) {

                        parent.getChildAt(i).setBackgroundColor(Color.WHITE);
                    }
                    adapt.select=position;
                    v.setBackgroundColor(Color.CYAN);
                    ((OnDetailSelectListener)getActivity()).onDetailSelect(position,Integer.toString(curDay),0);
                }

            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }
    public interface OnDetailSelectListener{
        public void onDetailSelect(int position,String day,int Time);
    }
}