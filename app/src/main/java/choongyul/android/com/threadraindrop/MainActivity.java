package choongyul.android.com.threadraindrop;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

public class MainActivity extends AppCompatActivity {
    private static final int MAKE_RAIN = 30;
    private static final int CALL_INVALIDATE = 10;
    FrameLayout frameLayout;
    Button btnStart, btnStop, btnPause;
    TextView tv1,tv2;
    int deviceWidth,deviceHeight;
    Random random;
    Stage stage;
    CountRaindropNumber countRaindropNumber;
    CallInvalidate callInvalidate;
    boolean pauseFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        deviceLength();
        widgetSetting();
        listenerSetting();

        stage = new Stage(this);
        frameLayout.addView(stage);

    }


    private void widgetSetting() {
        frameLayout = (FrameLayout) findViewById(R.id.frameLO);
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnPause = (Button) findViewById(R.id.btnPause);
        tv1 = (TextView) findViewById(R.id.tv1);
        tv2 = (TextView) findViewById(R.id.tv2);
//        tv2.setText(""+deviceHeight);
        random = new Random();


    }

    private void listenerSetting() {
        btnStart.setOnClickListener(clickListener);
        btnStop.setOnClickListener(clickListener);
        btnPause.setOnClickListener(clickListener);
    }

    private void deviceLength() {
        DisplayMetrics matrix = getResources().getDisplayMetrics();
        deviceWidth = matrix.widthPixels;
        deviceHeight = matrix.heightPixels;
    }

    MakeRain makeRain;
    private boolean makeRainFlag = true;
    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnStart:
                    //빗방울을 생성한다.
                    pauseFlag = false;
                    if(makeRainFlag) {
                        makeRainFlag = false;
                        makeRain = new MakeRain();
                        makeRain.start();
                    }
                    countRaindropNumber = new CountRaindropNumber();
                    countRaindropNumber.start();
                    callInvalidate = new CallInvalidate();
                    callInvalidate.start();
                    break;
                case R.id.btnStop:
                    // 빗방울을 멈춘다
                    makeRainFlag = true;
                    makeRain.makeFlag = false;
                    pauseFlag = true;
                    break;
                case R.id.btnPause:
                    // 빗방울 생성을 멈춘다
                    makeRain.makeFlag = false;
                    makeRainFlag = true;
                    break;
            }
        }
    };

    class Raindrop extends Thread {
        int x;
        int y;
        int radius;
        int speed;
//        int direction; // 나중에 알아서 하기
        boolean stopFlag = true;

        public Raindrop(){
            this.x = random.nextInt(1440);
            this.y = 0;
            this.radius = random.nextInt(30)+10;
            this.speed = random.nextInt(40)+10;

        }

        @Override
        public void run() {
            super.run();
            while (stopFlag && y <=deviceHeight + radius) {
                if(!pauseFlag) {
                    y = y + 1;
//                    stage.postInvalidate(); // runOnUiThread 와 비슷한 역할. 그려주는 스레드를 따로 뽑으려 하기때문에 주석처리함
                    try {
                        Thread.sleep(speed/5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            stage.removeRaindrop(this);

        }
    }

    class Stage extends View {
        Paint rainColor;
        List<Raindrop> raindrops;

        public Stage(Context context) {
            super(context);
            raindrops = new CopyOnWriteArrayList<>(); // 카피하고 쓸때
            rainColor = new Paint();
            rainColor.setColor(Color.CYAN);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

//            for(int i=0; i<raindrops.size() ; i++){
//                Raindrop raindrop = raindrops.get(i);
//                canvas.drawCircle(raindrop.x,raindrop.y,raindrop.radius,rainColor);
//            }

//             향포문의 문제점은
            for(Raindrop raindrop : raindrops) {
                canvas.drawCircle(raindrop.x,raindrop.y,raindrop.radius,rainColor);
            }
        }

        public void addRaindrop(Raindrop raindrop) {
            raindrops.add(raindrop);
        }

        public void removeRaindrop(Raindrop raindrop) {
            raindrops.remove(raindrop);
        }
    }


    final int WHAT = 100;
    int count = 0;
    class MakeRain extends Thread {
        boolean makeFlag;

        @Override
        public void run() {
            makeFlag = true;
            while(makeFlag) {
                Raindrop raindrop = new Raindrop();
                stage.addRaindrop(raindrop);

//                Message msg = new Message();
//                msg.what = WHAT;            //msg의 멤버인 what과 arg1, arg2는 int형 파라미터이다.
//                msg.arg1 = stage.raindrops.size();
//                handler.sendMessage(msg);


                raindrop.start();
                try {
                    Thread.sleep(MAKE_RAIN);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT:
                    tv1.setText(""+msg.arg1);
                    break;
            }
        }
    };

    class CountRaindropNumber extends Thread {

        @Override
        public void run() {
            while(true) {
                Message msg = new Message();
                msg.what = WHAT;            //msg의 멤버인 what과 arg1, arg2는 int형 파라미터이다.
                msg.arg1 = stage.raindrops.size();
                handler.sendMessage(msg);
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class CallInvalidate extends Thread {

        @Override
        public void run() {
            while(true) {
                stage.postInvalidate();
                try {
                    Thread.sleep(CALL_INVALIDATE);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        makeRain.interrupt();
        countRaindropNumber.interrupt();
        callInvalidate.interrupt();
        super.onDestroy();
    }

}
