-- Create the database
drop database healthsure;
CREATE DATABASE IF NOT EXISTS healthsure;
USE healthsure;

CREATE TABLE Providers (
    provider_id VARCHAR(20) PRIMARY KEY,
    provider_name VARCHAR(100),
    hospital_name VARCHAR(100),
    email VARCHAR(100),
    address VARCHAR(255),
    city VARCHAR(100),
    state VARCHAR(100),
    zip_code VARCHAR(20),
    status ENUM('APPROVED', 'PENDING', 'REJECTED')
);


-- Doctors Table
CREATE TABLE Doctors (
    doctor_id VARCHAR(20) PRIMARY KEY,
    provider_id VARCHAR(20),
    doctor_name VARCHAR(100) NOT NULL,
    qualification VARCHAR(255),
    specialization VARCHAR(100),
    license_no VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    address VARCHAR(225) NOT NULL,
    gender ENUM('MALE','FEMALE') NOT NULL,
    type ENUM('STANDARD', 'ADHOC') DEFAULT 'STANDARD',
    doctor_status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'INACTIVE',
    FOREIGN KEY (provider_id) REFERENCES Providers(provider_id)
);


CREATE TABLE Doctor_availability (
  availability_id VARCHAR(36) PRIMARY KEY,
  doctor_id VARCHAR(20) NOT NULL,
  available_date DATE NOT NULL,
  start_time TIME NOT NULL,
  end_time TIME NOT NULL,
  slot_type ENUM('STANDARD', 'ADHOC') DEFAULT 'STANDARD',
  max_capacity INT NOT NULL,
  patient_window INT GENERATED ALWAYS AS (
    TIMESTAMPDIFF(MINUTE, start_time, end_time) / max_capacity
  ) STORED,
  is_recurring BOOLEAN DEFAULT FALSE,
  notes VARCHAR(255),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (doctor_id) REFERENCES Doctors(doctor_id)
);
-- Create Recipient table
CREATE TABLE recipient (
    h_id VARCHAR(20) PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    mobile VARCHAR(10) UNIQUE,
    user_name VARCHAR(100) UNIQUE NOT NULL,
    gender ENUM('MALE', 'FEMALE') NOT NULL,
    dob DATE NOT NULL,
    address VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    password VARCHAR(255),
    email VARCHAR(150) UNIQUE NOT NULL,
    status ENUM('ACTIVE', 'INACTIVE') DEFAULT 'ACTIVE'
);


CREATE TABLE Appointment (
  appointment_id VARCHAR(36) PRIMARY KEY,
  doctor_id VARCHAR(20) NOT NULL,
  h_id VARCHAR(20) NOT NULL,
  availability_id VARCHAR(36) NOT NULL,
  provider_id VARCHAR(20) NOT NULL,
  requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  booked_at TIMESTAMP NULL,
  cancelled_at TIMESTAMP NULL,
  completed_at TIMESTAMP NULL,
  status ENUM('PENDING', 'BOOKED', 'CANCELLED', 'COMPLETED') DEFAULT 'PENDING',
  slot_no INT NOT NULL,
  start TIMESTAMP,
  end TIMESTAMP,
  notes TEXT,
  FOREIGN KEY (doctor_id) REFERENCES Doctors(doctor_id),
  FOREIGN KEY (h_id) REFERENCES Recipient(h_id),
  FOREIGN KEY (availability_id) REFERENCES Doctor_availability(availability_id),
  FOREIGN KEY (provider_id) REFERENCES Providers(provider_id)
);
-- Insert a single provider
INSERT INTO Providers (provider_id, provider_name, hospital_name, email, address, city, state, zip_code, status) VALUES
('PROV001', 'Global Healthcare', 'City General Hospital', 'contact@globalhealth.com', '123 Health Ave', 'Bengaluru', 'Karnataka', '560001', 'APPROVED');

-- Insert a single doctor
INSERT INTO Doctors (doctor_id, provider_id, doctor_name, qualification, specialization, license_no, email, address, gender, type, doctor_status) VALUES
('DOC001', 'PROV001', 'Dr. Priya Sharma', 'MD, MBBS', 'General Physician', 'LIC789012', 'dr.priyasharma@example.com', '456 Doctor St, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE');

-- Insert a single recipient
INSERT INTO recipient (h_id, first_name, last_name, mobile, user_name, gender, dob, address, password, email, status) VALUES
('REC001', 'Rahul', 'Kumar', '9876543210', 'rahulkumar', 'MALE', '1990-05-15', '789 Patient Rd, Bengaluru', 'hashed_password_123', 'rahul.kumar@example.com', 'ACTIVE');

-- Insert multiple doctor availabilities (past and future)

-- Past availabilities (relative to July 18, 2025)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL001', 'DOC001', '2025-06-10', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Morning clinic'),
('AVAIL002', 'DOC001', '2025-07-01', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Afternoon session');

-- Present/Near future availabilities (relative to July 18, 2025)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL003', 'DOC001', '2025-07-19', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Today''s availability'),
('AVAIL004', 'DOC001', '2025-07-20', '09:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Tomorrow morning slots');

-- Future availabilities
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL005', 'DOC001', '2025-08-05', '09:00:00', '12:00:00', 'STANDARD', 3, TRUE, 'Recurring Tuesday slots'),
('AVAIL006', 'DOC001', '2025-08-10', '13:00:00', '16:00:00', 'ADHOC', 2, FALSE, 'Special clinic for emergencies');

-- Insert various appointments (past, present, future, and all statuses)

-- Past appointments (relative to July 18, 2025)
INSERT INTO Appointment (appointment_id, doctor_id, h_id, availability_id, provider_id, requested_at, booked_at, completed_at, status, slot_no, start, end, notes) VALUES
('APPT001', 'DOC001', 'REC001', 'AVAIL001', 'PROV001', '2025-06-08 09:00:00', '2025-06-08 09:30:00', '2025-06-10 09:30:00', 'COMPLETED', 1, '2025-06-10 09:00:00', '2025-06-10 10:00:00', 'Follow-up checkup'),
('APPT002', 'DOC001', 'REC001', 'AVAIL002', 'PROV001', '2025-06-25 10:00:00', '2025-06-25 10:30:00', NULL, 'CANCELLED', 1, '2025-07-01 14:00:00', '2025-07-01 15:30:00', 'Patient cancelled due to travel');

-- Present/Near future appointments (relative to July 18, 2025)
INSERT INTO Appointment (appointment_id, doctor_id, h_id, availability_id, provider_id, requested_at, booked_at, status, slot_no, start, end, notes) VALUES
('APPT003', 'DOC001', 'REC001', 'AVAIL003', 'PROV001', '2025-07-17 11:00:00', '2025-07-17 11:30:00', 'BOOKED', 1, '2025-07-19 10:00:00', '2025-07-19 11:00:00', 'Routine consultation for today'),
('APPT004', 'DOC001', 'REC001', 'AVAIL004', 'PROV001', '2025-07-18 14:00:00', NULL, 'PENDING', 1, '2025-07-20 09:00:00', '2025-07-20 10:00:00', 'Waiting for doctor confirmation for tomorrow');

