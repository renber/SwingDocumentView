package de.renber.swing.controls.documentview.types;

public class PageVisibility {

	int pageIndex;
	float visiblePercentage;	
		
	public int getPageIndex() {
		return pageIndex;
	}
	
	/**
	 * Return the percentage of pages which is visible	 
	 */
	public float getVisiblePercentage() {
		return visiblePercentage;
	}
	
	public PageVisibility(int pageIndex, float visiblePercentage) {
		this.pageIndex = pageIndex;
		this.visiblePercentage = visiblePercentage;
	}
}
