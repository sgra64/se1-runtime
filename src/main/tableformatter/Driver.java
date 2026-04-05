package tableformatter;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;


public class Driver {

    // public static void main(String[] args) {
    //     var application = new Driver();
    //     application.run(args);
    // }

    public void run(String[] args) {
        var sb = new StringBuilder();
        TableFormatterBuilder builder = TableFormatter.builder();
        TableFormatter tf = builder
            .columns("| TEXTFIELD {*}ID{*} M |")
            .widths(20, 10, 10)
            // .alignments('L', 'R', 'R')
            .build(sb);
        // 
        tf
            // .line("", "-", "-")
            // .row("id", "mouse", "cash")
            // .row()
            // .line("=", " ", "")         //, "{---}")
            // .line("{<}={>}", "-", "{ }-")   // --> '<====================>'
            .row("{<}{label}{>}", "G{|}", " ")
            // .row("jkl", "mno", "pqr")
            
            // .row("{ }{xlabel}{ }", "{ }label", "[label]")
            // .row("label", "{|}sss{label}sss{|}", "label")
            // .line()
            // .line("-", "{-}-{-}", "-")
            // .row("ABC", "DE{:}", "F")
            // .line("{ }-{ }", "", "{<}={>}")
            // .line()
            .print(System.out);
        ;
    }


    public static class TableFormatter {
        private final List<Cell> cells;
        private final StringBuilder sb;
        private final StringBuilder splb = new StringBuilder();

        private TableFormatter(StringBuilder sb, List<Cell> cells) {
            this.sb = sb;
            this.cells = cells;
        }

        public static TableFormatterBuilder builder() {
            return new TableFormatterBuilder();
        }

        public TableFormatter line(String... markers) { return writeRow(true, markers); }

        public TableFormatter row(String... markers) { return writeRow(false, markers); }

        public void print(OutputStream os) {
            try {
                os.write(sb.toString().getBytes(Charset.forName("UTF-8")));
                sb.setLength(0);
            } catch (IOException e) { }
        }

        private TableFormatter writeRow(boolean lineMode, String... markers) {
            // initialize empty markers: line("-", "-", "-"); row(" ", " ", " ");
            markers = markers.length > 0? markers :
                cells.stream().filter(c -> ! c.isBorder()).map(c -> lineMode? "-" : " ").toArray(String[]::new);
            // 
            // 2-stage queues for borders (L,R) and fill char
            char bc, bc1, fc, fc1; bc = bc1 = fc = fc1 = 0;
            String text, text1; text = text1 = "";
            int j=0;
            for(var cell : cells) {
                if(j==0 || ! cell.isBorder()) {
                    String marker = j < markers.length? markers[j++] : "";
                    var spl = splitMarker(marker, new String[] {"", null, null, null});
                    char cx0 = spl[1]==null? 0 : (spl[1].length() > 0? spl[1].charAt(0) : ' ');
                    char cx1 = spl[2]==null? 0 : (spl[2].length() > 0? spl[2].charAt(0) : ' ');
                    bc = cx0==0? bc1 : cx0;
                    bc1 = cx1==0? 0 : cx1;
                    fc = fc1; fc1 = spl[0].length() > 0? spl[0].charAt(0) : ' ';
                    text = text1; text1 = spl[0];
                    // System.out.println(String.format("(%s)(%s)(%s)(%s)", spl[0], spl[1], spl[2], spl[3]));
                }
                if(cell.isBorder()) {
                    boolean emptySegment = lineMode?
                        ((fc==0 || fc==' ') && fc1==' ') : (text.equals("") && text1.equals(""));
                    // 
                    char fillChar = emptySegment? ' ' : (lineMode? '+' : cell.borderMarker());
                    fillChar = bc != 0? bc : fillChar;
                    cell.write(sb, true, fillChar, "nop", cell.align());
                } else {
                    char fillChar = lineMode? fc : ' ';
                    text = text.replaceAll("\\{label\\}", cell.label());
                    cell.write(sb, lineMode, fillChar, text, cell.align());
                }
            }
            sb.append("\n"); return this;
        }

