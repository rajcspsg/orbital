import java.awt.*;
import java.io.*;
import lib.awt.NumberInput;
import lib.AppletFrame;
//import MathFunction;

public class MathPlot extends java.applet.Applet {
	public static void main(String args[]) {
    	lib.AppletFrame.startApplet("MathPlot","Mathematical Plotting Engine",args);
    }

    GraphCanvas gcanvas;
    GraphRange range;
    public void init() {
        resize(400, 400);
        setBackground(Color.white);
        range = new GraphRange( -10,+10, -10,+10 );
        gcanvas = new GraphCanvas( range, 0.1, reCompileFunction() );

        setLayout( new BorderLayout() );
        add("North", new Button("Quit") );
        Panel GraphControl = new Panel();
        GraphControl.setLayout( new BorderLayout() );
        GraphControl.add("North", new Button("^") );
        GraphControl.add("South", new Button("v") );
        GraphControl.add("West", new Button("<") );
        GraphControl.add("East", new Button(">") );
        GraphControl.add("Center", gcanvas );
        add("Center", GraphControl );
        Panel MathControl = new Panel();
        MathControl.setLayout( new FlowLayout(FlowLayout.LEFT,10,10) );
        MathControl.add( new TextField(20) );
        MathControl.add( new NumberInput(new Double(0.1)) );
        add("South", MathControl );
    }
    public void paint(Graphics g) {
        System.out.println("painting Applet");
        paintComponents(g);
    }

    public boolean mouseDown(Event evt,int x,int y) {
        evt.translate(-gcanvas.bounds().x,-gcanvas.bounds().y);
System.err.println(""+x+"|"+y+"-->"+evt.x+"|"+evt.y+" E "+gcanvas.bounds());
        double scal = (evt.modifiers/*&Event.SHIFT_MASK*/)!=0 ? 2.0 : 0.5;
        double dx = range.x.max-range.y.min+1;
        double dy = range.y.max-range.y.min+1;
System.err.println("Rng: "+range.x.min+".."+range.x.max+"|"+range.y.min+".."+range.y.max+"\tPrc: "+gcanvas.precision);
        double m_x = gcanvas.vx(evt.x);
        double m_y = gcanvas.vy(evt.y);
System.err.println("Mid: "+m_x+"|"+m_y+" siz: "+dx+"|"+dy+" -->");
        dx *= scal; dy *= scal;
System.err.println("Mid: "+m_x+"|"+m_y+" siz: "+dx+"|"+dy);
        range.x.min = m_x - dx/2;
        range.x.max = m_x + dx/2;
        range.y.min = m_y - dy/2;
        range.y.max = m_y + dy/2;
        gcanvas.setRange( range );
        gcanvas.setPrecision( gcanvas.precision * scal );
System.err.println("Rng: "+range.x.min+".."+range.x.max+"|"+range.y.min+".."+range.y.max+"\tPrc: "+gcanvas.precision);
        return true;
    }
    public boolean action(Event evt,Object arg) {
        if ( "Quit".equals(arg) ) {
            System.exit(1);
            return true;
        }
        if ( evt.target instanceof Button ) {
            if ( "^".equals(arg) )
                {range.y.max+=0.5;range.y.min-=0.5;}
            else if ( "v".equals(arg) )
                {range.y.max-=0.5;range.y.min+=0.5;}
            else if ( "<".equals(arg) )
                {range.x.max-=0.5;range.x.min+=0.5;}
            else if ( ">".equals(arg) )
                {range.x.max+=0.5;range.x.min-=0.5;}
            else
            	return super.action( evt,arg );
            gcanvas.setRange( range );
        }
        if ( evt.target instanceof TextField ) {
            if ( ! "".equals(arg) )
              try {
                FileOutputStream ofs = new FileOutputStream("MathCurFunction.java");
                DataOutput os = new DataOutputStream( ofs );
                os.writeBytes("/*\n * auto generated by MathPlot\n */\n\n");
                os.writeBytes("public class MathCurFunction implements MathFunction {\n    public double function(double x) {\n        return ");
                os.writeBytes((String)arg);
                os.writeBytes(";\n    }\n}");
                gcanvas.setFunction( reCompileFunction() );
            } catch(IOException x) {x.printStackTrace();}
            return true;
        }
        if ( evt.target instanceof NumberInput ) {
            double prec = ((Double)arg).doubleValue();
            if ( prec != 0.0 ) gcanvas.setPrecision( prec );
        }
        return super.action(evt,arg);
    }

