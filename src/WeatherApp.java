import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Scanner;

// Abstract class for weather data
abstract class WeatherData {
    protected String location; // This is a variable to hold the location of the weather data


    public WeatherData(String location) {
        this.location = location;
    }

    public abstract void displayWeatherInfo();
}

// Subclass for forecast data that extends WeatherData
class Forecast extends WeatherData {
    private ArrayList<String> forecastPeriods;

    public Forecast(String location) {
        super(location);
        this.forecastPeriods = new ArrayList<>();
    }
    // Method to add a forecast to the list
    public void addForecast(String forecast) {
        forecastPeriods.add(forecast);
    }
    // Override method to display weather information to the user
    @Override
    public void displayWeatherInfo() {
        System.out.println();
        System.out.println("Forecast for " + location + ":");
        for (String forecast : forecastPeriods) {
            System.out.println(forecast);
        }
    }
}

public class WeatherApp {
    //This is the URL for the weather API that I used
    private static final String BASE_URL = "https://api.weather.gov";
    private static final Scanner scanner = new Scanner(System.in);

    //Main loop for user interaction
    public static void main(String[] args) {
        while (true) {
            System.out.println("*----------------------------*");
            System.out.println("Welcome to the Weather Channel");
            System.out.println("*----------------------------*");
            System.out.println();
            System.out.println("1. Get the Forecast for Rexburg, Idaho");
            System.out.println("2. Get the Forecast for a Custom Location");
            System.out.println("3. Close the Program");
            System.out.println();
            System.out.print("Please select an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                getForecast("43.826", "-111.7897"); // Default coordinates for Rexburg, ID
                break;
            } else if (choice == 2) {
                System.out.print("Enter Latitude: ");
                String latitude = scanner.nextLine();
                System.out.print("Enter Longitude: ");
                String longitude = scanner.nextLine();
                getForecast(latitude, longitude);
                break;
            } else if (choice == 3) {
                System.out.println("Closing program. . .");
                break;
            } else {
                System.out.println("This is not a valid choice.");
            }
        }
    }

    // Method to retrieve the weather forecast based on latitude and longitude
    private static void getForecast(String latitude, String longitude) {
        try {
            // Construct the URL for the points request using the provided latitude and longitude
            String pointUrl = BASE_URL + "/points/" + latitude + "," + longitude;
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pointUrl))
                    .header("User-Agent", "Java Weather App")
                    .build();
            // Send the request and receive the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Check if the request was successful
            if (response.statusCode() == 200) {
                // Parse the response JSON to extract grid ID and coordinates
                JSONObject jsonObj = new JSONObject(response.body());
                String gridId = jsonObj.getJSONObject("properties").getString("gridId");
                int gridX = jsonObj.getJSONObject("properties").getInt("gridX");
                int gridY = jsonObj.getJSONObject("properties").getInt("gridY");

                // Construct the URL for the forecast request using the grid information
                String forecastUrl = BASE_URL + "/gridpoints/" + gridId + "/" + gridX + "," + gridY + "/forecast";
                HttpRequest forecastRequest = HttpRequest.newBuilder()
                        .uri(URI.create(forecastUrl))
                        .header("User-Agent", "Java Weather App")
                        .build();
                // Send the forecast request and get the response
                HttpResponse<String> forecastResponse = client.send(forecastRequest, HttpResponse.BodyHandlers.ofString());

                // Check if the forecast request was successful
                if (forecastResponse.statusCode() == 200) {
                    JSONObject forecastObj = new JSONObject(forecastResponse.body());
                    JSONArray periods = forecastObj.getJSONObject("properties").getJSONArray("periods");
                    Forecast forecastData = new Forecast(latitude + ", " + longitude);

                        JSONObject period = periods.getJSONObject(0);
                        String detailedForecast = period.getString("detailedForecast");
                        forecastData.addForecast(detailedForecast);


                    forecastData.displayWeatherInfo();

                //These else and catch statements handle unsuccessful forecast requests
                } else {
                    System.out.println("Failed to retrieve forecast data. Status code: " + forecastResponse.statusCode());
                }
            } else {
                System.out.println("Failed to retrieve points data. Status code: " + response.statusCode());
            }
        } catch (Exception e) {
            System.out.println("An error occurred while retrieving weather data: " + e.getMessage());
        }
    }
}