package com.financetracker;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FinanceTrackerApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(FinanceTrackerApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        boolean launchDesktop = false;
        for (String arg : args) {
            if ("--desktop".equalsIgnoreCase(arg) || "--desktop=true".equalsIgnoreCase(arg)) {
                launchDesktop = true;
                break;
            }
        }
        if (launchDesktop) {
            launchSwingApp();
        }
    }

    private void launchSwingApp() {
        // Resolve the Swing desktop output directory relative to this backend location.
        // Works in both dev (mvn spring-boot:run from backend/) and from a fat JAR
        // placed in the project root alongside the build.bat output directory.
        Path[] candidates = {
                Paths.get("../out"),             // running from backend/
                Paths.get("./out"),              // running from project root
                Paths.get("out"),
        };

        String javaExe = ProcessHandle.current().info().command().orElse("java");

        for (Path candidate : candidates) {
            File mainClass = candidate.resolve("com/financetracker/Main.class").toFile();
            if (mainClass.exists()) {
                try {
                    String cp = candidate.toAbsolutePath().toString();
                    ProcessBuilder pb = new ProcessBuilder(javaExe, "-cp", cp, "com.financetracker.Main");
                    pb.inheritIO();
                    pb.start();
                    System.out.println("[desktop] Swing GUI launched from: " + cp);
                    return;
                } catch (Exception e) {
                    System.err.println("[desktop] Failed to launch Swing GUI: " + e.getMessage());
                }
            }
        }
        System.err.println("[desktop] Could not find Swing classes. Run build.bat first.");
    }
}
