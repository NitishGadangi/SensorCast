#include <SoftwareSerial.h>
const int ledPin = 13; // Built in LED in Arduino board
String msg;

int bluetoothRx = 2; // PIN 2 to HC05 Tx Pin
int bluetoothTx = 3; // Pin 3 to HC05 Rx Pin
SoftwareSerial bluetooth(bluetoothRx, bluetoothTx);

void setup() {
  // Initialization
  pinMode(ledPin, OUTPUT);
  digitalWrite(ledPin, LOW);
  Serial.begin(9600);
  bluetooth.begin(9600);// Communication rate of the Bluetooth Module
  msg = "";
}

void loop() {

  // To read message received from other Bluetooth Device
  if (bluetooth.available() > 0) { // Check if there is data coming
    msg = bluetooth.readString(); // Read the message as String
    Serial.println("Android Command: " + msg);
    bluetooth.println("Recieved Command: " + msg);
  }

  // Control LED in Arduino board
  if (msg == "0.0") {
    digitalWrite(ledPin, HIGH); // Turn on LED
    Serial.println("LED is turned on\n"); // Then send status message to Serial Monitor
    bluetooth.println("LED is turned on\n"); // Then send status message to Bluetooth
    msg = ""; // reset command
  } else {
    if msg == "5.0") {
      digitalWrite(ledPin, LOW); // Turn off LED
      Serial.println("LED is turned off\n"); // Then send status message to Serial Monitor
      bluetooth.println("LED is turned off\n"); // Then send status message to Bluetooth
      msg = ""; // reset command
    }
  }
}
