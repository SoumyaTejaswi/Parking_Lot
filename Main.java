import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

//--enums
enum VehicleType {
    CAR, BIKE, TRUCK
}

enum SpotType {
    COMPACT, LARGE, HANDICAPPED
}

//--Vehicle--
abstract class Vehicle {
    private String licensePlate;
    private VehicleType type;

    public Vehicle(String licensePlate, VehicleType type) {
        this.licensePlate = licensePlate;
        this.type = type;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public VehicleType getType() {
        return type;
    }
}

// Implementing classes for CAR, BIKE, TRUCK
class Car extends Vehicle {
    public Car(String licensePlate) {
        super(licensePlate, VehicleType.CAR);
    }
}

class Bike extends Vehicle {
    public Bike(String licensePlate) {
        super(licensePlate, VehicleType.BIKE);
    }
}

class Truck extends Vehicle {
    public Truck(String licensePlate) {
        super(licensePlate, VehicleType.TRUCK);
    }
}

//--ParkingSpot--
abstract class ParkingSpot {
    protected String id;
    protected boolean occupied;
    protected SpotType spotType;
    protected Vehicle currentVehicle;

    public boolean isOccupied() {
        return occupied;
    }

    public SpotType getSpotType() {
        return spotType;
    }

    public void assignVehicle(Vehicle vehicle) {
        this.currentVehicle = vehicle;
        this.occupied = true;
    }

    public void removeVehicle() {
        this.currentVehicle = null;
        this.occupied = false;
    }

    public abstract boolean canFitVehicle(Vehicle vehicle);
}

class CompactSpot extends ParkingSpot {
    public CompactSpot(String id) {
        this.id = id;
        this.spotType = SpotType.COMPACT;
        this.occupied = false;
    }

    @Override
    public boolean canFitVehicle(Vehicle vehicle) {
        return vehicle.getType() == VehicleType.CAR || vehicle.getType() == VehicleType.BIKE;
    }
}

class LargeSpot extends ParkingSpot {
    public LargeSpot(String id) {
        this.id = id;
        this.spotType = SpotType.LARGE;
        this.occupied = false;
    }

    @Override
    public boolean canFitVehicle(Vehicle vehicle) {
        return true;
    }
}

class HandicappedSpot extends ParkingSpot {
    public HandicappedSpot(String id) {
        this.id = id;
        this.spotType = SpotType.HANDICAPPED;
        this.occupied = false;
    }

    @Override
    public boolean canFitVehicle(Vehicle vehicle) {
        return vehicle.getType() == VehicleType.CAR;
    }
}

//--Ticket--
class ParkingTicket {
    private String ticketId;
    private Vehicle vehicle;
    private LocalDateTime entryTime;
    private ParkingSpot spot;

    public ParkingTicket(String ticketId, Vehicle vehicle, LocalDateTime entryTime, ParkingSpot spot) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.entryTime = entryTime;
        this.spot = spot;
    }

    public ParkingSpot getSpot() {
        return spot;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public String getTicketId() {
        return ticketId;
    }
}

class ParkingFloor {
    private List<ParkingSpot> spots;

    public ParkingFloor(List<ParkingSpot> spots) {
        this.spots = spots;
    }

    public ParkingSpot findAvailableSpot(Vehicle vehicle) {
        for (ParkingSpot spot : spots) {
            if (!spot.isOccupied() && spot.canFitVehicle(vehicle)) {
                return spot;
            }
        }
        return null;
    }

    public void displayAvailableSpots() {
        Map<SpotType, Long> availability = spots.stream()
                .filter(s -> !s.isOccupied())
                .collect(Collectors.groupingBy(ParkingSpot::getSpotType, Collectors.counting()));

        System.out.println("Available Spots:");
        availability.forEach((type, count) -> System.out.println(type + ": " + count));
    }
}

class ParkingLot {
    private List<ParkingFloor> floors;

    public ParkingLot(List<ParkingFloor> floors) {
        this.floors = floors;
    }

    public ParkingTicket parkVehicle(Vehicle vehicle) {
        for (ParkingFloor floor : floors) {
            ParkingSpot spot = floor.findAvailableSpot(vehicle);
            if (spot != null) {
                spot.assignVehicle(vehicle);
                return new ParkingTicket(UUID.randomUUID().toString(), vehicle, LocalDateTime.now(), spot);
            }
        }
        throw new RuntimeException("Parking Full");
    }

    public void unparkVehicle(ParkingTicket ticket) {
        ParkingSpot spot = ticket.getSpot();
        spot.removeVehicle();
        System.out.println("Unparked: " + ticket.getVehicle().getLicensePlate());
    }

    public void displayAvailability() {
        for (ParkingFloor floor : floors) {
            floor.displayAvailableSpots();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Map<String, ParkingTicket> activeTickets = new HashMap<>();

        // Setup Parking Lot
        List<ParkingSpot> floor1Spots = Arrays.asList(
                new CompactSpot("C1"), new LargeSpot("L1"), new HandicappedSpot("H1"));
        List<ParkingSpot> floor2Spots = Arrays.asList(
                new CompactSpot("C2"), new LargeSpot("L2"), new HandicappedSpot("H2"));

        ParkingFloor floor1 = new ParkingFloor(floor1Spots);
        ParkingFloor floor2 = new ParkingFloor(floor2Spots);
        ParkingLot lot = new ParkingLot(Arrays.asList(floor1, floor2));

        while (true) {
            System.out.println("--Parking Lot System--");
            System.out.println("1. Park Vehicle");
            System.out.println("2. Unpark Vehicle");
            System.out.println("3. Show Available Spots");
            System.out.println("4. Exit");
            System.out.print("Enter Choice: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1:
                    System.out.print("Enter License Plate: ");
                    String plate = scanner.nextLine();

                    System.out.print("Enter Vehicle Type (CAR/BIKE/TRUCK): ");
                    String typeInput = scanner.nextLine().toUpperCase();

                    try {
                        VehicleType type = VehicleType.valueOf(typeInput);
                        Vehicle vehicle;
                        switch (type) {
                            case CAR:
                                vehicle = new Car(plate);
                                break;
                            case BIKE:
                                vehicle = new Bike(plate);
                                break;
                            case TRUCK:
                                vehicle = new Truck(plate);
                                break;
                            default:
                                throw new IllegalArgumentException("Unknown vehicle type");
                        }

                        ParkingTicket ticket = lot.parkVehicle(vehicle);
                        activeTickets.put(ticket.getTicketId(), ticket);
                        System.out.println("Vehicle Parked. Ticket ID: " + ticket.getTicketId());

                    } catch (IllegalArgumentException e) {
                        System.out.println("Invalid Vehicle Type! Please use only CAR, TRUCK, or BIKE.");
                    } catch (RuntimeException e) {
                        System.out.println("Parking is Full! Try again later.");
                    }
                    break;

                case 2:
                    System.out.print("Enter Ticket ID: ");
                    String ticketId = scanner.nextLine();
                    ParkingTicket ticket = activeTickets.get(ticketId);
                    if (ticket != null) {
                        lot.unparkVehicle(ticket);
                        activeTickets.remove(ticketId);
                    } else {
                        System.out.println("Ticket Not Found");
                    }
                    break;

                case 3:
                    lot.displayAvailability();
                    break;

                case 4:
                    System.out.println("Exiting the program...");
                    return;

                default:
                    System.out.println("Invalid Choice! Try Again.");
            }
        }
    }
}
//Some exceptions while running the code:
/*
1.When entered 1. in the enter choice got exception
2.

*/