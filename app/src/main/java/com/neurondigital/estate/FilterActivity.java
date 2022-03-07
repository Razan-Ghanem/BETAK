package com.neurondigital.estate;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.gc.materialdesign.views.ButtonRectangle;
import com.mikepenz.community_material_typeface_library.CommunityMaterial;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsImageView;

import java.text.DecimalFormat;
import java.util.List;

import at.markushi.ui.CircleButton;

/**
 * Shows a menu screen to allow user to filter Properties
 */
public class FilterActivity extends AppCompatActivity {

    Context context;
    Activity activity;
    RadioGroup statusRadioGroup;
    Spinner typeSpinner, minPriceSpinner, maxPriceSpinner;
    TextView bedsNumber, roomsNumber, bathsNumber;

    List<PropertyDetail> propertyStatus;
    List<PropertyDetail> propertyTypes;

    int beds_i = 0, rooms_i = 0, baths_i = 0;
    int def_status;
    int def_type;
    int def_maximum_price;
    int def_minimum_price;

    //intent keys
    public final static String KEY_STATUS = "key_status", KEY_TYPE = "key_type", KEY_MIN_PRICE = "key_min_price", KEY_MAX_PRICE = "key_max_price", KEY_ROOMS = "key_rooms", KEY_BEDS = "key_beds", KEY_BATHS = "key_baths";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        this.context = this;
        this.activity = this;
        setContentView(R.layout.activity_filter);

