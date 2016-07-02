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
import uniosun.geofence.util.Utilities;

public class CreateGeofence extends BaseActivity {

    private static final String TAG = "CreateGeofence";
    private static final String REQUIRED = "Required";

    // [START declare_database_ref]
    private DatabaseReference mDatabase;
    // [END declare_database_ref]

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
        writeNewGeoFence(simpleGeofence);

       /* // [START single_value_read]
        //final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Log.e(TAG, "User " + userId + " is unexpectedly null");
                            Toast.makeText(CreateGeofence.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new post
                           SimpleGeofence simpleGeofence = new SimpleGeofence(
                                    YERBA_BUENA_ID,                // geofenceId.
                                    YERBA_BUENA_LATITUDE,
                                    YERBA_BUENA_LONGITUDE,
                                    YERBA_BUENA_RADIUS_METERS,
                                    GEOFENCE_EXPIRATION_TIME,
                                    Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT | Geofence.GEOFENCE_TRANSITION_DWELL
                            );
                            writeNewGeoFence(userId, user.username, title, simpleGeofence);
                        }

                        // Finish this Activity, back to the stream
                        finish();
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "getUser:onCancelled", databaseError.toException());
                    }
                });
        // [END single_value_read]*/
    }

    // [START write_fan_out]
    private void writeNewGeoFence(SimpleGeofence simpleGeofence) {
        // Create new post at /user-posts/$userid/$postid and at
        // /posts/$postid simultaneously
        Map<String, Object> geofenceValues = simpleGeofence.toMap();


        mDatabase.child("geofences").push().setValue(geofenceValues)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        clearFields();
                        Toast.makeText(CreateGeofence.this, "Successfully added", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, e.getCause().getMessage());
            }
        });
    }
    // [END write_fan_out]

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
