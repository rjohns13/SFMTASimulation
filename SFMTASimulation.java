/** 
SFMTASimulation.java
CS 111B
Due: Wednesday, July 17, 2013, 11:55 PM
Group E: Teddy Deng, Katherine Soohoo, Rebecca A. Johnson
We assume the csv data files are in the same directory as this file.
*/

import java.io.*; // To use the PrintWriter class
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.StringTokenizer;


/**
SFMTASimulation class creates a program to simulate the operation
of the San Francisco MUNI bus and light rail vehicle system
*/
public class SFMTASimulation {
    
    /* 
    We create an ArrayList containing all of the stations, ordered by station ID. 
    */
    private ArrayList<Station> stations = new ArrayList<Station>();
    private ArrayList<Vehicle> vehicles = new ArrayList<Vehicle>();
    private ArrayList<Person>  people   = new ArrayList<Person>(); // or two: drivers and passengers?
    
    // these lines adopted from Vehicle.java main()
    // Creates a two-dimensional string array for all 7 routes.
    private String[][] v8xBayshoreRoute = createRouteArray("8xBayshore.csv");
    private String[][] v47VanNessRoute = createRouteArray("47VanNess.csv");
    private String[][] v49MissionRoute = createRouteArray("49Mission.csv");
    private String[][] vLTaravalRoute = createRouteArray("LTaraval.csv");
    private String[][] vNJudahRoute = createRouteArray("NJudah.csv");
    private String[][] vKTRoute = createRouteArray("KIngleside.csv", "TThird.csv");
    
    
    /**
    main method instantiates an object of the class and runs the simulation.
    @param String[] args
    */
    public static void main(String[] args) {    
        /*
        Instantiate an SFMTASimulation object. This allows us to use
        the instance variables, available to all the class's methods.
        */
        SFMTASimulation ourMuni = new SFMTASimulation();
        
        // this method puts everything in motion
        ourMuni.runSimulation();
        
    }
    
