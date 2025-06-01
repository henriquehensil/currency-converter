package dev.hensil;

import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.Scanner;

public final class Main {

    private static final Collection<String> CODES;
    private static final Scanner SCANNER = new Scanner(System.in);

    static {
        SCANNER.useLocale(Locale.US);

        try {
            CODES = Conversor.getInstance().getCodes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) throws IOException {
        System.out.println("!======== Country codes ========!");
        for (String codes : CODES) {
            System.out.println(codes);
        }

        System.out.print("\n\nChoose: ");
        String code = SCANNER.next().toUpperCase();

        while (!CODES.contains(code)) {
            System.err.println("Unsupported code!");
            System.out.print("Choose: ");
            code = SCANNER.next().toUpperCase();
        }

        System.out.print("Amount: ");
        double amount = SCANNER.nextDouble();

        while (amount <= 0) {
            System.err.println("Amount must to be greater than 0");
            System.out.print("Amount: ");
            amount = SCANNER.nextDouble();
        }

        System.out.print("Target: ");
        String target = SCANNER.next().toUpperCase();

        while (!CODES.contains(target) || target.equalsIgnoreCase(code)) {
            System.err.println("Unsupported code!");
            System.out.print("Choose: ");
            target = SCANNER.next().toUpperCase();
        }

        double result = Conversor.getInstance().convert(code, amount, target);
        System.out.println("\n\n" + amount + " " + code + " = " + result + " " + target);
    }
}