package com.tkivilius.projectapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout mainDrawer;
    private int permissionRequestCode = 69;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Permission check
        CheckPermissions();

        // Toolbar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.mainToolbar);
        myToolbar.setTitle("Default");
        setSupportActionBar(myToolbar);

        // Drawer items
        this.mainDrawer = (DrawerLayout) findViewById(R.id.mainDrawer);

        // Create DrawerToggle
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, mainDrawer, myToolbar, R.string.drawer_button, R.string.drawer_button);
        mainDrawer.addDrawerListener(drawerToggle);
        drawerToggle.syncState();

        setNavigationViewListener();
    }

    /**
     * Check if Camera and External storage permissions are available
     * If they are not, request them
     */
    private void CheckPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, permissionRequestCode);
        }
    }

    // Permission response, simple Toast messages
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode == permissionRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Camera permission granted!", Toast.LENGTH_SHORT).show();
            }
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "External storage permission granted!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Adds menu items to Toolbar
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    // Set listener for Drawer
    private void setNavigationViewListener() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);
        // When app is first open to automatically open List
        navigationView.getMenu().performIdentifierAction(R.id.list, 0);
    }

    // Toolbar item click actions
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.deleteSetting: {
                // Delete the data file
                File file = new File(getFilesDir(), "saved_data.json");
                file.delete();

                // Delete pictures in directory
                File dir = new File(getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString());
                if (dir.isDirectory()) {
                    String[] children = dir.list();
                    for (int i = 0; i < children.length; i++) {
                        new File(dir, children[i]).delete();
                    }
                }

                return true;
            }
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    // Actions to perform when Drawer items are clicked
    // Swaps fragments ListFrag and AddItemFrag
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        switch (item.getItemId()) {
            case R.id.list:
                //Toast.makeText(this, "ListFrag was clicked", Toast.LENGTH_SHORT).show();
                ListFrag frag1 = new ListFrag();
                fragmentTransaction.replace(R.id.fragmentPlaceholder, frag1);
                break;
            case R.id.addToList:
                //Toast.makeText(this, "Add was clicked", Toast.LENGTH_SHORT).show();
                AddItemFrag frag2 = new AddItemFrag();
                fragmentTransaction.replace(R.id.fragmentPlaceholder, frag2);
                break;
            default:
                Toast.makeText(this, "Unhandled click", Toast.LENGTH_SHORT).show();
        }

        fragmentTransaction.commit();

        this.mainDrawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
