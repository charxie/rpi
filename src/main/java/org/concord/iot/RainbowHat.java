package org.concord.iot;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import javax.swing.*;

/**
 * @author Charles Xie
 */

public class RainbowHat {

    private GpioController gpio;
    private GpioPinDigitalInput buttonA;
    private GpioPinDigitalInput buttonB;
    private GpioPinDigitalInput buttonC;
    private GpioPinDigitalOutput redLed;
    private GpioPinDigitalOutput greenLed;
    private GpioPinDigitalOutput blueLed;

    public RainbowHat() {
        init();
    }

    private void init() {

        gpio = GpioFactory.getInstance();

        buttonA = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, "Button A", PinPullResistance.PULL_DOWN);
        buttonB = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, "Button B", PinPullResistance.PULL_DOWN);
        buttonC = gpio.provisionDigitalInputPin(RaspiPin.GPIO_27, "Button C", PinPullResistance.PULL_DOWN);
        redLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, "Red LED", PinState.LOW);
        greenLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "Green LED", PinState.LOW);
        blueLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "Blue LED", PinState.LOW);

        System.out.println("Initial state: red LED = " + redLed.isHigh() + ", green LED = " + greenLed.isHigh() + ", blue LED = " + blueLed.isHigh() + ", button A = " + buttonA.isHigh() + ", button B = " + buttonB.isHigh() + ", button C = " + buttonC.isHigh());

        GpioPinListenerDigital listener = event -> {
            GpioPin pin = event.getPin();
            PinState pinState = event.getState();
            System.out.println("GPIO Pin state change: " + pin + " = " + pinState);
            if (pinState.isHigh()) {
                redLed.low();
            } else {
                redLed.high();
            }
        };
        buttonA.addListener(listener);
        buttonB.addListener(listener);
        buttonC.addListener(listener);

        try {
            FileInputStream serviceAccount = new FileInputStream("raspberry-pi-java-firebase-adminsdk-eeeo1-f7e5dc2054.json");
            FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl("https://raspberry-pi-java.firebaseio.com").build();
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

        //redLed.blink(1000);
        //greenLed.blink(1000);
        //blueLed.blink(1000);

    }

    private void destroy() {
        buttonA.removeAllListeners();
        buttonB.removeAllListeners();
        buttonC.removeAllListeners();
    }

    private static void createAndShowGui() {

        final RainbowHat rainbowHat = new RainbowHat();

        final JFrame frame = new JFrame("Rainbow HAT");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(100, 100);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                rainbowHat.destroy();
                System.exit(0);
            }
        });

        JPanel contentPane = new JPanel(new BorderLayout());
        frame.setContentPane(contentPane);

        JPanel buttonPanel = new JPanel();
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        JButton button = new JButton("Close");
        button.addActionListener(e -> {
            rainbowHat.destroy();
            frame.dispose();
            System.exit(0);
        });
        buttonPanel.add(button);

        frame.pack();
        frame.setVisible(true);

    }

    public static void main(final String[] args) {
        EventQueue.invokeLater(() -> createAndShowGui());
    }

}