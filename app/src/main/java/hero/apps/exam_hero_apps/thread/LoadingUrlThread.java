package hero.apps.exam_hero_apps.thread;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.lang.ref.WeakReference;

import hero.apps.exam_hero_apps.global.Global;


/**
 * this class download image from url and insert to image view if error insert the #defaultRes
 */
public class LoadingUrlThread<T> extends Thread {


    private final WeakReference<ImageView> referenceImageView;
    private final WeakReference<ProgressBar> referenceProgressBar;
    private final String urlImage;
    private final OnDownloadBitmapListener<T> onDownloadBitmapListener;
    private final T tag;
    private int defaultRes;
    private Handler handler;
    Bitmap bitmap;



    /**
     * @param imageView                view to insert the image
     * @param progressBar              before start to download is show and after is dismiss
     * @param urlImage                 url of image to download
     * @param defaultRes               if error or some problem
     * @param tag                      some object you put and get after the process
     * @param onDownloadBitmapListener listener when bitmap ready or error
     */
    public LoadingUrlThread(ImageView imageView, ProgressBar progressBar, String urlImage, @DrawableRes int defaultRes, T tag, OnDownloadBitmapListener<T> onDownloadBitmapListener) {
        this.referenceImageView = new WeakReference<>(imageView);
        this.referenceProgressBar = new WeakReference<>(progressBar);
        this.urlImage = urlImage;
        this.defaultRes = defaultRes;
        this.tag = tag;
        this.onDownloadBitmapListener = onDownloadBitmapListener;
        handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void run() {
        super.run();
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (referenceProgressBar.get() != null) {
                    referenceProgressBar.get().setVisibility(View.VISIBLE);
                }
            }
        });

        try {
             bitmap = Picasso.get().load(urlImage).get();
        } catch (IOException e) {
             bitmap = null;
        }
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (referenceProgressBar.get() != null) {
                    referenceProgressBar.get().setVisibility(View.GONE);
                    referenceProgressBar.clear();
                }
                if (bitmap == null){
                    referenceImageView.get().setImageResource(defaultRes);
                    referenceImageView.clear();
                }else{
                    if (referenceImageView.get() != null){
                        referenceImageView.get().setImageBitmap(bitmap);
                        Global.fadeView(0, 1, referenceImageView.get(), 600);
                    }
                    if (onDownloadBitmapListener != null && bitmap != null) {
                        onDownloadBitmapListener.OnDownloadBitmap(bitmap, tag);
                    }

                }

            }
        });

    }



    public interface OnDownloadBitmapListener<T> {
        void OnDownloadBitmap(@NonNull Bitmap bitmap, T tag);
    }


}
