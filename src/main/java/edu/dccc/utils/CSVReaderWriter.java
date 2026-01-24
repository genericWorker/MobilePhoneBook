package edu.dccc.utils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class CSVReaderWriter<T extends CSVTemplate & Comparable<T>> { // Note the Comparable constraint

    private Collection<T> storage;
    private String filePath;
    private Class<T> type;

    public CSVReaderWriter(String filePath, Collection<T> storage, Class<T> type) {
        this.filePath = filePath;
        this.storage = storage;
        this.type = type;
    }

    public void loadFromCSV(boolean hasHeader) {
        File file = new File(filePath);
        if (!file.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            if (hasHeader) br.readLine();

            String line;
            while ((line = br.readLine()) != null) {
                // Ignore empty lines
                if (line.trim().isEmpty()) continue;
                //  Look for empty constructor and returns the class we are reading into
                T item = type.getDeclaredConstructor().newInstance();
                item.fromCSV(line.split(","));
                storage.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the data in a sorted fashion, regardless of how the
     * Collection (like PriorityQueue) stores it internally.
     */
    public void saveToCSVSorted(String header) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            if (header != null)
                pw.println(header);

            // 1. Copy the "scrambled" storage to a temporary List
            List<T> sortedList = new ArrayList<>(storage);

            // 2. Sort the list using the item's compareTo logic (Priority -> DueDate)
            Collections.sort(sortedList);

            // 3. Write the now-ordered list to the file
            for (T item : sortedList) {
                pw.println(item.toCSV());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Saves the current contents of the collection to the CSV file
     * exactly as they appear in the list (no sorting).
     * @param header The column titles (e.g., "Name,Phone"), or null if no header is desired.
     */
    public void saveToCSV(String header) {
        // We use try-with-resources to ensure the file closes even if an error occurs
        try (PrintWriter writer = new PrintWriter(new FileWriter(this.filePath))) {

            // 1. Write the header if one was provided
            if (header != null && !header.isEmpty()) {
                writer.println(header);
            }

            // 2. Loop through the bridgeList (this.storage)
            for (T item : this.storage) {
                // We assume T has a toCSV() method or we use toString()
                // In your project, your Contact class provides the CSV string
                writer.println(item.toString());
            }

            System.out.println("Successfully saved " + storage.size() + " records to " + filePath);

        } catch (IOException e) {
            System.err.println("Critical Error: Could not save to file " + filePath);
            e.printStackTrace();
        }
    }

}