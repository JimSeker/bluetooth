package edu.cs4730.controllersimpledemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;

/*
 * A simple demo to show how to get input from a bluetooth controller
 * See https://developer.android.com/training/game-controllers/controller-input.html
 * for a lot more info
*/

public class MainActivity extends AppCompatActivity {

    TextView name, last, logger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = (TextView) findViewById(R.id.Name);
        last = (TextView) findViewById(R.id.lastBtn);
        logger = (TextView) findViewById(R.id.logger);

        getGameControllerIds();

    }


    //getting the "joystick" or dpad motion.
    @Override
    public boolean onGenericMotionEvent(android.view.MotionEvent motionEvent) {
        float xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X);
        float yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y);

        boolean handled = false;
        // Check if the AXIS_HAT_X value is -1 or 1, and set the D-pad
        // LEFT and RIGHT direction accordingly.
        if (Float.compare(xaxis, -1.0f) == 0) {
           // Dpad.LEFT;
            last.setText("Dpad Left");
            handled = true;
        } else if (Float.compare(xaxis, 1.0f) == 0) {
            // Dpad.RIGHT;
            last.setText("Dpad Right");
            handled = true;
        }
        // Check if the AXIS_HAT_Y value is -1 or 1, and set the D-pad
        // UP and DOWN direction accordingly.
        else if (Float.compare(yaxis, -1.0f) == 0) {
            // Dpad.UP;
            last.setText("Dpad Up");
            handled = true;
        } else if (Float.compare(yaxis, 1.0f) == 0) {
            // Dpad.DOWN;
            last.setText("Dpad Down");
            handled = true;
        }
        if (!handled) {
            logger.append("dPad: X " + xaxis + " Y " + yaxis +"\n");
        }
        return handled;
    }

    //getting the buttons.  note, there is down and up action.  this only
    //looks for down actions.
    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
                == InputDevice.SOURCE_GAMEPAD) {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BUTTON_X:
                        last.setText("X Button");
                        break;
                    case KeyEvent.KEYCODE_BUTTON_A:
                        last.setText("A Button");
                        break;
                    case KeyEvent.KEYCODE_BUTTON_Y:
                        last.setText("Y Button");
                        break;
                    case KeyEvent.KEYCODE_BUTTON_B:
                        last.setText("B Button");
                        break;

                }

                logger.append("code is " + event.getKeyCode() + "\n");
            }
            return true;
        }

        return false;
    }

    //From Google's page on controller-input
    public ArrayList getGameControllerIds() {
        ArrayList gameControllerDeviceIds = new ArrayList();
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    || ((sources & InputDevice.SOURCE_JOYSTICK)
                    == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                name.setText(dev.getName() );
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    gameControllerDeviceIds.add(deviceId);
                }
            }
        }
        return gameControllerDeviceIds;
    }

}
