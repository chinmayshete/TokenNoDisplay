package com.example.tokennumberdisplay;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    String[] mode = { "Select Mode","1", "2", "3"};
    MaterialButton button_0,button_1,button_2,button_3,button_4,button_5,button_6,button_7,button_8,button_9;
    MaterialButton button_increment,button_decrement;
    MaterialButton button_enter,button_del_tok,button_clear,button_send;

    TextView current_textview,display_textview;
    Spinner current_mode,display_mode;

    MaterialButton button;
    String buttonText;
    String currentText;
    int value,currentValue;
    Map<Integer, String> modeTokenMap = new HashMap<>();
    DatabaseReference dbref;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FirebaseApp.initializeApp(this);
        dbref = FirebaseDatabase.getInstance().getReference("ModeData");

        current_textview = findViewById(R.id.current_textView);
        display_textview = findViewById(R.id.display_textView);

        current_mode = findViewById(R.id.current_mode);
        display_mode = findViewById(R.id.display_mode);

        assignId(button_0,R.id.button_0);
        assignId(button_1,R.id.button_1);
        assignId(button_2,R.id.button_2);
        assignId(button_3,R.id.button_3);
        assignId(button_4,R.id.button_4);
        assignId(button_5,R.id.button_5);
        assignId(button_6,R.id.button_6);
        assignId(button_7,R.id.button_7);
        assignId(button_8,R.id.button_8);
        assignId(button_9,R.id.button_9);

        assignId(button_enter,R.id.button_enter);
        assignId(button_del_tok,R.id.button_del_tok);
        assignId(button_clear,R.id.button_clear);
        assignId(button_send,R.id.button_send);

        assignId(button_increment,R.id.button_increment);
        assignId(button_decrement,R.id.button_decrement);

        ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item,mode);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        current_mode.setOnItemSelectedListener(this);
        display_mode.setOnItemSelectedListener(this);

        current_mode.setAdapter(aa);
        display_mode.setAdapter(aa);

