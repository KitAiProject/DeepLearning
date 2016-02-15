
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;




public class Main extends JFrame implements ActionListener {

    /**
     * @author Tomohiro Ueno,Hayashi Hiroki
     * @version 10
     */

    public static final double VER = 7.02;
    public static int X = 28;
    public static int Y = 28;
    public static int SAMPLE = 30;
    public static double ETA = 0.01;
    public static double NMIN = 0.8;
    public static int LEARNING_P = 100;

    public static final int B = 20;
    public static final int CHARACTER = 10;
    public static final char[] CS = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
    public static final int WSX = B * X;
    public static final int WSY = B * Y;
    private static double[] test = new double[X * Y];
    public static final double answer[][] = {
            {0, 0, 0, 0},
            {0, 0, 0, 1},
            {0, 0, 1, 0},
            {0, 0, 1, 1},
            {0, 1, 0, 0},
            {0, 1, 0, 1},
            {0, 1, 1, 0},
            {0, 1, 1, 1},
            {1, 0, 0, 0},
            {1, 0, 0, 1},
    };

    static final int LAYERNUM = 3;
    static final int MIDUNIT_NUM = X * Y;
    JPanel p1 = new JPanel();
    JPanel p2 = new JPanel();
    JButton jb1 = new JButton("Enter");
    JButton[] jb = new JButton[X * Y];
    double[] in = new double[CHARACTER];
    boolean learning_f = false;
    NetworkState state;

