package com.example.astrogauge;

import java.util.Calendar;
import java.util.TimeZone;

public class MoonPhaseCalculator {

    // Known new moon date as reference (January 6, 2000)
    private static final long KNOWN_NEW_MOON = 947182440000L; // in milliseconds
    private static final double LUNAR_CYCLE = 29.53058867; // days

    public static class MoonPhaseInfo {
        public String phaseName;
        public String phaseIcon;
        public double illumination;
        public String observationQuality;
        public int qualityScore; // 1-4 for scoring system

        public MoonPhaseInfo(String name, String icon, double illum, String quality, int score) {
            this.phaseName = name;
            this.phaseIcon = icon;
            this.illumination = illum;
            this.observationQuality = quality;
            this.qualityScore = score;
        }
    }

    public static MoonPhaseInfo getCurrentMoonPhase() {
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        long currentTime = now.getTimeInMillis();

        // Calculate days since known new moon
        double daysSinceNewMoon = (currentTime - KNOWN_NEW_MOON) / (1000.0 * 60 * 60 * 24);

        // Calculate current position in lunar cycle
        double phase = (daysSinceNewMoon % LUNAR_CYCLE) / LUNAR_CYCLE;

        // Calculate illumination percentage
        double illumination = (1 - Math.cos(phase * 2 * Math.PI)) / 2 * 100;

        // Determine phase name, icon, and observation quality
        String phaseName;
        String phaseIcon;
        String observationQuality;
        int qualityScore;

        if (phase < 0.033) {
            phaseName = "New Moon";
            phaseIcon = "🌑";
            observationQuality = "🌟 Perfect for deep-sky observation!";
            qualityScore = 4; // Best for deep sky
        } else if (phase < 0.216) {
            phaseName = "Waxing Crescent";
            phaseIcon = "🌒";
            observationQuality = "✨ Excellent for deep-sky objects";
            qualityScore = 4;
        } else if (phase < 0.283) {
            phaseName = "First Quarter";
            phaseIcon = "🌓";
            observationQuality = "⭐ Good for lunar and planetary viewing";
            qualityScore = 3;
        } else if (phase < 0.466) {
            phaseName = "Waxing Gibbous";
            phaseIcon = "🌔";
            observationQuality = "🌙 Great for moon observation";
            qualityScore = 2;
        } else if (phase < 0.533) {
            phaseName = "Full Moon";
            phaseIcon = "🌕";
            observationQuality = "🌕 Best for lunar photography!";
            qualityScore = 1; // Worst for deep sky, best for moon itself
        } else if (phase < 0.716) {
            phaseName = "Waning Gibbous";
            phaseIcon = "🌖";
            observationQuality = "🌙 Good for moon observation";
            qualityScore = 2;
        } else if (phase < 0.783) {
            phaseName = "Last Quarter";
            phaseIcon = "🌗";
            observationQuality = "⭐ Good for lunar and planetary viewing";
            qualityScore = 3;
        } else if (phase < 0.966) {
            phaseName = "Waning Crescent";
            phaseIcon = "🌘";
            observationQuality = "✨ Excellent for deep-sky objects";
            qualityScore = 4;
        } else {
            phaseName = "New Moon";
            phaseIcon = "🌑";
            observationQuality = "🌟 Perfect for deep-sky observation!";
            qualityScore = 4;
        }

        return new MoonPhaseInfo(phaseName, phaseIcon, illumination, observationQuality, qualityScore);
    }

    // Calculate approximate moonrise and moonset times
    public static class MoonTimes {
        public String moonrise;
        public String moonset;

        public MoonTimes(String rise, String set) {
            this.moonrise = rise;
            this.moonset = set;
        }
    }

    public static MoonTimes getMoonTimes(double latitude, double longitude) {
        // This is a simplified calculation
        // For production, you'd want to use a proper astronomy library

        Calendar now = Calendar.getInstance();
        MoonPhaseInfo phase = getCurrentMoonPhase();

        // Simplified calculation based on moon phase
        // Full moon rises around sunset (18:00), new moon rises around sunrise (6:00)
        double phaseOffset = (phase.illumination / 100.0) * 12; // 0-12 hour offset

        int riseHour = (int)(6 + phaseOffset);
        int setHour = (int)((18 + phaseOffset) % 24);

        // Add some variation based on current date
        int dayOfYear = now.get(Calendar.DAY_OF_YEAR);
        int riseMinute = (dayOfYear * 7) % 60;
        int setMinute = (dayOfYear * 11) % 60;

        String moonrise = String.format("%02d:%02d", riseHour, riseMinute);
        String moonset = String.format("%02d:%02d", setHour, setMinute);

        return new MoonTimes(moonrise, moonset);
    }
}