//        current_mode.setOnClickListener(this);
//        display_mode.setOnClickListener(this);

        dbref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String token = snapshot.getValue(String.class);
                    int modeIndex = Integer.parseInt(Objects.requireNonNull(snapshot.getKey()));
                    modeTokenMap.put(modeIndex, token);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle the error
                Toast.makeText(getApplicationContext(), "Failed to fetch data from Firebase", Toast.LENGTH_SHORT).show();
            }
        });

    }

    void assignId(MaterialButton btn,int id){
        btn = findViewById(id);
        btn.setOnClickListener(this);
    }


    public void onClick(View view) {
        if (view instanceof Spinner) {
            Spinner spinner = (Spinner) view;

            if (spinner.getId() == R.id.current_mode) {
                handleCurrentModeSpinner();
            } else if (spinner.getId() == R.id.display_mode) {
                handleDisplayModeSpinner();
            }
        } else if (view instanceof MaterialButton) {
            handleButtonClick((MaterialButton) view);
        }
    }

    private void handleCurrentModeSpinner() {
        int selectedModeIndex = current_mode.getSelectedItemPosition();
        String dataForSelectedMode = modeTokenMap.get(selectedModeIndex);
        current_textview.setText(dataForSelectedMode != null ? dataForSelectedMode : "");
    }

    private void handleDisplayModeSpinner() {
        int selectedDisplayModeIndex = display_mode.getSelectedItemPosition();
        String storedToken = modeTokenMap.get(selectedDisplayModeIndex);
        display_textview.setText(storedToken != null ? storedToken : "");
    }

    private void handleButtonClick(MaterialButton button) {
        buttonText = button.getText().toString();

        if ("DEL_TOK".equals(buttonText)) {
            // Handle DEL_TOK button click
            handleDelTokButtonClick();
        } else if ("CLR_ALL".equals(buttonText)) {
            // Handle CLR_ALL button click
            handleClrAllButtonClick();
        } else {
            try {
                value = Integer.parseInt(buttonText);
            } catch (NumberFormatException e) {
                value = 0;
            }

            currentText = display_textview.getText().toString();

            if (!buttonText.equals("ENTER") && !buttonText.equals("DEL_TOK") && !buttonText.equals("CLR_ALL") && !buttonText.equals("SEND")) {
                if (!currentText.isEmpty()) {
                    try {
                        currentValue = Integer.parseInt(currentText);

                        if (currentValue < 99) {
                            currentValue = currentValue * 10 + value;
                            display_textview.setText(String.valueOf(currentValue));
                        } else {
                            Toast.makeText(getApplicationContext(), "Maximum three digits allowed", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        currentValue = 0;
                    }
                } else {
                    display_textview.setText(String.valueOf(value));
                }
            }

            if ("ENTER".equals(buttonText)) {
                int selectedDisplayModeIndex = display_mode.getSelectedItemPosition();
                String enteredData = display_textview.getText().toString();

                if (!enteredData.isEmpty()) {
                    dbref.child(String.valueOf(selectedDisplayModeIndex)).setValue(enteredData);
                    String toastMessage = "Data for Display Mode " + selectedDisplayModeIndex + " stored in Firebase.";
                    Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter data before clicking ENTER.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void handleDelTokButtonClick() {
        int selectedDisplayModeIndex = display_mode.getSelectedItemPosition();

        // Check if there's data at the selected index
        if (modeTokenMap.containsKey(selectedDisplayModeIndex)) {
            // Remove data from the database
            dbref.child(String.valueOf(selectedDisplayModeIndex)).removeValue();

            // Remove data from the local map
            modeTokenMap.remove(selectedDisplayModeIndex);

            // Clear the display_textview
            display_textview.setText("");

            // Show a toast message indicating successful deletion
            String toastMessage = "Data for Display Mode " + selectedDisplayModeIndex + " deleted from Firebase.";
            Toast.makeText(getApplicationContext(), toastMessage, Toast.LENGTH_SHORT).show();
        } else {
            // Show a toast message indicating no data to delete
            Toast.makeText(getApplicationContext(), "No data to delete at Display Mode " + selectedDisplayModeIndex, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleClrAllButtonClick() {
        // Check if there is any data in the database
        if (!modeTokenMap.isEmpty()) {
            // Remove data for every index from the database
            for (int modeIndex : modeTokenMap.keySet()) {
                dbref.child(String.valueOf(modeIndex)).removeValue();
            }

            // Clear the local map
            modeTokenMap.clear();

            // Clear the display_textview
            display_textview.setText("");

            // Show a toast message indicating successful deletion
            Toast.makeText(getApplicationContext(), "All data for every index deleted from Firebase.", Toast.LENGTH_SHORT).show();
        } else {
            // Show a toast message indicating no data to delete
            Toast.makeText(getApplicationContext(), "No data to delete.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        if (position >= 1 && position <= 3) {
            String storedToken = modeTokenMap.get(position);
            //display_textview.setText(storedToken != null ? storedToken : "");
            Toast.makeText(getApplicationContext(), "Selected Mode: " + position +
                    "\nToken: " + (storedToken != null ? storedToken : ""), Toast.LENGTH_SHORT).show();

            // Update _tok_textView with the corresponding data
            //((TextView) findViewById(R.id._tok_textView)).setText(storedToken != null ? storedToken : "");

            // If it's the display_mode spinner, set the text in display_textview
            if (parent.getId() == R.id.display_mode) {
                display_textview.setText(storedToken != null ? storedToken : "");
            }

            // If it's the current_mode spinner, update current_textview based on the selected mode
            if (parent.getId() == R.id.current_mode) {
                int selectedModeIndex = current_mode.getSelectedItemPosition();
                String dataForSelectedMode = modeTokenMap.get(selectedModeIndex);
                current_textview.setText(dataForSelectedMode != null ? dataForSelectedMode : "");
            }
        }

        else {
            onNothingSelected(parent);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        //Toast.makeText(getApplicationContext(), "NOTHING SELECTED IN SPINNER", Toast.LENGTH_SHORT).show();
    }

}






