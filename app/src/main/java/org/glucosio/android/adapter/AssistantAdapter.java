package org.glucosio.android.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.glucosio.android.ActionTip;
import org.glucosio.android.R;
import org.glucosio.android.presenter.AssistantPresenter;

import java.util.ArrayList;

public class AssistantAdapter extends RecyclerView.Adapter<AssistantAdapter.ViewHolder> {
    private Context mContext;
    ArrayList<ActionTip> actionTips;
    private AssistantPresenter presenter;
    Resources res;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public View mView;
        public ViewHolder(View v) {
            super(v);
            mView = v;
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public AssistantAdapter(Context context, AssistantPresenter assistantPresenter, ArrayList<ActionTip> tips) {
        this.mContext = context;
        this.res = context.getResources();
        this.presenter = assistantPresenter;
        this.actionTips = tips;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public AssistantAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                        int viewType) {

        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_assistant_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView actionTipTitle = (TextView) holder.mView.findViewById(R.id.fragment_assistant_item_title);
        TextView actionTipDescription = (TextView) holder.mView.findViewById(R.id.fragment_assistant_item_description);
        AppCompatButton actionTipAction = (AppCompatButton) holder.mView.findViewById(R.id.fragment_assistant_item_action);
        actionTipTitle.setText(actionTips.get(position).getTipTitle());
        actionTipDescription.setText(actionTips.get(position).getTipDescription());
        actionTipAction.setText(actionTips.get(position).getTipAction());
        String actionTipTitleString = actionTips.get(position).getTipTitle();

        View.OnClickListener actionListener;
        if (actionTipTitleString.equals(res.getString(R.string.assistant_feedback_title))) {
            actionListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.openGitty();
                }
            };
        } else if (actionTipTitleString.equals(res.getString(R.string.assistant_export_title))) {
            actionListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.startExportActivity();
                }
            };
        } else if (actionTipTitleString.equals(res.getString(R.string.assistant_calculator_a1c_title))){
            actionListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.startA1CCalculatorActivity();
                }
            };
        } else {
            actionListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    presenter.addReading();
                }
            };
        }

        actionTipAction.setOnClickListener(actionListener);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return actionTips.size();
    }
}
