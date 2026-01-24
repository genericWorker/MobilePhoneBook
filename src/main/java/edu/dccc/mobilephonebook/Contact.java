package edu.dccc.mobilephonebook;

import edu.dccc.utils.CSVTemplate;

// Static Inner Model Class
public class Contact implements Comparable<Contact>, CSVTemplate {
    private String name;
    private String phone;

   public Contact() {

   }

    public Contact(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getName() { return name; }
    public String getPhone() { return phone; }

    @Override
    public String toString() {
        return name + " - " + phone;
    }

    @Override
    public int compareTo(Contact other) {
        // This provides the alphabetical sorting for the TreeSet
        return this.name.compareToIgnoreCase(other.getName());
    }

    @Override
    public String toCSV() {
        return name + "," + phone;
    }

    @Override
    public void fromCSV(String[] data) {
        if (data.length >= 2) {
            this.name = data[0];
            this.phone = data[1];
        }
    }
}