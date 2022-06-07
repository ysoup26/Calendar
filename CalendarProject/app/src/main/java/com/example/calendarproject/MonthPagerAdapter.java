package com.example.calendarproject;

import static java.util.Calendar.MONTH;
import static java.util.Calendar.YEAR;
import static java.util.Calendar.getInstance;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.Calendar;

public class MonthPagerAdapter extends FragmentStateAdapter {
    private static int NUM_ITEMS=3; //기본 갯수
    int ItemCenter=getItemCount()/2;
    MainActivity m;
    public MonthPagerAdapter(MainActivity fa) {
        super(fa);
        m=fa;
        fa.getSupportActionBar().setTitle(toString(ItemCenter)); //처음 시작화면의 메뉴바 텍스트 설정
    }

    // 각 페이지를 나타내는 프래그먼트 반환
    @Override
    public Fragment createFragment(int position) {
        MonthFragment fragment=new MonthFragment(position,ItemCenter,m);
        return fragment;
    }

    @Override
    public int getItemCount() { //프레그먼트 갯수를 정수의 최댓값만큼-사실상 3개가 재사용되는것 //이 값에 따라 갯수가 결정된다
        return Integer.MAX_VALUE;
    }
    public String toString(int position){
        Calendar now=getInstance();
        int m=now.get(MONTH)+(position-ItemCenter);
        now.set(MONTH, m); //현재 날짜에서 월정보 변경
        return now.get(YEAR)+"년 "+(now.get(MONTH)+1)+"월 ";
    }
}