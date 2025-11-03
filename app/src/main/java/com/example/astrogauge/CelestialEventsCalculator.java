package com.example.astrogauge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CelestialEventsCalculator {

    public static class CelestialEvent {
        public String title;
        public String date;
        public String description;
        public String icon;
        public String type; // "meteor", "eclipse", "planet", "conjunction"
        public int month;
        public int day;

        public CelestialEvent(String title, String date, String description, String icon, String type, int month, int day) {
            this.title = title;
            this.date = date;
            this.description = description;
            this.icon = icon;
            this.type = type;
            this.month = month;
            this.day = day;
        }
    }

    public static List<CelestialEvent> getUpcomingEvents2025() {
        List<CelestialEvent> events = new ArrayList<>();

        // 2025 Celestial Events Calendar

        // January 2025
        events.add(new CelestialEvent(
                "Quadrantids Meteor Shower",
                "January 3-4, 2025",
                "Peak viewing: Up to 120 meteors per hour. Best time: After midnight.",
                "☄️",
                "meteor",
                1, 3
        ));

        // February 2025
        events.add(new CelestialEvent(
                "Venus at Greatest Brightness",
                "February 14, 2025",
                "Venus will be at its brightest, visible in the evening sky.",
                "⭐",
                "planet",
                2, 14
        ));

        // March 2025
        events.add(new CelestialEvent(
                "Total Lunar Eclipse",
                "March 14, 2025",
                "Visible from North America, South America, and parts of Europe and Africa.",
                "🌑",
                "eclipse",
                3, 14
        ));

        events.add(new CelestialEvent(
                "March Equinox",
                "March 20, 2025",
                "Spring equinox in Northern Hemisphere. Equal day and night.",
                "🌍",
                "special",
                3, 20
        ));

        // April 2025
        events.add(new CelestialEvent(
                "Lyrids Meteor Shower",
                "April 22-23, 2025",
                "Peak: 10-20 meteors per hour. Best after midnight.",
                "☄️",
                "meteor",
                4, 22
        ));

        // May 2025
        events.add(new CelestialEvent(
                "Eta Aquarids Meteor Shower",
                "May 6-7, 2025",
                "Peak: 30-40 meteors per hour. Best in pre-dawn hours.",
                "☄️",
                "meteor",
                5, 6
        ));

        // June 2025
        events.add(new CelestialEvent(
                "June Solstice",
                "June 21, 2025",
                "Summer solstice in Northern Hemisphere. Longest day of the year.",
                "☀️",
                "special",
                6, 21
        ));

        // July 2025
        events.add(new CelestialEvent(
                "Delta Aquarids Meteor Shower",
                "July 28-29, 2025",
                "Peak: 15-20 meteors per hour. Best viewing in Southern Hemisphere.",
                "☄️",
                "meteor",
                7, 28
        ));

        // August 2025
        events.add(new CelestialEvent(
                "Perseids Meteor Shower",
                "August 12-13, 2025",
                "One of the best! Peak: 60-100 meteors per hour. Bright and fast meteors.",
                "☄️",
                "meteor",
                8, 12
        ));

        events.add(new CelestialEvent(
                "Saturn at Opposition",
                "August 21, 2025",
                "Saturn will be at its closest and brightest. Perfect for telescope viewing.",
                "🪐",
                "planet",
                8, 21
        ));

        // September 2025
        events.add(new CelestialEvent(
                "Partial Lunar Eclipse",
                "September 7, 2025",
                "Visible from Europe, Africa, Asia, and Australia.",
                "🌑",
                "eclipse",
                9, 7
        ));

        events.add(new CelestialEvent(
                "September Equinox",
                "September 22, 2025",
                "Autumn equinox in Northern Hemisphere.",
                "🍂",
                "special",
                9, 22
        ));

        // October 2025
        events.add(new CelestialEvent(
                "Orionids Meteor Shower",
                "October 21-22, 2025",
                "Peak: 10-20 meteors per hour. Debris from Halley's Comet!",
                "☄️",
                "meteor",
                10, 21
        ));

        // November 2025
        events.add(new CelestialEvent(
                "Taurids Meteor Shower",
                "November 5-6, 2025",
                "Peak: 5-10 meteors per hour. Known for bright fireballs!",
                "☄️",
                "meteor",
                11, 5
        ));

        events.add(new CelestialEvent(
                "Leonids Meteor Shower",
                "November 17-18, 2025",
                "Peak: 10-15 meteors per hour. Can produce meteor storms every 33 years.",
                "☄️",
                "meteor",
                11, 17
        ));

        // December 2025
        events.add(new CelestialEvent(
                "Geminids Meteor Shower",
                "December 13-14, 2025",
                "Best of the year! Peak: 120+ meteors per hour. Reliable and spectacular.",
                "☄️",
                "meteor",
                12, 13
        ));

        events.add(new CelestialEvent(
                "December Solstice",
                "December 21, 2025",
                "Winter solstice in Northern Hemisphere. Shortest day of the year.",
                "❄️",
                "special",
                12, 21
        ));

        events.add(new CelestialEvent(
                "Ursids Meteor Shower",
                "December 22-23, 2025",
                "Peak: 5-10 meteors per hour. Best in Northern Hemisphere.",
                "☄️",
                "meteor",
                12, 22
        ));

        return events;
    }

    public static CelestialEvent getNextUpcomingEvent() {
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH) + 1; // Calendar.MONTH is 0-based
        int currentDay = now.get(Calendar.DAY_OF_MONTH);

        List<CelestialEvent> allEvents = getUpcomingEvents2025();

        // Find the next event
        for (CelestialEvent event : allEvents) {
            if (event.month > currentMonth ||
                    (event.month == currentMonth && event.day >= currentDay)) {
                return event;
            }
        }

        // If no events left this year, return first event (for next year planning)
        return allEvents.get(0);
    }

    public static List<CelestialEvent> getUpcomingEventsList() {
        Calendar now = Calendar.getInstance();
        int currentMonth = now.get(Calendar.MONTH) + 1;
        int currentDay = now.get(Calendar.DAY_OF_MONTH);

        List<CelestialEvent> allEvents = getUpcomingEvents2025();
        List<CelestialEvent> upcomingEvents = new ArrayList<>();

        // Get next 6 upcoming events
        int count = 0;
        for (CelestialEvent event : allEvents) {
            if (count >= 6) break;

            if (event.month > currentMonth ||
                    (event.month == currentMonth && event.day >= currentDay)) {
                upcomingEvents.add(event);
                count++;
            }
        }

        // If less than 6 events, add from beginning of year for next year
        if (count < 6) {
            for (CelestialEvent event : allEvents) {
                if (count >= 6) break;
                upcomingEvents.add(event);
                count++;
            }
        }

        return upcomingEvents;
    }
}
