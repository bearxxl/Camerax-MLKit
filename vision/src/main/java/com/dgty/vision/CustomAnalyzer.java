package com.dgty.vision;

import static androidx.camera.core.ImageAnalysis.COORDINATE_SYSTEM_ORIGINAL;
import static com.google.android.gms.common.internal.Preconditions.checkArgument;
import static com.google.mlkit.vision.interfaces.Detector.TYPE_BARCODE_SCANNING;
import static com.google.mlkit.vision.interfaces.Detector.TYPE_POSE_DETECTION;
import static com.google.mlkit.vision.interfaces.Detector.TYPE_SEGMENTATION;
import static com.google.mlkit.vision.interfaces.Detector.TYPE_TEXT_RECOGNITION;

import android.annotation.SuppressLint;
import android.graphics.Matrix;
import android.media.Image;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Logger;
import androidx.camera.mlkit.vision.MlKitAnalyzer;
import androidx.camera.view.TransformExperimental;
import androidx.camera.view.transform.CoordinateTransform;
import androidx.camera.view.transform.ImageProxyTransformFactory;
import androidx.camera.view.transform.OutputTransform;
import androidx.core.util.Consumer;

import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.interfaces.Detector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;

/**
 * Title: CustomAnalyzer
 * Description:
 * Copyright (c) 即刻动身版权所有
 * Created DateTime: 2022-12-03 17:01
 * Created by xxl.
 */
public class CustomAnalyzer implements ImageAnalysis.Analyzer {

    private static final String TAG = "MlKitAnalyzer";

    private static final Size DEFAULT_SIZE = new Size(480, 360);

    @NonNull
    private final List<Detector<?>> mDetectors;
    private final int mTargetCoordinateSystem;
    // Synthetic access
    @NonNull
    final Consumer<CustomAnalyzer.Result> mConsumer;
    // Synthetic access
    final ImageProxyTransformFactory mImageAnalysisTransformFactory;
    @NonNull
    private final Executor mExecutor;

    @Nullable
    private Matrix mSensorToTarget;

