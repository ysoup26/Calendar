package com.example.calendarproject;

import static com.google.android.material.color.MaterialColors.getColor;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class MonthAdapter extends BaseAdapter {
    private Context mContext;
    private int mResource;
    ArrayList<String> mItems;
    int select;
    int PagePos;
    int startD;
    MainActivity m;
    CalDBHelper mDbHelper;

    public MonthAdapter(Context context, int resource, ArrayList<String> items,int date, MainActivity m,int p) {
        mContext = context;
        mItems = items;
        mResource = resource; //디자인-xml파일
        select=date;
        startD=date;
        this.m= m;
        mDbHelper = new CalDBHelper(m);
        PagePos=p;
    }

    // MyAdapter 클래스가 관리하는 항목의 총 개수를 반환
    @Override
    public int getCount() {
        return mItems.size();
    }

    // MyAdapter 클래스가 관리하는 항목의 중에서 position 위치의 항목을 반환
    @Override
    public Object getItem(int position) {
        return mItems.get(position); //일수 정보 돌려줌
    }

    // 항목 id를 항목의 위치로 간주함
    @Override
    public long getItemId(int position) {
        return position;
    }

    // position 위치의 항목에 해당되는 항목뷰를 반환하는 것이 목적임
    //@SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) { // 해당 항목 뷰가 이전에 생성된 적이 없는 경우
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // 항목 뷰를 정의한 xml 리소스(여기서는 mResource 값)으로부터 항목 뷰 객체를 메모리로 로드
            convertView = inflater.inflate(mResource, parent, false);
        }
         //convertView 변수로 참조되는 항목 뷰 객체내에 포함된 텍스트뷰 객체를 id를 통해 얻어옴
        TextView day = (TextView) convertView.findViewById(R.id.month_item_day);
        LinearLayout ll = convertView.findViewById(R.id.month_item_ll);
        TextView c1 = (TextView) convertView.findViewById(R.id.month_item_cal1);
        TextView c2 = (TextView) convertView.findViewById(R.id.month_item_cal2);
        // 어댑터가 관리하는 항목 데이터 중에서 position 위치의 항목의 문자열을 설정 텍스트뷰 객체에 설정

        if(!CheckDB(position,c1,c2)){
            c1.setText(" ");
            c2.setText(" ");
            c1.setBackgroundColor(Color.WHITE);
            c2.setBackgroundColor(Color.WHITE);
        }

        day.setText(mItems.get(position));

        if(position==select) { //position과 선택이 일치하면 CYAN색으로--다
            ll.setBackgroundColor(Color.CYAN);
        }else { //선택되지 않은 뷰는 WHITE로
            ll.setBackgroundColor(Color.WHITE);
        }
        int w=(parent.getWidth())/7;
        int h=((parent.getHeight()/6)/3)-9;
        day.setWidth(w);
        day.setHeight(h);
        c1.setWidth(w);
        c1.setHeight(h);
        c2.setWidth(w);
        c2.setHeight(h);
        return convertView;
    }
    private boolean CheckDB(int position,TextView c1,TextView c2){
        //setCurrentItem할때 이전 프레그먼트도 미리 만드는데 매번 DB에 접근하지 않도록 제어
        if(PagePos==m.vp.getCurrentItem()||PagePos==m.vp.getCurrentItem()-1||PagePos==m.vp.getCurrentItem()+1) {

            String searchS = m.MFA.toString(PagePos) + (position-startD+1) + "일 ";
            Cursor cursor = mDbHelper.getCalBySQL(searchS);
            int count=0;
            while(cursor.moveToNext()) {
                String s;
                if(count==0){
                    s=cursor.getString(1);
                    if(s.length()>5){
                        s=s.substring(0,6);
                        s+="..";
                    }
                    c1.setText(s);
                    c1.setBackgroundColor(mContext.getResources().getColor(R.color.orange));
                }else{
                    s=cursor.getString(1);
                    if(s.length()>5){
                        s=s.substring(0,6);
                        s+="..";
                    }
                    c2.setText(s);
                    c2.setBackgroundColor(mContext.getResources().getColor(R.color.yellow));
                    return true;
                }
                count++;
            }
            return true;
        }
        return false;
    }
}
