package jp.co.timecard;

import java.util.ArrayList;
import java.util.HashMap;

import jp.co.timecard.db.DbConstants;
import jp.co.timecard.db.DbOpenHelper;
import jp.co.timecard.db.TopDao;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class PassWordSetListActivity extends Activity {

	ArrayList<HashMap<String, String>> mList;
	PassWordSetListAdapter adapter;
	ListView listview;
	View getView;
	
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
		
		HashMap<String, String> item;
		String[] listdata = {"月次リスト","基本時間設定","パスワード設定"};
		
		for (int i = 0; i < listdata.length; i++) {
			// 初期表示
			item = new HashMap<String, String>();
			item.put("title", listdata[i]);
			item.put("desc", "現在の設定 ****");
			mList.add(item);
		}
		
		adapter = new PassWordSetListAdapter(this, mList);
		listview.setAdapter(adapter);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				// タイトル取得
				String title = mList.get(position).get("title");
				TopDao td = new TopDao(getApplicationContext());
				
				//String desc = mList.get(position).get("desc");
				//Log.d("debug", desc);
				//Log.d("debug", String.valueOf(item.entrySet()));
				
				if (title == "月次リスト") {
					String password = td.PassWordGet("monthly");
					ListViewClick(title, "monthly", password);
				} else if (title == "基本時間設定") {
					String password = td.PassWordGet("basetime");
					ListViewClick(title, "basetime", password);
				} else if (title == "パスワード設定") {
					String password = td.PassWordGet("password");
					ListViewClick(title, "password", password);
				}
			}
		});
	}

	/*
	 * パスワード設定リストクリック
	 */
	private void ListViewClick(final String title, final String screen_id, final String password) {
		
		final ContentValues cv = new ContentValues();
		final DbOpenHelper helper = new DbOpenHelper(getApplicationContext());
		final SQLiteDatabase db = helper.getWritableDatabase();
		final ListView listView;
	    listView = (ListView)findViewById(R.id.listview);
		
	    // パスワード入力フィールドを二つ表示する(現在、変更後)
 		final EditText editView1 = new EditText(PassWordSetListActivity.this); // 現在
 		final EditText editView2 = new EditText(PassWordSetListActivity.this); // 変更後
 		final int FP = ViewGroup.LayoutParams.FILL_PARENT;
     	editView1.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
 		editView2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
     	
 		LinearLayout layout = new LinearLayout(this);
     	layout.setOrientation(LinearLayout.VERTICAL); // 垂直方向
     	
     	// レイアウトに２つのビューをセット
     	TextView text1 = new TextView(this);
        text1.setText("現在のパスワード");
        layout.addView(text1, new LinearLayout.LayoutParams(FP, 50));
        layout.addView(editView1, new LinearLayout.LayoutParams(FP, 50));
     	
     	TextView text2 = new TextView(this);
        text2.setText("変更後のパスワード");
        layout.addView(text2, new LinearLayout.LayoutParams(FP, 50));
        layout.addView(editView2, new LinearLayout.LayoutParams(FP, 50));
     	
     	new AlertDialog.Builder(PassWordSetListActivity.this)
         .setIcon(android.R.drawable.ic_dialog_info)
         .setTitle("パスワードを入力してください。")
         .setView(layout)
         .setPositiveButton("OK", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {
            	 
            	if (!editView1.getText().toString().equals(password)) {
             		// 現在パスワード入力失敗時、Error
             		Toast.makeText(PassWordSetListActivity.this, 
                         "現在のパスワードを正しくを入力してください", 
                         Toast.LENGTH_SHORT).show();
             		return;
             	} else {
             		// 現在パスワード入力成功時
             		if (editView2.getText().toString().length() == 0) {
             			// 変更後のパスワード入力未入力時、Error
             			Toast.makeText(PassWordSetListActivity.this, 
                            "変更後のパスワードを入力してください", 
                            Toast.LENGTH_SHORT).show();
             			return;
             		}
             		
             		Toast.makeText(PassWordSetListActivity.this, 
						"パスワードを変更しました", 
						Toast.LENGTH_SHORT).show();
             		
             		// DBへ変更後のパスワード反映
            	    try {
            	    	if (title == "月次リスト") {
            				// 月次リストがクリックされた場合
            				final View listDataView = listView.getChildAt(0);
            				TextView time_tv = (TextView) listDataView.findViewById(R.id.desc);
            				cv.put("password", editView2.getText().toString());
            				time_tv.setText("現在の設定 " + "****"); // 月次リストパスワード画面反映
            				db.update(DbConstants.TABLE_NAME7, cv, "screen_id=?", 
            						new String[] {screen_id});
            				
            			} else if (title == "基本時間設定") {
            				// 基本時間設定がクリックされた場合
            				final View listDataView = listView.getChildAt(1);
            				TextView time_tv = (TextView) listDataView.findViewById(R.id.desc);
            				cv.put("password", editView2.getText().toString());
            				time_tv.setText("現在の設定 " + "****"); // 基本時間設定パスワード画面反映
            				db.update(DbConstants.TABLE_NAME7, cv, "screen_id=?", 
            						new String[] {screen_id});
            				
            			} else if (title == "パスワード設定") {
            				// パスワード設定がクリックされた場合
            				final View listDataView = listView.getChildAt(2);
            				TextView time_tv = (TextView) listDataView.findViewById(R.id.desc);
            				cv.put("password", editView2.getText().toString());
            				time_tv.setText("現在の設定 " + "****"); // パスワード設定パスワード画面反映
            				db.update(DbConstants.TABLE_NAME7, cv, "screen_id=?", 
            						new String[] {screen_id});
            				
            			 }
            	    } finally {
            			db.close();
            		}
             	}
             }
         })
         .setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
             public void onClick(DialogInterface dialog, int whichButton) {}
         })
         .show();
	};
}
