package edu.cs4730.controllersimpledemo_kt

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.InputDevice
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import edu.cs4730.controllersimpledemo_kt.databinding.ActivityMainBinding


@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var isJoyStick: Boolean = false
    private var isGamePad: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        // getGameControllerIds();
    }

    public override fun onResume() {
        super.onResume() // Always call the superclass method first
        getGameControllerIds()
    }


    //getting the "joystick" or dpad motion.

    override fun onGenericMotionEvent(motionEvent: MotionEvent): Boolean {
        var xaxis = 0.0f
        var yaxis = 0.0f
        var handled = false

        //if both are true, this code will show both JoyStick and dpad.  Which one you want to use is
        // up to you
        if (isJoyStick) {
            xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_X)
            yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_Y)

            binding.lastBtn.text = "JoyStick"
            binding.logger.append("JoyStick: X $xaxis Y $yaxis\n")
            handled = true
        }

        if (isGamePad) {
            xaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_X)
            yaxis = motionEvent.getAxisValue(MotionEvent.AXIS_HAT_Y)

            // Check if the AXIS_HAT_X value is -1 or 1, and set the D-pad
            // LEFT and RIGHT direction accordingly.
            if (xaxis.compareTo(-1.0f) == 0) {
                // Dpad.LEFT;
                binding.lastBtn.text = "Dpad Left"
                handled = true
            } else if (xaxis.compareTo(1.0f) == 0) {
                // Dpad.RIGHT;
                binding.lastBtn.text = "Dpad Right"
                handled = true
            } else if (yaxis.compareTo(-1.0f) == 0) {
                // Dpad.UP;
                binding.lastBtn.text = "Dpad Up"
                handled = true
            } else if (yaxis.compareTo(1.0f) == 0) {
                // Dpad.DOWN;
                binding.lastBtn.text = "Dpad Down"
                handled = true
            } else if ((xaxis.compareTo(0.0f) == 0) && (yaxis.compareTo(0.0f) == 0)) {
                //Dpad.center
                binding.lastBtn.text = "Dpad centered"
                handled = true
            }
            if (!handled) {
                binding.lastBtn.text = "Unknown"
                binding.logger.append("unhandled: X $xaxis Y $yaxis\n")
            }
        }
        return handled
    }

    //getting the buttons.  note, there is down and up action.  this only
    //looks for down actions.
    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        var handled = false
        if ((event.source and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) {
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (event.keyCode) {
                    KeyEvent.KEYCODE_BUTTON_X -> {
                        binding.lastBtn.text = "X Button"
                        handled = true
                    }

                    KeyEvent.KEYCODE_BUTTON_A -> {
                        binding.lastBtn.text = "A Button"
                        handled = true
                    }

                    KeyEvent.KEYCODE_BUTTON_Y -> {
                        binding.lastBtn.text = "Y Button"
                        handled = true
                    }

                    KeyEvent.KEYCODE_BUTTON_B -> {
                        binding.lastBtn.text = "B Button"
                        handled = true
                    }
                }
                if (!handled) binding.logger.append("code is " + event.keyCode + "\n")
            } else if (event.action == KeyEvent.ACTION_UP) {
                //don't care, but need to handle it.
                handled = true
            } else {
                binding.logger.append("unknown action " + event.action)
            }
        }
        return handled
    }

    //From Google's page on controller-input
    private fun getGameControllerIds(): ArrayList<Int> {
        val gameControllerDeviceIds = ArrayList<Int>()
        val deviceIds = InputDevice.getDeviceIds()
        for (deviceId in deviceIds) {
            val dev = InputDevice.getDevice(deviceId)
            val sources = dev!!.sources

            // Verify that the device has gamepad buttons, control sticks, or both.
            if (((sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) || ((sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK)) {
                // This device is a game controller. Store its device ID.
                binding.Name.text = dev.name
                if (!gameControllerDeviceIds.contains(deviceId)) {
                    gameControllerDeviceIds.add(deviceId)
                }
                //possible both maybe true.
                if ((sources and InputDevice.SOURCE_GAMEPAD) == InputDevice.SOURCE_GAMEPAD) isGamePad =
                    true
                if ((sources and InputDevice.SOURCE_JOYSTICK) == InputDevice.SOURCE_JOYSTICK) isJoyStick =
                    true
                binding.logger.append("GamePad: $isGamePad\n")
                binding.logger.append("JoyStick: $isJoyStick\n")
            }
        }
        return gameControllerDeviceIds
    }

}