package com.tkivilius.projectapp;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


// Single Method - readFRomFile
// Because method is required in BOTH Add and List fragments, it is seperated
public class FileHandler {

    /**
     * Reads a String from file
     * Credits to someone from https://stackoverflow.com/
     *
     * @param context  activity context
     * @param fileName to be read
     * @return read String
     */
    public static String readFromFile(Context context, String fileName) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(fileName);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString;
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.d("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.d("login activity", "Can not read file: " + e.toString());
        }

        return ret;
    }
}
