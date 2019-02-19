package org.concord.iot;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;
import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * @author Charles Xie
 */

public class RainbowHat {

    private static final int MINIMUM_SENSOR_DATA_TRANSMISSION_INTERVAL = 1000;

    private GpioController gpio;
    private GpioPinDigitalInput buttonA;
    private GpioPinDigitalInput buttonB;
    private GpioPinDigitalInput buttonC;
    private GpioPinDigitalOutput redLed;
    private GpioPinDigitalOutput greenLed;
    private GpioPinDigitalOutput blueLed;

    private Apa102 apa102;
    private int[] rainbow = new int[RainbowHatState.NUMBER_OF_LEDS_IN_STRIPE];

    private DatabaseReference database;

    public RainbowHat() {
        init();
    }

    private void init() {

        gpio = GpioFactory.getInstance();

        buttonA = gpio.provisionDigitalInputPin(RaspiPin.GPIO_29, "Button A");
        buttonB = gpio.provisionDigitalInputPin(RaspiPin.GPIO_28, "Button B");
        buttonC = gpio.provisionDigitalInputPin(RaspiPin.GPIO_27, "Button C");
        redLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_22, "Red LED", PinState.LOW);
        greenLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_24, "Green LED", PinState.LOW);
        blueLed = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_25, "Blue LED", PinState.LOW);

        GpioPinListenerDigital listener = event -> {
            GpioPin pin = event.getPin();
            PinState pinState = event.getState();
            System.out.println("GPIO Pin state change: " + pin + " = " + pinState);
            switch (pin.getPin().getAddress()) {
                case 29:
                    if (pinState.isHigh()) {
                        redLed.low();
                    } else {
                        redLed.high();
                    }
                    database.child("redLed").setValue(redLed.isHigh(), null);
                    break;
                case 28:
                    if (pinState.isHigh()) {
                        greenLed.low();
                    } else {
                        greenLed.high();
                    }
                    database.child("greenLed").setValue(greenLed.isHigh(), null);
                    break;
                case 27:
                    if (pinState.isHigh()) {
                        blueLed.low();
                    } else {
                        blueLed.high();
                    }
                    database.child("blueLed").setValue(blueLed.isHigh(), null);
                    break;
            }
        };
        buttonA.addListener(listener);
        buttonB.addListener(listener);
        buttonC.addListener(listener);

        apa102 = new Apa102();
        apa102.setColorForAll(Color.RED);

        try {
            FileInputStream serviceAccount = new FileInputStream("raspberry-pi-java-firebase-adminsdk-eeeo1-f7e5dc2054.json");
            FirebaseOptions options = new FirebaseOptions.Builder().setCredentials(GoogleCredentials.fromStream(serviceAccount)).setDatabaseUrl("https://raspberry-pi-java.firebaseio.com").build();
            FirebaseApp.initializeApp(options);
            database = FirebaseDatabase.getInstance().getReference("rainbow_hat");
            // database.setValue(new RainbowHatState(), null);
            database.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) { // This method is called once with the initial value and again whenever data at this location is updated.

                    System.out.println("Value changed: " + dataSnapshot.getValue());
                    RainbowHatState state = dataSnapshot.getValue(RainbowHatState.class);

                    redLed.setState(state.redLed);
                    greenLed.setState(state.greenLed);
                    blueLed.setState(state.blueLed);

                    for (int i = 0; i < rainbow.length; i++) { // the led strip goes from the right to the left (0 is the rightmost and 6 is the leftmost).
                        ArrayList<Integer> rgb = state.ledStripeColors.get(i);
                        rainbow[i] = Util.rgbToInt(rgb.get(0), rgb.get(1), rgb.get(2));
                    }

                }

                @Override
                public void onCancelled(DatabaseError error) {
                    System.out.println("Failed to read value: " + error.toException());
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void destroy() {
        buttonA.removeAllListeners();
        buttonB.removeAllListeners();
        buttonC.removeAllListeners();
        redLed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        greenLed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        blueLed.setShutdownOptions(true, PinState.LOW, PinPullResistance.OFF);
        apa102.turnoff();
        // stop all GPIO activity/threads by shutting down the GPIO controller (this method will forcefully shutdown all GPIO monitoring threads and scheduled tasks)
        gpio.shutdown();
    }

    private void createAndShowGui() {

        final JFrame frame = new JFrame("Rainbow HAT");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocation(100, 100);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                destroy();
                System.exit(0);
            }
        });

        JPanel contentPane = new JPanel(new BorderLayout());
        frame.setContentPane(contentPane);

        JPanel buttonPanel = new JPanel();
        contentPane.add(buttonPanel, BorderLayout.SOUTH);

        JButton button = new JButton("Close");
        button.addActionListener(e -> {
            destroy();
            frame.dispose();
            System.exit(0);
        });
        buttonPanel.add(button);

        frame.pack();
        frame.setVisible(true);

    }

    public static void main(final String[] args) {

        final RainbowHat rainbowHat = new RainbowHat();

        if (GraphicsEnvironment.isHeadless()) {

            Scanner scanner = new Scanner(System.in);
            String line = "";
            while (!"q".equalsIgnoreCase(line)) {
                line = scanner.next();
                System.out.println("You typed: " + line);
            }
            scanner.close();
            rainbowHat.destroy();
            System.exit(0); // call this to exit and avoid a broken pipe error

        } else {
            EventQueue.invokeLater(() -> rainbowHat.createAndShowGui());
        }

    }

}