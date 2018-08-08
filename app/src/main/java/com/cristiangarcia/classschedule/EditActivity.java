package com.cristiangarcia.classschedule;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

public class EditActivity extends AppCompatActivity {

    private ColorPickerView colorView;
    private AlertDialog colorDialog;

    private String previousData;
    private ArrayList<ClassData> pDataSummary;

    private ClassData firstClassData = new ClassData();

    private Menu optionsMenu;
    private TextInputEditText tvName;
    private TextInputEditText tvAdditional;
    private ColorPickerButton colorButton;
    private RecyclerView classesRecycler;

    private ToggleButton tgMon;
    private ToggleButton tgTue;
    private ToggleButton tgWed;
    private ToggleButton tgThu;
    private ToggleButton tgFri;
    private ToggleButton tgSat;
    private ToggleButton tgSun;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        setTitle(R.string.edit_activity_title);
        ActionBar bar = getSupportActionBar();
        if (bar != null)
            bar.setDisplayHomeAsUpEnabled(true);  // Ensure "go back" button is visible

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        tvName = findViewById(R.id.class_name_input);
        tvAdditional = findViewById(R.id.class_opt_data);
        colorButton = findViewById(R.id.color_picker_button);
        tgMon = findViewById(R.id.toggleMon);
        tgTue = findViewById(R.id.toggleTue);
        tgWed = findViewById(R.id.toggleWed);
        tgThu = findViewById(R.id.toggleThu);
        tgFri = findViewById(R.id.toggleFri);
        tgSat = findViewById(R.id.toggleSat);
        tgSun = findViewById(R.id.toggleSun);
        classesRecycler = findViewById(R.id.classes_recyclerview);

        if (extras != null && intent.hasExtra(getResources().getString(R.string.put_json)))
            previousData = extras.getString(getResources().getString(R.string.put_json));
        else
            previousData = "[]";

        if (extras != null && intent.hasExtra(getResources().getString(R.string.put_class_name))) {
            setClassName(extras.getString(getResources().getString(R.string.put_class_name)));
            firstClassData.setName(this.getClassName());
        }// else
         //   firstClassData = null;  // I don't really know, I guess

        if (extras != null && intent.hasExtra(getResources().getString(R.string.put_class_additional_data))) {
            setAdditionalData(extras.getString(getResources().getString(R.string.put_class_additional_data)));
            firstClassData.setAdditionalData(this.getAdditionalData());
        }

        if (extras != null && intent.hasExtra(getResources().getString(R.string.put_class_start_time))) {
            setStartTime(extras.getString(getResources().getString(R.string.put_class_start_time)));
            firstClassData.setStartTime(this.getClassStartTime());
        } else
            setStartTime("7:30");

        if (extras != null && intent.hasExtra(getResources().getString(R.string.put_class_end_time))) {
            setEndTime(extras.getString(getResources().getString(R.string.put_class_end_time)));
            firstClassData.setEndTime(this.getClassEndTime());
        } else
            setEndTime("9:00");

        if (extras != null && intent.hasExtra(getResources().getString(R.string.put_class_color))) {
            setClassColor(extras.getInt(getResources().getString(R.string.put_class_color)));
            firstClassData.setColor(getClassColor());
        }

        if (extras != null && intent.hasExtra(getResources().getString(R.string.put_class_days))) {
            setSelectedDays(new String[]{extras.getString(getResources().getString(R.string.put_class_days))});
            firstClassData.setDay(getSelectedDays()[0]);
        }