-- Future appointments
INSERT INTO Appointment (appointment_id, doctor_id, h_id, availability_id, provider_id, requested_at, booked_at, status, slot_no, start, end, notes) VALUES
('APPT005', 'DOC001', 'REC001', 'AVAIL005', 'PROV001', '2025-07-18 10:00:00', '2025-07-18 10:30:00', 'BOOKED', 1, '2025-08-05 09:00:00', '2025-08-05 10:00:00', 'Annual checkup in August'),
('APPT006', 'DOC001', 'REC001', 'AVAIL006', 'PROV001', '2025-07-18 11:00:00', NULL, 'PENDING', 1, '2025-08-10 13:00:00', '2025-08-10 14:30:00', 'Emergency appointment request for mid-August');

-- Inserting 20 doctors into the Doctors table
INSERT INTO Doctors (doctor_id, provider_id, doctor_name, qualification, specialization, license_no, email, address, gender, type, doctor_status) VALUES
('DOC002', 'PROV001', 'Dr. Amit Patel', 'MD, MBBS', 'Cardiology', 'LIC123456', 'amit.patel@example.com', '101 Heart St, Bengaluru', 'MALE', 'STANDARD', 'ACTIVE'),
('DOC003', 'PROV001', 'Dr. Neha Gupta', 'MS, MBBS', 'Orthopedics', 'LIC234567', 'neha.gupta@example.com', '202 Bone Ave, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE'),
('DOC004', 'PROV001', 'Dr. Rajesh Kumar', 'DM, MD', 'Neurology', 'LIC345678', 'rajesh.kumar@example.com', '303 Brain Rd, Bengaluru', 'MALE', 'STANDARD', 'ACTIVE'),
('DOC005', 'PROV001', 'Dr. Ananya Singh', 'MD, MBBS', 'Pediatrics', 'LIC456789', 'ananya.singh@example.com', '404 Child Lane, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE'),
('DOC006', 'PROV001', 'Dr. Vikram Sharma', 'MS, MBBS', 'General Surgery', 'LIC567890', 'vikram.sharma@example.com', '505 Scalpel St, Bengaluru', 'MALE', 'STANDARD', 'ACTIVE'),
('DOC007', 'PROV001', 'Dr. Priyanka Reddy', 'MD, MBBS', 'Dermatology', 'LIC678901', 'priyanka.reddy@example.com', '606 Skin Ave, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE'),
('DOC009', 'PROV001', 'Dr. Sunita Malhotra', 'MS, MBBS', 'Ophthalmology', 'LIC890123', 'sunita.malhotra@example.com', '808 Vision Lane, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE'),
('DOC010', 'PROV001', 'Dr. Ravi Verma', 'MD, MBBS', 'Psychiatry', 'LIC901234', 'ravi.verma@example.com', '909 Mind St, Bengaluru', 'MALE', 'STANDARD', 'ACTIVE'),
('DOC011', 'PROV001', 'Dr. Meera Iyer', 'DM, MD', 'Endocrinology', 'LIC012345', 'meera.iyer@example.com', '1010 Hormone Ave, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE'),
('DOC012', 'PROV001', 'Dr. Sanjay Nair', 'MS, MBBS', 'ENT', 'LIC123457', 'sanjay.nair@example.com', '1111 Ear Rd, Bengaluru', 'MALE', 'STANDARD', 'ACTIVE'),
('DOC013', 'PROV001', 'Dr. Kavita Desai', 'MD, MBBS', 'Rheumatology', 'LIC234568', 'kavita.desai@example.com', '1212 Joint Lane, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE'),
('DOC014', 'PROV001', 'Dr. Alok Mehta', 'DM, MD', 'Nephrology', 'LIC345679', 'alok.mehta@example.com', '1313 Kidney St, Bengaluru', 'MALE', 'STANDARD', 'ACTIVE'),
('DOC015', 'PROV001', 'Dr. Shweta Banerjee', 'MD, MBBS', 'Oncology', 'LIC456780', 'shweta.banerjee@example.com', '1414 Cancer Ave, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE'),
('DOC016', 'PROV001', 'Dr. Nikhil Choudhary', 'MS, MBBS', 'Urology', 'LIC567891', 'nikhil.choudhary@example.com', '1515 Urine Rd, Bengaluru', 'MALE', 'STANDARD', 'ACTIVE'),
('DOC017', 'PROV001', 'Dr. Pooja Kapoor', 'MD, MBBS', 'Gastroenterology', 'LIC678902', 'pooja.kapoor@example.com', '1616 Gut Lane, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE'),
('DOC018', 'PROV001', 'Dr. Manish Agarwal', 'MS, MBBS', 'Plastic Surgery', 'LIC789013', 'manish.agarwal@example.com', '1717 Cosmetic St, Bengaluru', 'MALE', 'STANDARD', 'ACTIVE'),
('DOC019', 'PROV001', 'Dr. Anjali Bhatia', 'MD, MBBS', 'Pulmonology', 'LIC890124', 'anjali.bhatia@example.com', '1818 Lung Ave, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE'),
('DOC020', 'PROV001', 'Dr. Rohit Saxena', 'DM, MD', 'Cardiac Surgery', 'LIC901235', 'rohit.saxena@example.com', '1919 Heart Rd, Bengaluru', 'MALE', 'STANDARD', 'ACTIVE'),
('DOC021', 'PROV001', 'Dr. Divya Menon', 'MD, MBBS', 'Infectious Diseases', 'LIC012346', 'divya.menon@example.com', '2020 Germ Lane, Bengaluru', 'FEMALE', 'STANDARD', 'ACTIVE');




-- Adding availabilities for multiple doctors (continuing from previous data)

