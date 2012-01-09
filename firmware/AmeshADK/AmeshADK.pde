#include <Max3421e.h>
#include <Usb.h>
#include <AndroidAccessory.h>

#define COMMAND_WEATHER 1
#define EVENT_WEATHER_RAIN  1
#define EVENT_WEATHER_SUNNY 2
#define DOUT_RAIN_SIGNAL 9
#define DOUT_SUNNY_SIGNAL 10


AndroidAccessory acc("kopanitsa",
"AmeshLightADK",
"DemoKit Arduino Board",
"1.0",
"http://www.android.com",
"0000000012345678");


void setup();
void loop();

byte msg[3];

void setup()
{
  Serial.begin(9600);
  Serial.print("\r\nStart");
  analogWrite(DOUT_RAIN_SIGNAL, LOW);
  analogWrite(DOUT_SUNNY_SIGNAL, LOW);
  acc.powerOn();
}

void loop()
{
  if (acc.isConnected()) {
      byte msg[3];
      int len = acc.read(msg, sizeof(msg), 1);
      char c0;
      if (len > 0) {
        byte command = msg[0];
        byte target = msg[1];
        byte event = msg[2];
        Serial.print("\r\nComming event. Command:");
        Serial.print(command, DEC);
        Serial.print(" target:");
        Serial.print(target, DEC);
        Serial.print(" event:");
        Serial.print(event, DEC);
        handleLight(command, target, event);
      }
  } else {
    Serial.print("\r\nNot Connected...");
    delay(500);
  }

  delay(100);
}

void handleLight(byte command, byte target, byte event){
  if (command == COMMAND_WEATHER) {
    if (event == EVENT_WEATHER_RAIN) {
      Serial.print("\r\nRAIN!");
      digitalWrite(DOUT_RAIN_SIGNAL, HIGH);
      digitalWrite(DOUT_SUNNY_SIGNAL, LOW);
    } else if (event == EVENT_WEATHER_SUNNY) {
      Serial.print("\r\nSUNNY!");
      digitalWrite(DOUT_RAIN_SIGNAL, LOW);
      digitalWrite(DOUT_SUNNY_SIGNAL, HIGH);
    }
  }
}

