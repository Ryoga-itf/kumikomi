#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>

#include <wiringPi.h>
#include <wiringPiI2C.h>

#include <fcntl.h>
#include <linux/i2c-dev.h>
#include <sys/ioctl.h>

int main() {
  int dev_fd;
  int data;
  dev_fd = wiringPiI2CSetup(0x48);
  if (dev_fd < 0) {
    printf("Error\n");
    return 0;
  }

  // Start conversation
  wiringPiI2CWrite(dev_fd, 0x51);
  usleep(5000);

  // get temp
  data = wiringPiI2CReadReg16(dev_fd, 0xAA);
  printf("data = 0x%x\n", data);

  // Stop conversation
  wiringPiI2CWrite(dev_fd, 0x22);

  float temp = 0.0;
  if ((data & (1 << 6)) != 0)
    temp += 64;
  if ((data & (1 << 5)) != 0)
    temp += 32;
  if ((data & (1 << 4)) != 0)
    temp += 16;
  if ((data & (1 << 3)) != 0)
    temp += 8;
  if ((data & (1 << 2)) != 0)
    temp += 4;
  if ((data & (1 << 1)) != 0)
    temp += 2;
  if ((data & (1 << 0)) != 0)
    temp += 1;

  if ((data & (1 << 15)) != 0)
    temp += (float)1 / 2;
  if ((data & (1 << 14)) != 0)
    temp += (float)1 / (2 * 2);
  if ((data & (1 << 13)) != 0)
    temp += (float)1 / (2 * 2 * 2);
  if ((data & (1 << 12)) != 0)
    temp += (float)1 / (2 * 2 * 2 * 2);

  if ((data & (1 << 7)) == 0)
    printf("float = %f\n", temp);
  else
    printf("float = %f\n", -temp);

  return 0;
}
