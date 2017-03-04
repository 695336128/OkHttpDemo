package com.example.zhang.okhttpdemo.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.zhang.okhttpdemo.R;
import com.example.zhang.okhttpdemo.event.TestEvent;

import org.greenrobot.eventbus.EventBus;

public class EventBusActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText eventEt;
    private Button submitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        initView();

    }

    private void initView() {
        eventEt = (EditText) findViewById(R.id.eventEt);
        submitBtn = (Button) findViewById(R.id.submitBtn);

        submitBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.submitBtn:
                submit();
                break;
        }
    }

    private void submit() {
        // validate
        String eventEtString = eventEt.getText().toString().trim();
        if (TextUtils.isEmpty(eventEtString)) {
            Toast.makeText(this, "eventEtString不能为空", Toast.LENGTH_SHORT).show();
            return;
        }
        TestEvent event = new TestEvent();
        event.setEventName(eventEtString);
        EventBus.getDefault().post(event);
        finish();

    }
}
