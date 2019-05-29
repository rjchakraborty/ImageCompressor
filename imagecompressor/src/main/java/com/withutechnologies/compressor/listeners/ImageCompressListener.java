package com.withutechnologies.compressor.listeners;

import java.io.File;

/**
 * Created by user on 05-04-2018.
 */

public interface ImageCompressListener {
    void onImageCompressed(byte[] bytes, File file);
}
