package jp.co.timecard;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * アダプター
 * @author TomohiroTano
 */
public class MonthlyAdapter extends ArrayAdapter<DailyState> {

	private ArrayList<DailyState> list;
    private LayoutInflater inflater;
    private ViewHolder     holder;

	public MonthlyAdapter(Context context, int textViewResourceId, ArrayList<DailyState> _list) {
		super(context, textViewResourceId, _list);

        this.list = _list;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        //ViewHolder
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(R.layout.monthly_item, null);

            holder = new ViewHolder();
            holder.date = (TextView) view.findViewById(R.id.date);
            holder.attendance = (TextView) view.findViewById(R.id.attendance);
            holder.leave = (TextView) view.findViewById(R.id.leave);
            holder.work_hour = (TextView) view.findViewById(R.id.work_hour);

            view.setTag(holder);

        } else {

        	holder = (ViewHolder) convertView.getTag();
		}

        DailyState item = list.get(position);
        if (item != null) {
            if (holder.date != null) {
            	holder.date.setText(item.getDate());
            }
            if (holder.attendance != null) {
            	holder.attendance.setText(item.getAttendance());
            }
            if (holder.leave != null) {
            	holder.leave.setText(item.getLeave());
            }
            if (holder.work_hour != null) {
            	holder.work_hour.setText(item.getWorkHour());
            }
        }
        return view;
    }

    /**
     * Viewを保持してgetView高速化。
     */
    private class ViewHolder {
    	TextView  date;
    	TextView  attendance;
    	TextView  leave;
    	TextView  work_hour;
    }
}
