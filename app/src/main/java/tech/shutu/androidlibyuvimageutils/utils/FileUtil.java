package tech.shutu.androidlibyuvimageutils.utils;

import android.os.Environment;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import tech.shutu.androidlibyuvimageutils.application.MyApplication;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO;

/**
 * Created by raomengyang on 15/01/2017.
 */

public class FileUtil {
    /**
     * save image to sdcard path: Pictures/MyTestImage/
     * 原始帧保存到SD卡
     */
    public static void saveYuvToSdCardStorage(byte[] imageData) {
        File imageFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (imageFile == null) {
            return;
        }

        try {
            FileOutputStream fos = new FileOutputStream(imageFile);
            fos.write(imageData);
            fos.close();
            Toast.makeText(MyApplication.getContext(), "Yuv file saved to path: " + imageFile.getPath(), Toast.LENGTH_LONG).show();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            SysUtil.o("File not found: " + e.getMessage());

        } catch (IOException e) {
            e.printStackTrace();
            SysUtil.o("Error accessing file: " + e.getMessage());
        }
    }

    public static File getOutputMediaFile(int type) {
        File imageFileDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyYuvImageTest");

        if (!imageFileDir.exists()) {
            if (!imageFileDir.mkdirs()) {
                SysUtil.o("can't makedir for imagefile");
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile;
        if (type == MEDIA_TYPE_IMAGE) {
            imageFile = new File(imageFileDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");
        } else if (type == MEDIA_TYPE_VIDEO) {
            imageFile = new File(imageFileDir.getPath() + File.separator +
                    "VID_" + timeStamp + ".mp4");
        } else {
            return null;
        }
        return imageFile;
    }
}