    private void runSimulation() {
        
        initializeStations();
        initializeVehicles();
        initializePassengers();
        initializeDrivers();
        
        //These two tasks together satisfy task 5
        printStationPeopleCount();
        printStationDriverCount();
        
        // Now kick off the movement and action
        
        
    }
    
    
    /**
    initializeStations method consumes our data files and instantiates
    Station objects, storing them in an array.
    */
    private void initializeStations() {
        //method variable declarations
        String inputStr;
        String name;
        int stationID;
        StringTokenizer strToken;
        int arrayIndex;
        /* set the following to null to avoid "variable *putFile might not have been initialized" compiler errors.
        */
        Scanner inputFile = null;
        Station thisStation;
        
        // Open the passengers file.
        try {
            File file = new File("passengers.csv");
            inputFile = new Scanner(file);
        }
        catch (FileNotFoundException e) {
            System.out.println("File passengers.csv not found.");
        }
        // Read until the end of the file.
        while (inputFile.hasNext())
        {
            inputStr = inputFile.nextLine();
            
            /* inputStr should look something like this:
            Isabella,17364,15204
            Passenger name, origin station ID, destination station ID
            We want to extract the name and origin to store in a Station object.
            */
            strToken = new StringTokenizer(inputStr, ",");
            
            name = strToken.nextToken();
            stationID = Integer.parseInt(strToken.nextToken());
            
            /* Now that we have the name of the passenger and place they're waiting, we'll create a new station object or add the passenger to the object containing this station ID, if it already exists.
            */
            if (stations.size() == 0) {
                //this is the first station - go ahead and add it to stations
                thisStation = new Station(stationID,name);
                stations.add(thisStation);
            }
            else if (stations.size() == 1) {
                /*
                findInArray works with at least two array elements. In this 
                case we just need to know if it's higher or lower than the one
                */
                
                //instantiate Station object
                thisStation = new Station(stationID,name);
                
                //add to before or after current element
                if (stationID > stations.get(0).getStationID()) {
                    stations.add(thisStation);
                }
                else {
                    stations.add(0,thisStation);
                }
            }
            else {
                
                arrayIndex = findInArray(stationID);
                
                if (arrayIndex >= 0) { //there is already an object for this station
                    // add this passenger to this station's queue
                    stations.get(arrayIndex).queuePassenger(name); 
                }
                else { // new station
                    // instantiate a new Station object
                    thisStation = new Station(stationID,name);
                    
                    /* now figure out where in array to put it. 
                    Beginning, end, or where in the middle?
                    */
                    if (stationID < stations.get(0).getStationID()) {
                        // Lowest ID, make it the first array element.
                        stations.add(0,thisStation);
                    }
                    else if (stationID >
                        stations.get(stations.size()-1).getStationID()) {
                        // Highest ID, make it the last array element.
                        stations.add(thisStation);
                    }
                    else {
                        /* find the right spot to put this new Station
                        in our sorted list
                        */
                        
                        arrayIndex = findSpotInArray(stationID);
                        
                        //now store it in array 
                        stations.add(arrayIndex,thisStation);
                    
                    }
                } // new station
            } // stations.size() > 0
        } // scanning through input file
        inputFile.close();// close the file when done.
    } // initializeStations method
    
    
    /**
    findInArray method searches for a station ID in stations.
    Utilizes binary search algorithm. 
    @param id the integer station ID
    @return the array index if found, otherwise -1.
    */
    private int findInArray(int id) {
        // declare and initialize method variables
        int highestIndex = stations.size()-1;
        int lowestIndex  = 0;
        int guessIndex = (highestIndex + lowestIndex ) / 2;
        int indexToReturn = -1;
        int guessID;
        boolean stopChecking = false;
        
        // look until we find it or run out of places to look
        while (!stopChecking && lowestIndex < highestIndex) {
        
            /* 
            Let's do a preliminary check to see if the ID is larger than the 
            largest unchecked ID in the array or smaller than the smallest
            unchecked ID in the array. If so, we know that it's not found.
            */
            if (id > stations.get(highestIndex).getStationID()  ||
                id < stations.get( lowestIndex).getStationID() ) {
                
                stopChecking = true; 
            }
            else {
                // Store the station ID of our guess in guessID
                guessID = stations.get(guessIndex).getStationID();
                
                if ( guessID == id ) {
                    indexToReturn = guessIndex;
                    stopChecking = true;
                }
                else if (guessID > id) {
                    // found ID too high; continue search below the guess index
                    highestIndex = guessIndex - 1;
                    guessIndex = (highestIndex + lowestIndex ) / 2;
                }
                else {
                    // found ID too low; continue search above the guess index
                    lowestIndex = guessIndex + 1;
                    guessIndex = (highestIndex + lowestIndex ) / 2;
                }
                
                /* We need to do this extra check, so while condition
                won't skip some cases.*/
                if (lowestIndex == highestIndex &&
                    id == stations.get(guessIndex).getStationID()) {
                    
                    stopChecking = true;
                    indexToReturn = guessIndex;
                }
                
            }
        } // while loop
            
        return indexToReturn;
        
    } // findInArray method
    
    
    /**
    findSpotInArray method searches for the spot where we should insert
    our new station ID into stations. It will be the index of the existing
    array element whose ID is just lower than our new ID.
    Utilizes algorithm similar to binary search. 
    @param newID the integer station ID
    @return the array index where we want to do the insert.
    */
    private int findSpotInArray(int newID) {
        // declare and initialize method variables
        int highestIndex = stations.size()-1;
        int lowestIndex  = 0;
        int guessIndex = (highestIndex + lowestIndex ) / 2;
        int guessID;
        boolean stopChecking = false;
        
        
        // look until we find it or run out of places to look
        while (!stopChecking && lowestIndex < highestIndex-1) {
            // Store the station ID of our guess in guessID
            guessID = stations.get(guessIndex).getStationID();
            
            if (guessID > newID) {
                // found ID too high; continue search below the guess index
                highestIndex = guessIndex ;
                guessIndex = (highestIndex + lowestIndex ) / 2;
            }
            else {
                // found ID too low; continue search above the guess index
                lowestIndex = guessIndex ;
                guessIndex = (highestIndex + lowestIndex ) / 2;
            }
        } // while loop
            
        return lowestIndex + 1;
        
    } // findSpotInArray method
    
       
    private void initializeVehicles() {
        // Instantiates the initial LRVs and Buses.
        LRV l8xBayshore = new LRV(v8xBayshoreRoute);
        LRV l47VanNess = new LRV(v47VanNessRoute);
        LRV l49Mission = new LRV(v49MissionRoute);
        Bus LTaraval = new Bus(vLTaravalRoute);
        Bus NJudah = new Bus(vNJudahRoute);
        Bus KInglesideTThird = new Bus(vKTRoute);
    }
    
    
    private void initializePassengers() {
        //File location into string
	String fileNamePassengers = "passengers.csv";
	
	//Creating passenger : an Array of Person objects.
	int totalNumPersonsPassenger = getTotalNumPassengersOrDrivers(fileNamePassengers);	//Getting size of file; this will be size of array of Person objects.
	Person[] passenger = new Person[totalNumPersonsPassenger];							//Creating Array of Person object with size.
	
	try {
		Scanner inputFile = new Scanner(new File(fileNamePassengers));
		
		for(int i = 0; inputFile.hasNextLine(); i++){
			
			String line = inputFile.nextLine();	//Stores line
			String[] tokens = line.split(",");		//Splitting tokens with comma delimiter ","
			
			//Using the person constructor the initialize each index (each person) with corresponding name, and ID's.
			passenger[i] = new Person(tokens[0], tokens[1], tokens[2], "Passenger");
		}
		
		inputFile.close();	//Close file when done.
		
	} catch (FileNotFoundException e) {
		System.out.println("File not found.");
	}
    }
    
