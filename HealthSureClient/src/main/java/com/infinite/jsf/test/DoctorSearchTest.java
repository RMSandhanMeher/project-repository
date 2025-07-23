package com.infinite.jsf.test;

import java.util.List;
import java.util.Scanner;

import com.infinite.jsf.provider.model.Doctors;
import com.infinite.jsf.recipient.controller.RecipientSearchDoctorController;

public class DoctorSearchTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        RecipientSearchDoctorController controller = new RecipientSearchDoctorController();

        System.out.println("=== Doctor Search Tester ===");

        System.out.print("Enter Search By (name/specialization/address): ");
        String searchBy = scanner.nextLine();

        System.out.print("Enter Search Value: ");
        String searchValue = scanner.nextLine();

        controller.setSearchBy(searchBy);
        controller.setSearchValue(searchValue);

        controller.executeSearch();

        List<Doctors> resultList = controller.getSearchResults();     
        
//        doctor_name – Front and center. This is what users connect with first.
//        specialization – Helps users quickly identify who suits their needs (e.g., cardiologist, dermatologist).
//        qualification – Adds credibility and builds trust.
//        doctor_status – Only show if the doctor is ACTIVE; helps filter out unavailable ones.
//        address – Crucial for knowing where the appointment will take place or if it’s nearby.
//        type – Indicates whether the doctor is regularly available (STANDARD) or only occasionally (ADHOC).
//        email – Useful if users want to reach out for follow-ups or confirmations, but consider privacy.
                

        if (resultList.isEmpty()) {
            System.out.println("No doctors found for your criteria.");
        } else {
            System.out.println("\n--- Search Results ---");
            for (Doctors doc : resultList) {
                System.out.println("Name        : " + doc.getDoctorName());
                System.out.println("Specialization : " + doc.getSpecialization());
                System.out.println("Qualification : " + doc.getQualification());
                System.out.println("Status : " + doc.getStatus());
                System.out.println("Address     : " + doc.getAddress());
                System.out.println("Address     : " + doc.getType());
                System.out.println("Gender      : " + doc.getEmail());
                System.out.println("------------------------------");
            }
        }

        scanner.close();
    }
}
