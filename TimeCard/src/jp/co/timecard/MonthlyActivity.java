package jp.co.timecard;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.co.timecard.db.Dao;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 月次画面
 */
public class MonthlyActivity extends Activity implements View.OnClickListener {

	private Calendar cal;
	private int mYear;
	private int mMonth;
	public String employee_id;
	
	final int PRE_MONTH = -1;
	final int NEX_MONTH = 1;
	final String con_url = "http://sashihara.web.fc2.com/employ_info.csv";
	//TextView mon_target_tv = (TextView) findViewById(R.id.mon_target);
	private String output_url = "http://sashihara.web.fc2.com/";
	DecimalFormat df = new DecimalFormat("00");
	
	
	Spinner spinner;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.monthly_main);

		// 表示する年月（最初は現在の年月）
		cal = Calendar.getInstance();

		// 社員情報プルダウン表示
		employ_select();

		// 初期表示月のセット
		setTargetMonth(0);

		// 表示月のカレンダーを作成
		// createCalender(employee_id);

		// 前月ボタン
		findViewById(R.id.button_pre_month).setOnClickListener(this);
		// 次月ボタン
		findViewById(R.id.button_next_month).setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		// 選択されている社員名
		String employ_name = spinner.getSelectedItem().toString();
					
		switch (v.getId()) {
		case R.id.button_pre_month:
			setTargetMonth(PRE_MONTH);
			
			// 「社員名」未選択時は、日別勤怠表示はしない
			if (!employ_name.equals("社員情報を選択して下さい")) {
				createCalender(employee_id);
			}
			break;
		case R.id.button_next_month:
			setTargetMonth(NEX_MONTH);
			
			// 「社員名」未選択時は、日別勤怠表示はしない
			if (!employ_name.equals("社員情報を選択して下さい")) {
				createCalender(employee_id);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.menu, menu);

		// Icon Set
		menu.findItem(R.id.finish).setIcon(
				android.R.drawable.ic_menu_close_clear_cancel);
		menu.findItem(R.id.return_top).setIcon(
				android.R.drawable.ic_menu_revert);
		menu.findItem(R.id.rec_export).setIcon(android.R.drawable.ic_menu_save);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	/*
	 * メニューが選択された時の処理
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()) {

		case R.id.return_top:
			// トップ画面へ戻る
			Intent i2 = new Intent();
			i2.setClassName("jp.co.timecard", "jp.co.timecard.TopActivity");
			startActivity(i2);
			return true;

		case R.id.finish:
			// 終了する
			AlertDialog.Builder dlg = new AlertDialog.Builder(this);
			dlg.setMessage("アプリを終了してよろしいですか？");
			dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// finish();
					// アクティビティをバックグラウンドに移動する
					moveTaskToBack(true);
				}
			});
			dlg.setNegativeButton("キャンセル",
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}
					});
			dlg.create().show();
			return true;

		case R.id.rec_export:
			TextView mon_target_tv = (TextView) findViewById(R.id.mon_target);
			
			// TODO 勤怠記録出力
			String file_name = mon_target_tv.getText().toString(); // 勤怠記録ファイル名
			//Log.d("debug",file_name);
			
			// 「年」「月」取り除き
			file_name = file_name.replaceAll("年","");
			file_name = file_name.replaceAll("月","");
			
			StringBuilder sb = new StringBuilder();
			sb.append(file_name);
			
			if (file_name.length() == 5) {
				// YYYYMの時
				sb.insert(4, "0");// 0パディング
			}
			sb.append("_" + employee_id);
			file_name = sb.toString();
			
			String url = output_url + file_name;
		    int start_day = cal.getActualMinimum(Calendar.DAY_OF_MONTH);
		    int end_day = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		    
		    String month_first = sb.substring(0, 4)+"/" +sb.substring(4, 6)+"/" 
					+ df.format(start_day);
		    String month_last = sb.substring(0, 4)+"/" +sb.substring(4, 6)+"/" 
					+ df.format(end_day);
		    
		    Dao dao = new Dao(this);
			dao.MonthServiceInfo(employee_id,month_first,month_last,url,sb.toString(),
					getApplicationContext());
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * 表示対象の年月を取得。
	 * 
	 * @param value
	 *            前月か次月ボタンの値
	 */
	public void setTargetMonth(int value) {
		TextView mon_target_tv = (TextView) findViewById(R.id.mon_target);
		cal.add(Calendar.MONTH, value);
		mYear = cal.get(Calendar.YEAR);
		mMonth = cal.get(Calendar.MONTH);
		cal.set(mYear, mMonth, 1);
		mon_target_tv.setText(String.valueOf(mYear) + "年" + String.valueOf(mMonth + 1)
				+ "月");
	}

	/**
	 * 月次データを取得し、ListViewに表示する。
	 */
	public void createCalender(String employee_id) {

		final ArrayList<DailyState> dayOfMonth = new ArrayList<DailyState>();
		// 選択されている社員名
		String employ_name = spinner.getSelectedItem().toString();
		Dao dao = new Dao(this);

		int dom = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		for (int i = 1; i <= dom; i++) {
			// DBから選択された社員の勤怠情報を取得してくる（なければ空欄）
			DailyState ds = new DailyState();

			String crrent_mMonth = String.format("%1$02d", mMonth + 1);
			String disp_date = String.format("%1$02d", i) + "日";
			ds.setDate(disp_date);
			
			String date_param = mYear + "/" + crrent_mMonth + "/"
					+ String.format("%1$02d", i);
			ds.setTargetDate(date_param);

			// 戻り値より出勤時刻・退勤時刻を取得
			String[] daily_param = dao.MonthlyList(date_param, employee_id);
			String attendance_time = daily_param[0];
			String leaveoffice_time = daily_param[1];
			String break_time = daily_param[2];

			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
			try {
				if (attendance_time != "" && leaveoffice_time != ""
						&& break_time != "") {
					Date at = sdf.parse(attendance_time);
					Date lt = sdf.parse(leaveoffice_time);
					Date bt = sdf.parse(break_time);
					// 全ての時間があれば合計時間計算、セット
					long sumtime = lt.getTime() - at.getTime() - bt.getTime()
							+ 1000 * 60 * 60 * 6;
					ds.setWorkHour(sdf.format(sumtime));
				}
				ds.setAttendance(attendance_time);
				ds.setLeave(leaveoffice_time);
				ds.setBreakTime(break_time);
				ds.setMonthEmploySelect(employee_id+":"+employ_name); // 社員id:社員名
				dayOfMonth.add(ds);
			} catch (java.text.ParseException e) {
				e.printStackTrace();
			}
		}
		
		MonthlyAdapter la = new MonthlyAdapter(getApplicationContext(),
				android.R.layout.simple_list_item_1, dayOfMonth);
		ListView lv = (ListView) findViewById(R.id.listview);
		lv.setAdapter(la);
		lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterview, View view,
					int position, long id) {
				DailyState ds = dayOfMonth.get(position);
				Intent i = new Intent(MonthlyActivity.this, DailyActivity.class);
				i.putExtra("DailyState", ds);
				startActivity(i);
			}
		});
	}

	/*
	 * 社員情報プルダウン表示
	 */
	private void employ_select() {
		TopActivity ta = new TopActivity();

		// 社員情報ファイルよりデータ取得
		List<String> employ_list = new ArrayList<String>();// 社員リスト

		// ネットから社員データ取得
		final Map<String, String> employ_data = ta.doNet(con_url);

		Set keySet = employ_data.keySet();
		Iterator keyIte = keySet.iterator();
		int i = 0;

		while (keyIte.hasNext()) {
			if (i == 0) {
				// 最初だけ「社員情報選択」の文言入れる
				employ_list.add("社員情報を選択して下さい");
			}
			String key = keyIte.next().toString(); // 社員ID
			String value = employ_data.get(key); // 社員氏名
			employ_list.add(value);

			i++;
			// System.out.println(key + "=" + value);
		}
		// Log.d("debug", employ_list.toString());

		spinner = (Spinner) findViewById(R.id.month_employ_select);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, employ_list);

		adapter.setDropDownViewResource(R.layout.employ_list);

		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				// 変更時、選択された社員の月次情報を画面に表示する。
				Spinner spinner = (Spinner) parent;
				String item = (String) spinner.getSelectedItem();

				final Set keySet = employ_data.keySet();
				final Iterator keyIte = keySet.iterator();

				while (keyIte.hasNext()) {
					employee_id = keyIte.next().toString(); 	// 社員ID
					String name = employ_data.get(employee_id); // 社員氏名
					
					if (item.equals("社員情報を選択して下さい")) {
						// 初期化(非表示)デフォルト時
						final ArrayList<DailyState> dayOfMonth = new ArrayList<DailyState>();
						MonthlyAdapter la = new MonthlyAdapter(getApplicationContext(),
								android.R.layout.simple_list_item_1, dayOfMonth);
						ListView lv = (ListView) findViewById(R.id.listview);
						lv.setAdapter(la);
						return;
					} else if (item.equals(name)) {
						// 名前選択時に「選択氏名」と社員データが一致すれば
						// 月次情報を表示
						createCalender(employee_id);
						Toast.makeText(MonthlyActivity.this,
								name+"さんの月次情報を表示しました。", Toast.LENGTH_LONG)
								.show();
						return;
					}

				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				// Spinnerで何も選択されなかった場合の動作
				// 初期値はココでセットする
			}
		});
	}

}