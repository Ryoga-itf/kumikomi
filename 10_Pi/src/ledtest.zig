const std = @import("std");
const c = @import("c");

pub fn main(init: std.process.Init) !void {
    const arena: std.mem.Allocator = init.arena.allocator();

    const args = try init.minimal.args.toSlice(arena);

    if (args.len < 3) {
        std.debug.print("Usage: {s} LED_Number ON(1)/OFF(0)\n", .{args[0]});
        return error.ArgMissing;
    }

    const led_num = try std.fmt.parseInt(usize, args[1], 10);
    const on_off = try std.fmt.parseInt(u1, args[2], 10);

    if (led_num > 4) {
        std.debug.print("Error: LED_Number must be specified from 0 to 4.\n", .{});
        return error.ArgFormatError;
    }

    if (c.wiringPiSetup() == -1) {
        return error.SetupError;
    }

    const led_pin = ([_]c_int{ 12, 13, 14, 11, 10 })[led_num];

    c.pinMode(led_pin, c.OUTPUT);
    c.digitalWrite(led_pin, on_off);
}
