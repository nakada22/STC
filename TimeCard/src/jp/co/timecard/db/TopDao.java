package jp.co.timecard.db;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jp.co.timecard.TopActivity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.TextView;

public class TopDao {

	private static DbOpenHelper helper = null;
	private static Cursor c = null;
	private static ContentValues cv = null;

	public TopDao(Context context) {
		helper = new DbOpenHelper(context);
		cv = new ContentValues();
	}
	
	/**
	 * 外部キーとして使用される社員ID登録メソッド(spinner選択時)
	 * 但しアプリ起動日付のデータがある時は、Insertしない
	 * @return None
	 * @param str 当日日付
	 */
	public void preKintaiSave(String kintai_date, String employee_id) {
		// 勤怠マスタにデータがあるか、なければInsert
		SQLiteDatabase db = helper.getWritableDatabase();

		//db.execSQL("DELETE FROM mst_kintai;");
		c = db.query(true, DbConstants.TABLE_NAME1, null, "employee_id=?",
				new String[] { employee_id }, null, null, null, null);
		
		// long ret;
		if (c.getCount() == 0) {
			// データが0件であれば勤怠マスタ登録
			try {
				cv.put(DbConstants.COLUMN_EMPLOYEE_ID, employee_id);
				cv.put(DbConstants.COLUMN_KINTAI_DATE, kintai_date);
				db.insert(DbConstants.TABLE_NAME1, null, cv);
			} finally {
				db.close();
			}
		}
	}

	/**
	 * 時刻設定マスタのデータ登録メソッド
	 * (アプリ起動時１回のみ)
	 * @return None
	 * @param str 現在日時
	 */
	public void preTimeSave(String str) {
		SQLiteDatabase db = helper.getWritableDatabase();
		// db.execSQL("DELETE FROM mst_initime;");
		c = db.query(true, DbConstants.TABLE_NAME5, null, null, null, null,
				null, null, null);
		
		if (c.getCount() == 0) {
			// データが0件であれば時刻設定マスタ登録
			try {
				cv.put(DbConstants.COLUMN_START_TIME, "09:30");
				cv.put(DbConstants.COLUMN_END_TIME, "18:30");
				cv.put(DbConstants.COLUMN_BREAK_TIME, "01:00");
				cv.put(DbConstants.COLUMN_REGIST_DATETIME, str);
				cv.put(DbConstants.COLUMN_UPDATE_DATETIME, str);
				db.insert(DbConstants.TABLE_NAME5, null, cv);

			} finally {
				db.close();
			}
		}
	}
	
	/**
	 * 休憩マスタの当日日付データ登録(退勤ボタン押下時)メソッド
	 * 
	 * @return None
	 * @param kintai_date 勤怠日付
	 * @param kintai_date_time 現在日時
	 */
	public void preBreakSave(String kintai_date, String kintai_date_time) {
		SQLiteDatabase db = helper.getWritableDatabase();
		// db.execSQL("DELETE FROM mst_break;");
		c = db.rawQuery("SELECT mk.kintai_id, mi.break_time FROM "
				+ "mst_kintai mk, mst_initime mi WHERE mk."
				+ DbConstants.COLUMN_KINTAI_DATE + "='" + kintai_date + "'",
				null);

		if (c.moveToFirst()) {
			String kintai_id = c.getString(c.getColumnIndex("kintai_id"));
			String break_time = c.getString(c.getColumnIndex("break_time"));

			Cursor c2 = db.rawQuery("SELECT * FROM " + "mst_break WHERE "
					+ DbConstants.COLUMN_EMPLOYEE_ID + "=" + kintai_id, null);

			// データがなければInsert
			if (c2.getCount() == 0) {
				try {
					cv.put(DbConstants.COLUMN_EMPLOYEE_ID, kintai_id);
					cv.put(DbConstants.COLUMN_BREAK_TIME, break_time);
					cv.put(DbConstants.COLUMN_REGIST_DATETIME, kintai_date_time);
					cv.put(DbConstants.COLUMN_UPDATE_DATETIME, kintai_date_time);

					db.insert(DbConstants.TABLE_NAME4, null, cv);
				} finally {
					db.close();
				}
			}
		}
	}

