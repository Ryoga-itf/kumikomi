const std = @import("std");
const c = @import("c");

pub fn main(init: std.process.Init) !void {
    const io = init.io;
    var stdout_file_writer: std.Io.File.Writer = .init(.stdout(), io, &.{});
    const stdout_writer = &stdout_file_writer.interface;

    const dev_fd = c.wiringPiI2CSetup(0x48);
    if (dev_fd < 0) {
        std.debug.print("Error: wiringPiI2CSetup failed\n", .{});
        return error.I2CSetupFailed;
    }

    // Start conversion
    if (c.wiringPiI2CWrite(dev_fd, 0x51) < 0) {
        std.debug.print("Error: I2C start command failed\n", .{});
        return error.I2CWriteFailed;
    }

    try io.sleep(.fromMilliseconds(5), .awake);

    // Get temp
    const raw = c.wiringPiI2CReadReg16(dev_fd, 0xAA);
    if (raw < 0) {
        std.debug.print("Error: I2C read failed\n", .{});
        return error.I2CReadFailed;
    }

    const data: u16 = @intCast(raw & 0xffff);

    try stdout_writer.print("data = 0x{x}\n", .{data});

    // Stop conversion
    if (c.wiringPiI2CWrite(dev_fd, 0x22) < 0) {
        std.debug.print("Warning: I2C stop command failed\n", .{});
    }

    const temp = decodeTemp(data);
    try stdout_writer.print("temp = {d} °C\n", .{temp});

    try stdout_writer.flush();
}

fn decodeTemp(data: u16) f32 {
    // bit 0..6: int
    const integer_part: u8 = @intCast(data & 0x7f);

    var temp: f32 = @floatFromInt(integer_part);

    // bit 15..12: float
    if ((data & (@as(u16, 1) << 15)) != 0) temp += 0.5;
    if ((data & (@as(u16, 1) << 14)) != 0) temp += 0.25;
    if ((data & (@as(u16, 1) << 13)) != 0) temp += 0.125;
    if ((data & (@as(u16, 1) << 12)) != 0) temp += 0.0625;

    // bit 7: sign
    const is_negative = (data & (@as(u16, 1) << 7)) != 0;

    return if (is_negative) -temp else temp;
}
