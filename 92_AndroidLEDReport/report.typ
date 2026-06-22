#import "@preview/tenv:0.1.2": parse_dotenv
#import "@preview/codelst:2.0.2": sourcecode

#let env = parse_dotenv(read(".env"))
#let textL = 1.8em
#let textM = 1.6em
#let fontSerif = ("Noto Serif", "Noto Serif CJK JP")
#let fontSan = ("Noto Sans", "Noto Sans CJK JP")

#let title = "組込みプログラム開発\nAndroidタブレット・スマホからLED点灯IoTレポート"
#let date = "2026 年 6 月 22 日"
#let authors = (
  (name: env.STUDENT_NAME, email: "学籍番号：" + env.STUDENT_ID, affiliation: "所属：情報科学類"),
)

#set document(author: authors.map(a => a.name), title: title)

#set page(numbering: "1", number-align: center)
#set text(font: fontSerif, lang: "ja")

#show heading: set text(font: fontSan, weight: "medium", lang: "ja")
#show heading.where(level: 1): it => pad(top: 1em, bottom: 0.4em, it)
#show heading.where(level: 2): it => pad(top: 1em, bottom: 0.4em, it)
#show heading.where(level: 3): it => pad(top: 1em, bottom: 0.4em, it)

// Figure
#show figure: it => pad(y: 1em, it)
#show figure.caption: it => pad(top: 0.6em, it)
#show figure.caption: it => text(size: 0.8em, it)

// Title row.
#align(center)[
  #block(text(textL, weight: 700, title))
  #v(1em, weak: true)
  #date
]

// Author information.
#pad(
  top: 0.5em,
  bottom: 0.5em,
  x: 2em,
  grid(
    columns: (1fr,) * calc.min(3, authors.len()),
    gutter: 1em,
    ..authors.map(author => align(center)[
      *#author.name* \
      #author.email \
      #author.affiliation
    ]),
  ),
)

#set par(justify: true)

#set footnote(numbering: sym.dagger + "1")
#set math.mat(delim: "[", gap: 0.7em)


#grid(
  columns: (0.7cm, 1fr, 0.7cm),
  [],
  [
    #set par(first-line-indent: 0em)
    *概要 --* #h(0.5em)
    本レポートでは、Android端末からネットワークを介してLEDの点灯状態を操作するIoTアプリケーションについて述べる。本システムでは、Androidアプリ上に配置したトグルボタンを操作することで、Raspberry Piに接続された複数のLEDを個別に点灯または消灯できる。アプリケーションの構成、Android端末とRaspberry Piの通信方法、および実機を用いた動作確認の結果を示す。
  ],
  [],
)

= システムの概要

本システムは、Android端末からHTTP通信を用いて、Raspberry Piに接続された5個のLEDを遠隔操作するIoTアプリケーションである。
Androidアプリの画面には、LED0からLED4までの各LEDに対応するトグルボタンが配置される。
ボタンを操作すると、`OnCheckedChangeListener` によって状態変化を検出し、LED 番号を表す `num` と、点灯状態を表す `stat` を含むHTTP GETリクエストをRaspberry Piへ送信する。

Raspberry Piでは、Apache2 Webサーバがリクエストを受信し、指定されたPHPプログラムを実行する。
PHPプログラムはGETパラメータからLED番号と点灯状態を取得し、SSH2を用いてRaspberry Pi自身へ接続した後、LED制御用のCプログラムを実行する。
CプログラムではwiringPiを利用し、指定されたLEDに対応するGPIOピンを出力に設定して、`digitalWrite`により点灯または消灯する。
これにより、Android端末からネットワークを介して各LEDを個別に操作できる。

= 動作確認した証拠の画像

#grid(
  columns: 2,
  gutter: 2em,
  [#figure(
    image("off.jpg"),
    caption: [動作確認の様子（すべての LED がオフの状態）]
  ) <fig1>],
  [#figure(
    image("on.jpg"),
    caption: [動作確認の様子（すべての LED がオンの状態）]
  ) <fig2>],
)

