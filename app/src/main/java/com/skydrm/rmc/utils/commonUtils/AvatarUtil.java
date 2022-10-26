package com.skydrm.rmc.utils.commonUtils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;

import com.skydrm.rmc.DevLog;
import com.skydrm.rmc.ExecutorPools;
import com.skydrm.rmc.SkyDRMApp;
import com.skydrm.rmc.domain.Constant;
import com.skydrm.rmc.domain.UserProfileInfo;
import com.skydrm.rmc.errorHandler.GenericError;
import com.skydrm.rmc.exceptions.SessionInvalidException;
import com.skydrm.rmc.ui.interacor.UserProfileHandler;
import com.skydrm.rmc.ui.widget.avatar.AvatarPlaceholder;
import com.skydrm.rmc.ui.widget.avatar.AvatarView;
import com.skydrm.rmc.utils.FileUtils;
import com.skydrm.sdk.exception.RmsRestAPIException;
import com.skydrm.sdk.rms.RestAPI;
import com.skydrm.sdk.rms.user.IRmUser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;

import static com.skydrm.rmc.ExecutorPools.Select_Type.REGULAR_BACK_GROUND;

/**
 * Created by hhu on 12/12/2016.
 */

public class AvatarUtil {
    private static final DevLog log = new DevLog(AvatarUtil.class.getSimpleName());
    private static volatile AvatarUtil mInstance;

    private Bitmap avatarBitmap;
    private File avatarFileDir;
    private File photoFile;
    private ISavePhoto mISavePhoto;

    private AvatarUtil() {
    }

    public static AvatarUtil getInstance() {
        if (mInstance == null) {
            synchronized (AvatarUtil.class) {
                if (mInstance == null) {
                    mInstance = new AvatarUtil();
                }
            }
        }
        return mInstance;
    }