	/**
	 * 出勤マスタのデータ登録メソッド
	 * 
	 * @return true or false
	 * @param kintai_date 出勤日
	 * @param kintai_date_time 出勤時刻
	 * @param atd_time 出勤時刻TextView
	 * @param employee_name 社員名
	 */
	public boolean AttendanceSave(String kintai_date, String kintai_date_time,
			TextView atd_time, String employee_name) {
		SQLiteDatabase db = helper.getWritableDatabase();
		// db.execSQL("DELETE FROM mst_attendance;");
		// Insertする社員ID取得
		c = db.query(true, DbConstants.TABLE_NAME6, null, "employee_name=?",
				new String[] { employee_name }, null, null, null, null);
		
		if (c.moveToFirst()) {
			String employee_id = c.getString(c.getColumnIndex("employee_id"));
			String ins_atd_time;

			// 現在時刻使用チェック時
			//if (atd_time != null) {
			//	ins_atd_time = atd_time.getText().toString();// 画面表示時刻で登録
			//} else {
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");// 現在時刻で登録
			ins_atd_time = sdf.format(Calendar.getInstance().getTime());
			//}

			Cursor c2 = db.rawQuery("SELECT * FROM " + "mst_attendance WHERE "
					+ DbConstants.COLUMN_EMPLOYEE_ID + "=" + employee_id
					+ " AND attendance_date='" + kintai_date + "'", null);
			
			// 既に退勤済の場合は、更新処理を行わない
			Cursor c3 = db.rawQuery("SELECT * FROM " + "mst_leaveoffice WHERE "
					+ DbConstants.COLUMN_EMPLOYEE_ID + "=" + employee_id
					+ " AND leaveoffice_date='" + kintai_date + "'", null);
			
			if (c2.getCount() == 0) {
				// データがなければ新規登録
				try {
					cv.put(DbConstants.COLUMN_EMPLOYEE_ID, employee_id);
					cv.put(DbConstants.COLUMN_ATTENDANCE_DATE, kintai_date);
					cv.put(DbConstants.COLUMN_ATTENDANCE_TIME, ins_atd_time);
					cv.put(DbConstants.COLUMN_REGIST_DATETIME, kintai_date_time);
					cv.put(DbConstants.COLUMN_UPDATE_DATETIME, kintai_date_time);

					db.insert(DbConstants.TABLE_NAME2, null, cv);
				} finally {
					db.close();
				}
			} else {

				// 既に出勤時刻登録済の場合は、新たな時刻で更新
				// また退勤済の場合は、更新処理を行わなわずToast表示
				if (c3.getCount() == 0) {
					try {
						cv.put(DbConstants.COLUMN_ATTENDANCE_TIME, ins_atd_time);
						cv.put(DbConstants.COLUMN_UPDATE_DATETIME,
								kintai_date_time);
						db.update(DbConstants.TABLE_NAME2, cv,
								DbConstants.COLUMN_EMPLOYEE_ID + "=" + employee_id,
								null);
					} finally {
						db.close();
					}
				} else {
					// Toast表示
					return false;
				}

			}
		}
		return true;
	}
	
	/**
	 * 出勤時刻表示用メソッド
	 * 
	 * @return None
	 * @param atd_date  出勤日
	 * @param tv 表示TextView
	 */
	public void AttendanceTimeDisp(String atd_date, TextView tv, String employ_name) {
		
		SQLiteDatabase db = helper.getWritableDatabase();
		// 社員ID取得SQL
		String SQL = "(SELECT employee_id FROM mst_trainee WHERE employee_name='"
					+ employ_name + "')";
		
		c = db.rawQuery("SELECT attendance_time FROM " + "mst_attendance "
				+ " WHERE " + DbConstants.COLUMN_ATTENDANCE_DATE + "='"
				+ atd_date + "' AND employee_id=" + SQL, null);
		
		if (c.moveToFirst()) {
			String attendance_time = c.getString(c
					.getColumnIndex("attendance_time"));
			tv.setText(attendance_time);
		}
	}