    /**
     * Constructor of {@link MlKitAnalyzer}.
     *
     * <p>The list of detectors will be invoked sequentially in order.
     *
     * <p>When the targetCoordinateSystem is {@link ImageAnalysis#COORDINATE_SYSTEM_ORIGINAL}, the
     * output coordinate system is defined by ML Kit, which is the buffer with rotation applied. For
     * example, if {@link ImageProxy#getHeight()} is {@code h} and the rotation is 90°, (0, 0) in
     * the result maps to the pixel (0, h) in the original buffer.
     *
     * <p>The constructor throws {@link IllegalArgumentException} if
     * {@code Detector#getDetectorType()} is TYPE_SEGMENTATION and {@code targetCoordinateSystem}
     * is COORDINATE_SYSTEM_ORIGINAL. Currently ML Kit does not support transformation with
     * segmentation.
     *
     * @param detectors              list of ML Kit {@link Detector}.
     * @param targetCoordinateSystem e.g. {@link ImageAnalysis#COORDINATE_SYSTEM_ORIGINAL}
     *                               the coordinates in ML Kit output will be based on this value.
     * @param executor               on which the consumer is invoked.
     * @param consumer               invoked when there is a new ML Kit result.
     */
    @OptIn(markerClass = TransformExperimental.class)
    public CustomAnalyzer(
            @NonNull List<Detector<?>> detectors,
            int targetCoordinateSystem,
            @NonNull Executor executor,
            @NonNull Consumer<CustomAnalyzer.Result> consumer) {
        if (targetCoordinateSystem != COORDINATE_SYSTEM_ORIGINAL) {
            for (Detector<?> detector : detectors) {
                checkArgument(detector.getDetectorType() != TYPE_SEGMENTATION,
                        "Segmentation only works with COORDINATE_SYSTEM_ORIGINAL");
            }
        }
        // Make an immutable copy of the app provided detectors.
        mDetectors = new ArrayList<>(detectors);
        mTargetCoordinateSystem = targetCoordinateSystem;
        mConsumer = consumer;
        mExecutor = executor;
        mImageAnalysisTransformFactory = new ImageProxyTransformFactory();
        mImageAnalysisTransformFactory.setUsingRotationDegrees(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @OptIn(markerClass = TransformExperimental.class)
    public final void analyze(@NonNull ImageProxy imageProxy) {
        // By default, the matrix is identity for COORDINATE_SYSTEM_ORIGINAL.
        Matrix transform = new Matrix();
        if (mTargetCoordinateSystem != COORDINATE_SYSTEM_ORIGINAL) {
            // Calculate the transform if not COORDINATE_SYSTEM_ORIGINAL.
            Matrix sensorToTarget = mSensorToTarget;
            if (sensorToTarget == null) {
                // If the app set a target coordinate system, do not perform detection until the
                // transform is ready.
                imageProxy.close();
                return;
            }
            OutputTransform analysisTransform =
                    mImageAnalysisTransformFactory.getOutputTransform(imageProxy);
            Size cropRectSize = new Size(imageProxy.getCropRect().width(),
                    imageProxy.getCropRect().height());
            @SuppressLint("RestrictedApi") CoordinateTransform coordinateTransform = new CoordinateTransform(analysisTransform,
                    new OutputTransform(sensorToTarget, cropRectSize));
            coordinateTransform.transform(transform);
        }
        // Detect the image recursively, starting from index 0.
        detectRecursively(imageProxy, 0, transform, new HashMap<>(), new HashMap<>());
    }

    /**
     * Recursively processes the image with {@link #mDetectors}.
     *
     * @param detectorIndex the current index of {@link #mDetectors} being processed.
     * @param values        values returned from the {@link #mDetectors}.
     * @param throwables    exceptions returned from the {@link #mDetectors}.
     */
    @OptIn(markerClass = ExperimentalGetImage.class)
    private void detectRecursively(
            @NonNull ImageProxy imageProxy,
            int detectorIndex,
            @NonNull Matrix transform,
            Map<Detector<?>, Object> values,
            @NonNull Map<Detector<?>, Throwable> throwables) {
        Image image = imageProxy.getImage();
        if (image == null) {
            // No-op if the frame is not backed by ImageProxy.
            imageProxy.close();
            return;
        }

        if (detectorIndex > mDetectors.size() - 1) {
            // Termination condition is met when the index reaches the end of the list.
            imageProxy.close();
            mExecutor.execute(() -> mConsumer.accept(
                    new CustomAnalyzer.Result(values, imageProxy.getImageInfo().getTimestamp(), throwables)));
            return;
        }
        Detector<?> detector = mDetectors.get(detectorIndex);
        int rotationDegrees = imageProxy.getImageInfo().getRotationDegrees();

        Task<?> mlKitTask;
        try {
            mlKitTask = detector.process(image, rotationDegrees, transform);
        } catch (Exception e) {
            // If the detector is closed, it will throw a MlKitException.UNAVAILABLE. It's not
            // public in the "mlkit:vision-interfaces" artifact so we have to catch a generic
            // Exception here.
            throwables.put(detector, new RuntimeException("Failed to process the image.", e));
            // This detector is closed, but the next one might still be open. Send the image to
            // the next detector.
            detectRecursively(imageProxy, detectorIndex + 1, transform, values,
                    throwables);
            return;
        }
        mlKitTask.addOnCompleteListener(
                mExecutor,
                task -> {
                    // Record the return value / exception.
                    if (task.isCanceled()) {
                        throwables.put(detector,
                                new CancellationException("The task is canceled."));
                    } else if (task.isSuccessful()) {
                        values.put(detector, task.getResult());
                    } else {
                        throwables.put(detector, task.getException());
                    }
                    // Go to the next detector.
                    detectRecursively(imageProxy, detectorIndex + 1, transform, values,
                            throwables);
                });
    }

    /**
     * {@inheritDoc}
     */
    @NonNull
    @Override
    public final Size getDefaultTargetResolution() {
        Size size = DEFAULT_SIZE;
        for (Detector<?> detector : mDetectors) {
            Size detectorSize = getTargetResolution(detector.getDetectorType());
            if (detectorSize.getHeight() * detectorSize.getWidth()
                    > size.getWidth() * size.getHeight()) {
                size = detectorSize;
            }
        }
        return size;
    }

    /**
     * Gets the recommended resolution for the given {@code Detector} type.
     *
     * <p> The resolution can be found on ML Kit's DAC page.
     */
    @NonNull
    private Size getTargetResolution(int detectorType) {
        switch (detectorType) {
            case TYPE_BARCODE_SCANNING:
            case TYPE_TEXT_RECOGNITION:
                return new Size(1280, 720);
            case TYPE_POSE_DETECTION:
                return new Size(1280, 720);
            default:
                return DEFAULT_SIZE;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final int getTargetCoordinateSystem() {
        return mTargetCoordinateSystem;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public final void updateTransform(@Nullable Matrix matrix) {
        if (matrix == null) {
            mSensorToTarget = null;
        } else {
            mSensorToTarget = new Matrix(matrix);
        }
    }

    /**
     * The aggregated MLKit result of a camera frame.
     */
    public static final class Result {

        @NonNull
        private final Map<Detector<?>, Object> mValues;
        @NonNull
        private final Map<Detector<?>, Throwable> mThrowables;
        private final long mTimestamp;

        public Result(@NonNull Map<Detector<?>, Object> values, long timestamp,
                      @NonNull Map<Detector<?>, Throwable> throwables) {
            mValues = values;
            mThrowables = throwables;
            mTimestamp = timestamp;
        }

        /**
         * Get the analysis result for the given ML Kit {@code Detector}.
         *
         * <p>Returns {@code null} if the detection is unsuccessful.
         *
         * <p>This method and {@link #getThrowable} may both return {@code null}. For example,
         * when a face detector processes a frame successfully and does not detect any faces.
         * However, if {@link #getThrowable} returns a non-null {@link Throwable}, then this
         * method will always return {@code null}.
         *
         * @param detector has to be one of the {@code Detector}s provided in
         *                 {@link MlKitAnalyzer}'s constructor.
         */
        @Nullable
        @SuppressWarnings("unchecked")
        public <T> T getValue(@NonNull Detector<T> detector) {
            checkDetectorExists(detector);
            return (T) mValues.get(detector);
        }

        /**
         * The error returned from the given {@code Detector}.
         *
         * <p>Returns {@code null} if the {@code Detector} finishes without exceptions.
         *
         * @param detector has to be one of the {@code Detector}s provided in
         *                 {@link MlKitAnalyzer}'s constructor.
         */
        @Nullable
        public Throwable getThrowable(@NonNull Detector<?> detector) {
            checkDetectorExists(detector);
            return mThrowables.get(detector);
        }

        /**
         * The timestamp of the camera frame.
         *
         * <p> The timestamp of the camera frame based on which the analysis result is produced.
         * This is the value of {@link ImageProxy#getImageInfo()#getTimestamp()}.
         */
        public long getTimestamp() {
            return mTimestamp;
        }

        private void checkDetectorExists(@NonNull Detector<?> detector) {
            checkArgument(mValues.containsKey(detector) || mThrowables.containsKey(detector),
                    "The detector does not exist");
        }
    }}