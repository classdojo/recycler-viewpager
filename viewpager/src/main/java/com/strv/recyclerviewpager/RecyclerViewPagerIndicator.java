package com.strv.recyclerviewpager;

/**
 * Created by Roman Palkoci on 17/06/2015.
 */


/**
 * A PageIndicator is responsible to show an visual indicator on the total views
 * number and the current visible view.
 */
public interface RecyclerViewPagerIndicator extends RecyclerViewPager.OnPageChangeListener {
	/**
	 * Bind the indicator to a setRecyclerViewPager.
	 *
	 * @param view
	 */
	void setRecyclerViewPager(RecyclerViewPager view);

	/**
	 * Bind the indicator to a setRecyclerViewPager.
	 *
	 * @param view
	 * @param initialPosition
	 */
	void setRecyclerViewPager(RecyclerViewPager view, int initialPosition);

	/**
	 * <p>Set the current page of both the setRecyclerViewPager and indicator.</p>
	 * <p/>
	 * <p>This <strong>must</strong> be used if you need to set the page before
	 * the views are drawn on screen (e.g., default start page).</p>
	 *
	 * @param item
	 */
	void setCurrentItem(int item);

	/**
	 * Notify the indicator that the fragment list has changed.
	 */
	void notifyDataSetChanged();
}