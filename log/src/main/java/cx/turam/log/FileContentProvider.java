package cx.turam.log;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.os.Process;

import java.io.File;
import java.io.FileNotFoundException;

public class FileContentProvider extends ContentProvider {
    @Override
    public String getType(Uri uri) {
        String mimetype = uri.getQuery();
        return mimetype == null ? "" : mimetype;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        if (Process.myUid() != Binder.getCallingUid()) {
            throw new SecurityException("Permission denied mfm");
        } else if ("r".equals(mode)) {
            return ParcelFileDescriptor.open(new File(uri.getPath()), 268435456);
        } else {
            throw new FileNotFoundException("mfm Bad mode for " + uri + ": " + mode);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }
}
