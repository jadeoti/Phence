package uniosun.geofence.ui.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;

import uniosun.geofence.R;
import uniosun.geofence.model.SimpleGeofence;
import uniosun.geofence.viewholder.GeofenceViewHolder;

public abstract class GeofenceListFragment extends Fragment {

    private static final String TAG = "GeofenceListFragment";

    // [START define_database_reference]
    private DatabaseReference mDatabase;
    // [END define_database_reference]

    private FirebaseRecyclerAdapter<SimpleGeofence, GeofenceViewHolder> mAdapter;
    private RecyclerView mRecycler;
    private LinearLayoutManager mManager;

    public GeofenceListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.fragment_all_fences, container, false);

        // [START create_database_reference]
        mDatabase = FirebaseDatabase.getInstance().getReference();
        // [END create_database_reference]

        mRecycler = (RecyclerView) rootView.findViewById(R.id.messages_list);
        mRecycler.setHasFixedSize(true);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Set up Layout Manager, reverse layout
        mManager = new LinearLayoutManager(getActivity());
        mManager.setReverseLayout(true);
        mManager.setStackFromEnd(true);
        mRecycler.setLayoutManager(mManager);

        // Set up FirebaseRecyclerAdapter with the Query
        Query fenceQuery = getQuery(mDatabase);
        mAdapter = new FirebaseRecyclerAdapter<SimpleGeofence, GeofenceViewHolder>(SimpleGeofence.class, R.layout.item_fence,
                GeofenceViewHolder.class, fenceQuery) {
            @Override
            protected void populateViewHolder(final GeofenceViewHolder viewHolder, final SimpleGeofence model, final int position) {
                final DatabaseReference postRef = getRef(position);

                // Set click listener for the whole post view
                final String postKey = postRef.getKey();
                viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Launch PostDetailActivity
                        /*Intent intent = new Intent(getActivity(), PostDetailActivity.class);
                        intent.putExtra(PostDetailActivity.EXTRA_POST_KEY, postKey);
                        startActivity(intent);*/
                    }
                });

                // Determine if the current user has liked this post and set UI accordingly
                viewHolder.switchView.setChecked(model.isEnabled());


                // Bind SimpleGeofence to ViewHolder, setting OnClickListener for the star button
                viewHolder.bindToFence(model, new View.OnClickListener() {
                    @Override
                    public void onClick(View starView) {
                        // Need to write to both places the post is stored
                        DatabaseReference globalFencesRef = mDatabase.child("geofences").child(postRef.getKey());
                        //DatabaseReference userPostRef = mDatabase.child("user-posts").child(model.uid).child(postRef.getKey());

                        // Run two transactions
                        onStarClicked(globalFencesRef);
                        //onStarClicked(userPostRef);
                    }
                });
            }
        };
        mRecycler.setAdapter(mAdapter);
    }

    // [START start_transaction_toggle_geofence]
    private void onStarClicked(DatabaseReference simpleGeofence) {
        simpleGeofence.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                SimpleGeofence geofence = mutableData.getValue(SimpleGeofence.class);
                if (geofence == null) {
                    return Transaction.success(mutableData);
                }

                if (geofence.isEnabled()) {
                    // Disable the geofence
                    geofence.setEnabled(false);
                } else {
                    // Enable the geofence
                    geofence.setEnabled(true);
                }

                // Set value and report transaction success
                mutableData.setValue(geofence);
                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b,
                                   DataSnapshot dataSnapshot) {
                // Transaction completed
                Log.d(TAG, "postTransaction:onComplete:" + databaseError);
                //TODO: broadcast to everyone
            }
        });
    }
    // [END end_transaction_toggle_geofence]

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    public String getUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public abstract Query getQuery(DatabaseReference databaseReference);

}
