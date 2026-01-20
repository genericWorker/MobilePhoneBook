package edu.dccc.mobilephonebook;

import java.io.*;

public class CSVReaderWriter {

    private DoublyLinkedList<ContactsApp.Contact> storage;
    private String filePath;
    public CSVReaderWriter(String filePath,   DoublyLinkedList<ContactsApp.Contact> storage) {
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
                    storage.add(new ContactsApp.Contact(parts[0].trim(), parts[1].trim()));
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public void saveToCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(filePath))) {
            pw.println("Name,Phone");
            for (ContactsApp.Contact c : storage) {
                pw.println(c.getName() + "," + c.getPhone());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

}
