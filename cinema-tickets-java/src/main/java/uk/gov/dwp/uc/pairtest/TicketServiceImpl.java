package uk.gov.dwp.uc.pairtest;

import java.util.HashMap;
import thirdparty.paymentgateway.TicketPaymentServiceImpl;
import thirdparty.seatbooking.SeatReservationServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketTypeRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

public class TicketServiceImpl implements TicketService {
    /**
     * Should only have private methods other than the one below.
     */

    @Override
    public void purchaseTickets(Long accountId, TicketTypeRequest... ticketTypeRequests) throws InvalidPurchaseException {

        //Intitalise service methods.
        TicketPaymentServiceImpl paymentService = new TicketPaymentServiceImpl();
        SeatReservationServiceImpl reserveSeats = new SeatReservationServiceImpl();

        //Hashmap for price list, type of seat is mapped to it's corresponding price.
        HashMap<String, Integer> priceList = new HashMap<String, Integer>();
        priceList.put("Adult", 20);
        priceList.put("Child", 10);
        priceList.put("Infant", 0);
        int adultSeats = 0;
        int childSeats = 0;
        int infantSeats = 0;
        int noOfTickets = ticketTypeRequests.length;

        if (accountId <= 0){
            System.out.println("Invalid purchase request: Account Id has to be greater than 0");
            throw new InvalidPurchaseException();
        }

        if (noOfTickets > 20){
            System.out.println("Invalid purchase request: Can only purchase a maximum of 20 tickets at one time");
            throw new InvalidPurchaseException();
        }

        //Loop through ticketrequests to count number of each type of seat.
        for (int i = 0; i < noOfTickets; i++) {
            if (ticketTypeRequests[i].getTicketType() == TicketTypeRequest.Type.ADULT){
                adultSeats++;
            }
            if (ticketTypeRequests[i].getTicketType() == TicketTypeRequest.Type.CHILD){
                childSeats++;
            }
            if (ticketTypeRequests[i].getTicketType() == TicketTypeRequest.Type.INFANT){
                infantSeats++;
            }
        }

        //if there is a child or infant but no adult throw exception.
        if((childSeats > 0 || infantSeats > 0) & adultSeats == 0){
            System.out.println("Invalid Transaction: Can only purchase child/infant ticket if accompanied by adult.");
            throw new InvalidPurchaseException();
        }

        //if there is an infant and not an equal amount of adults throw exception. As each infant should be seated with an adult.
        if(infantSeats > 0 & infantSeats > adultSeats){
            System.out.println("Invalid Transaction: There has to be 1 adult for every infant.");
            throw new InvalidPurchaseException();
        }

        int totalPrice = (adultSeats * priceList.get("Adult")) + (childSeats * priceList.get("Child")) + (infantSeats * priceList.get("Infant")); //Type of seat multiplied by price obtained from hashmap.
        int numberOfSeats = adultSeats + childSeats; //Infant seats not counted as they are seated with adult.

        paymentService.makePayment(accountId,totalPrice);
        reserveSeats.reserveSeat(accountId, numberOfSeats);
    }
}
