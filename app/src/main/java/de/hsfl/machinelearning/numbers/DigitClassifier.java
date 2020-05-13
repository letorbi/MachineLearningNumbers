package de.hsfl.machinelearning.numbers;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.drawable.shapes.Shape;
import android.util.Log;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class DigitClassifier {
    private static String TAG = "DigitClassifier";
    private static String MODEL_FILE = "mnist.tflite";
    // TODO Could the following constants be replaced by values we get from the system?
    private static int FLOAT_TYPE_SIZE = 4;
    private static int PIXEL_SIZE = 1;

    private Context context = null;
    private Interpreter interpreter = null;

    private int inputImageWidth = 0; // will be inferred from TF Lite model
    private int inputImageHeight = 0; // will be inferred from TF Lite model
    private int modelInputSize = 0; // will be inferred from TF Lite model

    DigitClassifier(Context context) {
        this.context = context;
    }

    public void init() throws IOException {
        Interpreter interpreter = new Interpreter(loadModel(), createOptions());
        int[] inputShape = interpreter.getInputTensor(0).shape();
        this.inputImageWidth = inputShape[1];
        this.inputImageHeight = inputShape[2];
        this.modelInputSize = FLOAT_TYPE_SIZE * inputShape[1] * inputShape[2] * PIXEL_SIZE;
        this.interpreter = interpreter;
        Log.d(TAG, "init() done");
    }

    public void close() {
        this.interpreter.close();
        this.inputImageWidth = 0;
        this.inputImageHeight = 0;
        this.modelInputSize = 0;
        this.interpreter = null;
        Log.d(TAG, "close() done");
    }

    public void classify(Bitmap bitmap) throws IllegalStateException {
        if (interpreter == null) {
            throw new IllegalStateException("interpreter is not initialized");
        }
        Log.d(TAG, "TODO classify bitmap");
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
