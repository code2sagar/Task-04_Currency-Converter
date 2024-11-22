import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

class CurrencyConverter {

    private static final String API_URL = "https://api.exchangerate-api.com/v4/latest/";

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Currency selection
        System.out.println("Available currencies: USD, EUR, GBP, JPY, AUD, CAD");
        System.out.print("Enter base currency: ");
        String baseCurrency = scanner.nextLine().toUpperCase();
        System.out.print("Enter target currency: ");
        String targetCurrency = scanner.nextLine().toUpperCase();

        // Amount input
        System.out.print("Enter amount to convert: ");
        double amount = scanner.nextDouble();

        try {
            // Fetch exchange rates
            Map<String, Double> rates = fetchExchangeRates(baseCurrency);
            if (rates != null && rates.containsKey(targetCurrency)) {
                double exchangeRate = rates.get(targetCurrency);
                double convertedAmount = amount * exchangeRate;

                // Display result
                System.out.printf("%.2f %s = %.2f %s%n", amount, baseCurrency, convertedAmount, targetCurrency);
            } else {
                System.out.println("Currency not found or invalid.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred while fetching exchange rates.");
        } finally {
            scanner.close();
        }
    }

    private static Map<String, Double> fetchExchangeRates(String baseCurrency) throws Exception {
        URL url = new URL(API_URL + baseCurrency);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Failed to connect: HTTP error code " + conn.getResponseCode());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            response.append(line);
        }
        br.close();

        // Parse the response to extract exchange rates
        return parseExchangeRates(response.toString());
    }

    private static Map<String, Double> parseExchangeRates(String response) {
        Map<String, Double> rates = new HashMap<>();

        // Simple parsing for the response
        // Example response format: {"base": "USD", "rates": {"EUR": 0.85, "GBP": 0.75, ...}}
        String ratesPart = response.split("\"rates\":")[1].split("}")[0] + "}";

        // Remove surrounding curly braces and split by commas
        String[] entries = ratesPart.replaceAll("[{}]", "").split(",");

        for (String entry : entries) {
            String[] keyValue = entry.split(":");
            if (keyValue.length == 2) {
                String currency = keyValue[0].replaceAll("\"", "").trim(); // Clean currency code
                double rate = Double.parseDouble(keyValue[1].trim());
                rates.put(currency, rate);
            }
        }
        return rates;
    }
}