package com.parag.campuspulse.config;

import com.parag.campuspulse.model.*;
import com.parag.campuspulse.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner loadData(
            SchoolRepository schoolRepository,
            ProgramRepository programRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            VenueRepository venueRepository,
            EventCategoryRepository eventCategoryRepository) {

        return args -> {

            // ==========================================
            // STEP 0: CREATE FIRST ADMIN
            // ==========================================

            if (userRepository.count() == 0) {
                System.out.println("👑 Creating first admin...");
                String adminEmail = "admin@smvdu.ac.in";
                String defaultPassword = "admin";
                String hashedPassword = passwordEncoder.encode(defaultPassword);

                User firstAdmin = new User(adminEmail, hashedPassword, UserRole.SYSTEM_ADMIN);
                firstAdmin.setMustChangePassword(true);
                userRepository.save(firstAdmin);

                System.out.println("✅ First admin created!");
                System.out.println("   Email: " + adminEmail);
                System.out.println("   Password: " + defaultPassword);
                System.out.println();
            }

            // ==========================================
            // STEP 1: LOAD SCHOOLS AND PROGRAMS
            // ==========================================

            if (schoolRepository.count() > 0) {
                System.out.println("✅ Schools and programs already loaded");

                if (venueRepository.count() > 0 && eventCategoryRepository.count() > 0) {
                    System.out.println("✅ Venues and categories already loaded");
                    return;
                }
            } else {
                System.out.println("📚 Loading SMVDU schools and programs...");

                // CREATE SCHOOLS
                School cse = schoolRepository.save(new School("CSE", "School of Computer Science & Engineering", "Engineering"));
                School ece = schoolRepository.save(new School("ECE", "School of Electronics & Communication Engineering", "Engineering"));
                School mech = schoolRepository.save(new School("MECH", "School of Mechanical Engineering", "Engineering"));
                School civil = schoolRepository.save(new School("CIVIL", "School of Civil Engineering", "Engineering"));
                School ee = schoolRepository.save(new School("EE", "School of Electrical Engineering", "Engineering"));
                School energy = schoolRepository.save(new School("ENERGY", "School of Energy Management", "Engineering"));
                School arch = schoolRepository.save(new School("ARCH", "School of Architecture & Landscape Design", "Engineering"));
                School physics = schoolRepository.save(new School("PHYSICS", "School of Physics", "Sciences"));
                School math = schoolRepository.save(new School("MATH", "School of Mathematics", "Sciences"));
                School biotech = schoolRepository.save(new School("BIOTECH", "School of Biotechnology", "Sciences"));
                School business = schoolRepository.save(new School("BUSINESS", "School of Business", "Management"));
                School econ = schoolRepository.save(new School("ECON", "School of Economics", "Management"));
                School phil = schoolRepository.save(new School("PHIL", "School of Philosophy & Culture", "Humanities"));
                School lang = schoolRepository.save(new School("LANG", "School of Languages & Literature", "Humanities"));
                School design = schoolRepository.save(new School("DESIGN", "School of Design", "Other"));
                School nursing = schoolRepository.save(new School("NURSING", "SMVD College of Nursing", "Other"));

                System.out.println("✅ Created " + schoolRepository.count() + " schools");

                // CREATE PROGRAMS (keeping your existing program code)
                programRepository.save(new Program("bcs", "BTech Computer Science & Engineering", cse, "UG", 4));
                programRepository.save(new Program("bec", "BTech Electronics & Communication Engineering", ece, "UG", 4));
                programRepository.save(new Program("bee", "BTech Electrical Engineering", ee, "UG", 4));
                programRepository.save(new Program("bme", "BTech Mechanical Engineering", mech, "UG", 4));
                programRepository.save(new Program("bce", "BTech Civil Engineering", civil, "UG", 4));

                Program bcm = new Program("bcm", "BTech Mathematics & Computing", math, "UG", 4);
                bcm.setAvailableFromYear(2024);
                programRepository.save(bcm);

                Program bib = new Program("bib", "BTech Biotechnology", biotech, "UG", 4);
                bib.setAvailableFromYear(2024);
                programRepository.save(bib);

                programRepository.save(new Program("bar", "Bachelor of Architecture", arch, "UG", 5));

                Program bbd = new Program("bbd", "Bachelor of Design", design, "UG", 5);
                bbd.setAvailableFromYear(2024);
                programRepository.save(bbd);

                // Integrated programs (2021-2023 only)
                Program ibb = new Program("ibb", "Integrated BBA-MBA", business, "INTEGRATED", 5);
                ibb.setHasEarlyExit(true);
                ibb.setEarlyExitYear(3);
                ibb.setAvailableUntilYear(2023);
                programRepository.save(ibb);

                Program ibe = new Program("ibe", "Integrated BA-MA English", lang, "INTEGRATED", 5);
                ibe.setHasEarlyExit(true);
                ibe.setEarlyExitYear(3);
                ibe.setAvailableUntilYear(2023);
                programRepository.save(ibe);

                Program ibp = new Program("ibp", "Integrated BA-MA Philosophy", phil, "INTEGRATED", 5);
                ibp.setHasEarlyExit(true);
                ibp.setEarlyExitYear(3);
                ibp.setAvailableUntilYear(2023);
                programRepository.save(ibp);

                Program ibo = new Program("ibo", "Integrated BSc-MSc Biotechnology", biotech, "INTEGRATED", 5);
                ibo.setHasEarlyExit(true);
                ibo.setEarlyExitYear(3);
                ibo.setAvailableUntilYear(2023);
                programRepository.save(ibo);

                Program iby = new Program("iby", "Integrated BSc-MSc Physics", physics, "INTEGRATED", 5);
                iby.setHasEarlyExit(true);
                iby.setEarlyExitYear(3);
                iby.setAvailableUntilYear(2023);
                programRepository.save(iby);

                Program ibm = new Program("ibm", "Integrated BSc-MSc Mathematics", math, "INTEGRATED", 5);
                ibm.setHasEarlyExit(true);
                ibm.setEarlyExitYear(3);
                ibm.setAvailableUntilYear(2023);
                programRepository.save(ibm);

                Program iba = new Program("iba", "Integrated BA-MA Economics", econ, "INTEGRATED", 5);
                iba.setHasEarlyExit(true);
                iba.setEarlyExitYear(3);
                iba.setAvailableUntilYear(2023);
                programRepository.save(iba);

                // FYUP programs (2024+)
                Program bbb = new Program("bbb", "BBA (4-year FYUP)", business, "UG", 4);
                bbb.setAvailableFromYear(2024);
                programRepository.save(bbb);

                Program bba = new Program("bba", "BA Economics (4-year FYUP)", econ, "UG", 4);
                bba.setAvailableFromYear(2024);
                programRepository.save(bba);

                Program bbe = new Program("bbe", "BA English (4-year FYUP)", lang, "UG", 4);
                bbe.setAvailableFromYear(2024);
                programRepository.save(bbe);

                Program bbo = new Program("bbo", "BSc Biotechnology (4-year FYUP)", biotech, "UG", 4);
                bbo.setAvailableFromYear(2024);
                programRepository.save(bbo);

                Program bby = new Program("bby", "BSc Physics (4-year FYUP)", physics, "UG", 4);
                bby.setAvailableFromYear(2024);
                programRepository.save(bby);

                Program bbm = new Program("bbm", "BSc Mathematics (4-year FYUP)", math, "UG", 4);
                bbm.setAvailableFromYear(2024);
                programRepository.save(bbm);

                Program bdd = new Program("bdd", "Design Your Degree", design, "UG", 4);
                bdd.setAvailableFromYear(2024);
                programRepository.save(bdd);

                programRepository.save(new Program("bns", "BSc Nursing", nursing, "UG", 3));

                // PG programs
                programRepository.save(new Program("mmb", "Master of Business Administration", business, "PG", 2));
                programRepository.save(new Program("men", "MA English", lang, "PG", 2));
                programRepository.save(new Program("mvs", "MA Sanskrit", lang, "PG", 2));
                programRepository.save(new Program("mmp", "MA Philosophy", phil, "PG", 2));
                programRepository.save(new Program("meo", "MA Economics", econ, "PG", 2));
                programRepository.save(new Program("mms", "MTech Computer Science & Engineering", cse, "PG", 2));
                programRepository.save(new Program("msm", "MTech Mechanical Engineering", mech, "PG", 2));
                programRepository.save(new Program("mmc", "MTech Electronics & Communication Engineering", ece, "PG", 2));
                programRepository.save(new Program("mes", "MTech Energy Management", energy, "PG", 2));
                programRepository.save(new Program("mpy", "MSc Physics", physics, "PG", 2));
                programRepository.save(new Program("mbt", "MSc Biotechnology", biotech, "PG", 2));
                programRepository.save(new Program("mmm", "MSc Mathematics", math, "PG", 2));

                System.out.println("✅ Created " + programRepository.count() + " programs");
            }

            // ==========================================
            // STEP 2: LOAD VENUES (79 venues)
            // ==========================================

            if (venueRepository.count() == 0) {
                System.out.println("\n🏛️  Loading venues...");

                // Get schools for venue assignment
                School cse = schoolRepository.findByCode("CSE").orElse(null);
                School ece = schoolRepository.findByCode("ECE").orElse(null);
                School mech = schoolRepository.findByCode("MECH").orElse(null);
                School ee = schoolRepository.findByCode("EE").orElse(null);
                School biotech = schoolRepository.findByCode("BIOTECH").orElse(null);
                School physics = schoolRepository.findByCode("PHYSICS").orElse(null);
                School civil = schoolRepository.findByCode("CIVIL").orElse(null);
                School arch = schoolRepository.findByCode("ARCH").orElse(null);
                School business = schoolRepository.findByCode("BUSINESS").orElse(null);
                School econ = schoolRepository.findByCode("ECON").orElse(null);
                School lang = schoolRepository.findByCode("LANG").orElse(null);
                School phil = schoolRepository.findByCode("PHIL").orElse(null);
                School design = schoolRepository.findByCode("DESIGN").orElse(null);

                // === CENTRAL MANAGED (6) ===
                Venue matrika = new Venue("Matrika Auditorium", VenueType.MANAGED, 1000);
                matrika.setLocation("Main Academic Block");
                matrika.setApprovalAuthority(VenueApprovalAuthority.CENTRAL_ADMIN);
                matrika.setAmenities("Projector, Sound System, AC, Stage");
                venueRepository.save(matrika);

                Venue sanskriti = new Venue("Sanskriti Kaksh", VenueType.MANAGED, 200);
                sanskriti.setLocation("Main Academic Block");
                sanskriti.setApprovalAuthority(VenueApprovalAuthority.CENTRAL_ADMIN);
                sanskriti.setAmenities("Projector, AC");
                venueRepository.save(sanskriti);

                for (int i = 1; i <= 4; i++) {
                    Venue lt = new Venue("Lecture Theatre " + i, VenueType.MANAGED, 120);
                    lt.setLocation("Academic Block");
                    lt.setApprovalAuthority(VenueApprovalAuthority.CENTRAL_ADMIN);
                    lt.setAmenities("Projector, Microphone");
                    venueRepository.save(lt);
                }

                // === CSE (COE Building + D Block) - 13 venues ===
                if (cse != null) {
                    Venue cseGF = new Venue("CSE Department Ground Floor", VenueType.MANAGED, 50);
                    cseGF.setLocation("COE Building");
                    cseGF.setSchool(cse);
                    cseGF.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    venueRepository.save(cseGF);

                    Venue cseConf = new Venue("Conference Room SoCSE", VenueType.MANAGED, 50);
                    cseConf.setLocation("COE Building");
                    cseConf.setSchool(cse);
                    cseConf.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    venueRepository.save(cseConf);

                    String[] cseRooms = {"COE205", "COE206", "D107", "D206"};
                    for (String room : cseRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation(room.startsWith("COE") ? "COE Building" : "Block D");
                        v.setSchool(cse);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }

                    String[] cseLabs = {"DBMS Lab", "Linux Lab", "Programming Lab", "Basic Computing Lab", "Project Lab"};
                    for (String lab : cseLabs) {
                        Venue v = new Venue(lab, VenueType.MANAGED, 60);
                        v.setLocation("COE Building");
                        v.setSchool(cse);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        v.setAmenities("60 Computers, Projector");
                        venueRepository.save(v);
                    }

                    Venue dlLab = new Venue("Deep Learning Lab", VenueType.MANAGED, 120);
                    dlLab.setLocation("COE Building");
                    dlLab.setSchool(cse);
                    dlLab.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    dlLab.setAmenities("120 Computers, GPUs");
                    venueRepository.save(dlLab);

                    Venue internetLab = new Venue("Internet Lab", VenueType.MANAGED, 120);
                    internetLab.setLocation("COE Building");
                    internetLab.setSchool(cse);
                    internetLab.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    internetLab.setAmenities("120 Computers");
                    venueRepository.save(internetLab);
                }

                // === ECE (Block C) - 5 venues ===
                if (ece != null) {
                    String[] eceRooms = {"C105", "C205"};
                    for (String room : eceRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("Block C");
                        v.setSchool(ece);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }

                    String[] eceLabs = {"MATLAB/Simulink Lab", "Communication Systems Lab", "VLSI Design Lab"};
                    for (String lab : eceLabs) {
                        Venue v = new Venue(lab, VenueType.MANAGED, 60);
                        v.setLocation("Block C");
                        v.setSchool(ece);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === MECHANICAL (Block B) - 5 venues ===
                if (mech != null) {
                    Venue mechConf = new Venue("Conference Room SoME", VenueType.MANAGED, 50);
                    mechConf.setLocation("Block B");
                    mechConf.setSchool(mech);
                    mechConf.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    venueRepository.save(mechConf);

                    Venue tpOffice = new Venue("T&P Office", VenueType.MANAGED, 30);
                    tpOffice.setLocation("Block B");
                    tpOffice.setSchool(mech);
                    tpOffice.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    venueRepository.save(tpOffice);

                    String[] mechRooms = {"B101", "B201"};
                    for (String room : mechRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("Block B");
                        v.setSchool(mech);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }

                    String[] mechLabs = {"Thermal Engineering Lab", "CAD/CAM Lab"};
                    for (String lab : mechLabs) {
                        Venue v = new Venue(lab, VenueType.MANAGED, 60);
                        v.setLocation("Block B");
                        v.setSchool(mech);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === ELECTRICAL (Block B) - 4 venues ===
                if (ee != null) {
                    String[] eeRooms = {"B102", "B202"};
                    for (String room : eeRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("Block B");
                        v.setSchool(ee);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }

                    String[] eeLabs = {"Power Systems Lab", "Control Systems Lab"};
                    for (String lab : eeLabs) {
                        Venue v = new Venue(lab, VenueType.MANAGED, 60);
                        v.setLocation("Block B");
                        v.setSchool(ee);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === CENTRAL WORKSHOP (Dual Approval) ===
                Venue centralWorkshop = new Venue("Central Workshop", VenueType.MANAGED, 80);
                centralWorkshop.setLocation("Between Block B and COALD");
                centralWorkshop.setApprovalAuthority(VenueApprovalAuthority.DUAL_DEPARTMENT);
                centralWorkshop.setAmenities("Lathe machines, Milling machines, Welding equipment");
                venueRepository.save(centralWorkshop);

                // === BIOTECH (COS Building + Block A) - 5 venues ===
                if (biotech != null) {
                    Venue biotechGF = new Venue("Biotech Department Ground Floor", VenueType.MANAGED, 50);
                    biotechGF.setLocation("COS Building (ICMR)");
                    biotechGF.setSchool(biotech);
                    biotechGF.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    venueRepository.save(biotechGF);

                    String[] biotechRooms = {"COS101", "COS201"};
                    for (String room : biotechRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("COS Building (ICMR)");
                        v.setSchool(biotech);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }

                    String[] biotechLabs = {"Microbiology Lab", "Molecular Biology Lab"};
                    for (String lab : biotechLabs) {
                        Venue v = new Venue(lab, VenueType.MANAGED, 60);
                        v.setLocation("COS Building (ICMR)");
                        v.setSchool(biotech);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === PHYSICS (Block A) - 4 venues ===
                if (physics != null) {
                    String[] physicsRooms = {"A102", "A202"};
                    for (String room : physicsRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("Block A");
                        v.setSchool(physics);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }

                    String[] physicsLabs = {"Optics Lab", "Electronics Lab"};
                    for (String lab : physicsLabs) {
                        Venue v = new Venue(lab, VenueType.MANAGED, 60);
                        v.setLocation("Block A");
                        v.setSchool(physics);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === CIVIL (COALD Building) - 4 venues ===
                if (civil != null) {
                    String[] civilRooms = {"COALD102", "COALD202"};
                    for (String room : civilRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("COALD Building");
                        v.setSchool(civil);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }

                    String[] civilLabs = {"Surveying Lab", "Structural Analysis Lab"};
                    for (String lab : civilLabs) {
                        Venue v = new Venue(lab, VenueType.MANAGED, 60);
                        v.setLocation("COALD Building");
                        v.setSchool(civil);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === ARCHITECTURE (COALD Building) - 5 venues ===
                if (arch != null) {
                    Venue archGF = new Venue("Architecture Department Ground Floor", VenueType.MANAGED, 50);
                    archGF.setLocation("COALD Building");
                    archGF.setSchool(arch);
                    archGF.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    venueRepository.save(archGF);

                    String[] archRooms = {"COALD101", "COALD201"};
                    for (String room : archRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("COALD Building");
                        v.setSchool(arch);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === BUSINESS (COB Building) - 4 venues ===
                if (business != null) {
                    Venue businessGF = new Venue("Business School Ground Floor", VenueType.MANAGED, 50);
                    businessGF.setLocation("COB Building");
                    businessGF.setSchool(business);
                    businessGF.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    venueRepository.save(businessGF);

                    Venue businessConf = new Venue("Conference Room Business", VenueType.MANAGED, 50);
                    businessConf.setLocation("COB Building");
                    businessConf.setSchool(business);
                    businessConf.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    venueRepository.save(businessConf);

                    String[] businessRooms = {"COB101", "COB201"};
                    for (String room : businessRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("COB Building");
                        v.setSchool(business);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === ECONOMICS (Block C) - 2 venues ===
                if (econ != null) {
                    String[] econRooms = {"C106", "C206"};
                    for (String room : econRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("Block C");
                        v.setSchool(econ);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === MATHEMATICS (Block D) - 2 venues ===
                School math = schoolRepository.findByCode("MATH").orElse(null);
                if (math != null) {
                    String[] mathRooms = {"D108", "D208"};
                    for (String room : mathRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("Block D");
                        v.setSchool(math);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === LANGUAGES (COL Building) - 4 venues ===
                if (lang != null) {
                    Venue langGF = new Venue("Languages Department Ground Floor", VenueType.MANAGED, 50);
                    langGF.setLocation("COL Building");
                    langGF.setSchool(lang);
                    langGF.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    venueRepository.save(langGF);

                    Venue langConf = new Venue("Conference Room Languages", VenueType.MANAGED, 50);
                    langConf.setLocation("COL Building");
                    langConf.setSchool(lang);
                    langConf.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                    venueRepository.save(langConf);

                    String[] langRooms = {"COL101", "COL201"};
                    for (String room : langRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 60);
                        v.setLocation("COL Building");
                        v.setSchool(lang);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === PHILOSOPHY (COL Building) - 2 venues ===
                if (phil != null) {
                    String[] philRooms = {"COL102", "COL202"};
                    for (String room : philRooms) {
                        Venue v = new Venue(room, VenueType.MANAGED, 40);
                        v.setLocation("COL Building");
                        v.setSchool(phil);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        venueRepository.save(v);
                    }
                }

                // === DESIGN (COALD Building) - 2 venues ===
                if (design != null) {
                    String[] designStudios = {"COALD103", "COALD203"};
                    for (String studio : designStudios) {
                        Venue v = new Venue(studio, VenueType.MANAGED, 40);
                        v.setLocation("COALD Building");
                        v.setSchool(design);
                        v.setApprovalAuthority(VenueApprovalAuthority.SCHOOL);
                        v.setAmenities("Design Studio");
                        venueRepository.save(v);
                    }
                }

                // === SPORTS MANAGED (1) ===
                Venue sportsIndoor = new Venue("Sports Complex Indoor Hall", VenueType.MANAGED, 30);
                sportsIndoor.setLocation("Sports Complex");
                sportsIndoor.setApprovalAuthority(VenueApprovalAuthority.SPORTS_COORDINATOR);
                sportsIndoor.setAmenities("Indoor Hall");
                venueRepository.save(sportsIndoor);

                // === OPEN SPACES (13) ===
                String[] openSpaces = {
                        "Red Rocks Admin Side", "Red Rocks Library Side", "Coffee Day Matrika",
                        "Campus Bytes", "Grocery Area", "Flag Area", "Sports Complex",
                        "Basketball Court (Trikuta)", "Basketball Court (Shivalik)",
                        "Volleyball Court", "Badminton Court (Vindhyachal)", "Tennis Court", "BC Junction"
                };

                for (String space : openSpaces) {
                    Venue v = new Venue(space, VenueType.OPEN_SPACE, null);
                    v.setLocation("Campus");
                    venueRepository.save(v);
                }

                System.out.println("✅ Created " + venueRepository.count() + " venues");
            }

            // ==========================================
            // STEP 3: LOAD EVENT CATEGORIES (15)
            // ==========================================

            if (eventCategoryRepository.count() == 0) {
                System.out.println("\n🎨 Loading event categories...");

                String[][] categories = {
                        {"Technical", "Workshops, hackathons, seminars, coding competitions", "#2196F3", "💻"},
                        {"Sports", "Tournaments, matches, fitness events", "#4CAF50", "⚽"},
                        {"Cultural", "Music, dance, drama, art exhibitions", "#FF5722", "🎭"},
                        {"Workshop", "Skill development, training sessions", "#9C27B0", "🛠️"},
                        {"Seminar", "Guest lectures, talks, conferences", "#FF9800", "🎤"},
                        {"Competition", "Contests, challenges, championships", "#F44336", "🏆"},
                        {"Social", "Community service, awareness campaigns, meetups", "#00BCD4", "🤝"},
                        {"Academic", "Project presentations, research symposiums", "#673AB7", "📚"},
                        {"Training and Placement", "Career development, placement prep", "#795548", "💼"},
                        {"Fest", "College festivals, celebrations", "#E91E63", "🎉"},
                        {"Club Activity", "Regular club meetings and activities", "#009688", "🎪"},
                        {"Orientation", "Welcome events for new students", "#3F51B5", "👋"},
                        {"Alumni", "Alumni meetups and networking", "#607D8B", "🎓"},
                        {"Environmental", "Tree plantation, cleanliness drives", "#4CAF50", "🌱"},
                        {"Literary", "Book clubs, poetry, debates", "#FF6F00", "📖"}
                };

                for (String[] cat : categories) {
                    eventCategoryRepository.save(new EventCategory(cat[0], cat[1], cat[2], cat[3]));
                }

                System.out.println("✅ Created " + eventCategoryRepository.count() + " categories");
            }

            System.out.println("\n🎉 Data loading complete!");
        };
    }
}