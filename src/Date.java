class Date {
	private double highPrice;
	private double lowPrice;
	private double openingPrice;
	private double closingPrice;
	private double adjClosing;
	private int shareVolume;
	private String dateString;

	public double getHighPrice() {
		return highPrice;
	}

	public void setHighPrice(double highPrice) {
		this.highPrice = highPrice;
	}

	public double getLowPrice() {
		return lowPrice;
	}

	public void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}

	public double getOpeningPrice() {
		return openingPrice;
	}

	public void setOpeningPrice(double openingPrice) {
		this.openingPrice = openingPrice;
	}

	public double getClosingPrice() {
		return closingPrice;
	}

	public void setClosingPrice(double closingPrice) {
		this.closingPrice = closingPrice;
	}

	@Override
	public String toString() {
		return dateString + " [highPrice=" + highPrice + ", lowPrice=" + lowPrice + ", openingPrice=" + openingPrice
				+ ", closingPrice=" + closingPrice + ", adjClosing=" + adjClosing + ", shareVolume=" + shareVolume
				+"]";
	}

	public double getAdjClosing() {
		return adjClosing;
	}

	public void setAdjClosing(double adjClosing) {
		this.adjClosing = adjClosing;
	}

	public int getShareVolume() {
		return shareVolume;
	}

	public void setShareVolume(int shareVolume) {
		this.shareVolume = shareVolume;
	}

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}
	
	public Date(double highPrice, double lowPrice, double openingPrice, double closingPrice, double adjClosing, int shareVolume, String dateString) {
		this.highPrice = highPrice;
		this.lowPrice = lowPrice;
		this.openingPrice = openingPrice;
		this.closingPrice = closingPrice;
		this.adjClosing = adjClosing;
		this.shareVolume = shareVolume;
		this.dateString = dateString;
	}
	
}








