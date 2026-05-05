import android.os.Bundle;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.touchsample.R;

public class MainActivity extends AppCompatActivity {
    private TextView mTouchTypeText;
    private TextView mTouchPointText1;
    private TextView mTouchPointText2;
    private TextView mTouchLengthText;
    private static final int NONE = 0;
    private static final int TOUCH = 1;
    private static final int DRAG = 2;
    private static final int PINCH = 3;
    private int mTouchMode = NONE;
    private float mDragStartX = 0.0f;
    private float mDragStartY = 0.0f;
    private double mPinchStartDistance = 0.0f;
    private String mTouchTypeString = "";
    private String mTouchPoint1String = "";
    private String mTouchPoint2String = "";
    private String mTouchLengthString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mTouchTypeText = (TextView) findViewById(R.id.text_view_touch_type);
        mTouchPointText1 = (TextView) findViewById(R.id.text_view_touch_point_1);
        mTouchPointText2 = (TextView) findViewById(R.id.text_view_touch_point_2);
        mTouchLengthText = (TextView) findViewById(R.id.text_view_touch_length);
    }

    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() >= 2) {
                    mPinchStartDistance = getPinchDistance(event);
                    if (mPinchStartDistance > 50f) {
                        mTouchMode = PINCH;
                        mTouchTypeString = "PINCH";
                        mTouchPoint1String = "x:" + event.getX(0) + ".y:" + event.getY(0);
                        mTouchPoint2String = "x:" + event.getX(1) + ".y:" + event.getY(1);
                        mTouchLengthString = "length:" + getPinchDistance(event);
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode == PINCH && mPinchStartDistance > 0) {
                    mTouchTypeString = "PINCH";
                    mTouchPoint1String = "x:" + event.getX(0) + ".y:" + event.getY(0);
                    mTouchPoint2String = "x:" + event.getX(1) + ".y:" + event.getY(1);
                    mTouchLengthString = "length:" + getPinchDistance(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
            case MotionEvent.ACTION_POINTER_UP:
                if (mTouchMode == PINCH) {
                    mTouchTypeString = "PINCH";
                    mTouchPoint1String = "x:" + event.getX(0) + ".y:" + event.getY(0);
                    mTouchPoint2String = "x:" + event.getX(1) + ".y:" + event.getY(1);
                    mTouchLengthString = "length:" + getPinchDistance(event);
                    mTouchMode = NONE;
                }
                break;
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mTouchMode == NONE && event.getPointerCount() == 1) {
                    mTouchMode = TOUCH;
                    mDragStartX = event.getX(0);
                    mDragStartY = event.getY(0);
                    mTouchTypeString = "TOUCH";
                    mTouchPoint1String = "x:" + mDragStartX + ".y:" + mDragStartY;
                    mTouchPoint2String = "x:" + event.getX(0) + ",y:" + event.getY(0);
                    mTouchLengthString = "length:" + getDragDistance(event);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mTouchMode == DRAG || mTouchMode == TOUCH) {
                    mTouchMode = DRAG;
                    mTouchTypeString = "DRAG";
                    mTouchPoint1String = "x:" + mDragStartX + ".y:" + mDragStartY;
                    mTouchPoint2String = "x:" + event.getX(0) + ",y:" + event.getY(0);
                    mTouchLengthString = "length:" + getDragDistance(event);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mTouchMode == TOUCH) {
                    mTouchTypeString = "TOUCH";
                } else if (mTouchMode == DRAG) {
                    mTouchTypeString = "DRAG";
                }
                mTouchPoint1String = "x:" + mDragStartX + ".y:" + mDragStartY;
                mTouchPoint2String = "x:" + event.getX(0) + ",y:" + event.getY(0);
                mTouchLengthString = "length:" + getDragDistance(event);
                mTouchMode = NONE;
                break;
        }
        mTouchTypeText.setText("touch type:" + mTouchTypeString);
        mTouchPointText1.setText(mTouchPoint1String);
        mTouchPointText2.setText(mTouchPoint2String);
        mTouchLengthText.setText(mTouchLengthString);
        return super.onTouchEvent(event);
    }

    private double getDragDistance(MotionEvent event) {
        double touchX0 = event.getX(0);
        double touchY0 = event.getY(0);
        double dragLengthX = touchX0 - mDragStartX;
        double dragLengthY = touchY0 - mDragStartY;
        return Math.sqrt(dragLengthX * dragLengthX + dragLengthY * dragLengthY);
    }

    private double getPinchDistance(MotionEvent event) {
        double x = event.getX(0) - event.getX(1);
        double y = event.getY(0) - event.getY(1);
        return Math.sqrt(x * x + y * y);
    }
}