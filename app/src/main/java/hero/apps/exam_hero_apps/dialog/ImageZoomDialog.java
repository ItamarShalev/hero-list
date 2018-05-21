package hero.apps.exam_hero_apps.dialog;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;



import hero.apps.exam_hero_apps.R;
import hero.apps.exam_hero_apps.global.AppContext;
import hero.apps.exam_hero_apps.global.BounceInterpolator;
import hero.apps.exam_hero_apps.global.Global;
import hero.apps.exam_hero_apps.view.ZoomView;


/**
 * Image zoom dialog for make zoom to image
 * you need set argument with title and the title must to be name of file
 */
public class ImageZoomDialog extends DialogFragment {

    public static final String KEY_TITLE = "KEY_TITLE";
    public static final String KEY_BITMAP = "KEY_BITMAP";



    private Bitmap bitmapImage;
    private String titleImage;


    protected int styleRes = R.style.SimpleStyleDialogShow;


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog.getWindow().getAttributes() != null){
            dialog.getWindow().getAttributes().windowAnimations = styleRes;
        }
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.round_dialog);
        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_image_zoom,container,false);
    }


    @Override
    public void onViewCreated(@NonNull View parent, @Nullable Bundle savedInstanceState) {
        initVar(savedInstanceState);

        ZoomView zoomView = parent.findViewById(R.id.zoom_image_view);
        TextView titleTextView = parent.findViewById(R.id.title_text_view);
        ImageButton exitImageButton = parent.findViewById(R.id.exit_image_button);

        zoomView.setImageBitmap(bitmapImage);
        titleTextView.setText(titleImage);
        exitImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Global.setAnimationClicked(v,new BounceInterpolator(0.9,50),400);
                dismiss();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_TITLE,titleImage);
        outState.putParcelable(KEY_BITMAP,bitmapImage);
    }


    private void initVar(Bundle savedInstanceState) {
        Bundle bundle;
        if (savedInstanceState != null){
            bundle = savedInstanceState;
        }else {
            bundle = getArguments();
        }

        if (bundle == null){
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        titleImage = bundle.getString(KEY_TITLE);

        if (titleImage == null){
            throw new IllegalArgumentException("Arguments must be contains bitmap image and title image");
        }
        bitmapImage = Global.BitmapUtils.readBitmapFromFile(getActivity(),titleImage);
        if (bitmapImage == null){
            bitmapImage = AppContext.getDefaultBitmapImageHero(getActivity());
        }


    }

}
