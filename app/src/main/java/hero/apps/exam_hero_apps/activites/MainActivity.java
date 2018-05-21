package hero.apps.exam_hero_apps.activites;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import hero.apps.exam_hero_apps.R;
import hero.apps.exam_hero_apps.adapter.HeroRecyclerViewAdapter;
import hero.apps.exam_hero_apps.data.Hero;
import hero.apps.exam_hero_apps.database.HeroSqlite;
import hero.apps.exam_hero_apps.global.AppContext;
import hero.apps.exam_hero_apps.global.Global;
import hero.apps.exam_hero_apps.thread.CheckingObjectUrlThread;

public class MainActivity extends AppCompatActivity implements Hero.FavoriteChangeHeroListener, Hero.OnImageReadyListener {


    private static final String KEY_HERO_LIST = "KEY_HERO_LIST";
    private RecyclerView heroListRecyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private HeroRecyclerViewAdapter heroRecyclerViewAdapter;
    private AppBarLayout appBarLayout;
    private ImageView favoriteHeroImageView;
    private Toolbar toolbar;
    private List<Hero> heroList;
    private HeroSqlite heroSqlite;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Global.ActivityUtils.setActivityFullScreen(this);
        setContentView(R.layout.activity_main);
        overridePendingTransition(R.anim.scale_in, R.anim.scale_out);
        Hero.setListenerFavoriteHero(this);
        if (savedInstanceState != null) {
            Hero[] heroes = (Hero[]) savedInstanceState.getParcelableArray(KEY_HERO_LIST);
            if (heroes != null) {
                heroList = new LinkedList<>(Arrays.asList(heroes));
            }
        }
        initViews();
        setSupportActionBar(toolbar);
        collapsingToolbarLayout.setTitle("");
        initObjects();
        initRecyclerView();
    }

    /**
     * init recycler view with the adapter and layoutManager
     */
    private void initRecyclerView() {
        heroListRecyclerView.setHasFixedSize(true);
        heroListRecyclerView.setLayoutManager(layoutManager);
        heroListRecyclerView.setAdapter(heroRecyclerViewAdapter);
    }

    /**
     * init all my object, sqlite, layoutManager and adapter for recyclerView
     */
    private void initObjects() {
        heroSqlite = new HeroSqlite(this);
        if (heroList == null) {
            heroList = heroSqlite.getAllHeros();
            updateHero();
        }
        if (heroList.size() > 0) {
            heroList.get(0).addOnImageReadyListener(this);
        }
        if (Global.ActivityUtils.isPortraitScreen(this)) {
            layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        } else {
            layoutManager = new GridLayoutManager(this, 2);
        }
        heroRecyclerViewAdapter = new HeroRecyclerViewAdapter(this, heroList);
    }

    /**
     * init all my view like imageView and textView
     */
    private void initViews() {
        heroListRecyclerView = findViewById(R.id.hero_list_recycler_view);
        favoriteHeroImageView = findViewById(R.id.favorite_hero_image_view);
        collapsingToolbarLayout = findViewById(R.id.collapsing_toolbar);
        appBarLayout = findViewById(R.id.app_bar);
        toolbar = findViewById(R.id.toolbar);
    }

    /**
     * @param outState save the heroes objects without images(Too much size)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArray(KEY_HERO_LIST, heroList.toArray(new Hero[]{}));
    }


    private void updateHero() {
        AppContext.updateHeroList(this, heroList, new CheckingObjectUrlThread.OnNewObjectListListener<Hero, Snackbar>() {
            /**
             * @param newHeroList all new heroes objects
             * @param tag my snackBar I need dismiss
             * @param responseJson the json read
             * save the json and adding all new objects and refresh the recyclercView
             *
             */
            @SuppressLint("Range")
            @Override
            public void onFound(List<Hero> newHeroList, Snackbar tag, String responseJson) {
                tag.dismiss();
                Global.ActivityUtils.makeAndShowSnackBar(MainActivity.this, getString(R.string.successfully_updated), Snackbar.LENGTH_SHORT);

                AppContext.updateJson(MainActivity.this, responseJson);

                int startIndexInsert = 0;
                boolean hasHeroFavorite = Hero.getCurrentFavoriteHero() != null;
                if (hasHeroFavorite) {
                    startIndexInsert = 1;
                } else {
                    newHeroList.get(0).addOnImageReadyListener(MainActivity.this);
                }
                heroList.addAll(startIndexInsert, newHeroList);
                heroSqlite.insertItem(newHeroList.toArray(new Hero[]{}));
                heroRecyclerViewAdapter.notifyDataSetChanged();
            }

            /**
             * @param tag my snackBar I need dismiss
             * Finish searching for no new results
             */
            @SuppressLint("Range")
            @Override
            public void onFinishSearch(Snackbar tag) {
                tag.dismiss();
                //Global.makeAndShowSnackBar(MainActivity.this,getString(R.string.already_updated),Snackbar.LENGTH_SHORT);

            }

            /**
             * @param stateNetworkingError What kind of problem
             * @param tag my snackBar I need dismiss
             *  after print to screen with the problem
             */
            @SuppressLint("Range")
            @Override
            public void onError(CheckingObjectUrlThread.StateNetworkingError stateNetworkingError, Snackbar tag) {
                tag.dismiss();
                String message = getString(R.string.error);
                message += " ";
                switch (stateNetworkingError) {
                    case NO_HAVE_INTERNET:
                        message += getString(R.string.no_have_internet_please_check_on);
                        break;
                    case INTERNET_SLOW:
                        message += getString(R.string.Internet_slow_please_refresh_and_fix);
                        break;
                    case ERROR_SERVER:
                    case ERROR_JSON:
                        message += getString(R.string.Failed_from_the_server_try_again_later);
                        break;
                }
                Global.ActivityUtils.makeAndShowSnackBar(MainActivity.this, message, Snackbar.LENGTH_LONG);
                receiverToInternetOn();

            }
        }, Global.ActivityUtils.makeAndShowDialogProgress(this, getString(R.string.heroes_updated)));
    }

    /**
     * receiver to internet on and try again to download all new objects, after call, so clear and stop receiver
     */
    private void receiverToInternetOn() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Global.NetworkingUtils.isOnline(context)) {
                    updateHero();
                    unregisterReceiver(this);
                }
            }
        };
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(broadcastReceiver, intentFilter);
    }


    /**
     * @param hero must be contains bitmap of image hero
     *             set title from name, and image from bitmap
     */
    private void updateTitleInfo(Hero hero) {
        Bitmap bitmap = hero.getImageBitmap();
        appBarLayout.setExpanded(true);
        Global.fadeView(0,1,favoriteHeroImageView,600);
        favoriteHeroImageView.setImageBitmap(bitmap);
        collapsingToolbarLayout.setTitle(hero.getName());

    }

    /**
     * @param oldHeroFavorite the last favorite hero
     * @param newFavoriteHero the new favorite hero
     *                        save the new hero refresh the views items from recyclerView replace the new favorite hero to top, scroll to top
     *                        and listener when image ready
     */
    @Override
    public void onFavoriteHeroChange(Hero oldHeroFavorite, Hero newFavoriteHero) {
        AppContext.updateFavoriteHero(this, newFavoriteHero.getName());
        int previousIndex = heroList.indexOf(newFavoriteHero);

        heroRecyclerViewAdapter.notifyItemChanged(Global.POSITION_HERO_FAVORITE);

        if (previousIndex > 0) {
            heroList.remove(newFavoriteHero);
            heroList.add(Global.POSITION_HERO_FAVORITE, newFavoriteHero);

            heroRecyclerViewAdapter.notifyItemMoved(previousIndex, Global.POSITION_HERO_FAVORITE);
            heroRecyclerViewAdapter.notifyItemChanged(Global.POSITION_HERO_FAVORITE);
        }

        heroListRecyclerView.scrollToPosition(Global.POSITION_HERO_FAVORITE);
        newFavoriteHero.addOnImageReadyListener(this);
    }

    /**
     * @param oldHeroFavorite the last favorite hero
     *                        When no have more hero favorite clear the hero
     */
    @Override
    public void onRemoveFavoriteHero(Hero oldHeroFavorite) {
        AppContext.removeFavoriteHero(this);
        heroRecyclerViewAdapter.notifyItemChanged(Global.POSITION_HERO_FAVORITE);

    }

    /**
     * @param hero the hero with the bitmap ready
     *             When listener to image of hero make this
     */
    @Override
    public void onImageReady(Hero hero) {
        updateTitleInfo(hero);
    }

    /**
     * If register to broadcasts and don't stop, so stop
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
            broadcastReceiver = null;
        }
/*        for (Hero hero : heroList) {
            if (hero.getImageBitmap() != null) {
                hero.getImageBitmap().recycle();
            }
        }*/
    }
}
