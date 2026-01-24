package edu.dccc.mobilephonebook;

import java.io.*;

public class CSVReaderWriterOld {

    private DoublyLinkedList<Contact> storage;
    private String filePath;
    public CSVReaderWriterOld(String filePath, DoublyLinkedList<Contact> storage) {
        this.filePath = filePath;
        this.storage = storage;
    }


    public  void loadFromCSV(boolean hasHeader) {
        File file = new File(filePath);
        if (!file.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            if (hasHeader) br.readLine();
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    storage.add(new Contact(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("Name,Phone");
            for (Contact c : storage) {
                pw.println(c.getName() + "," + c.getPhone());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

}
