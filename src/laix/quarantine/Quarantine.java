package laix.quarantine;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Quarantine {

    private static volatile int i = 0;
    private static volatile int le = 0;

    private static String pathSrcText = "source.txt";
    private static String pathCleanSrcText = "clean.txt";
    private static String pathCleanText = "result.txt";

    private static volatile Character ch;
    private static volatile Queue<Character> fifo = new LinkedList<Character>();

    private static volatile boolean isLive = true;
    private static boolean isBuffered = true;

    private static final Object monitor = new Object();

    public static void main(String[] args) throws IOException {
        Thread myThread1 = new Thread(new Runnable() {
            String s = "";
            public void run() //one thread
            {
                try {
                    s = FileHelper.getString(pathSrcText);
                } catch (IOException ex) {
                    Logger.getLogger(Quarantine.class.getName()).log(Level.SEVERE, null, ex);
                }
                le = s.length();
                synchronized (monitor) {
                    monitor.notify();
                }
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Quarantine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                while (isLive) {
                    if (i < s.length()) {
                        try {
                            s = FileHelper.getString(pathSrcText);
                        } catch (IOException ex) {
                            Logger.getLogger(Quarantine.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        ch = new Character(s.charAt(i));
                        synchronized (monitor) {
                            if (isBuffered) //buffer
                                fifo.add(ch);
                        }
                    }
                    i++;
                    Thread.yield();
                }
            }
        });

        Thread myThread2 = new Thread(new Runnable() {
            Character tmp;
            String s = "";
            String result;

            public void run() //two thread
            {
                synchronized (monitor) {
                    try {
                        monitor.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Quarantine.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                synchronized (monitor) {
                    monitor.notify();
                }

                while (isLive) {
                    try {
                        s = FileHelper.getString(pathSrcText);
                    } catch (IOException ex) {
                        Logger.getLogger(Quarantine.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    tmp = ch;
                    //get true simbols from buffer
                    synchronized (monitor) {
                        if (isBuffered && fifo.size() > 0) {
                            if (fifo.peek().toString().matches("[A-Za-z0-9 ]")) {
                                if (result == null) {
                                    result = fifo.poll().toString();
                                } else {
                                    result += fifo.poll().toString();
                                }
                            } else {
                                fifo.poll();
                            }
                        }
                    }
                    if (!isBuffered && i < le && tmp != null) {
                        if (tmp.toString().matches("[A-Za-z0-9 ]"))
                            result += tmp.toString();
                    } else if (i > le && ( fifo.size() == 0 || !isBuffered)) {
                        //запись чистого текста и проверкуа на потери
                        FileHelper.writeString(result, pathCleanText, false);
                        try {
                            System.out.println("res=" + result.length() + " src=" + FileHelper.getString(pathCleanSrcText).length() + " " + result);
                            System.out.println("" + ((float) result.length() / (float)FileHelper.getString(pathCleanSrcText).length() * 100) + "%");
                        } catch (IOException ex) {
                            Logger.getLogger(Quarantine.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        isLive = false;
                    }
                    Thread.yield();
                }
            }
        });

        myThread2.start();
        myThread1.start();
    }
}
