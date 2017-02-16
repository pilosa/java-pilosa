package com.pilosa.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * ORM for PQL queries.
 * <p>
 * Sample usage:
 * <pre>
 * Client client = // ... create the client
 * PilosaResponse = client.query("exampleDB",
 *                               Pql.union(Pql.bitmap(10, "foo"),
 *                                         Pql.bitmap(20, "foo")));
 * // process the response
 * </pre>
 *
 */
public final class Pql {
    Pql() {
    }

    /**
     * Creates a Bitmap query.
     *
     * @param id    bitmap ID
     * @param frame frame name
     * @return a PQL query
     */
    public static PqlBitmapQuery bitmap(int id, String frame) {
        return new PqlBitmapQuery(String.format("Bitmap(id=%d, frame=\"%s\")", id, frame));
    }

    /**
     * Creates a SetBit query
     *
     * @param id        bitmap ID
     * @param frame     frame name
     * @param profileID profile ID
     * @return a PQL query
     */
    public static PqlQuery setBit(int id, String frame, int profileID) {
        return new PqlQuery(String.format("SetBit(id=%d, frame=\"%s\", profileID=%d)", id, frame, profileID));
    }

    /**
     * Creates a ClearBit query
     *
     * @param id        bitmap ID
     * @param frame     frame name
     * @param profileID profile ID
     * @return a PQL query
     */
    public static PqlQuery clearBit(int id, String frame, int profileID) {
        return new PqlQuery(String.format("ClearBit(id=%d, frame=\"%s\", profileID=%d)", id, frame, profileID));
    }

    /**
     * Creates a Union query.
     *
     * @param bitmap1 first Bitmap
     * @param bitmap2 second Bitmap
     * @param bitmaps other Bitmaps
     * @return a PQL query
     */
    public static PqlBitmapQuery union(PqlBitmapQuery bitmap1, PqlBitmapQuery bitmap2, PqlBitmapQuery... bitmaps) {
        return bitmapOperation("Union", bitmap1, bitmap2, bitmaps);
    }

    /**
     * Creates a Union query for the same frame with different bitmap IDs.
     * <p>
     * Equivalent to <code>Union(Union(Bitmap(id1, frame), Bitmap(id2, frame)), Bitmap(id3, frame)) ...</code>
     *
     * @param frame frame name
     * @param id1   first bitmap ID
     * @param id2   second bitmap ID
     * @param ids   optional other bitmap IDs
     * @return a PQL query
     */
    public static PqlBitmapQuery union(String frame, int id1, int id2, int... ids) {
        PqlBitmapQuery r = Pql.union(Pql.bitmap(id1, frame), Pql.bitmap(id2, frame));
        for (int id : ids) {
            r = Pql.union(r, Pql.bitmap(id, frame));
        }
        return r;
    }

    /**
     * Creates an Intersect query.
     *
     * @param bitmap1 first Bitmap
     * @param bitmap2 second Bitmap
     * @param bitmaps other Bitmaps
     * @return a PQL query
     */
    public static PqlBitmapQuery intersect(PqlBitmapQuery bitmap1, PqlBitmapQuery bitmap2, PqlBitmapQuery... bitmaps) {
        return bitmapOperation("Intersect", bitmap1, bitmap2, bitmaps);
    }

    /**
     * Creates an Intersect query for the same frame with different bitmap IDs.
     * <p>
     * Equivalent to <code>Intersect(Intersect(Bitmap(id1, frame), Bitmap(id2, frame)), Bitmap(id3, frame)) ...</code>
     *
     * @param frame frame name
     * @param id1   first bitmap ID
     * @param id2   second bitmap ID
     * @param ids   optional other bitmap IDs
     * @return a PQL query
     */
    public static PqlBitmapQuery intersect(String frame, int id1, int id2, int... ids) {
        PqlBitmapQuery r = Pql.intersect(Pql.bitmap(id1, frame), Pql.bitmap(id2, frame));
        for (int id : ids) {
            r = Pql.intersect(r, Pql.bitmap(id, frame));
        }
        return r;
    }

    /**
     * Creates a Difference query.
     *
     * @param bitmap1 first Bitmap
     * @param bitmap2 second Bitmap
     * @param bitmaps other Bitmaps
     * @return a PQL query
     */
    public static PqlBitmapQuery difference(PqlBitmapQuery bitmap1, PqlBitmapQuery bitmap2, PqlBitmapQuery... bitmaps) {
        return bitmapOperation("Difference", bitmap1, bitmap2, bitmaps);
    }

