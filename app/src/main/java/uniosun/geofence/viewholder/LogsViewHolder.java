package uniosun.geofence.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import uniosun.geofence.R;
import uniosun.geofence.model.Activity;

public class LogsViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView bodyView;
    public TextView authorView;

    public LogsViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.time);
        bodyView = (TextView) itemView.findViewById(R.id.body);
        authorView = (TextView) itemView.findViewById(R.id.author);
    }

    public void bindToFence(Activity activity) {
        titleView.setText(activity.time);
        authorView.setText(activity.author);
        bodyView.setText(activity.body);
    }
}
