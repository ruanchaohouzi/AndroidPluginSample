package com.ruanchao.hookdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.ruanchao.hookdemo.hook.HookUtils;

public class MainActivity extends AppCompatActivity {

    private Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButton = findViewById(R.id.btn_test);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this,"我是正常的",Toast.LENGTH_LONG).show();
            }
        });

        //hook OnClickListener,在执行点击之前干了一些自己的坏事
        HookUtils.hookOnclick(this,mButton);
    }
}
