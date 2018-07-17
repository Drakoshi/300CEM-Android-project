package com.tkivilius.projectapp;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * A fragment which creates new items to be displayed in List
 */
public class AddItemFrag extends Fragment implements AdapterView.OnItemSelectedListener {
    // Statics
    public static final String JSON_TITLE = "title";
    public static final String JSON_DESCRIPTION = "description";
    public static final String JSON_PRIORITY = "priority";
    public static final String JSON_PHOTO = "photo";
    public static final String JSON_TIMESTAMP = "time";
    static final int REQUEST_TAKE_PHOTO = 1;

    // others
    public String mCurrentPhotoPath = null;
    public ImageView mImageView;
    private Button saveButton;

    // Spinner items
    private Spinner prioritySpinner;
    private Spinner reminderSpinner;
    private String[] priorityArray;
    private String[] reminderArray;

    // Input elements
    private EditText inputTitle;
    private EditText inputDesc;
    private String priority;
    private String reminder;

    public AddItemFrag() {
        // Required empty public constructor
    }

    // https://developer.android.com/training/camera/photobasics.html
    // Decodes the photo to normal size and sets it to the ImageView
    public static void setPic(String photoPath, ImageView imageView, Activity activity) {
        // Get the dimensions of the View
        int targetW = imageView.getWidth();
        int targetH = imageView.getHeight();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        int width = displayMetrics.widthPixels;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor;
        if (targetH == 0 || targetW == 0) {
            scaleFactor = Math.min(photoW / width, photoH / height);
        } else
            scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(photoPath, bmOptions);

        if (activity.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            imageView.setImageBitmap(rotatedBitmap);
        } else {
            imageView.setImageBitmap(bitmap);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_add, container, false);

        // get XML stuff
        this.mImageView = (ImageView) v.findViewById(R.id.itemImage);
        this.prioritySpinner = v.findViewById(R.id.spinnerPriority);
        this.priorityArray = getResources().getStringArray(R.array.priority);
        this.inputTitle = v.findViewById(R.id.inputTitle);
        this.inputDesc = v.findViewById(R.id.inputDescription);
        this.reminderSpinner = v.findViewById(R.id.spinnerReminder);
        this.reminderArray = getResources().getStringArray(R.array.reminder);

        // get Photo buttons
        Button photo = (Button) v.findViewById(R.id.photoTake);
        Button delete = (Button) v.findViewById(R.id.photoDelete);

        // Set Toolbar title
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Add item");

        // Save button Listener
        this.saveButton = v.findViewById(R.id.buttonSave);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OnClickSave();
            }
        });

        // Spinner setup
        ArrayAdapter<CharSequence> prioritySpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.priority, R.layout.spinner_item);
        prioritySpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        prioritySpinner.setAdapter(prioritySpinnerAdapter);
        prioritySpinner.setOnItemSelectedListener(this);

        ArrayAdapter<CharSequence> reminderSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.reminder, R.layout.spinner_item);
        prioritySpinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        reminderSpinner.setAdapter(reminderSpinnerAdapter);
        reminderSpinner.setOnItemSelectedListener(this);


        // Photo button listeners
        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Starts intent to take picture
                dispatchTakePictureIntent();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Restore default picture
                mImageView.setImageResource(R.drawable.no_img);

                //Delete previous picture from device
                if (mCurrentPhotoPath != null) {
                    File image = new File(mCurrentPhotoPath);
                    image.delete();
                    mCurrentPhotoPath = null;
                }
            }
        });

        // return View
        return v;
    }

    /**
     * Saves data from input fields
     * And makes notification if it was selected
     */
    public void OnClickSave() {
        // Get input field text
        String title = this.inputTitle.getText().toString();
        String description = this.inputDesc.getText().toString();

        // File name
        String fileName = "saved_data.json";

        // Check if not null
        if (mCurrentPhotoPath == null)
            mCurrentPhotoPath = "No Path";
        //Log.d("PHoto", mCurrentPhotoPath);

        // Generates and/or gets existing file to append to and then Saves it
        JSONToFile(GenerateJSONArray(fileName, title, description, this.priority, this.mCurrentPhotoPath), fileName);
        // Debugging ?
        JSONArray array = null;
        try {
            array = new JSONArray(FileHandler.readFromFile(getContext(), fileName));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (array != null) {
            Log.d("JSON", array.toString());
        }

        //Notification
        SetNotification();

        // Successful action message
        Toast.makeText(getContext(), "Item was successfully added!", Toast.LENGTH_SHORT).show();
    }

    /**
     * Schedules a notification, if reminder is not "No reminder"
     * Based on Reminder variable sets the delay on notification
     */
    private void SetNotification() {
        switch (this.reminder) {
            case "No reminder":
                // do nothing
                break;
            case "10s":
                scheduleNotification(makeNotification("Reminder", "Reminder for: " + this.inputTitle.getText().toString() + " has came!"), 10 * 1000);
                break;
            case "30s":
                scheduleNotification(makeNotification("Reminder", "Reminder for: " + this.inputTitle.getText().toString() + " has came!"), 30 * 1000);
                break;
            case "1m":
                scheduleNotification(makeNotification("Reminder", "Reminder for: " + this.inputTitle.getText().toString() + " has came!"), 1000 * 60);
                break;
            case "5min":
                scheduleNotification(makeNotification("Reminder", "Reminder for: " + this.inputTitle.getText().toString() + " has came!"), 5 * 1000 * 60);
                break;
            case "10m":
                scheduleNotification(makeNotification("Reminder", "Reminder for: " + this.inputTitle.getText().toString() + " has came!"), 10 * 1000 * 60);
                break;
            default:
                // do nothing
                break;

        }
    }

    /**
     * Generates new JSONArray object if it doesn't exist
     * Otherwise reads the existing JSONArray and adds a new entry and returns that
     *
     * @param filename    existing JSONArray file
     * @param title       title of new item
     * @param description description of new item
     * @param priority    priority of new item
     * @return JSONArray, with new entry or existing array with addition of new entry
     */
    private JSONArray GenerateJSONArray(String filename, String title, String description, String priority, String photoPath) {
        // JSON objects
        JSONArray array = null;
        JSONObject myJSON = new JSONObject();

        // try to get existing file
        File file = new File(getContext().getFilesDir(), filename);
        if (file.exists()) {
            // Execute if file already exists
            Log.d("JSON", "exists " + file.getAbsolutePath());
            try {
                // Reads the file
                array = new JSONArray(FileHandler.readFromFile(getContext(), filename));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            // File doesn't exist, create new one
            Log.d("JSON", "doesn't exist " + file.getAbsolutePath());
            array = new JSONArray();
        }

        // Put data into JSON object
        try {
            myJSON.put(AddItemFrag.JSON_TITLE, title);
            myJSON.put(AddItemFrag.JSON_DESCRIPTION, description);
            myJSON.put(AddItemFrag.JSON_PRIORITY, priority);
            myJSON.put(AddItemFrag.JSON_PHOTO, photoPath);
            myJSON.put(AddItemFrag.JSON_TIMESTAMP, GetTime());

            // put JSON object into JSON array
            if (array != null) {
                array.put(myJSON);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // Return result
        return array;
    }

    /**
     * Schedules a notification to be sent after time specified in delay
     * Code used: https://gist.github.com/BrandonSmith/6679223
     *
     * @param notification created Notification with makeNotification
     * @param delay        delay in milliseconds
     */
    private void scheduleNotification(Notification notification, int delay) {
        Intent notificationIntent = new Intent(getContext(), NotificationPublisher.class);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION_ID, 1);
        notificationIntent.putExtra(NotificationPublisher.NOTIFICATION, notification);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);
    }

    /**
     * Builds a notification
     * With default notification sound
     * On click sends user to MainActivity
     *
     * @param title - Title for notification
     * @param text  - main notification text
     * @return built Notification
     */
    private Notification makeNotification(String title, String text) {
        // Notification.Builder(context) is deprecated because of newest versions Type.O
        Notification.Builder builder = new Notification.Builder(getContext());
        // Basic Notification attributes
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setSmallIcon(R.mipmap.ic_launcher);

        // On click actions
        Intent intent = new Intent(getContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);

        // Notification sound
        Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(sound);

        // Return
        return builder.build();
    }

    /**
     * Gets current date and time
     *
     * @return String with current time and date Format template - dd-MM-yyyy HH:mm
     */
    public String GetTime() {
        String time;

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault());
        time = dateFormat.format(calendar.getTime());

        return time;
    }

    /**
     * Writes JSONArray to file
     *
     * @param jsonArray valid JSONArray file to be written
     * @param fileName  of file to be written into e.g. "example.json"
     */
    private void JSONToFile(JSONArray jsonArray, String fileName) {
        try {
            OutputStreamWriter out = new OutputStreamWriter(getContext().openFileOutput(fileName, Context.MODE_PRIVATE));
            out.write(jsonArray.toString());
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Sets which item was selected in Spinner
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.spinnerPriority) {
            this.priority = priorityArray[position];
        } else if (spinner.getId() == R.id.spinnerReminder) {
            this.reminder = reminderArray[position];
        }
    }

    // In case nothing was selected, set priority to Normal
    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Spinner spinner = (Spinner) parent;
        if (spinner.getId() == R.id.spinnerPriority) {
            this.priority = "Normal";
        } else if (spinner.getId() == R.id.spinnerReminder) {
            this.reminder = "No reminder";
        }
    }

    // taken from https://developer.android.com/training/camera/photobasics.html
    // Start an intent which opens Camera and gets the picture back
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getContext().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getContext(),
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    // https://developer.android.com/training/camera/photobasics.html
    // Create image file for photo to be in
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.d("PHOTO", mCurrentPhotoPath);
        return image;
    }

    // Intent result action
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {
            // Set picture from Camera to ImageView
            setPic(mCurrentPhotoPath, mImageView, getActivity());
        }
    }
}
