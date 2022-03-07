package com.neurondigital.estate;

import android.content.Context;
import android.graphics.Typeface;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.mikepenz.iconics.view.IconicsTextView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.util.List;

/**
 * Agent Adapter to show agent cards in list
 */
public class AgentAdapter extends RecyclerView.Adapter<ViewHolder> {

    List<Agent> agents;
    Context context;
    private AdapterView.OnItemClickListener onItemClickListener;
    Typeface robotoMedium;

    AgentAdapter(List<Agent> agents, AdapterView.OnItemClickListener onItemClickListener, Context context) {
        this.agents = agents;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        robotoMedium = Typeface.createFromAsset(context.getAssets(), "Roboto-Medium.ttf");
    }

    /**
     * Add items to adapter
     * @param agents
     */
    public void addItems(List<Agent> agents) {
        this.agents.addAll(agents);
    }


    /**
     * Holds the agent screen elements to avoid creating them multiple times
     */
    public class AgentViewHolder extends ViewHolder implements View.OnClickListener {
        RelativeLayout cv;
        TextView name;
        AspectRatioImageView image;
        IconicsTextView tel, email;

        AgentViewHolder(View itemView) {
            super(itemView);
            cv = (RelativeLayout) itemView.findViewById(R.id.card_view);
            image = (AspectRatioImageView) itemView.findViewById(R.id.image);
            name = (TextView) itemView.findViewById(R.id.name);
            tel = (IconicsTextView) itemView.findViewById(R.id.tel);
            email = (IconicsTextView) itemView.findViewById(R.id.email);

            //set image on click listener
            image.setOnClickListener(this);
            image.setBox(true);
        }

        @Override
        public void onClick(View view) {
            //passing the clicked position to the parent class
            onItemClickListener.onItemClick(null, view, getAdapterPosition(), view.getId());
        }
    }


    @Override
    public int getItemCount() {
        return agents.size();
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = null;
        v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.agent_card, viewGroup, false);
        ViewHolder rvh = new AgentViewHolder(v);
        return rvh;
    }


    @Override
    public void onBindViewHolder(final ViewHolder propertyViewHolder, final int i) {
        //set agents name
        ((AgentViewHolder) propertyViewHolder).name.setTypeface(robotoMedium);
        ((AgentViewHolder) propertyViewHolder).name.setText(agents.get(i).name);

        //set agent tel
        ((AgentViewHolder) propertyViewHolder).tel.setText("{faw-phone} " + agents.get(i).tel);

        //set agent email
        ((AgentViewHolder) propertyViewHolder).email.setText("{faw-envelope} " + agents.get(i).email);

        //set agent avatar
        if (agents.get(i).imageUrl != null) {
            RequestCreator r = Picasso.with(context).load(agents.get(i).imageUrl).placeholder(R.drawable.loading_agents);
            r.fit().centerCrop();
            r.into(((AgentViewHolder) propertyViewHolder).image);
        }
    }


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

}