package hero.apps.exam_hero_apps.data;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * This class is my data to show and make hero
 */
public class Hero implements Parcelable{

    private static WeakReference<FavoriteChangeHeroListener> listenerFavoriteHero;
    private static Hero currentFavoriteHero;
    private final String name;
    private final String[] abilitiesArray;
    private final String imageUrl;
    private Bitmap imageBitmap;
    private boolean isFavoriteHero;
    private List<WeakReference<OnImageReadyListener>> listListenerReadyImage;



    public Hero(String name, String[] abilitiesArray, String imageUrl, Bitmap imageBitmap, boolean isFavoriteHero) {
        this.name = name;
        this.abilitiesArray = abilitiesArray;
        this.imageUrl = imageUrl;
        this.imageBitmap = imageBitmap;
        if (isFavoriteHero) {
            makeFavorite();
        }
        listListenerReadyImage = new ArrayList<>();
    }

    protected Hero(Parcel in) {
        name = in.readString();
        abilitiesArray = in.createStringArray();
        imageUrl = in.readString();
        isFavoriteHero = in.readByte() != 0;
        if (isFavoriteHero){
            makeFavorite();
        }
    }


    public Hero(JSONObject jsonObject) {
        try {
            name = jsonObject.getString("title");
            JSONArray abilitiesJsonArray = jsonObject.getJSONArray("abilities");
            abilitiesArray = new String[abilitiesJsonArray.length()];
            for (int i = 0; i < abilitiesJsonArray.length(); i++) {
                abilitiesArray[i] = abilitiesJsonArray.getString(i);
            }
            imageUrl = jsonObject.getString("image");
            listListenerReadyImage = new ArrayList<>();
        } catch (JSONException e) {
            throw new RuntimeException(e.getMessage() + ", This is not hero json");
        }
    }


    //--------------------------
    //----------Listener--------
    //--------------------------




    public static final Creator<Hero> CREATOR = new Creator<Hero>() {
        @Override
        public Hero createFromParcel(Parcel in) {
            return new Hero(in);
        }

        @Override
        public Hero[] newArray(int size) {
            return new Hero[size];
        }
    };

    public static void setListenerFavoriteHero(FavoriteChangeHeroListener listenerFavoriteHero) {
        Hero.listenerFavoriteHero = new WeakReference<>(listenerFavoriteHero);
    }

    public static Hero getCurrentFavoriteHero() {
        return currentFavoriteHero;
    }


    //--------------------------
    //----------Getter----------
    //--------------------------

    public String getName() {
        return name;
    }

    public String[] getAbilitiesArray() {
        return abilitiesArray;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public Bitmap getImageBitmap() {
        return imageBitmap;
    }

    public boolean isFavoriteHero() {
        return isFavoriteHero;
    }


    //--------------------------
    //----------Setter----------
    //--------------------------

    /**
     * @param imageBitmap if is not null tell the all listeners
     */
    public void setImageBitmap(Bitmap imageBitmap) {
        this.imageBitmap = imageBitmap;
        if (imageBitmap != null) {
            WeakReference<OnImageReadyListener> referenceListenerImageReady;
            Iterator<WeakReference<OnImageReadyListener>> iterator = listListenerReadyImage.iterator();
            while (iterator.hasNext()){
                referenceListenerImageReady = iterator.next();
                if (referenceListenerImageReady.get() != null) {
                    referenceListenerImageReady.get().onImageReady(this);
                    referenceListenerImageReady.clear();
                }
                listListenerReadyImage.remove(referenceListenerImageReady);
            }

        }
    }

    /**
     * If already have Image so now else when this object be contains image
     */
    public void addOnImageReadyListener(OnImageReadyListener listenerImageReady) {
        if (imageBitmap != null) {
            listenerImageReady.onImageReady(this);
        } else {
            if (listenerImageReady != null) {
                listListenerReadyImage.add(new WeakReference<>(listenerImageReady));
            }
        }
    }




    /**
     *
     * remove if this object is favorite and tell the listener
     */

    public void removeFavorite() {
        if (this == currentFavoriteHero) {
            currentFavoriteHero.isFavoriteHero = false;
            if (listListenerReadyImage != null) {
                if (listenerFavoriteHero.get() != null) {
                    listenerFavoriteHero.get().onRemoveFavoriteHero(currentFavoriteHero);
                }
            }
            currentFavoriteHero = null;
        }
    }

    /**
     * cancel last favorite and make this object my favorite and tell the listener
     */
    public void makeFavorite() {
        if (currentFavoriteHero != null) {
            currentFavoriteHero.isFavoriteHero = false;

        }
        this.isFavoriteHero = true;
        if (listListenerReadyImage != null) {
            if (listenerFavoriteHero.get() != null) {
                listenerFavoriteHero.get().onFavoriteHeroChange(currentFavoriteHero, this);
            }
        }
        currentFavoriteHero = this;

    }


    //--------------------------
    //----------Override--------
    //--------------------------

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }




    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeStringArray(abilitiesArray);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (isFavoriteHero ? 1 : 0));
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Hero)) return false;
        if (obj == this) return true;
        Hero otherHero = ((Hero) obj);
        return this.name.equals(otherHero.name);
    }



    public interface FavoriteChangeHeroListener {
        void onFavoriteHeroChange(Hero oldHeroFavorite, Hero newFavoriteHero);

        void onRemoveFavoriteHero(Hero oldHeroFavorite);
    }

    public interface OnImageReadyListener {
        void onImageReady(Hero hero);
    }
}