        tvName.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence sequence, int start, int before, int end) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateSavable();
            }

            @Override
            public void beforeTextChanged(CharSequence sequence, int start, int before, int end) {
            }
        });

        colorButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showColorDialog();
            }
        });

        ToggleButton[] buttons = new ToggleButton[]{tgMon, tgTue, tgWed, tgThu, tgFri, tgSat, tgSun};
        for (ToggleButton btn : buttons) {
            btn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    updateSavable();
                }
            });
        }

        TimePicker[] timePickers = new TimePicker[]{findViewById(R.id.start_time_input), findViewById(R.id.start_time_input)};
        for (TimePicker picker : timePickers) {
            picker.setOnTimeChangedListener(new TimePicker.TimeChangedListener() {
                @Override
                public void onTimeChanged(String time) {
                    updateSavable();
                }
            });
        }

        showSummarizedData();
    }

    private void summarizeData() {
        pDataSummary = new ArrayList<>();

        if (previousData == null)
            return;

        JSONArray array;
        JSONObject obj;
        ClassData data;

        boolean useful = true;

        try {
            array = new JSONArray(previousData);
            for (int i = 0; i < array.length(); i++) {
                obj = (JSONObject) array.get(i);
                useful = true;

                for (ClassData value : pDataSummary) {
                    if (value.getName().equals(obj.getString("name")) &&
                            value.getColor() == Color.parseColor(obj.getString("color"))) {

                        useful = false;
                        break;
                    }
                }

                if (useful) {
                    data = new ClassData(obj.getString("name"), Color.parseColor(obj.getString("color")));
                    pDataSummary.add(data);
                }
            }

        } catch (org.json.JSONException e) {
            Pojo.addLog(getApplicationContext(), e.getMessage());
        }
    }

    private void showSummarizedData() {
        summarizeData();

        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        classesRecycler.setLayoutManager(llm);

        ClassAdapter adapter = new ClassAdapter(pDataSummary, this);
        classesRecycler.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_edit_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        optionsMenu = menu;
        updateSavable();
        return true;
    }

    private void showColorDialog() {
        if (colorDialog == null) {
            colorView = new ColorPickerView(getBaseContext());

            colorDialog = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.color_picker_dialog_title))
                    .setView(colorView)
                    .setPositiveButton(getResources().getString(R.string.color_picker_dialog_ok), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            colorButton.setSelectedColor(colorView.getSelectedColor());
                        }
                    })
                    .setNegativeButton(getResources().getString(R.string.color_picker_dialog_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create();
        } else {
            colorView.setSelectedColor(colorButton.getSelectedColor());
        }

        colorDialog.show();

        Display screenSize = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        screenSize.getSize(size);

        int dSize = size.x;
        if (size.y < dSize) dSize = size.y;

        dSize -= dSize / 20;  // Just a little margin

        Window win = colorDialog.getWindow();
        if (win != null)
            win.setLayout(dSize, dSize);
    }

    private String getClassName() {
        return tvName.getText().toString();
    }

    public void setClassName(String name) {
        tvName.setText(name);
    }

    private String getAdditionalData() {
        return tvAdditional.getText().toString();
    }

    private void setAdditionalData(String data) {
        tvAdditional.setText(data);
    }

    private String getClassStartTime() {
        return ((TimePicker)findViewById(R.id.start_time_input)).getTime();
    }

    private void setClassStartTime(String time) {
        ((TimePicker)findViewById(R.id.start_time_input)).setTime(time);
    }

    private String getClassEndTime() {
        return ((TimePicker)findViewById(R.id.end_time_input)).getTime();
    }

    private void setClassEndTime(String time) {
        ((TimePicker)findViewById(R.id.end_time_input)).setTime(time);
    }

    private int getClassColor() {
        return colorButton.getSelectedColor();
    }

    public void setClassColor(int color) {
        colorButton.setSelectedColor(color);
    }

    private String getStartTime() {
        return ((TimePicker)findViewById(R.id.start_time_input)).getTime();
    }

    private void setStartTime(String time) {
        ((TimePicker)findViewById(R.id.start_time_input)).setTime(time, true);
    }

    private String getEndTime() {
        return ((TimePicker)findViewById(R.id.end_time_input)).getTime();
    }

    private void setEndTime(String time) {
        ((TimePicker)findViewById(R.id.end_time_input)).setTime(time, true);
    }

    private String[] getSelectedDays() {
        String days = "";

        if (tgSun.isChecked()) { days += "sun#"; }
        if (tgMon.isChecked()) { days += "mon#"; }
        if (tgTue.isChecked()) { days += "tue#"; }
        if (tgWed.isChecked()) { days += "wed#"; }
        if (tgThu.isChecked()) { days += "thu#"; }
        if (tgFri.isChecked()) { days += "fri#"; }
        if (tgSat.isChecked()) { days += "sat#"; }

        if (days.endsWith("#"))
            days = days.substring(0, days.length() - 1);

        String[] result = days.split("#");
        if (result.length == 1 && result[0].equals(""))
            return new String[0];
        else
            return result;
    }

    private void setSelectedDays(String[] days) {
        tgSun.setChecked(Pojo.contains(days, "sun"));
        tgMon.setChecked(Pojo.contains(days, "mon"));
        tgTue.setChecked(Pojo.contains(days, "tue"));
        tgWed.setChecked(Pojo.contains(days, "wed"));
        tgThu.setChecked(Pojo.contains(days, "thu"));
        tgFri.setChecked(Pojo.contains(days, "fri"));
        tgSat.setChecked(Pojo.contains(days, "sat"));
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(getResources().getString(R.string.put_json), previousData);
        savedInstanceState.putString(getResources().getString(R.string.put_class_name), getClassName());
        savedInstanceState.putString(getResources().getString(R.string.put_class_additional_data), getAdditionalData());
        savedInstanceState.putString(getResources().getString(R.string.put_class_start_time), getClassStartTime());
        savedInstanceState.putString(getResources().getString(R.string.put_class_end_time), getClassEndTime());
        savedInstanceState.putInt(getResources().getString(R.string.put_class_color), getClassColor());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        previousData = savedInstanceState.getString(getResources().getString(R.string.put_json));
        setClassName(savedInstanceState.getString(getResources().getString(R.string.put_class_name)));
        setAdditionalData(savedInstanceState.getString(getResources().getString(R.string.put_class_additional_data)));
        setClassStartTime(savedInstanceState.getString(getResources().getString(R.string.put_class_start_time)));
        setClassEndTime(savedInstanceState.getString(getResources().getString(R.string.put_class_end_time)));
        setClassColor(savedInstanceState.getInt(getResources().getString(R.string.put_class_color)));
    }

    private void addCurrentData(Intent intent) {
        intent.putExtra(getResources().getString(R.string.new_class), true);
        intent.putExtra(getResources().getString(R.string.add_class_name), getClassName());
        intent.putExtra(getResources().getString(R.string.add_class_additional_data), getAdditionalData());
        intent.putExtra(getResources().getString(R.string.add_class_start_time), getClassStartTime());
        intent.putExtra(getResources().getString(R.string.add_class_end_time), getClassEndTime());
        intent.putExtra(getResources().getString(R.string.add_class_color), getClassColor());
        intent.putExtra(getResources().getString(R.string.add_class_days), getSelectedDays());
    }

    private void addFirstDataToDelete(Intent intent) {
        intent.putExtra(getResources().getString(R.string.delete_class), true);
        intent.putExtra(getResources().getString(R.string.delete_class_name), firstClassData.getName());
        intent.putExtra(getResources().getString(R.string.delete_class_additional_data), firstClassData.getAdditionalData());
        intent.putExtra(getResources().getString(R.string.delete_class_start_time), firstClassData.getStartTime());
        intent.putExtra(getResources().getString(R.string.delete_class_end_time), firstClassData.getEndTime());
        intent.putExtra(getResources().getString(R.string.delete_class_color), firstClassData.getColor());
        intent.putExtra(getResources().getString(R.string.delete_class_days), new String[] { firstClassData.getDay() });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(getResources().getString(R.string.put_json), previousData);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        switch (item.getItemId()) {
            case R.id.action_save_class:
                addCurrentData(intent);

                if (firstClassData != null && !firstClassData.equalsTo(null))
                    // Delete previous data
                    addFirstDataToDelete(intent);

                this.startActivity(intent);
                return true;

            case R.id.action_delete_class:
                if (firstClassData != null)
                    addFirstDataToDelete(intent);

                this.startActivity(intent);

                return true;

            case android.R.id.home:
                // Prevent destroy current displayed data
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setSaveItemEnabled(boolean enabled) {
        if (optionsMenu == null) return;

        MenuItem item;

        for (int i=0; i<optionsMenu.size(); i++) {
            item = optionsMenu.getItem(i);
            if (item.getItemId() == R.id.action_save_class) {
                if (enabled)
                    item.getIcon().setAlpha(255);
                else
                    item.getIcon().setAlpha(130);

                item.setEnabled(enabled);
                break;
            }
        }
    }

    private void updateSavable() {
        setSaveItemEnabled(!getClassName().equals("") &&
                           Pojo.getElapsedTime(getStartTime(), getEndTime()) > 0 &&
                           getSelectedDays().length > 0);
    }
}