        private String[] splitMarker(String str, String[] segs) {
            int j=0; splb.setLength(0);
            for(int ai=0; ai < str.length(); ai++) {
                char ch = str.charAt(ai);
                switch(ch) {
                    case '{': j++; segs[0] += splb.toString();
                    case '}': var s=splb.toString();
                        switch(s) {
                        case "label": segs[0] += "{label}"; segs[j--] = null; break;
                        // 
                        default: if(j < segs.length) segs[j]=s;
                        }
                        splb.setLength(0);
                        break;
                    // 
                    default: if(j==0) j++; splb.append(ch);
                }
            }
            segs[0] += splb.toString(); splb.setLength(0);
            return segs;
        }
    }

    public final static class TableFormatterBuilder {
        private final List<Cell> cells = new ArrayList<>();

        private TableFormatterBuilder() { }

        public TableFormatterBuilder columns(String def) {
            Arrays.stream(def  //"|  TEXTFIELD{*} ID | M {|}"
                .replaceAll("\\{\\|\\}", "|")
                .replaceAll("\\|", "@||@")
                .replaceAll("\\{(?<first>.?)\\}", "@|${first}@")
                .split("@")
            )
            .filter(s -> s.length() > 0)
            // .peek(System.out::println)
            .map(s -> {
                boolean border = s.charAt(0)=='|';
                int w = s.length();
                w -= border? 1 : 0; // deduct leading '|' for borders marker
                String label = s.replaceAll("^\\s+", "");
                int padL = border? 0 : w - label.length();
                label = label.replaceAll("\\s+$", "");
                int padR = border? 0 : w - padL - label.length();
                char borderMarker = border? s.charAt(1) : '|';
                // System.out.println(String.format("w=%d, padL=%d, padR=%d", w, padL, padR));
                return new Cell(label, w, padL, padR, 'L', border, borderMarker);
            }).collect(Collectors.toCollection(() -> cells));
            return this;
        }

        public TableFormatterBuilder widths(Integer... widths) {
            return configure((cell, w) -> cell.w(w), widths);
        }

        public TableFormatterBuilder alignments(Character... alignments) {
            return configure((cell, a) -> cell.align(a), alignments);
        }

        public TableFormatter build(StringBuilder sb) {
            return new TableFormatter(sb, cells);
        }

        private <T> TableFormatterBuilder configure(BiConsumer<Cell, T> configurer, T[] items) {
            int j=0;
            for(var cell : cells) {
                if( ! cell.isBorder() && j < items.length) {
                    configurer.accept(cell, items[j++]);
                }
            }
            return this;
        }
    }

    private static class Cell {
        String label; int w; int padL; int padR; char align; boolean isBorder; char borderMarker;
        // 
        private Cell(String label, int w, int padL, int padR, char align, boolean isBorder, char borderMarker) {
            this.label=label; this.w=w; this.padL=padL; this.padR=padR; this.align=align;
            this.isBorder=isBorder; this.borderMarker=borderMarker;
        }
        char align() { return align; }
        boolean isBorder() { return isBorder; }
        char borderMarker() { return borderMarker; }
        String label() { return label; }
        void w(int w) { this.w=w; }
        void align(char align) { this.align=align; }

        private void write(StringBuilder sb, boolean fillMode, char fillChar, String text, char align) {
            if(w > 0) {
                if(fillMode) {
                    sb.append(String.valueOf(fillChar).repeat(w));
                } else if(text != null) {
                    final int tlen = text.length();
                    final int effw = Math.max(0, w - padL - padR);
                    final String filler = String.valueOf(fillChar);
                    // 
                    final int pL = Math.min(w - padR, padL);
                    if(pL > 0) sb.append(filler.repeat(pL));
                    // 
                    if(align=='L') {
                        sb.append(tlen > effw? text.substring(0, effw) :
                            tlen < effw? String.format("%s%s", text, filler.repeat(effw - tlen)) : text);
                    } else {
                        sb.append(tlen > effw? text.substring(tlen - effw) :
                            tlen < effw? String.format("%s%s", filler.repeat(effw - text.length()), text) : text);
                    }
                    final int pR = Math.min(w, Math.min(w - pL, padR));
                    if(pR > 0) sb.append(filler.repeat(pR));
                }
            }
        }
    }

