package de.hsfl.machinelearning.numbers;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class DigitClassifier {
    private static String TAG = "DigitClassifier";
    private static String MODEL_FILE = "mnist.tflite";

    private Context context;
    private Interpreter interpreter = null;

    private int inputWidth = 0;
    private int inputHeight = 0;
    private int inputSize = 0;

    DigitClassifier(Context context) {
        this.context = context;
    }

    public void init() throws IOException {
        Interpreter interpreter = new Interpreter(loadModel(), createOptions());
        Tensor inputTensor = interpreter.getInputTensor(0);
        int[] inputShape = inputTensor.shape();
        Log.d(TAG, ""+inputShape[0]);
        this.inputWidth = inputShape[1];
        this.inputHeight = inputShape[2];
        this.inputSize = inputTensor.numBytes();
        Log.d(TAG, "Tensor data type: " + inputTensor.dataType().toString());
        this.interpreter = interpreter;
        Log.d(TAG, "init() done");
    }

    public void close() {
        if (this.interpreter != null) {
            this.interpreter.close();
            this.interpreter = null;
            this.inputWidth = 0;
            this.inputHeight = 0;
            Log.d(TAG, "close() done");
        }
    }

    public float[][] classify(Bitmap bitmap) throws IllegalStateException {
        if (interpreter == null) {
            throw new IllegalStateException("interpreter is not initialized");
        }
        ByteBuffer inputBuffer = preprocessBitmap(bitmap);
        Log.d(TAG, "Bitmap byte-buffer size: " + inputBuffer.position());
        float[][] result = new float[1][10]; // numbers 0 to 9
        interpreter.run(inputBuffer, result);
        Log.d(TAG, "classify() done");
        return result;
    }

    private ByteBuffer preprocessBitmap(Bitmap bitmap) {
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(inputSize);
        inputBuffer.order(ByteOrder.nativeOrder());
        int[] pixels = new int[inputWidth * inputHeight];
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true);
        scaled.getPixels(pixels, 0, scaled.getWidth(), 0, 0, scaled.getWidth(), scaled.getHeight());
        for (int pixel : pixels) {
            int sum = (pixel>>16 & 0xFF) + (pixel>>8 & 0xFF) + (pixel & 0xFF); // red + green + blue
            float normalizedPixel = sum / 3.0f / 255.0f;
            inputBuffer.putFloat(normalizedPixel);
        }
        return inputBuffer;
    }

    private ByteBuffer loadModel() throws IOException {
        // get asset file and create input stream
        AssetFileDescriptor fd = context.getAssets().openFd(MODEL_FILE);
        FileInputStream is = new FileInputStream(fd.getFileDescriptor());
        // convert input stream into byte buffer
        return is.getChannel().map(
            FileChannel.MapMode.READ_ONLY,
            fd.getStartOffset(),
            fd.getDeclaredLength()
        );
    }

    private Interpreter.Options createOptions() {
        Interpreter.Options options = new Interpreter.Options();
        options.setUseNNAPI(true);
        return options;
    }
}
