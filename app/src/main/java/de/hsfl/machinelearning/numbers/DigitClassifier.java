package de.hsfl.machinelearning.numbers;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.util.Log;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

public class DigitClassifier {
    private static String TAG = "DigitClassifier";
    private static String MODEL_FILE = "mnist.tflite";
    private static int OUTPUT_CLASSES = 10; // numbers 0 to 9
    // TODO Could the following constants be replaced by values we get from the system?
    private static int FLOAT_TYPE_SIZE = 4;
    private static int PIXEL_SIZE = 1;

    private Context context = null;
    private Interpreter interpreter = null;

    private int inputWidth = 0; // will be inferred from TF Lite model
    private int inputHeight = 0; // will be inferred from TF Lite model
    private int inputSize = 0; // will be inferred from TF Lite model

    DigitClassifier(Context context) {
        this.context = context;
    }

    public void init() throws IOException {
        Interpreter interpreter = new Interpreter(loadModel(), createOptions());
        int[] inputShape = interpreter.getInputTensor(0).shape();
        this.inputWidth = inputShape[1];
        this.inputHeight = inputShape[2];
        this.inputSize = FLOAT_TYPE_SIZE * inputShape[1] * inputShape[2] * PIXEL_SIZE;
        this.interpreter = interpreter;
        Log.d(TAG, "init() done");
    }

    public void close() {
        this.interpreter.close();
        this.interpreter = null;
        this.inputWidth = 0;
        this.inputHeight = 0;
        this.inputSize = 0;
        Log.d(TAG, "close() done");
    }

    public float[] classify(Bitmap bitmap) throws IllegalStateException {
        if (interpreter == null) {
            throw new IllegalStateException("interpreter is not initialized");
        }
        ByteBuffer byteBuffer = preprocessBitmap(bitmap);
        float[][] result = new float[1][OUTPUT_CLASSES];
        interpreter.run(byteBuffer, result);
        Log.d(TAG, "classify() done");
        return result[0];
    }

    private ByteBuffer preprocessBitmap(Bitmap bitmap) {
        int[] pixels = new int[inputWidth * inputHeight];
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true);
        scaledBitmap.getPixels(pixels, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(inputSize);
        byteBuffer.order(ByteOrder.nativeOrder());

        for (int pixel : pixels) {
            int r = (pixel >> 16 & 0xFF);
            int g = (pixel >> 8 & 0xFF);
            int b = (pixel & 0xFF);
            // Convert RGB to grayscale and normalize pixel value
            float normalizedPixel = (r + g + b) / 3.0f / 255.0f;
            byteBuffer.putFloat(normalizedPixel);
        }
        return byteBuffer;
    }

    private ByteBuffer loadModel() throws IOException {
        AssetFileDescriptor fd = context.getAssets().openFd(MODEL_FILE);
        FileInputStream is = new FileInputStream(fd.getFileDescriptor());
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
