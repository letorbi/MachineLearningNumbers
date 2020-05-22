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

    private Context context;
    private Interpreter interpreter = null;

    private int inputWidth = 0;
    private int inputHeight = 0;

    DigitClassifier(Context context) {
        this.context = context;
    }

    private ByteBuffer preprocessBitmap(Bitmap bitmap) {
        // the byte buffer hold the input data
        ByteBuffer inputBuffer = ByteBuffer.allocateDirect(FLOAT_TYPE_SIZE * inputWidth * inputHeight * PIXEL_SIZE);
        inputBuffer.order(ByteOrder.nativeOrder());
        // scale the bitmap to the size required by the interpreter and get the pixel values
        int[] pixels = new int[inputWidth * inputHeight];
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputWidth, inputHeight, true);
        scaledBitmap.getPixels(pixels, 0, scaledBitmap.getWidth(), 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight());
        // convert RGB into floats between 0 and 1 and write the result into the input buffer
        for (int pixel : pixels) {
            int r = (pixel >> 16 & 0xFF);
            int g = (pixel >> 8 & 0xFF);
            int b = (pixel & 0xFF);
            float normalizedPixel = (r + g + b) / 3.0f / 255.0f;
            inputBuffer.putFloat(normalizedPixel);
        }
        return inputBuffer;
    }

    private Interpreter.Options createOptions() {
        Interpreter.Options options = new Interpreter.Options();
        options.setUseNNAPI(true);
        return options;
    }
}