    private MathFunction reCompileFunction() {
     try {
        Class funcClass;
//        funcClass = Class.forName("MathCurFunction");
/*        {
            Process proc = Runtime.getRuntime().exec("javac MathCurFunction.java");
            proc.waitFor();
            if( proc.exitValue()!=0 )
                System.out.println("exec malreturned");
/*          //Compiler.enable();
            if( !Compiler.compileClass( funcClass ) ) {
                System.out.println(funcClass.toString()+" not compiled");
            }
            Compiler.enable();
            Compiler.disable();
        }*/
        funcClass = Class.forName("MathCurFunction");
        MathFunction func = (MathFunction)funcClass.newInstance();
        return func;
     } catch(Exception x) {x.printStackTrace();/*showStatus("Fehlerhafte Funktion");*/}
     return null;
    }
}


/**
 * Structs containing a summoned representation of a Range within a Graph.
 */
class GrValueRange {
    double min;
    double max;
    GrValueRange(double min,double max) {
        this.min = min;
        this.max = max;
    }
}
class GraphRange {
    GrValueRange x;
    GrValueRange y;
    GraphRange(GrValueRange x,GrValueRange y) {
        this.x=x;
        this.y=y;
    }
    GraphRange(double xmin,double xmax,double ymin,double ymax) {
        this( new GrValueRange(xmin,xmax),new GrValueRange(ymin,ymax) );
    }
}

/**
 * a Canvas displaying a Graph within a determined GraphRange.
 */
class GraphCanvas extends Canvas {
    double precision;
    GraphRange range;
    MathFunction function;
    public GraphCanvas(GraphRange rng,double precision,MathFunction function) {
        this.range = rng;
        this.precision = precision;
        this.function = function;
    }
    public void setRange(GraphRange rng) {
        this.range = rng;
        repaint();
    }
    public void setPrecision(double precision) {
        this.precision = precision;
        repaint();
    }
    public void setFunction(MathFunction function) {
        this.function = function;
        repaint();
    }

    private double xscal;
    private double yscal;
    private int dx;
    private int dy;
    int sx(double xv) {
        return (int)( xscal * (+xv-range.x.min) );
    }
    int sy(double yv) {      // swap y-coord up/down
        return (int)( yscal * (-yv-range.y.min) );
    }
    double vx(int sx) {
        return +( (double)sx/xscal + range.x.min );
    }
    double vy(int sy) {     //??? swap
        return ( (double)sy/yscal + range.y.min );
    }

    public void paint(Graphics g) {
        Dimension d=size();
        g.setColor(Color.blue);
        g.drawRect(0,0,d.width,d.height);

        dx = (int)(range.x.max-range.x.min)+1;
        dy = (int)(range.y.max-range.y.min)+1;
        xscal =  (double)d.width  / dx;
        yscal =  (double)d.height / dy;
        //g.translate(sx(-range.x.min),sy(-range.y.min));

        drawCoords(g);

        drawFunc(g,function);
    }
    private void drawFunc(Graphics g,MathFunction f) {
        g.setColor(Color.black);
        double x,y;
        for(x=range.x.min;x<range.x.max;x+=precision) {
            y = f.function(x);
            g.drawLine( sx(x),sy(y), sx(x+precision),sy(f.function(x+precision)) );
        }
    }
    private void drawCoords(Graphics g) {
        g.setColor(Color.darkGray);
        g.drawLine(sx(range.x.min),sy(0), sx(range.x.max),sy(0));
        for(double x=range.x.min;x<range.x.max;x++) {
          g.drawLine(sx(x),sy(0)-1, sx(x),sy(0)+1);
        }
        g.drawLine(sx(0),sy(range.y.min), sx(0),sy(range.y.max));
        for(double y=range.y.min;y<range.y.max;y++) {
          g.drawLine(sx(0)-1,sy(y), sx(0)+1,sy(y));
        }
    }
}