        //set toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // close activity on back button pressed
                finish();
            }
        });

        //get data from intent if available
        Intent intent = getIntent();
        def_status = intent.getIntExtra(FilterActivity.KEY_STATUS, 0);
        def_type = intent.getIntExtra(FilterActivity.KEY_TYPE, 0);
        def_maximum_price = intent.getIntExtra(FilterActivity.KEY_MAX_PRICE, 0);
        def_minimum_price = intent.getIntExtra(FilterActivity.KEY_MIN_PRICE, 0);
        beds_i = intent.getIntExtra(FilterActivity.KEY_BEDS, 0);
        baths_i = intent.getIntExtra(FilterActivity.KEY_BATHS, 0);
        rooms_i = intent.getIntExtra(FilterActivity.KEY_ROOMS, 0);

        //initialise
        init();
    }

    /**
     * Initialise filter options
     */
    public void init() {

        //status------------------------------------------------------------------------------------
        statusRadioGroup = (RadioGroup) findViewById(R.id.statusRadioGroup);
        final int radioBtnmargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics());

        //load property statuses
        PropertyDetail.loadPropertyDetails(context, PropertyDetail.PROPERTY_STATUSES, "", new PropertyDetail.onMultipleDownloadedListener() {
            @Override
            public void onMultipleDownloaded(List<PropertyDetail> items) {
                propertyStatus = items;

                //remove all
                statusRadioGroup.removeAllViews();

                //add any status
                RadioButton radioButtonView = new RadioButton(context);
                radioButtonView.setText(getString(R.string.filter_any));
                radioButtonView.setId(0);
                RadioGroup.LayoutParams rprms = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                rprms.setMargins(radioBtnmargin, 0, radioBtnmargin, 0);
                statusRadioGroup.addView(radioButtonView, rprms);

                //add statuses from server
                for (int i = 0; i < items.size(); i++) {
                    radioButtonView = new RadioButton(context);
                    radioButtonView.setText(items.get(i).name);
                    radioButtonView.setId(items.get(i).id);
                    rprms = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, RadioGroup.LayoutParams.WRAP_CONTENT);
                    rprms.setMargins(radioBtnmargin, 0, radioBtnmargin, 0);
                    statusRadioGroup.addView(radioButtonView, rprms);
                }
                statusRadioGroup.check(def_status);

            }
        });

        //property types----------------------------------------------------------------------------
        typeSpinner = (Spinner) findViewById(R.id.typeSpinner);
        PropertyDetail.loadPropertyDetails(context, PropertyDetail.PROPERTY_TYPES, "", new PropertyDetail.onMultipleDownloadedListener() {
            @Override
            public void onMultipleDownloaded(List<PropertyDetail> items) {
                propertyTypes = items;
                String[] propertyTypeNames = new String[propertyTypes.size() + 1];
                //any
                propertyTypeNames[0] = getString(R.string.filter_any);
                //actual property types
                for (int i = 0; i < propertyTypes.size(); i++) {
                    propertyTypeNames[i + 1] = propertyTypes.get(i).name;
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, propertyTypeNames);
                // Specify the layout to use when the list of choices appears
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                typeSpinner.setAdapter(adapter);

                for (int i = 0; i < propertyTypes.size(); i++) {
                    if (propertyTypes.get(i).id == def_type)
                        typeSpinner.setSelection(i + 1);
                }
            }
        });

        //min/max price-----------------------------------------------------------------------------
        minPriceSpinner = (Spinner) findViewById(R.id.minPriceSpinner);
        maxPriceSpinner = (Spinner) findViewById(R.id.maxPriceSpinner);

        String[] prices = new String[Configurations.FILTER_PROPERTY_PRICES.length + 1];
        DecimalFormat dFormat = new DecimalFormat("####,###,###");

        //any
        prices[0] = getString(R.string.filter_any);

        //prices
        for (int i = 0; i < Configurations.FILTER_PROPERTY_PRICES.length; i++) {
            prices[i + 1] = getResources().getString(R.string.currency) + dFormat.format(Configurations.FILTER_PROPERTY_PRICES[i]);
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_item, prices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        minPriceSpinner.setAdapter(adapter);
        maxPriceSpinner.setAdapter(adapter);

        //set default values
        for (int i = 0; i < Configurations.FILTER_PROPERTY_PRICES.length; i++) {
            if (Configurations.FILTER_PROPERTY_PRICES[i] == def_minimum_price)
                minPriceSpinner.setSelection(i + 1);
            if (Configurations.FILTER_PROPERTY_PRICES[i] == def_maximum_price)
                maxPriceSpinner.setSelection(i + 1);
        }


        //Beds--------------------------------------------------------------------------------------

        //bed icon
        IconicsImageView bedIcon = (IconicsImageView) findViewById(R.id.bedIcon);
        bedIcon.setColorRes(R.color.colorSecondaryText);
        bedIcon.setIcon(FontAwesome.Icon.faw_bed);

        //beds text
        bedsNumber = (TextView) findViewById(R.id.beds);

        //beds Plus/minus buttons
        CircleButton beds_plus = (CircleButton) findViewById(R.id.beds_plus);
        CircleButton beds_minus = (CircleButton) findViewById(R.id.beds_minus);

        beds_plus.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_plus)
                .color(ContextCompat.getColor(context, R.color.colorWhite))
                .sizeDp(14)

        );
        beds_minus.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_minus)
                .color(ContextCompat.getColor(context, R.color.colorWhite))
                .sizeDp(14)
        );
        beds_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (beds_i + 1 < Configurations.FILTER_ROOMS.length)
                    beds_i++;
                bedsNumber.setText(Configurations.FILTER_ROOMS[beds_i] + "+");
            }
        });
        beds_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (beds_i - 1 >= 0)
                    beds_i--;
                bedsNumber.setText(Configurations.FILTER_ROOMS[beds_i] + "+");
            }
        });

        //default value
        bedsNumber.setText(Configurations.FILTER_ROOMS[beds_i] + "+");


        //Rooms-------------------------------------------------------------------------------------

        //room icon
        IconicsImageView roomIcon = (IconicsImageView) findViewById(R.id.roomIcon);
        roomIcon.setColorRes(R.color.colorSecondaryText);
        roomIcon.setIcon(FontAwesome.Icon.faw_building);

        //rooms text
        roomsNumber = (TextView) findViewById(R.id.rooms);

        //rooms Plus/minus buttons
        CircleButton rooms_plus = (CircleButton) findViewById(R.id.rooms_plus);
        CircleButton rooms_minus = (CircleButton) findViewById(R.id.rooms_minus);

        rooms_plus.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_plus)
                .color(ContextCompat.getColor(context, R.color.colorWhite))
                .sizeDp(14)

        );
        rooms_minus.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_minus)
                .color(ContextCompat.getColor(context, R.color.colorWhite))
                .sizeDp(14)
        );
        rooms_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rooms_i + 1 < Configurations.FILTER_ROOMS.length)
                    rooms_i++;
                roomsNumber.setText(Configurations.FILTER_ROOMS[rooms_i] + "+");
            }
        });
        rooms_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (rooms_i - 1 >= 0)
                    rooms_i--;
                roomsNumber.setText(Configurations.FILTER_ROOMS[rooms_i] + "+");
            }
        });

        //default value
        roomsNumber.setText(Configurations.FILTER_ROOMS[rooms_i] + "+");


        //Baths-------------------------------------------------------------------------------------

        //bath icon
        IconicsImageView bathIcon = (IconicsImageView) findViewById(R.id.bathIcon);
        bathIcon.setColorRes(R.color.colorSecondaryText);
        bathIcon.setIcon(FontAwesome.Icon.faw_bath);

        //baths text
        bathsNumber = (TextView) findViewById(R.id.baths);

        //rooms Plus/minus buttons
        CircleButton baths_plus = (CircleButton) findViewById(R.id.baths_plus);
        CircleButton baths_minus = (CircleButton) findViewById(R.id.baths_minus);

        baths_plus.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_plus)
                .color(ContextCompat.getColor(context, R.color.colorWhite))
                .sizeDp(14)

        );
        baths_minus.setImageDrawable(new IconicsDrawable(this)
                .icon(CommunityMaterial.Icon.cmd_minus)
                .color(ContextCompat.getColor(context, R.color.colorWhite))
                .sizeDp(14)
        );
        baths_plus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (baths_i + 1 < Configurations.FILTER_ROOMS.length)
                    baths_i++;
                bathsNumber.setText(Configurations.FILTER_ROOMS[baths_i] + "+");
            }
        });
        baths_minus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (baths_i - 1 >= 0)
                    baths_i--;
                bathsNumber.setText(Configurations.FILTER_ROOMS[baths_i] + "+");
            }
        });

        //default value
        bathsNumber.setText(Configurations.FILTER_ROOMS[baths_i] + "+");


        //Search Button-----------------------------------------------------------------------------
        ButtonRectangle searchBtn = (ButtonRectangle) findViewById(R.id.searchBtn);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra(KEY_STATUS, statusRadioGroup.getCheckedRadioButtonId());
                if (typeSpinner.getSelectedItemPosition() > 0)
                    intent.putExtra(KEY_TYPE, propertyTypes.get(typeSpinner.getSelectedItemPosition() - 1).id);
                if (minPriceSpinner.getSelectedItemPosition() > 0)
                    intent.putExtra(KEY_MIN_PRICE, Configurations.FILTER_PROPERTY_PRICES[minPriceSpinner.getSelectedItemPosition() - 1]);
                if (maxPriceSpinner.getSelectedItemPosition() > 0)
                    intent.putExtra(KEY_MAX_PRICE, Configurations.FILTER_PROPERTY_PRICES[maxPriceSpinner.getSelectedItemPosition() - 1]);
                intent.putExtra(KEY_ROOMS, Configurations.FILTER_ROOMS[rooms_i]);
                intent.putExtra(KEY_BATHS, Configurations.FILTER_ROOMS[baths_i]);
                intent.putExtra(KEY_BEDS, Configurations.FILTER_ROOMS[beds_i]);

                setResult(RESULT_OK, intent);
                finish();
            }
        });


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.filter_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle clear button
        switch (item.getItemId()) {
            case R.id.clear:

                //set all to 0
                def_status = 0;
                def_type = 0;
                def_maximum_price = 0;
                def_minimum_price = 0;
                beds_i = 0;
                baths_i = 0;
                rooms_i = 0;

                //re-initialise
                init();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
