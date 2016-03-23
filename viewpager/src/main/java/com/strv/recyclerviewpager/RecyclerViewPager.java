package com.strv.recyclerviewpager;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Roman on 11/06/2015.
 */
public class RecyclerViewPager extends RecyclerView implements View.OnTouchListener {
	private static final int CHANGE_PAGE_OFFSET = 250;
	private static final double SWING = 50;
	private static final String TAG = RecyclerViewPager.class.getSimpleName();
	int userDragX;
	int userDragY;
	int totalX;
	int totalY;
	/**
	 * Number at position in list represent number of the last item in the adapter on the position's page
	 */
	private List<Integer> mLastPositionsIndexes;
	private boolean mScrollByUser;
	private List<OnPageChangeListener> mOnPageChangeListeners;
	private List<OnPageCountChangeListener> mOnPageCountChangeListeners;
	private int mCurrentPage;
	private int mCurrentPageRotated = -1;
	private int mTotalPageCount;
	private CustomScrollGridLayoutManager mGridLayoutManager;
	private boolean mVertical = true;
	private PagerAdapter mAdapter;
	private int mChangePageOffset = -1;
	private int mInternalMargin;
	private boolean mTouchEnabled = true;
	private int mCurrentOrientation = -1;


	public RecyclerViewPager(Context context) {
		this(context, null);
	}


	public RecyclerViewPager(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}


	public RecyclerViewPager(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}


	public void setVertical(boolean vertical) {
		mVertical = vertical;
		mGridLayoutManager.setOrientation(mVertical ? LinearLayoutManager.VERTICAL : GridLayoutManager.HORIZONTAL);
	}


	public void goToPage(int page, boolean smoothScroll) {
		int dx;
		int dy;

		if(mScrollByUser) {
			mCurrentPageRotated = -1;
			int scrollByX = Math.abs(userDragX) != 0 ? getWidth() - Math.abs(userDragX) : 0;
			int scrollByY = Math.abs(userDragY) != 0 ? getHeight() - Math.abs(userDragY) : 0;

			if(mCurrentPage == page) {
				dx = userDragX * -1;
				dy = userDragY * -1;
			} else if(mCurrentPage < page) {
				dx = scrollByX;
				dy = scrollByY;
			} else {
				dx = scrollByX * -1;
				dy = scrollByY * -1;
			}


			if(userDragX >= 0 && userDragX != (totalX % getWidth())) {
				dx += userDragX - (totalX % getWidth());
			}
			if(userDragX < 0 && userDragX != (totalX % getWidth()) - getWidth()) {
				dx += userDragX - (totalX % getWidth()) + getWidth();
			}

			if(userDragY >= 0 && userDragY != (totalY % getHeight())) {
				dy += userDragY - (totalY % getHeight());
			}
			if(userDragY < 0 && userDragY != (totalY % getHeight()) - getHeight()) {
				dx += userDragY - (totalY % getHeight()) + getHeight();
			}
		} else {
			dx = !mVertical ? (page - mCurrentPage) * getWidth() : 0;
			dy = mVertical ? (page - mCurrentPage) * getHeight() : 0;
		}

		mScrollByUser = false;
		if(smoothScroll) {
			smoothScrollBy(dx, dy);
		} else {
			scrollBy(dx, dy);
		}
		userDragX = 0;
		userDragY = 0;
	}


	public void goToPreviousPage(boolean smoothScroll) {
		if(mCurrentPage >= 0) {
			goToPage(mCurrentPage - 1, smoothScroll);
		} else {
			goToPage(mCurrentPage, smoothScroll);
		}
	}


	public int getTotalPageCount() {
		return mTotalPageCount;
	}


	public void goToNextPage(boolean smoothScroll) {
		if(mCurrentPage < mTotalPageCount) {
			goToPage(mCurrentPage + 1, smoothScroll);
		} else {
			goToPage(mCurrentPage, smoothScroll);
		}
	}


	@Override
	public void setOnTouchListener(OnTouchListener l) {
		if(l != null && l instanceof RecyclerViewPager) {
			super.setOnTouchListener(l);
		} else {
//			Log.e(TAG, "Cannot set onTouchListener");
		}
	}


