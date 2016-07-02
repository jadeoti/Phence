package uniosun.geofence.ui;

import android.os.Bundle;

import uniosun.geofence.R;

public class FencesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fences);
        getActionBarToolbar().setTitle(R.string.title_fences);
    }

    @Override
    protected int getSelfNav() {
        return R.id.nav_all;
    }
}