    /**
     * Creates an Difference query for the same frame with different bitmap IDs.
     * <p>
     * Equivalent to <code>Difference(Difference(Bitmap(id1, frame), Bitmap(id2, frame)), Bitmap(id3, frame)) ...</code>
     *
     * @param frame frame name
     * @param id1   first bitmap ID
     * @param id2   second bitmap ID
     * @param ids   optional other bitmap IDs
     * @return a PQL query
     */
    public static PqlBitmapQuery difference(String frame, int id1, int id2, int... ids) {
        PqlBitmapQuery r = Pql.difference(Pql.bitmap(id1, frame), Pql.bitmap(id2, frame));
        for (int id : ids) {
            r = Pql.difference(r, Pql.bitmap(id, frame));
        }
        return r;
    }

    /**
     * Creates a Count query.
     *
     * @param bitmap the bitmap query
     * @return a PQL query
     */
    public static PqlQuery count(PqlBitmapQuery bitmap) {
        return new PqlQuery(String.format("Count(%s)", bitmap));
    }

    /**
     * Creates a TopN query.
     *
     * @param frame frame name
     * @param n     number of items to return
     * @return
     */
    public static PqlBitmapQuery topN(String frame, int n) {
        return new PqlBitmapQuery(String.format("TopN(frame=\"%s\", n=%d)", frame, n));
    }

    /**
     * Creates a TopN query.
     *
     * @param bitmap the bitmap query
     * @param frame  frame name
     * @param n      number of items to return
     * @return a PQL query
     */
    public static PqlBitmapQuery topN(PqlBitmapQuery bitmap, String frame, int n) {
        return new PqlBitmapQuery(String.format("TopN(%s, frame=\"%s\", n=%d)", bitmap, frame, n));
    }

    /**
     * Creates a TopN query.
     *
     * @param bitmap the bitmap query
     * @param frame  frame name
     * @param n      number of items to return
     * @param field  field name
     * @param values filter values to be matched against the field
     * @return a PQL query
     */
    public static PqlBitmapQuery topN(PqlBitmapQuery bitmap, String frame, int n, String field, Object... values) {
        try {
            String valuesString = mapper.writeValueAsString(values);
            return new PqlBitmapQuery(String.format("TopN(%s, frame=\"%s\", n=%d, field=\"%s\", %s)",
                    bitmap, frame, n, field, valuesString));
        } catch (JsonProcessingException ex) {
            throw new PilosaException("Error while converting values", ex);
        }
    }

    /**
     * Creates a Range query.
     *
     * @param id    bitmap ID
     * @param frame frame name
     * @param start start timestamp
     * @param end   end timestamp
     * @return a PQL query
     */
    public static PqlQuery range(int id, String frame, Date start, Date end) {
        DateFormat fmtDate = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat fmtTime = new SimpleDateFormat("HH:mm");
        return new PqlQuery(String.format("Range(id=%d, frame=\"%s\", start=\"%sT%s\", end=\"%sT%s\")",
                id, frame, fmtDate.format(start), fmtTime.format(start), fmtDate.format(end), fmtTime.format(end)));
    }

    /**
     * Creates a SetBitmapAttrs query.
     *
     * @param id         bitmap ID
     * @param frame      frame name
     * @param attributes bitmap attributes
     * @return a PQL query
     */
    public static PqlQuery setBitmapAttrs(int id, String frame, Map<String, Object> attributes) {
        String attributesString = createAttributesString(attributes);
        return new PqlQuery(String.format("SetBitmapAttrs(id=%d, frame=\"%s\", %s)",
                id, frame, attributesString));
    }

    /**
     * Creates a SetProfileAttrs query
     *
     * @param id         profile ID
     * @param attributes profile attributes
     * @return a PQL query
     */
    public static PqlQuery setProfileAttrs(int id, Map<String, Object> attributes) {
        String attributesString = createAttributesString(attributes);
        return new PqlQuery(String.format("SetProfileAttrs(id=%d, %s)",
                id, attributesString));
    }

    private static ObjectMapper mapper = new ObjectMapper();

    private static String createAttributesString(Map<String, Object> attributes) {
        try {
            List<String> kvs = new ArrayList<>(attributes.size());
            for (Map.Entry<String, Object> item : attributes.entrySet()) {
                kvs.add(String.format("%s=%s", item.getKey(), mapper.writeValueAsString(item.getValue())));
            }
            return StringUtils.join(kvs, ", ");
        } catch (JsonProcessingException ex) {
            throw new PilosaException("Error while converting values", ex);
        }
    }

    private static PqlBitmapQuery bitmapOperation(String name, PqlBitmapQuery bitmap1, PqlBitmapQuery bitmap2, PqlBitmapQuery... bitmaps) {
        String qry = String.format("%s, %s", bitmap1, bitmap2);
        if (bitmaps.length > 0) {
            StringBuilder builder = new StringBuilder(bitmaps.length);
            builder.append(qry);
            for (PqlBitmapQuery bitmap : bitmaps) {
                builder.append(", ");
                builder.append(bitmap);
            }
            qry = builder.toString();
        }
        return new PqlBitmapQuery(String.format("%s(%s)", name, qry));
    }
}