    private void initializeDrivers() {
        //File location into string
        String fileNameDrivers = "drivers.csv";
        
        //Creating driver : an Array of Person objects.
	int totalNumPersonsDriver = getTotalNumPassengersOrDrivers(fileNameDrivers);	//Getting size of file; this will be size of array of Person objects.
	Person[] driver = new Person[totalNumPersonsDriver];							//Creating Array of Person object with size.
	
	try {
		Scanner inputFile = new Scanner(new File(fileNameDrivers));
		
		for(int i = 0; inputFile.hasNextLine(); i++){
			
			String line = inputFile.nextLine();	//Stores line
			String[] tokens = line.split(",");		//Splitting tokens with comma delimiter ","
			
			//Using the person constructor the initialize each index (each person) with corresponding name, and ID's.
			driver[i] = new Person(tokens[0], tokens[1], tokens[2], "Driver");
		}
		
		inputFile.close();	//Close file when done.
		
	} catch (FileNotFoundException e) {
		System.out.println("File not found.");
	}
    }
     
    /**
    printStationPeopleCount method
    For each station print out on one line, separated by a comma "," the Stop
    ID, and the number of passengers waiting. Save to StationPeopleCount.txt
    */
    private void printStationPeopleCount() {
        
        PrintWriter outputFile = null;
        
        try {
            outputFile = new PrintWriter("StationPeopleCount.txt");
        }
        catch (Exception e) {
            System.out.println("Error");
        }
        for (Station s : stations)
            outputFile.println(s.getStationID() + "," + s.getPassengerCount());
            
        outputFile.close();
        
        
    } // printStationPeopleCount()
    
    
    /**
    printStationDriverCount method
    For each origin or terminal station, print out on one line separated by a
    comma "," the Stop ID and the number of drivers waiting there.
    Save to StationDriverCount.txt
    */
    private void printStationDriverCount() {
        
    }
    
    
    /** 
     * The createRouteArray method reads a route file and returns a two-dimensional
     * array with the first column holding the route name and the second column
     * holding the stop ID.
     * @param fileName The name of the file.
     * @return routeArray A two-dimensional array separating the route name
     *          and stop ID into two columns.
     */
    public static String[][] createRouteArray(String fileName) {
        // Calls the getNumOfStops method to determine the number of stops.
        int numOfStops = getNumOfStops(fileName); 
        Scanner inputFile = null;
        
        // Creates a two-dimensional array with a length equal to the number of stops.
        String[][] routeArray = new String[numOfStops][numOfStops];
        
        String line;     // Store the line being read
        String[] tokens; // Stores the tokenized string
        
        try {
            inputFile = new Scanner(new File(fileName));
        }
        catch (FileNotFoundException e) {
            System.out.println("File not found.");
        }
        
        inputFile.nextLine(); // Skips first line
        
        for (int i = 0; inputFile.hasNext(); i++) {
            line = inputFile.nextLine();
            tokens = line.split(",");
            
            routeArray[i][0] = tokens[0]; // Stores the route name
            routeArray[i][1] = tokens[1]; // Stores the stop ID
        }
        
        inputFile.close();
        
        return routeArray;
    }
    
