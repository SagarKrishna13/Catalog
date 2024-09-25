import org.json.JSONObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Scanner;

public class ShamirSecretSharing {

    // Function to decode the Y value based on its base
    public static BigInteger decodeValue(String base, String value) {
        int baseInt = Integer.parseInt(base);  // Parse base to an integer
        return new BigInteger(value, baseInt); // Convert value to BigInteger based on the base
    }

    // Function to perform Lagrange interpolation
    public static BigDecimal lagrangeInterpolation(int[] x, BigDecimal[] y, int k) {
        BigDecimal secret = BigDecimal.ZERO;

        for (int i = 0; i < k; i++) {
            BigDecimal term = y[i];
            for (int j = 0; j < k; j++) {
                if (i != j) {
                    BigDecimal numerator = BigDecimal.valueOf(-x[j]);
                    BigDecimal denominator = BigDecimal.valueOf(x[i] - x[j]);
                    term = term.multiply(numerator).divide(denominator, BigDecimal.ROUND_HALF_UP);
                }
            }
            secret = secret.add(term);
        }

        return secret.setScale(2, BigDecimal.ROUND_HALF_UP);  // Return result with 2 decimal precision
    }

    public static void main(String[] args) {
        Scanner inputScanner = new Scanner(System.in);
        System.out.print("Enter the path to the JSON file: ");
        String filePath = inputScanner.nextLine();

        try {
            // Read the JSON file
            File file = new File(filePath);
            Scanner fileScanner = new Scanner(file);
            StringBuilder jsonString = new StringBuilder();
            while (fileScanner.hasNextLine()) {
                jsonString.append(fileScanner.nextLine());
            }
            fileScanner.close();

            // Parse the JSON data
            JSONObject jsonObject = new JSONObject(jsonString.toString());
            JSONObject keys = jsonObject.getJSONObject("keys");
            int n = keys.getInt("n"); // Number of points
            int k = keys.getInt("k"); // Minimum points required

            // Check if the points in the JSON match the provided 'n'
            if (jsonObject.length() - 1 != n) {
                throw new IllegalArgumentException("Expected " + n + " points, but found " + (jsonObject.length() - 1));
            }

            // Arrays to store x and y values
            int[] xValues = new int[k];
            BigDecimal[] yValues = new BigDecimal[k];

            // Extract x and y values from the JSON object and decode them
            int index = 0;
            for (String key : jsonObject.keySet()) {
                if (!key.equals("keys") && index < k) {
                    int x = Integer.parseInt(key);  // x value is the key
                    JSONObject point = jsonObject.getJSONObject(key);
                    String base = point.getString("base");
                    String value = point.getString("value");

                    // Decode y value from the specified base
                    BigInteger decodedY = decodeValue(base, value);
                    xValues[index] = x;
                    yValues[index] = new BigDecimal(decodedY);
                    index++;
                }
            }

            // Calculate the constant term (secret) using Lagrange Interpolation
            BigDecimal secret = lagrangeInterpolation(xValues, yValues, k);

            // Output the secret (constant term c)
            System.out.println("The constant term (secret) is: " + secret);

        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
