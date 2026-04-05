package tableformatter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/*
 * Original:
 * - https://github.com/sgra64/ordering-system/blob/c3-datamodel/src/main/runnables/TableFormatter.java
 */
public class TableFormatter {
    private final List<Seg> segs;
    private final StringBuilder sb;

    private static final class Seg {
        final String label;
        final boolean isBorder;
        int width=0; int lm=0; int rm=0; char alignment='L';
        // 
        Seg(String label, boolean isBorder, List<Seg> segs) {
            this.width = label.length();
            var sp2 = label.replaceAll("^\\s+","");    // trim white spaces left
            if(sp2.length() > 0) {
                label = sp2; lm = width - label.length();
                sp2 = label.replaceAll("\\s+$","");    // trim white spaces right
                if(sp2.length() > 0) {
                    label = sp2; rm = width - lm - sp2.length();
                }
            }
            this.label = label;
            this.isBorder = isBorder;
        }
    }

    public static TableFormatterBuilder_2 builder() { return new TableFormatterBuilder_2(); }

    private TableFormatter(List<Seg> segs, StringBuilder sb) {
        this.segs = segs;
        this.sb = sb;
    }

    public final static class TableFormatterBuilder_2 {
        private final List<Seg> segs = new ArrayList<>();
        private final StringBuilder sb = new StringBuilder();

        public TableFormatterBuilder_2 columns(String spec) {
            for(int i=0; i < spec.length(); i++) {
                char c = spec.charAt(i);
                if(c=='|' || c=='-') {
                    if(sb.length() > 0) {
                        segs.add(new Seg(sb.toString(), false, segs));
                        sb.setLength(0);
                    }
                    segs.add(new Seg(c=='-'? "" : String.valueOf(c), true, segs));
                } else {
                    sb.append(c);
                }
            }
            if(sb.length() > 0) {
                segs.add(new Seg(sb.toString(), false, segs));
                sb.setLength(0);
            }
            return this;
        }

        public TableFormatterBuilder_2 widths(int... widths) {
            int j=0;
            for(var seg : segs) {
                if( ! seg.isBorder && j < widths.length) {
                    seg.width = widths[j++];
                }
            }
            return this;
        }

        public TableFormatterBuilder_2 alignments(String alignments) {
            int j=0;
            for(var seg : segs) {
                if( ! seg.isBorder && j < alignments.length()) {
                    seg.alignment = alignments.charAt(j++);
                }
            }
            return this;
        }

        public TableFormatter build() {
            return new TableFormatter(segs, sb);
        }
    }

    public TableFormatter row(String... cells) {
        return row(false, cells);
    }

    public TableFormatter line(String... cells) {
        return row(true, cells);
    }

    public void print(OutputStream os) {
        try {
            os.write(sb.toString().getBytes(Charset.forName("UTF-8")));
            sb.setLength(0);
        } catch (IOException e) { }
    }

    private TableFormatter row(boolean lineMode, String... cells) {
        if(cells.length==0) {
            // create row(" ", " ", " ")
            row(lineMode, segs.stream().filter(s -> ! s.isBorder).map(s -> " ").toArray(String[]::new));
        } else {
            int j=0; boolean e1=false; boolean e2=false;
            for(int i=0; i < segs.size(); i++) {
                var seg = segs.get(i);
                var cell = j < cells.length? cells[j] : "";
                e2=e1; e1=cell.length() > 0;
                if(seg.isBorder) {
                    String borderMarker = lineMode? "+" : "|";
                    cell(seg, e1 || e2? borderMarker : " ");
                } else {
                    String out = lineMode && cell.length() > 0? "{---}" : cell;
                    cell(seg, out);
                    j++;
                }
            }
            sb.append("\n");
        }
        return this;
    }

    private void cell(Seg seg, String s) {
        if(seg.isBorder) {
            sb.append(s);
        } else {
            int m = seg.alignment=='L'? seg.lm : seg.rm;
            if(s.matches("\\{---\\}")) {
                s = "-".repeat(seg.width);
                m = 0;  // reset cell margins
            } else {
                if(s.matches("\\{label\\}")) {
                    s = seg.label;
                }
            }
            int effw = seg.width - m;
            var pad = m > 0? " ".repeat(m) : "";
            if(seg.alignment=='L') {
                s = s.length() > effw? s.substring(0, effw) : s;
                var fmt = String.format("%s%ds", "%s%-", effw);
                sb.append(String.format(fmt, pad, s));   // "%s%-4s"
            } else {
                s = s.length() > effw? s.substring(s.length() - effw) : s;
                var fmt = String.format("%s%ds%s", "%", effw, "%s");
                sb.append(String.format(fmt, s, pad));
            }
        }
    }
}
