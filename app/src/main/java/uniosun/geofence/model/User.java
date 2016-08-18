package uniosun.geofence.model;

import com.google.firebase.database.IgnoreExtraProperties;

// [START blog_user_class]
@IgnoreExtraProperties
public class User {

    public String username;
    public String email;
    public boolean isAdmin;
    public String screenName;
    public String phone;

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String username, String email) {
        this.username = username;
        this.email = email;
        this.isAdmin = false;
        this.screenName = username;
        this.phone = "080";
    }

}
// [END blog_user_class]
