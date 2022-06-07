package com.example.calendarproject;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CalDBHelper extends SQLiteOpenHelper {
    final static String TAG = "SQLiteDBTest";

    public CalDBHelper(Context context) {
        super(context, CalContract.DB_NAME, null, CalContract.DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i(TAG, getClass().getName() + ".onCreate()");
        db.execSQL(CalContract.Cals.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        Log.i(TAG, getClass().getName() + ".onUpgrade()");
        db.execSQL(CalContract.Cals.DELETE_TABLE);
        onCreate(db);
    }
    //레코드 추가
    public void insertCalBySQL(String title, String start, String end, String address,
                               String memo, String cal, String hour){
        try {
            String sql = String.format(
                    "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s) VALUES (NULL, '%s', '%s', '%s', '%s', '%s', '%s', '%s')",
                    CalContract.Cals.TABLE_NAME,
                    CalContract.Cals._ID,
                    CalContract.Cals.KEY_TITLE,
                    CalContract.Cals.KEY_START,
                    CalContract.Cals.KEY_END,
                    CalContract.Cals.KEY_ADDRESS,
                    CalContract.Cals.KEY_MEMO,
                    CalContract.Cals.KEY_CAL,
                    CalContract.Cals.KEY_HOUR,
                    title,start,end,address,memo,cal,hour);

            getWritableDatabase().execSQL(sql);
        }catch (SQLException e) {
            Log.e(TAG,"Error in inserting recodes");
        }
    }
    //전체 레코드 읽어오기(내용 확인을 위해)
    public Cursor getAllCalsBySQL() {
        String sql = "Select * FROM " + CalContract.Cals.TABLE_NAME;
        return getReadableDatabase().rawQuery(sql,null);
    }
    //특정 연월일 레코드 읽어오기
    public Cursor getCalBySQL(String cal) {
        String sql = String.format("Select * FROM %s WHERE %s = '%s'",
                CalContract.Cals.TABLE_NAME,
                CalContract.Cals.KEY_CAL,cal);
        return getReadableDatabase().rawQuery(sql,null);
    }
    //특정 연월일-시간 레코드 읽어오기(월간달력)
    public Cursor getCalHourBySQL(String info,String hour) { //그 날의 세부일정 받아올 때
        String sql = String.format("Select * FROM %s WHERE %s = '%s' AND %s = '%s'",
                CalContract.Cals.TABLE_NAME,
                CalContract.Cals.KEY_CAL,info,
                CalContract.Cals.KEY_HOUR,hour);
        return getReadableDatabase().rawQuery(sql,null);
    }
    //연월일-시 기준으로 레코드 삭제
    public void deleteCalBySQL(String info,String hour) {
        String sql = String.format (
                "DELETE FROM %s WHERE %s = '%s' AND %s = '%s'",
                CalContract.Cals.TABLE_NAME,
                CalContract.Cals.KEY_CAL, info,
                CalContract.Cals.KEY_HOUR,hour);
        getWritableDatabase().execSQL(sql);
    }
    //연월일-시 기준으로 레코드 업데이트
    public void updateCalBySQL(String title, String start, String end, String address,
                               String memo, String info,String hour,String newHour){
        try {
            String sql = String.format(
                    "UPDATE  %s SET %s = '%s',%s = '%s',%s = '%s',%s = '%s',%s = '%s',%s = '%s' WHERE %s = '%s' AND %s = '%s'",
                    CalContract.Cals.TABLE_NAME,
                    CalContract.Cals.KEY_TITLE,title,
                    CalContract.Cals.KEY_START,start,
                    CalContract.Cals.KEY_END,end,
                    CalContract.Cals.KEY_ADDRESS,address,
                    CalContract.Cals.KEY_MEMO,memo,
                    CalContract.Cals.KEY_HOUR,newHour,
                    CalContract.Cals.KEY_CAL,info,
                    CalContract.Cals.KEY_HOUR,hour);

            getWritableDatabase().execSQL(sql);
            Log.d(TAG, "updateCalBySQL: ");
        }catch (SQLException e) {
            Log.e(TAG,"Error in inserting recodes");
        }
    }
}
