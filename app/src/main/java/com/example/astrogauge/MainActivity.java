package com.example.astrogauge;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.widget.LinearLayout;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import org.json.JSONException;
import org.json.JSONObject;
import android.graphics.Typeface;
import android.view.Gravity;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.Arrays;
import android.app.AlertDialog;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import org.json.JSONArray;

public class MainActivity extends AppCompatActivity {
    private LinearLayout calendarSummaryCard, calendarViewContainer, calendarGrid;
    private LinearLayout eventDetailsPanel;
    private TextView upcomingEventsCount, nextEventPreview, currentMonthYear;
    private TextView selectedEventTitle, selectedEventDate, selectedEventDescription;
    private TextView whyCelestialText, whyCelestialExplanation;
    private Button prevMonthButton, nextMonthButton;

    private int currentDisplayMonth = Calendar.getInstance().get(Calendar.MONTH);
    private int currentDisplayYear = Calendar.getInstance().get(Calendar.YEAR);
    private Map<Integer, CelestialEventsCalculator.CelestialEvent> eventsByDay = new HashMap<>();

    // Constants
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final String OPENWEATHER_API_KEY = "BuildConfig.OPENWEATHER_API_KEY";
    private static final double MUMBAI_LAT = 18.9667;
    private static final double MUMBAI_LON = 72.8333;
    private static final String NODEMCU_SERVER = "http://192.168.217.194/";

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
    private Button getMoonPhaseButton;
    private TextView moonPhaseIcon, moonPhaseName, moonIllumination;
    private TextView moonriseTime, moonsetTime, moonObservationQuality;
    private LinearLayout moonPhaseDisplay;
    private TextView whyMoonText, whyMoonExplanation;
    private LinearLayout cloudForecastSummaryCard, cloudForecastDetailsContainer;
    private LinearLayout optimalTimesContainer;
    private TextView forecastSummaryText, nextClearNightText, optimalTimesCount;
    private TextView forecastStatusText, whyCloudForecastText, whyCloudForecastExplanation;
    private Button refreshForecastButton, setReminderButton;
    private List<ForecastPeriod> currentOptimalPeriods = new ArrayList<>();


    // Location/API
    private FusedLocationProviderClient fusedLocationClient;
    private RequestQueue requestQueue;

    // Parameter tracking
    private boolean hasCloudData = false;
    private boolean hasLensData = false;
    private boolean hasTempHumidityData = false;
    private boolean hasLightData = false;
    private boolean hasMoonData = false;

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
        getMoonPhaseButton = findViewById(R.id.getMoonPhaseButton);
        moonPhaseIcon = findViewById(R.id.moonPhaseIcon);
        moonPhaseName = findViewById(R.id.moonPhaseName);
        moonIllumination = findViewById(R.id.moonIllumination);
        moonriseTime = findViewById(R.id.moonriseTime);
        moonsetTime = findViewById(R.id.moonsetTime);
        moonObservationQuality = findViewById(R.id.moonObservationQuality);
        moonPhaseDisplay = findViewById(R.id.moonPhaseDisplay);
        whyMoonText = findViewById(R.id.whyMoonText);
        whyMoonExplanation = findViewById(R.id.whyMoonExplanation);
        calendarSummaryCard = findViewById(R.id.calendarSummaryCard);
        calendarViewContainer = findViewById(R.id.calendarViewContainer);
        calendarGrid = findViewById(R.id.calendarGrid);
        eventDetailsPanel = findViewById(R.id.eventDetailsPanel);
        upcomingEventsCount = findViewById(R.id.upcomingEventsCount);
        nextEventPreview = findViewById(R.id.nextEventPreview);
        currentMonthYear = findViewById(R.id.currentMonthYear);
        selectedEventTitle = findViewById(R.id.selectedEventTitle);
        selectedEventDate = findViewById(R.id.selectedEventDate);
        selectedEventDescription = findViewById(R.id.selectedEventDescription);
        whyCelestialText = findViewById(R.id.whyCelestialText);
        whyCelestialExplanation = findViewById(R.id.whyCelestialExplanation);
        prevMonthButton = findViewById(R.id.prevMonthButton);
        nextMonthButton = findViewById(R.id.nextMonthButton);
        // Cloud Forecast elements
        cloudForecastSummaryCard = findViewById(R.id.cloudForecastSummaryCard);
        cloudForecastDetailsContainer = findViewById(R.id.cloudForecastDetailsContainer);
        optimalTimesContainer = findViewById(R.id.optimalTimesContainer);
        forecastSummaryText = findViewById(R.id.forecastSummaryText);
        nextClearNightText = findViewById(R.id.nextClearNightText);
        optimalTimesCount = findViewById(R.id.optimalTimesCount);
        forecastStatusText = findViewById(R.id.forecastStatusText);
        whyCloudForecastText = findViewById(R.id.whyCloudForecastText);
        whyCloudForecastExplanation = findViewById(R.id.whyCloudForecastExplanation);
        refreshForecastButton = findViewById(R.id.refreshForecastButton);
        setReminderButton = findViewById(R.id.setReminderButton);
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
        getMoonPhaseButton.setOnClickListener(v -> getMoonPhaseData());
        whyMoonText.setOnClickListener(v -> toggleVisibility(whyMoonExplanation));
        calendarSummaryCard.setOnClickListener(v -> toggleCalendarView());
        prevMonthButton.setOnClickListener(v -> changeMonth(-1));
        nextMonthButton.setOnClickListener(v -> changeMonth(1));
        whyCelestialText.setOnClickListener(v -> toggleVisibility(whyCelestialExplanation));
        cloudForecastSummaryCard.setOnClickListener(v -> toggleCloudForecastView());
        refreshForecastButton.setOnClickListener(v -> loadCloudForecast());
        setReminderButton.setOnClickListener(v -> setReminderForBestTime());
        whyCloudForecastText.setOnClickListener(v -> toggleVisibility(whyCloudForecastExplanation));
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

