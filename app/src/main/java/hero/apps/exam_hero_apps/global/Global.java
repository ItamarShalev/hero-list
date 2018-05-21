package hero.apps.exam_hero_apps.global;


import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.design.widget.BaseTransientBottomBar;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import hero.apps.exam_hero_apps.R;
import hero.apps.exam_hero_apps.thread.LoadingUrlThread;

public class Global {


    public static final int POSITION_HERO_FAVORITE = 0;
    private static final String defaultDelimiter = " ";


    /**
     * @param view the view make animation
     * @param interpolator if null make default
     * @param duration if null default is 1000
     */
    public static void setAnimationClicked(View view,BounceInterpolator interpolator,Integer duration){
        if (interpolator == null){
            interpolator = new BounceInterpolator(0.2,15);
        }
        Animation anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.bounce);
        anim.setInterpolator(interpolator);
        anim.setDuration(duration);
        view.startAnimation(anim);
    }
    /**
     * @param objectsArray The object you want split string (Must override toString())
     * @param delimiter The delimiter you want in space
     * @return All string from the array with delimiter
     */
    public static String arrayToString(Object[] objectsArray, String delimiter) {
        if (objectsArray == null) {
            return "Null array";
        } else if (objectsArray.length == 0) {
            return "Empty array";
        }
        if (delimiter == null) {
            delimiter = defaultDelimiter;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < objectsArray.length - 1; i++) {
            stringBuilder.append(objectsArray[i].toString()).append(delimiter);
        }
        stringBuilder.append(objectsArray[objectsArray.length - 1].toString());
        return stringBuilder.toString();
    }

    /**
     * @param from fade in float points (from 0.0 to 1.0 )
     * @param to fade in float points (from 0.0 to 1.0 )
     * @param view The view want make on fade
     * @param time of animation
     */
    public static void fadeView(float from, float to, View view, long time) {
        Animation alpha = new AlphaAnimation(from, to);
        alpha.setInterpolator(new AccelerateInterpolator());
        alpha.setDuration(time);
        view.startAnimation(alpha);
    }


    /**
     * ActivityUtils for some function static for activity
     */
    public static class ActivityUtils{

        public static void setActivityFullScreen(AppCompatActivity activity) {
            hideStatusBar(activity);
            hideActionBar(activity);
        }

        public static void hideStatusBar(Activity activity) {
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
            activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }

        public static void hideActionBar(AppCompatActivity activity) {
            android.app.ActionBar actionBar = activity.getActionBar();
            if (actionBar != null) {
                actionBar.hide();
            } else {
                ActionBar compactActionBar = activity.getSupportActionBar();
                if (compactActionBar != null) {
                    compactActionBar.hide();
                }
            }

        }

        public static boolean isPortraitScreen(Context context){
            return context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT;
        }

        /**
         * @param message The message you want show
         * @param duration SnackBar duration
         */
        public static void makeAndShowSnackBar(Activity activity, String message,@BaseTransientBottomBar.Duration int duration) {
            Snackbar.make(activity.findViewById(android.R.id.content), message, duration).show();
        }


        /**
         * This snackBar show until you dismiss it
         * @param message The message you want show
         * @return The snackBar you need dismiss
         */
        public static Snackbar makeAndShowDialogProgress(Activity activity, String message) {
            Snackbar snackDialogProgress = Snackbar.make(activity.findViewById(android.R.id.content), message, Snackbar.LENGTH_INDEFINITE);
            ViewGroup contentLay = (ViewGroup) snackDialogProgress.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
            ProgressBar item = new ProgressBar(activity);
            contentLay.addView(item);
            snackDialogProgress.show();
            return snackDialogProgress;
        }
    }

    /**
     * ActivityUtils for some function static for networking
     */
    public static class NetworkingUtils{

        /**
         *
         * this function download image from url and insert to image view if error insert the #defaultRes
         * @param imageView view to insert the image
         * @param progressBar before start to download is show and after is dismiss
         * @param urlImage url of image to download
         * @param defaultRes if error or some problem
         * @param tag som object you put and get after the process
         * @param onDownloadBitmapListener listener when bitmap ready or error
         * @param <T> what you want
         */
        public static <T> void insertImageFromUrl(ImageView imageView, ProgressBar progressBar ,String urlImage,@DrawableRes int defaultRes, T tag, LoadingUrlThread.OnDownloadBitmapListener<T> onDownloadBitmapListener) {
           new LoadingUrlThread<>(imageView,progressBar, urlImage, defaultRes, tag, onDownloadBitmapListener).start();

           // new LoadingUrlAsyncTask<>(imageView,progressBar, urlImage, defaultRes, tag, onDownloadBitmapListener).execute();
        }

        /**
         * @return If there is Internet (not necessarily fast and good)
         */
        public static boolean isOnline(Context context){
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }

        /**
         * @param urlServer url for read the bytes
         * @param timeOut after this time throw #SocketTimeoutException
         * @return the bytes read from the server if error return null
         * @throws IOException some problem in server
         * @throws SocketTimeoutException internet slow and #timeOut passed
         * @throws UnknownHostException no have internet
         */
        public static byte[] getHttpGetBytes(String urlServer, int timeOut) throws IOException, SocketTimeoutException, UnknownHostException {
            byte[] response = null;
            URL url = null;
            HttpURLConnection urlConnection = null;
            InputStream inputStream = null;
            try {
                url = new URL(urlServer);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setConnectTimeout(timeOut);
                urlConnection.setRequestMethod("GET");
                urlConnection.setUseCaches(false);
                urlConnection.setRequestProperty("content-Type", "text/plain");
                urlConnection.connect();
                inputStream = urlConnection.getInputStream();
                response = new byte[urlConnection.getContentLength()];
                byte[] buffer = new byte[1024];
                int actuallyRead;
                int lastIndex = 0;
                while ((actuallyRead = inputStream.read(buffer)) != -1) {
                    for (int i = 0; i < actuallyRead; i++) {
                        response[lastIndex] = buffer[i];
                        lastIndex++;
                    }
                }
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
            return response;
        }

        /**
         * {@link #getHttpGetBytes(String, int)} (java.util.concurrent.Executor, Object[])} with
         * @return String from the bytes
         */
        public static String getHttpGetString(String urlServer, int timeOut) throws IOException, SocketTimeoutException, UnknownHostException {
            byte[] dataBytes = getHttpGetBytes(urlServer, timeOut);
            return new String(dataBytes);
        }


        public interface NetworkingListener {
            void onSuccess(String response);
            void onFaild(int typeFail);
        }

    }

    /**
     * BitmapUtils for some function static for bitmap
     */
    public static class BitmapUtils {

        /**
         * @param fileName the file name you want read
         * @return the file if error so null
         */
        private static File getFileImage(Context context, String fileName) {
            fileName = fileName.replace(" ","_");
            ContextWrapper wrapper = new ContextWrapper(context);
            File directory = wrapper.getDir("imageDir", Context.MODE_PRIVATE);
            if (!directory.exists()){
                directory.mkdirs();
            }
            File file = new File(directory, fileName + ".jpg");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    AppContext.catchException(context, Global.class, e);
                }
            }
            return file;
        }

        /**
         * @param bitmapImage for save to file, must be not null
         * @param fileName the file name you want write
         */
        public static void saveBitmapToFile(Context context,@NonNull Bitmap bitmapImage, String fileName) {
            OutputStream outputStream = null;
            try {
                outputStream = new FileOutputStream(getFileImage(context, fileName));
                bitmapImage.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            } catch (Exception e) {
                AppContext.catchException(context, Global.class, e);
            } finally {
                try {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    AppContext.catchException(context, Global.class, e);
                }
            }
        }

        /**
         * @param fileName the file name you want get
         * @return return bitmap from file, if error return null
         */
        public static Bitmap readBitmapFromFile(Context context, String fileName) {
            try {
                return BitmapFactory.decodeStream(new FileInputStream(getFileImage(context, fileName)));
            } catch (FileNotFoundException e) {
                AppContext.catchException(context, Global.class, e);
                return null;
            }
        }

        /**
         * @param bitmap the bitmap you want make to drawable, must be non null
         * @return drawable from the bitmap
         */
        public static Drawable getDrawableFromBitmap(@NonNull Context context, @NonNull Bitmap bitmap) {
            return new BitmapDrawable(context.getResources(), bitmap);
        }


        /**
         * @param res id only from drawable class
         */
        public static Bitmap getBitmapFromRes(Context context, @DrawableRes int res) {
            return BitmapFactory.decodeResource(context.getResources(), res);
        }


    }
}
