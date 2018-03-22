/* -*- java -*- ************************************************************************** *
 *
 *                     Copyright (C) KNAPP AG
 *
 *   The copyright to the computer program(s) herein is the property
 *   of Knapp.  The program(s) may be used   and/or copied only with
 *   the  written permission of  Knapp  or in  accordance  with  the
 *   terms and conditions stipulated in the agreement/contract under
 *   which the program(s) have been supplied.
 *
 * *************************************************************************************** */

package com.knapp.codingcontest.kcc2018.solution;

import com.knapp.codingcontest.kcc2018.data.Container;
import com.knapp.codingcontest.kcc2018.data.Institute;
import com.knapp.codingcontest.kcc2018.data.Order;
import com.knapp.codingcontest.kcc2018.warehouse.Shuttle;
import com.knapp.codingcontest.kcc2018.warehouse.Warehouse;
import com.knapp.codingcontest.kcc2018.warehouse.WorkStation;
import com.knapp.codingcontest.kcc2018.warehouse.aisle.Aisle;
import com.knapp.codingcontest.kcc2018.warehouse.aisle.Location;
import com.knapp.codingcontest.kcc2018.warehouse.aisle.Position;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is the code YOU have to provide
 * <p>
 * warehouse all the operations you should need
 */
public class Solution {
    /**
     * TODO: Your name
     */
    public static final String PARTICIPANT_NAME = "Stefan Ortner";

    /**
     * TODO: The Id of your institute - please refer to the handout
     */
    public static final Institute PARTICIPANT_INSTITUTION = Institute.HTL_Kaindorf;

    // ----------------------------------------------------------------------------

    private final Warehouse warehouse;

    // ----------------------------------------------------------------------------

    public Solution(final Warehouse warehouse) {
        this.warehouse = warehouse;
    }

    // ----------------------------------------------------------------------------


    private Container getClosestExpected(Order o, Position position) {
        List<Container> containers = new ArrayList<>();
        for (int i = 0; i < warehouse.getAisles().length; i++) {
            for (Location loc : warehouse.getAisle(i).getLocations()) {
                for (int j = 0; j < loc.getContainers().size(); j++) {
                    if (o.getProductCode().equals(loc.getContainers().get(j).getProductCode())) {
                        containers.add(loc.getContainers().get(j));
                    }
                }
            }
        }
        long distance = Long.MAX_VALUE;

        Container closestExpected = null;

        for (Container c : containers) {
            long help = warehouse.calcMoveCost(position, c.getLocation().getPosition());
            if (o.getRemainingQuantity() > c.getQuantity()) {
                help += 10000;
            }
            if (help < distance) {
                distance = help;

                closestExpected = c;
            }

        }

        return closestExpected;
    }


    private List<Location> getEmptyLocationsInAisle(int aisle) {
        List<Location> emptyLocationsInAisle = new ArrayList<>();


        for (Location l : warehouse.getAisle(aisle).getLocations()) {
            if (l.getContainers().size() < 2) {
                emptyLocationsInAisle.add(l);
            }

        }

        return emptyLocationsInAisle;
    }

    private Location getClosestEmpty(int aisle, Location location) {

        List<Location> locations = getEmptyLocationsInAisle(aisle);
        Location loc = null;
        long distance = Long.MAX_VALUE;

        for (Location l : locations) {
            if (l.getContainers().size() < 2) {

                long help = warehouse.calcMoveCost(location, l);
                if (help < distance) {
                    distance = help;

                    loc = l;
                }

            }
        }
        return loc;
    }

    private Container getExpectedContainer(Order o) {

        for (Container c : warehouse.getAllContainers()) {
            if (o.getProductCode().equals(c.getProductCode())) {
                return c;
            }
        }
        return null;

    }

