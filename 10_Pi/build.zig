const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.resolveTargetQuery(.{
        .cpu_arch = .arm,
        .os_tag = .linux,
        .abi = .gnueabihf,
    });
    const optimize = b.standardOptimizeOption(.{});

    const wiringpi_src = b.path("WiringPi/wiringPi/");
    const wiringpi = b.addLibrary(.{
        .name = "wiringPi",
        .linkage = .static,
        .root_module = b.createModule(.{
            .target = target,
            .optimize = optimize,
            .link_libc = true,
        }),
    });
    wiringpi.root_module.addIncludePath(wiringpi_src);
    wiringpi.root_module.addCSourceFiles(.{
        .root = wiringpi_src,
        .files = &.{
            "wiringPi.c",
            "wiringSerial.c",
            "wiringShift.c",
            "piHiPri.c",
            "piThread.c",
            "wiringPiSPI.c",
            "wiringPiI2C.c",
            "softPwm.c",
            "softTone.c",
            "mcp23008.c",
            "mcp23016.c",
            "mcp23017.c",
            "mcp23s08.c",
            "mcp23s17.c",
            "sr595.c",
            "pcf8574.c",
            "pcf8591.c",
            "mcp3002.c",
            "mcp3004.c",
            "mcp4802.c",
            "mcp3422.c",
            "max31855.c",
            "max5322.c",
            "ads1115.c",
            "sn3218.c",
            "bmp180.c",
            "htu21d.c",
            "ds18b20.c",
            "rht03.c",
            "drcSerial.c",
            "pseudoPins.c",
            "wpiExtensions.c",
            "wiringPiLegacy.c",
        },
        .flags = &.{
            "-D_GNU_SOURCE",
        },
    });

    const c = b.addTranslateC(.{
        .root_source_file = b.path("src/c.h"),
        .target = target,
        .optimize = optimize,
    });
    c.addIncludePath(wiringpi_src);

    const exe = b.addExecutable(.{
        .name = "kumikomi",
        .root_module = b.createModule(.{
            .root_source_file = b.path("src/main.zig"),
            .target = target,
            .optimize = optimize,
            .link_libc = true,
            .imports = &.{
                .{
                    .name = "c",
                    .module = c.createModule(),
                },
            },
        }),
    });

    exe.root_module.linkLibrary(wiringpi);

    b.installArtifact(exe);
}
