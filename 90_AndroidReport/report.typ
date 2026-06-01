#import "@preview/tenv:0.1.2": parse_dotenv
#import "@preview/codelst:2.0.2": sourcecode

#let env = parse_dotenv(read(".env"))
#let textL = 1.8em
#let textM = 1.6em
#let fontSerif = ("Noto Serif", "Noto Serif CJK JP")
#let fontSan = ("Noto Sans", "Noto Sans CJK JP")

#let title = "組込みプログラム開発Android アプリレポート"
#let date = "2026 年 6 月 1 日"
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
    *概要 --* #h(0.5em) 本レポートでは、Android 端末のセンサを用いた料理練習アプリ「Napori」の開発について述べる。Napori は、スマートフォンをピザ生地に見立て、手のひらの上で回転させることでピザ回しを疑似的に練習するアプリである。ジャイロセンサおよび方位センサを用いて端末の回転や傾きを取得し、その動作に応じて画面上のピザ画像を回転させ、スコアを算出する。さらに、ゲームとして楽しめるようにスコア表示やベストスコア表示を行うことで、ユーザが継続的に練習できる体験を目指した。
  ],
  [],
)

= 背景

まず、本アプリを開発した背景には、Android アプリ開発課題において「センサまたはカメラを用いたアプリ」を作成する必要があったことがある。
課題では、単にセンサ値を表示するだけでなく、人間の動きによって得られる情報を活用し、利用シーンを想定したアプリを作ることが求められていた。
そこで本開発では、センサの値をゲーム性のある動作へ変換し、ユーザが身体を動かしながら楽しめるアプリを目指した。

また、アプリのコンセプトとして、あえて実用性とユーモアの境界にある題材を選ぶことにした。
スマートフォンは薄く、手のひらの上で扱うことができ、落とすと大きな問題になるという点で、ピザ生地と奇妙な共通点を持つ。
そこで、スマートフォンをピザ生地に見立てて回すことで、ピザ回しを練習できるのではないかと考えた。

さらに、ピザ産業に関する社会的背景も存在する。
日本のピザ市場は大きく、2023 年度には 3,237.5 億円規模であったとされる。#footnote[ピザ協議会, 「2023年度ピザマーケットは3,237.5億円　調査史上2番目の市場規模　30年前の1.8倍」, https://pizzakyogikai.gr.jp/2023-japan-pizza-market]
一方で、ピザ店の登録件数は減少傾向にあり、宅配ピザ店の倒産も増加している。#footnote[タウンページデータベース, 業種分類「ピザハウス」の登録件数.] #footnote[東京商工リサーチ, 「宅配ピザ店の倒産が倍増で過去最多　原材料費の高騰や深刻な人手不足が影響」, https://www.tsr-net.co.jp/data/detail/1198239_1527.html]
飲食業全体では人手不足が問題となっており、調理人材の不足も指摘されている。#footnote[内閣官房, 新しい資本主義実現会議資料, https://www.cas.go.jp/jp/seisaku/atarashii_sihonsyugi/shouryokukatousi/01.pdf]
このような状況において、ピザ作りを気軽に体験・練習できる機会を増やすことは、将来的な調理技能への関心を高める一助となる可能性がある。

以上より、本アプリでは「スマートフォンでピザ回しを練習する」という一見奇妙な体験を通して、センサを活用した身体的なインタラクションを実現することを目的とした。

= アプリの設計

== システム概要

Napori は、Android 端末に搭載されたジャイロセンサと方位センサを利用する。
ジャイロセンサからは端末の角速度を取得し、スマートフォンがどの程度回転しているかを推定する。また、方位センサまたは回転ベクトルセンサからは端末の姿勢を取得し、端末が水平に近い状態で保たれているかを判定する。

アプリ全体は、主に以下の要素から構成される。

- センサ値取得部
- ゲームロジック部
- UI 表示部
- スコア管理部

センサ値取得部では、Android の `SensorManager` を用いてセンサイベントを受け取る。
ゲームロジック部では、取得した角速度や傾きからスコア、コンボ、失敗判定などを計算する。
UI 表示部では、Jetpack Compose を用いてピザ画像、スコア、回転速度、傾き、ゲーム状態を表示する。
スコア管理部では、今回のスコアやベストスコアを管理する。

