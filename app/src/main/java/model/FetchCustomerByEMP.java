package model;

public class FetchCustomerByEMP {

    String cardName;
    String checkInDate;
    String checkInTime;
    String checkoutTime;
    String location;

    public FetchCustomerByEMP(String cardName, String checkInDate, String checkInTime, String checkoutTime, String location) {
        this.cardName = cardName;
        this.checkInDate = checkInDate;
        this.checkInTime = checkInTime;
        this.checkoutTime = checkoutTime;
        this.location = location;
    }

    public String getCardName() {
        return cardName;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public String getCheckInTime() {
        return checkInTime;
    }

    public String getCheckoutTime() {
        return checkoutTime;
    }

    public String getLocation() {
        return location;
    }
}
