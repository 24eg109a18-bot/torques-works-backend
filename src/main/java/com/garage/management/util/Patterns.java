package com.garage.management.util;

/** Regular expressions shared by the DTO validation annotations. */
public final class Patterns {
    private Patterns() {}

    public static final String PASSWORD = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$";
    public static final String PHONE = "^[6-9]\\d{9}$";
    /** Indian registration numbers such as TS09EB1234, KA-05-MN-9090, DL 1 C 1234 */
    public static final String REG_NO = "(?i)^[A-Z]{2}[ -]?\\d{1,2}[ -]?[A-Z]{0,3}[ -]?\\d{4}$";
}
