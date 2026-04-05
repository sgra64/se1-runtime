package tableformatter;

import java.util.List;

public class Driver_222 {
    public static void main(String[] args) {
        var application = new Driver_222();
        application.run(args);
    }

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
            String arg = i < args.length && args[i].length() > 0? args[i].substring(0, 1) : " ";
            c.fill(sb, arg);
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

    private String[] splitter(String str, StringBuilder segb, String[] segs) {
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

    private boolean isSpecial(String arg) {
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

    public void run(String[] args) {
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

    void run2(String[] args) {
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
