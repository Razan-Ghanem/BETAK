package com.neurondigital.estate;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;

import com.android.vending.billing.IabBroadcastReceiver;
import com.android.vending.billing.IabHelper;
import com.android.vending.billing.IabResult;
import com.android.vending.billing.Inventory;
import com.android.vending.billing.Purchase;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.messaging.FirebaseMessaging;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.SectionDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import java.util.ArrayList;
import java.util.List;

import static com.neurondigital.estate.Configurations.PUBLIC_KEY;
import static com.neurondigital.estate.Configurations.TEST_DEVICES;

/**
 * This is the first Activity shown.
 * <p>
 * Handles the generation of the side navigation drawer, shows the main fragment and shows ads if enabled
 */

public class MainActivity extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener {

    Toolbar toolbar;
    Drawer drawer;
    Context context;
    AppCompatActivity activity;

    //ad related
    AdView ad;
    LinearLayout BackgroundLayout;
    AdvertHelper advertHelper;
    int ad_counter = 0;

    //billing
    BillingHelper billingHelper;

    //navigation drawer item identification numbers
    final int NAV_HOME = 0, NAV_FAV = 1, NAV_MORE = 3, NAV_INFO = 4, NAV_PREMIUM = 5, NAVSETTINGS = 6, NAV_FINDAGENT = 7, NAV_CATEGORIES = 100;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        Configuration configuration = getResources().getConfiguration();
//        configuration.setLayoutDirection(new Locale("fa"));
//        configuration.setLocale(new Locale("fa"));
//        getResources().updateConfiguration(configuration, getResources().getDisplayMetrics());

        setContentView(R.layout.activity_main);
        activity = this;


        this.context = this;

        // Sample AdMob app ID: ca-app-pub-3940256099942544~3347511713
        if (getResources().getString(R.string.admob_app_id).length() > 0)
            MobileAds.initialize(this, getResources().getString(R.string.admob_app_id));

        //portrait only
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //enable/disable Firebase topic sunscription
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        if (sharedPref.getBoolean("pref_enable_push_notifications", true))
            FirebaseMessaging.getInstance().subscribeToTopic(Configurations.FIREBASE_PUSH_NOTIFICATION_TOPIC);
        else
            FirebaseMessaging.getInstance().unsubscribeFromTopic(Configurations.FIREBASE_PUSH_NOTIFICATION_TOPIC);


