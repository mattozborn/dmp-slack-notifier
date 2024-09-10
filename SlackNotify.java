/* SlackNotify.java
 * Matt Ozborn
 *
 * This is a program written in java so as to not have to install anything unapproved on company machines. 
 * This program takes a Slack webhook url, pixel coordinates of a screen, and a message to send to Slack as
 * user input. It detects changes in the screen by periodically capturing new screenshots and comparing them.
 * If a change is detected, it sends the desired message to Slack as a POST request with a JSON payload.
 */

import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.AWTException;
import java.io.OutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
import javax.imageio.ImageIO;


public class SlackNotify {

    // Global variable to store webhook URL
    private static String slackWebhookUrl;

    // Global variable to store the desired message to send to Slack
    private static String slackNotification;

    // Global variables to store coordinates for ROI
    private static int topLeftX, topLeftY, bottomRightX, bottomRightY;

    // Global variable to store the first screenshot
    private static BufferedImage firstScreenshot = null;
    

    public static void main(String[] args) {
        // Create a scanner object to collect user-defined input
        Scanner scanner = new Scanner(System.in);
        
        // Prompt the user for the webhook URL
        slackWebhookUrl = promptString("Enter the slack webhook URL: ");

        // Prompt the user for the message to send to Slack
        slackNotification = promptString("Enter the message you would like to send to Slack: ");

        // Prompt the user for the top left and bottom right x/y coordinates
        topLeftX = promptInteger("Enter the X-coordinate of the top-left corner of the region of interest: ");
        topLeftY = promptInteger("Enter the Y-coordinate of the top-left corner of the region of interest: ");
        bottomRightX = promptInteger("Enter the X-coordinate of the bottom-right corner of the region of interest: ");
        bottomRightY = promptInteger("Enter the Y-coordinate of the bottom-right corner of the region of interest: ");
        
        try {
            // Call the method to start detecting screen changes in the ROI
            detectScreenChanges();
        } catch (AWTException e) {
            System.out.println("Exiting the program. AWTException passed back to main() from detectScreenChanges():");
            e.printStackTrace();
            System.exit(0);  // Gracefully exit the application after printing stack to console
        } catch (InterruptedException e) {
            System.out.println("Exiting the program. InterruptedException passed back to main() from detectScreenChanges():");
            e.printStackTrace();
            System.exit(0);  // Gracefully exit the application after printing stack to console
        }
    }
    

    private static void saveImage(BufferedImage image, String filename) {
        try {
            // Write the image to a file
            ImageIO.write(image, "png", new File(filename));
            System.out.println("Image saved successfully: " + filename);
        } catch (IOException e) {
            System.out.println("Failed to save image: " + e.getMessage());
            return;  // Gracefully return out of the saveImage method and continue to capture the ROI
        }
    }


    private static void sendSlackNotification(String message) {
        // Debug: Let the console know that we are sending a notification
        System.out.println("Sending Slack notification...");

        // Create the payload as a JSON string with the provided message
        String payload = "{\"text\": \"" + message + "\"}";
        
        try {    
            // Create a URL object with the Slack webhook URL
            URL url = new URL(slackWebhookUrl);

            // Open a connection to the Slack webhook URL
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");

            // Send the payload to the Slack webhook
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(payload.getBytes(StandardCharsets.UTF_8));
            outputStream.flush();

            // Get the response code from the Slack webhook
            int responseCode = connection.getResponseCode();

            // Check the response code and print a message to the console
            if (responseCode == 200) {
                System.out.println("Slack notification sent successfully.");
            } else {
                System.out.println("Failed to send Slack notification. Response code: " + responseCode);
            }

        } catch (IOException e) {
            System.out.println("Failed to send Slack notification: " + e.getMessage());
            return;  // Gracefully return out of the sendSlackNotification method and continue to capture the ROI
        }
    }
    

    private static void detectScreenChanges() throws AWTException, InterruptedException {
        // Debug: Let the console know that we are now detecting changes in the ROI
        System.out.println("Detecting changes in the ROI...");

        // Create a rectangle representing the ROI
        Rectangle roi = new Rectangle(topLeftX, topLeftY, bottomRightX - topLeftX, bottomRightY - topLeftY);

        // Create a robot to capture the screen
        Robot robot = new Robot();

        // Get the first screenshot
        BufferedImage lastScreenshot = robot.createScreenCapture(roi);

        // Set the first screenshot if we have never done so
        if (firstScreenshot == null) {
            firstScreenshot = lastScreenshot;

            // Debug: Save the first image captured to file so we are able to view
            saveImage(firstScreenshot, "first-screenshot.png");
        }

        while (true) {
            // Wait for a short time (1 second)
            Thread.sleep(1000);

            // Take a new screenshot
            BufferedImage currentScreenshot = robot.createScreenCapture(roi);

            // Compare the new screenshot with the previous one and the initial screenshot
            if (!areEqual(currentScreenshot, lastScreenshot) && !areEqual(currentScreenshot, firstScreenshot)) {
                System.out.println(slackNotification);
                
                // Debug: Save the new image captured to file so we are able to view
                saveImage(currentScreenshot, "current-screenshot.png");

                // Pass the message to the method that sends it to Slack
                sendSlackNotification(slackNotification);

                // Update the previous screenshot with the new one
                lastScreenshot = currentScreenshot;
            }
        }
    }
    

    private static boolean areEqual(BufferedImage image1, BufferedImage image2) {
        // Debug: Let the console know we are comparing screen captures
        //System.out.println("Checking if images are equal...");

        // Iterate over each pixel of the images and compare them
        for (int x = 0; x < image1.getWidth(); x++) {
            for (int y = 0; y < image1.getHeight(); y++) {
                // Get the RGB values of the corresponding pixels in each image
                int pixel1 = image1.getRGB(x, y);
                int pixel2 = image2.getRGB(x, y);

                // Compare the RGB values of the pixels from each image
                if (pixel1 != pixel2) {
                    return false;  // Pixels are different, images are not equal
                }
            }
        }
        return true;  // All pixels are the same, images are equal
    }
    

    private static String promptString(String prompt) {
        // Create a Scanner object to collect user-defined input
        Scanner scanner = new Scanner(System.in);

        // Print the input prompt passed to this method
        System.out.println(prompt);

        // Collect the input and store it to be returned
        String value = scanner.nextLine();
        //scanner.nextLine();  // Discard the newline character left in the input buffer

        // Return the value back to the main method
        return value;
    }


    private static int promptInteger(String prompt) {
        // Create a Scanner object to collect user-defined input
        Scanner scanner = new Scanner(System.in);

        // Print the input prompt passed to this method
        System.out.println(prompt);

        // Loop until we get a valid integer value as input
        while (!scanner.hasNextInt()) {
            System.out.print("Invalid input. Please enter an integer: ");
            scanner.nextLine();  // Discard the invalid input
        }
        
        // Collect the input and store it to be returned
        int value = scanner.nextInt();
        scanner.nextLine();  // Discard the newline character left in the input buffer

        // Return the value back to the main method
        return value;
    }

}
