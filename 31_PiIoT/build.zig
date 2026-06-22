const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.resolveTargetQuery(.{
        .cpu_arch = .arm,
        .os_tag = .linux,
        .abi = .gnueabihf,
    });
    const optimize = b.standardOptimizeOption(.{ .preferred_optimize_mode = .ReleaseSmall });

    const wiringpi_src = b.path("../00_ThirdParty/WiringPi/wiringPi/");
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

    const zap = b.dependency("zap", .{
        .target = target,
        .optimize = optimize,
        .openssl = false, // set to true to enable TLS support
    });

    const kumikomi_iot_exe = b.addExecutable(.{
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
    kumikomi_iot_exe.root_module.linkLibrary(wiringpi);
    kumikomi_iot_exe.root_module.addImport("zap", zap.module("zap"));
    b.installArtifact(kumikomi_iot_exe);
}
