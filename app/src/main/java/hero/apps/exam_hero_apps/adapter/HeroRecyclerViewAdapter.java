package hero.apps.exam_hero_apps.adapter;


import android.app.Activity;
import android.app.FragmentManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import hero.apps.exam_hero_apps.R;
import hero.apps.exam_hero_apps.data.Hero;
import hero.apps.exam_hero_apps.database.HeroSqlite;
import hero.apps.exam_hero_apps.dialog.ImageZoomDialog;
import hero.apps.exam_hero_apps.global.AppContext;
import hero.apps.exam_hero_apps.global.BounceInterpolator;
import hero.apps.exam_hero_apps.global.Global;
import hero.apps.exam_hero_apps.thread.LoadingUrlThread;

public class HeroRecyclerViewAdapter extends RecyclerView.Adapter<HeroRecyclerViewAdapter.HeroRecyclerViewHolder> {

    private static final String delimiter = ", ";
    private static final String TAG_IMAGE_ZOOM_DIALOG = "ImageZoomDialog";
    private final Activity activity;
    private final LayoutInflater layoutInflater;
    private final List<Hero> heroList;
    private final HeroSqlite heroSqlite;
    private final BounceInterpolator interpolatorHeart;

    /**
     * when click on imageView , open dialog with the image and can zoom it
     */
    private View.OnClickListener imageClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            FragmentManager fragmentManager = activity.getFragmentManager();
            if (fragmentManager.findFragmentByTag(TAG_IMAGE_ZOOM_DIALOG) == null) {
                Hero hero = ((Hero) v.getTag());
                ImageZoomDialog imageZoomDialog = new ImageZoomDialog();
                Bundle argumentsBundle = new Bundle();
                argumentsBundle.putString(ImageZoomDialog.KEY_TITLE, hero.getName());
                imageZoomDialog.setArguments(argumentsBundle);
                imageZoomDialog.show(fragmentManager, TAG_IMAGE_ZOOM_DIALOG);
            }


        }
    };
    /**
     * update the hero clicked, if is already my favorite so cancel
     * else make favorite and replace with the last favorite
     */
    private View.OnClickListener changeFavoriteHeroOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            HeroRecyclerViewHolder holder = ((HeroRecyclerViewHolder) v.getTag());

            int currentPosition = holder.getAdapterPosition();

            Hero newFavoriteHero = heroList.get(currentPosition);
            if (newFavoriteHero.isFavoriteHero()) {
                newFavoriteHero.removeFavorite();
            } else {
                newFavoriteHero.makeFavorite();
            }
        }
    };


    public HeroRecyclerViewAdapter(Activity activity, List<Hero> heroList) {
        this.activity = activity;
        this.heroList = heroList;
        heroSqlite = new HeroSqlite(activity);
        layoutInflater = LayoutInflater.from(activity);
        interpolatorHeart = new BounceInterpolator(0.5, 25);
    }

    @NonNull
    @Override
    public HeroRecyclerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View rowView = layoutInflater.inflate(R.layout.row_hero_details, parent, false);
        rowView.setOnClickListener(changeFavoriteHeroOnClickListener);
        HeroRecyclerViewHolder holder = new HeroRecyclerViewHolder(rowView);
        holder.heroImage.setOnClickListener(imageClickListener);
        return holder;
    }


    @Override
    public void onBindViewHolder(@NonNull final HeroRecyclerViewHolder holder, int position) {
        Hero currentHero = heroList.get(position);
        holder.rowView.setTag(holder);
        holder.heroImage.setTag(currentHero);
        Bitmap bitmap = currentHero.getImageBitmap();

        // If not have image, download and save the image
            if (bitmap == null) {
                Global.NetworkingUtils.insertImageFromUrl(holder.heroImage, null, currentHero.getImageUrl(), AppContext.getDefaultResImageHero(), currentHero, new LoadingUrlThread.OnDownloadBitmapListener<Hero>() {
                    @Override
                    public void OnDownloadBitmap(@NonNull Bitmap bitmap, Hero tag) {
                        tag.setImageBitmap(bitmap);
                        heroSqlite.insertImage(tag);
                    }
                });
                holder.heroImage.setImageResource(AppContext.getDefaultResImageHero());
            } else {
                holder.heroImage.setImageBitmap(bitmap);
            }


        holder.heroNameTextView.setText(currentHero.getName());
        String abilities = Global.arrayToString(currentHero.getAbilitiesArray(), delimiter);
        holder.heroAbilitiesTextView.setText(abilities);


        //id is my hero so visible the heart
        if (currentHero.isFavoriteHero()) {
            Global.fadeView(0, 1, holder.favoriteHeroImage, 400);
            holder.favoriteHeroImage.setVisibility(View.VISIBLE);
            Global.setAnimationClicked(holder.favoriteHeroImage, interpolatorHeart,1100);

        } else {
            holder.favoriteHeroImage.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return heroList.size();
    }


    class HeroRecyclerViewHolder extends RecyclerView.ViewHolder {

        private final ImageView heroImage;
        private final TextView heroNameTextView;
        private final TextView heroAbilitiesTextView;
        private final ImageView favoriteHeroImage;
        private final View rowView;

        HeroRecyclerViewHolder(View rowView) {
            super(rowView);
            this.rowView = rowView;
            heroImage = itemView.findViewById(R.id.hero_image);
            heroNameTextView = itemView.findViewById(R.id.hero_name_text_view);
            heroAbilitiesTextView = itemView.findViewById(R.id.hero_abilities_text_view);
            favoriteHeroImage = itemView.findViewById(R.id.favorite_hero_image);
        }
    }
}
