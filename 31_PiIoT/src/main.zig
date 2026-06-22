const std = @import("std");
const zap = @import("zap");
const c = @import("c");

const led_pins = [_]c_int{ 12, 13, 14, 11, 10 };

var gpio_mutex: std.Thread.Mutex = .{};

pub fn main() !void {
    if (c.wiringPiSetup() == -1) {
        std.debug.print("Error: setup failed.\n", .{});
        return error.SetupError;
    }
    inline for (led_pins) |pin| {
        c.pinMode(pin, c.OUTPUT);
        c.digitalWrite(pin, c.LOW);
    }

    var listener = zap.HttpListener.init(.{
        .port = 3000,
        .on_request = onRequest,
        .log = true,
    });
    try listener.listen();

    std.debug.print(
        \\Listening on 0.0.0.0:3000
        \\
        \\Examples:
        \\  http://127.0.0.1:3000/on?num=1
        \\  http://127.0.0.1:3000/off?num=1
        \\
    , .{});

    zap.start(.{
        .threads = 2,
        .workers = 1, // mutex
    });
}

fn onRequest(r: zap.Request) !void {
    const path = r.path orelse {
        r.sendBody("error") catch {};
        return;
    };

    const value: c_int = if (std.mem.eql(u8, path, "/on"))
        c.HIGH
    else if (std.mem.eql(u8, path, "/off"))
        c.LOW
    else {
        r.setStatusNumeric(404);
        r.sendBody("error") catch {};
        return;
    };

    const num_text = r.getParamSlice("num") orelse {
        r.setStatusNumeric(400);
        r.sendBody("error") catch {};
        return;
    };

    const led_num = std.fmt.parseInt(usize, num_text, 10) catch {
        r.setStatusNumeric(400);
        r.sendBody("error") catch {};
        return;
    };

    if (led_num >= led_pins.len) {
        r.setStatusNumeric(400);
        r.sendBody("error") catch {};
        return;
    }

    c.digitalWrite(led_pins[led_num], value);

    r.sendBody("ok") catch {};
}
