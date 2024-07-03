package com.example.todolist;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView listView;
    private Button button;
    private CheckBox reminderCheckBox;
    private Button timePickerButton;
    private SharedPreferences sharedPreferences;
    private Calendar reminderTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listView = findViewById(R.id.listView);
        button = findViewById(R.id.button);
        reminderCheckBox = findViewById(R.id.reminderCheckBox);
        timePickerButton = findViewById(R.id.timePickerButton);

        sharedPreferences = getSharedPreferences("com.example.todolist", Context.MODE_PRIVATE);

        items = new ArrayList<>();
        loadItems();

        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(itemsAdapter);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                addItem(view);
            }
        });

        timePickerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                showTimePicker();
            }
        });

        reminderCheckBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Show or hide the time picker button based on the checkbox state
            if (isChecked) {
                timePickerButton.setVisibility(View.VISIBLE);
            } else {
                timePickerButton.setVisibility(View.GONE);
                reminderTime = null; // Clear reminder time if checkbox is unchecked
            }
        });

        setUpListViewListener();
    }

    private void setUpListViewListener() {
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                Context context = getApplicationContext();
                Toast.makeText(context, "Item Removed", Toast.LENGTH_SHORT).show();

                items.remove(i);
                itemsAdapter.notifyDataSetChanged();
                saveItems();
                return true;
            }
        });
    }

    private void addItem(View view) {
        EditText input = findViewById(R.id.editText2);
        String itemText = input.getText().toString();

        if (!(itemText.equals(""))) {
            itemsAdapter.add(itemText);
            input.setText("");
            saveItems();

            if (reminderCheckBox.isChecked() && reminderTime != null) {
                setReminder(itemText, reminderTime); // Set a reminder for the task if checkbox is checked
            }
        } else {
            Toast.makeText(getApplicationContext(), "Please enter text", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveItems() {
        Set<String> set = new HashSet<>(items);
        sharedPreferences.edit().putStringSet("tasks", set).apply();
    }

    private void loadItems() {
        Set<String> set = sharedPreferences.getStringSet("tasks", new HashSet<>());
        items.addAll(set);
    }

    private void showTimePicker() {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                reminderTime = Calendar.getInstance();
                reminderTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                reminderTime.set(Calendar.MINUTE, minute);
                reminderTime.set(Calendar.SECOND, 0);
                reminderTime.set(Calendar.MILLISECOND, 0);


            }
        }, hour, minute, true);

        timePickerDialog.show();
    }

    private void setReminder(String task, Calendar reminderTime) {
        // Convert the reminder time to milliseconds
        long reminderTimeInMillis = reminderTime.getTimeInMillis();

        Intent intent = new Intent(this, NotificationReceiver.class);
        intent.putExtra("task", task);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTimeInMillis, pendingIntent);

        Toast.makeText(this, "Reminder set for " + reminderTime.get(Calendar.HOUR_OF_DAY) + ":" + String.format("%02d", reminderTime.get(Calendar.MINUTE)), Toast.LENGTH_SHORT).show();
    }
}
