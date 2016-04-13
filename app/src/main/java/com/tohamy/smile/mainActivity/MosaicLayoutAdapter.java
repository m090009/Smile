package com.tohamy.smile.mainActivity;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.AbstractDraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.tohamy.smile.R;
import com.tohamy.smile.models.ImageUtils;
import com.tohamy.smile.models.Item;

import org.lucasr.twowayview.TwoWayLayoutManager;
import org.lucasr.twowayview.widget.SpannableGridLayoutManager;
import org.lucasr.twowayview.widget.TwoWayView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by tohamy on 3/17/15.
 */
public class MosaicLayoutAdapter extends RecyclerView.Adapter<MosaicLayoutAdapter.MosaicViewHolder> {
    private ArrayList<Item> items;
    private final int defaultNumber = 10;
    private final Context context;
    private final TwoWayView recyclerView;
    private ArrayList<Item> itemList = null;
    private final int [][][] patterns = new int [][][]{
            {{2,2},{1,1},{1,1}},
            {{1,1},{2,2},{1,1}},
            {{1,2},{1,1}},
            {{1,1},{1,2}},
            {{2,3}},
            {{1,1},{1,1},{1,1}}
    };
    private int cellCount = 0;
    private int prevPattern = -1;
    int rand;
    private List<CellPattern> chosenPatterns = null;
    private int blockSize;
    // Constructor ::


    public MosaicLayoutAdapter(Context context,
                               TwoWayView recyclerView,
                               ArrayList<Item> itemList,
                               int blockSize) {
        this.context = context;
        this.recyclerView = recyclerView;
        this.itemList = itemList;
        this.items = this.itemList;
        chosenPatterns = new ArrayList<CellPattern>();
        this.blockSize = blockSize;

    }

    @Override
    public MosaicViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        final View view = LayoutInflater.from(this.context).inflate(R.layout.mosaic_item, viewGroup, false);
        return new MosaicViewHolder(view);    }

    @Override
    public void onBindViewHolder(MosaicViewHolder holder, int position) {
        //Set the items for each item using the list
//        holder.setItem(itemList.get(position));
        //Making sure everything is vertical
        boolean isVertical = (recyclerView.getOrientation() == TwoWayLayoutManager.Orientation.VERTICAL);

        final View itemView = holder.itemView;
        final int itemId = position;

            final SpannableGridLayoutManager.LayoutParams layoutParams =
                    (SpannableGridLayoutManager.LayoutParams) itemView.getLayoutParams();

            if(this.cellCount == 0){
                rand = new Random().nextInt(patterns.length);
                if(rand == prevPattern)
                    rand = new Random().nextInt(patterns.length);
                else if(position+1 == items.size())
                    rand = 4;
                this.cellCount = patterns[rand].length;
                prevPattern = rand;
            }

            int c,span1,span2;

            if(chosenPatterns.size() <= position) {
//                if(position == 0)
//                    chosenPatterns = new ArrayList<CellPattern>();
                c = patterns[rand].length - this.cellCount;
                span1 = patterns[rand][c][0];
                span2 = patterns[rand][c][1];
                //cell rows and cols
                chosenPatterns.add(new CellPattern(span1, span2));
                this.cellCount--;
            } else{
                span1 = chosenPatterns.get(position).getRow();
                span2 = chosenPatterns.get(position).getCol();
            }

            final int colSpan = (isVertical ? span2 : span1);
            final int rowSpan = (isVertical ? span1 : span2);
            //

            if (layoutParams.rowSpan != rowSpan || layoutParams.colSpan != colSpan) {
                layoutParams.rowSpan = rowSpan;
                layoutParams.colSpan = colSpan;
                itemView.setLayoutParams(layoutParams);
            }
            holder.setNew(false);
            Uri uri = Uri.parse(ImageUtils.FRESCO_FILE + items.get(position).getImageUrl());
            SimpleDraweeView imageView = holder.mosiacImageView;
            ImageUtils.requestImageResize(span2*this.blockSize, span1*this.blockSize, uri, imageView);
            //requestImageNothing(uri, imageView);
        //?????????????
    }


    public void requestImageResize(int width, int height, Uri uri, SimpleDraweeView view){
        ResizeOptions imageResizeOption = new ResizeOptions(width/2, height/2);
        ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                .setResizeOptions(imageResizeOption)
                .setLocalThumbnailPreviewsEnabled(true)
                .build();
        AbstractDraweeController controller =
                Fresco.newDraweeControllerBuilder()
                                .setOldController(view.getController())
                                .setImageRequest(imageRequest)
                                .setAutoPlayAnimations(true)
                                .build();
        try {
            view.setController(controller);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void repopulate(ArrayList<Item> items){
        this.items = items;
        notifyDataSetChanged();
    }

    public void removeFromList(int postion){
        this.items.remove(postion);
        this.notifyItemRemoved(postion);
//        this.notifyDataSetChanged();
        this.notifyItemRangeChanged(postion, this.items.size());
        this.chosenPatterns.remove(this.chosenPatterns.size()-1);
    }

    public void addImage(){

    }

    public void resetView(){
        if(items.isEmpty())
            items.addAll(itemList);
        else if(items.size() < itemList.size()){
            items = new ArrayList<>();
            items.addAll(itemList);
        }

        chosenPatterns = new ArrayList<CellPattern>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.items.size();
    }

    class MosaicViewHolder extends RecyclerView.ViewHolder {
        public final SimpleDraweeView mosiacImageView;
        public Item mosaicItem;
        private boolean isNew = true;

        public MosaicViewHolder(View itemView) {
            super(itemView);
            this.mosiacImageView = (SimpleDraweeView) itemView.findViewById(R.id.mosaic_item_imageView);
        }

        public void setItem(Item mosiacItem){
            this.mosaicItem = mosiacItem;
            if(mosiacImageView != null){
                mosiacImageView.setBackgroundColor(mosiacItem.getDefaultColor());
            }
        }

        public void setImage(int color){
            this.mosiacImageView.setBackgroundColor(color);
//            Uri uri = Uri.parse("res://com.m090009.mosaicvietest/" + R.drawable.img2);
//        this.mosiacImageView.setImageURI(uri);
        }

        public void setNew(boolean isNew) {
            this.isNew = isNew;
        }

        public boolean isNew() {
            return isNew;
        }
    }

    private class CellPattern {
        private int row;
        private int col;

        public CellPattern(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public void setRow(int row) {
            this.row = row;
        }

        public int getCol() {
            return col;
        }

        public void setCol(int col) {
            this.col = col;
        }
    }
}