-- Doctor 1 (DOC001) - Already has some availabilities, adding more
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL007', 'DOC001', '2025-07-22', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Morning clinic'),
('AVAIL008', 'DOC001', '2025-07-22', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Afternoon session'),
('AVAIL009', 'DOC001', '2025-07-25', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Friday availability'),
('AVAIL010', 'DOC001', '2025-07-25', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Late afternoon slots');

-- Doctor 2 (DOC002 - Cardiologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL011', 'DOC002', '2025-07-23', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Cardiac consultation morning'),
('AVAIL012', 'DOC002', '2025-07-23', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Cardiac follow-ups'),
('AVAIL013', 'DOC002', '2025-07-26', '09:00:00', '12:00:00', 'STANDARD', 3, TRUE, 'Recurring Saturday cardiac clinic');

-- Doctor 3 (DOC003 - Orthopedist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL014', 'DOC003', '2025-07-24', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Bone and joint clinic'),
('AVAIL015', 'DOC003', '2025-07-24', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'Emergency orthopedic cases'),
('AVAIL016', 'DOC003', '2025-07-27', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Sunday special clinic');

-- Doctor 4 (DOC004 - Neurologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL017', 'DOC004', '2025-07-28', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Neurology consultation'),
('AVAIL018', 'DOC004', '2025-07-28', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Follow-up appointments'),
('AVAIL019', 'DOC004', '2025-07-29', '10:00:00', '13:00:00', 'ADHOC', 2, FALSE, 'Special neurology cases');

-- Doctor 5 (DOC005 - Pediatrician)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL020', 'DOC005', '2025-07-30', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Child wellness check'),
('AVAIL021', 'DOC005', '2025-07-30', '14:00:00', '17:00:00', 'STANDARD', 3, FALSE, 'Vaccination hours'),
('AVAIL022', 'DOC005', '2025-07-31', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Newborn consultations');

-- Doctor 6 (DOC006 - General Surgeon)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL023', 'DOC006', '2025-08-01', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Pre-surgery consultations'),
('AVAIL024', 'DOC006', '2025-08-01', '12:00:00', '15:00:00', 'STANDARD', 2, FALSE, 'Post-op follow-ups');

-- Doctor 7 (DOC007 - Dermatologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL025', 'DOC007', '2025-08-02', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Skin check clinic'),
('AVAIL026', 'DOC007', '2025-08-02', '14:00:00', '17:00:00', 'STANDARD', 3, FALSE, 'Cosmetic consultations');

-- Doctor 9 (DOC009 - Ophthalmologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL027', 'DOC009', '2025-08-03', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Eye examination'),
('AVAIL028', 'DOC009', '2025-08-03', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Vision therapy');

-- Doctor 10 (DOC010 - Psychiatrist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL029', 'DOC010', '2025-08-04', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Therapy sessions'),
('AVAIL030', 'DOC010', '2025-08-04', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Counseling');

-- Doctor 11 (DOC011 - Endocrinologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL031', 'DOC011', '2025-08-05', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Diabetes management'),
('AVAIL032', 'DOC011', '2025-08-05', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Hormone therapy');

-- Doctor 12 (DOC012 - ENT)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL033', 'DOC012', '2025-08-06', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Ear, nose, throat check'),
('AVAIL034', 'DOC012', '2025-08-06', '12:00:00', '15:00:00', 'STANDARD', 3, FALSE, 'Allergy consultations');

-- Doctor 13 (DOC013 - Rheumatologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL035', 'DOC013', '2025-08-07', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Arthritis clinic'),
('AVAIL036', 'DOC013', '2025-08-07', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Joint pain management');

-- Doctor 14 (DOC014 - Nephrologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL037', 'DOC014', '2025-08-08', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Kidney function tests'),
('AVAIL038', 'DOC014', '2025-08-08', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Dialysis consultations');

-- Doctor 15 (DOC015 - Oncologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL039', 'DOC015', '2025-08-09', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Cancer screening'),
('AVAIL040', 'DOC015', '2025-08-09', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Treatment planning');

-- Doctor 1 (DOC001 - General Physician)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL041', 'DOC001', '2025-08-11', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'General checkups'),
('AVAIL042', 'DOC001', '2025-08-11', '14:00:00', '17:00:00', 'STANDARD', 3, FALSE, 'Follow-up appointments'),
('AVAIL043', 'DOC001', '2025-08-14', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Routine consultations'),
('AVAIL044', 'DOC001', '2025-08-14', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Health certificates'),
('AVAIL045', 'DOC001', '2025-08-17', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Vaccination clinic'),
('AVAIL046', 'DOC001', '2025-08-17', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Chronic disease management'),
('AVAIL047', 'DOC001', '2025-08-20', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'General medicine'),
('AVAIL048', 'DOC001', '2025-08-20', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'Urgent care slots'),
('AVAIL049', 'DOC001', '2025-08-23', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Preventive care'),
('AVAIL050', 'DOC001', '2025-08-23', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Health screenings'),
('AVAIL051', 'DOC001', '2025-08-26', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Annual physicals'),
('AVAIL052', 'DOC001', '2025-08-26', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Medication reviews'),
('AVAIL053', 'DOC001', '2025-08-29', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Wellness visits'),
('AVAIL054', 'DOC001', '2025-08-29', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Diagnostic consultations'),
('AVAIL055', 'DOC001', '2025-09-01', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'General practice'),
('AVAIL056', 'DOC001', '2025-09-01', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Patient education'),
('AVAIL057', 'DOC001', '2025-09-04', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Family medicine'),
('AVAIL058', 'DOC001', '2025-09-04', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Geriatric care'),
('AVAIL059', 'DOC001', '2025-09-07', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Primary care'),
('AVAIL060', 'DOC001', '2025-09-07', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Minor procedures');

-- Doctor 2 (DOC002 - Cardiologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL061', 'DOC002', '2025-08-12', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Cardiac evaluations'),
('AVAIL062', 'DOC002', '2025-08-12', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'EKG readings'),
('AVAIL063', 'DOC002', '2025-08-15', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Heart health clinic'),
('AVAIL064', 'DOC002', '2025-08-15', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Cardiac rehab planning'),
('AVAIL065', 'DOC002', '2025-08-18', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Hypertension management'),
('AVAIL066', 'DOC002', '2025-08-18', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Cholesterol checks'),
('AVAIL067', 'DOC002', '2025-08-21', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Arrhythmia consultations'),
('AVAIL068', 'DOC002', '2025-08-21', '14:00:00', '17:00:00', 'ADHOC', 2, FALSE, 'Pacemaker follow-ups'),
('AVAIL069', 'DOC002', '2025-08-24', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Cardiac stress tests'),
('AVAIL070', 'DOC002', '2025-08-24', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Echocardiogram reviews'),
('AVAIL071', 'DOC002', '2025-08-27', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Heart failure clinic'),
('AVAIL072', 'DOC002', '2025-08-27', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Medication adjustments'),
('AVAIL073', 'DOC002', '2025-08-30', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Preventive cardiology'),
('AVAIL074', 'DOC002', '2025-08-30', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Cardiac risk assessments'),
('AVAIL075', 'DOC002', '2025-09-02', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Valve disorder clinic'),
('AVAIL076', 'DOC002', '2025-09-02', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Holter monitor reviews'),
('AVAIL077', 'DOC002', '2025-09-05', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Congenital heart checks'),
('AVAIL078', 'DOC002', '2025-09-05', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Post-cardiac event care'),
('AVAIL079', 'DOC002', '2025-09-08', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Cardiac imaging reviews'),
('AVAIL080', 'DOC002', '2025-09-08', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Angina management');

-- Doctor 3 (DOC003 - Orthopedist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL081', 'DOC003', '2025-08-13', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Fracture clinic'),
('AVAIL082', 'DOC003', '2025-08-13', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Sports injury evaluations'),
('AVAIL083', 'DOC003', '2025-08-16', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Joint replacement consults'),
('AVAIL084', 'DOC003', '2025-08-16', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Post-op follow-ups'),
('AVAIL085', 'DOC003', '2025-08-19', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Arthritis management'),
('AVAIL086', 'DOC003', '2025-08-19', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'Emergency injury slots'),
('AVAIL087', 'DOC003', '2025-08-22', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Pediatric orthopedics'),
('AVAIL088', 'DOC003', '2025-08-22', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Spine evaluations'),
('AVAIL089', 'DOC003', '2025-08-25', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Hand surgery consults'),
('AVAIL090', 'DOC003', '2025-08-25', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Casting and bracing'),
('AVAIL091', 'DOC003', '2025-08-28', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Osteoporosis clinic'),
('AVAIL092', 'DOC003', '2025-08-28', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Joint injections'),
('AVAIL093', 'DOC003', '2025-08-31', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Work-related injuries'),
('AVAIL094', 'DOC003', '2025-08-31', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Physical therapy planning'),
('AVAIL095', 'DOC003', '2025-09-03', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Foot and ankle clinic'),
('AVAIL096', 'DOC003', '2025-09-03', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Shoulder evaluations'),
('AVAIL097', 'DOC003', '2025-09-06', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Hip pain consultations'),
('AVAIL098', 'DOC003', '2025-09-06', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Knee injury follow-ups'),
('AVAIL099', 'DOC003', '2025-09-09', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Orthopedic trauma'),
('AVAIL100', 'DOC003', '2025-09-09', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Pre-surgical evaluations');

-- Continuing with the same pattern for remaining doctors (DOC004-DOC021)
-- Each doctor will have 20 new availability slots (10 dates × 2 slots per date)

-- Doctor 4 (DOC004 - Neurologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL101', 'DOC004', '2025-08-11', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Headache clinic'),
('AVAIL102', 'DOC004', '2025-08-11', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'EEG reviews'),
('AVAIL103', 'DOC004', '2025-08-14', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Epilepsy management'),
('AVAIL104', 'DOC004', '2025-08-14', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Neuromuscular evaluations'),
('AVAIL105', 'DOC004', '2025-08-17', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Stroke follow-ups'),
('AVAIL106', 'DOC004', '2025-08-17', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Cognitive testing'),
('AVAIL107', 'DOC004', '2025-08-20', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Multiple sclerosis clinic'),
('AVAIL108', 'DOC004', '2025-08-20', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'Emergency neuro consults'),
('AVAIL109', 'DOC004', '2025-08-23', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Movement disorders'),
('AVAIL110', 'DOC004', '2025-08-23', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Nerve conduction studies'),
('AVAIL111', 'DOC004', '2025-08-26', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Alzheimer''s evaluations'),
('AVAIL112', 'DOC004', '2025-08-26', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Parkinson''s management'),
('AVAIL113', 'DOC004', '2025-08-29', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Peripheral neuropathy'),
('AVAIL114', 'DOC004', '2025-08-29', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Brain injury rehab'),
('AVAIL115', 'DOC004', '2025-09-01', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Migraine management'),
('AVAIL116', 'DOC004', '2025-09-01', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'EMG testing reviews'),
('AVAIL117', 'DOC004', '2025-09-04', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Neuropathy clinic'),
('AVAIL118', 'DOC004', '2025-09-04', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Spinal cord evaluations'),
('AVAIL119', 'DOC004', '2025-09-07', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Concussion follow-ups'),
('AVAIL120', 'DOC004', '2025-09-07', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Neurogenetic disorders');

-- Doctor 5 (DOC005 - Pediatrician)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL121', 'DOC005', '2025-08-12', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Newborn checks'),
('AVAIL122', 'DOC005', '2025-08-12', '14:00:00', '17:00:00', 'STANDARD', 3, FALSE, 'Well-child visits'),
('AVAIL123', 'DOC005', '2025-08-15', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Vaccination clinic'),
('AVAIL124', 'DOC005', '2025-08-15', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Developmental screenings'),
('AVAIL125', 'DOC005', '2025-08-18', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'School physicals'),
('AVAIL126', 'DOC005', '2025-08-18', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Asthma management'),
('AVAIL127', 'DOC005', '2025-08-21', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'ADHD evaluations'),
('AVAIL128', 'DOC005', '2025-08-21', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'Sick child appointments'),
('AVAIL129', 'DOC005', '2025-08-24', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Infant nutrition'),
('AVAIL130', 'DOC005', '2025-08-24', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Allergy testing'),
('AVAIL131', 'DOC005', '2025-08-27', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Teen health'),
('AVAIL132', 'DOC005', '2025-08-27', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Behavioral consultations'),
('AVAIL133', 'DOC005', '2025-08-30', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Growth monitoring'),
('AVAIL134', 'DOC005', '2025-08-30', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Ear infection follow-ups'),
('AVAIL135', 'DOC005', '2025-09-02', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Sports physicals'),
('AVAIL136', 'DOC005', '2025-09-02', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Genetic counseling'),
('AVAIL137', 'DOC005', '2025-09-05', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'New patient evaluations'),
('AVAIL138', 'DOC005', '2025-09-05', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Chronic condition management'),
('AVAIL139', 'DOC005', '2025-09-08', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Immunization catch-up'),
('AVAIL140', 'DOC005', '2025-09-08', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Breastfeeding support');

-- Doctor 6 (DOC006 - General Surgeon)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL141', 'DOC006', '2025-08-13', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Pre-operative consultations'),
('AVAIL142', 'DOC006', '2025-08-13', '12:00:00', '15:00:00', 'STANDARD', 2, FALSE, 'Post-operative follow-ups'),
('AVAIL143', 'DOC006', '2025-08-16', '09:00:00', '12:00:00', 'STANDARD', 2, FALSE, 'Hernia evaluations'),
('AVAIL144', 'DOC006', '2025-08-16', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Gallbladder consultations'),
('AVAIL145', 'DOC006', '2025-08-19', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Appendectomy consults'),
('AVAIL146', 'DOC006', '2025-08-19', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Wound care management'),
('AVAIL147', 'DOC006', '2025-08-22', '09:00:00', '12:00:00', 'STANDARD', 2, FALSE, 'Colorectal surgery reviews'),
('AVAIL148', 'DOC006', '2025-08-22', '14:00:00', '17:00:00', 'ADHOC', 1, FALSE, 'Emergency surgical consults'),
('AVAIL149', 'DOC006', '2025-08-25', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Biopsy discussions'),
('AVAIL150', 'DOC006', '2025-08-25', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Surgical clearance exams'),
('AVAIL151', 'DOC006', '2025-08-28', '09:00:00', '12:00:00', 'STANDARD', 2, FALSE, 'Laparoscopic consultations'),
('AVAIL152', 'DOC006', '2025-08-28', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Tumor board follow-ups'),
('AVAIL153', 'DOC006', '2025-08-31', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Trauma surgery reviews'),
('AVAIL154', 'DOC006', '2025-08-31', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Surgical wound checks'),
('AVAIL155', 'DOC006', '2025-09-03', '09:00:00', '12:00:00', 'STANDARD', 2, FALSE, 'Breast surgery consults'),
('AVAIL156', 'DOC006', '2025-09-03', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Thyroid evaluations'),
('AVAIL157', 'DOC006', '2025-09-06', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Vascular access planning'),
('AVAIL158', 'DOC006', '2025-09-06', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Surgical oncology consults'),
('AVAIL159', 'DOC006', '2025-09-09', '09:00:00', '12:00:00', 'STANDARD', 2, FALSE, 'Minimally invasive options'),
('AVAIL160', 'DOC006', '2025-09-09', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Surgical second opinions');

-- Doctor 7 (DOC007 - Dermatologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL161', 'DOC007', '2025-08-11', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Acne treatment clinic'),
('AVAIL162', 'DOC007', '2025-08-11', '14:00:00', '17:00:00', 'STANDARD', 3, FALSE, 'Skin cancer screenings'),
('AVAIL163', 'DOC007', '2025-08-14', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Eczema management'),
('AVAIL164', 'DOC007', '2025-08-14', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Psoriasis treatments'),
('AVAIL165', 'DOC007', '2025-08-17', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Cosmetic consultations'),
('AVAIL166', 'DOC007', '2025-08-17', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Botox injections'),
('AVAIL167', 'DOC007', '2025-08-20', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Rash evaluations'),
('AVAIL168', 'DOC007', '2025-08-20', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'Urgent dermatology issues'),
('AVAIL169', 'DOC007', '2025-08-23', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Hair loss treatments'),
('AVAIL170', 'DOC007', '2025-08-23', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Nail disorder clinic'),
('AVAIL171', 'DOC007', '2025-08-26', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Pediatric dermatology'),
('AVAIL172', 'DOC007', '2025-08-26', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Laser therapy consults'),
('AVAIL173', 'DOC007', '2025-08-29', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Allergy patch testing'),
('AVAIL174', 'DOC007', '2025-08-29', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Wart removal clinic'),
('AVAIL175', 'DOC007', '2025-09-01', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Rosacea management'),
('AVAIL176', 'DOC007', '2025-09-01', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Mole evaluations'),
('AVAIL177', 'DOC007', '2025-09-04', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Filler injections'),
('AVAIL178', 'DOC007', '2025-09-04', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Chemical peel consults'),
('AVAIL179', 'DOC007', '2025-09-07', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Sun damage assessments'),
('AVAIL180', 'DOC007', '2025-09-07', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Biopsy follow-ups');

-- Doctor 9 (DOC009 - Ophthalmologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL181', 'DOC009', '2025-08-12', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Comprehensive eye exams'),
('AVAIL182', 'DOC009', '2025-08-12', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Glaucoma evaluations'),
('AVAIL183', 'DOC009', '2025-08-15', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Cataract consultations'),
('AVAIL184', 'DOC009', '2025-08-15', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Diabetic eye exams'),
('AVAIL185', 'DOC009', '2025-08-18', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'LASIK evaluations'),
('AVAIL186', 'DOC009', '2025-08-18', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Retinal imaging reviews'),
('AVAIL187', 'DOC009', '2025-08-21', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Dry eye treatments'),
('AVAIL188', 'DOC009', '2025-08-21', '14:00:00', '17:00:00', 'ADHOC', 2, FALSE, 'Emergency eye care'),
('AVAIL189', 'DOC009', '2025-08-24', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Pediatric ophthalmology'),
('AVAIL190', 'DOC009', '2025-08-24', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Contact lens fittings'),
('AVAIL191', 'DOC009', '2025-08-27', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Macular degeneration'),
('AVAIL192', 'DOC009', '2025-08-27', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Visual field testing'),
('AVAIL193', 'DOC009', '2025-08-30', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Eye infection clinic'),
('AVAIL194', 'DOC009', '2025-08-30', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Post-op cataract checks'),
('AVAIL195', 'DOC009', '2025-09-02', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Eye pressure monitoring'),
('AVAIL196', 'DOC009', '2025-09-02', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Ocular inflammation'),
('AVAIL197', 'DOC009', '2025-09-05', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Low vision services'),
('AVAIL198', 'DOC009', '2025-09-05', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Corneal evaluations'),
('AVAIL199', 'DOC009', '2025-09-08', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Eye muscle therapy'),
('AVAIL200', 'DOC009', '2025-09-08', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Optic nerve exams');

-- Doctor 10 (DOC010 - Psychiatrist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL201', 'DOC010', '2025-08-13', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Depression evaluations'),
('AVAIL202', 'DOC010', '2025-08-13', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Anxiety management'),
('AVAIL203', 'DOC010', '2025-08-16', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Medication management'),
('AVAIL204', 'DOC010', '2025-08-16', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Bipolar disorder clinic'),
('AVAIL205', 'DOC010', '2025-08-19', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'ADHD assessments'),
('AVAIL206', 'DOC010', '2025-08-19', '15:00:00', '18:00:00', 'ADHOC', 1, FALSE, 'Crisis intervention'),
('AVAIL207', 'DOC010', '2025-08-22', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'PTSD therapy'),
('AVAIL208', 'DOC010', '2025-08-22', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'OCD treatments'),
('AVAIL209', 'DOC010', '2025-08-25', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Schizophrenia management'),
('AVAIL210', 'DOC010', '2025-08-25', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Sleep disorder clinic'),
('AVAIL211', 'DOC010', '2025-08-28', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Addiction counseling'),
('AVAIL212', 'DOC010', '2025-08-28', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Mood disorder follow-ups'),
('AVAIL213', 'DOC010', '2025-08-31', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Geriatric psychiatry'),
('AVAIL214', 'DOC010', '2025-08-31', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Eating disorder therapy'),
('AVAIL215', 'DOC010', '2025-09-03', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Psychotic disorders'),
('AVAIL216', 'DOC010', '2025-09-03', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Personality disorder consults'),
('AVAIL217', 'DOC010', '2025-09-06', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Psychiatric evaluations'),
('AVAIL218', 'DOC010', '2025-09-06', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Medication adjustments'),
('AVAIL219', 'DOC010', '2025-09-09', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Cognitive behavioral therapy'),
('AVAIL220', 'DOC010', '2025-09-09', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Stress management');

-- Doctor 11 (DOC011 - Endocrinologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL221', 'DOC011', '2025-08-11', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Diabetes management clinic'),
('AVAIL222', 'DOC011', '2025-08-11', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Thyroid function reviews'),
('AVAIL223', 'DOC011', '2025-08-14', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Hormone therapy consults'),
('AVAIL224', 'DOC011', '2025-08-14', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Metabolic disorder evaluations'),
('AVAIL225', 'DOC011', '2025-08-17', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Insulin pump training'),
('AVAIL226', 'DOC011', '2025-08-17', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'PCOS management'),
('AVAIL227', 'DOC011', '2025-08-20', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Diabetes education'),
('AVAIL228', 'DOC011', '2025-08-20', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'Endocrine emergencies'),
('AVAIL229', 'DOC011', '2025-08-23', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Osteoporosis evaluations'),
('AVAIL230', 'DOC011', '2025-08-23', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Calcium disorder clinic'),
('AVAIL231', 'DOC011', '2025-08-26', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Pituitary disorder consults'),
('AVAIL232', 'DOC011', '2025-08-26', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Adrenal function tests'),
('AVAIL233', 'DOC011', '2025-08-29', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Weight management clinic'),
('AVAIL234', 'DOC011', '2025-08-29', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Lipid disorder follow-ups'),
('AVAIL235', 'DOC011', '2025-09-01', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Gestational diabetes'),
('AVAIL236', 'DOC011', '2025-09-01', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Hypoglycemia evaluations'),
('AVAIL237', 'DOC011', '2025-09-04', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Type 1 diabetes care'),
('AVAIL238', 'DOC011', '2025-09-04', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Endocrine tumor consults'),
('AVAIL239', 'DOC011', '2025-09-07', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Metabolic syndrome clinic'),
('AVAIL240', 'DOC011', '2025-09-07', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Hormone replacement therapy');

-- Doctor 12 (DOC012 - ENT Specialist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL241', 'DOC012', '2025-08-12', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Sinusitis evaluations'),
('AVAIL242', 'DOC012', '2025-08-12', '12:00:00', '15:00:00', 'STANDARD', 3, FALSE, 'Hearing loss assessments'),
('AVAIL243', 'DOC012', '2025-08-15', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Tonsillitis clinic'),
('AVAIL244', 'DOC012', '2025-08-15', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Voice disorder evaluations'),
('AVAIL245', 'DOC012', '2025-08-18', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Ear infection follow-ups'),
('AVAIL246', 'DOC012', '2025-08-18', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Allergy-related ENT issues'),
('AVAIL247', 'DOC012', '2025-08-21', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Nasal obstruction consults'),
('AVAIL248', 'DOC012', '2025-08-21', '14:00:00', '17:00:00', 'ADHOC', 2, FALSE, 'ENT emergencies'),
('AVAIL249', 'DOC012', '2025-08-24', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Sleep apnea evaluations'),
('AVAIL250', 'DOC012', '2025-08-24', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Tinnitus management'),
('AVAIL251', 'DOC012', '2025-08-27', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Pediatric ENT clinic'),
('AVAIL252', 'DOC012', '2025-08-27', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Balance disorder tests'),
('AVAIL253', 'DOC012', '2025-08-30', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Head and neck cancer screenings'),
('AVAIL254', 'DOC012', '2025-08-30', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Facial trauma follow-ups'),
('AVAIL255', 'DOC012', '2025-09-02', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Deviated septum evaluations'),
('AVAIL256', 'DOC012', '2025-09-02', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Swallowing disorder clinic'),
('AVAIL257', 'DOC012', '2025-09-05', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Earwax removal clinic'),
('AVAIL258', 'DOC012', '2025-09-05', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'ENT surgical consults'),
('AVAIL259', 'DOC012', '2025-09-08', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Chronic cough evaluations'),
('AVAIL260', 'DOC012', '2025-09-08', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Nasal polyp follow-ups');

-- Doctor 13 (DOC013 - Rheumatologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL261', 'DOC013', '2025-08-13', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Arthritis pain management'),
('AVAIL262', 'DOC013', '2025-08-13', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Lupus evaluations'),
('AVAIL263', 'DOC013', '2025-08-16', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Rheumatoid arthritis clinic'),
('AVAIL264', 'DOC013', '2025-08-16', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Joint injection procedures'),
('AVAIL265', 'DOC013', '2025-08-19', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Fibromyalgia assessments'),
('AVAIL266', 'DOC013', '2025-08-19', '15:00:00', '18:00:00', 'ADHOC', 1, FALSE, 'Acute joint inflammation'),
('AVAIL267', 'DOC013', '2025-08-22', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Osteoarthritis management'),
('AVAIL268', 'DOC013', '2025-08-22', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Autoimmune disorder consults'),
('AVAIL269', 'DOC013', '2025-08-25', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Gout treatment clinic'),
('AVAIL270', 'DOC013', '2025-08-25', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Vasculitis evaluations'),
('AVAIL271', 'DOC013', '2025-08-28', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Psoriatic arthritis clinic'),
('AVAIL272', 'DOC013', '2025-08-28', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Biologic therapy monitoring'),
('AVAIL273', 'DOC013', '2025-08-31', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Scleroderma evaluations'),
('AVAIL274', 'DOC013', '2025-08-31', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Pediatric rheumatology'),
('AVAIL275', 'DOC013', '2025-09-03', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Sjogren\'s syndrome clinic'),
('AVAIL276', 'DOC013', '2025-09-03', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Inflammatory back pain'),
('AVAIL277', 'DOC013', '2025-09-06', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Polymyalgia rheumatica'),
('AVAIL278', 'DOC013', '2025-09-06', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Rheumatology lab reviews'),
('AVAIL279', 'DOC013', '2025-09-09', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Connective tissue disorders'),
('AVAIL280', 'DOC013', '2025-09-09', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Rheumatology second opinions');

-- Doctor 14 (DOC014 - Nephrologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL281', 'DOC014', '2025-08-11', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Chronic kidney disease clinic'),
('AVAIL282', 'DOC014', '2025-08-11', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Dialysis evaluations'),
('AVAIL283', 'DOC014', '2025-08-14', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Hypertension kidney consults'),
('AVAIL284', 'DOC014', '2025-08-14', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Proteinuria assessments'),
('AVAIL285', 'DOC014', '2025-08-17', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Kidney stone prevention'),
('AVAIL286', 'DOC014', '2025-08-17', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Electrolyte disorder clinic'),
('AVAIL287', 'DOC014', '2025-08-20', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Acute kidney injury follow-ups'),
('AVAIL288', 'DOC014', '2025-08-20', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'Renal emergencies'),
('AVAIL289', 'DOC014', '2025-08-23', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Glomerular disease clinic'),
('AVAIL290', 'DOC014', '2025-08-23', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Transplant evaluations'),
('AVAIL291', 'DOC014', '2025-08-26', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Diabetic kidney disease'),
('AVAIL292', 'DOC014', '2025-08-26', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Nephrotic syndrome'),
('AVAIL293', 'DOC014', '2025-08-29', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Polycystic kidney disease'),
('AVAIL294', 'DOC014', '2025-08-29', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Renal artery stenosis'),
('AVAIL295', 'DOC014', '2025-09-01', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Home dialysis training'),
('AVAIL296', 'DOC014', '2025-09-01', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Kidney biopsy reviews'),
('AVAIL297', 'DOC014', '2025-09-04', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Pediatric nephrology'),
('AVAIL298', 'DOC014', '2025-09-04', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Renal nutrition counseling'),
('AVAIL299', 'DOC014', '2025-09-07', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Hyponatremia management'),
('AVAIL300', 'DOC014', '2025-09-07', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Post-transplant care');

-- Doctor 15 (DOC015 - Oncologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL301', 'DOC015', '2025-08-12', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Breast cancer consults'),
('AVAIL302', 'DOC015', '2025-08-12', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Lung cancer evaluations'),
('AVAIL303', 'DOC015', '2025-08-15', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Chemotherapy planning'),
('AVAIL304', 'DOC015', '2025-08-15', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Radiation therapy consults'),
('AVAIL305', 'DOC015', '2025-08-18', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Colorectal cancer clinic'),
('AVAIL306', 'DOC015', '2025-08-18', '14:00:00', '17:00:00', 'ADHOC', 1, FALSE, 'Oncology emergencies'),
('AVAIL307', 'DOC015', '2025-08-21', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Prostate cancer reviews'),
('AVAIL308', 'DOC015', '2025-08-21', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Immunotherapy options'),
('AVAIL309', 'DOC015', '2025-08-24', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Hematology oncology'),
('AVAIL310', 'DOC015', '2025-08-24', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Cancer genetic testing'),
('AVAIL311', 'DOC015', '2025-08-27', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Palliative care consults'),
('AVAIL312', 'DOC015', '2025-08-27', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Clinical trial options'),
('AVAIL313', 'DOC015', '2025-08-30', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Lymphoma evaluations'),
('AVAIL314', 'DOC015', '2025-08-30', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Survivorship planning'),
('AVAIL315', 'DOC015', '2025-09-02', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Melanoma clinic'),
('AVAIL316', 'DOC015', '2025-09-02', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Cancer pain management'),
('AVAIL317', 'DOC015', '2025-09-05', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Pancreatic cancer consults'),
('AVAIL318', 'DOC015', '2025-09-05', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Second opinion reviews'),
('AVAIL319', 'DOC015', '2025-09-08', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Head and neck oncology'),
('AVAIL320', 'DOC015', '2025-09-08', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Supportive care services');

-- Doctor 16 (DOC016 - Urologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL321', 'DOC016', '2025-08-13', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Prostate health clinic'),
('AVAIL322', 'DOC016', '2025-08-13', '12:00:00', '15:00:00', 'STANDARD', 3, FALSE, 'Kidney stone evaluations'),
('AVAIL323', 'DOC016', '2025-08-16', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Bladder cancer screenings'),
('AVAIL324', 'DOC016', '2025-08-16', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Incontinence treatments'),
('AVAIL325', 'DOC016', '2025-08-19', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Vasectomy consultations'),
('AVAIL326', 'DOC016', '2025-08-19', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'UTI follow-ups'),
('AVAIL327', 'DOC016', '2025-08-22', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Erectile dysfunction clinic'),
('AVAIL328', 'DOC016', '2025-08-22', '14:00:00', '17:00:00', 'ADHOC', 2, FALSE, 'Urological emergencies'),
('AVAIL329', 'DOC016', '2025-08-25', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Pediatric urology'),
('AVAIL330', 'DOC016', '2025-08-25', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'PSA test reviews'),
('AVAIL331', 'DOC016', '2025-08-28', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Overactive bladder clinic'),
('AVAIL332', 'DOC016', '2025-08-28', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Urodynamic testing'),
('AVAIL333', 'DOC016', '2025-08-31', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Prostate biopsy consults'),
('AVAIL334', 'DOC016', '2025-08-31', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Male infertility evaluations'),
('AVAIL335', 'DOC016', '2025-09-03', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Renal mass assessments'),
('AVAIL336', 'DOC016', '2025-09-03', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Cystoscopy follow-ups'),
('AVAIL337', 'DOC016', '2025-09-06', '08:00:00', '11:00:00', 'STANDARD', 3, FALSE, 'Testicular health clinic'),
('AVAIL338', 'DOC016', '2025-09-06', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Urethral stricture evaluations'),
('AVAIL339', 'DOC016', '2025-09-09', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'BPH treatment options'),
('AVAIL340', 'DOC016', '2025-09-09', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Urologic oncology consults');

-- Doctor 17 (DOC017 - Gastroenterologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL341', 'DOC017', '2025-08-11', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Colonoscopy consultations'),
('AVAIL342', 'DOC017', '2025-08-11', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'IBD management clinic'),
('AVAIL343', 'DOC017', '2025-08-14', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'GERD treatment options'),
('AVAIL344', 'DOC017', '2025-08-14', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Liver disease evaluations'),
('AVAIL345', 'DOC017', '2025-08-17', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Celiac disease clinic'),
('AVAIL346', 'DOC017', '2025-08-17', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Pancreatic function tests'),
('AVAIL347', 'DOC017', '2025-08-20', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Endoscopy prep instructions'),
('AVAIL348', 'DOC017', '2025-08-20', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'GI bleeding evaluations'),
('AVAIL349', 'DOC017', '2025-08-23', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'IBS management clinic'),
('AVAIL350', 'DOC017', '2025-08-23', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Food intolerance testing'),
('AVAIL351', 'DOC017', '2025-08-26', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Hepatitis monitoring'),
('AVAIL352', 'DOC017', '2025-08-26', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Colorectal cancer screening'),
('AVAIL353', 'DOC017', '2025-08-29', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Gastroparesis evaluations'),
('AVAIL354', 'DOC017', '2025-08-29', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Bariatric surgery consults'),
('AVAIL355', 'DOC017', '2025-09-01', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Diverticulitis management'),
('AVAIL356', 'DOC017', '2025-09-01', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'ERCP follow-ups'),
('AVAIL357', 'DOC017', '2025-09-04', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Pediatric gastroenterology'),
('AVAIL358', 'DOC017', '2025-09-04', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Fecal transplant consults'),
('AVAIL359', 'DOC017', '2025-09-07', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Nutritional counseling'),
('AVAIL360', 'DOC017', '2025-09-07', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Gut microbiome assessments');

-- Doctor 18 (DOC018 - Plastic Surgeon)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL361', 'DOC018', '2025-08-12', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Breast reconstruction consults'),
('AVAIL362', 'DOC018', '2025-08-12', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Cosmetic surgery evaluations'),
('AVAIL363', 'DOC018', '2025-08-15', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Scar revision clinic'),
('AVAIL364', 'DOC018', '2025-08-15', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Botox and filler treatments'),
('AVAIL365', 'DOC018', '2025-08-18', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Burn reconstruction'),
('AVAIL366', 'DOC018', '2025-08-18', '14:00:00', '17:00:00', 'ADHOC', 1, FALSE, 'Trauma reconstruction'),
('AVAIL367', 'DOC018', '2025-08-21', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Rhinoplasty consultations'),
('AVAIL368', 'DOC018', '2025-08-21', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Laser skin resurfacing'),
('AVAIL369', 'DOC018', '2025-08-24', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Hand surgery evaluations'),
('AVAIL370', 'DOC018', '2025-08-24', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Post-bariatric body contouring'),
('AVAIL371', 'DOC018', '2025-08-27', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Facial trauma reconstruction'),
('AVAIL372', 'DOC018', '2025-08-27', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Microsurgery consults'),
('AVAIL373', 'DOC018', '2025-08-30', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Breast augmentation consults'),
('AVAIL374', 'DOC018', '2025-08-30', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Tummy tuck evaluations'),
('AVAIL375', 'DOC018', '2025-09-02', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Eyelid surgery clinic'),
('AVAIL376', 'DOC018', '2025-09-02', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Liposuction consultations'),
('AVAIL377', 'DOC018', '2025-09-05', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Skin cancer reconstruction'),
('AVAIL378', 'DOC018', '2025-09-05', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Cleft palate follow-ups'),
('AVAIL379', 'DOC018', '2025-09-08', '10:00:00', '13:00:00', 'STANDARD', 2, FALSE, 'Gender confirmation consults'),
('AVAIL380', 'DOC018', '2025-09-08', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Post-mastectomy revisions');

-- Doctor 19 (DOC019 - Pulmonologist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL381', 'DOC019', '2025-08-13', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Asthma management clinic'),
('AVAIL382', 'DOC019', '2025-08-13', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'COPD evaluations'),
('AVAIL383', 'DOC019', '2025-08-16', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Sleep apnea assessments'),
('AVAIL384', 'DOC019', '2025-08-16', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Pulmonary function tests'),
('AVAIL385', 'DOC019', '2025-08-19', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Lung cancer screenings'),
('AVAIL386', 'DOC019', '2025-08-19', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Bronchoscopy consults'),
('AVAIL387', 'DOC019', '2025-08-22', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Interstitial lung disease'),
('AVAIL388', 'DOC019', '2025-08-22', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'Respiratory emergencies'),
('AVAIL389', 'DOC019', '2025-08-25', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Pulmonary fibrosis clinic'),
('AVAIL390', 'DOC019', '2025-08-25', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Oxygen therapy reviews'),
('AVAIL391', 'DOC019', '2025-08-28', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Bronchiectasis management'),
('AVAIL392', 'DOC019', '2025-08-28', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Pulmonary hypertension'),
('AVAIL393', 'DOC019', '2025-08-31', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Occupational lung disease'),
('AVAIL394', 'DOC019', '2025-08-31', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'TB testing and treatment'),
('AVAIL395', 'DOC019', '2025-09-03', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Sarcoidosis evaluations'),
('AVAIL396', 'DOC019', '2025-09-03', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Lung transplant consults'),
('AVAIL397', 'DOC019', '2025-09-06', '09:00:00', '12:00:00', 'STANDARD', 3, FALSE, 'Pleural effusion clinic'),
('AVAIL398', 'DOC019', '2025-09-06', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Pulmonary rehabilitation'),
('AVAIL399', 'DOC019', '2025-09-09', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Cystic fibrosis care'),
('AVAIL400', 'DOC019', '2025-09-09', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Ventilator management');

-- Doctor 20 (DOC020 - Cardiac Surgeon)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL401', 'DOC020', '2025-08-11', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Bypass surgery consults'),
('AVAIL402', 'DOC020', '2025-08-11', '12:00:00', '15:00:00', 'STANDARD', 2, FALSE, 'Valve replacement evaluations'),
('AVAIL403', 'DOC020', '2025-08-14', '09:00:00', '12:00:00', 'STANDARD', 2, FALSE, 'Aortic aneurysm clinic'),
('AVAIL404', 'DOC020', '2025-08-14', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Post-op cardiac care'),
('AVAIL405', 'DOC020', '2025-08-17', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Minimally invasive options'),
('AVAIL406', 'DOC020', '2025-08-17', '13:00:00', '16:00:00', 'ADHOC', 1, FALSE, 'Cardiac surgical emergencies'),
('AVAIL407', 'DOC020', '2025-08-20', '09:00:00', '12:00:00', 'STANDARD', 2, FALSE, 'Congenital heart repairs'),
('AVAIL408', 'DOC020', '2025-08-20', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'LVAD implant consults'),
('AVAIL409', 'DOC020', '2025-08-23', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Heart transplant evaluations'),
('AVAIL410', 'DOC020', '2025-08-23', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Surgical second opinions'),
('AVAIL411', 'DOC020', '2025-08-26', '09:00:00', '12:00:00', 'STANDARD', 2, FALSE, 'Atrial fibrillation surgery'),
('AVAIL412', 'DOC020', '2025-08-26', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Cardiac tumor evaluations'),
('AVAIL413', 'DOC020', '2025-08-29', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Pericardial window consults'),
('AVAIL414', 'DOC020', '2025-08-29', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Post-cardiotomy care'),
('AVAIL415', 'DOC020', '2025-09-01', '09:00:00', '12:00:00', 'STANDARD', 2, FALSE, 'Vascular access surgery'),
('AVAIL416', 'DOC020', '2025-09-01', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Mechanical valve reviews'),
('AVAIL417', 'DOC020', '2025-09-04', '08:00:00', '11:00:00', 'STANDARD', 2, FALSE, 'Pediatric cardiac surgery'),
('AVAIL418', 'DOC020', '2025-09-04', '13:00:00', '16:00:00', 'STANDARD', 2, FALSE, 'Surgical risk assessments'),
('AVAIL419', 'DOC020', '2025-09-07', '09:00:00', '12:00:00', 'STANDARD', 2, FALSE, 'Aortic dissection repairs'),
('AVAIL420', 'DOC020', '2025-09-07', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Post-op complication reviews');

-- Doctor 21 (DOC021 - Infectious Disease Specialist)
INSERT INTO Doctor_availability (availability_id, doctor_id, available_date, start_time, end_time, slot_type, max_capacity, is_recurring, notes) VALUES
('AVAIL421', 'DOC021', '2025-08-12', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'HIV/AIDS management'),
('AVAIL422', 'DOC021', '2025-08-12', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Tropical disease clinic'),
('AVAIL423', 'DOC021', '2025-08-15', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Antibiotic stewardship'),
('AVAIL424', 'DOC021', '2025-08-15', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Post-travel infections'),
('AVAIL425', 'DOC021', '2025-08-18', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'MRSA treatment plans'),
('AVAIL426', 'DOC021', '2025-08-18', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Vaccination counseling'),
('AVAIL427', 'DOC021', '2025-08-21', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Chronic infection clinic'),
('AVAIL428', 'DOC021', '2025-08-21', '15:00:00', '18:00:00', 'ADHOC', 2, FALSE, 'Fever of unknown origin'),
('AVAIL429', 'DOC021', '2025-08-24', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'TB treatment monitoring'),
('AVAIL430', 'DOC021', '2025-08-24', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Immunocompromised host'),
('AVAIL431', 'DOC021', '2025-08-27', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Hospital-acquired infections'),
('AVAIL432', 'DOC021', '2025-08-27', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Parasitic infection clinic'),
('AVAIL433', 'DOC021', '2025-08-30', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Lyme disease evaluations'),
('AVAIL434', 'DOC021', '2025-08-30', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Antimicrobial resistance'),
('AVAIL435', 'DOC021', '2025-09-02', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Viral hepatitis clinic'),
('AVAIL436', 'DOC021', '2025-09-02', '15:00:00', '18:00:00', 'STANDARD', 2, FALSE, 'Fungal infection treatments'),
('AVAIL437', 'DOC021', '2025-09-05', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Pediatric infectious disease'),
('AVAIL438', 'DOC021', '2025-09-05', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'Post-surgical infections'),
('AVAIL439', 'DOC021', '2025-09-08', '10:00:00', '13:00:00', 'STANDARD', 3, FALSE, 'Zoonotic disease clinic'),
('AVAIL440', 'DOC021', '2025-09-08', '14:00:00', '17:00:00', 'STANDARD', 2, FALSE, 'International travel prep');


