package uniosun.geofence.viewholder;

import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.TextView;

import uniosun.geofence.R;
import uniosun.geofence.model.SimpleGeofence;

public class GeofenceViewHolder extends RecyclerView.ViewHolder {

    public TextView titleView;
    public TextView coordinateView;
    public TextView radiusView;
    public SwitchCompat switchView;

    public GeofenceViewHolder(View itemView) {
        super(itemView);

        titleView = (TextView) itemView.findViewById(R.id.title);
        coordinateView = (TextView) itemView.findViewById(R.id.coordinate);
        radiusView = (TextView) itemView.findViewById(R.id.radius);
        switchView = (SwitchCompat) itemView.findViewById(R.id.toggle);
    }

    public void bindToFence(SimpleGeofence simpleGeofence, View.OnClickListener switchClickListener) {
        titleView.setText(simpleGeofence.getDescription());

        coordinateView.setText(String.format("(%.4f, %.4f)", simpleGeofence.getLatitude(), simpleGeofence.getLongitude()));
        radiusView.setText(Double.toString(simpleGeofence.getRadius()));
        switchView.setChecked(simpleGeofence.isEnabled());

        switchView.setOnClickListener(switchClickListener);
    }
}
