package org.hiranoaiku.mikrolab;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import org.hiranoaiku.mikrolab.controller.AudioSpeaker;
import org.hiranoaiku.mikrolab.model.Clock;
import org.hiranoaiku.mikrolab.controller.KeyRunner;
import org.hiranoaiku.mikrolab.model.Profiler;
import org.hiranoaiku.mikrolab.model.Ports;
import org.hiranoaiku.mikrolab.model.Processor;
import org.hiranoaiku.mikrolab.model.Memory;
import org.hiranoaiku.mikrolab.model.Rom;
import org.hiranoaiku.mikrolab.view.DataView;
import org.hiranoaiku.mikrolab.controller.DisplayRunner;
import org.hiranoaiku.mikrolab.view.Switch;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener
{

    private Processor cpu;
    private Memory memory;
    private Ports ports;
    private Clock clock;
    private DisplayRunner displayRunner;
    private Thread clockThread, displayThread, perfThread;
    private Button buttonReset, buttonSettings, buttonRun;
    private Switch switch0, switch1, switch2, switch3;
    private Vibrator vibrator;
    private KeyRunner keyRunner;
    public AudioSpeaker audioSpeaker;
    private Profiler perfMonitor;
    private AlertDialog.Builder settingsDialog, aboutDialog;

    public HashMap<Integer, Button> buttons;
    public final int buttonIds[] = new int[] {
            R.id.buttonH0, R.id.buttonH1, R.id.buttonH2, R.id.buttonH3,
            R.id.buttonH4, R.id.buttonH5, R.id.buttonH6, R.id.buttonH7,
            R.id.buttonH8, R.id.buttonH9, R.id.buttonHA, R.id.buttonHB,
            R.id.buttonHC, R.id.buttonHD, R.id.buttonHE, R.id.buttonHF,
            R.id.buttonRun, R.id.buttonReturn, R.id.buttonSet,
            R.id.buttonDec, R.id.buttonInc, R.id.buttonWrite,
            R.id.buttonOutput, R.id.buttonInput
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        setContentView(R.layout.activity_control_pad);

        initControls();
        createSystem();
        createDialogs();
        initSystem();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        runThreads();
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent)
    {
        switch(motionEvent.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                controlDown(view);
                tactileFeedback();
                break;
            case MotionEvent.ACTION_UP:
                controlUp(view);
                break;
        }
        return true;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        clock.stop();
        displayRunner.stop();
        perfMonitor.stop();
    }

    private void controlUp(View view)
    {
        keyRunner.resetPressedKey();
        ports.setPort(Ports.portA, (char) 0xFF);
    }

    private void controlDown(View view)
    {
        if(view == buttonReset)
        {
            deviceReset();
        }
        else if(view == buttonSettings)
        {
            settingsDialog.show();
        }
        else if(view == switch3) // ШАГ/АВТ
        {
            switch3.toggle();
            clock.enableStepMode(switch3.getData() != 0);
        }
        else if(view == switch2)
        {
            switch2.toggle();
            ports.setPort(Ports.portC, (char) ((switch2.dataIndex() << 1) | (ports.getPort(Ports.portC) & 0xFD)));
        }
        else if(view == switch1)
        {
            switch1.toggle();
            ports.setPort(Ports.portC, (char) ((switch1.dataIndex() << 2) | (ports.getPort(Ports.portC) & 0xFB)));
        }
        else if(view == switch0)
        {
            switch0.toggle();
            ports.setPort(Ports.portC, (char) ((switch0.dataIndex() << 3) | (ports.getPort(Ports.portC) & 0xF7)));
        }
        else
        {
            for(int i=0; i<buttonIds.length; i++)
            {
                if(view == buttons.get(buttonIds[i]))
                {
                    keyRunner.setPressedKeyIndex(i);
                }
            }
        }
    }

    private void tactileFeedback() {
        vibrator.vibrate(20);
    }

    private void deviceReset()
    {
        cpu.reset();
        switch0.setData((char) 0);
        switch1.setData((char) 0);
        switch2.setData((char) 0);
    }

    private void initControls()
    {
        buttons = new HashMap<Integer, Button>();
        for(int id : buttonIds) {
            buttons.put(id, (Button) findViewById(id));
            buttons.get(id).setOnTouchListener(this);
        }
        buttonReset = (Button) findViewById(R.id.buttonReset);
        buttonSettings = (Button) findViewById(R.id.buttonSettings);
        buttonRun = (Button) findViewById(R.id.buttonRun);
        switch0 = (Switch) findViewById(R.id.switchView0);
        switch1 = (Switch) findViewById(R.id.switchView1);
        switch2 = (Switch) findViewById(R.id.switchView2);
        switch3 = (Switch) findViewById(R.id.switchView3);
        buttonReset.setOnTouchListener(this);
        buttonSettings.setOnTouchListener(this);
        switch0.setOnTouchListener(this);
        switch1.setOnTouchListener(this);
        switch2.setOnTouchListener(this);
        switch3.setOnTouchListener(this);
        switch3.setLabels("Ш", "А");
        switch3.setToggleColor(0xFF005000);
        buttonReset.setWidth(buttons.get(R.id.buttonH0).getWidth());
        buttonReset.setHeight(buttons.get(R.id.buttonH0).getHeight());
    }

    private void createDialogs()
    {
        aboutDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_about_title))
                .setMessage(Html.fromHtml(getString(R.string.dialog_about_text)))
                .setCancelable(true)
                .setPositiveButton(getString(R.string.dialog_about_button), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        settingsDialog = new AlertDialog.Builder(this)
                .setTitle(getString(R.string.dialog_settings_title))
                .setCancelable(true)
                .setItems(
                        new CharSequence[]{
                                getString(R.string.dialog_settings_item_help),
                                getString(R.string.dialog_settings_item_about),
                                getString(R.string.dialog_settings_item_quit)
                        },
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        File help = new File(Environment.getExternalStorageDirectory(),"microlab.png");//File path
                                        if (help.exists()) //Checking for the file is exist or not
                                        {
                                            Uri path = Uri.fromFile(help);
                                            Intent objIntent = new Intent(Intent.ACTION_VIEW);
                                            objIntent.setDataAndType(path, "image/png");
                                            objIntent.setFlags(Intent. FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(objIntent);//Staring the pdf viewer
                                        }
                                        break;
                                    case 1:
                                        aboutDialog.show();
                                        break;
                                    case 2:
                                        finish();
                                        break;
                                }
                            }
                        });
    }

    private void runThreads()
    {
        clockThread = new Thread(clock);
        displayThread = new Thread(displayRunner);
        perfThread = new Thread(perfMonitor);
        clockThread.start();
        displayThread.start();
        perfThread.start();
    }

    private void createSystem()
    {
        memory = new Memory(0x8400, 0x600, 0x8000); ///< $8400 - верхний порог ОЗУ, $600 - верхний порог ПЗУ, $8000 - нижний порог ОЗУ
        keyRunner = new KeyRunner();
        audioSpeaker = new AudioSpeaker();
        ports = new Ports(keyRunner, audioSpeaker);
        cpu = new Processor(memory, ports);
        clock = new Clock(cpu);
        displayRunner = new DisplayRunner(this, memory, ports);
        perfMonitor = new Profiler(clock);
    }

    private void initSystem()
    {
        // привязка генератора звука к тактовому генератору
        audioSpeaker.assignClock(clock);

        // привязка ячеек памяти к символам дисплея
        displayRunner.bindMemoryCell((DataView) findViewById(R.id.digit0), 0x83F8);
        displayRunner.bindMemoryCell((DataView) findViewById(R.id.digit1), 0x83F9);
        displayRunner.bindMemoryCell((DataView) findViewById(R.id.digit2), 0x83FA);
        displayRunner.bindMemoryCell((DataView) findViewById(R.id.digit3), 0x83FB);
        displayRunner.bindMemoryCell((DataView) findViewById(R.id.digit4), 0x83FC);
        displayRunner.bindMemoryCell((DataView) findViewById(R.id.digit5), 0x83FD);
        displayRunner.bindMemoryCell((DataView) findViewById(R.id.digit6), 0x83FE);
        displayRunner.bindMemoryCell((DataView) findViewById(R.id.digit7), 0x83FF);

        // привязка порта B к светодиодам
        displayRunner.bindPort((DataView) findViewById(R.id.ledStrip), Ports.portB);

        // загрузка ПЗУ
        memory.load(0, Rom.hexDump);
    }
}
