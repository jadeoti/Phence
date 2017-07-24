/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uniosun.geofence.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.TextUtils;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import timber.log.Timber;
import uniosun.geofence.Config;
import uniosun.geofence.GeofenceErrorMessages;
import uniosun.geofence.R;
import uniosun.geofence.model.Activity;
import uniosun.geofence.model.User;
import uniosun.geofence.network.GeofenceClient;
import uniosun.geofence.network.WebService;
import uniosun.geofence.ui.MapsActivity;

import static uniosun.geofence.util.Utilities.getUid;

/*import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.Wearable;*/

/**
 * Listener for geofence transition changes.
 * <p>
 * Receives geofence transition events from Location Services in the form of an Intent containing
 * the transition type and geofence id(s) that triggered the transition. Creates a notification
 * as the output.
 */
public class GeofenceTransitionsIntentService extends IntentService {

    protected static final String TAG = "geofence-transitions-service";

    // [START webservice_reference]
    private WebService mWebService;
    // [END webservice_reference]


    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private String adminPhone;

    private String mTransitionDetails;

    /**
     * This constructor is required, and calls the super IntentService(String)
     * constructor with the name for a worker thread.
     */
    public GeofenceTransitionsIntentService() {
        // Use the TAG to name the worker thread.
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mWebService = new GeofenceClient().getService();
        // [START initialize_database_ref]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END initialize_database_ref]

        getAdmins(mDatabase);

    }

