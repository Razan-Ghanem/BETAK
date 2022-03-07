package com.neurondigital.estate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.ecloud.pulltozoomview.PullToZoomScrollViewEx;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;
import com.mikepenz.iconics.view.IconicsTextView;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;

/**
 * This activity shows a single property
 */
public class SinglePropertyActivity extends AppCompatActivity {

    PullToZoomScrollViewEx scrollView;
    IconicsImageView favoriteBtn;
    Property property;
    Context context;
    Activity activity;
    TextView priceView, priceView2, statusView;
    IconicsTextView photosView;
    TextView areaView, bathsView, bedsView, roomsView;
    IconicsTextView featuresView, addressView;
    WebView descriptionView;
    ImageView propertyImage;
    private GoogleMap map;
    private Marker marker;

    TextView email, call;
    CallHelper callhelper;

    String telephone = "", email_address = "";
    String email_subject = "";

    MaterialRippleLayout tel_btn_layout;
    MaterialRippleLayout email_btn_layout;

    //slideshow
    int slideshow_current_image = 0;
    Runnable slideshowRunnable;
    Handler handler;

    public static String ITEM_KEY = "item_key";
    int ItemId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_property);
        this.context = this;
        this.activity = this;

        //only portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //setup toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // back button pressed
                finish();
            }
        });

        //initialise call helper
        callhelper = new CallHelper(this);

        //get scrollview
        scrollView = (PullToZoomScrollViewEx) findViewById(R.id.scroll_view);

        //set zoom, content and header view
        View headView = LayoutInflater.from(this).inflate(R.layout.property_head_view, null, false);
        View zoomView = LayoutInflater.from(this).inflate(R.layout.property_zoom_view, null, false);
        View contentView = LayoutInflater.from(this).inflate(R.layout.property_content_view, null, false);
        scrollView.setHeaderView(headView);
        scrollView.setZoomView(zoomView);
        scrollView.setScrollContentView(contentView);

        //set aspect ratio of header image
        DisplayMetrics localDisplayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(localDisplayMetrics);
        int mScreenWidth = localDisplayMetrics.widthPixels;
        LinearLayout.LayoutParams localObject = new LinearLayout.LayoutParams(mScreenWidth, (int) (9.0F * (mScreenWidth / 16.0F)));
        scrollView.setHeaderLayoutParams(localObject);

        //get property image element
        propertyImage = (ImageView) zoomView.findViewById(R.id.image);

        //handle image swipe/clicks
        propertyImage.setOnTouchListener(new OnSwipeTouchListener(this) {

            @Override
            public void onClick() {
                openFullScreenImage();
            }

            @Override
            public void onSwipeLeft() {
                //next image
                slideshow_current_image--;
                if (slideshow_current_image < 0)
                    slideshow_current_image = property.imageUrl.length - 1;

                //switch image
                Picasso.with(context)
                        .load(property.imageUrl[slideshow_current_image])
                        .fit()
                        .placeholder(R.drawable.loading)
                        .into(propertyImage);

            }

            @Override
            public void onSwipeRight() {

                //next image
                slideshow_current_image++;
                if (slideshow_current_image >= property.imageUrl.length)
                    slideshow_current_image = 0;

                //switch image
                Picasso.with(context)
                        .load(property.imageUrl[slideshow_current_image])
                        .fit()
                        .placeholder(R.drawable.loading)
                        .into(propertyImage);
            }
        });

        //set favorite button
        favoriteBtn = (IconicsImageView) headView.findViewById(R.id.favorite);
        favoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (property.isFavorite(context)) {
                    favoriteBtn.setIcon("faw_star_o");
                    property.setFavorite(context, false);
                } else {
                    favoriteBtn.setIcon("faw_star");
                    property.setFavorite(context, true);
                }
            }
        });

        //get property prices elements
        priceView = (TextView) contentView.findViewById(R.id.price);
        priceView2 = (TextView) contentView.findViewById(R.id.price2);

        //get property status and photo counter
        statusView = (TextView) headView.findViewById(R.id.status);
        photosView = (IconicsTextView) headView.findViewById(R.id.photos);

        //get property tags element
        bathsView = (TextView) contentView.findViewById(R.id.bathsView);
        bedsView = (TextView) contentView.findViewById(R.id.bedsView);
        roomsView = (TextView) contentView.findViewById(R.id.roomsView);
        areaView = (TextView) contentView.findViewById(R.id.areaView);

        //set description view
        descriptionView = (WebView) contentView.findViewById(R.id.descriptionView);
        descriptionView.setWebViewClient(new WebViewClient() {
            public void onPageFinished(WebView view, String url) {
            }
        });
        //enable javascript
        descriptionView.getSettings().setJavaScriptEnabled(true);

        //favorites and address views
        featuresView = (IconicsTextView) contentView.findViewById(R.id.featureView);
        addressView = (IconicsTextView) contentView.findViewById(R.id.addressView);

        //set up call
        call = (TextView) contentView.findViewById(R.id.tel);
        tel_btn_layout = (MaterialRippleLayout) contentView.findViewById(R.id.tel_btn_layout);
        tel_btn_layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //callhelper.call(1232, telephone);
                Intent callIntent = new Intent(Intent.ACTION_DIAL);
                callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
                callIntent.setData(Uri.parse("tel://" + telephone));
                startActivity(callIntent);

            }
        });
        tel_btn_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    call.setTextColor(ContextCompat.getColor(context, R.color.colorText));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    call.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                }
                return false;
            }
        });

        //set up email
        email = (TextView) contentView.findViewById(R.id.email);
        email_btn_layout = (MaterialRippleLayout) contentView.findViewById(R.id.email_btn_layout);
        email_btn_layout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("message/rfc822");
                i.putExtra(Intent.EXTRA_EMAIL, new String[]{email_address});
                i.putExtra(Intent.EXTRA_SUBJECT, email_subject);
                try {
                    startActivity(Intent.createChooser(i, getResources().getString(R.string.email_send_email)));
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(activity, getResources().getString(R.string.email_no_email), Toast.LENGTH_SHORT).show();

                }

            }
        });
        email_btn_layout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    email.setTextColor(ContextCompat.getColor(context, R.color.colorText));
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    email.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));
                }
                return false;
            }
        });

        //map
        initialiseMap();


        //out of rotation
        if (savedInstanceState != null) {
            ItemId = savedInstanceState.getInt(ITEM_KEY);
        } else {
            //get property id from intent (from deep link or prev menu)
            ItemId = Property.getPropertyIdFromIntent(getIntent(), savedInstanceState);
            System.out.println("property id:" + ItemId);
        }

        //correct id?
        if (ItemId < 0) {
            finish();
            return;
        }

        //load property
        Property.loadProperty(this, ItemId, new Property.onPropertyDownloadedListener() {
            @Override
            public void onPropertyDownloaded(Property propertyLocal) {
                propertyLocal.viewed(activity);
                property = propertyLocal;
                setTitle(property.name);
                DecimalFormat dFormat = new DecimalFormat("####,###,###");
                priceView.setText(getString(R.string.currency) + dFormat.format(property.saleprice));
                if (property.rentprice > 0)
                	priceView2.setText(String.format(getResources().getString(R.string.rent_price), getString(R.string.currency) + dFormat.format(property.rentprice)));
                bathsView.setText("" + property.bathrooms);
                bedsView.setText("" + property.bedrooms);
                roomsView.setText("" + property.rooms);
                areaView.setText("" + property.area);
                descriptionView.loadData(Functions.HTMLTemplate(property.description), "text/html; charset=utf-8", "utf-8");
                featuresView.setText(property.getFeatures(context));
                addressView.setText(property.getAddress());
                statusView.setText(property.statusName);
                setMapCoordinates(property.gpslat, property.gpslng);

                //set fav button
                refreshFavoriteBtn();

                //set image
                if (property.imageUrl != null) {
                    Picasso.with(context)
                            .load(property.imageUrl[0])
                            .fit()
                            .placeholder(R.drawable.loading)
                            .into(propertyImage);
                }


                //set up email and phone
                email_address = property.email;
                email_subject = property.name;
                telephone = property.tel;
                if (telephone.length() < 1)
                    tel_btn_layout.setVisibility(View.GONE);
                if (email_address.length() < 1)
                    email_btn_layout.setVisibility(View.GONE);

                //photos count
                photosView.setText(String.format(getResources().getString(R.string.photos_count), "" + property.imageUrl.length));
            }
        });


        //set slideshow timer
        slideshowRunnable = new Runnable() {
            @Override
            public void run() {
                //next image
                slideshow_current_image++;
                if (slideshow_current_image >= property.imageUrl.length)
                    slideshow_current_image = 0;

                //switch image
                Picasso.with(context)
                        .load(property.imageUrl[slideshow_current_image])
                        .fit()
                        .placeholder(R.drawable.loading)
                        .into(propertyImage);

                //delay
                handler.postDelayed(this, Configurations.SLIDESHOW_TIME_SECONDS * 1000);
            }
        };

        handler = new Handler();
        handler.postDelayed(slideshowRunnable, Configurations.SLIDESHOW_TIME_SECONDS * 1000);

    }

    protected void onSaveInstanceState(Bundle onOrientChange) {
        super.onSaveInstanceState(onOrientChange);
        onOrientChange.putInt(ITEM_KEY, ItemId);
    }


    /**
     * Open images in full screen mode
     */
    public void openFullScreenImage() {
        Bundle b = new Bundle();
        b.putStringArray("imageUrl", property.imageUrl);
        b.putInt("slideshow_seconds", Configurations.SLIDESHOW_TIME_SECONDS);
        Intent i = new Intent(context, FullScreenImage.class);
        i.putExtras(b);
        startActivity(i);
    }


    /**
     * Set the map coordinates
     *
     * @param lat
     * @param lng
     */
    public void setMapCoordinates(Double lat, double lng) {
        if (map == null)
            return;
        LatLng coordinate = new LatLng(lat, lng);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(coordinate)      // Sets the center of the map to Mountain View
                .zoom(13)                   // Sets the zoom
                .bearing(0)                // Sets the orientation of the camera to east
                .tilt(0)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


        marker = map.addMarker(new MarkerOptions().position(coordinate));
    }


    /**
     * Initialise mini map
     */
    private void initialiseMap() {
        if (map != null) {
            return;
        }
        ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map = googleMap;
            }
        });

        // Initialize map options. For example:
        // mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        callhelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


    /**
     * Refresh Favorite button (star).
     * Checks state from local preferences
     */
    public void refreshFavoriteBtn() {
        if (property.isFavorite(context)) {
            favoriteBtn.setIcon("faw_star");
        } else {
            favoriteBtn.setIcon("faw_star_o");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        initialiseMap();
        descriptionView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        descriptionView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        descriptionView.destroy();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.single_property_menu, menu);

        //set share icon from FontAwsome
        menu.findItem(R.id.share).setIcon(
                new IconicsDrawable(context)
                        .icon(FontAwesome.Icon.faw_share_alt)
                        .color(ContextCompat.getColor(context, R.color.md_white_1000))
                        .sizeDp(18));
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle share
        switch (item.getItemId()) {
            case R.id.share:
                if (property != null)
                    property.share(activity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
