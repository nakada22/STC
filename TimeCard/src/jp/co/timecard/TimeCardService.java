package jp.co.timecard;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Parcel;
import android.os.RemoteException;
import android.widget.Toast;

public class TimeCardService extends Service{  
    private PendingIntent alarmSender;  
    
    // 現状は未使用
    @Override  
    public IBinder onBind(Intent intent) {  
        return binder;  
    }  
      
    @Override  
    public void onStart(Intent intent, int startId) {  
        // 起動確認  
        Toast.makeText(this, "Service has been started.", Toast.LENGTH_SHORT).show();  
    	
        Thread thr = new Thread(null, task, "AlarmService_Service");  
        thr.start();  
    }  
  
    private Runnable task = new Runnable() {          
        @Override  
        public void run() {  
            // 次回起動(1分ごと)
            long now = System.currentTimeMillis();  
            alarmSender = PendingIntent.getService(TimeCardService.this, 0,   
                    new Intent(TimeCardService.this, TimeCardService.class), 0);  
            AlarmManager am = (AlarmManager)getSystemService(Context.ALARM_SERVICE);  
            am.set(AlarmManager.RTC, now + 60*1000, alarmSender);  
              
            // サービス終了  
            TimeCardService.this.stopSelf();  
        }  
    };  
  
    private final IBinder binder = new Binder() {  
        @Override  
        protected boolean onTransact(int code, Parcel data, Parcel reply,  
                int flags) throws RemoteException {  
            return super.onTransact(code, data, reply, flags);  
        }  
    };  
}  