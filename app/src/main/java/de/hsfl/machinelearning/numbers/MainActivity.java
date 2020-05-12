package de.hsfl.machinelearning.numbers;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.divyanshu.draw.widget.DrawView;

public class MainActivity extends AppCompatActivity implements DrawView.OnTouchListener {

    private static String TAG = "MainActivity";

    DrawView drawView;
    TextView predictionText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = findViewById(R.id.draw_view);
        predictionText = findViewById(R.id.prediction_text);

        drawView.setOnTouchListener(this);
    }

    public void onResetClick(View view) {
        drawView.clearCanvas();
        predictionText.setText(getString(R.string.prediction_default));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        drawView.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.d(TAG, "TODO Do some classifying...");
        }
        return true;
    }
}
