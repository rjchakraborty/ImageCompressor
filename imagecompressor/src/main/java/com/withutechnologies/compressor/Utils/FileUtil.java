package com.withutechnologies.compressor.Utils;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

/**
 * Created by RJ Chakraborty on 29-05-2019.
 */

public class FileUtil {



    public static String getFileName(String path) {
        return path.substring(path.lastIndexOf("/") + 1);
    }

    public static boolean checkIfNull(String value) {
        return value != null && !TextUtils.isEmpty(value) && !value.equalsIgnoreCase("null");
    }


    public static File getOutputMediaFile(Context context, String filename) {

        File mediaStorageDir = new File(context.getFilesDir() +  File.separator + context.getDir("ImageUtilities", Context.MODE_PRIVATE) + File.separator);

        // Create the storage directory if it does not exist
        if (mediaStorageDir != null && !mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        File mediaFile = null;
        if (mediaStorageDir != null) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator + filename);
        }

        return mediaFile;
    }


}
