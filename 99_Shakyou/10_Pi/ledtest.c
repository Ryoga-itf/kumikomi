#include <stdio.h>
#include <stdlib.h>

#include <wiringPi.h>

int LED_PIN[] = {12, 13, 14, 11, 10};

int main(int argc, char *argv[]) {
  if (argc < 3) {
    printf("Usage: %s LED_Number ON(1)/OFF(0)\n", argv[0]);
    return -1;
  }

  int led_num = atoi(argv[1]);
  int on_off = atoi(argv[2]);

  if (led_num > 4) {
    printf("Error: LED_Number must be specified from 0 to 4.\n");
    return -1;
  }

  if (!(on_off == 0 || on_off == 1)) {
    printf("Error: ON/OFF must be specified to 0 or 1.\n");
    return -1;
  }

  if (wiringPiSetup() == -1) {
    printf("Error: setup failed.\n");
    return -1;
  }

  pinMode(LED_PIN[led_num], OUTPUT);
  digitalWrite(LED_PIN[led_num], on_off);

  return 0;
}
