package de.hsfl.machinelearning.numbers;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.renderscript.RSInvalidStateException;
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
        // setting the following stroke and colors seems to improve recognition
        drawView.setStrokeWidth(70.0f);
        drawView.setColor(Color.WHITE);
        drawView.setBackgroundColor(Color.BLACK);
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
            try {
                classifyDrawing();
            }
            catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        return true;
    }

    private void classifyDrawing() throws IllegalStateException {
        Bitmap bitmap = drawView.getBitmap();
        if (bitmap == null) {
            throw new IllegalStateException("DrawView contains no bitmap");
        }
        float[][] result = digitClassifier.classify(bitmap);
        showResult(result);
    }

    private void showResult(float[][] result) {
        int maxIndex = 0;
        for (int i=0; i < result[0].length; i++) {
            Log.d(TAG, String.format("result: %d -> %2f", i, result[0][i]));
            if (result[0][i] > result[0][maxIndex]) {
                maxIndex = i;
            }
        }
        String output = String.format("Prediction: %d (%2f / 1.0)", maxIndex, result[0][maxIndex]);
        predictionText.setText(output);
    }
}