    Main() {


        message("");
        message("Neural Network - v" + VER + "");
        read_config();
        double[][][] teacher = new double[CHARACTER][SAMPLE][X * Y];//SAMPLE?¿½Í‚Ç‚ï¿½MNIST?¿½?¿½?¿½@CHARACTER?¿½Í‚Ç‚Ì•ï¿½?¿½?¿½?¿½?¿½?¿½@X*Y?¿½Í‰ï¿½f

        jb1.setFont(new Font(null, Font.PLAIN, 28));
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setTitle("Perceptron");
        setResizable(false);
        p1.setLayout(new GridLayout(1, 1));
        p2.setLayout(new GridLayout(Y, X));
        for (int i = 0; i < X * Y; i++) {
            jb[i] = new JButton();
            jb[i].addActionListener(this);
            jb[i].setBackground(Color.WHITE);
            p2.add(jb[i]);
        }
        p1.add(jb1);
        jb1.addActionListener(this);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        getContentPane().add(p2);
        getContentPane().add(BorderLayout.NORTH, p1);
        setSize(WSX, WSY);
        setVisible(true);
        double[] out = new double[X*Y];

        message("Roading Data [ " + SAMPLE + " ]");
        System.out.print(" ");
        for (int n = 0; n < CS.length; n++) {
            for (int i = 0; i < SAMPLE; i++) {
                try {
                    teacher[n][i] = read_img(n, i);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
            System.out.print("#");
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
            System.out.print("#");
        }
        message("");

        state = new NetworkState(teacher, LAYERNUM, MIDUNIT_NUM, answer, CS);

        for (int i = 0; i < test.length; i++) {
            test[i] = 0;
        }

        message("Learning START [ (" + X + "?¿½~" + Y + ")?¿½~" + CHARACTER + "?¿½~" + SAMPLE + "?¿½~" + LEARNING_P + " ]");
        System.out.print(" ");

        state.BackPropagationCalc();
        message("");
        message("Learning END");
        message("");
        learning_f = true;

    }

    public static void message(String str) {
        System.out.println(" " + str);
    }

    public static boolean isNumber(String num) {
        try {
            Integer.parseInt(num);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static void main(String[] args) {
        new Main();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!learning_f) {
            return;
        }


        try {
            test = read_img2();
        } catch (Exception ev) {
            System.out.println("[Error-0]" + ev.getMessage());
        }

        //double[] outnum = new double[answer[0].length];

        state.calc(test,true);
        /*
        try {
        	state.calc(test,true);
        } catch (java.lang.NullPointerException npe) {
        	System.out.println("[Error-1]" + npe.getMessage());
        }
        */
        //state.calc(test);

        //message(" This is ["+CS[bToD(output)]+"].");
        //message("This is ["+CS[printPercent(output)]+"]");

        int num=0;
        for(int i=0;i<answer[0].length;i++){
        	//System.out.println(state.outnum[i]);
        	/*
        	try {
        		System.out.printf("output[%d]: %4.2f\n",i,state.outnum[i]);
        	} catch(java.lang.NullPointerException npe) {
            	System.out.println("[Error-1]" + npe.getMessage());
        	}
        	*/
        }

        for (int i = 0; i < jb.length; i++) {
            if (e.getSource() == jb[i]) {
                test[i] = (test[i] + 1) % 2;
                if (test[i] == 0) {
                    jb[i].setBackground(Color.WHITE);
                } else {
                    jb[i].setBackground(Color.BLACK);
                }
            }
        }
        for (int i = 0; i < jb.length; i++) {
            if (test[i] == 0) {
                jb[i].setBackground(Color.WHITE);
            } else {
                jb[i].setBackground(Color.BLACK);
            }
        }
    }

    public double[] read_img(int num, int page) throws IOException {
        double[] in = new double[X * Y];
        File f = new File("src/MINIST/" + String.valueOf(num) + "_" + String.valueOf(page) + ".bmp");
        BufferedImage read = ImageIO.read(f);

        for (int i = 0; i < Y; i++) {
            for (int j = 0; j < X; j++) {
                int c = read.getRGB(j, i);
                if ((c >> 16 & 0xff) < 128) {
                    in[j + X * i] = 1;
                } else {
                    in[j + X * i] = 0;
                }
            }
        }
        return in;

    }

    int printPercent(double[] outputArray) {
        int num = 0;
        for (int i = 0; i < 10; i++) {

            if (outputArray[i] > outputArray[num]) {
                num = i;
            }
        }
        return num;
    }

    public double[] read_img2() throws IOException {
        double[] in = new double[X * Y];
        File f = new File("src/input.bmp");
        BufferedImage read = ImageIO.read(f);

        for (int i = 0; i < Y; i++) {
            for (int j = 0; j < X; j++) {
                int c = read.getRGB(j, i);
                if ((c >> 16 & 0xff) < 128) {
                    in[j + X * i] = 1;
                } else {
                    in[j + X * i] = 0;
                }
            }
        }
        return in;

    }

    public void read_config() {
        String str = "";
        File f = new File("Per_config.ini");

        try {
            if (checkBeforeReadfile(f)) {
                message("");
                message("[Config Data]");
                try {
                    Thread.sleep(500);
                } catch (Exception e) {
                }
                BufferedReader br = new BufferedReader(new FileReader(f));
                while ((str = br.readLine()) != null) {
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                    }
                    if (str.indexOf("X") == 0) {
                        X = Integer.parseInt(str.substring(str.indexOf("=") + 1));
                        message("X=" + X);
                    }
                    if (str.indexOf("Y") == 0) {
                        Y = Integer.parseInt(str.substring(str.indexOf("=") + 1));
                        message("Y=" + Y);
                    }
                    if (str.indexOf("SAMPLE") == 0) {
                        SAMPLE = Integer.parseInt(str.substring(str.indexOf("=") + 1));
                        message("SAMPLE=" + SAMPLE);
                    }
                    if (str.indexOf("ETA") == 0) {
                        ETA = Double.parseDouble(str.substring(str.indexOf("=") + 1));
                        message("ETA=" + ETA);
                    }
                    if (str.indexOf("LEARNING") == 0) {
                        LEARNING_P = Integer.parseInt(str.substring(str.indexOf("=") + 1));
                        message("LEARNING=" + LEARNING_P);
                    }
                    if (str.indexOf("NMIN") == 0) {
                        NMIN = Double.parseDouble(str.substring(str.indexOf("=") + 1));
                        message("NMIN=" + NMIN);
                    }
                }
                message("");
                br.close();
            } else {
                return;
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            System.out.println(e);
        }

    }

    private static boolean checkBeforeReadfile(File file) {
        if (file.exists()) {
            if (file.isFile() && file.canRead()) {
                return true;
            }
        }
        return false;
    }

    public int bToD(double[] ans) {
        int decimal = 0;
        int base = 1;
        for (int i = ans.length - 1; i >= 0; i--) {
            int num;
            if (ans[i] > 0.5) {
                num = 1;
            } else {
                num = 0;
            }
            decimal += num * base;
            base *= 2;
        }
        return decimal;
    }


}
