package com.zegocloud.zimkit.components.message.utils.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Environment;
import android.text.TextUtils;

import com.zegocloud.zimkit.common.utils.ZIMKitFileUtils;
import java.io.File;
import java.io.FileOutputStream;

import com.zegocloud.zimkit.services.internal.ZIMKitCore;

public class HEIFImageHelper {

    static final String suffix_jpg = ".jpg";
    static final String suffix_heif = "heif";
    static final String suffix_heic = "heic";

    /**
     * Google does not provide a unified interface, so it is recommended that applications determine whether
     * the HEIF format codec is supported by the phone system version.
     * <p>
     * Currently, Android O version MR1 supports software decoding of HEIF still images,
     * Android P supports HEIF software decoding, software encoding
     * <p>
     * Whether to support Heif format images
     *
     * @return
     */
    public static boolean supportHeif() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Determine if it is a heif image
     *
     * @param path
     * @return
     */
    public static boolean isHeif(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        String suffixStr = ZIMKitFileUtils.getFileSuffix(path);
        return suffixStr.equals(suffix_heic) || suffixStr.equals(suffix_heif);
    }

    /**
     * Convert HEIF format files (with two suffixes: .heif and .heic) to jpg
     * <p>
     * <p>
     * The conversion of HEIF format to JPEG format is mainly considered for backward compatibility.
     * Sending HEIF images from a phone that supports HEIF format to a phone that does not support this
     * format will result in the images not working if the original HEIF format is sent directly.
     * Therefore, to avoid this, consider converting HEIF format images to JPEG format before sending them.
     *
     * @param path Path of heif
     * @return
     */
    public static synchronized String heifToJpg(String path) {
        if (TextUtils.isEmpty(path)) {
            return path;
        }
        if (!isHeif(path)) {
            return path;
        }
        String tempPath = ZIMKitCore.getInstance().getApplication().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + File.separator + "images/" + System.currentTimeMillis() + "_zimkit" + suffix_jpg;
        FileOutputStream fileOutputStream = null;
        Bitmap bitmap = null;
        try {
            File f = new File(tempPath);
            if (!f.getParentFile().exists()) {// If no parent folder exists
                f.getParentFile().mkdirs();
            }

            ExifInterface localExifInterface = new ExifInterface(path);
            int rotateInt = localExifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            float rotate = 0.0F;
            if (rotateInt == 6) {
                rotate = 90.0F;
            } else if (rotateInt == 3) {
                rotate = 180.0F;
            } else if (rotateInt == 8) {
                rotate = 270.0F;
            } else {
                rotate = 0.0F;
            }

            Matrix matrix = new Matrix();
            matrix.postRotate(rotate);
            bitmap = BitmapFactory.decodeFile(path);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

            fileOutputStream = new FileOutputStream(tempPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
            fileOutputStream.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
        return tempPath;
    }

}
