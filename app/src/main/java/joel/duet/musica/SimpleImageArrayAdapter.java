package joel.duet.musica;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

/**
 *
 * Created by joel on 24/01/16 at 15:15 at 15:17.
 */
public final class SimpleImageArrayAdapter extends ArrayAdapter<Integer> {

    private Integer[] images;

    public SimpleImageArrayAdapter(Context context, Integer[] images) {
        super(context, android.R.layout.simple_list_item_1, images);
        this.images = images;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getImageForPosition(position);
    }

    private View getImageForPosition(int position) {
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(images[position]);
        return imageView;
    }
}