    /**
     * Handles incoming intents.
     *
     * @param intent sent by Location Services. This Intent is provided to Location
     *               Services (inside a PendingIntent) when addGeofences() is called.
     */
    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceErrorMessages.getErrorString(this,
                    geofencingEvent.getErrorCode());
            Timber.e(errorMessage);
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL) {

            // Get the geofences that were triggered. A single event can trigger multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // Get the transition details as a String.
            mTransitionDetails = getGeofenceTransitionDetails(
                    this,
                    geofenceTransition,
                    triggeringGeofences
            );

            // Send notification and log the transition details.
            sendNotification(mTransitionDetails);

            // Send activity log
            submitActivityLog(mTransitionDetails);

            // send sms
            sendMessage(mTransitionDetails);

            Timber.i(mTransitionDetails);
        } else {
            // Log the error.
            Timber.e(getString(R.string.geofence_transition_invalid_type, geofenceTransition));
            submitActivityLog(getString(R.string.geofence_transition_invalid_type, geofenceTransition));
        }
    }

    /**
     * Gets transition details and returns them as a formatted string.
     *
     * @param context             The app context.
     * @param geofenceTransition  The ID of the geofence transition.
     * @param triggeringGeofences The geofence(s) triggered.
     * @return The transition details formatted as String.
     */
    private String getGeofenceTransitionDetails(
            Context context,
            int geofenceTransition,
            List<Geofence> triggeringGeofences) {

        String geofenceTransitionString = getTransitionString(geofenceTransition);

        // Get the Ids of each geofence that was triggered.
        ArrayList triggeringGeofencesIdsList = new ArrayList();
        for (Geofence geofence : triggeringGeofences) {
            triggeringGeofencesIdsList.add(geofence.getRequestId());
        }
        String triggeringGeofencesIdsString = TextUtils.join(", ", triggeringGeofencesIdsList);

        return geofenceTransitionString + ": " + triggeringGeofencesIdsString;
    }

    /**
     * Posts a notification in the notification bar when a transition is detected.
     * If the user clicks the notification, control goes to the MainActivity.
     */
    private void sendNotification(String notificationDetails) {
        // Create an explicit content Intent that starts the main Activity.
        Intent notificationIntent = new Intent(getApplicationContext(), MapsActivity.class);

        // Construct a task stack.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);

        // Add the main Activity to the task stack as the parent.
        stackBuilder.addParentStack(MapsActivity.class);

        // Push the content Intent onto the stack.
        stackBuilder.addNextIntent(notificationIntent);

        // Get a PendingIntent containing the entire back stack.
        PendingIntent notificationPendingIntent =
                stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Get a notification builder that's compatible with platform versions >= 4
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        // Define the notification settings.
        builder.setSmallIcon(R.drawable.ic_notification_icon)
                // In a real app, you may want to use a library like Volley
                // to decode the Bitmap.
                .setLargeIcon(BitmapFactory.decodeResource(getResources(),
                        R.mipmap.ic_launcher))
                .setColor(Color.RED)
                .setContentTitle(notificationDetails)
                .setContentText(getString(R.string.geofence_transition_notification_text))
                .setContentIntent(notificationPendingIntent);

        // Dismiss notification once the user touches it.
        builder.setAutoCancel(true);

        // Get an instance of the Notification manager
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Issue the notification
        mNotificationManager.notify(0, builder.build());
    }

    /**
     * Maps geofence transition types to their human-readable equivalents.
     *
     * @param transitionType A transition type constant defined in Geofence
     * @return A String indicating the type of transition
     */
    private String getTransitionString(int transitionType) {
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                return getString(R.string.geofence_transition_entered);
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                return getString(R.string.geofence_transition_exited);
            case Geofence.GEOFENCE_TRANSITION_DWELL:
                return getString(R.string.geofence_transition_dwell);
            default:
                return getString(R.string.unknown_geofence_transition);
        }
    }

    private void sendMessage(String message) {
        Call<JsonObject> smsCall = mWebService.sendSms(Config.SMS_API_KEY,
                "Geofence Project", message, adminPhone);
        try {
            smsCall.execute();
        } catch (IOException e) {
            submitActivityLog(e.getMessage());
        }
    }


    private void getAdmins(DatabaseReference databaseReference) {
        Query query = databaseReference.child("users");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue() == null) return;

                try {
                    Timber.d("singlevalueondatachange: %s", dataSnapshot.getValue().toString());

                    GenericTypeIndicator<HashMap<String, User>> typeIndicator =
                            new GenericTypeIndicator<HashMap<String, User>>() {
                            };

                    Collection<User> userCollection = dataSnapshot.getValue(typeIndicator).values();

                    ArrayList<User> users = new ArrayList<>(userCollection);

                    ArrayList<String> adminPhones = new ArrayList<String>();
                    for (User user : users) {
                        if (user.isAdmin) {
                            adminPhones.add(user.phone);
                        }
                    }
                    if (adminPhones.size() >= 1) {
                        adminPhone = TextUtils.join(",", adminPhones);
                        //adminPhone = adminPhones.get(0);
                    }
                } catch (DatabaseException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                /*Map<String, SimpleGeofence> td = (HashMap<String,SimpleGeofence>) dataSnapshot.getValue();
                onGeofenceAvailable(td);*/
                //Log.d(TAG, "childadded:" + dataSnapshot.getValue().getClass());

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //Log.d(TAG, "ondatachange:" + dataSnapshot.getValue().getClass());

                //Map<String, SimpleGeofence> td = (HashMap<String,SimpleGeofence>) dataSnapshot.getValue();
                //onGeofenceAvailable(td);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void submitActivityLog(final String details) {

        // [START single_value_read]
        final String userId = getUid();
        mDatabase.child("users").child(userId).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        // Get user value
                        User user = dataSnapshot.getValue(User.class);

                        // [START_EXCLUDE]
                        if (user == null) {
                            // User is null, error out
                            Timber.e("User %s is unexpectedly null", userId);
                            Toast.makeText(GeofenceTransitionsIntentService.this,
                                    "Error: could not fetch user.",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // Write new user activity
                            writeNewPost(userId, user.username, details);
                        }

                        // Finish this Activity, back to the stream
                        // [END_EXCLUDE]
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Timber.w("getUser:onCancelled %s", databaseError.toException());
                    }
                });
        // [END single_value_read]
    }

    // [START write_fan_out]
    private void writeNewPost(String userId, String username, String body) {
        // Create new post at /user-activities/$userid/$activity_id and at
        // /activities/$activity_id simultaneously
        String key = mDatabase.child("activities").push().getKey();
        long time = System.currentTimeMillis();
        Activity post = new Activity(userId, username, new Date(time).toString(), body);
        Map<String, Object> postValues = post.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/activities/" + key, postValues);
        childUpdates.put("/user-activities/" + userId + "/" + key, postValues);

        mDatabase.updateChildren(childUpdates);
    }
    // [END write_fan_out]
}

