package com.zegocloud.zimkit.components.album.internal.entity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.zegocloud.zimkit.components.album.internal.loader.AlbumLoader;
import com.zegocloud.zimkit.R;

public class Album implements Parcelable {

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Nullable
        @Override
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
    public static final String ALBUM_ID_ALL = String.valueOf(-1);
    public static final String ALBUM_NAME_ALL = "All";

    private final String mId;
    private final Uri mCoverUri;
    private final String mDisplayName;
    private long mCount;

    public Album(String id, Uri coverUri, String albumName, long count) {
        mId = id;
        mCoverUri = coverUri;
        mDisplayName = albumName;
        mCount = count;
    }

    private Album(Parcel source) {
        mId = source.readString();
        mCoverUri = source.readParcelable(Uri.class.getClassLoader());
        mDisplayName = source.readString();
        mCount = source.readLong();
    }

    /**
     * Constructs a new {@link Album} entity from the {@link Cursor}. This method is not responsible for managing cursor
     * resource, such as close, iterate, and so on.
     */
    public static Album valueOf(Cursor cursor) {
        int columnIndex = cursor.getColumnIndex(AlbumLoader.COLUMN_URI);
        int columnIndex1 = cursor.getColumnIndex("bucket_id");
        int columnIndex2 = cursor.getColumnIndex("bucket_display_name");
        int columnIndex3 = cursor.getColumnIndex(AlbumLoader.COLUMN_COUNT);
        if (columnIndex >= 0 && columnIndex1 >= 0 && columnIndex2 >= 0 && columnIndex3 >= 0) {
            String column = cursor.getString(columnIndex);
            return new Album(cursor.getString(columnIndex1),
                Uri.parse(column != null ? column : ""),
                cursor.getString(columnIndex2),
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
        dest.writeString(mId);
        dest.writeParcelable(mCoverUri, 0);
        dest.writeString(mDisplayName);
        dest.writeLong(mCount);
    }

    public String getId() {
        return mId;
    }

    public Uri getCoverUri() {
        return mCoverUri;
    }

    public long getCount() {
        return mCount;
    }

    public void addCaptureCount() {
        mCount++;
    }

    public String getDisplayName(Context context) {
        if (isAll()) {
            return context.getString(R.string.zimkit_album_name_all);
        }
        return mDisplayName;
    }

    public boolean isAll() {
        return ALBUM_ID_ALL.equals(mId);
    }

    public boolean isEmpty() {
        return mCount == 0;
    }

}