        // private TableFormatter writeRow(boolean lineMode, String... markers) {
        //     // create all-line marker ("-", "-", "-"...) line if no markers are given
        //     if(markers.length==0 && defaultLineMarker==null) {
        //         defaultLineMarker = cells.stream().filter(c -> ! c.isBorder()).map(c -> "-").toArray(String[]::new);
        //     }
        //     markers = markers.length==0? defaultLineMarker : markers;
        //     final int mlen = markers.length;
        //     // 
        //     var spl=splitMarker(null, markers[0], new String[] {"", "", ""});
        //     System.out.println(String.format("(%s)(%s)(%s)", spl[0], spl[1], spl[2]));
        //     String m=spl[0];
        //     char egL=spl[1].length() > 0? spl[1].charAt(0) : (lineMode? '+' : cells.get(0).borderMarker());
        //     char egR=spl[2].length() > 0? spl[2].charAt(0) : (lineMode? '+' : cells.get(0).borderMarker());
        //     boolean hasBorder=m.length() > 0, hadBorder=false;
        //     int j=1;
        //     for(var cell : cells) {
        //         if(cell.isBorder()) {   // write border markers: '+' or '|'
        //             // cell.write(true, hasBorder || hadBorder? (egL==0? (lineMode? '+' : cell.borderMarker()) : egL) : whiteSpace, null, cell.align());
        //             cell.write(sb, true, hasBorder || hadBorder? (egL==0? (lineMode? '+' : cell.borderMarker()) : egL) : whiteSpace, null, cell.align());
        //             // System.out.println("egL: " + cell.borderMarker());
        //             egL = egR;
        //         } else {
        //             if(lineMode) {      // fill line segment: '------'
        //                 cell.write(sb, true, hasBorder? m.charAt(0) : whiteSpace, null, cell.align());
        //             } else {
        //                 m = m.replaceAll("\\[label\\]", cell.label());
        //                 // m = m.replaceAll("\\{label\\}", cell.label());
        //                 cell.write(sb, false, whiteSpace, m, cell.align());
        //             }
        //             if(j < mlen) {
        //                 spl=splitMarker(cell, markers[j++], new String[] {"", "", ""});
        //                 // System.out.println(String.format("(%s)(%s)(%s)", spl[0], spl[1], spl[2]));
        //                 m=spl[0];
        //                 // egL=spl[1].length() > 0? spl[1].charAt(0) : egL;
        //                 // egR=spl[2].length() > 0? spl[2].charAt(0) : (lineMode? '+' : cell.borderMarker());
        //                 egL=spl[1].length() > 0? spl[1].charAt(0) : (lineMode? egL : (char)0x00);//egL;
        //                 egR=spl[2].length() > 0? spl[2].charAt(0) : (char)0x00;
        //             } else {
        //                 m="";
        //                 egR=(char)0x00; //whiteSpace;
        //             }
        //             hadBorder=hasBorder; hasBorder=m.length() > 0;
        //         }
        //     }
        //     sb.append("\n");
        //     return this;
        // }









    // public static class Cells {
    //     private final List<Cell> cells;
    //     private final StringBuilder sb;
    //     private final StringBuilder segb = new StringBuilder();
    //     private String[] defaultLineMarker = null;

