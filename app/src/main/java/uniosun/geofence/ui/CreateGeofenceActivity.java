package uniosun.geofence.ui;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Map;

import uniosun.geofence.R;
import uniosun.geofence.model.SimpleGeofence;
import uniosun.geofence.model.SimplePoint;
import uniosun.geofence.util.Utilities;

public class CreateGeofenceActivity extends BaseActivity {

    public static final String POINT_DETAILS = "uniosun.geofence.ui.CGA";
    public static final String EDITING = "uniosun.geofence.ui.EDITING";
    private static final String TAG = "CreateGeofenceActivity";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

    // point description from map
    private SimplePoint mSimplePoint;

    private boolean mIsEditing;

    private EditText mTitleField, mLongitudeField, mLatitudeField, mRadiusField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_geofence);


        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        mTitleField = (EditText) findViewById(R.id.field_title);
        mLatitudeField = (EditText) findViewById(R.id.field_latitude);
        mLongitudeField = (EditText) findViewById(R.id.field_longitude);
        mRadiusField = (EditText) findViewById(R.id.field_radius);

        findViewById(R.id.fab_submit_post).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitPost();
            }
        });

        tryFillPage();
    }

    private void tryFillPage() {
        if (getIntent().getExtras() != null) {
            Bundle extras = getIntent().getExtras();
            if (extras.containsKey(POINT_DETAILS)) {
                mSimplePoint = (SimplePoint) extras.getSerializable(POINT_DETAILS);
                if (mSimplePoint != null) {
                    // fill points
                    mTitleField.setText(mSimplePoint.getDescription());
                    mLongitudeField.setText(Double.toString(mSimplePoint.getLongitude()));
                    mLatitudeField.setText(Double.toString(mSimplePoint.getLatitude()));
                    mRadiusField.setText(Double.toString(mSimplePoint.getRadius()));
                }

            }

            if (extras.containsKey(EDITING)) {
                mIsEditing = extras.getBoolean(EDITING, false);
            }
        }
    }

    private void submitPost() {
        if (!Utilities.isOnline(this)) {
            Snackbar.make(getActionBarToolbar(), "You are currently offline", Snackbar.LENGTH_SHORT).show();
            return;
        }
        final String title = mTitleField.getText().toString();
        final String lat = mLatitudeField.getText().toString();
        final String lng = mLongitudeField.getText().toString();
        final String radius = mRadiusField.getText().toString();

        // Title is required
        if (TextUtils.isEmpty(title)) {
            mTitleField.setError(REQUIRED);
            return;
        }
        // latitude is required
        if (TextUtils.isEmpty(lat)) {
            mLatitudeField.setError(REQUIRED);
            return;
        }

        // latitude is required
        if (TextUtils.isEmpty(lng)) {
            mLongitudeField.setError(REQUIRED);
            return;
        }

        // radius is required
        if (TextUtils.isEmpty(radius)) {
            mRadiusField.setError(REQUIRED);
            return;
        }

        if (Float.parseFloat(radius) <= 0.0f) {
            mRadiusField.setError(REQUIRED);
            return;
        }

        // Write new post
        SimpleGeofence simpleGeofence = new SimpleGeofence(
                "id",                // geofenceId.
                Double.parseDouble(lat),
                Double.parseDouble(lng),
                Float.parseFloat(radius),
                Geofence.NEVER_EXPIRE,
                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL,
                title,
                true
        );
        if (!mIsEditing) {
            writeNewGeoFence(simpleGeofence);
        } else {
            updateFence(mSimplePoint.getId(), simpleGeofence);
        }
    }

    // [START write_new_fence]
    private void writeNewGeoFence(SimpleGeofence simpleGeofence) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        Map<String, Object> geofenceValues = simpleGeofence.toMap();


        mDatabase.child("geofences").push().setValue(geofenceValues)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(CreateGeofenceActivity.this, "Successfully added", Toast.LENGTH_SHORT).show();
                        //clearFields();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getCause().getMessage());
            }
        });
    }
    // [END write_new_fence]

    // [START update_fence]
    private void updateFence(String fenceId, SimpleGeofence simpleGeofence) {
        Map<String, Object> geofenceValues = simpleGeofence.toMap();


        mDatabase.child("geofences").child(fenceId).setValue(geofenceValues)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        Toast.makeText(CreateGeofenceActivity.this, "Successfully edited", Toast.LENGTH_SHORT).show();
                        //clearFields();
                        finish();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getCause().getMessage());
            }
        });
    }
    // [END update_fence]

    private void clearFields() {
        mTitleField.setText(null);
        mLongitudeField.setText(null);
        mLatitudeField.setText(null);
        mRadiusField.setText(null);
    }


    @Override
    protected int getSelfNav() {
        return R.id.nav_new;
    }
}