	public void setTouchEnabled(boolean enabled) {
		mTouchEnabled = enabled;
	}


	public boolean isTouchEnabled() {
		return mTouchEnabled;
	}


	@Override
	public boolean onInterceptTouchEvent(MotionEvent e) {
		return mTouchEnabled && super.onInterceptTouchEvent(e);
	}


	@Override
	public boolean onTouch(View v, MotionEvent event) {
		final int action = MotionEventCompat.getActionMasked(event);

		switch(action) {
			case MotionEvent.ACTION_DOWN:
				break;
			case MotionEventCompat.ACTION_POINTER_DOWN:
				break;
			case MotionEvent.ACTION_UP: {
				stopScroll();
				completeScroll();
			}
			return true;
		}
		return false;
	}


	/**
	 * Add a listener that will be invoked whenever the page changes or is incrementally
	 * scrolled. See {@link OnPageChangeListener}.
	 * <p/>
	 * <p>Components that add a listener should take care to remove it when finished.
	 * Other components that take ownership of a view may call {@link #clearOnPageChangeListeners()}
	 * to remove all attached listeners.</p>
	 *
	 * @param listener listener to add
	 */
	public void addOnPageChangeListener(OnPageChangeListener listener) {
		if(mOnPageChangeListeners == null) {
			mOnPageChangeListeners = new ArrayList<>();
		}
		mOnPageChangeListeners.add(listener);
	}


	/**
	 * Remove a listener that was previously added via
	 * {@link #addOnPageChangeListener(OnPageChangeListener)}.
	 *
	 * @param listener listener to remove
	 */
	public void removeOnPageChangeListener(OnPageChangeListener listener) {
		if(mOnPageChangeListeners != null) {
			mOnPageChangeListeners.remove(listener);
		}
	}


	/**
	 * Remove all listeners that are notified of any changes in scroll state or position.
	 */
	public void clearOnPageChangeListeners() {
		if(mOnPageChangeListeners != null) {
			mOnPageChangeListeners.clear();
		}
	}


	/**
	 * Add a listener that will be invoked whenever the count of pages changes. See {@link OnPageCountChangeListener}.
	 * <p/>
	 * <p>Components that add a listener should take care to remove it when finished.
	 * Other components that take ownership of a view may call {@link #clearOnPageChangeListeners()}
	 * to remove all attached listeners.</p>
	 *
	 * @param listener listener to add
	 */
	public void addOnPageCountChangeListener(OnPageCountChangeListener listener) {
		if(mOnPageCountChangeListeners == null) {
			mOnPageCountChangeListeners = new ArrayList<>();
		}
		mOnPageCountChangeListeners.add(listener);
	}


	/**
	 * Remove a listener that was previously added via
	 * {@link #addOnPageCountChangeListener(OnPageCountChangeListener)}.
	 *
	 * @param listener listener to remove
	 */
	public void removeOnPageCountChangeListener(OnPageCountChangeListener listener) {
		if(mOnPageCountChangeListeners != null) {
			mOnPageCountChangeListeners.remove(listener);
		}
	}


	/**
	 * Remove all listeners that are notified of any changes in scroll state or position.
	 */
	public void clearOnPageCountChangeListeners() {
		if(mOnPageCountChangeListeners != null) {
			mOnPageCountChangeListeners.clear();
		}
	}


	@Override
	public PagerAdapter getAdapter() {
		return (PagerAdapter) super.getAdapter();
	}


	@Override
	public void setAdapter(Adapter adapter) {
		super.setAdapter(adapter);
		if(!(adapter instanceof PagerAdapter)) {
			throw new RuntimeException("Adapter has to be instance of PagerAdapter");
		}
		mAdapter = (PagerAdapter) adapter;

		setSpanCount();
		setTotalPageCount();
		mAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
			@Override
			public void onChanged() {
				onDataChanged();
			}


			@Override
			public void onItemRangeChanged(int positionStart, int itemCount) {
				onDataChanged();
			}


			@Override
			public void onItemRangeInserted(int positionStart, int itemCount) {
				onDataChanged();
			}


			@Override
			public void onItemRangeRemoved(int positionStart, int itemCount) {
				onDataChanged();
			}


			@Override
			public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
				onDataChanged();
			}