    public void runWarehouseOperations() {
        System.out.println("### Your output starts here");

        //  ==> CODE YOUR SOLUTION HERE !!!");

        WorkStation ws = warehouse.getWorkStation();

        Shuttle shuttle = warehouse.getShuttle();

        List<Order> orders = warehouse.getOrders();
        Collection<Container> allContainers = warehouse.getAllContainers();
        List<Location> locations = new ArrayList<>();


        for (Order o : orders) {

            Container expected;
            Location location;

            System.out.println(o.getOrderCode());

            expected = getClosestExpected(o, shuttle.getCurrentPosition());
            location = expected.getLocation();

            int i = 0;
            while (o.getRemainingQuantity() > 0) {

                shuttle.moveToPosition(location.getPosition());
                if (location.getContainers().get(0).equals(expected)) {
                    shuttle.loadFrom(location, expected);
                    shuttle.moveToPosition(ws);
                    int currentAisle = shuttle.getCurrentPosition().getAisle();

                    ws.pickOrder(o);
                    //System.out.println("remQ = " + o.getRemainingQuantity());

                    //Location l = getEmptyLocationsInAisle().get(i);
                    try {
                        Location l = getClosestEmpty(currentAisle, expected.getLocation());
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }


                    shuttle.moveToPosition(location.getPosition());
                    shuttle.storeTo(location);
                    if (o.getProductCode().equals(orders.get(i + 1).getProductCode())) {
                        shuttle.moveToPosition(expected.getLocation().getPosition());
                    } else {
                        shuttle.moveToPosition(ws);
                    }
                } else {
                    int currentAisle = shuttle.getCurrentPosition().getAisle();
                    Location l = null;
                    try {
                        l = getClosestEmpty(currentAisle, expected.getLocation());

                    } catch (Exception e) {
                        Logger.getLogger(e.getMessage());
                    }
                    shuttle.loadFrom(location, location.getContainers().get(0));
                    shuttle.moveToPosition(l.getPosition());
                    shuttle.storeTo(l);
                    continue;
                }

                try {
                    expected = getClosestExpected(o, shuttle.getCurrentPosition());
                    location = expected.getLocation();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }

            }

        }


        System.out.println("### Your output stops here");

        System.out.println("");
        System.out.println(String.format("--> Total operation cost        : %10d", warehouse.getCurrentOperationsCost()));
        System.out.println(String.format("--> Total unfinished order cost : %10d", warehouse.getCurrentUnfinishedOrdersCost()));
        System.out.println(String.format("--> Total cleanup cost          : %10d", warehouse.getCurrentCleanupCost()));
        System.out.println(String.format("                                  ------------"));
        System.out.println(String.format("==> TOTAL COST                  : %10d", warehouse.getCurrentTotalCost()));
        System.out.println(String.format("                                  ============"));
    }


    // ----------------------------------------------------------------------------

    private void apis() {
        // collaborators
        final Shuttle shuttle = warehouse.getShuttle();
        final WorkStation workStation = warehouse.getWorkStation();

        // information
        final Warehouse.Characteristics c = warehouse.getCharacteristics();
        final int numberOfAisles = c.getNumberOfAisles();
        final int numberOfPositionsPerAisle = c.getNumberOfPositionsPerAisle();
        final int locationDepth = c.getLocationDepth();

        // information that change with operations! (orders/containers/locations/...)
        final Collection<Location> locations = warehouse.getAisle(0).getLocations();
        final Location location = warehouse.getAisle(0).getLocation(0, Aisle.Side.Left);

        final List<Order> orders = warehouse.getOrders();
        final Collection<Container> allContainers = warehouse.getAllContainers();

        final long currentTotalCost = warehouse.getCurrentTotalCost();

        final Container loadedContainer = shuttle.getLoadedContainer();
        final Position currentPosition = shuttle.getCurrentPosition();
        final boolean isAtWorkStation = shuttle.isAtWorkStation();

        //
        // operations
        shuttle.moveToPosition(workStation);
        shuttle.moveToPosition(location.getPosition());
        final Container expected = location.getContainers().get(0);
        shuttle.loadFrom(location, expected);

        final Order order = null;
        workStation.pickOrder(order);

        shuttle.storeTo(location);
    }

    // ----------------------------------------------------------------------------
    // ----------------------------------------------------------------------------
}