#figure(
  image("photo.jpg", width: 50%),
  caption: [動作確認の様子]
) <fig3>

動作確認の様子を示す。
@fig1 はアプリを起動した直後の様子であり、すべての LED がオフであることが確認できる。
@fig2 はアプリを操作し、すべての LED を点灯した様子である。@fig3 は @fig2 を横から撮影したものである。

= システムの発展

テキストに記載されているシステムでは、Raspberry Pi上のApache2がHTTPリクエストを受け取り、PHPプログラムからSSHを用いてRaspberry Pi自身へ接続し、LED制御用のCプログラムを実行していた。
しかし、この構成ではApache2、PHP、SSH、外部コマンドという複数の要素を経由するため、処理経路が複雑である。
また、SSHのユーザ名とパスワードをPHPソースコード内に記述していることや、HTTP GETパラメータを文字列として外部コマンドへ渡していることから、安全性にも問題がある。

そこで発展として、ZigとHTTPサーバライブラリZapを用いた、LED制御専用のWebサーバを実装した。
新しいプログラムはRaspberry Pi上の3000番ポートでHTTPリクエストを待ち受け、`/on?num=1` または `/off?num=1` のような要求を受信する。
URLのパスから点灯または消灯を判定し、`num` パラメータを整数として解析した後、LED番号が0以上5未満であることを確認する。
入力が不正な場合はHTTPステータス400または404を返し、正しい場合のみ wiringPi の `digitalWrite` を直接呼び出してGPIOを制御する。

この変更により、PHP、SSH、パスワード、sudoを伴う外部コマンド実行が不要となり、Androidアプリから受信した値をZigプログラム内で検証してからGPIOを操作できるようになった。
また、文字列をシェルコマンドとして実行しないため、OSコマンドインジェクションの危険性を低減できる。
システム構成も「Androidアプリ、Zig製HTTPサーバ、wiringPi、GPIO、LED」という単純なものになり、処理の流れを把握しやすくなった。

ビルドにはZigのクロスコンパイル機能を利用し、開発環境上でRaspberry Pi向けの32ビットARM Linuxのシングルバイナリを生成した。
ターゲットにはarm-linux-gnueabihfを指定し、Raspberry Pi上で動作する実行ファイルを作成している。
Raspberry Piには SCP コマンドを用いて実行バイナリを転送している。

また、GPIO制御に使用するwiringPiについては、Raspberry Piにインストールされた共有ライブラリを動的に参照するのではなく、wiringPiのCソースコードをZigのビルド処理に組み込んだ。
build.zigではwiringPiを静的ライブラリとしてコンパイルし、生成されるZigの実行ファイルへ静的リンクしている。
これにより、実行環境に特定のwiringPi共有ライブラリが存在することへの依存を減らし、使用するwiringPiの実装をプロジェクト側で固定できる。

ZigからwiringPiを利用するため、CヘッダはTranslateCによってZigのモジュールへ変換している。
これにより、既存のCライブラリであるwiringPiを再利用しながら、HTTPサーバ、入力値の解析、エラー処理などの主要部分をZigで実装した。
さらに、最適化モードにはReleaseSmallを指定し、組込み機器上での利用を意識して実行ファイルのサイズを抑えている。

以下に `main.zig` およびビルドに用いた `build.zig` を示す。

#sourcecode[```zig
const std = @import("std");
const zap = @import("zap");
const c = @import("c");

const led_pins = [_]c_int{ 12, 13, 14, 11, 10 };

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
```]

#sourcecode[```zig
const std = @import("std");

pub fn build(b: *std.Build) void {
    const target = b.resolveTargetQuery(.{
        .cpu_arch = .arm,
        .os_tag = .linux,
        .abi = .gnueabihf,
    });
    const optimize = b.standardOptimizeOption(.{ .preferred_optimize_mode = .ReleaseSmall });

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
```]
