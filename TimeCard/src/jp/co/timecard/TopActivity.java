package jp.co.timecard;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import jp.co.timecard.db.TopDao;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public class TopActivity extends Activity implements View.OnClickListener {
	int mYear;
	int mMonth;
	int hourOfDay;
	int minute;
	boolean is24HourView = true;
	Calendar c = Calendar.getInstance();
	DecimalFormat df = new DecimalFormat("00");
	Date date = new Date();
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
	SimpleDateFormat timestamp_sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	Spinner spinner;
	Handler mHandler = new Handler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main);

		findViewById(R.id.checkBox1).setOnClickListener(this);
		findViewById(R.id.attendance).setOnClickListener(this);
		findViewById(R.id.leaveoffice).setOnClickListener(this);
		findViewById(R.id.ini).setOnClickListener(this);

		mYear = c.get(Calendar.YEAR);
		mMonth = c.get(Calendar.MONTH) + 1;
		hourOfDay = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);

		// TextViewに線をセット
		TextView textView_line = (TextView) findViewById(R.id.textView_line);
		TextView textView_line2 = (TextView) findViewById(R.id.textView_line2);
		TextView textView_line3 = (TextView) findViewById(R.id.textView_line3);
		TextView textView_line4 = (TextView) findViewById(R.id.textView_line4);

		textView_line.setBackgroundResource(R.layout.line);
		textView_line2.setBackgroundResource(R.layout.line);
		textView_line3.setBackgroundResource(R.layout.line);
		textView_line4.setBackgroundResource(R.layout.line);

		CurrentDisp();
		TopPreInsert();
		employ_select(); // 「社員情報選択」表示
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.checkBox1:
			checkBoxChange();
			break;
		case R.id.attendance:
			AttendChange();
			break;
		case R.id.leaveoffice:
			LeaveofficeChange();
			break;
		case R.id.ini:
			IniChange();
			break;
		default:
			break;
		}
	}

	/*
	 * トップ画面を開くと同時に勤怠マスタ(当日日付無ければ) 時刻設定マスタへデータ登録・既に当日の勤怠記録があれば画面表示 初期パスワード発行
	 */
	public void TopPreInsert() {
		final TextView start_tv = (TextView) findViewById(R.id.start_time2); // 始業時刻
		final TextView end_tv = (TextView) findViewById(R.id.last_time2); // 終業時刻
		final TextView break_tv = (TextView) findViewById(R.id.bleak_time2); // 休憩時間
		final TextView sumtime_tv = (TextView) findViewById(R.id.sum_time2); // 合計時間

		TopDao td = new TopDao(getApplicationContext());

		td.preTimeSave(timestamp_sdf.format(date)); //
		td.TopTimeDisp(sdf.format(date), start_tv, end_tv, break_tv, sumtime_tv);
		td.prePassWordSave(); // 初期パスワード発行
		// http://zuccyimemo.blog.fc2.com/blog-entry-1.html

	}

	// 現在時刻
	public void Current_date(TextView tv) {
		Calendar c = Calendar.getInstance();
		hourOfDay = c.get(Calendar.HOUR_OF_DAY);
		minute = c.get(Calendar.MINUTE);
		is24HourView = true;

		StringBuilder sb = new StringBuilder().append(df.format(hourOfDay))
				.append(":").append(df.format(minute));
		tv.setText(sb);
	}

	/*
	 * 設定ボタンクリック
	 */
	public void IniChange() {
		TimePickerDialog timePickerDialog;

		// 画面表示されている時刻取得
		final TextView tv = (TextView) findViewById(R.id.currenttime);
		String disptime = String.valueOf(tv.getText());

		hourOfDay = Integer.parseInt(disptime.substring(0, 2));
		minute = Integer.parseInt(disptime.substring(3, 5));
		is24HourView = true;

		TimePickerDialog.OnTimeSetListener TimeSetListener = new TimePickerDialog.OnTimeSetListener() {
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

				// 初期表示時刻を設定(画面表示されている時刻)
				StringBuilder sb = new StringBuilder()
						.append(df.format(hourOfDay)).append(":")
						.append(df.format(minute));
				Current_date(tv);
				tv.setText(sb);
			}
		};

		// 時刻設定ダイアログの作成
		timePickerDialog = new TimePickerDialog(TopActivity.this,
				TimeSetListener, hourOfDay, minute, is24HourView);
		// timePickerDialog.setTitle("時間設定");
		timePickerDialog.setMessage("出退勤時刻設定");
		timePickerDialog.show();

		// @Override
		// public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
		// {
		// do nothing
		// }

	};

	/*
	 * チェックボックスチェック時の処理
	 */
	public void checkBoxChange() {
		final Button inibtn = (Button) findViewById(R.id.ini);
		final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);

		// チェックの on/off 切り替えを無効とする
		checkBox.setChecked(!checkBox.isChecked());
		inibtn.setVisibility(View.INVISIBLE);
		if (checkBox.isChecked() == true) {
			inibtn.setVisibility(View.INVISIBLE);
		} else {
			inibtn.setVisibility(View.VISIBLE);
		}
	}

	/*
	 * 出勤ボタン押下処理
	 */
	public void AttendChange() {
		spinner = (Spinner) findViewById(R.id.employ_select);

		// 選択されている社員名
		String employ_name = spinner.getSelectedItem().toString();

		// 「社員名」未選択時は、登録処理を行わない
		if (employ_name.equals("社員情報を選択して下さい")) {
			Toast.makeText(TopActivity.this, "社員名が未選択です", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		final ImageButton imgbutton = new ImageButton(this);
		imgbutton.setImageResource(R.drawable.atendance);
		final TextView start_tv = (TextView) findViewById(R.id.start_time2);
		boolean atd_flg;
		// TextView atd_tv = (TextView) findViewById(R.id.currenttime);

		String currenttime = timestamp_sdf.format(Calendar.getInstance()
				.getTime()); // 現在時刻

		// 現在時刻使用チェック時、現在時刻で出勤マスタへDB登録
		TopDao td = new TopDao(getApplicationContext());
		atd_flg = td.AttendanceSave(sdf.format(date), currenttime, null,
				employ_name);

		// 既に退勤済みの場合
		if (atd_flg == false) {
			Toast.makeText(TopActivity.this, "既に退勤済みです", Toast.LENGTH_SHORT)
					.show();
		} else {
			if (imgbutton.isEnabled() == true) {
				Toast.makeText(TopActivity.this, "出勤", Toast.LENGTH_SHORT)
						.show();
			}
		}

		td.AttendanceTimeDisp(sdf.format(date), start_tv, employ_name);

		// 出勤ボタン押下後に、Spinnerを初期値にもどす(３秒間Sleepさせる)
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
				}
				mHandler.post(new Runnable() {
					public void run() {
						// Spinnerを初期値にもどす
						employ_select();
					}
				});

			}
		}).start();
	}

	/*
	 * 退勤ボタン押下処理
	 */
	public void LeaveofficeChange() {
		final ImageButton leaveimgbutton = new ImageButton(this);
		leaveimgbutton.setImageResource(R.drawable.leave);

		final TextView start_tv = (TextView) findViewById(R.id.start_time2); // 始業時刻
		final TextView end_tv = (TextView) findViewById(R.id.last_time2); // 終業時刻
		final TextView break_tv = (TextView) findViewById(R.id.bleak_time2); // 休憩時間
		final TextView sumtime_tv = (TextView) findViewById(R.id.sum_time2); // 合計時間
		spinner = (Spinner) findViewById(R.id.employ_select);

		// 選択されている社員名
		String employee_name = spinner.getSelectedItem().toString();

		// 「社員名」未選択時は、登録処理を行わない
		if (employee_name.equals("社員情報を選択して下さい")) {
			Toast.makeText(TopActivity.this, "社員名が未選択です", Toast.LENGTH_SHORT)
					.show();
			return;
		}

		// 退勤マスタ・休憩マスタへDB登録（画面で設定した時刻）
		TextView leave_tv = (TextView) findViewById(R.id.currenttime);
		TopDao td = new TopDao(getApplicationContext());

		// まだ未出勤の場合は、退勤記録をしないようにする
		String currenttime = timestamp_sdf.format(Calendar.getInstance()
				.getTime());
		boolean leave_flg;

		// 現在時刻使用チェック時、現在時刻で退勤時刻を記録
		leave_flg = td.LeaveofficeSave(sdf.format(date), currenttime, null,
				employee_name);

		// まだ未出勤の場合
		if (leave_flg == false) {
			Toast.makeText(TopActivity.this, "先に出勤記録を行って下さい。",
					Toast.LENGTH_SHORT).show();
		} else {
			if (leaveimgbutton.isEnabled() == true) {
				Toast.makeText(TopActivity.this, "退勤", Toast.LENGTH_SHORT)
						.show();
			}
			td.preBreakSave(sdf.format(date), currenttime, employee_name);
			td.TopTimeDisp(sdf.format(date), start_tv, end_tv, break_tv,
					sumtime_tv);

		}

		//
		// 退勤ボタン押下後に、Spinnerを初期値にもどす(３秒間Sleepさせる)
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
				}
				mHandler.post(new Runnable() {
					public void run() {
						// Spinnerを初期値にもどす
						employ_select();
					}
				});
			}
		}).start();

	}

	/*
	 * 月次リストボタンクリック時の処理
	 */
	public void MonthlyListChange() {
		final ImageButton monthlstimgbtn = new ImageButton(this);
		monthlstimgbtn.setImageResource(R.drawable.tsukiji);
		final Intent intent = new Intent();

		intent.setClassName("jp.co.timecard", "jp.co.timecard.MonthlyActivity");
		startActivity(intent);

	}

	/*
	 * デフォルト画面表示
	 */
	public void CurrentDisp() {

		Calendar calender = Calendar.getInstance();
		int week = calender.get(Calendar.DAY_OF_WEEK) - 1;// 1(日曜)～7(土曜)
		String[] week_name = { "日", "月", "火", "水", "木", "金", "土" };
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy'/'MM'/'dd'('"
				+ week_name[week] + "')'");
		Date date = new Date();

		// 現在日表示
		TextView textView = (TextView) findViewById(R.id.datetime);
		textView.setText(sdf.format(date));

		// 現在時刻表示
		TextView tv = (TextView) findViewById(R.id.currenttime);
		Current_date(tv);

		// チェックボックス true 設定ボタン非表示
		CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox1);
		checkBox.setChecked(true);

		Button inibtn = (Button) findViewById(R.id.ini);
		inibtn.setVisibility(View.INVISIBLE);
	}

	/**************
	 * オプションメニュー
	 **************/
	// メニューアイテム識別用のID
	private static final int MENU_ID_A = 0;
	private static final int MENU_ID_B = 1;
	private static final int MENU_ID_C = 2;

	// オプションメニューの作成
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// メニューアイテムの追加
		MenuItem menuItemA, menuItemB, menuItemC;

		menuItemA = menu.add(Menu.NONE, MENU_ID_A, Menu.NONE, "月次リスト");
		menuItemA.setIcon(android.R.drawable.ic_menu_month);

		menuItemB = menu.add(Menu.NONE, MENU_ID_B, Menu.NONE, "設定");
		menuItemB.setIcon(android.R.drawable.ic_menu_manage);

		menuItemC = menu.add(Menu.NONE, MENU_ID_C, Menu.NONE, "終了");
		menuItemC.setIcon(android.R.drawable.ic_menu_close_clear_cancel);
		return true;
	}

	// メニューが選択された時の処理
	public boolean onOptionsItemSelected(MenuItem item) {
		final TopDao td = new TopDao(getApplicationContext());

		// パスワード入力ダイアログを表示
		switch (item.getItemId()) {

		case MENU_ID_A:
			// 月次リスト
			String pass = td.PassWordGet("monthly");
			PassWordInput(pass, "jp.co.timecard.MonthlyActivity");
			return true;

		case MENU_ID_B:
			final CharSequence[] items = { "基本時間設定", "パスワード設定" };

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("設定");
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					if (item == 0) {
						// 基本時間設定クリック時
						String pass = td.PassWordGet("basetime");
						PassWordInput(pass,
								"jp.co.timecard.BaseSetListActivity");

					} else if (item == 1) {
						// パスワード設定クリック時
						String pass = td.PassWordGet("password");
						PassWordInput(pass,
								"jp.co.timecard.PassWordSetListActivity");

					}
				}
			});
			builder.create();
			builder.show();
			return true;

		case MENU_ID_C:
			// アクティビティをバックグラウンドに移動する
			moveTaskToBack(true);
			return true;
		}
		return false;
	}

	/*
	 * パスワード入力表示メソッド
	 */
	public void PassWordInput(final String password, final String change_place) {
		final Intent intent = new Intent();
		final EditText editView = new EditText(TopActivity.this);
		editView.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);

		// パスワード入力を表示する
		new AlertDialog.Builder(TopActivity.this)
				.setIcon(android.R.drawable.ic_dialog_info)
				.setTitle("パスワードを入力してください。")
				.setView(editView)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						if (editView.getText().toString().equals(password)) {
							// パスワード入力成功時、月次画面遷移
							intent.setClassName("jp.co.timecard", change_place);
							startActivity(intent);
						} else {
							// パスワード入力失敗時
							Toast.makeText(TopActivity.this,
									"正しいパスワードを入力してください", Toast.LENGTH_LONG)
									.show();
						}
					}
				})
				.setNegativeButton("キャンセル",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
							}
						}).show();
	}

	/*
	 * 社員情報選択プルダウン
	 */
	private void employ_select() {
		final TopDao td = new TopDao(getApplicationContext());

		// 社員情報ファイルよりデータ取得
		final String con_url = "http://sashihara.web.fc2.com/employ_info.csv";
		List<String> employ_list = new ArrayList<String>();// 社員リスト

		// ネットから社員データ取得
		final Map<String, String> employ_data = doNet(con_url);

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

			// 社員情報のDB登録
			td.EmployDBInsert(key, value);
			i++;
			// System.out.println(key + "=" + value);
		}
		// Log.d("debug", employ_list.toString());

		spinner = (Spinner) findViewById(R.id.employ_select);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, employ_list);

		adapter.setDropDownViewResource(R.layout.employ_list);
		// spinner.setPrompt("社員情報を選択して下さい");
		spinner.setSelection(0);

		spinner.setAdapter(adapter);
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {

				// 変更時、画面の時刻表示を一旦リセットする。
				final TextView start_tv = (TextView) findViewById(R.id.start_time2); // 始業時刻
				final TextView end_tv = (TextView) findViewById(R.id.last_time2); // 終業時刻
				final TextView break_tv = (TextView) findViewById(R.id.bleak_time2); // 休憩時間
				final TextView sumtime_tv = (TextView) findViewById(R.id.sum_time2); // 合計時間
				start_tv.setText("");
				end_tv.setText("");
				break_tv.setText("");
				sumtime_tv.setText("");

				Spinner spinner = (Spinner) parent;
				String item = (String) spinner.getSelectedItem();

				final Set keySet = employ_data.keySet();
				final Iterator keyIte = keySet.iterator();

				while (keyIte.hasNext()) {
					String key = keyIte.next().toString(); // 社員ID
					String name = employ_data.get(key); // 社員氏名

					// 名前選択時に「選択氏名」と社員データが一致すれば
					// 勤怠マスタにDB登録(社員ID)
					if (item.equals(name)) {
						td.preKintaiSave(sdf.format(date), key); // 社員ID登録
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

	/*
	 * ネット接続 (社員情報ゲット)
	 */
	public Map<String, String> doNet(String u) {
		// HashMapからMapのインスタンスを生成(戻り値用文字列 )
		Map<String, String> ret = new HashMap<String, String>();

		try {
			URL url = new URL(u); // URLクラスのインスタンス作成
			URLConnection con = url.openConnection(); // コネクションを開く。
			InputStream is = con.getInputStream(); // 接続先のデータを取得

			// 取得した文字列を文字列にして返す。
			BufferedReader input = new BufferedReader(new InputStreamReader(is,
					"Shift_JIS"));
			String line = "";

			while ((line = input.readLine()) != null) {
				// 1行をデータの要素に分割
				StringTokenizer st = new StringTokenizer(line, ",");

				// トークンの出力
				while (st.hasMoreTokens()) {
					ret.put(st.nextToken(), st.nextToken());
				}
			}
			input.close();
			return ret;

		} catch (MalformedURLException e) {
			// URLが間違っている場合はこの例外が発生
			// 例外の場合は空文字を返す
			return ret;
		} catch (IOException e) {
			// 例外の場合は空文字を返す
			return ret;
		}
	}
}