        //set toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        //Generate the side navigation drawer
        drawer = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                //.withRootView(R.id.drawer_container)
                .withDisplayBelowStatusBar(true)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .addDrawerItems(getDrawerItems(null))
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        //On click: open the required activity or fragment
                        Intent intent;
                        switch ((int) drawerItem.getIdentifier()) {
                            case NAV_HOME:
                                changeFragment(new SearchFragment());
                                break;
                            case NAV_MORE:
                                changeFragment(new HomeFragment());
                                break;
                            case NAV_FAV:
                                changeFragment(new FavoriteFragment());
                                break;
                            case NAV_INFO:
                                changeFragment(new InfoFragment());
                                break;
                            case NAV_PREMIUM:
                                intent = new Intent(context, PremiumActivity.class);
                                startActivity(intent);
                                break;
                            case NAVSETTINGS:
                                intent = new Intent(context, SettingsActivity.class);
                                startActivity(intent);
                                break;
                            case NAV_FINDAGENT:
                                changeFragment(new AgentsFragment());
                                break;
                            default:
                                //opens the categories displayed in drawer
                                if (drawerItem.getIdentifier() > NAV_CATEGORIES) {
                                    Bundle b = new Bundle();
                                    b.putInt("Category_id", (int) (drawerItem.getIdentifier() - NAV_CATEGORIES));
                                    Fragment f = new PropertiesInCategoryFragment();
                                    f.setArguments(b);
                                    changeFragment(f);
                                }
                        }
                        drawer.closeDrawer();
                        return true;
                    }
                })
                .build();



        //initialise billing
        billingHelper = new BillingHelper(this,
                new BillingHelper.RefreshListener() {
                    @Override
                    public void onRefresh(boolean isPremium, Inventory inventory) {
                        if (isPremium) {
                            //destroy banner ad
                            if (ad != null)
                                ad.destroy();
                            if (BackgroundLayout != null)
                                BackgroundLayout.removeView(ad);

                            //remove upgrade 'Go premium' from drawer
                            drawer.removeItem(NAV_PREMIUM);

                            //remove purchase
                            invalidateOptionsMenu();
                        }
                    }
                },
                new IabHelper.OnIabPurchaseFinishedListener() {
                    public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                        System.out.println("Purchase successful " + result);
                    }
                },
                new IabHelper.OnConsumeFinishedListener() {
                    public void onConsumeFinished(Purchase purchase, IabResult result) {
                    }
                }, PUBLIC_KEY);


        // Important: Dynamically register for broadcast messages about updated purchases.
        // We register the receiver here instead of as a <receiver> in the Manifest
        // because we always call getPurchases() at startup, so therefore we can ignore
        // any broadcasts sent while the app isn't running.
        // Note: registering this listener in an Activity is a bad idea, but is done here
        // because this is a SAMPLE. Regardless, the receiver must be registered after
        // IabHelper is setup, but before first call to getPurchases().
        billingHelper.mBroadcastReceiver = new IabBroadcastReceiver(MainActivity.this);
        IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
        registerReceiver(billingHelper.mBroadcastReceiver, broadcastFilter);

        //Admob Banner and Interstitial Advert
        BackgroundLayout = (LinearLayout) findViewById(R.id.background_layout);
        if (!billingHelper.isPremium()) {
            advertHelper = new AdvertHelper(this, getResources().getString(R.string.interstitial_ad), null);
            advertHelper.initialiseInterstitialAd(TEST_DEVICES);
            AdRequest.Builder builder = new AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR);
            for (int i = 0; i < TEST_DEVICES.length; i++) {
                builder.addTestDevice(TEST_DEVICES[i]);
            }
            final AdRequest adRequest = builder.build();

            ad = (AdView) findViewById(R.id.adView);
            if (getResources().getString(R.string.banner_ad).length() > 1) {
                ad.loadAd(adRequest);
            } else {
                if (ad != null)
                    ad.destroy();
                if (BackgroundLayout != null)
                    BackgroundLayout.removeView(ad);
            }
        }


        //load categories for side menu
        Category.loadCategories(context, "", new Category.onCategoriesDownloadedListener() {
            @Override
            public void onCategoriesDownloaded(List<Category> categories) {
                refreshNavDrawer(categories);
            }
        });


    }


    /**
     * Removes all items from drawer and creates them again to refresh.
     *
     * @param categories - List of Categories
     */
    public void refreshNavDrawer(List<Category> categories) {
        drawer.removeAllItems();
        drawer.addItems(getDrawerItems(categories));
    }

    /**
     * Generates a list of Drawer items
     *
     * @param categories
     * @return
     */
    public IDrawerItem[] getDrawerItems(List<Category> categories) {
        List<IDrawerItem> drawerItems = new ArrayList<>();

        //Add Home, Favorites and Shopping list
        drawerItems.add(new PrimaryDrawerItem().withIdentifier(NAV_HOME).withName(R.string.nav_home).withIcon(FontAwesome.Icon.faw_home));
        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_FAV).withName(R.string.nav_favorites).withIcon(FontAwesome.Icon.faw_star));

        if (Configurations.DISPLAY_CATEGORIES_IN_NAVIGATION_DRAWER) {
            //Add categories and more...
            drawerItems.add(new SectionDrawerItem().withName(R.string.nav_categories));
            if (categories != null) {
                for (int i = 0; i < categories.size(); i++) {
                    if (i < Configurations.CATEGORIES_TO_SHOW_IN_NAVIGATION_DRAWER) {
                        PrimaryDrawerItem temp = new SecondaryDrawerItem().withIdentifier(NAV_CATEGORIES + categories.get(i).id).withName(categories.get(i).name);
                        drawerItems.add(temp);
                        if (categories.get(i).icon.length() > 3) {
                            String icon = "faw_" + categories.get(i).icon.substring(3, categories.get(i).icon.length());
                            temp.withIcon(FontAwesome.Icon.valueOf(icon));
                        }
                    }
                }
            }
            drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_MORE).withName(R.string.nav_categories_more).withIcon(FontAwesome.Icon.faw_ellipsis_h));
            drawerItems.add(new DividerDrawerItem());
        } else {
            //add just a categories button
            drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_MORE).withName(R.string.nav_categories).withIcon(FontAwesome.Icon.faw_bars));
        }

        //add final 4 items
        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_FINDAGENT).withName(R.string.nav_find_agent).withIcon(FontAwesome.Icon.faw_user));
        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_INFO).withName(R.string.nav_info).withIcon(FontAwesome.Icon.faw_question));
        if (Configurations.PUBLIC_KEY.length() > 0)
            drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAV_PREMIUM).withName(R.string.nav_go_premium).withIcon(FontAwesome.Icon.faw_money));
        drawerItems.add(new SecondaryDrawerItem().withIdentifier(NAVSETTINGS).withName(R.string.nav_settings).withIcon(FontAwesome.Icon.faw_cog));

        return drawerItems.toArray(new IDrawerItem[0]);
    }

    /**
     * Open interstitial Ad every couple of times. The number of clicks can be set from strings.xml
     * Doesn't display ads in premium mode.
     *
     * @return
     */
    public boolean loadInterstitial() {
        if (!billingHelper.isPremium()) {
            ad_counter++;
            if (ad_counter >= getResources().getInteger(R.integer.ad_shows_after_X_clicks)) {
                advertHelper.openInterstitialAd(new AdvertHelper.InterstitialListener() {
                    @Override
                    public void onClosed() {
                    }

                    @Override
                    public void onNotLoaded() {

                    }
                });

                ad_counter = 0;
                return true;
            }
        }
        return false;
    }

    /**
     * Change main fragment
     *
     * @param fragment
     */
    public void changeFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.mainFragment, fragment);
        transaction.commit();
    }


    /**
     * On back pressed, always go to home fragment before closing
     */
    @Override
    public void onBackPressed() {
        //if stack has items left
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            //get current fragment
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.mainFragment);

            //only close if in HomeFragment else go to HomeFragment
            if (fragment instanceof SearchFragment) {
                finish();
            } else {
                changeFragment(new SearchFragment());
            }
        } else {
            super.onBackPressed();
        }
    }


    /**
     * Broadcast receiver for billing
     */
    @Override
    public void receivedBroadcast() {
        billingHelper.receivedBroadcast();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //back from billing
        if (billingHelper.onActivityResult(requestCode, resultCode, data)) {

        }

    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onPause() {
        if (advertHelper != null)
            advertHelper.onPause();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (advertHelper != null)
            advertHelper.onResume();
        if (billingHelper != null)
            billingHelper.refreshInventory();
    }

    @Override
    public void onDestroy() {
        if (advertHelper != null)
            advertHelper.onDestroy();
        super.onDestroy();
        if (billingHelper.mBroadcastReceiver != null) {
            unregisterReceiver(billingHelper.mBroadcastReceiver);
        }
        if (billingHelper != null)
            billingHelper.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

}
