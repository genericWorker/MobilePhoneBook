package edu.dccc.mobilephonebook;

import edu.dccc.utils.CSVTemplate;

import java.time.LocalDateTime;

// Static Inner Model Class
public class Contact implements Comparable<Contact>, CSVTemplate {
    private String name;
    private String phone;
    private LocalDateTime lastModified;


   public Contact() {

   }

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
        this.lastModified = LocalDateTime.now(); // Initialize here!
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }
    public LocalDateTime getLastModified() {
        return lastModified;
    }

    @Override
    public String toString() {
         return name + "  -  " + phone;
//       return String.format("%-20s |  %15s", name, phone);
    }

    @Override
    public int compareTo(Contact other) {
        // 1. Sort by Name (Case-Insensitive)
        int nameCompare = this.name.compareToIgnoreCase(other.name);
        if (nameCompare != 0) {
            return nameCompare;
        }

        // 2. Tie-breaker 1: Phone Number
        int phoneCompare = this.phone.compareTo(other.phone);
        if (phoneCompare != 0) {
            return phoneCompare;
        }

        // 3. Timestamp (The "Birth Certificate")
        // If name and phone are identical, the one created earlier comes first.
        return this.lastModified.compareTo(other.lastModified);
    }

    @Override
    public String toCSV() {
        return name + "," + phone + "," + lastModified.toString();
    }

    public void fromCSV(String[] data) {
        if (data.length >= 2) {
            this.name = data[0].trim();
            this.phone = data[1].trim();
        }

        // Check if the timestamp column exists
        if (data.length >= 3) {
            try {
                this.lastModified = LocalDateTime.parse(data[2]);
            } catch (Exception e) {
                // Fallback if the date format is weird
                this.lastModified = LocalDateTime.now();
            }
        } else {
            // LEGACY DATA FIX: If no timestamp exists, give it one now
            this.lastModified = LocalDateTime.now();
        }
    }
}