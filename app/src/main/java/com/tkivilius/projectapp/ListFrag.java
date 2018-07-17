package com.tkivilius.projectapp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A fragment which displays item created by the AddItemFrag
 * Shows buttons to be clicked on and displayed detailed data of the item
 */
public class ListFrag extends Fragment {
    // Static variables for intent data
    public static final String DETAIL_TITLE = "detail_title";
    public static final String DETAIL_DESCRIPTION = "detail_description";
    public static final String DETAIL_IMAGE = "detail_image";
    public static final String DETAIL_TIMESTAMP = "time";

    // Table and Array variables
    private TableLayout mainTable;
    private JSONArray arrayData;

    public ListFrag() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        // Set Title in Toolbar
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("List");

        // Read the data from file
        this.mainTable = v.findViewById(R.id.listTable);
        try {
            this.arrayData = new JSONArray(FileHandler.readFromFile(getContext(), "saved_data.json"));
        } catch (JSONException e) {
            this.arrayData = new JSONArray();
            e.printStackTrace();
        }

        // Add item from JSON array to TableView
        PopulateTable();

        return v;
    }

    // Creates new TableRow with default parameters
    public TableRow NewRow() {
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.MATCH_PARENT));
        return row;
    }

    // Creates a Button Associated with its data
    public void ButtonClick(Button button) {
        // get JSON object
        JSONObject object = null;
        try {
            object = (JSONObject) this.arrayData.get((int) button.getTag());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /*if (object != null)
            Log.d("JSON", "The button has associated JSONObject: " + object.toString());*/

        // Intent to show stuff in another window
        Intent intent = new Intent(getContext(), DetailDisplay.class);
        try {
            intent.putExtra(DETAIL_TITLE, object.getString(AddItemFrag.JSON_TITLE));
            intent.putExtra(DETAIL_DESCRIPTION, object.getString(AddItemFrag.JSON_DESCRIPTION));
            intent.putExtra(DETAIL_IMAGE, object.getString(AddItemFrag.JSON_PHOTO));
            intent.putExtra(DETAIL_TIMESTAMP, object.getString(AddItemFrag.JSON_TIMESTAMP));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }

    // Creates new button to be displayed be added into Table
    public Button NewButton(String text, int tag, String prio) {
        Button button = new Button(getContext());

        // Limit text on button
        if (text.length() > 10) {
            String s;
            s = text.substring(0, 10);
            text = s;
        }

        button.setText(text);
        button.setTag(tag);

        // Why String == String not working as intended, android studio suggests equals, but that requires min API level 19
        String[] priorities = getResources().getStringArray(R.array.priority);
        //Log.d("JSON",prio + " " + priorities[0]+ " " + priorities[1]+ " " + priorities[2]);
        if (prio.compareTo(priorities[0]) == 0) {
            //Log.d("JSON","WHITE");
            button.setTextColor(Color.WHITE);
        } else if (prio.compareTo(priorities[1]) == 0) {
            //Log.d("JSON","YELLOW");
            button.setTextColor(Color.YELLOW);
        } else if (prio.compareTo(priorities[2]) == 0) {
            //Log.d("JSON","RED");
            button.setTextColor(Color.RED);
        } else {
            Log.d("JSON", "Priority invalid");
            button.setTextColor(Color.WHITE);
        }

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // call new intent to open in detail
                Button button = (Button) v;
                //Toast.makeText(getContext(), "my name is " + button.getText() + " and my tag is " + button.getTag(), Toast.LENGTH_SHORT).show();
                ButtonClick(button);
            }
        });
        return button;
    }

    // Adds buttons into next available space
    // Creates new row if all full, or none exist
    public void SafeAddButtons(String title, int num, String prio) {
        int tableChildCount = this.mainTable.getChildCount();
        // If 0 children - no rows, add row then add button
        if (tableChildCount == 0) {
            TableRow temp = NewRow();
            Button tempButton = NewButton(title, num, prio);

            temp.addView(tempButton);
            this.mainTable.addView(temp);

        } else // Non 0 value, row exists
        {
            // get last bottom most row
            TableRow parentRow = (TableRow) this.mainTable.getChildAt(this.mainTable.getChildCount() - 1);
            int childCount = parentRow.getChildCount();
            // Check if the row have children AKA buttons
            // less than 3 - Add button
            if (childCount < 3) {
                Button tempButton = NewButton(title, num, prio);

                parentRow.addView(tempButton);
            }
            // 3 buttons exist - add new row, add new button
            else if (childCount == 3) {
                TableRow temp = NewRow();
                Button tempButton = NewButton(title, num, prio);

                temp.addView(tempButton);
                this.mainTable.addView(temp);
            }
        }
    }

    // Unpacks JSON array and sends that data to SafeAddButtons to be added into appropriate places
    public void PopulateTable() {

        for (int i = 0; i < this.arrayData.length(); i++) {
            JSONObject object;
            String title = "";
            String prio = "";

            // unpack
            try {
                object = (JSONObject) this.arrayData.get(i);
                title = object.get("title").toString();
                prio = object.getString("priority");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            SafeAddButtons(title, i, prio);
        }
    }
}

