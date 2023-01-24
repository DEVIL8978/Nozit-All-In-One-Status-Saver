package com.cd.statussaver.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.cd.statussaver.R;
import com.cd.statussaver.activity.VideoPlayerActivity;
import com.cd.statussaver.databinding.ItemsWhatsappViewBinding;
import com.cd.statussaver.model.FBStoryModel.NodeModel;

import java.util.ArrayList;

import static com.cd.statussaver.util.Utils.RootDirectoryFacebook;
import static com.cd.statussaver.util.Utils.startDownload;


public class FBStoriesListAdapter extends RecyclerView.Adapter<FBStoriesListAdapter.ViewHolder> {
    private Context context;
    private ArrayList<NodeModel> nodeModels;

    public FBStoriesListAdapter(Context context, ArrayList<NodeModel> list) {
        this.context = context;
        this.nodeModels = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        return new ViewHolder(DataBindingUtil.inflate(layoutInflater, R.layout.items_whatsapp_view, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int position) {
        NodeModel nodeModel = nodeModels.get(position);
        try {
            if (nodeModel.getNodeDataModel().getAttachmentsList().get(0).getMediaDataModel().get__typename().equalsIgnoreCase("Video")) {
                viewHolder.binding.ivPlay.setVisibility(View.VISIBLE);
            } else {
                viewHolder.binding.ivPlay.setVisibility(View.GONE);
            }



            viewHolder.binding.ivPlay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra("PathVideo",nodeModel.getNodeDataModel().getAttachmentsList().get(0).getMediaDataModel().getPlayable_url_quality_hd());
                    context.startActivity(intent);
                }
            });
            Glide.with(context)
                    .load(nodeModel.getNodeDataModel().getAttachmentsList().get(0).getMediaDataModel().getPreviewImage().get("uri").getAsString())
                    .thumbnail(0.2f).into(viewHolder.binding.pcw);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        viewHolder.binding.tvDownload.setOnClickListener(v -> {
            if (nodeModel.getNodeDataModel().getAttachmentsList().get(0).getMediaDataModel().get__typename().equalsIgnoreCase("Video")) {
                startDownload(nodeModel.getNodeDataModel().getAttachmentsList().get(0).getMediaDataModel().getPlayable_url_quality_hd(),
                        RootDirectoryFacebook, context, "fbstory_" + System.currentTimeMillis() + ".mp4");
            } else {
                startDownload(nodeModel.getNodeDataModel().getAttachmentsList().get(0).getMediaDataModel().getPreviewImage().get("uri").getAsString(),
                        RootDirectoryFacebook, context, "fbstory_" + System.currentTimeMillis() + ".png");

            }
        });


    }

    @Override
    public int getItemCount() {
        return nodeModels == null ? 0 : nodeModels.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ItemsWhatsappViewBinding binding;

        public ViewHolder(ItemsWhatsappViewBinding mbinding) {
            super(mbinding.getRoot());
            this.binding = mbinding;
        }
    }
}
