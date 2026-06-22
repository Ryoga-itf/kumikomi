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
