package edu.cs4730.controllersimpledemo;

import android.annotation.SuppressLint;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple demo to show how to get input from a bluetooth controller
 * See https://developer.android.com/training/game-controllers/controller-input.html
 * for a lot more info
 */
@SuppressLint("SetTextI18n")
public class MainActivity extends AppCompatActivity {

    TextView name, last, logger;

    Boolean isJoyStick = false, isGamePad = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.Name);
        last = findViewById(R.id.lastBtn);
        logger = findViewById(R.id.logger);

        // getGameControllerIds();

    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        getGameControllerIds();
    }

    //getting the "joystick" or dpad motion.

    @Override
    public boolean onGenericMotionEvent(android.view.MotionEvent motionEvent) {
        float xaxis = 0.0f, yaxis = 0.0f;
        boolean handled = false;

        //if both are true, this code will show both JoyStick and dpad.  Which one you want to use is
        // up to you
        if (isJoyStick) {
            xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_X);
            yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_Y);

            last.setText("JoyStick");
            logger.append("JoyStick: X " + xaxis + " Y " + yaxis + "\n");
            handled = true;
        }

        if (isGamePad) {
            xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X);
            yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y);

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
            } else if ((Float.compare(xaxis, 0.0f) == 0)
                && (Float.compare(yaxis, 0.0f) == 0)) {
                //Dpad.center
                last.setText("Dpad centered");
                handled = true;
            }
            if (!handled) {
                last.setText("Unknown");
                logger.append("unhandled: X " + xaxis + " Y " + yaxis + "\n");
            }

        }
        return handled;
    }

    //getting the buttons.  note, there is down and up action.  this only
    //looks for down actions.
    @Override
    public boolean dispatchKeyEvent(android.view.KeyEvent event) {
        boolean handled = false;
        if ((event.getSource() & InputDevice.SOURCE_GAMEPAD)
            == InputDevice.SOURCE_GAMEPAD) {

            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_BUTTON_X:
                        last.setText("X Button");
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_A:
                        last.setText("A Button");
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_Y:
                        last.setText("Y Button");
                        handled = true;
                        break;
                    case KeyEvent.KEYCODE_BUTTON_B:
                        last.setText("B Button");
                        handled = true;
                        break;
                }
                if (!handled)
                    logger.append("code is " + event.getKeyCode() + "\n");
            } else if (event.getAction() == KeyEvent.ACTION_UP) {
                //don't care, but need to handle it.
                handled = true;
            } else {
                logger.append("unknown action " + event.getAction());
            }
            return handled;
        }

        return handled;
    }

    //From Google's page on controller-input
    public ArrayList<Integer> getGameControllerIds() {
        ArrayList<Integer> gameControllerDeviceIds = new ArrayList<Integer>();
        int[] deviceIds = InputDevice.getDeviceIds();
        for (int deviceId : deviceIds) {
            InputDevice dev = InputDevice.getDevice(deviceId);
            int sources = dev.getSources();

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                || ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                name.setText(dev.getName());
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    gameControllerDeviceIds.add(deviceId);
                }
                //possible both maybe true.
                if ((sources & InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD)
                    isGamePad = true;
                if ((sources & InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)
                    isJoyStick = true;
                logger.append("GamePad: " + isGamePad + "\n");
                logger.append("JoyStick: " + isJoyStick + "\n");
            }

        }
        return gameControllerDeviceIds;
    }

}
