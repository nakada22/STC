package jp.co.timecard.db;

/**
* データベース関連の定数を定義する。
* @version 1.0
* @author Tomohiro Tano
*/
public class DbConstants {

	//データベースの名前とバージョン
    public static final String DATABASE_NAME = "TimeCardDB";
    public static final int DATABASE_VERSION = 1;

    //作成、使用するテーブル名とカラム名
    public static final String TABLE_NAME1 = "mst_kintai";
    public static final String TABLE_NAME2 = "mst_attendance";
    public static final String TABLE_NAME3 = "mst_leaveoffice";
    public static final String TABLE_NAME4 = "mst_break";
    public static final String TABLE_NAME5 = "mst_initime";
    public static final String TABLE_NAME6 = "mst_trainee";
    public static final String TABLE_NAME7 = "mst_password";
    
    public static final String COLUMN_EMPLOYEE_ID = "employee_id";
    public static final String COLUMN_KINTAI_DATE = "kintai_date";
    
    public static final String COLUMN_ATTENDANCE_ID = "attendance_id";
    public static final String COLUMN_ATTENDANCE_DATE = "attendance_date";
    public static final String COLUMN_ATTENDANCE_TIME = "attendance_time";
    public static final String COLUMN_REGIST_DATETIME = "regist_datetime";
    public static final String COLUMN_UPDATE_DATETIME = "update_datetime";

    public static final String COLUMN_LEAVEOFFICE_ID = "leaveoffice_id";
    public static final String COLUMN_LEAVEOFFICE_DATE = "leaveoffice_date";
    public static final String COLUMN_LEAVEOFFICE_TIME = "leaveoffice_time";
    
    public static final String COLUMN_BREAK_ID = "break_id";
    public static final String COLUMN_BREAK_DATE = "break_date";
    public static final String COLUMN_BREAK_TIME = "break_time";
 
    public static final String COLUMN_START_TIME = "start_time";
    public static final String COLUMN_END_TIME = "end_time";
    
    public static final String COLUMN_TRAINEE_ID = "trainee_id";
    public static final String COLUMN_EMPLOYEE_NAME = "employee_name";
    
    public static final String COLUMN_SCREEN_ID = "screen_id";
    public static final String COLUMN_PASSWORD = "password";
    
    // SQL CREATE文(TABLE1)
    public static final String CREATE_TABLE1 =
            "CREATE TABLE " + TABLE_NAME1 + " ("
                            + COLUMN_EMPLOYEE_ID + " INTEGER NOT NULL,"
                            + COLUMN_KINTAI_DATE + " TEXT NOT NULL, "
                            + "PRIMARY KEY(" + COLUMN_EMPLOYEE_ID + ", " + COLUMN_KINTAI_DATE
                            + ") )";
    public static final String CREATE_TABLE2 =
            "CREATE TABLE " + TABLE_NAME2 + " ("
                            + COLUMN_ATTENDANCE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + COLUMN_EMPLOYEE_ID + " INTEGER NOT NULL, "
                            + COLUMN_ATTENDANCE_DATE + " TEXT NOT NULL, "
                            + COLUMN_ATTENDANCE_TIME + " TEXT NOT NULL, "
                            + COLUMN_REGIST_DATETIME + " TEXT, "
                            + COLUMN_UPDATE_DATETIME + " TEXT "
                            + " );";
    public static final String CREATE_TABLE3 =
            "CREATE TABLE " + TABLE_NAME3 + " ("
                            + COLUMN_LEAVEOFFICE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + COLUMN_EMPLOYEE_ID + " INTEGER NOT NULL, "
                            + COLUMN_LEAVEOFFICE_DATE + " TEXT NOT NULL, "
                            + COLUMN_LEAVEOFFICE_TIME + " TEXT NOT NULL, "
                            + COLUMN_REGIST_DATETIME + " TEXT, "
                            + COLUMN_UPDATE_DATETIME + " TEXT "
                            + " );";
    public static final String CREATE_TABLE4 =
            "CREATE TABLE " + TABLE_NAME4 + " ("
                            + COLUMN_BREAK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + COLUMN_EMPLOYEE_ID + " INTEGER NOT NULL, "
                            + COLUMN_BREAK_DATE + " TEXT NOT NULL, "
                            + COLUMN_BREAK_TIME + " TEXT NOT NULL, "
                            + COLUMN_REGIST_DATETIME + " TEXT, "
                            + COLUMN_UPDATE_DATETIME + " TEXT "
                            + " );";
    public static final String CREATE_TABLE5 =
            "CREATE TABLE " + TABLE_NAME5 + " ("
                            + COLUMN_START_TIME + " TEXT DEFAULT '09:30',"
                            + COLUMN_END_TIME + " TEXT DEFAULT '18:30', "
                            + COLUMN_BREAK_TIME + " TEXT DEFAULT '1:00', "
                            + COLUMN_REGIST_DATETIME + " TEXT, "
                            + COLUMN_UPDATE_DATETIME + " TEXT"
                            + " );";

    public static final String CREATE_TABLE6 = 
            "CREATE TABLE " + TABLE_NAME6 + " ("
            		+ COLUMN_TRAINEE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_EMPLOYEE_ID + " INTEGER NOT NULL, "
                    + COLUMN_EMPLOYEE_NAME + " TEXT NOT NULL"
                    + " );";
    
    public static final String CREATE_TABLE7 = 
            "CREATE TABLE " + TABLE_NAME7 + " ("
            		+ COLUMN_SCREEN_ID + " TEXT PRIMARY KEY NOT NULL,"
                    + COLUMN_PASSWORD + " TEXT NOT NULL"
                    + " );";
    
    //SQL DROP TABLE文
    public static final String DATABASE_UPDATE1 ="DROP TABLE IF EXISTS " + TABLE_NAME1;
    public static final String DATABASE_UPDATE2 ="DROP TABLE IF EXISTS " + TABLE_NAME2;
    public static final String DATABASE_UPDATE3 ="DROP TABLE IF EXISTS " + TABLE_NAME3;
    public static final String DATABASE_UPDATE4 ="DROP TABLE IF EXISTS " + TABLE_NAME4;
    public static final String DATABASE_UPDATE5 ="DROP TABLE IF EXISTS " + TABLE_NAME5;
    public static final String DATABASE_UPDATE6 ="DROP TABLE IF EXISTS " + TABLE_NAME6;
    public static final String DATABASE_UPDATE7 ="DROP TABLE IF EXISTS " + TABLE_NAME7;
     
}