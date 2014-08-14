/*
 * This file is part of the CRN Toolbox Center.
 * Copyright 2010-2011 Jakob Weigert, University of Passau
 */

package de.uni_passau.fim.esl.crn_toolbox_center.tools;

import java.io.File;
import java.util.Arrays;
import java.util.Stack;

import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import de.uni_passau.fim.esl.crn_toolbox_center.R;

/**
 * Represents a file system browser to let the user choose a file of the
 * desired type.
 * 
 * @author Jakob Weigert <weigert@fim.uni-passau.de> (University of Passau)
 *
 */
public class FileSystemBrowserJson extends ListActivity {

    // Set here the desired file extension for filtering or "" for no filtering:
    private static final String FILE_EXTENSION_FILTER = ".json";
    
    private static final String LIST_DIR_EXCEPTION = "Unable to read directory content";
    private static final String PREFS_NAME = "Preferences";
    private static final String PREF_KEY_CUR_DIR = "currentDirectory";
    private static final String PARENT_DIR = "../";
    private static final String SLASH = "/";
    private static final String DEFAULT_DIR = "/mnt/sdcard";
    private static final int RESULT_CODE_FILE_SELECTED = 42;


    ArrayAdapter<String> mArrayAdapter;
    Stack<String> mPathStack = new Stack<String>();

    /**
     * Creates a file system browser, which lets the user
     * select a file of the given type. 
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.filelistview);

        mArrayAdapter = new ArrayAdapter<String>(this, R.layout.simplerow);

        setListAdapter(mArrayAdapter);

        // Restore preferences (last directory)
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        String lastDirectory = settings.getString(PREF_KEY_CUR_DIR, DEFAULT_DIR);

        mPathStack.push(SLASH);
        if (lastDirectory != "") {
            String[] pieces = lastDirectory.split(SLASH);
            for (int i = 1; i < pieces.length; i++) {
                mPathStack.push(mPathStack.peek() + pieces[i] + SLASH);
            }
        }

        listDirectoryContent(mPathStack.peek());

    }

    /**
     * Changes the directory or returns a selected file to the
     * activity which created this file system browser.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        if (v instanceof TextView) {
            TextView textView = (TextView) v;
            String entry = textView.getText().toString();
            if (entry.endsWith(PARENT_DIR)) {
                mPathStack.pop();
                listDirectoryContent(mPathStack.peek());
            } else if (entry.endsWith(SLASH)) {
                mPathStack.push(mPathStack.peek() + entry);
                listDirectoryContent(mPathStack.peek());
            } else {
                // File selected. At first save current directory for next
                // use of file system browser.
                SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString(PREF_KEY_CUR_DIR, mPathStack.peek());
                editor.commit();

                // Set path as result and finish.
                Intent data = new Intent();
                Uri.Builder builder = new Uri.Builder();
                builder.path(mPathStack.peek() + entry);
                data.setData(builder.build());
                setResult(RESULT_CODE_FILE_SELECTED, data);
                finish();

            }
        }
    }

    /**
     * Lists the contents of a directory and displays it.
     * 
     * @param directory The directory path.
     */
    @SuppressWarnings("unused") // Unreachable code depending on filter constant
    private void listDirectoryContent(String directory) {
        try {
            mArrayAdapter.clear();
            if (!directory.equals(SLASH)) {
                mArrayAdapter.add(PARENT_DIR);
            }
            File path = new File(directory);
            File[] files = path.listFiles();
            Arrays.sort(files);
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    mArrayAdapter.add(files[i].getName() + SLASH);
                }
            }
            for (int i = 0; i < files.length; i++) {
                if (!files[i].isDirectory()) {
                    if (FILE_EXTENSION_FILTER != "") {
                        if (files[i].getName().toLowerCase().endsWith(FILE_EXTENSION_FILTER)) {
                            mArrayAdapter.add(files[i].getName());
                        }
                    } else { // If no filtering is desired
                        mArrayAdapter.add(files[i].getName());
                    }
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, LIST_DIR_EXCEPTION, Toast.LENGTH_LONG).show();
        }
        setTitle(directory);
    }

}