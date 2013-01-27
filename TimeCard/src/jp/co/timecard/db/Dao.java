package jp.co.timecard.db;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Dao {

	private DbOpenHelper helper = null;

	public Dao(Context context) {
		helper = new DbOpenHelper(context);

	}

	/*
	 * 選択された社員の月次勤怠情報を取得
	 * */
	public String[] MonthlyList(String date, String employee_id){
		SQLiteDatabase db = helper.getWritableDatabase();
		String kintai_list[] = new String[3];
		
		// 出勤時刻のみ打刻されていた場合の出勤時刻取得
		Cursor c2 = db.rawQuery("SELECT ma.attendance_time FROM mst_attendance ma " +
				"WHERE ma.employee_id=" + employee_id + " AND ma.attendance_date='" + 
				date + "'",null);
		
		if (c2.moveToFirst()) {
			// 出勤時刻取得
			if (c2.getCount() != 0) {
				kintai_list[0] = c2.getString(0);
			} else {
				kintai_list[0] = "";
			}
			
			//社員IDを元に退勤時刻・休憩時間を取得
			Cursor c3 = db.rawQuery("SELECT ml.leaveoffice_time, mb.break_time FROM" +
					" mst_attendance ma, mst_leaveoffice ml, mst_break mb " +
					"WHERE ma.employee_id=" + employee_id + 
					" AND ma.employee_id = ml.employee_id AND " +
					"ma.employee_id = mb.employee_id AND " + 
					"ml.leaveoffice_date='"+date+"' AND " + 
					"mb.break_date='"+date+"'" 
					, null);

			if (c3.moveToFirst()){
				kintai_list[1] = c3.getString(0); // leaveoffice_time
				kintai_list[2] = c3.getString(1); // break_time
			}else{
				kintai_list[1] = "";
				kintai_list[2] = "";
			}
			
		} else {
			// 出勤時刻なしの場合
			kintai_list[0] = "";
			kintai_list[1] = "";
			kintai_list[2] = "";
		}
			
		db.close();
		return kintai_list;
	}

	/*
	 * 日次画面の時刻でDB更新
	 * @param update_param Update 時刻
	 * @param employee_id
	 * 
	 * */
	public void DailyUpdate(String[] update_param, String employee_id){
		SQLiteDatabase db = helper.getWritableDatabase();
		SimpleDateFormat timestamp_sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		String currenttime = timestamp_sdf.format(Calendar.getInstance().getTime());
		String[] put_key = new String[]{"attendance_time",
				"leaveoffice_time","break_time"};
		String[] put_table = new String[]{"mst_attendance",
				"mst_leaveoffice","mst_break"};
		String[] put_pos = new String[]{"attendance_date",
				"leaveoffice_date","break_date","employee_id"};

		try {
			// 画面表示されている日付より勤怠マスタよりemployee_idがあるか確認
			Cursor c = db.rawQuery("SELECT mk.employee_id FROM " +
					"mst_kintai mk WHERE mk.kintai_date=? AND mk.employee_id=?", 
					new String[]{update_param[3],employee_id});
			Cursor c2 = db.rawQuery("SELECT ma.employee_id FROM " +
					"mst_attendance ma WHERE ma.attendance_date=? AND ma.employee_id=?", 
					new String[]{update_param[3],employee_id});
			Cursor c3 = db.rawQuery("SELECT ml.employee_id FROM " +
					"mst_leaveoffice ml WHERE ml.leaveoffice_date=? AND ml.employee_id=?", 
					new String[]{update_param[3],employee_id});
			
			Cursor c4 = db.rawQuery("SELECT mb.employee_id FROM " +
					"mst_break mb" + " WHERE mb.break_date=? AND mb.employee_id=?", 
					new String[]{update_param[3],employee_id});
			
			// 表示されている日次画面の日付の出勤・退勤マスタのデータがあるかどうか確認
			// なければInsert、あればUpdateを行う。
			if (c.moveToFirst()){
				// 社員IDがあれば、休憩マスタのデータの有無の確認
				employee_id = c.getString(0);
				
				int atd_cnt = c2.getCount(); 	// 出勤記録の有無(0:無 1:有)
				int lo_cnt = c3.getCount();  	// 退勤記録の有無(0:無 1:有)
				int break_cnt = c4.getCount();  // 休憩記録の有無(0:無 1:有)

				// 出勤・退勤・休憩記録の有無によってInsert、updateを分岐
				// 出勤記録処理
				ContentValues atd_cv = new ContentValues();
				if (atd_cnt == 0) {
					// 出勤記録がなければInsert
					atd_cv.put("employee_id", employee_id);
					atd_cv.put(put_pos[0], update_param[3]);
					atd_cv.put(put_key[0], update_param[0]);
					atd_cv.put("regist_datetime", currenttime);
					atd_cv.put("update_datetime", currenttime);
					db.insert(DbConstants.TABLE_NAME2, null, atd_cv);
				} else {
					// 出勤記録あればUpdate
					atd_cv.put(put_key[0], update_param[0]);
					db.update(put_table[0], atd_cv, put_pos[0]+"=? AND "+put_pos[3]+"=?",
							new String[]{update_param[3],employee_id});
				}
				//Log.d("debug", String.valueOf(lo_cnt));

				// 退勤記録処理
				ContentValues lo_cv = new ContentValues();
				if (lo_cnt == 0) {
					// 退勤記録がなければInsert
					lo_cv.put("employee_id", employee_id);
					lo_cv.put(put_pos[1], update_param[3]);
					lo_cv.put(put_key[1], update_param[1]);
					lo_cv.put("regist_datetime", currenttime);
					lo_cv.put("update_datetime", currenttime);
					db.insert(DbConstants.TABLE_NAME3, null, lo_cv);
				} else {
					// 退勤記録あればUpdate
					lo_cv.put(put_key[1], update_param[1]);
					db.update(put_table[1], lo_cv, put_pos[1]+"=? AND "+put_pos[3]+"=?",
							new String[]{update_param[3],employee_id});
				}
				// 休憩記録処理
				ContentValues break_cv = new ContentValues();
				if (break_cnt == 0) {
					// 休憩記録がなければInsert
					break_cv.put("employee_id", employee_id);
					break_cv.put(put_pos[2], update_param[3]);
					break_cv.put(put_key[2], update_param[2]);
					break_cv.put("regist_datetime", currenttime);
					break_cv.put("update_datetime", currenttime);
					db.insert(DbConstants.TABLE_NAME4, null, break_cv);
				} else {
					// 休憩記録あればUpdate
					break_cv.put(put_key[2], update_param[2]);
					db.update(put_table[2], break_cv, put_pos[2]+"=? AND "+put_pos[3]+"=?",
							new String[]{update_param[3],employee_id});
				}

			} else {
				// 社員IDがない場合(当日日付でないや、未来日付等)全てをInsert
				// 勤怠マスタへの登録(社員id発行)
				ContentValues cv = new ContentValues();
				cv.put("employee_id", employee_id);
				cv.put("kintai_date", update_param[3]);
				db.insert(DbConstants.TABLE_NAME1, null, cv);
				Cursor c5 = db.rawQuery("SELECT mk.employee_id FROM " +
						"mst_kintai mk WHERE mk.kintai_date=? AND mk.employee_id=?", 
						new String[]{update_param[3],employee_id});
				
				if (c5.moveToFirst()){
					// 登録されていれば、その社員ID・日付を元に出勤・退勤・休憩マスタ登録
					for (int i = 0; i < put_key.length; i++){
						//Log.d("debug",update_param[3]);
						ContentValues cv2 = new ContentValues();
						// Insertデータ生成
						cv2.put("employee_id", c5.getString(0));
						cv2.put(put_pos[i], update_param[3]); // 日付セット
						cv2.put(put_key[i], update_param[i]); // 時刻セット
						cv2.put("regist_datetime", currenttime);
						cv2.put("update_datetime", currenttime);
						db.insert(put_table[i], null, cv2);
					}
				}
			}
		} finally {
			db.close();
		}
	}

	/*
	 * 日次画面表示用のデフォルトの設定時刻取得メソッド
	 * */
	public String[] DailyDefaultTime(){
		SQLiteDatabase db = helper.getWritableDatabase();
		String iniparam[] = new String[3];
		try {
			Cursor c = db.rawQuery("SELECT mi.start_time, mi.end_time, mi.break_time " +
					"FROM " + "mst_initime mi", null);
			if (c.moveToFirst()){
				iniparam[0] = c.getString(0); 	// 始業時刻
				iniparam[1] = c.getString(1);	// 終業時刻
				iniparam[2] = c.getString(2);	// 休憩時刻
			}
		} finally {
			db.close();
		}
		return iniparam;
	}

	/*
	 * 日次画面の休憩時間timePickerDialog用の取得メソッド
	 * */
	public String BreakTimeGet(String employee_id, String date){
		SQLiteDatabase db = helper.getWritableDatabase();
		String break_time = null;

		try {
			// 社員idを取得
			Cursor c = db.rawQuery("SELECT mb.break_time FROM mst_break mb " +
					"WHERE mb.employee_id=? AND mb.break_date=?", 
					new String[]{employee_id, date});
			//Log.d("debug",employee_id);
			//Log.d("debug",date);
			
			if (c.moveToFirst() && c.getCount() != 0){
				// 休憩記録があれば
				//Log.d("debug","休憩記録があれば");
				break_time = c.getString(0);
			} else {
				//Log.d("debug","休憩記録がなければ時刻設定マスタ");
				// 休憩記録がなければ時刻設定マスタの休憩時間を取得
				String[] default_param = DailyDefaultTime();
				break_time = default_param[2];
			}
		} finally {
			db.close();
		}
		return break_time;
	}

	/*
	 * 日次画面におけるデータ削除メソッド
	 */
	public void DailyDelete(String date, String employee_id) {
		SQLiteDatabase db = helper.getWritableDatabase();
		try {
			// 社員Id取得
			Cursor c = db.rawQuery("SELECT mk.employee_id FROM " +
					"mst_kintai mk" + " WHERE mk.kintai_date=? AND mk.employee_id=?", 
					new String[]{date, employee_id});
			if (c.moveToFirst()) {
				// 当該社員ID・日付に紐づく出勤・退勤・休憩マスタのデータを削除
				// 勤怠マスタのデータは削除後の再登録のケースを考慮し、削除は行わない
				db.delete(DbConstants.TABLE_NAME2, "attendance_date=? AND " +
						"employee_id=?", new String[]{date, employee_id});
				db.delete(DbConstants.TABLE_NAME3, "leaveoffice_date=? AND " +
						"employee_id=?", new String[]{date, employee_id});
				db.delete(DbConstants.TABLE_NAME4, "break_date=? AND " +
						"employee_id=?", new String[]{date, employee_id});
			}
		} finally {
			db.close();
		}
	}
	
	/*
	 * 日次画面の「前」「次」押下時の勤怠記録取得メソッド
	 * */
	public String[] DailyGetParam(String date, String employee_id){
		String kintaiparam[] = new String[3];
		String employee_id_str = "SELECT mk.employee_id FROM mst_kintai mk " +
				"WHERE mk.kintai_date=? AND mk.employee_id=?";
		
		String[] default_param = DailyDefaultTime(); // 時刻設定マスタのデータ
		SQLiteDatabase db = helper.getWritableDatabase();
		
		try {
			Cursor c5 = db.rawQuery(employee_id_str, new String[]{date,employee_id});
			if (c5.moveToFirst()){
				// 社員IDがあれば
				// 出勤時刻取得SQL
				Cursor c = db.rawQuery("SELECT ma.attendance_time FROM " +
						"mst_attendance ma WHERE ma.employee_id=(" + employee_id_str + ") " +
								"AND ma.attendance_date=?", 
								new String[]{date,employee_id,date});
				
				// 出退勤記録の有無によって、時刻設定マスタからのデータ取得をすべきか決定
				if (c.moveToFirst()){
					// 出勤記録がある場合
					kintaiparam[0] = c.getString(0); // 出勤時刻
				} else {
					// 出勤記録がない場合、時刻設定マスタから3つデータ取得
					kintaiparam = default_param;
					return kintaiparam;
				}
				// 退勤時刻取得SQL
				Cursor c2 = db.rawQuery("SELECT ml.leaveoffice_time FROM " +
						"mst_leaveoffice ml WHERE ml.employee_id=(" + employee_id_str + ") " +
								"AND ml.leaveoffice_date=?", new String[]{date,employee_id,date});
				
				if (c2.moveToFirst()){
					// 退勤記録がある場合
					kintaiparam[1] = c2.getString(0); // 退勤時刻
				} else {
					// 退勤記録がない場合、時刻設定マスタから(退勤・休憩時間のみ)データ取得
					kintaiparam[1] = default_param[1];
					kintaiparam[2] = default_param[2];
					return kintaiparam;
				}
				// 休憩時刻取得SQL
				Cursor c3 = db.rawQuery("SELECT mb.break_time FROM " +
						"mst_break mb WHERE mb.employee_id=(" + employee_id_str + ") " +
								"AND mb.break_date=?", new String[]{date,employee_id,date});
				
				if (c3.moveToFirst()){
					// 休憩記録がある場合
					kintaiparam[2] = c3.getString(0); // 休憩時刻
				}
			} else {
				// 社員IDがなければ
				kintaiparam = default_param;
				return kintaiparam;
			}
		} finally {
			db.close();
		}
		return kintaiparam;
	}
	
	/*
	 * 勤怠情報出力メソッド
	 */
	public void MonthServiceInfo(String employee_id, String month_first,
			 					String month_last, String u, String file_name){
		
		// 社員IDに紐づく月別勤怠情報を取得
		SQLiteDatabase db = helper.getWritableDatabase();
		//SELECT * from mst_attendance WHERE attendance_date BETWEEN '2013/01/14' AND '2013/01/20'
		String month_kintai_str = "SELECT mk.kintai_date, ma.attendance_time, " +
				"ml.leaveoffice_time FROM mst_kintai mk, mst_attendance ma, " +
				"mst_leaveoffice ml WHERE mk.employee_id=? AND " +
				"mk.employee_id = ma.employee_id AND " +
//				"mk.employee_id = ml.employee_id AND " +
//				"mk.employee_id = mb.employee_id AND " +
				"ma.attendance_date BETWEEN ? AND ?";
		
		//日付,出勤時刻,退勤時刻,労働時間
		//2013/01/04,09:23,18:23,08:00
		Log.d("debug",employee_id);
		Log.d("debug",month_first);
		Log.d("debug",month_last);
		Log.d("debug",u);
		Log.d("debug",file_name);
		
		Cursor c = db.rawQuery(month_kintai_str, 
				new String[]{employee_id,month_first,month_last});
		
		if (c.moveToFirst()){
			
			StringBuilder sb = new StringBuilder();
			String kintai_date = c.getString(0); 	// 勤怠日付
			String kintai_attend = c.getString(1); 	// 出勤時刻
			String kintai_leave = c.getString(2); 	// 退勤時刻
			
			// TODO 合計時間計算
			sb.append(kintai_date + ",");
			sb.append(kintai_attend + ",");
			sb.append(kintai_leave + ",");
			
			// TODO CSV出力
			try {
				URL url = new URL(u); // URLクラスのインスタンス作成
				URLConnection con = url.openConnection(); // コネクションを開く。
				OutputStream os = con.getOutputStream();
				byte [] output = sb.toString().getBytes("Shift_JIS");
				
//				File file = new File(u);
//				PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
//		        pw.println(sb.toString());
//		        pw.close();
				
				
				
//				File file = new File(u);
//				FileWriter filewriter = new FileWriter(file, true);
//				filewriter.write(sb.toString());
				//os.close();
				
				
				
				
				//FileOutputStream fp = new FileOutputStream(file_name);
				//OutputStreamWriter ow = new OutputStreamWriter(fp, "Shift_JIS");
				//int contents;
				
				
				
				
				
			} catch (Exception e) {
				
			} finally{
				db.close();
			}
			
			
		}
		
	}
	
	
	
	
	
}