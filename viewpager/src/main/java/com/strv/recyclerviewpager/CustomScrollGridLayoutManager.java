package com.strv.recyclerviewpager;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;


/**
 * Created by Roman on 22/06/2015.
 */
public class CustomScrollGridLayoutManager extends GridLayoutManager {
	private CustomSpanSizeLookup mCustomSpanSizeLookup;


	public CustomScrollGridLayoutManager(Context context, int spanCount) {
		super(context, spanCount);
		initLayoutManager();
	}


	public CustomScrollGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
		super(context, spanCount, orientation, reverseLayout);
		initLayoutManager();
	}

	public CustomSpanSizeLookup getCustomSpanSizeLookup() {
		return mCustomSpanSizeLookup;
	}


	public void setCustomSpanSizeLookup(CustomSpanSizeLookup customSpanSizeLookup) {
		mCustomSpanSizeLookup = customSpanSizeLookup;
	}


	private void initLayoutManager() {
		mCustomSpanSizeLookup = new DefaultCustomSpanSizeLookup();
	}


	/**
	 * A helper class to provide the number of spans each item occupies.
	 * <p/>
	 * Default implementation sets each item to occupy exactly 1 span.
	 *
	 * @see GridLayoutManager#setSpanSizeLookup(SpanSizeLookup)
	 */
	public static abstract class CustomSpanSizeLookup {
		/**
		 * Returns the number of span occupied by the item at <code>position</code> in other direction
		 *
		 * @param position The adapter position of the item
		 * @return The number of spans occupied by the item at the provided position
		 */
		abstract public int getSpanSize(int position);
	}


	private class DefaultCustomSpanSizeLookup extends CustomSpanSizeLookup {
		@Override
		public int getSpanSize(int position) {
			return 1;
		}
	}
}