    /** 
     * The createRouteArray method reads two route files and returns a two-dimensional
     * array with the first column holding the route name and the second column
     * holding the stop ID.
     * @param fileName The name of the first file.
     * @param fileName2 The name of the second file.
     * @return routeArray A two-dimensional array separating the route name
     *          and stop ID into two columns.
     */
    public static String[][] createRouteArray(String fileName, String fileName2) {
        // Calls the getNumOfStops method to determine the number of stops.
        int numOfStops = getNumOfStops(fileName, fileName2);    
        
        int fileOneStopNum = getNumOfStops(fileName);
        
        Scanner inputFile = null;
        Scanner inputFile2 = null;
        
        // Creates a two-dimensional array with a length equal to the number of stops.
        String[][] routeArray = new String[numOfStops][numOfStops];
        
        String line;     // Store the line being read
        String[] tokens; // Stores the tokenized string
        
        try {
            inputFile = new Scanner(new File(fileName));
            inputFile2 = new Scanner(new File(fileName2));
        }
        catch (FileNotFoundException e) {
            System.out.println("File(s) not found.");
        }
        
        inputFile.nextLine(); // Skips first line
        
        for (int i = 0; inputFile.hasNext(); i++) {
            line = inputFile.nextLine();
            tokens = line.split(",");
            
            routeArray[i][0] = tokens[0]; // Stores the route name
            routeArray[i][1] = tokens[1]; // Stores the stop ID
        }
        
        inputFile.close();
        
        // Adds the stops from the second file to the array.
        inputFile2.nextLine();
        inputFile2.nextLine(); // Skips the first two lines
        
        for (int i = 0; inputFile2.hasNext(); i++) {
            line = inputFile2.nextLine();
            tokens = line.split(",");
            
            routeArray[fileOneStopNum + i][0] = tokens[0]; // Stores the route name
            routeArray[fileOneStopNum + i][1] = tokens[1]; // Stores the stop ID
        }
        
        inputFile2.close();
        
        return routeArray;
    }
    
    /**
     * The getNumOfStops reads a route file and counts the number of stops.
     * @param fileName The name of the file.
     * @return stopCount The number of stops in the route.
     */
    public static int getNumOfStops(String fileName) {
        int stopCount = 0;  // Counter for stops in a route
        Scanner inputFile = null;
        
        try {
            inputFile = new Scanner(new File(fileName));
        }
        catch (FileNotFoundException e) {
            System.out.println("File " + fileName + " not found.");
        }
        
        inputFile.nextLine(); // Skips the first line
        
        while (inputFile.hasNextLine()) {
            inputFile.nextLine();   // Reads next line in the file
            stopCount++;            // Adds to the stops counter
        }
        
        inputFile.close();
        
        return stopCount;
    }
    