	/**
	 * 退勤マスタのデータ登録メソッド
	 * 
	 * @return true or false
	 * @param kintai_date  勤怠日
	 * @param kintai_date_time 勤怠時刻
	 * @param leave_time 退勤時刻
	 */
	public boolean LeaveofficeSave(String kintai_date, String kintai_date_time,
			TextView leave_time) {
		SQLiteDatabase db = helper.getWritableDatabase();
		// db.execSQL("DELETE FROM mst_leaveoffice;");

		// Insertする謹怠ID取得
		c = db.rawQuery("SELECT mk.kintai_id FROM " + "mst_kintai mk"
				+ " WHERE mk." + DbConstants.COLUMN_KINTAI_DATE + "='"
				+ kintai_date + "'", null);

		if (c.moveToFirst()) {
			String kintai_id = c.getString(c.getColumnIndex("kintai_id"));
			String ins_leave_time;

			// 現在時刻使用チェック時
			if (leave_time != null) {
				ins_leave_time = leave_time.getText().toString();// 画面表示時刻で登録
			} else {
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");// 現在時刻で登録
				ins_leave_time = sdf.format(Calendar.getInstance().getTime());
			}

			Cursor c2 = db.rawQuery("SELECT * FROM " + "mst_leaveoffice WHERE "
					+ DbConstants.COLUMN_EMPLOYEE_ID + "=" + kintai_id, null);

			Cursor c3 = db.rawQuery("SELECT * FROM " + "mst_attendance WHERE "
					+ DbConstants.COLUMN_EMPLOYEE_ID + "=" + kintai_id, null);

			if (c2.getCount() == 0) {
				// 退勤記録がなくても、出勤記録もなければ新規登録は行わない
				if (c3.getCount() == 0) {
					// Toast表示
					return false;
				} else {
					try {
						cv.put(DbConstants.COLUMN_EMPLOYEE_ID, kintai_id);
						cv.put(DbConstants.COLUMN_LEAVEOFFICE_DATE, kintai_date);
						cv.put(DbConstants.COLUMN_LEAVEOFFICE_TIME,
								ins_leave_time);
						cv.put(DbConstants.COLUMN_REGIST_DATETIME,
								kintai_date_time);
						cv.put(DbConstants.COLUMN_UPDATE_DATETIME,
								kintai_date_time);
						db.insert(DbConstants.TABLE_NAME3, null, cv);
					} finally {
						db.close();
					}
				}
			} else {
				// 既に退勤時刻登録済の場合は、新たな時刻で更新
				try {
					cv.put(DbConstants.COLUMN_LEAVEOFFICE_TIME, ins_leave_time);
					cv.put(DbConstants.COLUMN_UPDATE_DATETIME, kintai_date_time);
					db.update(DbConstants.TABLE_NAME3, cv,
							DbConstants.COLUMN_EMPLOYEE_ID + "=" + kintai_id,
							null);
				} finally {
					db.close();
				}
			}
		} else {
			// 勤怠Idがない場合(出勤記録がない場合)
			return false;
		}
		return true;
	}

