package com.tkivilius.projectapp;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

// Display detailed info of Item in list
// Displays Name and description
// And Photo if it was taken

public class DetailDisplay extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_display);

        // View objects
        TextView title = (TextView) findViewById(R.id.detailTitle);
        TextView description = (TextView) findViewById(R.id.detailDescription);
        TextView time = (TextView) findViewById(R.id.detailDate);
        ImageView photo = (ImageView) findViewById(R.id.detailImage);

        // Get intent
        Intent intent = getIntent();

        // Set TextView text from intent data
        title.setText(intent.getStringExtra(ListFrag.DETAIL_TITLE));
        description.setText(intent.getStringExtra(ListFrag.DETAIL_DESCRIPTION));
        time.setText(intent.getStringExtra(ListFrag.DETAIL_TIMESTAMP));

        // Set ImageView if given
        if (intent.getStringExtra(ListFrag.DETAIL_IMAGE).compareTo("No Path") != 0)
            AddItemFrag.setPic(intent.getStringExtra(ListFrag.DETAIL_IMAGE), photo, this);
    }
}