    //     Cells(StringBuilder sb, String def) {
    //         this.sb=sb;
    //         this.cells = Arrays.stream(def  //"|  TEXTFIELD{*} ID | M {|}"
    //             .replaceAll("\\{\\|\\}", "|")
    //             .replaceAll("\\|", "@||@")
    //             .replaceAll("\\{(?<first>.?)\\}", "@|${first}@")
    //             .split("@")
    //         )
    //         .filter(s -> s.length() > 0)
    //         .peek(System.out::println)
    //         .map(s -> {
    //             boolean border = s.charAt(0)=='|';
    //             int w = s.length(); w -= border? 1 : 0; // deduct leading '|' for borders marker
    //             String label = s.replaceAll("^\\s+", "");
    //             int padL = border? 0 : w - label.length();
    //             label = s.replaceAll("\\s+$", "");
    //             int padR = border? 0 : w - label.length();
    //             char borderMarker = w > 0? s.charAt(1) : '|';
    //             // System.out.println(w);
    //             // System.out.println(String.format("w=%d, padL=%d, padR=%d", w, padL, padR));
    //             return new Cell(label, w, padL, padR, 'L', border, borderMarker);
    //         }).toList();
    //     }

    //     Cells line(String... markers) { return writeRow(true, markers); }
    //     Cells row(String... markers) { return writeRow(false, markers); }
    //     // 
    //     private Cells writeRow(boolean lineMode, String... markers) {
    //         final char whiteSpace = ' ';
    //         markers = defaultLineMarker(markers);   // {"-", "-", "-"}
    //         final int mlen = markers.length;
    //         // 
    //         var spl=splitMarker(markers[0], new String[] {"", "", ""});
    //         // System.out.println(String.format("(%s)(%s)(%s)", spl[0], spl[1], spl[2]));
    //         String m=spl[0];
    //         char egL=spl[1].length() > 0? spl[1].charAt(0) : (lineMode? '+' : cells.get(0).borderMarker());
    //         char egR=spl[2].length() > 0? spl[2].charAt(0) : (lineMode? '+' : cells.get(0).borderMarker());
    //         boolean hasBorder=m.length() > 0, hadBorder=false;
    //         int j=1;
    //         for(var cell : cells) {
    //             if(cell.isBorder()) {   // write border markers: '+' or '|'
    //                 // cell.write(true, hasBorder || hadBorder? (egL==0? (lineMode? '+' : cell.borderMarker()) : egL) : whiteSpace, null, cell.align());
    //                 cell.write(sb, true, hasBorder || hadBorder? (egL==0? (lineMode? '+' : cell.borderMarker()) : egL) : whiteSpace, null, cell.align());
    //                 // System.out.println("egL: " + cell.borderMarker());
    //                 egL = egR;
    //             } else {
    //                 if(lineMode) {      // fill line segment: '------'
    //                     cell.write(sb, true, hasBorder? m.charAt(0) : whiteSpace, null, cell.align());
    //                 } else {
    //                     cell.write(sb, false, whiteSpace, m, cell.align());
    //                 }
    //                 if(j < mlen) {
    //                     spl=splitMarker(markers[j++], new String[] {"", "", ""});
    //                     // System.out.println(String.format("(%s)(%s)(%s)", spl[0], spl[1], spl[2]));
    //                     m=spl[0];
    //                     // egL=spl[1].length() > 0? spl[1].charAt(0) : egL;
    //                     // egR=spl[2].length() > 0? spl[2].charAt(0) : (lineMode? '+' : cell.borderMarker());
    //                     egL=spl[1].length() > 0? spl[1].charAt(0) : (lineMode? egL : (char)0x00);//egL;
    //                     egR=spl[2].length() > 0? spl[2].charAt(0) : (char)0x00;
    //                 } else {
    //                     m="";
    //                     egR=(char)0x00; //whiteSpace;
    //                 }
    //                 hadBorder=hasBorder; hasBorder=m.length() > 0;
    //             }
    //         }
    //         sb.append("\n");
    //         return this;
    //     }

