package im.zego.zimkitmessages.utils;

import android.text.TextUtils;

import im.zego.zimkitcommon.utils.ZIMKitFileUtils;
import im.zego.zimkitmessages.R;

public class FileIconUtils {

    /**
     * File Suffix Name
     */
    private static final String[] excelArray = {"xlsx", "xlsm", "xlsb", "xltx", "xltm", "xls", "xlt", "xls", "xml", "xlr", "xlw", "xla", "xlam"};
    private static final String[] zipArray = {"rar", "zip", "arj", "gz", "arj", "z"};
    private static final String[] wordArray = {"doc", "docx", "rtf", "dot", "html", "tmp", "wps"};
    private static final String[] pptArray = {"ppt", "pptx", "pptm"};
    private static final String[] pdfArray = {"pdf"};
    private static final String[] txtArray = {"txt"};
    private static final String[] videoArray = {"mp4", "m4v", "mov", "qt", "avi", "flv", "wmv", "asf", "mpeg", "mpg", "vob", "mkv", "asf", "rm", "rmvb", "vob", "ts", "dat", "3gp", "3gpp", "3g2", "3gpp2", "webm"};
    private static final String[] audioArray = {"mp3", "wma", "wav", "mid", "ape", "flac", "ape", "alac", "m4a"};
    private static final String[] imageArray = {"tiff", "heif", "heic", "jpg", "jpeg", "png", "gif", "bmp", "webp"};

    public static int queryFileIcon(String path) {
        if (TextUtils.isEmpty(path)) {
            return R.mipmap.message_ic_file_unsure;
        }
        return queryFileIcon(path, R.mipmap.message_ic_file_unsure);
    }

    /**
     * Type of query file
     *
     * @param path
     * @return
     */
    public static int queryFileIcon(String path, int defaultIconId) {
        int iconId = compareEndWith(path, R.mipmap.message_ic_file_excel, excelArray);
        if (iconId == 0) {
            iconId = compareEndWith(path, R.mipmap.message_ic_file_compressed, zipArray);
        }
        if (iconId == 0) {
            iconId = compareEndWith(path, R.mipmap.message_ic_file_word, wordArray);
        }
        if (iconId == 0) {
            iconId = compareEndWith(path, R.mipmap.message_ic_file_ppt, pptArray);
        }
        if (iconId == 0) {
            iconId = compareEndWith(path, R.mipmap.message_ic_file_pdf, pdfArray);
        }
        if (iconId == 0) {
            iconId = compareEndWith(path, R.mipmap.message_ic_file_txt, txtArray);
        }
        if (iconId == 0) {
            iconId = compareEndWith(path, R.mipmap.message_ic_file_audio, audioArray);
        }
        if (iconId == 0) {
            iconId = compareEndWith(path, R.mipmap.message_ic_file_video, videoArray);
        }
        if (iconId == 0) {
            iconId = compareEndWith(path, R.mipmap.message_ic_file_image, imageArray);
        }
        if (iconId == 0) {
            // Use default icon
            iconId = defaultIconId;
        }
        return iconId;
    }

    private static int compareEndWith(String path, int iconId, String[] endWithArray) {
        String pathSuffix = ZIMKitFileUtils.getFileSuffix(path);
        for (String s : endWithArray) {
            if (pathSuffix.equals(s)) {
                return iconId;
            }
        }
        return 0;
    }

}
