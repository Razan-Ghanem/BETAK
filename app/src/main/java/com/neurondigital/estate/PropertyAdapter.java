package com.neurondigital.estate;

import android.content.Context;
import android.graphics.Typeface;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Property Adapter to show property cards in list
 */
public class PropertyAdapter extends RecyclerView.Adapter<ViewHolder> {

    List<Property> properties;
    Context context;
    private AdapterView.OnItemClickListener onItemClickListener;
    Typeface robotoMedium;

    PropertyAdapter(List<Property> properties, AdapterView.OnItemClickListener onItemClickListener, Context context) {
        this.properties = properties;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");
    }

    /**
     * Add items to adapter
     * @param properties
     */
    public void addItems(List<Property> properties) {
        this.properties.addAll(properties);
    }


    /**
     * Holds the property screen elements to avoid creating them multiple times
     */
    public class PropertyViewHolder extends ViewHolder implements View.OnClickListener {
        CardView cv;
        TextView name, price;
        AspectRatioImageView image;
        IconicsTextView stats, features;

        PropertyViewHolder(View itemView) {
            super(itemView);
            cv = (CardView) itemView.findViewById(R.id.card_view);
            image = (AspectRatioImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.property_name);
            stats = (IconicsTextView) itemView.findViewById(R.id.stats);

            if (Configurations.LIST_MENU_TYPE == Configurations.LIST_1COLUMNS) {
                price = (TextView) itemView.findViewById(R.id.property_price);
                features = (IconicsTextView) itemView.findViewById(R.id.property_features);
            }

            //set image on click listener
            image.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            //passing the clicked position to the parent class
            onItemClickListener.onItemClick(null, view, getAdapterPosition(), view.getId());
        }
    }


    @Override
    public int getItemCount() {
        return properties.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = null;

        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_1COLUMNS)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.property_card_1column, viewGroup, false);
        else if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS)
            v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.property_card_2column, viewGroup, false);

        RecyclerView.ViewHolder rvh = new PropertyViewHolder(v);
        return rvh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder propertyViewHolder, final int i) {
        //set property name
        ((PropertyViewHolder) propertyViewHolder).name.setTypeface(robotoMedium);
        ((PropertyViewHolder) propertyViewHolder).name.setText(properties.get(i).name);

        //set price and feature icons
        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_1COLUMNS) {
            DecimalFormat dFormat = new DecimalFormat("####,###,###");
            ((PropertyViewHolder) propertyViewHolder).price.setText(context.getString(R.string.currency) + dFormat.format(properties.get(i).saleprice));
            ((PropertyViewHolder) propertyViewHolder).features.setText("{faw-bath} " + properties.get(i).bathrooms + "   {faw-bed} " + properties.get(i).bedrooms + "   {faw-building} " + properties.get(i).rooms);

        }

        //set image as box when in 2 column mode
        if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
            ((PropertyViewHolder) propertyViewHolder).image.setBox(true);
        }

        //load property image with picasso
        if (properties.get(i).imageUrl != null) {
            RequestCreator r = Picasso.with(context).load(properties.get(i).imageUrl[0]).placeholder(R.drawable.loading);
            if (Configurations.LIST_MENU_TYPE == Configurations.LIST_2COLUMNS) {
                r.fit().centerCrop();
            }
            r.into(((PropertyViewHolder) propertyViewHolder).image);
        }

        //set stats
        ((PropertyViewHolder) propertyViewHolder).stats.setText("{faw-eye} " + properties.get(i).viewed + "  {faw-star} " + properties.get(i).favorited);
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}