	/**
	 * 始業・終業・休憩時間・合計時間 表示用メソッド
	 * 
	 * @return なし
	 * @param leave_date
	 * @param tv1 始業時刻
	 * @param tv2 終業時刻
	 * @param tv3 休憩時間
	 * @param tv4 合計時間
	 */
	public void TopTimeDisp(String leave_date, TextView tv1, TextView tv2,
			TextView tv3, TextView tv4) {
		
		SQLiteDatabase db = helper.getWritableDatabase();
		c = db.rawQuery(
				"SELECT ml.leaveoffice_time, mb.break_time, ma.attendance_time"
						+ " FROM mst_leaveoffice ml, mst_break mb, mst_attendance ma"
						+ " WHERE ml." + DbConstants.COLUMN_LEAVEOFFICE_DATE
						+ "='" + leave_date + "'"
						+ " AND ml.employee_id = mb.employee_id"
						+ " AND ml.employee_id = ma.employee_id", null);

		if (c.moveToFirst()) {
			String leave_time = c.getString(c
					.getColumnIndex("leaveoffice_time"));
			String break_time = c.getString(c.getColumnIndex("break_time"));
			String attendance_time = c.getString(c
					.getColumnIndex("attendance_time"));

			tv1.setText(attendance_time);
			tv2.setText(leave_time);
			tv3.setText(break_time);

			// 合計時間を計算の上表示
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			try {
				Date at = sdf.parse(attendance_time);
				Date lt = sdf.parse(leave_time);
				Date bt = sdf.parse(break_time);
				long sumtime = lt.getTime() - at.getTime() - bt.getTime()
						+ 1000 * 60 * 60 * 6;
				// long sumtime = lt.getTime() - at.getTime() - bt.getTime();

				// 秒を時間に変換、合計時間がマイナスの時は00:00
				if (sumtime < 0) {
					tv4.setText("00:00");
				} else {
					tv4.setText(sdf.format(sumtime));
				}

			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * パスワードマスタのデータ登録メソッド (アプリ起動時１回のみ)
	 * 
	 * @return なし
	 * @param  なし
	 */
	public void prePassWordSave() {

		SQLiteDatabase db = helper.getWritableDatabase();
		// db.execSQL("DELETE FROM mst_password;");
		c = db.query(true, DbConstants.TABLE_NAME7, null, null, null, null,
				null, null, null);

		// 初期パスワード(月次, 基本時間, パスワード設定)
		String[] pass_str = new String[] { "test1", "test2", "test3" };
		String[] screen_str = new String[] { "monthly", "basetime", "password" };
		
		if (c.getCount() == 0) {
			// データが0件であれば時刻設定マスタ登録
			try {
				for (int i = 0; i < pass_str.length; i++) {
					cv.put("screen_id", screen_str[i]);
					cv.put("password", pass_str[i]);
					db.insert(DbConstants.TABLE_NAME7, null, cv);
				}
			} finally {
				db.close();
			}
		}
	}

	/**
	 * パスワードマスタのパスワード取得処理
	 * 
	 * @return password パスワード
	 * @param screen_id 画面ID
	 */
	public String PassWordGet(String screen_id) {
		SQLiteDatabase db = helper.getWritableDatabase();
		String password = null;
		c = db.query(true, DbConstants.TABLE_NAME7, null, "screen_id=?",
				new String[] { screen_id }, null, null, null, null);

		if (c.moveToFirst()) {
			password = c.getString(1);
		}
		return password;
	}

	/**
	 * 社員情報ファイルのデータDB登録処理
	 * 
	 * @return なし
	 * @param employ_num  社員ID
	 * @param employ_name 社員名
	 */
	public void EmployDBInsert(String employ_num, String employ_name) {

		SQLiteDatabase db = helper.getWritableDatabase();
		// db.execSQL("DELETE FROM mst_trainee;");
		c = db.query(true, DbConstants.TABLE_NAME6, null, "employee_id=?",
				new String[] { employ_num }, null, null, null, null);

		if (c.getCount() == 0) {
			// データが0件であれば研修生マスタに登録
			try {
				cv.put("employee_id", employ_num);
				cv.put("employee_name", employ_name);
				db.insert(DbConstants.TABLE_NAME6, null, cv);
			} finally {
				db.close();
			}
		}
	}
}