    /**
     * The getNumOfStops reads two route files and counts the number of stops.
     * @param fileName The name of the file.
     * @param fileName2 The name of the second file.
     * @return stopCount The number of stops in the route.
     */
    public static int getNumOfStops(String fileName, String fileName2) {
        int stopCount = 0;  // Counter for stops in a route
        
        Scanner inputFile = null;
        Scanner inputFile2 = null;
        
        try {
            inputFile = new Scanner(new File(fileName));
            inputFile2 = new Scanner(new File(fileName2));
        }
        catch (FileNotFoundException e) {
            System.out.println("File(s) not found.");
        }
        
        inputFile.nextLine(); // Skips the first line
        
        while (inputFile.hasNextLine()) {
            inputFile.nextLine();   // Reads next line in the file
            stopCount++;            // Adds to the stops counter
        }
        
        inputFile.close();
        
        // Reads the number of stops from the second file and adds to counter.
        inputFile2.nextLine();
        inputFile2.nextLine(); // Skips the first two lines
        
        while (inputFile2.hasNextLine()) {
            inputFile2.nextLine();   // Reads next line in the file
            stopCount++;             // Adds to the stops counter
        }
        
        inputFile2.close();
        
        return stopCount;
    }
    
    /**
	 * Method calculates and returns the number of lines in a file; Num of lines determines how many "people" are present in file.
	 * @param filename Name of file we will read.
	 * @return An integer that will influence size of an Array of Person objects
	 */
	public static int getTotalNumPassengersOrDrivers(String filename){
		
		int personCount = 0;	//Set flag to 0.
		
		try {
			Scanner inputFile = new Scanner(new File(filename));
			
			while(inputFile.hasNextLine()){
				inputFile.nextLine();			//Move to next line.
				personCount++;
			}
			
			inputFile.close();
			
		} catch (FileNotFoundException e) {
			System.out.println("File not found.");
		}
		
		return personCount;	//Return the count
	}
    
} // SFMTASimulation


class Station {
    
    private int stationID; // unique station identifier
    private ArrayList<String> passengers; // passengers waiting at station
    private ArrayList<String> drivers;    // drivers waiting at station
    private boolean isOriginOrTerminus;   
    
    /**
    constructor, no-argument
    This shouldn't be called, as we probably always want the station ID provided. 
    */
    public Station() {
        
        stationID  = 0;
        passengers = new ArrayList<String>();
        drivers    = new ArrayList<String>();
        isOriginOrTerminus = false;
    }
    
    /**
    constructor, one-argument
    @param id int station ID.
    */
    public Station(int id) {
        
        stationID  = id;
        passengers = new ArrayList<String>();
        drivers    = new ArrayList<String>();
        isOriginOrTerminus = false;
    }
    
    
    /**
    constructor, two-argument
    @param id int station ID.
    @param p String passenger name to add to passengers.
    */
    public Station(int id, String p) {
        
        stationID  = id;
        passengers = new ArrayList<String>();
        drivers    = new ArrayList<String>();
        isOriginOrTerminus = false;
        
        passengers.add( p);
    }
    
    
    /**
    constructor, three-argument
    @param id int station ID.
    @param d String driver name to add to drivers.
    @param driverFlag boolean, indicates this new station has a driver waiting
    */
    public Station(int id, String d, boolean driverFlag) {
        
        stationID  = id;
        passengers = new ArrayList<String>();
        drivers    = new ArrayList<String>();
        isOriginOrTerminus = true; // drivers wait at origin or terminus
        
        drivers.add( d);
    }
    
    
    /**
    getStationID method
    @return the int station ID of this station
    */
    public int getStationID() {
        
        return stationID;
    }
    
    
    /**
    getPassengerCount method
    @return the int count of passengers at this station
    */
    public int getPassengerCount() {
        
        return passengers.size();
    }
    
    
    /**
    getDriverCount method
    @return the int count of drivers at this station
    */
    public int getDriverCount() {
        
        return drivers.size();
    }
    
    
    /**
    getIsOriginOrTerminus method
    @return the boolean flag of whether station is origin or terminus
    */
    public boolean getIsOriginOrTerminus() {
        
        return isOriginOrTerminus;
    }
    
    
    /**
    queuePassenger method
    Called when we have a new passenger waiting at this station.
    @param p String passenger to add to passengers
    */
    public void queuePassenger(String p) {
        
        passengers.add(p);
    }
    
    
    /**
    queueDriver method
    Called when we have a new driver waiting at this station.
    @param d String driver to add to drivers
    */
    public void queueDriver(String d) {
        
        drivers.add(d);
    }
    
    
    /**
    popPassenger method
    Called when the first passenger in line has left this station.
    */
    public void popPassenger() {
        
        passengers.remove(0);
    }
    
    
    /**
    popDriver method
    Called when the first driver in line has left this station.
    */
    public void popDriver() {
        
        drivers.remove(0);
    }
    
    
    /**
    setIsOriginOrTerminus method
    @param is - the boolean flag of whether station is origin or terminus
    */
    public void setIsOriginOrTerminus(boolean is) {
        
        isOriginOrTerminus = is;
    }
    
        
}


