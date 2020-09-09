package utils;

import entities.Line;
import entities.Rectangle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;

import static utils.Constants.*;
import static utils.Constants.FRAME_WIDTH;

public class RectangleService {
    private static List<Rectangle> rectangles;

    private static List<Rectangle> placedRectangles;
    
    private static List<Line> lines;

    private static BlockingQueue<List<Rectangle>> linesDeque = new LinkedBlockingDeque<>();

    private static int currentLineHeight;

    private static ExecutorService rectangleService;

    private static Object lock = new Object();

    public static List<Rectangle> runRectanglePlacement(int threadNr, int rectangleNr) {
        rectangleService = Executors.newFixedThreadPool(threadNr);
        generateRectangles(rectangleNr);
        splitWork(threadNr);
        placeRectangles();

        return placedRectangles;
    }

    private static void placeRectangles() {
        placedRectangles = new ArrayList<>();

        while(!linesDeque.isEmpty()){
            Future<String> futureMessage = rectangleService.submit(() -> {
                try {
                    processLine(linesDeque.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return "Thread " + Thread.currentThread().getId() + " sorted the rectangles";
            });

            try {
                futureMessage.get();
                System.out.println( futureMessage.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        awaitTerminationAfterShutdown(rectangleService);
    }

    private static void splitWork(int threadNumber) {
        int splitRectanglesSize = rectangles.size() / threadNumber;
        List<Rectangle> splitRectanglesAL = new ArrayList<>();
        currentLineHeight = 0;
        lines = new CopyOnWriteArrayList<>();

        for(int i = 0; i < rectangles.size(); i++){
            if(i!=0 && i % splitRectanglesSize == 0){
                linesDeque.add(splitRectanglesAL);
                splitRectanglesAL = new ArrayList<>();
            }

            splitRectanglesAL.add(rectangles.get(i));

            if(i == rectangles.size() - 1){
                linesDeque.add(splitRectanglesAL);
            }
        }
    }

    private static Line createNewLine(List<Rectangle> splitRectangles, int lineIndex) {
        Line line = new Line();

        line.setHeight(currentLineHeight);
        currentLineHeight += splitRectangles.get(0).getHeight();
        line.setIndex(lineIndex);
        lines.add(line);

        return line;
    }

    private static void generateRectangles(int nrOfRectangles){
        rectangles = new ArrayList<>();

        for(int i=1; i<= nrOfRectangles; i++){
            rectangles.add(new Rectangle( i,
                    (int) (MIN_RECTANGLE_WIDTH + (Math.random() * (MAX_RECTANGLE_WIDTH - MIN_RECTANGLE_WIDTH))),
                    (int) (MIN_RECTANGLE_HEIGHT + (Math.random() * (MAX_RECTANGLE_HEIGHT - MIN_RECTANGLE_HEIGHT)))
            ));
        }

        Collections.sort(rectangles, new HeightComparator());
    }

    private static void processLine(List<Rectangle> retrievedRectangles){
        createNewLine(retrievedRectangles, 0);
        while(retrievedRectangles.size() > 0){
            for(int i=0; i < retrievedRectangles.size(); i++){
                for(int j = 0; j < lines.size(); j++){
                    if(placeRectangle(retrievedRectangles.get(i), lines.get(j))){
                        retrievedRectangles.remove(retrievedRectangles.get(i));
                        break;
                    }else{
                        if(j == lines.size() - 1){
                            int lineIndex = lines.get(j).getIndex() + 1;
                            createNewLine(retrievedRectangles, lineIndex);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static boolean placeRectangle(Rectangle rectangle, Line line){
        synchronized (lock){
            if(rectangle.getWidth() < line.getRemainingWidth() ){
                rectangle.setY(line.getHeight());
                rectangle.setX(FRAME_WIDTH - line.getRemainingWidth());

                line.setRemainingWidth( line.getRemainingWidth() - rectangle.getWidth() );

                placedRectangles.add(rectangle);
                return rectangles.remove(rectangle);
            }

            return false;
        }
    }

    public static void awaitTerminationAfterShutdown(ExecutorService threadPool) {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
