package de.hsfl.machinelearning.numbers;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.divyanshu.draw.widget.DrawView;

public class MainActivity extends AppCompatActivity implements DrawView.OnTouchListener {

    private static String TAG = "MainActivity";

    DrawView drawView;
    TextView predictionText;

    DigitClassifier digitClassifier =  new DigitClassifier(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawView = findViewById(R.id.draw_view);
        drawView.setOnTouchListener(this);
        predictionText = findViewById(R.id.prediction_text);

        try {
            digitClassifier.init();
        }
        catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        digitClassifier.close();
    }

    public void onResetClick(View view) {
        drawView.clearCanvas();
        predictionText.setText(getString(R.string.prediction_default));
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        drawView.onTouchEvent(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            classifyDrawing();
        }
        return true;
    }

    private void classifyDrawing() {
        Bitmap bitmap = drawView.getBitmap();
        if (bitmap != null) {
            try {
                digitClassifier.classify(bitmap);
                predictionText.setText("TODO classify drawing");
            }
            catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
    }
}
