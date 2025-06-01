package dev.hensil;

import com.google.gson.*;

import java.io.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.nio.file.Path;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class Conversor {

    // Static initializers

    private static final String KEY;
    private static final URI STANDARD_URI = java.net.URI.create("https://v6.exchangerate-api.com/v6/");

    private static final Conversor INSTANCE = new Conversor(HttpClient.newHttpClient(), new HashSet<>());

    static {
        // Load key
        try (FileReader reader = new FileReader(Path.of("").toAbsolutePath().resolve("key.txt").toFile())) {
            char [] chars = new char[24];
            if (reader.read(chars) == -1) {
                throw new IOException("Empty key file");
            }

            KEY = new String(chars);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Conversor getInstance() {
        return INSTANCE;
    }

    // Objects

    private final HttpClient client;
    private final Collection<String> countries;

    private Conversor(HttpClient client, Set<String> countries) {
        this.client = client;
        this.countries = countries;
    }

    public Collection<String> getCodes() throws IOException {
        if (isAlreadyRequested()) return countries;

        // Create request
        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .uri(getCodesUri())
                .build();

        try {
            // Send request
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse body to Json
            JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();

            // If an error occurs
            if (object.get("result").getAsString().equalsIgnoreCase("error")) {
                throw new RuntimeException("Cannot get codes: " + object.get("error_type"));
            }

            JsonArray array = object.getAsJsonArray("supported_codes");

            for (JsonElement element : array) {
                if (element.isJsonArray()) {
                    // Add code
                    countries.add(element.getAsJsonArray().get(0).getAsString());
                }
            }

            return countries;
        } catch (InterruptedException e) {
            throw new RuntimeException("Cannot get the country codes", e);
        }
    }

    public double convert(String baseCode, double amount, String targetCode) throws IOException {
        if (amount <= 0) {
            throw new RuntimeException("Illegal amount value: " + amount);
        } else if (isAlreadyRequested() && (!countries.contains(baseCode) || !countries.contains(targetCode))) {
            throw new RuntimeException("Unsupported codes: " + baseCode + "or " + targetCode);
        } else try {
            HttpRequest request = HttpRequest.newBuilder()
                    .GET()
                    .version(HttpClient.Version.HTTP_1_1)
                    .uri(getPairCodeUri(baseCode, targetCode, amount))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // Parse body to Json
            JsonObject object = JsonParser.parseString(response.body()).getAsJsonObject();

            // If an error occurs
            if (object.get("result").getAsString().equalsIgnoreCase("error")) {
                throw new RuntimeException("Cannot get codes: " + object.get("error-type").getAsString());
            }

            return object.get("conversion_result").getAsDouble();
        } catch (InterruptedException e) {
            throw new RuntimeException("Cannot get the country codes", e);
        }
    }

    private URI getCodesUri() {
        return STANDARD_URI.resolve(KEY + "/codes");
    }

    private URI getPairCodeUri(String base, String target, double amount) {
        return STANDARD_URI.resolve(KEY + "/pair/" + base + "/" + target + "/" + amount);
    }

    private boolean isAlreadyRequested() {
        return !countries.isEmpty();
    }
}