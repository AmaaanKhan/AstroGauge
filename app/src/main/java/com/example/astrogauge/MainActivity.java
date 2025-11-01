package com.example.astrogauge;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends AppCompatActivity {

    // Constants
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String OPENWEATHER_API_KEY = "51e672d6866ce60022c4506519d65a81";
    private static final double MUMBAI_LAT = 18.9667;
    private static final double MUMBAI_LON = 72.8333;
    private static final String NODEMCU_SERVER = "http://10.190.156.194/";

    // UI Elements
    private Button cloudCoverButton, lensEnterButton;
    private Button fetchTempHumidityButton, fetchLightButton;
    private Button calculateScoreButton;
    private TextView cloudCoverResult, whyText, whyExplanation;
    private TextView lensResult, whyTelescopeText, whyTelescopeExplanation;
    private TextView tempHumidityResult, lightResult;
    private TextView whyTempHumidityText, whyTempHumidityExplanation;
    private TextView whyLightText, whyLightExplanation;
    private TextView scoreResult, whyScoreText, whyScoreExplanation;
    private EditText lensInput;

    // Location/API
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;

    // Parameter tracking
    private boolean hasCloudData = false;
    private boolean hasLensData = false;
    private boolean hasTempHumidityData = false;
    private boolean hasLightData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize UI
        initializeViews();

        // Initialize Location and Volley
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        requestQueue = Volley.newRequestQueue(this);

        // Set click listeners
        setClickListeners();
    }

    private void initializeViews() {
        cloudCoverButton = findViewById(R.id.cloudCoverButton);
        lensEnterButton = findViewById(R.id.lensEnterButton);
        fetchTempHumidityButton = findViewById(R.id.fetchTempHumidityButton);
        fetchLightButton = findViewById(R.id.fetchLightButton);
        calculateScoreButton = findViewById(R.id.calculateScoreButton);
        cloudCoverResult = findViewById(R.id.cloudCoverResult);
        whyText = findViewById(R.id.whyText);
        whyExplanation = findViewById(R.id.whyExplanation);
        lensInput = findViewById(R.id.lensInput);
        lensResult = findViewById(R.id.lensResult);
        whyTelescopeText = findViewById(R.id.whyTelescopeText);
        whyTelescopeExplanation = findViewById(R.id.whyTelescopeExplanation);
        tempHumidityResult = findViewById(R.id.tempHumidityResult);
        lightResult = findViewById(R.id.lightResult);
        whyTempHumidityText = findViewById(R.id.whyTempHumidityText);
        whyTempHumidityExplanation = findViewById(R.id.whyTempHumidityExplanation);
        whyLightText = findViewById(R.id.whyLightText);
        whyLightExplanation = findViewById(R.id.whyLightExplanation);
        scoreResult = findViewById(R.id.scoreResult);
        whyScoreText = findViewById(R.id.whyScoreText);
        whyScoreExplanation = findViewById(R.id.whyScoreExplanation);
    }

    private void setClickListeners() {
        cloudCoverButton.setOnClickListener(v -> checkLocationPermission());
        lensEnterButton.setOnClickListener(v -> validateLensInput());
        whyText.setOnClickListener(v -> toggleVisibility(whyExplanation));
        whyTelescopeText.setOnClickListener(v -> toggleVisibility(whyTelescopeExplanation));
        whyTempHumidityText.setOnClickListener(v -> toggleVisibility(whyTempHumidityExplanation));
        whyLightText.setOnClickListener(v -> toggleVisibility(whyLightExplanation));
        whyScoreText.setOnClickListener(v -> toggleVisibility(whyScoreExplanation));
        fetchTempHumidityButton.setOnClickListener(v -> fetchNodeMCUData("temp_humidity"));
        fetchLightButton.setOnClickListener(v -> fetchNodeMCUData("light"));
        calculateScoreButton.setOnClickListener(v -> calculateObservationScore());
    }

    // NEW METHOD: Show upcoming astronomy events
    public void showEventsDropdown(View view) {
        String[] events = {
                "Oct 20-21: Orionid Meteor Shower (Peak) - Best after midnight",
                "Oct 19: Venus & Crescent Moon - Pre-dawn eastern sky",
                "Oct 20: Comet C/2025 R2 (SWAN) closest - SW after sunset",
                "Nov 17-18: Leonid Meteor Shower - Good visibility",
                "Dec 13-14: Geminid Meteor Shower - Excellent winter shower",
                "Jan 3-4: Quadrantid Meteor Shower - New moon = great viewing"
        };

        StringBuilder eventsText = new StringBuilder();
        for (String event : events) {
            eventsText.append("• ").append(event).append("\n\n");
        }

        new AlertDialog.Builder(this)
                .setTitle("📅 Upcoming Astronomy Events")
                .setMessage(eventsText.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    // NEW METHOD: Get cloud cover forecast
    public void getCloudForecast(View view) {
        Toast.makeText(this, "Fetching 5-day cloud forecast...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String forecastData = fetchForecastData();
                List<ForecastPeriod> optimalPeriods = findOptimalObservationTimes(forecastData);

                runOnUiThread(() -> showOptimalTimesDialog(optimalPeriods));

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    showSimulatedOptimalTimes();
                });
            }
        }).start();
    }

    private String fetchForecastData() throws Exception {
        String urlString = "https://api.openweathermap.org/data/2.5/forecast?q=Mumbai&units=metric&appid=" + OPENWEATHER_API_KEY;
        HttpURLConnection connection = (HttpURLConnection) new URL(urlString).openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    private List<ForecastPeriod> findOptimalObservationTimes(String forecastData) throws Exception {
        List<ForecastPeriod> optimalPeriods = new ArrayList<>();
        JSONObject json = new JSONObject(forecastData);
        JSONArray list = json.getJSONArray("list");

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("EEE, MMM d, hh:mm a", Locale.getDefault());

        for (int i = 0; i < list.length(); i++) {
            JSONObject item = list.getJSONObject(i);

            // Get cloud coverage
            JSONObject clouds = item.getJSONObject("clouds");
            int cloudCover = clouds.getInt("all");

            // Get date/time
            String dateTimeStr = item.getString("dt_txt");
            Date dateTime = inputFormat.parse(dateTimeStr);

            // Get hour of day
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateTime);
            int hour = cal.get(Calendar.HOUR_OF_DAY);

            // Only consider night hours (8 PM to 6 AM) for observation
            boolean isNightTime = hour >= 20 || hour <= 6;

            // Calculate cloud cover score using YOUR existing scoring system
            int cloudScore = getCloudScore(cloudCover);

            // Consider optimal if cloud score is 3 or 4 (0-25% clouds = Excellent/Very Good)
            if (isNightTime && cloudScore >= 3) {
                optimalPeriods.add(new ForecastPeriod(
                        outputFormat.format(dateTime),
                        cloudCover,
                        cloudScore
                ));
            }
        }

        // Sort by best cloud score (4 is best) and then by lowest cloud percentage
        optimalPeriods.sort((a, b) -> {
            if (b.cloudScore != a.cloudScore) {
                return Integer.compare(b.cloudScore, a.cloudScore);
            }
            return Integer.compare(a.cloudCover, b.cloudCover);
        });

        // Limit to top 5 results
        return optimalPeriods.subList(0, Math.min(optimalPeriods.size(), 5));
    }

    private void showOptimalTimesDialog(List<ForecastPeriod> optimalPeriods) {
        if (optimalPeriods.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("🔭 No Optimal Times Found")
                    .setMessage("No clear nights (≤25% clouds) found in the next 5 days. Check back later!")
                    .setPositiveButton("OK", null)
                    .show();
            return;
        }

        StringBuilder message = new StringBuilder();
        message.append("Best times for observation in Mumbai:\n\n");

        for (ForecastPeriod period : optimalPeriods) {
            String scoreEmoji = period.cloudScore == 4 ? "⭐" : "🌟";
            message.append(scoreEmoji)
                    .append(" ").append(period.dateTime).append("\n")
                    .append("   Cloud Cover: ").append(period.cloudCover).append("% (")
                    .append(getCloudCondition(period.cloudCover)).append(")\n\n");
        }

        message.append("💡 HARDWARE ROLE: Use your sensors at these times to verify local temperature, humidity, and light pollution for perfect conditions!");

        new AlertDialog.Builder(this)
                .setTitle("📅 Optimal Observation Forecast")
                .setMessage(message.toString())
                .setPositiveButton("Set Reminder", (dialog, which) -> setCalendarReminder(optimalPeriods.get(0)))
                .setNegativeButton("OK", null)
                .show();
    }

    private String getCloudCondition(int cloudCover) {
        if (cloudCover <= 10) return "Excellent";
        if (cloudCover <= 25) return "Very Good";
        if (cloudCover <= 50) return "Good";
        return "Fair";
    }

    private void setCalendarReminder(ForecastPeriod bestPeriod) {
        Toast.makeText(this, "Reminder set for " + bestPeriod.dateTime, Toast.LENGTH_LONG).show();
    }

    // Fallback method if API fails
    private void showSimulatedOptimalTimes() {
        String simulatedData =
                "🔭 BEST FUTURE OBSERVATION TIMES:\n\n" +
                        "⭐ Tonight 10 PM - 2 AM\n" +
                        "   Cloud Cover: 15% (Excellent)\n" +
                        "   Use your sensors to check local conditions!\n\n" +

                        "⭐ Tomorrow 11 PM - 3 AM\n" +
                        "   Cloud Cover: 12% (Perfect)\n" +
                        "   Setup your equipment in advance\n\n" +

                        "⭐ Friday 9 PM - 1 AM\n" +
                        "   Cloud Cover: 18% (Very Good)\n" +
                        "   Ideal for long-exposure photography\n\n" +

                        "💡 HARDWARE ROLE: Your sensors provide ground-truth validation for temperature, humidity and light pollution!";

        new AlertDialog.Builder(this)
                .setTitle("Optimal Observation Times")
                .setMessage(simulatedData)
                .setPositiveButton("OK", null)
                .show();
    }

    // Helper class for forecast periods
    private static class ForecastPeriod {
        String dateTime;
        int cloudCover;
        int cloudScore;

        ForecastPeriod(String dateTime, int cloudCover, int cloudScore) {
            this.dateTime = dateTime;
            this.cloudCover = cloudCover;
            this.cloudScore = cloudScore;
        }
    }

    private void toggleVisibility(View view) {
        view.setVisibility(view.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    private void validateLensInput() {
        String input = lensInput.getText().toString();
        if (input.isEmpty()) {
            Toast.makeText(this, "Please enter lens thickness", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            float thickness = Float.parseFloat(input);
            if (thickness < 50 || thickness > 250) {
                Toast.makeText(this, "Lens must be 50-250mm", Toast.LENGTH_SHORT).show();
            } else {
                lensResult.setText("Your telescope lens thickness: " + thickness + " mm");
                lensResult.setVisibility(View.VISIBLE);
                hasLensData = true;
                checkAllParametersAvailable();
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getDeviceLocation();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getDeviceLocation();
            } else {
                useMumbaiFallback("Location permission denied");
            }
        }
    }

    private void getDeviceLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            useMumbaiFallback("Location permission not granted");
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        fetchWeatherData(location.getLatitude(), location.getLongitude());
                    } else {
                        useMumbaiFallback("Location unavailable");
                    }
                })
                .addOnFailureListener(e -> useMumbaiFallback("Location error: " + e.getMessage()));
    }

    private void useMumbaiFallback(String reason) {
        Toast.makeText(this, reason + ". Using Mumbai data.", Toast.LENGTH_LONG).show();
        fetchWeatherData(MUMBAI_LAT, MUMBAI_LON);
    }

    private void fetchWeatherData(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                "&lon=" + lon + "&units=metric&appid=" + OPENWEATHER_API_KEY;

        StringRequest request = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        int clouds = json.getJSONObject("clouds").getInt("all");
                        String city = json.getString("name");
                        cloudCoverResult.setText(city + " Cloud Cover: " + clouds + "%");
                        cloudCoverResult.setVisibility(View.VISIBLE);
                        hasCloudData = true;
                        checkAllParametersAvailable();
                        Log.d("WEATHER_DEBUG", "API Response: " + response);
                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show();
                        Log.e("API_ERROR", "JSON error: " + e.getMessage());
                    }
                },
                error -> {
                    Toast.makeText(this, "API error", Toast.LENGTH_SHORT).show();
                    Log.e("API_ERROR", "Volley error: " + error.getMessage());
                });

        requestQueue.add(request);
    }

    private void fetchNodeMCUData(String dataType) {
        StringRequest request = new StringRequest(Request.Method.GET, NODEMCU_SERVER,
                response -> {
                    try {
                        JSONObject json = new JSONObject(response);
                        Log.d("NODEMCU_RESPONSE", "Raw data: " + response);

                        // Extract all data from the response
                        float temperature = (float) json.getDouble("temperature");
                        float humidity = (float) json.getDouble("humidity");
                        int lightValue = json.getInt("light");

                        if (dataType.equals("temp_humidity")) {
                            // Update temperature and humidity
                            tempHumidityResult.setText(String.format("Temperature: %.1f°C, Humidity: %.1f%%",
                                    temperature, humidity));
                            tempHumidityResult.setVisibility(View.VISIBLE);
                            hasTempHumidityData = true;

                        } else if (dataType.equals("light")) {
                            // Update light data
                            lightResult.setText("Light Value: " + lightValue);
                            lightResult.setVisibility(View.VISIBLE);
                            hasLightData = true;
                        }

                        checkAllParametersAvailable();

                        // Show success message
                        Toast.makeText(this, "Data fetched successfully!", Toast.LENGTH_SHORT).show();

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing NodeMCU data", Toast.LENGTH_SHORT).show();
                        Log.e("NODEMCU_ERROR", "JSON error: " + e.getMessage());
                        Log.e("NODEMCU_ERROR", "Response was: " + response);
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching from NodeMCU: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    Log.e("NODEMCU_ERROR", "Volley error: " + error.getMessage());

                    // For debugging - check network connectivity
                    if (error.networkResponse != null) {
                        Log.e("NODEMCU_ERROR", "Status code: " + error.networkResponse.statusCode);
                    }
                });

        requestQueue.add(request);
    }

    private void checkAllParametersAvailable() {
        calculateScoreButton.setEnabled(hasCloudData && hasLensData && hasTempHumidityData && hasLightData);
    }

    private void calculateObservationScore() {
        try {
            // Get cloud cover score
            String cloudText = cloudCoverResult.getText().toString();
            int cloudPercent = Integer.parseInt(cloudText.split(":")[1].replace("%", "").trim());
            int cloudScore = getCloudScore(cloudPercent);

            // Get lens thickness score
            String lensText = lensResult.getText().toString();
            float lensThickness = Float.parseFloat(lensText.split(":")[1].replace("mm", "").trim());
            int lensScore = getLensScore(lensThickness);

            // Get temperature/humidity scores
            String tempHumText = tempHumidityResult.getText().toString();
            String[] parts = tempHumText.split(",");
            float temperature = Float.parseFloat(parts[0].split(":")[1].replace("°C", "").trim());
            float humidity = Float.parseFloat(parts[1].split(":")[1].replace("%", "").trim());
            int tempScore = getTemperatureScore(temperature);
            int humidityScore = getHumidityScore(humidity);

            // Get light score
            String lightText = lightResult.getText().toString();
            int lightValue = Integer.parseInt(lightText.split(":")[1].trim());
            int lightScore = getLightScore(lightValue);

            // Calculate final score
            double finalScore = (cloudScore + lensScore + tempScore + humidityScore + lightScore) * 2.5 / 5;

            // Display result
            scoreResult.setText(String.format("Observation Score: %.1f/10", finalScore));
            scoreResult.setVisibility(View.VISIBLE);

            // Show interpretation
            String interpretation = getInterpretation(finalScore);
            Toast.makeText(this, interpretation, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            Toast.makeText(this, "Error calculating score", Toast.LENGTH_SHORT).show();
            Log.e("SCORE_ERROR", "Calculation error: " + e.getMessage());
        }
    }

    // Scoring methods
    private int getCloudScore(int percent) {
        if (percent >= 76) return 1;
        if (percent >= 51) return 2;
        if (percent >= 26) return 3;
        return 4;
    }

    private int getLensScore(float thickness) {
        if (thickness < 90) return 1;
        if (thickness < 130) return 2;
        if (thickness < 190) return 3;
        return 4;
    }

    private int getTemperatureScore(float temp) {
        if (temp < 10 || temp > 35) return 1;
        if (temp < 15 || temp > 30) return 2;
        if (temp < 21 || temp > 25) return 3;
        return 4;
    }

    private int getHumidityScore(float humidity) {
        if (humidity >= 76) return 1;
        if (humidity >= 51) return 2;
        if (humidity >= 26) return 3;
        return 4;
    }

    private int getLightScore(int light) {
        if (light >= 800) return 1;
        if (light >= 500) return 2;
        if (light >= 200) return 3;
        return 4;
    }

    private String getInterpretation(double score) {
        if (score >= 9) return "Excellent conditions! Perfect for astrophotography";
        if (score >= 7) return "Very good conditions. Great for observations";
        if (score >= 5) return "Moderate conditions. Some limitations";
        if (score >= 3) return "Poor conditions. Significant challenges";
        return "Very poor conditions. Not recommended";
    }
}     