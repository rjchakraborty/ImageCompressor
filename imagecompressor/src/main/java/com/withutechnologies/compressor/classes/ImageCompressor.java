package com.withutechnologies.compressor.classes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;

import androidx.annotation.NonNull;

import com.withutechnologies.compressor.Utils.FileUtil;
import com.withutechnologies.compressor.configuration.ImageConfiguration;
import com.withutechnologies.compressor.listeners.ImageCompressListener;

import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

/**
 * Created by RJ Chakraborty on 29-05-2019.
 */

public class ImageCompressor {

    private ImageCompressListener mListener;
    private String imagePath, fileName;
    private Context mContext;
    float maxHeight = 2000.0f;
    float maxWidth = 1000.0f;


    private ImageCompressor(Context context, int config, String imagePath, ImageCompressListener listener) {
        this.mContext = context;
        this.mListener = listener;
        this.imagePath = imagePath;
        this.fileName = String.format(Locale.ENGLISH, "IMG_%d_%s", System.currentTimeMillis(), FileUtil.getFileName(imagePath));
        switch (config){
            case ImageConfiguration.MEDIA_QUALITY_HIGH:
                this.maxHeight = 3000.0f;
                this.maxWidth = 2500.0f;
                break;
            case ImageConfiguration.MEDIA_QUALITY_HIGHEST:
                this.maxHeight = 5000.0f;
                this.maxWidth = 4500.0f;
                break;
            case ImageConfiguration.MEDIA_QUALITY_LOW:
                this.maxHeight = 700.0f;
                this.maxWidth = 650.0f;
                break;
            case ImageConfiguration.MEDIA_QUALITY_LOWEST:
                this.maxHeight = 400.0f;
                this.maxWidth = 350.0f;
                break;
            case ImageConfiguration.MEDIA_QUALITY_MEDIUM:
                this.maxHeight = 2000.0f;
                this.maxWidth = 1500.0f;
                break;
        }
        compressImage();
    }

    private ImageCompressor(Context context, float maxHeight, float maxWidth, String imagePath, ImageCompressListener listener) {
        this.mContext = context;
        this.mListener = listener;
        this.imagePath = imagePath;
        this.fileName = String.format(Locale.ENGLISH, "IMG_%d_%s", System.currentTimeMillis(), FileUtil.getFileName(imagePath));
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
        compressImage();
    }

    public static ImageCompressor with(Context context) {
        return new ImageCompressor.Builder(context).build();
    }

    public static class Builder {

        private ImageCompressListener mListener;
        private String imagePath;
        private Context mContext;
        float maxHeight = -1.0f;
        float maxWidth = -1.0f;
        int config = ImageConfiguration.MEDIA_QUALITY_MEDIUM;

        public Builder(@NonNull Context context) {
            this.mContext = context;
        }

        public Builder setImage(String imagePath) {
            this.imagePath = imagePath;
            return this;
        }

        public Builder setConfiguration(int configuration) {
            this.config = configuration;
            return this;
        }

        public Builder setImageOutputSize(float maxHeight, float maxWidth) {
            this.maxHeight = maxHeight;
            this.maxWidth = maxWidth;
            return this;
        }

        public Builder onImageCompressed(ImageCompressListener listener) {
            this.mListener = listener;
            return this;
        }

        public ImageCompressor build() {
            if (maxHeight > 1.0f && maxWidth > 1.0f) {
                return new ImageCompressor(mContext, maxHeight, maxWidth, imagePath, mListener);
            } else {
                return new ImageCompressor(mContext, config, imagePath, mListener);
            }
        }

    }

    private void compressImage() {

        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;

        if (actualHeight > 0 && actualWidth > 0) {

            float imgRatio = (float) actualWidth / (float) actualHeight;
            float maxRatio = maxWidth / maxHeight;

//      width and height values are set maintaining the aspect ratio of the image

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }
//      setting inSampleSize value allows to load a scaled down version of the original image
            options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
//      inJustDecodeBounds set to false to load the actual bitmap
            options.inJustDecodeBounds = false;
            options.inTempStorage = new byte[16 * 1024];

            try {
//          load the bitmap from its path
                bmp = BitmapFactory.decodeFile(imagePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = null;
            if (scaledBitmap != null) {
                canvas = new Canvas(scaledBitmap);
                canvas.setMatrix(scaleMatrix);
                canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2.0f, middleY - bmp.getHeight() / 2.0f, new Paint(Paint.FILTER_BITMAP_FLAG));
            }

//      check the rotation of the image and display it properly
            ExifInterface exif;
            try {
                exif = new ExifInterface(imagePath);
                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                }
                if (scaledBitmap != null) {
                    scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            byte[] resultImage = null;
            File outputFile = FileUtil.getOutputMediaFile(mContext, fileName);
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                if (scaledBitmap != null) {
                    if (fileName.endsWith("png")) {
                        scaledBitmap.compress(Bitmap.CompressFormat.PNG, 80, baos);
                    } else if (fileName.endsWith("webp")) {
                        scaledBitmap.compress(Bitmap.CompressFormat.WEBP, 80, baos);
                    } else {
                        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, baos);
                    }
                    resultImage = baos.toByteArray();
                    baos.close();

                    if (resultImage != null && outputFile != null) {
                        FileUtils.writeByteArrayToFile(outputFile, resultImage);
                        if (mListener != null && outputFile.exists()) {
                            mListener.onImageCompressed(resultImage, outputFile);
                        }
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }


    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }
}
