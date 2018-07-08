package Activities;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;


import java.util.Calendar;

import Data.DataBaseHandler;

import Model.Target;
import Receiver.AlarmReceiver;
import Receiver.SetDueOnComplete;
import Util.AlarmCreater;
import Util.CurrentDateAndTime;
import Util.Validator;
import siddharthbisht.targettracker.R;

public class AddTargetActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText etTopic;
    private TextView date;
    private TextView time;
    private Button submit;
    DataBaseHandler handler;
    private int mYear, mMonth, mDay, mHour, mMinute;
    private int sYear, sMonth, sDate, sHour, sMinute;
    public AlarmCreater creater;
    public Validator validator;
    public static final String TAG="AddTargetActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_target2);
        etTopic = findViewById(R.id.etAddTopicName);
        date = findViewById(R.id.tvDateToStore);
        time = findViewById(R.id.tvTimeToStore);
        submit = findViewById(R.id.btSubmitAdd);
        handler=new DataBaseHandler(this);
        date.setOnClickListener(this);
        time.setOnClickListener(this);
        submit.setOnClickListener(this);
        creater=new AlarmCreater();
        validator=new Validator();
        initializeDateTime();
    }

    /**
     * Add a method to store the information in the list and migrate to the current task fragement(Main Activity)
     * Add a  method to put current date and current time in date and time textviews
     * Add a method to put onclick listenet in date and time to store the date and time entered by the user
     */


    public void initializeDateTime() {
        Calendar calendar=Calendar.getInstance();
        mYear=calendar.get(Calendar.YEAR);
        mMonth=calendar.get(Calendar.DAY_OF_MONTH);
        mDay=calendar.get(Calendar.DAY_OF_MONTH);
        mHour=calendar.get(Calendar.HOUR_OF_DAY);
        mMinute=calendar.get(Calendar.MINUTE);
        String sDateStr=mDay+"/"+(mMonth+1)+"/"+mYear;
        String sTimeStr=mHour+":"+mMinute;
        date.setText(sDateStr);
        time.setText(sTimeStr);
        String s=mYear+"/"+mMonth+"/"+mDay;
        Log.d("TodayDate:",s);
        sYear=mYear;
        sMonth=mMonth;
        sDate=mDay;
        String s1=sDate+"/"+sMonth+"/"+sYear;
        Log.d("currentDate:",s1);
        sHour=mHour;
        sMinute=mMinute;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tvDateToStore:
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddTargetActivity.this,
                        new DatePickerDialog.OnDateSetListener() {

                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                sYear=year;
                                sMonth=monthOfYear;
                                sDate=dayOfMonth;
                                date.setText(sDate + "-" + (sMonth + 1) + "-" + sYear);
                                String s=sYear+"/"+sMonth+"/"+sDate;
                                Log.d("DatePicker:",s);
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();
                break;
            case R.id.tvTimeToStore:
                // Launch Time Picker Dialog
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddTargetActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {

                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay,
                                                  int minute) {
                                sHour=hourOfDay;
                                sMinute=minute;
                                time.setText(sHour+ ":" +sMinute);
                                String s=sHour+":"+sMinute;
                                Log.d("TimePicker:",s);

                            }
                        }, mHour, mMinute, false);
                timePickerDialog.show();

                break;
            case R.id.btSubmitAdd:
                if (etTopic.getText().toString().isEmpty()){
                    Toast.makeText(AddTargetActivity.this,"Enter topic name",Toast.LENGTH_SHORT).show();
                    date.setText(mDay+"/"+mMonth+"/"+mYear);
                    time.setText(mHour+":"+mMinute);
                }
                else{
                    if (validator.validateDateTime(sYear,sMonth,sDate,sHour,sMinute)==true){

                        time.setText(sHour+":"+sMinute);
                        date.setText(sDate + "/" + (sMonth + 1) + "/" + sYear);
                        saveTargetToDb(v, sYear, sMonth, sDate, sHour, sMinute);
                    }
                    else {
                        Toast.makeText(AddTargetActivity.this,"Enter time",Toast.LENGTH_SHORT).show();
                    }
                }

                Log.d("Inside button:", String.valueOf(sYear) + "/" + String.valueOf(sMonth) + "/" + String.valueOf(sDate) + " //" + String.valueOf(sHour) + ":" + String.valueOf(sMinute));
                }
    }


    private void saveTargetToDb(View v,int year, int month, int date, int hours, int minutes) {
        Target target = new Target();
        String topic = etTopic.getText().toString();
        target.setTopic(topic);
        target.setFinishDate(date);
        target.setFinishMonth(month);
        target.setFinishYear(year);
        target.setFinishHour(hours);
        target.setFinishMinute(minutes);
        target.setDue(0);
        target.setCompletionStatus(0);
        //Save to database
        handler.addTarget(target);
        Log.d("added target:",target.getTopic());
        int id=handler.getLastItem().getId();
        Log.d("added item id:",String.valueOf(id));
        //Getting alarm time in millis
        Long timeinMillis=getTimeInMillis(year,month,date,hours,minutes);
        creater.setDueStatus(AddTargetActivity.this,id,(timeinMillis));
        creater.setAlarm(AddTargetActivity.this,(timeinMillis-(15*60*1000)),id,target.getTopic());
        Snackbar.make(v,  target.getTopic()+" Saved", Snackbar.LENGTH_LONG).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent=new Intent(AddTargetActivity.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                finish();
            }
        }, 1000);
    }
    public long getTimeInMillis(int year,int month,int date,int hour,int minutes){
        Calendar calendar=Calendar.getInstance();
        calendar.set(year,month,date,hour,minutes,0);
        Log.d("time:",String.valueOf(year)+"/"+String.valueOf(month)+"/"+String.valueOf(date)+" //"+String.valueOf(hour)+":"+String.valueOf(minutes));
        return calendar.getTimeInMillis();
    }
}


