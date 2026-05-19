const std = @import("std");
const c = @import("c");

pub fn main(init: std.process.Init) !void {
    const arena: std.mem.Allocator = init.arena.allocator();

    const args = try init.minimal.args.toSlice(arena);

    if (args.len < 2) {
        std.debug.print("Usage: {s} SW_Number\n", .{args[0]});
        return error.ArgMissing;
    }

    const sw_num = try std.fmt.parseInt(usize, args[1], 10);

    if (sw_num > 1) {
        std.debug.print("Error: SW_Number must be specified from 0 to 1.\n", .{});
        return error.ArgFormatError;
    }

    if (c.wiringPiSetup() == -1) {
        std.debug.print("Error: setup failed.\n", .{});
        return error.SetupError;
    }

    const sw_pin = ([_]c_int{ 0, 7 })[sw_num];

    c.pinMode(sw_pin, c.INPUT);
    const sw_val = c.digitalRead(sw_pin);

    const io = init.io;
    var stdout_file_writer: std.Io.File.Writer = .init(.stdout(), io, &.{});
    const stdout_writer = &stdout_file_writer.interface;

    try stdout_writer.print("{d}\n", .{sw_val});

    try stdout_writer.flush();
}