    public void setUserAvatar(final Activity activity, final AvatarView userAvatar) {
        try {
            final IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
            if (rmUser == null || userAvatar == null || activity == null) {
                return;
            }
            final String rmUserName = rmUser.getName();
            userAvatar.setImageDrawable(new AvatarPlaceholder(activity, rmUserName, 30, "skydrm.com", " "));
            if (avatarBitmap != null) {
                userAvatar.setImageBitmap(avatarBitmap);
                log.v("setUserAvatar:from memory ");
                return;
            }
            // by Osmond, wait for RMS supported !!!
//            if (checkLocalAvatarAvailable()) {
//                FileInputStream inputStream = new FileInputStream(photoFile);
//                Bitmap userAvatarBt = BitmapFactory.decodeStream(inputStream);
//                if (userAvatarBt != null) {
//                    userAvatar.setImageBitmap(userAvatarBt);
//                    avatarBitmap = userAvatarBt;
//                    log.v("setUserAvatar:from local ");
//                }
//            } else {
//                retrieveAvatarFromRMS(activity, new IRetrieveAvatar() {
//                    @Override
//                    public void retrieveSuccess(Bitmap avatar) {
//                        if (avatar == null) {
//                            log.v("retrieveSuccess:error...");
//                            return;
//                        }
//                        avatarBitmap = avatar;
//                        userAvatar.setImageBitmap(avatar);
//                        log.v("setUserAvatar:retrieve avatar from rms");
//                    }
//
//                    @Override
//                    public void retrieveFailed() {
//                        //set default avatar
//                        log.v("retrieveFailed:set default avatar");
//                        userAvatar.setImageDrawable(new AvatarPlaceholder(activity, rmUserName, 30, "skydrm.com", " "));
//                    }
//                });
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private File getAvatarFileDir() {
        File avatarDir = null;
        try {
            avatarDir = new File(SkyDRMApp.getInstance().getCommonDirs().userRoot(), "Avatar");
            if (!avatarDir.exists()) {
                avatarDir.mkdir();
            } else {
                return avatarDir;
            }
        } catch (SessionInvalidException e) {
            e.printStackTrace();
        }
        return avatarDir;
    }

//    public Bitmap getCirclarBitmap(@NonNull Bitmap bitmap) {
//
//        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
//                bitmap.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(output);
//        final int color = 0xff424242;
//        final Paint paint = new Paint();
//        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
//
//        paint.setAntiAlias(true);
//
//        paint.setColor(color);
//
//        canvas.drawCircle(bitmap.getWidth() / 2,
//                bitmap.getHeight() / 2,
//                bitmap.getWidth() / 2,
//                paint);
//
//        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
//        canvas.drawBitmap(bitmap, rect, rect, paint);
//        return output;
//    }

    public void takePhoto(Activity activity) {
        if (checkSDCardAvailable()) {
            Intent cameraIntent = new Intent("android.media.action.IMAGE_CAPTURE");
            String rootPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String name = System.currentTimeMillis() + ".jpg";
            File imageFile = new File(rootPath + "/DCIM/Camera/", name);
            if (mISavePhoto != null) {
                mISavePhoto.savePhoto(imageFile);
            }
            cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
            cameraIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 0);
            activity.startActivityForResult(cameraIntent, Constant.TAKE_PHOTO_FOR_AVATAR);
        } else {
            ToastUtil.showToast(activity, "SDCard is not available...");
        }
    }

    public void picPhoto(Activity activity) {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.addCategory(Intent.CATEGORY_OPENABLE);
        activity.startActivityForResult(photoPickerIntent, Constant.PICK_PHOTO_FOR_AVATAR);
    }

    /**
     * Save image to the SD card
     */
    public void cacheAvatar(Bitmap photoBitmap) {
        if (photoBitmap == null) {
            return;
        }
        this.avatarBitmap = photoBitmap;
        if (avatarFileDir == null) {
            avatarFileDir = getAvatarFileDir();
        }
        FileOutputStream fileOutputStream = null;
        try {
            File photoFile = new File(avatarFileDir, Constant.IMAGE_FILE_NAME);
            fileOutputStream = new FileOutputStream(photoFile);
            if (photoBitmap.compress(Bitmap.CompressFormat.JPEG, 90, fileOutputStream)) {
                fileOutputStream.flush();
            }
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
        }
    }

    public Bitmap convertToBitmap(String path, int w, int h) {
        try {
            BitmapFactory.Options opts = new BitmapFactory.Options();
            opts.inJustDecodeBounds = true;
            opts.inPreferredConfig = Bitmap.Config.ARGB_8888;
            BitmapFactory.decodeFile(path, opts);
            int width = opts.outWidth;
            int height = opts.outHeight;
            float scaleWidth = 0.f, scaleHeight = 0.f;
            if (width > w || height > h) {
                scaleWidth = ((float) width) / w;
                scaleHeight = ((float) height) / h;
            }
            opts.inJustDecodeBounds = false;
            float scale = Math.max(scaleWidth, scaleHeight);
            opts.inSampleSize = (int) scale;
            WeakReference<Bitmap> weak = new WeakReference<Bitmap>(
                    BitmapFactory.decodeFile(path, opts));
            Bitmap bMapRotate = Bitmap.createBitmap(weak.get(), 0, 0, weak
                    .get().getWidth(), weak.get().getHeight(), null, true);
            if (bMapRotate != null) {
                return bMapRotate;
            }
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap convertToBitmap(String filePath) {
        log.v("convertToBitmap: " + filePath);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, 800, 800);
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bm = BitmapFactory.decodeFile(filePath, options);
        if (bm == null) {
            log.e("convertToBitmap:bm==null");
            return null;
        }
        int degree = readPictureDegree(filePath);
        bm = rotateBitmap(bm, degree);
        Bitmap bitmap = null;
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try {
            baos = new ByteArrayOutputStream();
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            int option = 100;
            while (baos.toByteArray().length / 1024 > 50) {
                log.v("convertToBitmap: " + option);
                baos.reset();
                bm.compress(Bitmap.CompressFormat.JPEG, option, baos);
                option -= 10;
            }
            bais = new ByteArrayInputStream(baos.toByteArray());
            log.v("convertToBitmap:size " + (baos.toByteArray().length / 1024));
            bitmap = BitmapFactory.decodeStream(bais);
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                log.e(e);
            }
            try {
                if (bais != null) {
                    bais.close();
                }
            } catch (Exception e) {
                log.e(e);
            }
        }
        return bitmap;
    }

    public Bitmap getSmallBitmap(Bitmap larger) {
        Bitmap bitmap = null;
        ByteArrayOutputStream baos = null;
        ByteArrayInputStream bais = null;
        try {
            baos = new ByteArrayOutputStream();
            larger.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            int option = 100;
            while (baos.toByteArray().length / 1024 > 50) {
                log.v("convertToBitmap: " + option);
                baos.reset();
                larger.compress(Bitmap.CompressFormat.JPEG, option, baos);
                option -= 10;
            }
            bais = new ByteArrayInputStream(baos.toByteArray());
            log.v("convertToBitmap:size " + (baos.toByteArray().length / 1024));
            bitmap = BitmapFactory.decodeStream(bais);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.close();
                }
            } catch (Exception e) {
                log.e(e);
            }
            try {
                if (bais != null) {
                    bais.close();
                }
            } catch (Exception e) {
                log.e(e);
            }
        }
        return null;
    }

    public int readPictureDegree(String path) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
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

    public Bitmap rotateBitmap(Bitmap bitmap, int rotate) {
        if (bitmap == null)
            return null;

        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        // Setting post rotate to 90
        Matrix mtx = new Matrix();
        mtx.postRotate(rotate);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, true);
    }

    private int calculateInSampleSize(BitmapFactory.Options options,
                                      int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // Calculate ratios of height and width to requested height and
            // width
            final int heightRatio = Math.round((float) height
                    / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);

            // Choose the smallest ratio as inSampleSize value, this will
            // guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }
        return inSampleSize;
    }

    /**
     * @param context       used to show progress
     * @param bitmap        user avatar
     * @param iUpdateAvatar callback
     */
    public void sendAvatarToRMS(final Context context, final Bitmap bitmap, final IUpdateAvatar iUpdateAvatar) {
        class SendAvatarAsncTask extends AsyncTask<Void, Void, Boolean> {
            private ProgressDialog loadingDialog;
            private boolean flag;
            private RmsRestAPIException mRmsRestAPIException;

            private SendAvatarAsncTask(Context context) {
            }

            @Override
            protected void onPreExecute() {
                if (((Activity) context).isFinishing() || isCancelled()) {
                    return;
                }
                if (loadingDialog == null) {
                    loadingDialog = new ProgressDialog(context);
                    loadingDialog.setTitle("Please wait...");
                    loadingDialog.show();
                }
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                ByteArrayOutputStream baos = null;
                try {
                    baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 10, baos);
                    byte[] avatarBytes = baos.toByteArray();
                    log.v("sendToRMS: " + FileUtils.transparentFileSize(avatarBytes.length));
                    IRmUser rmUser = SkyDRMApp.getInstance().getSession().getRmUser();
                    SkyDRMApp.getInstance()
                            .getSession()
                            .getRmsRestAPI()
                            .getUserService(rmUser)
                            .updateUserProfile(avatarBytes, new RestAPI.IRequestCallBack<String>() {
                                @Override
                                public void onSuccess(String result) {
                                    log.v("onSuccess: " + result);
                                    flag = true;
                                }

                                @Override
                                public void onFailed(int statusCode, String errorMsg) {
                                    log.e("onFailed:--statusCode: " + statusCode + "--errorMsg:" + errorMsg);
                                    flag = false;
                                }
                            });
                } catch (RmsRestAPIException e) {
                    mRmsRestAPIException = e;
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (baos != null) {
                            baos.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return flag;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                if (loadingDialog != null && !((Activity) context).isFinishing()) {
                    loadingDialog.dismiss();
                }
                if (result) {
                    if (iUpdateAvatar != null) {
                        iUpdateAvatar.updateSuccess();
                    }
                } else {
                    GenericError.handleCommonException(context, true, mRmsRestAPIException);
                    if (iUpdateAvatar != null) {
                        iUpdateAvatar.updateFailed();
                    }
                }
            }
        }
        new SendAvatarAsncTask(context).executeOnExecutor(ExecutorPools.SelectSmartly(REGULAR_BACK_GROUND));
    }

    private void retrieveAvatarFromRMS(Context context, final IRetrieveAvatar iRetrieveAvatar) {
        log.v("retrieveAvatarFromRMS: ");
        UserProfileHandler.getDefault().retrieveUserProfileInfo(context, new UserProfileHandler.IRetrieveCallback() {
            @Override
            public void onRetrieveSuccess(UserProfileInfo info) {
                log.v("onRetrieveSuccess:UserProfileInfo ");
                iRetrieveAvatar.retrieveSuccess(info.getUserAvatar());
            }

            @Override
            public void onRetrieveFailed() {
                iRetrieveAvatar.retrieveFailed();
            }
        });
    }

    public void clearAvatarCache() {
        // clear cache in local
        if (avatarFileDir == null) {
            avatarFileDir = getAvatarFileDir();
        }
        File photoFile = new File(avatarFileDir, Constant.IMAGE_FILE_NAME);
        if (photoFile.exists() && photoFile.length() > 0) {
            photoFile.delete();
        }
        // clear cache in memory
        avatarBitmap = null;
    }

    private boolean checkLocalAvatarAvailable() {
        if (avatarFileDir == null) {
            avatarFileDir = getAvatarFileDir();
        }
        if (photoFile == null) {
            photoFile = new File(avatarFileDir, Constant.IMAGE_FILE_NAME);
        }
        return photoFile.exists() && photoFile.length() != 0;
    }

    private boolean checkSDCardAvailable() {
        final String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }

    public void setISavePhoto(ISavePhoto iSavePhoto) {
        this.mISavePhoto = iSavePhoto;
    }

    public interface IUpdateAvatar {
        void updateSuccess();

        void updateFailed();
    }

    interface IRetrieveAvatar {
        void retrieveSuccess(Bitmap avatar);

        void retrieveFailed();
    }

    public interface ISavePhoto {
        void savePhoto(File photoFile);
    }
}
