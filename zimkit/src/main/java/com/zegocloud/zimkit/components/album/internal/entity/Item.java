package com.zegocloud.zimkit.components.album.internal.entity;

import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;

import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.MediaColumns;
import androidx.annotation.Nullable;

import com.zegocloud.zimkit.components.album.MimeType;

public class Item implements Parcelable {
    public static final Creator<Item> CREATOR = new Creator<Item>() {
        @Override
        @Nullable
        public Item createFromParcel(Parcel source) {
            return new Item(source);
        }

        @Override
        public Item[] newArray(int size) {
            return new Item[size];
        }
    };
    public static final long ITEM_ID_CAPTURE = -1;
    public static final String ITEM_DISPLAY_NAME_CAPTURE = "Capture";
    public final long id;
    public final String mimeType;
    public final Uri uri;
    public final long size;
    public final long duration; // only for video, in ms

    private Item(long id, String mimeType, long size, long duration) {
        this.id = id;
        this.mimeType = mimeType;
        Uri contentUri;
        if (isImage()) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (isVideo()) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            // ?
            contentUri = MediaStore.Files.getContentUri("external");
        }
        this.uri = ContentUris.withAppendedId(contentUri, id);
        this.size = size;
        this.duration = duration;
    }

    private Item(Parcel source) {
        id = source.readLong();
        mimeType = source.readString();
        uri = source.readParcelable(Uri.class.getClassLoader());
        size = source.readLong();
        duration = source.readLong();
    }

    public static Item valueOf(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(FileColumns._ID);
        int columnIndex1 = cursor.getColumnIndex(MediaColumns.MIME_TYPE);
        int columnIndex2 = cursor.getColumnIndex(MediaColumns.SIZE);
        int columnIndex3 = cursor.getColumnIndex("duration");
        if (columnIndex >= 0 && columnIndex1 >= 0 && columnIndex2 >= 0 && columnIndex3 >= 0) {
            return new Item(cursor.getLong(columnIndex),
                cursor.getString(columnIndex1),
                cursor.getLong(columnIndex2),
                cursor.getLong(columnIndex3));
        } else {
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(mimeType);
        dest.writeParcelable(uri, 0);
        dest.writeLong(size);
        dest.writeLong(duration);
    }

    public Uri getContentUri() {
        return uri;
    }

    public boolean isCapture() {
        return id == ITEM_ID_CAPTURE;
    }

    public boolean isImage() {
        return MimeType.isImage(mimeType);
    }

    public boolean isGif() {
        return MimeType.isGif(mimeType);
    }

    public boolean isVideo() {
        return MimeType.isVideo(mimeType);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Item)) {
            return false;
        }

        Item other = (Item) obj;
        return id == other.id
            && (mimeType != null && mimeType.equals(other.mimeType)
            || (mimeType == null && other.mimeType == null))
            && (uri != null && uri.equals(other.uri)
            || (uri == null && other.uri == null))
            && size == other.size
            && duration == other.duration;
    }

    @Override
    public int hashCode() {
        int result = 1;
        result = 31 * result + Long.valueOf(id).hashCode();
        if (mimeType != null) {
            result = 31 * result + mimeType.hashCode();
        }
        result = 31 * result + uri.hashCode();
        result = 31 * result + Long.valueOf(size).hashCode();
        result = 31 * result + Long.valueOf(duration).hashCode();
        return result;
    }
}