class Vehicle {

    private enum Direction {INBOUND, OUTBOUND}

    private int idNumber;           // The identification number of the vehicle
    private int stopID;             // The stop ID of where the vehicle is currently located
    private int stopIndex;          // Counter to keep track of stops made
    private Direction vehicleDir;   // Direction of the vehicle
    
    private int numOfCoaches;   // Number of coaches for a vehicle
    private int maxCapacity;    // Maximum number of passengers a vehicle can hold
    
    private ArrayList<String> passengerList; // List of passenger aboard the vehicle
    private String[][] routeList;            // List of stations where the vehicle stops
    
    /**
     * No-Arg Constructor
     */
    public Vehicle() {
        idNumber = 0;
        stopID = 0;
        stopIndex = 0;
        vehicleDir = Direction.INBOUND;
        
        numOfCoaches = 0;
        maxCapacity = 0;
        
        passengerList = new ArrayList<String>(0);
        routeList = new String[0][0];
    }
    
    /**
     * One-Argument Constructor
     * @param route The route that the object will follow.
     */
    public Vehicle(String[][] route) {
        idNumber = generateRandIDNum();
        stopID = Integer.parseInt(route[0][1]);
        stopIndex = 0;
        vehicleDir = Direction.INBOUND;
        
        numOfCoaches = generateRandCoachNum();
        maxCapacity = numOfCoaches * 20;
        
        passengerList = new ArrayList<String>(maxCapacity);
        routeList = route;
    }
    
    /**
     * The generateRandIDNum generates and returns a random number ranging
     * from 1 to 999.
     */
    private int generateRandIDNum() {
        Random randGenerator = new Random();
        int randNum = randGenerator.nextInt(998) + 1;
        return randNum;
    }
    
    /**
     * The generateRandCoachNum generates a random number from 1 to 2.
     */
    private int generateRandCoachNum() {
        Random randGenerator = new Random();
        int randNum = randGenerator.nextInt(2) + 1;
        return randNum;
    }
    
    /**
     * The getIDNumber method returns the object's ID number.
     * @return An integer value of the object's ID number.
     */
    public int getIDNumber() {
        return idNumber;
    }
    
    /**
     * The getStopID method returns the object's the id of the current stop.
     * @return An integer value of the object's current stop id.
     */
    public int getStopID() {
        return stopID;
    }
    
    /**
     * The getDirection method retursn the object's current direction.
     * @return The Direction type of the object (INBOUND or OUTBOUND)
     */
    public Direction getDirection() {
        return vehicleDir;
    }
    
    /**
     * The getNumOfCoaches method returns the object's number of coaches.
     * @return An integer value of the number of coaches the object has.
     */
    public int getNumOfCoaches() {
        return numOfCoaches;
    }
    
    /**
     * The getMaxCapacity method returns the object's max capacity of passengers.
     * @return An integer value of the object's maximum capacity.
     */
    public int getMaxCapacity() {
        return maxCapacity;
    }
    
    /**
     * The getNumOfPassengers method returns the number of passengers in the vehicle.
     * @return An integer value of the current of number of passengers in the vehicle.
     */
    public int getNumOfPassengers() {
        return passengerList.size();
    }
    
    /**
     * The getPassengerList method returns an array of the passengers' name
     * in each element.
     * @return list An array of the passengers' name. 
     */
    public String[] getPassengerList() {
        String[] list = new String[passengerList.size()];
    
        for (int i = 0; i < list.length; i++)
            list[i] = passengerList.get(i);
            
        return list;
    }
    
