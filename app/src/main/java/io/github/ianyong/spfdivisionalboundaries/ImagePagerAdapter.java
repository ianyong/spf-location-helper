package io.github.ianyong.spfdivisionalboundaries;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import androidx.viewpager.widget.PagerAdapter;

public class ImagePagerAdapter extends PagerAdapter {

    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<String> imagePaths;

    public ImagePagerAdapter(Context context, ArrayList<String> imagePaths) {
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.imagePaths = imagePaths;
    }

    @Override
    public int getCount() {
        return imagePaths.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = layoutInflater.inflate(R.layout.pager_item, container, false);
        ImageView imageView = itemView.findViewById(R.id.image_view);
        Picasso.get().load("https://www.dogster.com/wp-content/uploads/2017/08/A-pomeranian-on-grass.jpg")
                .into(imageView);
        container.addView(itemView);
        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

}
