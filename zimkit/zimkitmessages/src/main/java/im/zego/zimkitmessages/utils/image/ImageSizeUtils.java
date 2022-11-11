package im.zego.zimkitmessages.utils.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.IOException;
import java.math.BigDecimal;

import im.zego.zimkitcommon.ZIMKitManager;
import im.zego.zimkitcommon.utils.ZIMKitScreenUtils;

public class ImageSizeUtils {

    public static class ImageSize {
        public int imgConWidth = 0;
        public int imgConHeight = 0;

        public ImageSize(int imgConWidth, int imgConHeight) {
            this.imgConWidth = imgConWidth;
            this.imgConHeight = imgConHeight;
        }
    }

    /**
     * Convert to the appropriate width and height for display in the interface
     *
     * @param w image wide
     * @param h image high
     * @return
     */
    public static ImageSize getImageConSize(int w, int h) {
        int mScreenWidth = ZIMKitScreenUtils.getScreenWidth(ZIMKitManager.share().getApplication());

        try {
            int maxWH = (int) div(mScreenWidth, 2, 4);
            int minWH = (int) div(mScreenWidth, 4, 4);

            if (w <= 0 && h <= 0) {
                return new ImageSize(maxWH, maxWH);
            }

            if (w > h) {
                h = mul(div(h, w, 4), maxWH);
                h = Math.max(h, minWH);
                w = maxWH;
            } else {
                w = mul(div(w, h, 4), maxWH);
                w = Math.max(w, minWH);
                h = maxWH;
            }

            return new ImageSize(w, h);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ImageSize(w, h);

    }


    /**
     * Provides mul methods for exact multiplication operations
     *
     * @param value1 Multiplied
     * @param value2 Multiplier
     * @return The product of two parameters
     */
    public static int mul(double value1, double value2) {
        BigDecimal b1 = new BigDecimal(Double.valueOf(value1));
        BigDecimal b2 = new BigDecimal(Double.valueOf(value2));
        return b1.multiply(b2).intValue();
    }

    /**
     * Provide exact division method div
     *
     * @param value1 Divided number
     * @param value2 Divisor
     * @param scale  Precise range
     * @return The quotient of the two parameters
     * @throws IllegalAccessException
     */
    public static double div(double value1, double value2, int scale) throws IllegalAccessException {
        //If the exact range is less than 0, an exception message is thrown
        if (scale < 0) {
            throw new IllegalAccessException("accuracy cannot be less than 0");
        }
        BigDecimal b1 = new BigDecimal(Double.toString(value1));
        BigDecimal b2 = new BigDecimal(Double.toString(value2));
        //The default retention of two will be an error, here set to retain 4 decimal places
        return b1.divide(b2, scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }


    public static int[] getImageSize(String path) {
        int size[] = new int[2];
        try {
            BitmapFactory.Options onlyBoundsOptions = new BitmapFactory.Options();
            onlyBoundsOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, onlyBoundsOptions);
            int originalWidth = onlyBoundsOptions.outWidth;
            int originalHeight = onlyBoundsOptions.outHeight;

            int degree = getBitmapDegree(path);
            if (degree == 0) {
                size[0] = originalWidth;
                size[1] = originalHeight;
            } else {
                //Image resolution is 480x800 as standard
                float hh = 800f;//Here set the height to 800f
                float ww = 480f;//Here set the width to 480f
                if (degree == 90 || degree == 270) {
                    hh = 480;
                    ww = 800;
                }
                //Scaling ratio. Since it is a fixed scale scaling, only one of the height or width data is used for calculation
                int be = 1;//be=1 means no scaling
                if (originalWidth > originalHeight && originalWidth > ww) {
                    //If the width is large then scale according to the width fixed size
                    be = (int) (originalWidth / ww);
                } else if (originalWidth < originalHeight && originalHeight > hh) {
                    //If the height is high then scale according to the width fixed size
                    be = (int) (originalHeight / hh);
                }
                if (be <= 0)
                    be = 1;
                BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
                bitmapOptions.inSampleSize = be;//Set the scaling ratio
                bitmapOptions.inDither = true;//optional
                bitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;//optional
                Bitmap bitmap = BitmapFactory.decodeFile(path, bitmapOptions);
                bitmap = rotateBitmapByDegree(bitmap, degree);
                size[0] = bitmap.getWidth();
                size[1] = bitmap.getHeight();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return size;
    }

    /**
     * Read the angle of rotation of the image
     */
    public static int getBitmapDegree(String fileName) {
        int degree = 0;
        try {
            // Read the image from the specified path and get its EXIF information
            ExifInterface exifInterface = new ExifInterface(fileName);
            // Get the rotation information of the image
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /**
     * Rotate the image at an angle
     *
     * @param bm     Pictures that need to be rotated
     * @param degree Rotation angle
     * @return Picture after rotation
     */
    public static Bitmap rotateBitmapByDegree(Bitmap bm, int degree) {
        Bitmap returnBm = null;

        // Generate rotation matrix based on rotation angle
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        try {
            // Rotate the original image according to the rotation matrix and get the new image
            returnBm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
        } catch (OutOfMemoryError e) {
        }
        if (returnBm == null) {
            returnBm = bm;
        }
        if (bm != returnBm) {
            bm.recycle();
        }
        return returnBm;
    }


}
