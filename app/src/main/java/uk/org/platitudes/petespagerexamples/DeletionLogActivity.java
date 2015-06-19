package uk.org.platitudes.petespagerexamples;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;


public class DeletionLogActivity extends ActionBarActivity implements View.OnClickListener {

    private ArrayAdapter arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deletion_log);
        ListView listView = (ListView) findViewById(R.id.deletion_log);
        arrayAdapter = new DeleteLogAdapter(this, R.layout.delete_log_row_layout, MainTabActivity.sTheMainActivity.deleteLog);
        listView.setAdapter(arrayAdapter);
        View clearButton = findViewById(R.id.clear_button);
        clearButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        // Responds to the "clear" button being clicked
        MainTabActivity.sTheMainActivity.deleteLog.clear();
        arrayAdapter.notifyDataSetChanged();
    }
}