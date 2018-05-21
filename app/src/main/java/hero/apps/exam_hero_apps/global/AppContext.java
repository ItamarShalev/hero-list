package hero.apps.exam_hero_apps.global;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.design.widget.Snackbar;
import android.util.Log;


import org.json.JSONObject;

import java.util.List;

import hero.apps.exam_hero_apps.R;
import hero.apps.exam_hero_apps.data.Hero;
import hero.apps.exam_hero_apps.thread.CheckingObjectUrlThread;


/**
 * App context is class for static function for this app
 */
public class AppContext {


    private static final String JSON_URL = "https://heroapps.co.il/employee-tests/android/androidexam.json";

    private static final String TAG_NAME_FAVORITE_HERO = "favorite_hero";
    private static final String TAG_LAST_JSON = "TAG_LAST_JSON";

    public static int getDefaultResImageHero(){
        return R.drawable.ic_launcher_background;
    }
    public static Bitmap getDefaultBitmapImageHero(Context context){
        return Global.BitmapUtils.getBitmapFromRes(context, getDefaultResImageHero());
    }

    /**
     * This method handle exception throws , *Only example* for now she only print error on logcat
     * Usually she send to server and the developer can handle problems
     * @param e The exception cached
     */
    public static void catchException(Context context,Class mClass,Exception e) {
        Log.e(mClass.getName(),e.getMessage());
        e.getStackTrace();
    }

    /**
     * this function create new thread , and this thread get new object from json from server
     * @param heroes the list you want equals and get only new object from json, nullable if you want get all object from json
     * @param newHeroListListener listener when finish with new objects, when finish without new objects and when error
     * @param tag Some object you want get after the process finish
     */
    public static void updateHeroList(Context context,List<Hero> heroes, final CheckingObjectUrlThread.OnNewObjectListListener newHeroListListener, Snackbar tag) {
        CheckingObjectUrlThread<Hero, Snackbar> checkingObjectUrlThread = new CheckingObjectUrlThread<Hero, Snackbar> (
                JSON_URL,
                getLastJson(context),
                heroes,
                newHeroListListener,
                new CheckingObjectUrlThread.ParseJsonToObject<Hero>() {
                    @Override
                    public Hero getObjectFromJson(JSONObject jsonObject) {
                        return new Hero(jsonObject);
                    }
                },
                tag);
        checkingObjectUrlThread.start();

    }


    /**
     * @param name the name of favorite hero
     */
    public static void updateFavoriteHero(Context context,String name){
        writeString(context,TAG_NAME_FAVORITE_HERO,name);
    }

    public static String getNameFavoriteHero(Context context){
       return readString(context,TAG_NAME_FAVORITE_HERO);
    }


    public static void removeFavoriteHero(Context context){
        remove(context,TAG_NAME_FAVORITE_HERO);
    }



    /**
     @return  The last json you get from the server
     */
    public static String getLastJson(Context context){
        return readString(context,TAG_LAST_JSON);
    }

    /**
     * @param json The last json you get from the server
     */
    public static void updateJson(Context context,String json){
        writeString(context,TAG_LAST_JSON,json);
    }


    /**
     * @param context for get SharedPreferences
     * @param tag the tag and name directory you want read
     * @param str the string you want put
     */
    private static void writeString(Context context,String tag, String str){
        SharedPreferences sharedPreferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(tag,str);
        editor.apply();
    }

    /**
     * @return string from the SharedPreferences with #tag for get the string
     */
    private static String readString(Context context,String tag){
        SharedPreferences sharedPreferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
        return sharedPreferences.getString(tag,null);
    }


    /**
     * @param tag this tag removed
     */
    private static void remove(Context context,String tag){
        SharedPreferences sharedPreferences = context.getSharedPreferences(tag, Context.MODE_PRIVATE);
        sharedPreferences.edit().remove(tag).apply();
    }


}
