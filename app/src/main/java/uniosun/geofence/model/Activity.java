package uniosun.geofence.model;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

// [START post_class]
@IgnoreExtraProperties
public class Activity {

    public String uid;
    public String author;
    public String time;
    public String body;


    public Activity() {
        // Default constructor required for calls to DataSnapshot.getValue(Post.class)
    }

    public Activity(String uid, String author, String time, String body) {
        this.uid = uid;
        this.author = author;
        this.time = time;
        this.body = body;
    }

    // [START post_to_map]
    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("author", author);
        result.put("time", time);
        result.put("body", body);

        return result;
    }
    // [END post_to_map]

}
// [END post_class]
