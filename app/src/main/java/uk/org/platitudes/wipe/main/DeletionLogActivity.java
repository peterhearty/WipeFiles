package uk.org.platitudes.wipe.main;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import uk.org.platitudes.petespagerexamples.R;
import uk.org.platitudes.wipe.adapters.DeleteLogAdapter;
import uk.org.platitudes.wipe.main.MainTabActivity;


public class DeletionLogActivity extends ActionBarActivity implements View.OnClickListener {

    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deletion_log);
        ListView listView = (ListView) findViewById(R.id.deletion_log);
        arrayAdapter = new DeleteLogAdapter(this, R.layout.delete_log_row_layout, MainTabActivity.sTheMainActivity.mDeleteLog);
        listView.setAdapter(arrayAdapter);
        View clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        // Responds to the "clear" button being clicked
        MainTabActivity.sTheMainActivity.mDeleteLog.clear();
        arrayAdapter.notifyDataSetChanged();
    }
}