== アプリの動作

アプリを起動すると、タイトル画面が表示される。
ユーザが Start ボタンを押すとゲームが開始される。
ゲーム中、ユーザはスマートフォンを手のひらの上に置き、ピザ生地を回すように端末を回転させる。

端末が適切に回転している場合、画面上のピザ画像も回転し、スコアが加算される。
回転が速く、かつ端末の傾きが小さいほど、安定してピザを回せているとみなし、高いスコアが得られる。
一方で、端末が大きく傾いた場合や、回転が不安定になった場合には、コンボが途切れる、またはスコアが伸びにくくなる。

この設計により、単に端末を振るだけでは高得点が取れず、ピザ生地を水平に保ちながら回すような動作が必要になる。
すなわち、実際のピザ回しに近い「安定した回転」をゲーム内の評価指標として扱うことができる。

== UI 設計

UI は、スマートフォン画面上に大きくピザ画像を表示する構成とした。
ユーザは端末を動かしながら画面を見るため、細かい情報を多く表示するのではなく、現在のスコア、残り時間、回転速度、傾きなど、ゲームに必要な情報を簡潔に表示することを重視した。

ピザ画像はセンサ値に応じて回転するため、ユーザは自分の動作がアプリに反映されていることを直感的に理解できる。

= 実装

== 使用技術

本アプリは Kotlin と Jetpack Compose を用いて実装した。
Jetpack Compose を用いることで、UI の状態を Kotlin の状態管理と自然に対応付けることができ、センサ値によってリアルタイムに変化する画面を比較的簡潔に記述できる。

そのため、ソースリスト（Java ソースと View の id 対応）は含まれていない。（Kotlin コードにその情報がそのまま書かれている）

また、ゲームロジックは UI から分離して実装した。これにより、センサ値を入力としてスコアやゲーム状態を計算する処理を単体テストしやすくした。
センサを使うアプリでは、実機上でなければ確認しにくい処理が多いが、ゲームロジックを純粋な Kotlin のクラスとして分離することで、実機に依存しない検証が可能になる。

== センサ値の表現

センサから取得した値は、そのまま UI に渡すのではなく、ゲームロジックで扱いやすい形式に変換する。
以下に、センサ値を表すデータクラスの例を示す。

#sourcecode[```kotlin
data class SensorFrame(
    val timestampNanos: Long, 
    val gyroZ: Float,
    val pitch: Float,
    val roll: Float
)
```]

ここで、`gyroZ` は端末の Z 軸まわりの角速度を表す。
ピザ回しでは、スマートフォンを画面に対して垂直な軸まわりに回転させる動作が中心となるため、この値を主に利用する。
また、`pitch` と `roll` は端末の傾きを表し、端末が水平に保たれているかを判定するために用いる。

== ゲーム状態の管理

ゲーム状態は、スコア、コンボ、経過時間、ピザ画像の回転角などを持つデータクラスとして管理した。

#sourcecode[```kotlin
data class GameState(
    val score: Int = 0,
    val combo: Int = 0,
    val bestScore: Int = 0,
    val pizzaRotationDeg: Float = 0f,
    val spinDegPerSec: Float = 0f,
    val tiltDeg: Float = 0f,
    val message: String = "スマホを水平に保って回してください"
)
```]

このように状態を 1 つのデータクラスにまとめることで、Jetpack Compose の UI と接続しやすくなる。
UI は `GameState` の値に応じて再描画されるため、センサ値の更新に対して画面が自動的に変化する。

== ゲームロジック

ゲームロジックでは、センサ値から回転速度と傾きを計算し、スコアを更新する。
以下に主要部分の例を示す。

#sourcecode[```kotlin
class PizzaSpinEngine {
    private var lastTimestampNanos: Long? = null
    private var state = GameState()

