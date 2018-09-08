/*
 * Created by Angel Leon (@gubatron), Alden Torres (aldenml)
 * Copyright (c) 2011-2015, FrostWire(R). All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package zig.zak.media.tor.android.gui.views;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import zig.zak.media.tor.R;

/**
 * @author gubatron
 * @author aldenml
 */
public class PromotionsView extends LinearLayout {
    private GridView gridview;

    public PromotionsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        View.inflate(getContext(), R.layout.view_promotions, this);

        if (isInEditMode()) {
            return;
        }

        gridview = findViewById(R.id.view_promotions_gridview);

    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        // aldenml: The need of this method is because don't have the best
        // use of saved states for fragments starting from the top activity.
        // When the activity configuration changes (for example, orientation)
        // the GridView is kept in memory, then the need of this forced unbind.
        //
        // Additionally, I'm recycling the picasso drawables for older devices. 
        unbindPromotionDrawables();
    }

    private void unbindPromotionDrawables() {
        for (int i = 0; gridview != null && i < gridview.getChildCount(); i++) {
            if (gridview.getChildAt(i) instanceof ImageView) {
                unbindPromotionDrawable((ImageView) gridview.getChildAt(i));
            }
        }
    }

    private void unbindPromotionDrawable(ImageView view) {
        if (view.getDrawable() != null) {
            Drawable d = view.getDrawable();
            d.setCallback(null);
            view.setImageDrawable(null);
        }
    }
}