			private void onDataChanged() {
				setTotalPageCount();
				// If page was removed and current page was on last page
				if(mCurrentPage >= getTotalPageCount()) {
					totalX = 0;
					totalY = 0;
					mCurrentPage = 0;
					goToPage(getTotalPageCount() - 1, true);
				}
			}
		});
	}


	private void setSpanCount() {
		int spanCount = (mVertical ? mAdapter.mItemsColumnCount : mAdapter.mItemsRowCount);
		if(spanCount != mGridLayoutManager.getSpanCount()) {
			mGridLayoutManager.setSpanCount(spanCount);
		}
	}


	public int getCurrentPage() {
		return mCurrentPage;
	}


	@Override
	public void onScrollStateChanged(int newState) {
		if(mOnPageChangeListeners != null) {
			for(OnPageChangeListener listener : mOnPageChangeListeners) {
				listener.onPageScrollStateChanged(newState);
			}
		}
		switch(newState) {
			case RecyclerView.SCROLL_STATE_IDLE:
				break;
			case RecyclerView.SCROLL_STATE_DRAGGING:
				mScrollByUser = true;
				break;
			case RecyclerView.SCROLL_STATE_SETTLING:
				break;
		}
	}


	int getInternalMargin() {
		return mInternalMargin;
	}


	void setInternalMargin(int internalMargin) {
		mInternalMargin = internalMargin;
	}


	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
	}


	@Override
	protected Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
