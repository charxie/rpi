package org.concord.iot;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.IOException;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

/**
 * @author Charles Xie
 */

public class RainbowHat {

    public static void main(final String[] args) {

        final GpioController gpio = GpioFactory.getInstance();
        GpioPinDigitalInput buttonA = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, "Button A", PinPullResistance.PULL_DOWN);
        GpioPinDigitalInput buttonB = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, "Button B", PinPullResistance.PULL_DOWN);
        GpioPinDigitalInput buttonC = gpio.provisionDigitalInputPin(RaspiPin.GPIO_27, "Button C", PinPullResistance.PULL_DOWN);
        GpioPinDigitalOutput redLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, "Red LED", PinState.HIGH);
        GpioPinDigitalOutput greenLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "Green LED", PinState.HIGH);
        GpioPinDigitalOutput blueLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "Blue LED", PinState.HIGH);

        System.out.println("RainbowHat " + redLed.isHigh() + ", " + greenLed.isHigh() + ", " + blueLed.isHigh() + ", " + buttonA.isHigh() + ", " + buttonB.isHigh() + ", " + buttonC.isHigh());
        //redLed.blink(1000);
        //greenLed.blink(1000);
        //blueLed.blink(1000);

        GpioPinListenerDigital listener = new GpioPinListenerDigital() {
            @Override
            public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
                GpioPin pin = event.getPin();
                PinState pinState = event.getState();
                System.out.println(" --> GPIO PIN STATE CHANGE: " + pin + " = " + pinState);
                if (pinState.isLow()) {
                    redLed.low();
                } else {
                    redLed.high();
                }
            }
        };
        buttonA.addListener(listener);
        buttonB.addListener(listener);
        buttonC.addListener(listener);

        try {
            FileInputStream serviceAccount = new FileInputStream("raspberry-pi-java-firebase-adminsdk-eeeo1-f7e5dc2054.json");
            FirebaseOptions options = new FirebaseOptions.Builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://raspberry-pi-java.firebaseio.com")
                    .build();
            FirebaseApp.initializeApp(options);
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference ref = database.getReference("Name");
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) { // This method is called once with the initial value and again whenever data at this location is updated.
                    System.out.println("Value changed: " + dataSnapshot.getValue());
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("Failed to read value: " + error.toException());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
