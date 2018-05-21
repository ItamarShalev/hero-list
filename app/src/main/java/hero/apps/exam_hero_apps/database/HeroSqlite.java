package hero.apps.exam_hero_apps.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import hero.apps.exam_hero_apps.data.Hero;
import hero.apps.exam_hero_apps.global.AppContext;
import hero.apps.exam_hero_apps.global.Global;


/**
 * database for save all hero, if I dont want build A lot of code
 * It is possible because I always save the last JSON every time
 * But to a more intelligent and future form I chose to use with SQLite
 */
public class HeroSqlite extends SQLiteOpenHelper {


    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "heroes.db";
    private static final String TABLE_NAME = "heroes";

    private static final String COLUMN_HERO_NAME = "name";
    private static final String COLUMN_ABILITIES = "abilities";
    private static final String COLUMN_URL_IMAGE = "image_url";
    private static final String REGEX = ",";
    private final Context context;


    public HeroSqlite(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
        this.context = context;
    }

    private File getDirHeroImage(Hero hero) {
        return getDirHeroImage(hero.getName());
    }

    private File getDirHeroImage(String name) {
        return new File(context.getFilesDir(), name + "_image.jpg");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " ( " +
                COLUMN_HERO_NAME + " TEXT PRIMARY KEY, " +
                COLUMN_ABILITIES + " TEXT, " +
                COLUMN_URL_IMAGE + " TEXT);");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME + ";");
        onCreate(db);
    }

    private Hero[] heroArray;
    public final void insertItem(Hero... heroes) {
        heroArray = heroes;
        new Thread(new Runnable() {
            @Override
            public void run() {
                SQLiteDatabase db = null;
                try {
                    db = getWritableDatabase();
                    for (Hero hero : heroArray) {
                        if (hero != null) {
                            ContentValues contentValues = insertHeroToContentValues(hero);
                            db.insert(TABLE_NAME, null, contentValues);
                        }
                    }
                    heroArray = null;
                } catch (Exception e) {
                    AppContext.catchException(context, this.getClass(), e);


                } finally {
                    if (db != null) {
                        db.close();
                    }
                }
            }
        }).start();
    }


    /**
     * Because SQLite is limited to 1 megabyte I used with files
     */
    public final void insertImage(Hero hero) {
        if (hero == null || hero.getImageBitmap() == null) return;
        Global.BitmapUtils.saveBitmapToFile(context, hero.getImageBitmap(), hero.getName());
    }

    public final void updateItem(String name, Hero hero) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            ContentValues contentValues = insertHeroToContentValues(hero);
            db.update(TABLE_NAME, contentValues, COLUMN_HERO_NAME + "='" + name + "'", null);
        } catch (Exception e) {
            AppContext.catchException(context, this.getClass(), e);

        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public final void updateItem(Hero hero) {
        updateItem(hero.getName(), hero);
    }

    public final boolean delete(String... names) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            for (String name : names) {
                String query = "DELETE FROM " + TABLE_NAME + " WHERE " + COLUMN_HERO_NAME + " =\"" + name + "\";";
                db.execSQL(query);
            }
            return true;
        } catch (SQLException ignore) {
            return false;
        } finally {
            if (db != null) {
                db.close();
            }
        }
    }

    public final List<Hero> getAllHeros() {
        List<Hero> list = new LinkedList<>();
        SQLiteDatabase db = null;
        Cursor cursor = null;
        try {
            db = getReadableDatabase();
            String query = "SELECT * FROM " + TABLE_NAME;
            cursor = db.rawQuery(query, null, null);
            cursor.moveToFirst();
            Hero hero;
            while (!cursor.isAfterLast()) {
                hero = getObjectFromCursor(cursor);
                if (hero.isFavoriteHero()) {
                    list.add(Global.POSITION_HERO_FAVORITE, hero);
                } else {
                    list.add(hero);
                }
                cursor.moveToNext();
            }
        } catch (Exception e) {
            AppContext.catchException(context, this.getClass(), e);
        } finally {
            if (db != null) {
                db.close();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
        return list;
    }


    /**
     * make ContentValues to insert database from HeroObject
     */
    private ContentValues insertHeroToContentValues(Hero hero) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_HERO_NAME, hero.getName());
        contentValues.put(COLUMN_ABILITIES, Global.arrayToString(hero.getAbilitiesArray(), REGEX));
        contentValues.put(COLUMN_URL_IMAGE, hero.getImageUrl());
        return contentValues;
    }

    /**
     * @param cursor this cursor contains one object
     * @return new oject from cursor
     */
    private Hero getObjectFromCursor(Cursor cursor) {

        String name = cursor.getString(cursor.getColumnIndex(COLUMN_HERO_NAME));
        String imageUrl = cursor.getString(cursor.getColumnIndex(COLUMN_URL_IMAGE));
        String[] abilitiesArray = cursor.getString(cursor.getColumnIndex(COLUMN_ABILITIES)).split(REGEX);


        //get the bitmap from file
        Bitmap bitmap = Global.BitmapUtils.readBitmapFromFile(context, name);

        //get the name favorite name from sharedPreferences
        String nameFavoriteHero = AppContext.getNameFavoriteHero(context);

        boolean isFavoriteHero = nameFavoriteHero != null && nameFavoriteHero.equals(name);
        return new Hero(name, abilitiesArray, imageUrl, bitmap, isFavoriteHero);
    }
}