//		Log.d(TAG, "Save State");

		SavedState ss = new SavedState(superState);
		//end

		ss.currentPage = this.mCurrentPage;
		ss.currentPageRotated = this.mCurrentPageRotated;
		ss.currentOrientation = this.mCurrentOrientation;
		if(mAdapter != null) {
			ss.rowCount = mAdapter.mItemsRowCount;
			ss.columnCount = mAdapter.mItemsColumnCount;
		}

		return ss;
	}


	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(!(state instanceof SavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState ss = (SavedState) state;

		super.onRestoreInstanceState(ss.getSuperState());
		//end

		scrollToPosition(0);
		final int page = ss.currentPage > getTotalPageCount() ? getTotalPageCount() - 1 : ss.currentPage;

		getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
			@Override
			public boolean onPreDraw() {
				getViewTreeObserver().removeOnPreDrawListener(this);
				goToPage(page, false);
				return false;
			}
		});
	}


	private int findPageForPosition(int firstVisiblePerPage) {
		for(int i = 0; i < mLastPositionsIndexes.size(); i++) {
			if(mLastPositionsIndexes.get(i) >= firstVisiblePerPage) {
				return i;
			}
		}
		return 0;
	}


	private void setTotalPageCount() {
		if(mAdapter == null) {
			return;
		}
//		Log.d(TAG, "setTotalPageCount");
		if(mLastPositionsIndexes == null) {
			mLastPositionsIndexes = new ArrayList<>();
		} else {
			mLastPositionsIndexes.clear();
		}
		int oneWaySpanCount;
		int otherWaySpanCount;
		int currentPage = 0;
		int expectedItemPerPageCount = mAdapter.mItemsColumnCount * mAdapter.mItemsRowCount;
		int totalPerItem;
		for(int i = 0, count = mAdapter.getItemCount(); i < count; i++) {
			oneWaySpanCount = mGridLayoutManager.getSpanSizeLookup().getSpanSize(i);
			otherWaySpanCount = mGridLayoutManager.getCustomSpanSizeLookup().getSpanSize(i);
			totalPerItem = oneWaySpanCount * otherWaySpanCount;
			if(totalPerItem > expectedItemPerPageCount) {
				mLastPositionsIndexes.add(currentPage, i - 1);
				currentPage++;
				expectedItemPerPageCount = mAdapter.mItemsColumnCount * mAdapter.mItemsRowCount;
			}
			expectedItemPerPageCount -= totalPerItem;
		}
		if(expectedItemPerPageCount == 0) {
			mLastPositionsIndexes.add(currentPage, mAdapter.getItemCount() - 1);
			currentPage++;
		}
		int previousPageCount = mTotalPageCount;
		mTotalPageCount = currentPage;

		if(mOnPageCountChangeListeners != null && previousPageCount != mTotalPageCount) {
			for(OnPageCountChangeListener listener : mOnPageCountChangeListeners) {
				listener.onPageCountChange(mTotalPageCount);
			}
		}
	}


	private void init() {
		setOnTouchListener(this);
		addOnScrollListener(new CustomOnScrollListener());
		mGridLayoutManager = new CustomScrollGridLayoutManager(getContext(), 1);
		mGridLayoutManager.setOrientation(mVertical ? LinearLayoutManager.VERTICAL : GridLayoutManager.HORIZONTAL);
		setLayoutManager(mGridLayoutManager);
	}


	private int getFirstVisiblePerPage(int page) {
		return page > 0 ? mLastPositionsIndexes.get(page - 1) + 1 : 0;
	}


	private int getLastVisiblePerPage(int page) {
		return page < mTotalPageCount ? mLastPositionsIndexes.get(page) : mLastPositionsIndexes.get(mLastPositionsIndexes.size() - 1);
	}


	private void completeScroll() {
		setChangePageOffset();
		boolean stayOnPage = Math.abs(userDragX) <= mChangePageOffset && Math.abs(userDragY) <= mChangePageOffset;
		if(stayOnPage) {
			goToPage(mCurrentPage, true);
			return;
		}

		boolean scrollLeftOrDown = userDragX > mChangePageOffset || userDragY > mChangePageOffset;
		if(scrollLeftOrDown) {
			goToNextPage(true);
		} else {
			goToPreviousPage(true);
		}
	}


	private void setChangePageOffset() {
		if(mChangePageOffset < 0) {
			mChangePageOffset = getMeasuredWidth() / 8;
		}
	}


	public interface OnPageChangeListener {

		/**
		 * This method will be invoked when the current page is scrolled, either as part
		 * of a programmatically initiated smooth scroll or a user initiated touch scroll.
		 *
		 * @param position             Position index of the first page currently being displayed.
		 *                             Page position+1 will be visible if positionOffset is nonzero.
		 * @param positionOffset       Value from [0, 1) indicating the offset from the page at position.
		 * @param positionOffsetPixels Value in pixels indicating the offset from position.
		 */
		void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

		/**
		 * This method will be invoked when a new page becomes selected. Animation is not
		 * necessarily complete.
		 *
		 * @param position Position index of the new selected page.
		 */
		void onPageSelected(int position);

		/**
		 * Called when the scroll state changes. Useful for discovering when the user
		 * begins dragging, when the pager is automatically settling to the current page,
		 * or when it is fully stopped/idle.
		 *
		 * @param state The new scroll state.
		 * @see RecyclerView#SCROLL_STATE_IDLE
		 * @see RecyclerView#SCROLL_STATE_DRAGGING
		 * @see RecyclerView#SCROLL_STATE_SETTLING
		 */
		void onPageScrollStateChanged(int state);
	}


	public interface OnPageCountChangeListener {
		void onPageCountChange(int count);
	}


	static class SavedState implements Parcelable {
		public static final SavedState EMPTY_STATE = new SavedState() {
		};
		//required field that makes Parcelables from a Parcel
		public static final Parcelable.Creator<SavedState> CREATOR =
				new Parcelable.Creator<SavedState>() {
					public SavedState createFromParcel(Parcel in) {
						return new SavedState(in);
					}


					public SavedState[] newArray(int size) {
						return new SavedState[size];
					}
				};

		int rowCount;
		int columnCount;
		int currentPage;
		public int currentPageRotated;
		// This keeps the parent(RecyclerView)'s state
		Parcelable superState;
		public int currentOrientation;


		SavedState() {
			superState = null;
		}


		SavedState(Parcelable superState) {
			this.superState = superState != EMPTY_STATE ? superState : null;
		}


		private SavedState(Parcel in) {
			// Parcel 'in' has its parent(RecyclerView)'s saved state.
			// To restore it, class loader that loaded RecyclerView is required.
			Parcelable superState = in.readParcelable(RecyclerView.class.getClassLoader());
			this.superState = superState != null ? superState : EMPTY_STATE;
			this.currentPage = in.readInt();
			this.rowCount = in.readInt();
			this.columnCount = in.readInt();
			this.currentPageRotated = in.readInt();
			this.currentOrientation = in.readInt();
		}


		public Parcelable getSuperState() {
			return superState;
		}


		@Override
		public int describeContents() {
			return 0;
		}


		@Override
		public void writeToParcel(Parcel out, int flags) {
			out.writeParcelable(superState, flags);
			out.writeInt(this.currentPage);
			out.writeInt(this.rowCount);
			out.writeInt(this.columnCount);
			out.writeInt(this.currentPageRotated);
			out.writeInt(this.currentOrientation);
		}
	}


	public static abstract class PagerViewHolder extends RecyclerView.ViewHolder {
		RecyclerViewPager mRecyclerViewPager;


		public PagerViewHolder(final View itemView, final RecyclerViewPager recyclerViewPager) {
			super(itemView);
			mRecyclerViewPager = recyclerViewPager;
		}


		public int getAdapterPosition(boolean transposed) {
			int adapterPosition = super.getAdapterPosition();
			if(adapterPosition == NO_POSITION || !transposed) {
				return adapterPosition;
			}
			return mRecyclerViewPager.getAdapter().getTransposedPosition(adapterPosition);
		}


//		public int getLayoutPosition(boolean transposed) {
//			int layoutPosition = super.getLayoutPosition();
//			if(layoutPosition == NO_POSITION || !transposed) {
//				return layoutPosition;
//			}
//			return mRecyclerViewPager.getAdapter().getTransposedPosition(layoutPosition);
//		}


		protected int getSpanSize() {
			return 1;
		}


		private int getDesiredSizeAndSetRecyclerMarginIfNeeded(RecyclerViewPager recyclerViewPager, int recyclerSize, int adapterItemsCount) {
			if(adapterItemsCount % getSpanSize() != 0) {
				throw new RuntimeException("Span count " + getSpanSize() + " has to be divisible by " + adapterItemsCount);
			}
			int margin = recyclerSize % adapterItemsCount != 0 ? (recyclerSize % adapterItemsCount) : 0;
			int desiredSize = (recyclerSize - margin) / ((adapterItemsCount / getSpanSize()));

			if(margin != 0) {
				recyclerViewPager.setInternalMargin(margin);
			}

			return desiredSize;
		}
	}


	public static abstract class PagerAdapter extends RecyclerView.Adapter<PagerViewHolder> {
		private int mItemsRowCount;
		private int mItemsColumnCount;
		private boolean mTransposed;

		protected final RecyclerViewPager mRecyclerViewPager;


		public PagerAdapter(int itemsRowCount, int itemsColumnCount, RecyclerViewPager recyclerViewPager) {
			this(itemsRowCount, itemsColumnCount, recyclerViewPager, true);
		}


		public PagerAdapter(int itemsRowCount, int itemsColumnCount, RecyclerViewPager recyclerViewPager, boolean transpose) {
			mItemsRowCount = itemsRowCount;
			mItemsColumnCount = itemsColumnCount;
			mRecyclerViewPager = recyclerViewPager;
			mTransposed = transpose;
		}


		public boolean isTransposed() {
			return mTransposed;
		}


		public int getItemsRowCount() {
			return mItemsRowCount;
		}


		public int getItemsColumnCount() {
			return mItemsColumnCount;
		}


		public void setItemsRowCount(int itemsRowCount) {
			mItemsRowCount = itemsRowCount;
			mRecyclerViewPager.setSpanCount();
			notifyDataSetChanged();
		}


		public void setItemsColumnCount(int itemsColumnCount) {
			mItemsColumnCount = itemsColumnCount;
			mRecyclerViewPager.setSpanCount();
			notifyDataSetChanged();
		}


		@Override
		public final void onBindViewHolder(PagerViewHolder holder, int position) {
			final CustomScrollGridLayoutManager manager = (CustomScrollGridLayoutManager) mRecyclerViewPager.getLayoutManager();
			MarginLayoutParams params = (MarginLayoutParams) holder.itemView.getLayoutParams();
			final int desiredSize;
			if(manager.getOrientation() == RecyclerView.VERTICAL) {
				desiredSize = holder.getDesiredSizeAndSetRecyclerMarginIfNeeded(mRecyclerViewPager, mRecyclerViewPager.getHeight(), mItemsRowCount);
			} else {
				desiredSize = holder.getDesiredSizeAndSetRecyclerMarginIfNeeded(mRecyclerViewPager, mRecyclerViewPager.getWidth(), mItemsColumnCount);
			}

//			int position = holder.getLayoutPosition();
			int page = getPageForViewPosition(position);
			int startPositionOnPage = mRecyclerViewPager.getFirstVisiblePerPage(page);
			int positionOnFirstPage = position - startPositionOnPage;
			int adapterItemCount = mRecyclerViewPager.mVertical ? mItemsColumnCount : mItemsRowCount;
			boolean addMargin =
					(positionOnFirstPage / adapterItemCount) < mRecyclerViewPager.getInternalMargin() / holder.getSpanSize();
			int margin = addMargin ? holder.getSpanSize() : 0;
			if(mRecyclerViewPager.mVertical) {
				params.height = desiredSize + margin;
			} else {
				params.width = desiredSize + margin;
			}
			holder.itemView.setLayoutParams(params);
			holder.itemView.requestLayout();


			onBindViewHolder(holder, getTransposedPosition(position), mTransposed);
		}


		public abstract void onBindViewHolder(PagerViewHolder holder, int position, boolean transposed);


		@Override
		public final int getItemViewType(int position) {
			return getItemViewType(getTransposedPosition(position), mTransposed);
		}


		public int getPageForViewPosition(int position) {
			for(int i = 0; i < mRecyclerViewPager.mLastPositionsIndexes.size(); i++) {
				if(mRecyclerViewPager.mLastPositionsIndexes.get(i) >= position) {
					return i;
				}
			}
			return 0;
		}


		protected int getItemViewType(int position, boolean transposed) {
			return super.getItemViewType(position);
		}


		private int getTransposedPosition(int position) {
			if(mTransposed) {
				return transposePosition(position);
			}
			return position;
		}


		private int transposePosition(int position) {
			int spanCount = mRecyclerViewPager.mGridLayoutManager.getCustomSpanSizeLookup().getSpanSize(position);
			int page = getPageForViewPosition(position);
			int startPositionOnPage = mRecyclerViewPager.getFirstVisiblePerPage(page);
			int positionOnFirstPage = position - startPositionOnPage;
			int transposedPosition = startPositionOnPage + ((positionOnFirstPage % mItemsRowCount) * (mItemsColumnCount / spanCount) + (positionOnFirstPage / mItemsRowCount));
			return transposedPosition;
		}


	}


	private class CustomOnScrollListener extends RecyclerView.OnScrollListener {


		@Override
		public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
			if(mScrollByUser) {
				userDragX += dx;
				userDragY += dy;
			}
			totalX += dx;
			totalY += dy;

			int total = mVertical ? totalY : totalX;
			int pageSize = mVertical ? getHeight() : getWidth();
			int onPage = pageSize == 0 ? 0 : (total % pageSize);
			float positionOffset = onPage != 0 ? onPage / (float) pageSize : 0;

			if(mCurrentPage != total / pageSize) {
				mCurrentPage = total / pageSize;
				if(mOnPageChangeListeners != null)
					for(OnPageChangeListener listener : mOnPageChangeListeners) {
						if(listener != null) {
							listener.onPageSelected(mCurrentPage);
						}
					}
			}

			if(mOnPageChangeListeners != null) {
				for(OnPageChangeListener listener : mOnPageChangeListeners) {
					listener.onPageScrolled(total / pageSize, positionOffset, onPage);
				}
			}
		}
	}


}
