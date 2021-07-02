#include <SoftwareSerial.h>

String msg;

int bluetoothRx = 2;
int bluetoothTx = 3;
// declare the pins here,
// which you are looking forward to use based on your application

SoftwareSerial bluetooth(bluetoothRx, bluetoothTx);

void setup() {
  // Do Pin Initialization here
  
  // Communication rate for Serial Monitor
  Serial.begin(9600);
  // Communication rate of the Bluetooth Module
  bluetooth.begin(9600);
  msg = "";
}

void loop() {

  // To read message received from the Android
  if (bluetooth.available() > 0) { // Check if there is data coming
    msg = bluetooth.readString(); // Read the message as String
    //printing message recieved to Serial Monitor
    Serial.println("Android Command: " + msg);
    // sending recieved msg back to Android device as a confirmation
    bluetooth.println("Recieved : " + msg);
  }

  // Do something with the recieved msg here.
  
}
