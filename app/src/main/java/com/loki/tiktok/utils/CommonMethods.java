

package com.loki.tiktok.utils;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CommonMethods {

    private static final String tagName = CommonMethods.class.getSimpleName();

    //write intent data into file
    public static File writeIntoFile(Context context, Intent data, File file) {

        AssetFileDescriptor videoAsset = null;
        try {
            videoAsset = context.getContentResolver().openAssetFileDescriptor(Objects.requireNonNull(data.getData()), "r");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        FileInputStream in;
        try {
            in = Objects.requireNonNull(videoAsset).createInputStream();

            OutputStream out;
            out = new FileOutputStream(file);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;

            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }

            in.close();
            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    //copy file from one source file to destination file
    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!Objects.requireNonNull(destFile.getParentFile()).exists())
            //noinspection ResultOfMethodCallIgnored
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            //noinspection ResultOfMethodCallIgnored
            destFile.createNewFile();
        }

        try (FileChannel source = new FileInputStream(sourceFile).getChannel(); FileChannel destination = new FileOutputStream(destFile).getChannel()) {
            destination.transferFrom(source, 0, source.size());
        }
    }

    //get video duration in seconds
    public static long convertDurationInSec(long duration) {
        return (TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
    }

    //get video duration in minutes
    public static long convertDurationInMin(long duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        Log.v(tagName, "min: " + minutes);

        if (minutes > 0) {
            return minutes;
        } else {
            return 0;
        }
    }

    //get video duration in minutes & seconds
    public static String convertDuration(long duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        Log.v(tagName, "min: " + minutes);

        if (minutes > 0) {
            return minutes + "";
        } else {
            return "00:" + (TimeUnit.MILLISECONDS.toSeconds(duration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
        }
    }

    //get video duration based on uri
    public static int getMediaDuration(Context context, Uri uriOfFile) {
        MediaPlayer mp = MediaPlayer.create(context, uriOfFile);
        return mp.getDuration();
    }

    //get file extension based on file path
    public static String getFileExtension(String filePath) {
        String extension = filePath.substring(filePath.lastIndexOf("."));
        Log.v(tagName, "extension: " + extension);
        return extension;
    }
}
