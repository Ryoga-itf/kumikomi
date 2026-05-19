const std = @import("std");
const Io = std.Io;

const c = @import("c");

pub fn main(init: std.process.Init) !void {
    std.debug.print("All your {s} are belong to us.\n", .{"codebase"});

    const arena: std.mem.Allocator = init.arena.allocator();

    const args = try init.minimal.args.toSlice(arena);
    for (args) |arg| {
        std.log.info("arg: {s}", .{arg});
    }

    if (c.wiringPiSetup() == -1) {
        return error.SetupError;
    }

    c.pinMode(12, c.OUTPUT);
    c.digitalWrite(12, 1);

    std.debug.print("OK\n", .{});

    // const io = init.io;
    //
    // Stdout is for the actual output of your application, for example if you
    // are implementing gzip, then only the compressed bytes should be sent to
    // stdout, not any debugging messages.
    // var stdout_buffer: [1024]u8 = undefined;
    // var stdout_file_writer: Io.File.Writer = .init(.stdout(), io, &stdout_buffer);
    // const stdout_writer = &stdout_file_writer.interface;
    //
    // try stdout_writer.flush(); // Don't forget to flush!
}