                        if (dataType.equals("temp_humidity")) {
                            float temperature = (float) json.getDouble("temperature");
                            float humidity = (float) json.getDouble("humidity");
                            tempHumidityResult.setText(String.format("Temperature: %.1f°C, Humidity: %.1f%%",
                                    temperature, humidity));
                            tempHumidityResult.setVisibility(View.VISIBLE);
                            hasTempHumidityData = true;
                        } else if (dataType.equals("light")) {
                            int lightValue = json.getInt("light");
                            lightResult.setText("Light Value: " + lightValue);
                            lightResult.setVisibility(View.VISIBLE);
                            hasLightData = true;
                        }

                        checkAllParametersAvailable();

                    } catch (JSONException e) {
                        Toast.makeText(this, "Error parsing NodeMCU data", Toast.LENGTH_SHORT).show();
                        Log.e("NODEMCU_ERROR", "JSON error: " + e.getMessage());
                    }
                },
                error -> {
                    Toast.makeText(this, "Error fetching from NodeMCU", Toast.LENGTH_SHORT).show();
                    Log.e("NODEMCU_ERROR", "Volley error: " + error.getMessage());
                });

        requestQueue.add(request);
    }

    private void checkAllParametersAvailable() {
        calculateScoreButton.setEnabled(hasCloudData && hasLensData &&
                hasTempHumidityData && hasLightData && hasMoonData);
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

    private void getMoonPhaseData() {
        // Get current moon phase
        MoonPhaseCalculator.MoonPhaseInfo phaseInfo = MoonPhaseCalculator.getCurrentMoonPhase();

        // Get moon rise/set times
        MoonPhaseCalculator.MoonTimes moonTimes = MoonPhaseCalculator.getMoonTimes(MUMBAI_LAT, MUMBAI_LON);

        // Update UI
        moonPhaseIcon.setText(phaseInfo.phaseIcon);
        moonPhaseName.setText(phaseInfo.phaseName);
        moonIllumination.setText(String.format("%.1f%% Illuminated", phaseInfo.illumination));
        moonriseTime.setText(moonTimes.moonrise);
        moonsetTime.setText(moonTimes.moonset);
        moonObservationQuality.setText(phaseInfo.observationQuality);

        // Show display
        moonPhaseDisplay.setVisibility(View.VISIBLE);

        Toast.makeText(this, "Moon phase data loaded!", Toast.LENGTH_SHORT).show();
    }
    private void toggleCalendarView() {
        if (calendarViewContainer.getVisibility() == View.GONE) {
            calendarViewContainer.setVisibility(View.VISIBLE);
            loadCalendarSummary();
            displayCalendar();
        } else {
            calendarViewContainer.setVisibility(View.GONE);
        }
    }

    private void loadCalendarSummary() {
        List<com.example.astrogauge.CelestialEventsCalculator.CelestialEvent> allEvents =
                com.example.astrogauge.CelestialEventsCalculator.getUpcomingEvents2025();

        // Count remaining events
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH) + 1;
        int currentDay = now.get(Calendar.DAY_OF_MONTH);

        int remainingEvents = 0;
        for (CelestialEventsCalculator.CelestialEvent event : allEvents) {
            if (event.month > currentMonth ||
                    (event.month == currentMonth && event.day >= currentDay)) {
                remainingEvents++;
            }
        }

        upcomingEventsCount.setText(remainingEvents + " Events");

        // Get next event
        CelestialEventsCalculator.CelestialEvent nextEvent =
                CelestialEventsCalculator.getNextUpcomingEvent();
        nextEventPreview.setText("Next: " + nextEvent.title + " " + nextEvent.icon);
    }

    private void displayCalendar() {
        // Clear previous calendar
        calendarGrid.removeAllViews();
        eventsByDay.clear();

        // Get events for this year
        List<CelestialEventsCalculator.CelestialEvent> allEvents =
                CelestialEventsCalculator.getUpcomingEvents2025();

        // Map events to days for current month
        for (CelestialEventsCalculator.CelestialEvent event : allEvents) {
            if (event.month == currentDisplayMonth + 1) {
                eventsByDay.put(event.day, event);
            }
        }

        // Update month/year display
        String[] monthNames = {"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"};
        currentMonthYear.setText(monthNames[currentDisplayMonth] + " " + currentDisplayYear);

        // Calculate calendar layout
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentDisplayYear, currentDisplayMonth, 1);

        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1; // 0 = Sunday

        Calendar today = Calendar.getInstance();
        int todayDay = today.get(Calendar.DAY_OF_MONTH);
        int todayMonth = today.get(Calendar.MONTH);
        int todayYear = today.get(Calendar.YEAR);

        // Create calendar grid
        LinearLayout weekRow = null;
        int dayCounter = 0;

        // Add empty cells for days before month starts
        for (int i = 0; i < firstDayOfWeek; i++) {
            if (weekRow == null) {
                weekRow = createWeekRow();
            }
            weekRow.addView(createEmptyDayCell());
            dayCounter++;
        }

        // Add days of month
        for (int day = 1; day <= daysInMonth; day++) {
            if (weekRow == null || dayCounter % 7 == 0) {
                if (weekRow != null) {
                    calendarGrid.addView(weekRow);
                }
                weekRow = createWeekRow();
            }

            boolean isToday = (day == todayDay && currentDisplayMonth == todayMonth &&
                    currentDisplayYear == todayYear);
            boolean hasEvent = eventsByDay.containsKey(day);

            weekRow.addView(createDayCell(day, isToday, hasEvent));
            dayCounter++;
        }

        // Fill remaining cells in last row
        while (dayCounter % 7 != 0) {
            weekRow.addView(createEmptyDayCell());
            dayCounter++;
        }

        if (weekRow != null) {
            calendarGrid.addView(weekRow);
        }
    }

    private LinearLayout createWeekRow() {
        LinearLayout row = new LinearLayout(this);
        row.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        row.setOrientation(LinearLayout.HORIZONTAL);
        return row;
    }

    private TextView createDayCell(int day, boolean isToday, boolean hasEvent) {
        TextView dayCell = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                (int) (50 * getResources().getDisplayMetrics().density),
                1.0f
        );
        params.setMargins(2, 2, 2, 2);
        dayCell.setLayoutParams(params);
        dayCell.setText(String.valueOf(day));
        dayCell.setTextSize(14);
        dayCell.setGravity(Gravity.CENTER);
        dayCell.setClickable(true);
        dayCell.setFocusable(true);

        // Style based on state
        if (hasEvent) {
            dayCell.setBackgroundColor(0xFF4A90E2); // Blue for events
            dayCell.setTextColor(0xFFFFFFFF); // White text
            dayCell.setTypeface(null, Typeface.BOLD);

            // Add click listener for event days
            final int eventDay = day;
            dayCell.setOnClickListener(v -> showEventDetails(eventDay));
        } else if (isToday) {
            dayCell.setBackgroundColor(0x334A90E2); // Transparent blue
            dayCell.setTextColor(0xFF64B5F6); // Light blue text
            dayCell.setTypeface(null, Typeface.BOLD);
        } else {
            dayCell.setBackgroundColor(0x1AFFFFFF); // Very transparent white
            dayCell.setTextColor(0xFFE0E7FF); // Light text
        }

        return dayCell;
    }

    private TextView createEmptyDayCell() {
        TextView emptyCell = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                0,
                (int) (50 * getResources().getDisplayMetrics().density),
                1.0f
        );
        params.setMargins(2, 2, 2, 2);
        emptyCell.setLayoutParams(params);
        emptyCell.setBackgroundColor(0x0AFFFFFF); // Almost transparent
        return emptyCell;
    }

    private void showEventDetails(int day) {
        CelestialEventsCalculator.CelestialEvent event = eventsByDay.get(day);

        if (event != null) {
            selectedEventTitle.setText(event.icon + " " + event.title);
            selectedEventDate.setText(event.date);
            selectedEventDescription.setText(event.description);
            eventDetailsPanel.setVisibility(View.VISIBLE);

            // Scroll to show event details
            final int scrollDelay = 100;
            eventDetailsPanel.postDelayed(() -> {
                // Smooth scroll not available in LinearLayout, but visibility change helps
            }, scrollDelay);
        }
    }

    private void changeMonth(int direction) {
        currentDisplayMonth += direction;

        if (currentDisplayMonth < 0) {
            currentDisplayMonth = 11;
            currentDisplayYear--;
        } else if (currentDisplayMonth > 11) {
            currentDisplayMonth = 0;
            currentDisplayYear++;
        }

        // Hide event details when changing months
        eventDetailsPanel.setVisibility(View.GONE);

        displayCalendar();
    }
    private void toggleCloudForecastView() {
        if (cloudForecastDetailsContainer.getVisibility() == View.GONE) {
            cloudForecastDetailsContainer.setVisibility(View.VISIBLE);
            if (optimalTimesContainer.getChildCount() == 0) {
                loadCloudForecast();
            }
        } else {
            cloudForecastDetailsContainer.setVisibility(View.GONE);
        }
    }

    private void loadCloudForecastSummary() {
        // Quick summary without opening full view
        new Thread(() -> {
            try {
                String forecastData = fetchForecastData();
                List<ForecastPeriod> optimalPeriods = findOptimalObservationTimes(forecastData);

                runOnUiThread(() -> {
                    if (!optimalPeriods.isEmpty()) {
                        ForecastPeriod best = optimalPeriods.get(0);
                        nextClearNightText.setText("Next clear: " + best.dateTime);
                        optimalTimesCount.setText(optimalPeriods.size() + " optimal times found");
                    } else {
                        nextClearNightText.setText("No clear nights in next 5 days");
                        optimalTimesCount.setText("Check back later");
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    nextClearNightText.setText("Tap to load forecast");
                    optimalTimesCount.setText("Internet connection required");
                });
            }
        }).start();
    }

    private void loadCloudForecast() {
        forecastStatusText.setText("🔄 Loading forecast...");
        optimalTimesContainer.removeAllViews();
        setReminderButton.setEnabled(false);

        Toast.makeText(this, "Fetching 5-day cloud forecast...", Toast.LENGTH_SHORT).show();

        new Thread(() -> {
            try {
                String forecastData = fetchForecastData();
                List<ForecastPeriod> optimalPeriods = findOptimalObservationTimes(forecastData);
                currentOptimalPeriods = optimalPeriods;

                runOnUiThread(() -> {
                    displayOptimalTimes(optimalPeriods);
                    forecastStatusText.setText("✅ Forecast loaded successfully");

                    if (!optimalPeriods.isEmpty()) {
                        setReminderButton.setEnabled(true);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    forecastStatusText.setText("⚠️ Using simulated data (API unavailable)");
                    displaySimulatedForecast();
                });
            }
        }).start();
    }

    private void displayOptimalTimes(List<ForecastPeriod> optimalPeriods) {
        optimalTimesContainer.removeAllViews();

        if (optimalPeriods.isEmpty()) {
            // Show no results message
            TextView noResults = new TextView(this);
            noResults.setText("No clear nights (≤25% clouds) found in next 5 days.\nCheck back later!");
            noResults.setTextSize(14);
            noResults.setTextColor(0xFFE0E7FF);
            noResults.setGravity(Gravity.CENTER);
            noResults.setPadding(32, 64, 32, 64);
            optimalTimesContainer.addView(noResults);
            return;
        }

        // Add each optimal time as a card
        for (int i = 0; i < optimalPeriods.size(); i++) {
            ForecastPeriod period = optimalPeriods.get(i);
            optimalTimesContainer.addView(createOptimalTimeCard(period, i == 0));
        }
    }

    private LinearLayout createOptimalTimeCard(ForecastPeriod period, boolean isBest) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(20, 20, 20, 20);

        // Background color based on quality
        if (isBest) {
            card.setBackgroundColor(0x40FFD700); // Gold tint for best
        } else if (period.cloudScore == 4) {
            card.setBackgroundColor(0x404A90E2); // Blue for excellent
        } else {
            card.setBackgroundColor(0x30FFFFFF); // Light for good
        }

        // Best badge
        if (isBest) {
            TextView bestBadge = new TextView(this);
            bestBadge.setText("⭐ BEST TIME");
            bestBadge.setTextSize(10);
            bestBadge.setTextColor(0xFFFFD700);
            bestBadge.setTypeface(null, Typeface.BOLD);
            bestBadge.setPadding(0, 0, 0, 8);
            card.addView(bestBadge);
        }

        // Date/Time
        TextView dateTime = new TextView(this);
        dateTime.setText("📅 " + period.dateTime);
        dateTime.setTextSize(16);
        dateTime.setTextColor(0xFFFFFFFF);
        dateTime.setTypeface(null, Typeface.BOLD);
        card.addView(dateTime);

        // Cloud coverage
        TextView cloudInfo = new TextView(this);
        String emoji = period.cloudScore == 4 ? "⭐" : "🌟";
        String condition = getCloudCondition(period.cloudCover);
        cloudInfo.setText(String.format("%s Cloud Cover: %d%% (%s)", emoji, period.cloudCover, condition));
        cloudInfo.setTextSize(14);
        cloudInfo.setTextColor(0xFF64B5F6);
        LinearLayout.LayoutParams cloudParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cloudParams.setMargins(0, 8, 0, 8);
        cloudInfo.setLayoutParams(cloudParams);
        card.addView(cloudInfo);

        // Recommendation
        TextView recommendation = new TextView(this);
        if (period.cloudScore == 4) {
            recommendation.setText("Perfect for deep-sky observation!");
        } else if (period.cloudScore == 3) {
            recommendation.setText("Great for general stargazing");
        }
        recommendation.setTextSize(13);
        recommendation.setTextColor(0xFFE0E7FF);
        card.addView(recommendation);

        return card;
    }

    private void displaySimulatedForecast() {
        optimalTimesContainer.removeAllViews();

        // Create sample forecast periods
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, MMM d, hh:mm a", Locale.getDefault());

        ForecastPeriod[] simulatedPeriods = {
                new ForecastPeriod(sdf.format(cal.getTime()) + " (Tonight)", 15, 4),
                new ForecastPeriod(getNextDayFormatted(cal, 1, sdf), 12, 4),
                new ForecastPeriod(getNextDayFormatted(cal, 2, sdf), 22, 3),
                new ForecastPeriod(getNextDayFormatted(cal, 3, sdf), 18, 4),
                new ForecastPeriod(getNextDayFormatted(cal, 4, sdf), 25, 3)
        };

        currentOptimalPeriods = Arrays.asList(simulatedPeriods);

        for (int i = 0; i < simulatedPeriods.length; i++) {
            optimalTimesContainer.addView(createOptimalTimeCard(simulatedPeriods[i], i == 0));
        }

        setReminderButton.setEnabled(true);
    }

    private String getNextDayFormatted(Calendar cal, int daysToAdd, SimpleDateFormat sdf) {
        Calendar future = (Calendar) cal.clone();
        future.add(Calendar.DAY_OF_MONTH, daysToAdd);
        future.set(Calendar.HOUR_OF_DAY, 22);
        future.set(Calendar.MINUTE, 0);
        return sdf.format(future.getTime());
    }

    private void setReminderForBestTime() {
        if (currentOptimalPeriods.isEmpty()) {
            Toast.makeText(this, "No optimal times available", Toast.LENGTH_SHORT).show();
            return;
        }

        ForecastPeriod bestPeriod = currentOptimalPeriods.get(0);

        new AlertDialog.Builder(this)
                .setTitle("🔔 Set Reminder")
                .setMessage("Set a reminder for:\n\n" + bestPeriod.dateTime +
                        "\nCloud Cover: " + bestPeriod.cloudCover + "% (" +
                        getCloudCondition(bestPeriod.cloudCover) + ")\n\n" +
                        "Reminder will be set 1 hour before optimal time.")
                .setPositiveButton("Set Reminder", (dialog, which) -> {
                    // Here you can implement actual calendar/notification reminder
                    Toast.makeText(this, "Reminder set for " + bestPeriod.dateTime, Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }


    // Make sure you have this helper method
    private String getCloudCondition(int cloudCover) {
        if (cloudCover <= 10) return "Excellent";
        if (cloudCover <= 25) return "Very Good";
        if (cloudCover <= 50) return "Good";
        return "Fair";
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

}