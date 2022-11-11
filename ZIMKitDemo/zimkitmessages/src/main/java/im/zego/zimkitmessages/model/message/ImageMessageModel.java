package im.zego.zimkitmessages.model.message;

import android.text.TextUtils;

import androidx.databinding.Bindable;

import im.zego.zim.entity.ZIMImageMessage;
import im.zego.zim.entity.ZIMMessage;
import im.zego.zim.enums.ZIMMessageSentStatus;
import im.zego.zimkitmessages.utils.image.ImageSizeUtils;

public class ImageMessageModel extends ZIMKitMessageModel {

    private String fileLocalPath;
    private String fileDownloadUrl;
    private String thumbnailDownloadUrl;
    private String largeImageDownloadUrl;
    private String fileName;
    //The container width of the image display is obtained by calculating
    private int imgWidth;
    //The height of the container displayed in the picture is obtained by calculating
    private int imgHeight;

    @Override
    public void onProcessMessage(ZIMMessage message) {
        if (message instanceof ZIMImageMessage) {
            ZIMImageMessage imageMessage = (ZIMImageMessage) message;
            this.fileName = imageMessage.getFileName();
            this.fileLocalPath = imageMessage.getFileLocalPath();
            this.fileDownloadUrl = imageMessage.getFileDownloadUrl();
            this.largeImageDownloadUrl = imageMessage.getLargeImageDownloadUrl();

            boolean sentStatus = imageMessage.getSentStatus() == ZIMMessageSentStatus.FAILED || getSentStatus() == ZIMMessageSentStatus.SENDING;
            this.thumbnailDownloadUrl = sentStatus ? imageMessage.getFileLocalPath() : imageMessage.getThumbnailDownloadUrl();

            int imageWidth = imageMessage.getThumbnailWidth();
            int imageHeight = imageMessage.getThumbnailHeight();
            ImageSizeUtils.ImageSize imageSize = ImageSizeUtils.getImageConSize(imageWidth, imageHeight);
            this.imgWidth = imageSize.imgConWidth;
            this.imgHeight = imageSize.imgConHeight;
        }
    }

    @Bindable
    public String getFileLocalPath() {
        return fileLocalPath;
    }

    public void setFileLocalPath(String fileLocalPath) {
        this.fileLocalPath = fileLocalPath;
    }

    @Bindable
    public String getFileDownloadUrl() {
        return fileDownloadUrl;
    }

    public void setFileDownloadUrl(String fileDownloadUrl) {
        this.fileDownloadUrl = fileDownloadUrl;
    }

    @Bindable
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Bindable
    public String getThumbnailDownloadUrl() {
        return !TextUtils.isEmpty(thumbnailDownloadUrl) ? thumbnailDownloadUrl : fileLocalPath;
    }

    public void setThumbnailDownloadUrl(String thumbnailDownloadUrl) {
        this.thumbnailDownloadUrl = thumbnailDownloadUrl;
    }

    @Bindable
    public String getLargeImageDownloadUrl() {
        return largeImageDownloadUrl;
    }

    public void setLargeImageDownloadUrl(String largeImageDownloadUrl) {
        this.largeImageDownloadUrl = largeImageDownloadUrl;
    }

    @Bindable
    public int getImgWidth() {
        return imgWidth;
    }

    public void setImgWidth(int imgWidth) {
        this.imgWidth = imgWidth;
    }

    @Bindable
    public int getImgHeight() {
        return imgHeight;
    }

    public void setImgHeight(int imgHeight) {
        this.imgHeight = imgHeight;
    }

}