        // private void write(Cell cell, boolean fill, char fillChar, String text, char align) {
        //     int w = cell.w();
        //     int padL = cell.padL();
        //     int padR = cell.padR();
        //     if(w > 0) {
        //         if(fill) { //|| align=='|') {
        //             sb.append(String.valueOf(fillChar).repeat(w));
        //         } else if(text != null) {
        //             final int tlen = text.length();
        //             final var effw = Math.max(0, w - padL - padR);
        //             final var filler = String.valueOf(fillChar);
        //             // 
        //             final int pL = Math.min(w - padR, padL);
        //             if(pL > 0) sb.append(filler.repeat(pL));
        //             // 
        //             if(align=='L') {
        //                 sb.append(tlen > effw? text.substring(0, effw) :
        //                     tlen < effw? String.format("%s%s", text, filler.repeat(effw - tlen)) : text);
        //             } else {
        //                 sb.append(tlen > effw? text.substring(tlen - effw) :
        //                     tlen < effw? String.format("%s%s", filler.repeat(effw - text.length()), text) : text);
        //             }
        //             final int pR = Math.min(w, Math.min(w - pL, padR));
        //             if(pR > 0) sb.append(filler.repeat(pR));
        //         }
        //     }
        // }

        //     private String[] defaultLineMarker(String... markers) {
        //         if(markers.length==0 && defaultLineMarker==null) {
        //             defaultLineMarker = cells.stream().filter(c -> ! c.isBorder()).map(c -> "-").toArray(String[]::new);
        //         }
        //         return markers.length==0? defaultLineMarker : markers;
        //     }
        // }

/*
    static String[] splitter(String str, StringBuilder segb, String[] segs) {
        if(str==null || segb==null) {
            for(int i=0; segs != null && i < segs.length; i++) {
                segs[i] = i==0? "" : null;  // reset segs[]
            }
        } else {
            segb.setLength(0); int j=0;
            for(int ai=0; ai < str.length(); ai++) {
                char ch = str.charAt(ai);
                switch(ch) {
                    case '{': j++; segs[0] += segb.toString();
                    case '}': segs[j] = segb.toString(); segb.setLength(0); break;
                    default: segb.append(ch);
                }
            }
            segs[0] += segb.toString();
            segb.setLength(0); 
        }
        return segs;
    }

    static String[] picker(String[] segs) {
        int slen = segs.length;
        if(slen > 2 && segs[0].length()==0) {
            int i = isSpecial(segs[1])? 1 : isSpecial(segs[2])? 2 : -1;
            if(i >= 0) {
                segs[0] = String.format("{%s}", segs[i]);
                if(i==1) {
                    segs[i] = null; i+=2;
                }
                for( ; i < slen-1; i++) {
                    segs[i] = segs[i+1];
                    if(i+1==slen-1) {   // null last element with left-shift
                        segs[i+1] = null;
                    }
                }
            }
        }
        return segs;
    }

    static boolean isSpecial(String arg) {
        return arg != null && arg.equals("---");
    }
*/
}






