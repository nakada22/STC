package jp.co.timecard;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

import jp.co.timecard.db.Dao;
import jp.co.timecard.db.DbConstants;
import jp.co.timecard.db.DbOpenHelper;
import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;

public class BaseSetListActivity extends Activity {

	ArrayList<HashMap<String, String>> mList;
	BaseSetListAdapter adapter;
	ListView listview;
	View getView;
	int mYear;
	int mMonth;
	int hourOfDay;
	int minute;
	boolean is24HourView;
	DecimalFormat df = new DecimalFormat("00");
	SimpleDateFormat timestamp_sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.base_set_list);

		listview = (ListView) findViewById(R.id.listview);
		mList = new ArrayList<HashMap<String, String>>();
		
		// リスト表示
		ListViewDisp();

	}

	/*
	 * リスト表示
	 */
	private void ListViewDisp() {

		// 時刻設定マスタよりデータ取得
		Dao dao = new Dao(this);
		String[] ini_time = dao.DailyDefaultTime();
		HashMap<String, String> item;
		
		// 初期表示
		item = new HashMap<String, String>();
		item.put("title", "始業時刻設定");
		item.put("desc", "現在の設定 " + ini_time[0]);
		mList.add(item);

		item = new HashMap<String, String>();
		item.put("title", "終業時刻設定");
		item.put("desc", "現在の設定 " + ini_time[1]);
		mList.add(item);
		
		item = new HashMap<String, String>();
		item.put("title", "休憩時間設定");
		item.put("desc", "現在の設定 " + ini_time[2]);
		mList.add(item);
		
		adapter = new BaseSetListAdapter(this, mList);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
					String currenttime = timestamp_sdf.format(Calendar
						.getInstance().getTime());

				// タイトル、設定
				String title = mList.get(position).get("title");
				//String desc = mList.get(position).get("desc");
				//Log.d("debug", desc);
				//Log.d("debug", String.valueOf(item.entrySet()));
				ListViewClick(title, title, currenttime, position);
			}
		});
	}

	/*
	 * 時刻設定リストクリック
	 */
	private void ListViewClick(String settitle, final String title,
			final String update_date_time, final int position) {
		TimePickerDialog timePickerDialog;
		final ContentValues cv = new ContentValues();
		final DbOpenHelper helper = new DbOpenHelper(getApplicationContext());
		final SQLiteDatabase db = helper.getWritableDatabase();

		// 時刻設定マスタよりデータ取得
		Dao dao = new Dao(this);
		final String[] ini_time = dao.DailyDefaultTime();

		// 始業時刻設定がクリックされた場合
		if (title == "始業時刻設定") {
			hourOfDay = Integer.parseInt(ini_time[0].substring(0, 2));
			minute = Integer.parseInt(ini_time[0].substring(3, 5));
		} else if (title == "終業時刻設定") {
			hourOfDay = Integer.parseInt(ini_time[1].substring(0, 2));
			minute = Integer.parseInt(ini_time[1].substring(3, 5));
		} else if (title == "休憩時間設定") {
			hourOfDay = Integer.parseInt(ini_time[2].substring(0, 2));
			minute = Integer.parseInt(ini_time[2].substring(3, 5));
		}
		is24HourView = true;

		TimePickerDialog.OnTimeSetListener TimeSetListener = new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

				// 初期表示時刻を設定、セット時更新処理
				StringBuilder sb = new StringBuilder()
						.append(df.format(hourOfDay)).append(":")
						.append(df.format(minute));
				String a = sb.toString();

				ListView listView;
			    listView = (ListView)findViewById(R.id.listview);

				try {
					if (title == "始業時刻設定") {
						final View listDataView = listView.getChildAt(0);
						TextView time_tv = (TextView) listDataView.findViewById(R.id.desc);
						cv.put(DbConstants.COLUMN_START_TIME, a);
						time_tv.setText("現在の設定 " + a); // 設定時刻画面反映
						
					} else if (title == "終業時刻設定") {
						final View listDataView = listView.getChildAt(1);
						TextView time_tv = (TextView) listDataView.findViewById(R.id.desc);
						cv.put(DbConstants.COLUMN_END_TIME, a);
						time_tv.setText("現在の設定 " + a); // 設定時刻画面反映
						
					} else if (title == "休憩時間設定") {
						final View listDataView = listView.getChildAt(2);
						TextView time_tv = (TextView) listDataView.findViewById(R.id.desc);
						cv.put(DbConstants.COLUMN_BREAK_TIME, a);
						time_tv.setText("現在の設定 " + sb); // 設定時刻画面反映
					}

					cv.put(DbConstants.COLUMN_UPDATE_DATETIME, update_date_time);
					db.update(DbConstants.TABLE_NAME5, cv, null, null);
				} finally {
					db.close();
				}
			}
		};

		// 時刻設定ダイアログの作成
		timePickerDialog = new TimePickerDialog(this, TimeSetListener,
				hourOfDay, minute, is24HourView);
		// timePickerDialog.setTitle("時間設定");
		timePickerDialog.setMessage(settitle);
		timePickerDialog.show();
	};
}
