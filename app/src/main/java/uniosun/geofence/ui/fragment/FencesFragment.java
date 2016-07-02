package uniosun.geofence.ui.fragment;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

public class FencesFragment extends GeofenceListFragment {

    public FencesFragment() {
    }

    @Override
    public Query getQuery(DatabaseReference databaseReference) {
        // [START fences_query]
        // Last 100 posts, these are automatically the 100 most recent
        // due to sorting by push() keys
        Query recentPostsQuery = databaseReference.child("geofences")
                .limitToFirst(100);
        // [END recent_posts_query]

        return recentPostsQuery;
    }
}