    fun update(frame: SensorFrame): GameState {
        val last = lastTimestampNanos
        lastTimestampNanos = frame.timestampNanos

        if (last == null) {
            return state
        }

        val dt = (frame.timestampNanos - last) / 1_000_000_000f
        val spinDegPerSec = Math.toDegrees(frame.gyroZ.toDouble()).toFloat()
        val tiltDeg = Math.toDegrees(
            kotlin.math.sqrt(
                frame.pitch * frame.pitch + frame.roll * frame.roll
            ).toDouble()
        ).toFloat()

        val isStable = tiltDeg < 20f
        val isSpinning = kotlin.math.abs(spinDegPerSec) > 60f

        val addedScore =
            if (isStable && isSpinning) {
                (kotlin.math.abs(spinDegPerSec) * dt).toInt()
            } else {
                0
            }

        val newCombo =
            if (addedScore > 0) state.combo + 1 else 0

        val message =
            when {
                !isStable -> "傾きすぎです。ピザを落とさないように！"
                !isSpinning -> "もっと勢いよく回しましょう"
                else -> "Good! 水平キープ"
            }

        state = state.copy(
            score = state.score + addedScore,
            combo = newCombo,
            pizzaRotationDeg = state.pizzaRotationDeg + spinDegPerSec * dt,
            spinDegPerSec = spinDegPerSec,
            tiltDeg = tiltDeg,
            message = message
        )

        return state
    }
}```]

この実装では、端末が水平に近く、かつ一定以上の角速度で回転している場合にスコアを加算する。
単に端末を激しく振るだけでは傾きが大きくなるため、安定したスコア獲得が難しくなる。
これにより、ピザ回しらしい「水平に保ちながら回す」動作を促すことができる。

== センサイベントの取得

Android では、`SensorManager` を用いてセンサイベントを取得する。
以下に、ジャイロセンサと回転ベクトルセンサを登録する処理の例を示す。

#sourcecode[```kotlin
class SensorController(
    context: Context,
    private val onFrame: (SensorFrame) -> Unit
) : SensorEventListener {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val gyro =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    private val rotationVector =
        sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private var latestPitch: Float = 0f
    private var latestRoll: Float = 0f

    fun start() {
        sensorManager.registerListener(
            this,
            gyro,
            SensorManager.SENSOR_DELAY_GAME
        )
        sensorManager.registerListener(
            this,
            rotationVector,
            SensorManager.SENSOR_DELAY_GAME
        )
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ROTATION_VECTOR -> {
                val matrix = FloatArray(9)
                val orientation = FloatArray(3)
                SensorManager.getRotationMatrixFromVector(matrix, event.values)
                SensorManager.getOrientation(matrix, orientation)

                latestPitch = orientation[1]
                latestRoll = orientation[2]
            }

            Sensor.TYPE_GYROSCOPE -> {
                onFrame(
                    SensorFrame(
                        timestampNanos = event.timestamp,
                        gyroZ = event.values[2],
                        pitch = latestPitch,
                        roll = latestRoll
                    )
                )
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}
```]

センサ値は高頻度に更新されるため、UI 側で直接複雑な処理を行うと見通しが悪くなる。
そこで、センサ値取得処理とゲームロジックを分離し、`SensorFrame` として値を渡す構成にした。

== Jetpack Compose による画面表示

UI では、ゲーム状態に応じてスコアやピザ画像を表示する。
ピザ画像には `graphicsLayer` を用いて回転を適用する。

#sourcecode[```kotlin
@Composable
fun GameScreen(
    state: GameState,
    onStartClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Napori",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("score\n${state.score}")
            Text("combo\n${state.combo}")
            Text("best\n${state.bestScore}")
        }

        Spacer(modifier = Modifier.height(32.dp))

        Image(
            painter = painterResource(id = R.drawable.pizza),
            contentDescription = "pizza",
            modifier = Modifier
                .size(260.dp)
                .graphicsLayer {
                    rotationZ = state.pizzaRotationDeg
                }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text("spin  ${state.spinDegPerSec.toInt()} deg/s")
        Text("tilt  ${state.tiltDeg.toInt()} deg")
        Text(state.message)

        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onStartClick) {
            Text("Start")
        }
    }
}
```]

Jetpack Compose を用いることで、状態が変わるたびに画面が再描画されるため、ピザ画像の回転やスコア表示を自然に実装できた。

== テスト

本アプリでは、ゲームロジックを UI から分離したため、センサ値を擬似的に与えてテストすることができる。
以下に、安定して回転している場合にスコアが増加することを確認するテストの例を示す。

#sourcecode[```kotlin
@Test
fun scoreIncreasesWhenPhoneSpinsStably() {
    val engine = PizzaSpinEngine()

    engine.update(
        SensorFrame(
            timestampNanos = 0L,
            gyroZ = 0f,
            pitch = 0f,
            roll = 0f
        )
    )

    val state = engine.update(
        SensorFrame(
            timestampNanos = 1_000_000_000L,
            gyroZ = 3.0f,
            pitch = 0.05f,
            roll = 0.05f
        )
    )

    assertTrue(state.score > 0)
    assertTrue(state.combo > 0)
}
```]

このように、実機のセンサを使わずにゲームロジックの正しさを確認できる点は、開発を進める上で有効であった。

= 動作例

== 起動画面

@fig1 に、Napori の起動画面を示す。
画面中央にはピザ画像を大きく配置し、下部に Start ボタンを表示している。
ユーザはこの画面からゲームを開始する。

#figure(
  image("title.jpg", width: 30%),
  caption: [Napori の起動画面]
) <fig1>

起動画面では、アプリ名とピザ画像を前面に出すことで、ユーザがアプリの目的をすぐに理解できるようにした。
また、操作説明を短く表示し、スマートフォンを手のひらの上で回すアプリであることを示している。

== プレイ画面

@fig2 に、プレイ中の画面を示す。
画面上部にはスコア、コンボ、ベストスコアが表示される。
中央にはピザ画像が表示され、端末の回転に応じて画像も回転する。
画面下部には、現在の回転速度と傾き、さらにユーザへのフィードバック文が表示される。

#figure(
  image("game.jpg", width: 30%),
  caption: [Napori のプレイ画面]
) <fig2>

たとえば、ユーザがスマートフォンを水平に近い状態で回転させると、回転速度が `deg/s` 単位で表示され、スコアが増加する。
このとき、画面には “Good! 水平キープ” のようなフィードバックが表示される。
一方で、端末を大きく傾けると「傾きすぎです」のようなメッセージが表示され、スコアが伸びにくくなる。

== 結果画面

30秒が経過すると、結果画面に遷移し、スコアが表示される。

== 操作の流れ


Napori の基本的な操作手順は以下の通りである。

1. アプリを起動する。
2. Start ボタンを押してゲームを開始する。
3. スマートフォンを手のひらの上に置く。
4. ピザ生地を回すようにスマートフォンを回転させる。
5. 端末を水平に保ちながら回すとスコアが増加する。
6. 回転速度や傾きに応じてフィードバックが表示される。
7. ゲーム終了後、スコアやベストスコアを確認する。

このように、Napori は画面タッチを中心とした通常のスマートフォンゲームとは異なり、端末そのものを動かすことを主な入力とする。
これにより、センサを利用した身体的なゲーム体験を実現している。

= 得られた効果と考察

== うまくいった点

本アプリの開発で特にうまくいった点は、スマートフォンの回転という物理的な動作を、画面上のピザ画像の回転やスコアに直感的に対応付けられたことである。
ユーザが端末を回すとピザ画像も回るため、自分の動作がすぐにアプリへ反映されていることが分かる。
この即時性は、センサを用いたアプリにおいて重要である。

また、ゲームロジックを UI から分離したことにより、実装の見通しが良くなった。
センサ値の取得、ゲーム状態の更新、UI 表示を別々の責務として扱うことで、コードの変更やテストが容易になった。
特に、センサ値を直接扱うアプリでは、実機でなければ確認できない部分が多いが、ゲームロジックを独立させることで、擬似的な入力を用いたテストが可能になった。

さらに、Jetpack Compose を用いたことで、状態に応じて UI を更新する処理を簡潔に記述できた。
スコア、コンボ、回転角、フィードバック文などが `GameState` に集約されているため、UI は状態を表示するだけでよく、実装が整理された。

== 難しかった点

一方で、センサ値の扱いには難しさがあった。
ジャイロセンサの値は端末の向きや持ち方によって解釈が変わるため、どの軸の角速度をピザ回しとして扱うかを調整する必要があった。
また、センサ値にはノイズが含まれるため、閾値を単純に設定するだけでは、意図しないスコア加算や判定のブレが発生することがあった。

特に難しかったのは、ゲームとして楽しい判定と、実際の動作に近い判定のバランスである。
判定を厳しくしすぎるとスコアがほとんど伸びず、ユーザが楽しみにくくなる。
一方で、判定を緩くしすぎると、単に端末を振るだけで高得点が取れてしまい、ピザ回しの練習らしさが失われる。
そのため、回転速度と傾きの閾値を調整し、安定して回すことを評価するようにした。

また、本アプリはスマートフォンを実際に動かすため、安全性にも注意が必要である。
コンセプト上、「床に落としたら終わり」というリアルさがある一方で、本当に端末を落とすと危険である。
そのため、実際に遊ぶ際には柔らかい場所や安全な環境で行う必要がある。

== 想定外の事故

開発中には、端末を軽く振っただけでもジャイロセンサの値が大きく変化し、スコアが加算されてしまう場合があった。
これは、回転運動と振動運動を完全には区別できていなかったためである。
そこで、単に角速度だけを見るのではなく、傾きが一定範囲内に収まっているかも同時に確認することで、不自然な入力をある程度抑制した。

また、端末の持ち方によって回転方向の符号が逆に感じられる場合があった。
これにより、ユーザの動作と画面上のピザ画像の回転方向が直感に反することがあった。
今後は、ゲーム開始時の端末姿勢を基準として座標系を補正することで、より自然な操作感に改善できると考えられる。

== このアプリで得られた効果

Napori により、スマートフォン内蔵センサを用いて、端末そのものを入力デバイスとするゲーム体験を実現できた。
通常のタッチ操作ではなく、端末を回す、傾ける、水平に保つといった身体的な操作をゲームに取り入れることで、センサを活用した Android アプリの可能性を確認できた。

また、ピザ回しというユーモラスな題材を選ぶことで、センサ値の意味をユーザが直感的に理解しやすくなった。
単に角速度や傾きを数値として表示するのではなく、「ピザを回す」「落とさないようにする」という目的に変換することで、センサの値がゲーム体験として意味を持つようになった。

== 将来的な展望

今後の展望として、まずスマートウォッチを用いた不正防止が考えられる。
スマートフォンだけでは、手で持って振る動作と、実際に手のひら上で回す動作を完全に区別することは難しい。
そこで、スマートウォッチのセンサを併用し、手首の動きとスマートフォンの動きを比較することで、より正確にピザ回し動作を判定できる可能性がある。

また、Napori の考え方はピザ回しに限らず、他の料理工程にも応用できる。
たとえば、ジャイロセンサとマイクを用いて生地を伸ばす動作を検出する練習アプリや、スマートフォンを振ってバターを作るチャレンジなど、さまざまな料理ミニゲームへ発展させることができる。

このように、Napori は単なるピザ回しゲームではなく、スマートフォンのセンサを活用した料理練習プラットフォームへの発展可能性を持つアプリである。
今後は、センサ値の解析精度を高めるとともに、より多様な料理動作をゲームとして実装していきたい。

== まとめ

本レポートでは、ジャイロセンサと方位センサを用いたピザ回し練習アプリ「Napori」の開発について述べた。
Napori は、スマートフォンをピザ生地に見立て、端末の回転や傾きをもとにスコアを算出するアプリである。
Kotlin と Jetpack Compose を用いて実装し、ゲームロジックを UI から分離することで、テストしやすい構成とした。

開発を通して、センサ値をそのまま表示するのではなく、ユーザにとって意味のある体験へ変換することの重要性を確認できた。
また、センサ値のノイズや端末姿勢の違いなど、実機を扱うアプリ特有の難しさも明らかになった。

Napori は一見すると奇妙なアプリであるが、身体動作とスマートフォンセンサを結びつけることで、新しい練習体験やゲーム体験を生み出せる可能性を示している。
今後は、スマートウォッチとの連携や、他の料理工程への拡張を行い、より実用的かつ楽しい料理練習アプリへ発展させていきたい。
