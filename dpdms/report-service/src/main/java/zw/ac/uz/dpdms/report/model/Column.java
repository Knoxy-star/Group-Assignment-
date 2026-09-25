package zw.ac.uz.dpdms.report.model;

/** One report column: the JSON field it reads and its heading. */
public record Column(String key, String label) {

    /** Special key: latitude and longitude combined into one "GPS" cell. */
    public static final String GPS = "_gps";
}
