package com.neurondigital.estate;

/**
 * Created by melvin on 25/09/2016.
 */
public class Configurations {

    /**
     * I have put a lot of effort to ensure that everything is working fine. If you find any problem
     * feel free to contact me via the Codecanyon item's comment section or send me a personal
     * message from here: https://codecanyon.net/user/neurondigital?ref=neurondigital#contact
     * <p/>
     * Thanks for buying this Template!
     * Check out my other products on CodeCanyon: http://codecanyon.net/user/neurondigital/portfolio?ref=neurondigital
     */

    
    //GENERAL---------------------------------------------------------------------------------------
    //slideshow timer
    public final static int SLIDESHOW_TIME_SECONDS = 10;

    //categories displayed in navigation bar? (true/false)
    public final static boolean DISPLAY_CATEGORIES_IN_NAVIGATION_DRAWER = false;

    //Categories to show in navigation drawer
    public final static int CATEGORIES_TO_SHOW_IN_NAVIGATION_DRAWER = 10;

    //Server URL
    public static String SERVER_URL = "https://estate.neurondigital.com/";


    //FILTER----------------------------------------------------------------------------------------
    //filter min/max prices
    public final static int[] FILTER_PROPERTY_PRICES = new int[]{10000, 20000, 40000, 50000, 75000, 100000, 150000, 200000, 300000, 500000, 1000000, 10000000};

    //filter beds/rooms/baths number
    public final static int[] FILTER_ROOMS = new int[]{0, 1, 2, 3, 4, 5, 6};


    //LIST TYPE------------------------------------------------------------------------------------
    public final static int LIST_2COLUMNS = 1, LIST_1COLUMNS = 2;
    public final static int LIST_MENU_TYPE = LIST_1COLUMNS;


    //IN-APP PURCHASE-------------------------------------------------------------------------------
    //To use the in-app purchase feature an EXTENDED LICENSE NEEDS to be purchased.
    //Only put in a public key if you purchased an extended license from
    //CODECANYON: http://codecanyon.net/user/neurondigital/portfolio?ref=neurondigital

    //OPTIONAL - Leave 'PUBLIC_KEY' empty to disable in-app purchase.
    final static String PUBLIC_KEY = "";

    // For testing use:  "android.test.purchased";  to make the purchase always accepted without an actual payment
    //Needs to be the same as the product id used in the Google Play Dashboard
    final static String SKU_PREMIUM ="your_premium_upgrade_sku_here";
    //final static String SKU_PREMIUM = "android.test.purchased";//uncomment this for testing



    //FIREBASE PUSH NOTIFICATION--------------------------------------------------------------------
    public final static String FIREBASE_PUSH_NOTIFICATION_TOPIC = "news";



    //TEST DEVICES----------------------------------------------------------------------------------
    //This is use so that ads aren't shown on test devices. Just put a list of device uuids.
    public final static String[] TEST_DEVICES = new String[]{};

}