/*
    public record Cell(
        int w, int padL, int padR, char align, char type
    ) {
        public Cell(int w, int padL, int padR, char align) { this(w, padL, padR, align, '*'); }
        public Cell(int w, char type) { this(w, 0, 0, 'L', type); }

        public Cell write(StringBuilder sb, String text, String... fill) {
            String fillchar = fill.length > 0? (fill[0].length() > 1? fill[0].substring(0, 1) : fill[0]) : " ";
            int len = text.length();
            var effw = w - padL - padR;
            if(padL > 0) sb.append(fillchar.repeat(padL));
            if(align=='L') {
                sb.append(len > effw? text.substring(0, effw) :
                    len < effw? String.format("%s%s", text, fillchar.repeat(effw - text.length())) : text);
            } else {
                sb.append(len > effw? text.substring(len - effw) :
                    len < effw? String.format("%s%s", fillchar.repeat(effw - text.length()), text) : text);
            }
            if(padR > 0) sb.append(fillchar.repeat(padR));
            return this;
        }
        public Cell fill(StringBuilder sb, String fill) {
            sb.append((fill.length() > 1? fill.substring(0, 1) : fill).repeat(w));
            return this;
        }
        public boolean isBorder() { return type=='|'; }
        public String out(StringBuilder sb) { var out = sb.toString(); sb.setLength(0); return out; }
    }

    List<Cell> cells = List.of(
        new Cell(1, '|'),
        new Cell(6, 1, 0, 'L'),
        new Cell(1, '|'),
        new Cell(12, 1, 0, 'L'),
        new Cell(1, '|'),
        new Cell(12, 1, 0, 'L'),
        new Cell(1, '|')
    );

    void line(StringBuilder sb, List<Cell> cells, String... args) {
        args = args.length > 0? args : cells.stream()
            .map(c -> c.isBorder()? "+" : "-").toArray(String[]::new);
        // 
        for(int i=0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            // if(i < args.length) {
                String arg = i < args.length && args[i].length() > 0? args[i].substring(0, 1) : " ";
                c.fill(sb, arg);
            // }
        }
        sb.append("\n");
    }

    void row(StringBuilder sb, List<Cell> cells, String... args) {
        int j=0;
        for(Cell c : cells) {
            String text = j < args.length? args[j] : "";
            if(c.isBorder()) {
                // c.fill(sb, text==null? " " : "|");
                c.fill(sb, "|");
            } else {
                if(text != null) {
                    c.write(sb, text, " ");
                } else {
                    c.fill(sb, " ");
                }
                j++;
            }
        }
        sb.append("\n");
    }

    boolean isSpecial(String arg) {
        return arg != null && arg.equals("---");
    }

    String[] picker(String[] segs) {
        int slen = segs.length;
        if(slen > 2 && segs[0].length()==0) {
            int i = isSpecial(segs[1])? 1 : isSpecial(segs[2])? 2 : -1;
            if(i >= 0) {
                segs[0] = String.format("{%s}", segs[i]);
                if(i==1) {
                    segs[i] = null; i+=2;
                }
                for( ; i < slen-1; i++) {
                    segs[i] = segs[i+1];
                    if(i+1==slen-1) {   // null last element with left-shift
                        segs[i+1] = null;
                    }
                }
            }
        }
        return segs;
    }

    void row2(StringBuilder sb, List<Cell> cells, String... args) {
        String[] template = cells.stream().map(c -> c.isBorder()? "|" : "---").toArray(String[]::new);
        String[] segs = new String[4];
        StringBuilder segb = new StringBuilder();
        int j=0;
        for(int i=0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            if( ! c.isBorder()) {
                String arg = j < args.length? args[j] : null;
                j++;
                if(arg != null) {
                    segs = splitter(null, segb, segs);  // reset st[]
                    segs = splitter(arg, segb, segs);   // split 'arg' into st[]
                    segs = picker(segs);
                    System.out.println(String.format("(%s)(%s)(%s)(%s)", segs[0], segs[1], segs[2], segs[3]));
                }
            }
        }
        // 
        for(int i=0; i < cells.size(); i++) {
            Cell c = cells.get(i);
            if(c.isBorder()) {
                c.fill(sb, template[i]);
            } else {
                String arg = template[i];
                if(arg.equals("---")) {
                    c.fill(sb, arg);
                } else {
                    c.write(sb, arg, " ");
                }
            }
        }
        sb.append("\n");
    }

    void run2(String[] args) {
        var sb = new StringBuilder();
        // 
        line(sb, cells, "+", "---", "+", "---", "+", "---", "+");
        line(sb, cells, "", "", "*", "***", "*", "", "");
        line(sb, cells, "", "", "+", "===", "+", "", "");
        line(sb, cells, "", "", "=", "===", "=", "", "");
        line(sb, cells, "", "", "", "===", "", "", "");
        line(sb, cells, "+", "---", "+", "", "+", "---", "+");
        line(sb, cells);
        // row(sb, cells, "A", "+ +", "| |", "DEF");
        row2(sb, cells, "{*}{---}{#}", "{}{}", "{---}");
        line(sb, cells);
        System.out.println(sb.toString());
    }

    void run3(String[] args) {
        System.out.println("Hello, TableFormatter");
        var tf = TableFormatter.builder()
            .columns("| ID | NAME | CONTACT |")
            .alignments("L")
            .widths(6, 12, 12)
            .build();
        // 
        tf.line()
            .row("{label}", "{label}", "{label}")
            .line()
            .row("ABCD", "B", "C")
            .line("", "", "")
            .line(" ", "", "")
            .line(" ", " ", "")
            .line(" ", " ", " ")
            .line("", " ", " ")
            .line(" ", "", "")
            .line("", " ", "")
            .line("", "", " ")
            .line(" ", "", " ")
            // .row("", "{-R}total:", "{-}========={-}")
            .row("", "", "642.70")
            .line("", "", " ")
            .print(System.out);
    }
}
*/
    // void row2(StringBuilder sb, List<Cell> cells, String... args) {
    //     int i = 0;
    //     int p1=-1; int q1=-1; int p2=-1; int q2=-1;
    //     String f1=""; String f2=""; Cell c0=null; Cell c1=null;
    //     // 
    //     for(Cell c : cells) {
    //         String arg = i < args.length? args[i] : "";
    //         // 
    //         p1 = arg.length() > 0 && arg.charAt(0)=='{'? 1 : -1;
    //         q1 = p1 >= 0? arg.indexOf('}') : -1;
    //         f1 = q1 >= 0? arg.substring(p1, q1) : "";
    //         // 
    //         p2 = arg.length() > 0 && arg.charAt(arg.length()-1)=='}'? arg.length()-2 : -1;
    //         q2 = p2 >= 0? arg.lastIndexOf("{")+1 : -1;
    //         f2 = q2 >= 0? arg.substring(p2, q2) : ">";
    //         // 
    //         if(c.isBorder()) {
    //             // c.fill(sb, f2);
    //             c0 = c;
    //         } else {
    //             if(c0 != null) {
    //                 c0.fill(sb, f1);
    //             }
    //             String text = q2 >= 0? arg.substring(0, p2-1) : arg;
    //             text = q1 >= 0? text.substring(q1+1) : text;
    //             c.write(sb, text, " ");
    //             i++;
    //         }
    //     }
    //     sb.append("\n");
    // }

    // void row2(StringBuilder sb, List<Cell> cells, String... args) {
    //     int i = 0;
    //     int p1=-1; int q1=-1; int p2=-1; int q2=-1;
    //     String f1=""; String f2=""; Cell c0=null; Cell c1=null;
    //     // 
    //     for(Cell c : cells) {
    //         String arg = i < args.length? args[i] : "";
    //         // 
    //         p1 = arg.length() > 0 && arg.charAt(0)=='{'? 1 : -1;
    //         q1 = p1 >= 0? arg.indexOf('}') : -1;
    //         f1 = q1 >= 0? arg.substring(p1, q1) : "";
    //         // 
    //         p2 = arg.length() > 0 && arg.charAt(arg.length()-1)=='}'? arg.length()-2 : -1;
    //         q2 = p2 >= 0? arg.lastIndexOf("{")+1 : -1;
    //         f2 = q2 >= 0? arg.substring(p2, q2) : ">";
    //         // 
    //         if(c.isBorder()) {
    //             // c.fill(sb, f2);
    //             c0 = c;
    //         } else {
    //             if(c0 != null) {
    //                 c0.fill(sb, f1);
    //             }
    //             String text = q2 >= 0? arg.substring(0, p2-1) : arg;
    //             text = q1 >= 0? text.substring(q1+1) : text;
    //             c.write(sb, text, " ");
    //             i++;
    //         }
    //     }
    //     sb.append("\n");
    // }