    /**
     * The addPassenger methods adds a passenger to the vehicle.
     * @param name The name of the passenger.
     */
    public void addPassenger(String name) {
        if (getNumOfPassengers() == getMaxCapacity())
            System.out.println("The vehicle is full!");
        else
            passengerList.add(name);
    }
    
    /**
     * The removePassenger method removes a passenger from the vehicle.
     * @param name The name of the passenger.
     */
    public void removePassenger(String name) {
        for (int i = 0; i < passengerList.size(); i++) {
            if (name.equals(passengerList.get(i)))
                passengerList.remove(i);
            else
                System.out.println("Passenger does not exist!");
        }
    }
    
    /**
     * The goToNextStop method moves the vehicle to each stop listed in the route.
     * Once an origin or terminus is reached, the vehicle changes direction.
     */
    public void goToNextStop() {
        // If vehicle is INBOUND, move forward in the array.
        if (vehicleDir.equals(Direction.INBOUND)) {
            if (stopIndex < routeList.length - 1) {
                stopIndex++;    
            }
            else {
                switchDirection();
                stopIndex--;
            }
        }
        
        // If vehicle is OUTBOUND, move backwards in the array.
        else if (vehicleDir.equals(Direction.OUTBOUND)) {
            if (stopIndex != 0) {
                stopIndex--;
            }
            else {
                switchDirection();
                stopIndex++;
            }
        }
        
        // Store the current station's stop ID.
        stopID = Integer.parseInt(routeList[stopIndex][1]);    
    }
    
    /**
     * The switchDirection method changes the vehicle's direction.
     */
    public void switchDirection() {
        if (vehicleDir.equals(Direction.INBOUND))
            vehicleDir = Direction.OUTBOUND;
        else if (vehicleDir.equals(Direction.OUTBOUND))
            vehicleDir = Direction.INBOUND;
    }

}

class Bus extends Vehicle {

    private enum Type {BUS}
    Type vType;

    public Bus() {
        super();
        vType = Type.BUS;
    }
    
    public Bus(String[][] route) {
        super(route);
        vType = Type.BUS;
    }
}

class LRV extends Vehicle {

    private enum Type {LRV}
    Type vType;

    public LRV() {
        super();
        vType = Type.LRV;
    }
    
    public LRV(String[][] route) {
        super(route);
        vType = Type.LRV;
    }
}

class Person {

	private String currentStationID;	//Store string retrieved from vehicle class
	private String currentVehicleID;	
	private String startID;				//Initializes when an instance of Person in created
	private String stopID;
	private String name;
	private String personType;			//Later on when we need to transfer/ get off... might need (could use enums, dont know how)
	
	/**
	 * No arg constructor
	 */
	public Person(){

		startID = null;
		stopID = null;
		String name = null;
		System.out.println("Error, please enter name of person.");
	}
	
	/**
	 * Constructor that creates individual person classes.
	 * @param nameTag Assign to name.
	 * @param beginPos Assign to start ID.
	 * @param endPos Assign to Stop ID.
	 * @param typeOfPerson Assign to personType.
	 */
	public Person(String nameTag, String beginPos, String endPos, String typeOfPerson){
		
		setStartID(beginPos);
		setStopID(endPos);
		setName(nameTag);
		personType = typeOfPerson;
	}
		
	public void setStartID(String startPos){
		
		startID = startPos;
	}
	
	public void setStopID(String endPos){
		
		stopID = endPos;
	}
	
	public void setName(String nameTag){
		
		name = nameTag;
	}
	
	public String getStartID(){
		
		return startID;
	}
	
	public String getStopID(){
		
		return stopID;
	}
	
	public String getName(){
		
		return name;
	}
	
	public void setCurrentStationID(String stationID){
		
		//Needs to access vehicle class, get station ("location") id.
	}
	
	public void setCurrentVehicleID(String vehicleID){
		//Needs to access vehicle class, get vehicle id.
	}
	
	public String getCurrentStationID(){
		
		return currentStationID;
	}
	
	public String getCurrentVehicleID(){
		
		return currentVehicleID;
	}
	
	public String getPersonType(){
		
		return personType;
	}
}

