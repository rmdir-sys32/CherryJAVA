import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RespParser {

    // public static void main(String[] args) throws Exception {
    // String resp = "*2\r\n" + "$3\r\nGET\r\n" + "$3\r\nkey\r\n";
    // System.out.println(resp);
    // RespReader reader =
    // new RespReader(new ByteArrayInputStream(resp.getBytes(StandardCharsets.UTF_8)));

    // RespValue value = reader.readValue();

    // System.out.println("Type: " + value.type);


    // @SuppressWarnings("unchecked")
    // List<RespValue> list = (List<RespValue>) value.value;

    // for (RespValue v : list) {
    // System.out.println(v.type + "->" + v.value);
    // }


    // // Testing serialisation
    // RespValue serial = new RespValue(Type.BULK_STRING, "hello");

    // byte[] b = serial.serialize();

    // System.out.println(new String(b, StandardCharsets.UTF_8));

    // }


    // Wrapping the input stream class in buffered stream to avoid making slow individual read
    // system calls
    public static class RespReader {
        private final BufferedInputStream in;

        public RespReader(InputStream in) {
            this.in = new BufferedInputStream(in);
        }

        // Master Read function to Read Value and call respective read method
        public RespValue readValue() throws IOException {
            int prefix = in.read();
            // System.out.println("Prefix : " + prefix);
            if (prefix == -1) {
                throw new EOFException("Connection closed by client");
            }

            switch (prefix) {
                case '+':
                    return new RespValue(Type.SIMPLE_STRING, readLine());
                case '-':
                    return new RespValue(Type.ERROR, readLine());
                case ':':
                    return new RespValue(Type.INTEGER, Long.parseLong(readLine()));
                case '$':
                    return readBulkString();
                case '*':
                    return readArray();
                default:
                    throw new IOException("Invalid RESP prefix '" + (char) prefix + "'");
            }
        }

        // Read Simple String e.g. "+OK\r\n" , + has already been read
        public String readLine() throws IOException {
            StringBuilder sb = new StringBuilder();
            // we are reading UTF-8 encoding when reading the data not characters or string hence
            // every read wil give an interger which will be type casted into a character
            int c;
            while ((c = in.read()) != -1) {
                if (c == '\r') {
                    int next = in.read();
                    if (next == '\n') {
                        break; // End of line reached
                    }

                    // Handle malformed line endings by treating a standalone '\r' as a literal
                    // character and appending the following byte if the stream hasn't ended.
                    sb.append('\r');
                    if (next != -1) {
                        sb.append((char) next);
                    }
                } else {
                    sb.append((char) c);
                }
            }
            return sb.toString();
        }


        // Read bulk String e.g "$5\r\nhello\r\n" , $ has already been read
        public RespValue readBulkString() throws IOException {
            String lenStr = readLine();
            int length = Integer.parseInt(lenStr);

            if (length == -1)
                return new RespValue(Type.BULK_STRING, null);

            byte[] bytes = new byte[length];

            int read = 0; // to prevent broken value e.g. $5\r\n\ hel \r\n
            while (read < length) {
                // int read(byte[] b, int off, int len)
                int currentRead = in.read(bytes, read, length - read);
                if (currentRead == -1) {
                    throw new EOFException(
                            "Premature end of stream while reading bulk string content");
                }
                read += currentRead;
            }

            // Consume CLRF
            int r = in.read();
            int n = in.read();

            if (r != '\r' || n != '\n') {
                throw new IOException("Excepted CLRF at the end of bulk string");
            }
            return new RespValue(Type.BULK_STRING, new String(bytes, StandardCharsets.UTF_8));
        }


        // Read Array e.g. "*2\r\n$3\r\nGET\r\n$3\r\nkey\r\n" , * has already been read
        public RespValue readArray() throws IOException {
            String lenStr = readLine();
            int size = Integer.parseInt(lenStr);
            if (size == -1) {
                return new RespValue(Type.ARRAY, null);
            }

            List<RespValue> elements = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                elements.add(readValue());
            }

            return new RespValue(Type.ARRAY, elements);
        }


    }
}


enum Type {
    SIMPLE_STRING, ERROR, INTEGER, BULK_STRING, ARRAY
}


/**
 * Respresents any RESP value
 */

class RespValue {
    public final Type type;
    public final Object value; // Can be string, long, or List<RespValue>

    public RespValue(Type type, Object value) {
        this.type = type;
        this.value = value;
    }

    // Serializes the RespValue object into standard RESP byte format;
    public byte[] serialize() {
        switch (type) {
            case SIMPLE_STRING:
                return ("+" + value + "\r\n").getBytes(StandardCharsets.UTF_8);
            case ERROR:
                return ("-" + value + "\r\n").getBytes(StandardCharsets.UTF_8);
            case INTEGER:
                return (":" + value + "\r\n").getBytes(StandardCharsets.UTF_8);
            case BULK_STRING:
                if (value == null) {
                    return "$-1\r\n".getBytes(StandardCharsets.UTF_8);
                }
                String str = (String) value;
                return ("$" + str.length() + "\r\n" + str + "\r\n")
                        .getBytes(StandardCharsets.UTF_8);
            case ARRAY:
                if (value == null) {
                    return "$-1\r\n".getBytes(StandardCharsets.UTF_8);
                }

                @SuppressWarnings("unchecked")
                List<RespValue> list = (List<RespValue>) value;
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                try {
                    bos.write(("*" + list.size() + "\r\n").getBytes(StandardCharsets.UTF_8));

                    for (RespValue val : list) {
                        bos.write(val.serialize());
                    }
                } catch (IOException ignored) {
                }
                return bos.toByteArray();
            default:
                throw new IllegalArgumentException("Unknown RESP type");
        }

    }


}
